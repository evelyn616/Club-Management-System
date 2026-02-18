<template>
    <div class="activity-list-container">
        <h1>活動列表</h1>
        
         
        <!--搜尋和篩選區-->
        <div class="search-filter">
            <!-- 基本搜尋 -->
             <div class="basic-search">
                <input v-model="searchKeyword" type="text" placeholder="搜尋活動..." @input="handleSearch" class="search-input" />
                <button @click="handleSearch" class="search-btn">🔍</button>

             </div>

          
            <!-- 進階篩選 -->
             <div class="filters">
                <!--活動類型-->
                <div class="filter-group">
                    <label for="activityTypeFilter">活動類型:</label>
                    <select v-model="selectedActivityType" id="activityTypeFilter" @change="handleFilterChange">
                        <option value="">全部</option>
                        <option value="REGULAR">社課</option>
                        <option value="SPECIAL">特殊活動</option>
                        <option value="TRAINING">團練</option>
                        <option value="PERFORMANCE">演出</option>
                        <option value="COMPETITION">比賽</option>
                    </select>
                </div>
                <!--舞風標籤-->
                <div class="filter-group">
                    <label for="danceStyleFilter">舞風標籤:</label>
                    <select v-model="selectedDanceStyle" id="danceStyleFilter" @change="handleFilterChange">
                        <option value="">全部</option>
                        <option v-for="style in danceStyles" :key="style" :value="style">{{ style }}</option>
                    </select>
                </div>
                <!--活動狀態篩選-->
                <div class="filter-group">
                    <label>活動狀態：</label>
                    <select v-model="selectedStatus" @change="handleFilterChange">
                        <option value="">全部</option>
                        <option value="DRAFT">草稿</option>
                        <option value="SCHEDULE">預約發布</option>
                        <option value="PUBLISHED">已發布</option>
                        <option value="COMPLETED">已完成</option>
                        <option value="CANCELLED">已取消</option>
                    </select>
                </div>
                
                <button 
                    @click="openAdvancedFilters" 
                    class="btn-advanced-filters"
                    :class="{ 'has-filters': hasAdvancedFilters }"
                >
                    <span class="icon">⚙️</span>
                    <span>進階篩選</span>
                    <span v-if="hasAdvancedFilters" class="filter-badge">{{ advancedFilterCount }}</span>
                </button>
            <!--清除篩選按鈕-->
            <button v-if="hasActiveFilters" @click="clearFilters" class="clear-btn">清除篩選</button>
             </div>

        </div>
    
        <p v-if="loading">載入中...</p>
        
         
         <button @click="router.push({ name: 'create-activity-container' })" class="btn-create">
        建立活動
      </button>

         
      <!-- 顯示活動列表 -->
         <table class="activities-table">
            <thead>
                <tr>
                    <th>活動ID</th>
                    <th>活動標題</th>
                    <th>開始時間</th>
                    <th>結束時間</th>
                    <th>地點</th>
                    <th>活動類型</th>
                    <th>費用</th>
                    <th>狀態</th>
                    <th>操作</th>

                </tr>
            </thead>
            <tbody>
                <tr v-for="activity in filteredActivities" :key="activity.id">
                    <td>{{ activity.id }}</td>
                    <td>{{ activity.title }}</td>
                    <td>{{ formatDateTime(activity.startTime) }}</td>
                    <td>{{ formatDateTime(activity.endTime) }}</td>
                    <td>{{ activity.location }}</td>
                    <td>{{ getActivityTypeLabel(activity.activityType) }}</td>
                    <td>{{ activity.feeAmount }}</td>
                    <td><span class="status-badge" :class="activity.status.toLowerCase()">{{ getStatusLabel(activity.status) }}</span></td>
                    <!--操作按鈕-->
                    <td class="col-actions">
                        <div class="action-buttons">
                        <!-- 草稿:可以編輯、發布、刪除 -->
                            <template v-if="activity.status === 'DRAFT'">
                                <button 
                                @click="updateActivity(activity.id)" class="btn-action btn-edit"title="編輯草稿">
                                <span class="icon">✏️</span>
                                <span>編輯</span>
                                </button>
                                
                                <button @click="goToPublish(activity.id)"class="btn-action btn-publish"title="發布活動">
                                <span class="icon">🚀</span>
                                <span>發布</span>
                                </button>
                                
                                <button @click="deleteDraft(activity.id, activity.title)" class="btn-action btn-delete"title="刪除草稿"
                                >
                                <span class="icon">🗑️</span>
                                <span>刪除</span>
                                </button>
                            </template>

                        <!-- 預約發布狀態 -->
                            <template v-else-if="activity.status === 'SCHEDULE'">
                                <button 
                                    @click="updateActivity(activity.id)" 
                                    class="btn-action btn-edit"
                                    title="編輯活動"
                                >
                                    <span class="icon">✏️</span>
                                    <span>編輯</span>
                                </button>
                                
                                <button 
                                    @click="showCancelScheduledDialog(activity)" 
                                    class="btn-action btn-cancel"
                                    title="取消活動"
                                >
                                    <span class="icon">❌</span>
                                    <span>取消預約發布</span>
                                </button>
                            </template>

                        <!-- 已發布狀態 -->
                            <template v-else-if="activity.status === 'PUBLISHED'">
                                <button 
                                    @click="updateActivity(activity.id)" 
                                    class="btn-action btn-edit"
                                    title="編輯活動"
                                >
                                    <span class="icon">✏️</span>
                                    <span>編輯</span>
                                </button>
                                
                                <button 
                                    @click="showCancelActivityDialog(activity)" 
                                    class="btn-action btn-cancel"
                                    title="取消活動"
                                >
                                    <span class="icon">❌</span>
                                    <span>取消</span>
                                </button>
                            </template>

                        

                        
                        </div>
                       
                    </td>
                </tr>
            </tbody>
         </table>

          <!-- ✅ 進階篩選彈出視窗 -->
        <div v-if="showAdvancedModal" class="modal-overlay" @click="closeAdvancedFilters">
            <div class="modal-content advanced-filters-modal" @click.stop>
                <!-- 視窗標題 -->
                <div class="modal-header">
                    <h2>⚙️ 進階篩選設定</h2>
                    <button @click="closeAdvancedFilters" class="btn-close">✕</button>
                </div>

                <!-- 視窗內容 -->
                <div class="modal-body">
                    <!-- 時間篩選區 -->
                    <div class="filter-section">
                        <h3 class="section-title">📅 時間篩選</h3>
                        
                        <!-- 快速時間範圍 -->
                        <div class="quick-filters">
                            <button 
                                v-for="range in quickTimeRanges" 
                                :key="range.value"
                                @click="applyQuickTimeRange(range.value)"
                                class="quick-filter-btn"
                                :class="{ active: tempFilters.activeQuickRange === range.value }"
                            >
                                {{ range.label }}
                            </button>
                        </div>

                        <!-- 自訂日期範圍 -->
                        <div class="date-range-group">
                            <div class="date-input-group">
                                <label>開始日期:</label>
                                <input 
                                    type="date" 
                                    v-model="tempFilters.dateFilter.startDate"
                                    class="date-input"
                                />
                            </div>

                            <div class="date-separator">→</div>

                            <div class="date-input-group">
                                <label>結束日期:</label>
                                <input 
                                    type="date" 
                                    v-model="tempFilters.dateFilter.endDate"
                                    :min="tempFilters.dateFilter.startDate"
                                    class="date-input"
                                />
                            </div>
                        </div>

                        <!-- 日期篩選模式 -->
                        <div class="date-mode-group">
                            <label class="mode-label">篩選依據:</label>
                            <div class="radio-options">
                                <label class="radio-option">
                                    <input type="radio" value="startTime" v-model="tempFilters.dateFilterMode" />
                                    <span>活動開始時間</span>
                                </label>
                                <label class="radio-option">
                                    <input type="radio" value="endTime" v-model="tempFilters.dateFilterMode" />
                                    <span>活動結束時間</span>
                                </label>
                                <label class="radio-option">
                                    <input type="radio" value="createdAt" v-model="tempFilters.dateFilterMode" />
                                    <span>創建時間</span>
                                </label>
                            </div>
                        </div>
                    </div>

                    <!-- 排序方式 -->
                    <div class="filter-section">
                        <h3 class="section-title">📊 排序方式</h3>
                        
                        <div class="sort-options">
                            <label class="sort-option">
                                <input type="radio" value="startTime-desc" v-model="tempFilters.sortOrder" />
                                <div class="option-content">
                                    <span class="option-icon">📅↓</span>
                                    <div class="option-text">
                                        <span class="option-title">開始時間 (新→舊)</span>
                                        <span class="option-desc">最近的活動優先</span>
                                    </div>
                                </div>
                            </label>

                            <label class="sort-option">
                                <input type="radio" value="startTime-asc" v-model="tempFilters.sortOrder" />
                                <div class="option-content">
                                    <span class="option-icon">📅↑</span>
                                    <div class="option-text">
                                        <span class="option-title">開始時間 (舊→新)</span>
                                        <span class="option-desc">最早的活動優先</span>
                                    </div>
                                </div>
                            </label>

                            <label class="sort-option">
                                <input type="radio" value="created-desc" v-model="tempFilters.sortOrder" />
                                <div class="option-content">
                                    <span class="option-icon">🆕↓</span>
                                    <div class="option-text">
                                        <span class="option-title">創建時間 (新→舊)</span>
                                        <span class="option-desc">最新創建的優先</span>
                                    </div>
                                </div>
                            </label>

                            <label class="sort-option">
                                <input type="radio" value="created-asc" v-model="tempFilters.sortOrder" />
                                <div class="option-content">
                                    <span class="option-icon">🆕↑</span>
                                    <div class="option-text">
                                        <span class="option-title">創建時間 (舊→新)</span>
                                        <span class="option-desc">最早創建的優先</span>
                                    </div>
                                </div>
                            </label>

                            <label class="sort-option">
                                <input type="radio" value="title-asc" v-model="tempFilters.sortOrder" />
                                <div class="option-content">
                                    <span class="option-icon">🔤</span>
                                    <div class="option-text">
                                        <span class="option-title">標題 (A→Z)</span>
                                        <span class="option-desc">按字母順序排列</span>
                                    </div>
                                </div>
                            </label>
                        </div>
                    </div>

                    <!-- 其他篩選 -->
                    <div class="filter-section">
                        <h3 class="section-title">🎯 其他條件</h3>
                        
                        <div class="checkbox-options">
                            <label class="checkbox-option">
                                <input type="checkbox" v-model="tempFilters.showFullOnly" />
                                <span>只顯示已額滿的活動</span>
                            </label>

                            <label class="checkbox-option">
                                <input type="checkbox" v-model="tempFilters.showFreeOnly" />
                                <span>只顯示免費活動</span>
                            </label>

                            <label class="checkbox-option">
                                <input type="checkbox" v-model="tempFilters.showWithFeeOnly" />
                                <span>只顯示收費活動</span>
                            </label>
                        </div>
                    </div>
                </div>

                <!-- 視窗底部按鈕 -->
                <div class="modal-footer">
                    <button @click="resetAdvancedFilters" class="btn-reset">
                        <span class="icon">↺</span>
                        <span>重置</span>
                    </button>
                    <div class="action-buttons-modal">
                        <button @click="closeAdvancedFilters" class="btn-cancel-modal">取消</button>
                        <button @click="applyAdvancedFilters" class="btn-apply">
                            <span class="icon">✓</span>
                            <span>套用篩選</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>

         <!--取消對話框-->
          <div v-if="showCancelDialog" class="modal-overlay" @click="closeCancelDialog">
            <div class="dialog-content cancel-dialog" @click.stop>
                <div class="dialog-header">
                    <h2>⚠️ 確認取消活動</h2>
                    <button @click="closeCancelDialog" class="btn-close">✕</button>
                </div>

                <div class="dialog-body">
                    <div class="warning-message">
                        <p class="warning-icon">⚠️</p>
                        <p class="warning-text">確定要取消以下活動嗎?</p>
                    </div>
                
                    <div class="activity-info-box">
                        <h3>{{ cancelTargetActivity?.title }}</h3>
                        <div class="info-row">
                            <span class="label">📅 開始時間:</span>
                            <span class="value">{{ formatDateTime(cancelTargetActivity?.startTime) }}</span>
                        </div>
                        <div class="info-row">
                            <span class="label">📍 地點:</span>
                            <span class="value">{{ cancelTargetActivity?.location }}</span>
                        </div>
                        <div class="info-row" v-if="cancelTargetActivity?.currentParticipants">
                            <span class="label">👥 當前報名人數:</span>
                            <span class="value">{{ cancelTargetActivity.currentParticipants }} 人</span>
                        </div>
                    </div>

                    <div class="cancel-reason-section">
                        <label for="cancelReason">取消原因 (選填):</label>
                        <textarea 
                            id="cancelReason" 
                            v-model="cancelReason" 
                            placeholder="請說明活動的取消原因,系統會通知已報名的會員..." 
                            rows="4" 
                            class="cancel-reason-input"
                        ></textarea>
                    </div>

                    <div class="notice-box">
                        <p class="notice-title">⚠️ 注意事項:</p>
                        <ul class="notice-list">
                            <li>取消後無法復原!</li>
                            <li>已報名的會員將會收到通知</li>
                            <li>活動狀態將變成「已取消」</li>
                        </ul>
                    </div>
                </div>

                <!-- ✅ 修正:移到 dialog-content 裡面 -->
                <div class="dialog-actions">
                    <button @click="closeCancelDialog" class="btn-secondary" :disabled="isProcessing">
                        返回
                    </button>
                    <!-- ✅ 修正:按鈕內容 -->
                    <button @click="confirmCancelActivity" class="btn-danger" :disabled="isProcessing">
                        <span v-if="isProcessing">處理中...</span>
                        <span v-else>確認取消活動</span>
                    </button>
                </div>
            </div>
        </div>
         <!-- 刪除確認對話框 -->
    <div v-if="showDeleteDialog" class="dialog-overlay" @click="cancelDelete">
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>⚠️ 確認刪除</h3>
        </div>
        
        <div class="dialog-body">
          <p>確定要刪除草稿活動嗎?</p>
          <p class="activity-name">「{{ deleteTarget.name }}」</p>
          <p class="warning">此操作無法復原!</p>
        </div>
        
        <div class="dialog-actions">
          <button @click="cancelDelete" class="btn-cancel">
            取消
          </button>
          <button @click="confirmDelete" class="btn-confirm-delete">
            確定刪除
          </button>
        </div>
      </div>
    </div>
    <!-- 取消預約發布對話框 -->
<div v-if="showCancelScheduledDialogFlag" class="modal-overlay" @click="closeCancelScheduledDialog">
    <div class="dialog-content cancel-dialog" @click.stop>
        <div class="dialog-header">
            <h2>⚠️ 確認取消預約發布</h2>
            <button @click="closeCancelScheduledDialog" class="btn-close">✕</button>
        </div>
        <div class="dialog-body">
            <p>確定要把活動「{{ cancelScheduledTarget?.title }}」取消預約發布，回到草稿狀態嗎？</p>
            <div class="activity-info-box">
                <h3>{{ cancelScheduledTarget?.title }}</h3>
                <div class="info-row">
                    <span class="label">📅 開始時間:</span>
                    <span class="value">{{ formatDateTime(cancelScheduledTarget?.startTime) }}</span>
                </div>
                <div class="info-row">
                    <span class="label">📍 地點:</span>
                    <span class="value">{{ cancelScheduledTarget?.location }}</span>
                </div>
            </div>
        </div>
        <div class="dialog-actions">
            <button @click="closeCancelScheduledDialog" class="btn-secondary">返回</button>
            <button @click="confirmCancelScheduledPublish" class="btn-danger" :disabled="isProcessing">
                <span v-if="isProcessing">處理中...</span>
                <span v-else>確認取消預約發布</span>
            </button>
        </div>
    </div>
</div>
</div>
            
    
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'


const router = useRouter();

//建立資料參數
//狀態
const activities = ref([])
const loading = ref(false)
const isProcessing = ref(false)
//基本篩選
const searchKeyword = ref('')
const selectedStatus = ref('');
const selectedActivityType = ref('')
const selectedDanceStyle = ref('')
//進階篩選
const showAdvancedModal = ref(false);
const advancedFilters = ref({
    dateFilter: {
        startDate: '',
        endDate: ''
    },
    dateFilterMode: 'startTime',
    sortOrder: 'startTime-desc',
    activeQuickRange: '',
    showFullOnly: false,
    showFreeOnly: false,
    showWithFeeOnly: false
})
// ✅ 臨時篩選 (在彈窗中修改)
const tempFilters = ref({
    dateFilter: {
        startDate: '',
        endDate: ''
    },
    dateFilterMode: 'startTime',
    sortOrder: 'startTime-desc',
    activeQuickRange: '',
    showFullOnly: false,
    showFreeOnly: false,
    showWithFeeOnly: false
})

//快速時間範圍
const quickTimeRanges =[
    {value: 'today', label: '今天'},
    {value: 'tomorrow', label: '明天'},
    {value: 'nextWeek', label: '下週'},
    {value: 'thisWeek', label: '本週'},
    {value: 'thisMonth', label: '本月'},
    {value: 'nextMonth', label: '下個月'},
    {value: 'past7days', label: '過去七天'},
    {value: 'past30days', label: '過去30天'},
]
//刪除
const showDeleteDialog = ref(false);
const deleteTarget = ref({id: null, name: ''});
//取消
const showCancelDialog = ref(false);
const cancelTargetActivity = ref(null);
const cancelReason = ref('');

// 取消預約發布對話框相關
const showCancelScheduledDialogFlag = ref(false);
const cancelScheduledTarget = ref(null);

//載入活動資料
const loadActivities = async () => {
    loading.value = true//開始載入
    try {
        const response = await activityApi.getAllActivities()
        activities.value = response.data
        console.log('活動列表:', activities.value);
    } catch (error) {
        console.error('載入活動資料失敗:', error)
        alert('載入活動資料失敗，請稍後再試。')
    }
    finally {
        loading.value = false//結束載入
    }
}

const showCancelScheduledDialog = (activity) => {
    cancelScheduledTarget.value = activity;
    showCancelScheduledDialogFlag.value = true;
}

const closeCancelScheduledDialog = () => {
    showCancelScheduledDialogFlag.value = false;
    cancelScheduledTarget.value = null;
}

// 確認取消預約發布
const confirmCancelScheduledPublish = async () => {
    if (!cancelScheduledTarget.value) return;

    if (!confirm(`確定要取消預約發布「${cancelScheduledTarget.value.title}」嗎？`)) return;

    isProcessing.value = true;

    try {
        
        await activityApi.cancelScheduledPublish(cancelScheduledTarget.value.id);

        // 2️⃣ 更新本地列表
        const activity = activities.value.find(a => a.id === cancelScheduledTarget.value.id);
        if (activity) {
            activity.status = 'DRAFT';
            
        }

        alert(`✅ 活動「${cancelScheduledTarget.value.title}」已回到草稿狀態`);
        closeCancelScheduledDialog();
    } catch (error) {
        console.error('取消預約發布失敗:', error);
        alert('❌ 取消預約發布失敗，請稍後再試。');
    } finally {
        isProcessing.value = false;
    }
}

// ========== 單一操作：取消活動 ==========

const showCancelActivityDialog = (activity) => {
    cancelTargetActivity.value = activity;
    cancelReason.value = '';
    showCancelDialog.value = true
}

const closeCancelDialog = () => {
    showCancelDialog.value = false
    cancelTargetActivity.value = null
}

const confirmCancelActivity = async () => {
    if (!cancelTargetActivity.value) return;
    
    if(!confirm(`確定要取消活動「${cancelTargetActivity.value.title}」嗎?`)) {
        return;
    }
    isProcessing.value = true
    
    try {
        await activityApi.cancelActivity(cancelTargetActivity.value.id, {
            reason: cancelReason.value || '管理員取消活動'
        });
        alert(`✅ 活動「${cancelTargetActivity.value.title}」已取消！`)
        
        // 更新狀態
        const activity = activities.value.find(a => a.id === cancelTargetActivity.value.id)
        if (activity) {
            activity.status = 'CANCELLED';
            activity.cancelReason = cancelReason.value || '管理員取消活動'

        }
        
        closeCancelDialog();
        
    } catch (error) {
        console.error('取消活動失敗:', error)
        alert('❌ 取消活動失敗，請稍後再試。')
    } finally {
        isProcessing.value = false
    }
}



// ========== 導航功能 ==========

//編輯草稿快捷
const updateActivity = (activityId) => {
  console.log('編輯草稿 ID:', activityId);
  router.push(`/admin/update-activity-container/${activityId}`);
};

//跳轉到發布頁面
const goToPublish = (activityId) => {
  console.log('前往發布頁面,活動 ID:', activityId);
  router.push({
    name: 'publish-activity-container',
    params: { activityId }
  });
};



//刪除草稿(顯示提示對話框)
const deleteDraft = (activityId, activityName) =>{
    deleteTarget.value = {
        id: activityId,
        name: activityName
    }
    showDeleteDialog.value = true;
} 

//取消刪除
const cancelDelete = () =>{
    showDeleteDialog.value = false;
    deleteTarget.value = {
        id: null,
        name: ''
    }
    
}
//確認刪除
const confirmDelete = async() => {
    const activityId = deleteTarget.value.id;

    try{
        console.log('刪除草稿 ID:', activityId);
        await activityApi.deleteDraftActivity(activityId);
        alert("草稿已刪除")

        // 從列表中移除
        activities.value = activities.value.filter(activity => activity.id !== activityId);
        showDeleteDialog.value = false
        deleteTarget.value = {
            id: null,
            name: ''

        }

    }
    catch(error){
        console.log('刪除草稿 ID:',activityId,'失敗');
        if(error.response?.status === 403){
            alert("沒有權限刪除此草稿");
        }
        else if(error.response?.status === 404){
            alert("此草稿不存在");
        }
        else if(error.response?.status === 400){
            alert("只能刪除草稿狀態的活動");
        }
        else{
            alert("刪除失敗，請稍後在試..")
        }

    }

}

//標準化舞風標籤
const normalizeText = (text) => {
    if (!text) return '';
    return text
        .toLowerCase()
        .replace(/[\s-_]+/g, '') // 將多個空格替換為單個空格
        .trim(); // 去除前後空格
}

// ============ 計算屬性 ============

// 是否有啟用篩選
const hasActiveFilters = computed(() => {
    return searchKeyword.value || 
           selectedStatus.value || 
           selectedActivityType.value || 
           selectedDanceStyle.value ||
           hasAdvancedFilters.value
})

// ✅ 是否有啟用進階篩選
const hasAdvancedFilters = computed(() => {
    return advancedFilters.value.dateFilter.startDate ||
           advancedFilters.value.dateFilter.endDate ||
           advancedFilters.value.sortOrder !== 'startTime-desc' ||
           advancedFilters.value.showFullOnly ||
           advancedFilters.value.showFreeOnly ||
           advancedFilters.value.showWithFeeOnly
})

// ✅ 進階篩選數量
const advancedFilterCount = computed(() => {
    let count = 0
    if (advancedFilters.value.dateFilter.startDate || advancedFilters.value.dateFilter.endDate) count++
    if (advancedFilters.value.sortOrder !== 'startTime-desc') count++
    if (advancedFilters.value.showFullOnly) count++
    if (advancedFilters.value.showFreeOnly) count++
    if (advancedFilters.value.showWithFeeOnly) count++
    return count
})

//篩選後的活動列表
const filteredActivities = computed(() => {
    let result = [...activities.value];//複製原始資料
    const now = new Date();

    //狀態篩選
    if(selectedStatus.value){
        result = result.filter(activity => activity.status === selectedStatus.value)
    }

    //關鍵字搜尋
    if (searchKeyword.value) {
        const keyword = normalizeText(searchKeyword.value);
        result = result.filter(activity =>{
            const title = normalizeText(activity.title);
            const desc = normalizeText(activity.description);
            return title.includes(keyword) || (activity.description && desc.includes(keyword));
        }
            
        );
    }
    
    //活動類型篩選
    if (selectedActivityType.value) {
        result = result.filter(activity =>
            activity.activityType === selectedActivityType.value
        );
    }

    //舞風標籤篩選
    if (selectedDanceStyle.value) {
        const style = normalizeText(selectedDanceStyle.value);
        result = result.filter(activity => {
            const title = normalizeText(activity.title);
            const desc = normalizeText(activity.description);
            return title.includes(style) || (activity.description && desc.includes(style));
        });
    }
    //時間範圍篩選
    if(advancedFilters.value.dateFilter.startDate || advancedFilters.value.endDate){
        result = result.filter(activity => {
            let activityDate;
            switch(advancedFilters.value.dateFilterMode){
                case 'startTime':
                    activityDate = activity.startTime;
                    break;
                case 'endTime':
                    activityDate = activity.endTime;
                    break;
                case 'createdAt':
                    activityDate = activity.createdAt;
                    break;
                default:
                    activityDate = activity.startTime;
            }

            if(!activityDate) return false;

            const targetDate = new Date(activityDate);
            targetDate.setHours(0, 0, 0, 0);

            if(advancedFilters.value.dateFilter.startDate){
                const startDate = new Date(advancedFilters.value.dateFilter.startDate);
                startDate.setHours(0, 0, 0, 0);
                if(targetDate < startDate) return false;
            }

            if(advancedFilters.value.dateFilter.endDate){
                const endDate = new Date(advancedFilters.value.dateFilter.endDate);
                endDate.setHours(23, 59 ,59, 999);
                if(targetDate > endDate) return false;
            }

            return true;
        });
    }

    //其他篩選
    if(advancedFilters.value.showFullOnly){
        result = result.filter(activity => activity.currentParticipants >= activity.maxParticipants);
    }

    if(advancedFilters.value.showFreeOnly){
        result = result.filter(activity => activity.feeAmount === 0)
    }
    if(advancedFilters.value.showWithFeeOnly){
        result = result.filter(activity => activity.feeAmount > 0)
    }

    result.sort((a, b) => {
        switch(advancedFilters.value.sortOrder) {
            case 'startTime-desc':
                return new Date(b.startTime) - new Date(a.startTime)
            case 'startTime-asc':
                return new Date(a.startTime) - new Date(b.startTime)
            case 'created-desc':
                return new Date(b.createdAt || 0) - new Date(a.createdAt || 0)
            case 'created-asc':
                return new Date(a.createdAt || 0) - new Date(b.createdAt || 0)
            case 'title-asc':
                return (a.title || '').localeCompare(b.title || '')
            default:
                return 0
        }
    })
    

    return result;
});
// ============ 進階篩選功能 ============

// ✅ 開啟進階篩選視窗
const openAdvancedFilters = () => {
    tempFilters.value = JSON.parse(JSON.stringify(advancedFilters.value))
    showAdvancedModal.value = true
}

// ✅ 關閉進階篩選視窗
const closeAdvancedFilters = () => {
    showAdvancedModal.value = false
}

// ✅ 套用進階篩選
const applyAdvancedFilters = () => {
    advancedFilters.value = JSON.parse(JSON.stringify(tempFilters.value))
    showAdvancedModal.value = false
    handleFilterChange()
}
// ✅ 重置進階篩選
const resetAdvancedFilters = () => {
    tempFilters.value = {
        dateFilter: { startDate: '', endDate: '' },
        dateFilterMode: 'startTime',
        sortOrder: 'startTime-desc',
        activeQuickRange: '',
        showFullOnly: false,
        showFreeOnly: false,
        showWithFeeOnly: false,
    }
}

// ✅ 快速時間範圍選擇
const applyQuickTimeRange = (rangeValue) => {
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    
    tempFilters.value.activeQuickRange = rangeValue

    switch (rangeValue) {
        case 'today': {
            const todayStr = today.toISOString().split('T')[0]
            tempFilters.value.dateFilter.startDate = todayStr
            tempFilters.value.dateFilter.endDate = todayStr
            break
        }
        case 'tomorrow': {
            const tomorrow = new Date(today)
            tomorrow.setDate(today.getDate() + 1)
            const tomorrowStr = tomorrow.toISOString().split('T')[0]
            tempFilters.value.dateFilter.startDate = tomorrowStr
            tempFilters.value.dateFilter.endDate = tomorrowStr
            break
        }
        case 'thisWeek': {
            const weekStart = new Date(today)
            weekStart.setDate(today.getDate() - today.getDay())
            const weekEnd = new Date(weekStart)
            weekEnd.setDate(weekStart.getDate() + 6)
            tempFilters.value.dateFilter.startDate = weekStart.toISOString().split('T')[0]
            tempFilters.value.dateFilter.endDate = weekEnd.toISOString().split('T')[0]
            break
        }
        case 'nextWeek': {
            const nextWeekStart = new Date(today)
            nextWeekStart.setDate(today.getDate() + (7 - today.getDay()))
            const nextWeekEnd = new Date(nextWeekStart)
            nextWeekEnd.setDate(nextWeekStart.getDate() + 6)
            tempFilters.value.dateFilter.startDate = nextWeekStart.toISOString().split('T')[0]
            tempFilters.value.dateFilter.endDate = nextWeekEnd.toISOString().split('T')[0]
            break
        }
        case 'thisMonth': {
            const monthStart = new Date(today.getFullYear(), today.getMonth(), 1)
            const monthEnd = new Date(today.getFullYear(), today.getMonth() + 1, 0)
            tempFilters.value.dateFilter.startDate = monthStart.toISOString().split('T')[0]
            tempFilters.value.dateFilter.endDate = monthEnd.toISOString().split('T')[0]
            break
        }
        case 'nextMonth': {
            const nextMonthStart = new Date(today.getFullYear(), today.getMonth() + 1, 1)
            const nextMonthEnd = new Date(today.getFullYear(), today.getMonth() + 2, 0)
            tempFilters.value.dateFilter.startDate = nextMonthStart.toISOString().split('T')[0]
            tempFilters.value.dateFilter.endDate = nextMonthEnd.toISOString().split('T')[0]
            break
        }
        case 'past7days': {
            const past7 = new Date(today)
            past7.setDate(today.getDate() - 7)
            tempFilters.value.dateFilter.startDate = past7.toISOString().split('T')[0]
            tempFilters.value.dateFilter.endDate = today.toISOString().split('T')[0]
            break
        }
        case 'past30days': {
            const past30 = new Date(today)
            past30.setDate(today.getDate() - 30)
            tempFilters.value.dateFilter.startDate = past30.toISOString().split('T')[0]
            tempFilters.value.dateFilter.endDate = today.toISOString().split('T')[0]
            break
        }
    }
}

const handleFilterChange = () => {
    console.log('🎯 篩選條件已變更');
};

const handleSearch = () => {
    console.log('🔍 搜尋:', searchKeyword.value);
};

const clearDateFilter = () => {
    advancedFilters.value.dateFilter.startDate = ''
    advancedFilters.value.dateFilter.endDate = ''
    advancedFilters.value.activeQuickRange = ''
}

const clearFilters = () => {
    searchKeyword.value = '';
    selectedStatus.value = '';
    selectedActivityType.value = '';
    selectedDanceStyle.value = '';
    advancedFilters.value = {
        dateFilter: { startDate: '', endDate: '' },
        dateFilterMode: 'startTime',
        sortOrder: 'startTime-desc',
        activeQuickRange: '',
        showFullOnly: false,
        showFreeOnly: false,
        showWithFeeOnly: false,
    }
    console.log('✅ 清除所有篩選');
};

//格式化活動類型
const getActivityTypeLabel = (type) => {
    const labels = {
        'REGULAR': '社課',
        'SPECIAL': '特殊活動',
        'TRAINING': '團練',
        'PERFORMANCE': '演出',
        'COMPETITION': '比賽'
    }
    return labels[type] || type;
}

//舞風類型
const danceStyles = [
    'Hip Hop',
    'Jazz',
    'Popping',
    'Locking',
    'Breaking',
    'House',
    'Waacking',
    'Voguing',
    'Urban',
    'K-pop',
    'Freestyle'
];

//格式化狀態
const getStatusLabel = (status) => {
    const labels = {
        'DRAFT': '草稿',
        'PUBLISHED': '已發布',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消',
        'SCHEDULE' : '已預約'
    }
    return labels[status] || status;
}
//格式化時間
const formatDateTime = (dateTime) => {
   if (!dateTime) return '-';

    const date = new Date(dateTime);
   //格式化成 2026/01/08 19:00
   return date.toLocaleString('zh-TW', {
       year: 'numeric',
       month: '2-digit',
       day: '2-digit',
       hour: '2-digit',
       minute: '2-digit',
   });
}


onMounted(() => {
    loadActivities();
})
</script>


<style scoped>
.activity-list-page {
    padding: 20px;
}

/* ========== 表格樣式 ========== */
.activities-table {
    width: 100%;
    border-collapse: collapse;
    background: white;
    margin-top: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    border-radius: 8px;
    overflow: hidden;
}

.activities-table th {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 16px 12px;
    text-align: left;
    font-weight: 600;
    font-size: 14px;
}

.activities-table td {
    padding: 16px 12px;
    border-bottom: 1px solid #f0f0f0;
    font-size: 14px;
}

.activities-table tbody tr:hover {
    background: #f8f9ff;
}

.activities-table tbody tr:last-child td {
    border-bottom: none;
}

/* ========== 狀態標籤 ========== */
.status-badge {
    display: inline-block;
    padding: 6px 12px;
    border-radius: 16px;
    font-size: 12px;
    font-weight: 600;
}

.status-badge.draft {
    background: #9e9e9e;
    color: white;
}

.status-badge.schedule {
    background: #ff9800;
    color: white;
}

.status-badge.published {
    background: #4caf50;
    color: white;
}

.status-badge.cancelled {
    background: #f44336;
    color: white;
}

.status-badge.completed {
    background: #607d8b;
    color: white;
}

.btn-create {
    padding: 12px 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
    margin-bottom: 20px;
}

.btn-create:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

/* ========== 對話框基礎樣式 (彈出視窗遮罩) ========== */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 2000;
    animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
}

/* ========== 對話框容器 ========== */
.dialog-content {
    background: white;
    border-radius: 16px;
    width: 90%;
    max-width: 500px;
    max-height: 90vh;
    overflow-y: auto;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
    animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* ========== 對話框標題 ========== */
.dialog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px;
    border-bottom: 1px solid #f0f0f0;
}

.dialog-header h2,
.dialog-header h3 {
    margin: 0;
    font-size: 20px;
    color: #333;
}

/* 關閉按鈕 */
.btn-close {
    background: none;
    border: none;
    font-size: 28px;
    cursor: pointer;
    color: #999;
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    transition: all 0.2s;
}

.btn-close:hover {
    background: #f5f5f5;
    color: #333;
}

/* ========== 對話框內容 ========== */
.dialog-body {
    padding: 24px;
}

/* ========== 警告訊息區 ========== */
.warning-message {
    text-align: center;
    margin-bottom: 24px;
}

.warning-icon {
    font-size: 48px;
    margin: 0 0 12px 0;
}

.warning-text {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin: 0;
}

/* ========== 活動資訊框 ========== */
.activity-info-box {
    background: #f5f7fa;
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 20px;
    text-align: center;
}

.activity-info-box h3 {
    margin: 0 0 12px 0;
    color: #333;
}

/* 活動名稱 (刪除對話框專用) */
.activity-name {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin: 0 0 8px 0;
}

/* 警告通知 */
.warning-notice {
    color: #f44336;
    font-weight: 600;
    font-size: 14px;
    margin: 0;
}

/* 舊的 warning 樣式 (保留向下相容) */
.warning {
    color: #f44336;
    font-weight: 600;
    text-align: center;
    margin-top: 16px;
}

/* ========== 取消活動對話框專用 ========== */
.cancel-dialog {
    max-width: 600px;
}

.info-row {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;
    font-size: 14px;
}

.info-row .label {
    color: #666;
    min-width: 120px;
    font-weight: 500;
}

.info-row .value {
    color: #333;
    font-weight: 600;
}

.cancel-reason-section {
    margin: 20px 0;
}

.cancel-reason-section label {
    display: block;
    margin-bottom: 8px;
    font-weight: 600;
    color: #333;
    font-size: 14px;
}

.cancel-reason-input {
    width: 100%;
    padding: 12px;
    border: 2px solid #e0e0e0;
    border-radius: 8px;
    font-family: inherit;
    font-size: 14px;
    resize: vertical;
    transition: border-color 0.2s;
}

.cancel-reason-input:focus {
    outline: none;
    border-color: #f44336;
}

.cancel-reason-input::placeholder {
    color: #999;
}

/* ========== 注意事項框 ========== */
.notice-box {
    background-color: #fff3e0;
    padding: 16px;
    border-radius: 8px;
    border-left: 4px solid #ff9800;
}

.notice-title {
    margin: 0 0 8px 0;
    font-weight: 600;
    color: #e65100;
    font-size: 14px;
}

.notice-list {
    margin: 0;
    padding-left: 20px;
}

.notice-list li {
    margin-bottom: 4px;
    color: #666;
    font-size: 13px;
}

/* ========== 對話框按鈕區 ========== */
.dialog-actions {
    display: flex;
    gap: 12px;
    padding: 16px 24px;
    border-top: 1px solid #f0f0f0;
    justify-content: flex-end;
}

/* 按鈕樣式 */
.btn-secondary,
.btn-danger,
.btn-cancel,
.btn-confirm-delete {
    padding: 12px 24px;
    border: none;
    border-radius: 8px;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
}

/* 次要按鈕 */
.btn-secondary,
.btn-cancel {
    background: #f5f5f5;
    color: #333;
}

.btn-secondary:hover:not(:disabled),
.btn-cancel:hover {
    background: #e0e0e0;
}

/* 危險按鈕 */
.btn-danger,
.btn-confirm-delete {
    background: #f44336;
    color: white;
}

.btn-danger:hover:not(:disabled),
.btn-confirm-delete:hover {
    background: #d32f2f;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(244, 67, 54, 0.4);
}

.btn-danger:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

/* ========== 操作按鈕樣式 ========== */
.col-actions {
    min-width: 180px;
}

.action-buttons {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

.btn-action {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 8px 12px;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    font-size: 13px;
    font-weight: 600;
}

.btn-edit {
    background: #e8f5e9;
    color: #388e3c;
}

.btn-edit:hover {
    background: #4caf50;
    color: white;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(76, 175, 80, 0.3);
}

.btn-publish {
    background: #f3e5f5;
    color: #7b1fa2;
}

.btn-publish:hover {
    background: #9c27b0;
    color: white;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(156, 39, 176, 0.3);
}

.btn-delete {
    background: #ffebee;
    color: #c62828;
}

.btn-delete:hover {
    background: #f44336;
    color: white;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(244, 67, 54, 0.3);
}

.btn-action.btn-cancel {
    background: #fff3e0;
    color: #e65100;
}

.btn-action.btn-cancel:hover {
    background: #ff9800;
    color: white;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(255, 152, 0, 0.3);
}

.btn-disabled {
    background: #f5f5f5;
    color: #9e9e9e;
    cursor: not-allowed;
}

/* ========== 搜尋篩選區樣式 ========== */
.search-filter {
    background: white;
    padding: 20px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    margin-bottom: 20px;
    width: 100%;
}

.basic-search {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
}

.search-input {
    flex: 1;
    padding: 12px 16px;
    border: 2px solid #e0e0e0;
    border-radius: 8px;
    font-size: 15px;
    transition: border-color 0.2s;
}

.search-input:focus {
    outline: none;
    border-color: #667eea;
}

.search-btn {
    padding: 12px 24px;
    background: #667eea;
    color: white;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
}

.search-btn:hover {
    background: #5568d3;
    transform: translateY(-1px);
}

.filters {
    display: flex;
    gap: 16px;
    align-items: center;
    flex-wrap: wrap;
}

.filter-group {
    display: flex;
    align-items: center;
    gap: 8px;
}

.filter-group label {
    font-size: 14px;
    color: #666;
    font-weight: 500;
}

.filter-group select {
    padding: 8px 12px;
    border: 2px solid #e0e0e0;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    transition: border-color 0.2s;
}

.filter-group select:focus {
    outline: none;
    border-color: #667eea;
}

/* ========== 進階篩選按鈕 ========== */
.btn-advanced-filters {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    background: white;
    border: 2px solid #e0e0e0;
    border-radius: 6px;
    color: #666;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    position: relative;
}

.btn-advanced-filters:hover {
    border-color: #667eea;
    color: #667eea;
    transform: translateY(-1px);
}

.btn-advanced-filters.has-filters {
    background: #667eea;
    border-color: #667eea;
    color: white;
}

.btn-advanced-filters.has-filters:hover {
    background: #5568d3;
    border-color: #5568d3;
}

.filter-badge {
    position: absolute;
    top: -8px;
    right: -8px;
    background: #f44336;
    color: white;
    border-radius: 50%;
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 11px;
    font-weight: 700;
}

/* 清除篩選按鈕 */
.clear-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    background: #ffebee;
    border: 1px solid #f44336;
    border-radius: 6px;
    color: #c62828;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
}

.clear-btn:hover {
    background: #f44336;
    color: white;
    transform: translateY(-1px);
}

/* ========== 進階篩選彈出視窗 ========== */
.advanced-filters-modal {
    width: 90%;
    max-width: 720px;
    background: #ffffff;
    border-radius: 20px;
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
    animation: modalSlideUp 0.35s ease;
    overflow: hidden;
}

@keyframes modalSlideUp {
    from {
        opacity: 0;
        transform: translateY(30px) scale(0.98);
    }
    to {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}

/* ========== 進階篩選 Header ========== */
.advanced-filters-modal .modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 24px;
    border-bottom: 1px solid #f0f0f0;
    background: linear-gradient(135deg, #667eea, #764ba2);
    color: #fff;
}

.advanced-filters-modal .modal-header h2 {
    font-size: 18px;
    font-weight: 700;
    margin: 0;
}

.advanced-filters-modal .btn-close {
    color: #fff;
}

.advanced-filters-modal .btn-close:hover {
    background: rgba(255, 255, 255, 0.2);
}

/* ========== 進階篩選 Body ========== */
.advanced-filters-modal .modal-body {
    padding: 24px;
    max-height: 65vh;
    overflow-y: auto;
}

/* ========== 區塊共用樣式 ========== */
.filter-section {
    margin-bottom: 28px;
}

.section-title {
    font-size: 16px;
    font-weight: 700;
    color: #333;
    margin-bottom: 12px;
}

/* ========== 快速時間按鈕 ========== */
.quick-filters {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 16px;
}

.quick-filter-btn {
    padding: 8px 14px;
    border-radius: 999px;
    border: 2px solid #e0e0e0;
    background: #fff;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
}

.quick-filter-btn:hover {
    border-color: #667eea;
    color: #667eea;
}

.quick-filter-btn.active {
    background: #667eea;
    border-color: #667eea;
    color: #fff;
}

/* ========== 日期區間 ========== */
.date-range-group {
    display: flex;
    align-items: flex-end;
    gap: 12px;
    margin-bottom: 16px;
}

.date-input-group {
    flex: 1;
}

.date-input-group label {
    display: block;
    font-size: 13px;
    color: #666;
    margin-bottom: 6px;
}

.date-input {
    width: 100%;
    padding: 10px 12px;
    border-radius: 8px;
    border: 2px solid #e0e0e0;
    font-size: 14px;
}

.date-input:focus {
    outline: none;
    border-color: #667eea;
}

.date-separator {
    font-size: 18px;
    color: #999;
    padding-bottom: 8px;
}

/* ========== 日期篩選模式 ========== */
.date-mode-group {
    margin-top: 12px;
}

.mode-label {
    display: block;
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 8px;
}

.radio-options {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
}

.radio-option {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    cursor: pointer;
}

/* ========== 排序選項 ========== */
.sort-options {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 12px;
}

.sort-option {
    display: block;
    border: 2px solid #e0e0e0;
    border-radius: 12px;
    padding: 12px;
    cursor: pointer;
    transition: all 0.2s;
}

.sort-option:hover {
    border-color: #667eea;
    background: #f5f7ff;
}

.sort-option input {
    display: none;
}

.sort-option input:checked + .option-content {
    background: #667eea;
    color: #fff;
}

.option-content {
    display: flex;
    gap: 12px;
    align-items: center;
    border-radius: 10px;
    padding: 10px;
}

.option-icon {
    font-size: 22px;
}

.option-title {
    font-weight: 700;
}

.option-desc {
    font-size: 12px;
    opacity: 0.85;
}

/* ========== Checkbox 區 ========== */
.checkbox-options {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.checkbox-option {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    cursor: pointer;
}

/* ========== 進階篩選 Footer ========== */
.advanced-filters-modal .modal-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    border-top: 1px solid #f0f0f0;
    background: #fafafa;
}

.btn-reset {
    background: #f5f5f5;
    border: none;
    border-radius: 8px;
    padding: 10px 16px;
    font-weight: 600;
    cursor: pointer;
}

.btn-reset:hover {
    background: #e0e0e0;
}

.action-buttons-modal {
    display: flex;
    gap: 12px;
}

.btn-cancel-modal {
    background: #f5f5f5;
    border: none;
    border-radius: 8px;
    padding: 10px 16px;
    font-weight: 600;
    cursor: pointer;
}

.btn-apply {
    background: linear-gradient(135deg, #667eea, #764ba2);
    color: #fff;
    border: none;
    border-radius: 8px;
    padding: 10px 20px;
    font-weight: 700;
    cursor: pointer;
}

.btn-apply:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

/* ========== 刪除對話框特殊動畫 ========== */
.delete-dialog .warning-icon {
    animation: shake 0.5s ease-in-out;
}

@keyframes shake {
    0%, 100% { transform: translateX(0); }
    25% { transform: translateX(-10px); }
    75% { transform: translateX(10px); }
}

.delete-dialog .activity-info-box {
    background: linear-gradient(135deg, #ffebee, #ffcdd2);
    border: 2px solid #f44336;
}
</style>