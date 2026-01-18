<template>
    <div class="activity-list-container">
        <h1>活動列表</h1>
        <!-- 顯示載入狀態 -->
         
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
                <!--舞風標籤-->
                <div class="filter-group">
                    <label for="danceStyleFilter">舞風標籤:</label>
                    <select v-model="selectedDanceStyle" id="danceStyleFilter" @change="handleFilterChange">
                        <option value="">全部</option>
                        <option v-for="style in danceStyles" :key="style" :value="style">{{ style }}</option>
                    </select>
                </div>
            <!--清除篩選按鈕-->
            <button v-if="hasActiveFilters()" @click="clearFilters" class="clear-btn">清除篩選</button>
             </div>

        </div>
        </div>
        <p v-if="loading">載入中...</p>
        <!-- 顯示活動列表 -->
         <p v-else>目前有{{ activities.length }}個活動<router-link to="/views/admin/CreateActivity.vue" class="btn-create">
                ➕ 建立活動
            </router-link></p>

         

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
                </tr>
            </thead>
            <tbody>
                <tr v-for="activity in activities" :key="activity.id">
                    <td>{{ activity.id }}</td>
                    <td>{{ activity.title }}</td>
                    <td>{{ formatDateTime(activity.startTime) }}</td>
                    <td>{{ formatDateTime(activity.endTime) }}</td>
                    <td>{{ activity.location }}</td>
                    <td>{{ getActivityTypeLabel(activity.activityType) }}</td>
                    <td>{{ activity.feeAmount }}</td>
                    <td><span class="status-badge" :class="activity.status.toLowerCase()">{{ getStatusLabel(activity.status) }}</span></td>
                </tr>
            </tbody>
         </table>
         </div>
            
    
   
    </template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'

const router = useRouter();

//建立資料參數
const activities = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const selectedActivityType = ref('')
const selectedDanceStyle = ref('')
const selectedActivity = ref(null)
const selectedActivities = ref([]) //批次選取的活動ID陣列
const showRegisterModal = ref(false)
const showCompleteModal = ref(false)
const showCancelModal = ref(false)
const isProcessing = ref(false)

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

//批次操作
//是否全選
const isAllSelected = computed(() => {
    const selectedActivities = activities.value.filter(a => canSelect(a))
    return selectedActivities.length > 0 &&
        selectedActivities.length === selectableActivities.length});

//全選或取消全選
const toggleSelectAll =() => {
    if (isAllSelected.value) {
        //取消全選
        selectedActivities.value= []
        }
     else {
        //全選
        selectedActivities.value = activities.value
            .filter(a => canSelect(a))
            .map(a => a.id)
    }
}

const canSelect = (activity) => {
    //只有草稿和已發布狀態的活動可以被選取
    return activity.status === 'DRAFT' || activity.status === 'PUBLISHED';
}

//清除選取
const clearSelection = () => {
    selectedActivities.value = []
}

// 是否可以批次發布（選中的都是草稿）
const canBatchPublish = computed(() => {
    if (selectedActivities.value.length === 0) return false
    return selectedActivities.value.every(id => {
        const activity = activities.value.find(a => a.id === id)
        return activity?.status === 'DRAFT'
    })
})

// 是否可以批次完成（選中的都是已發布）
const canBatchComplete = computed(() => {
    if (selectedActivities.value.length === 0) return false
    return selectedActivities.value.every(id => {
        const activity = activities.value.find(a => a.id === id)
        return activity?.status === 'PUBLISHED'
    })
})

// 批次發布
const batchPublish = async () => {
    if (!confirm(`確定要發布 ${selectedActivities.value.length} 個活動嗎？`)) {
        return
    }
    
    isProcessing.value = true
    
    try {
        await Promise.all(
            selectedActivities.value.map(id => 
                activityApi.publishActivity(id)
            )
        )
        
        // 更新狀態
        selectedActivities.value.forEach(id => {
            const activity = activities.value.find(a => a.id === id)
            if (activity) activity.status = 'PUBLISHED'
        })
        
        alert(`✅ 成功發布 ${selectedActivities.value.length} 個活動！`)
        clearSelection()
        
    } catch (error) {
        console.error('批次發布失敗:', error)
        alert('❌ 批次發布失敗，請稍後再試。')
    } finally {
        isProcessing.value = false
    }
}

// 批次完成
const batchComplete = async () => {
    if (!confirm(`確定要完成 ${selectedActivities.value.length} 個活動嗎？`)) {
        return
    }
    
    isProcessing.value = true
    
    try {
        await Promise.all(
            selectedActivities.value.map(id => 
                activityApi.completeActivity(id)
            )
        )
        
        // 更新狀態
        selectedActivities.value.forEach(id => {
            const activity = activities.value.find(a => a.id === id)
            if (activity) activity.status = 'COMPLETED'
        })
        
        alert(`✅ 成功完成 ${selectedActivities.value.length} 個活動！`)
        clearSelection()
        
    } catch (error) {
        console.error('批次完成失敗:', error)
        alert('❌ 批次完成失敗，請稍後再試。')
    } finally {
        isProcessing.value = false
    }
}

// ========== 單一操作：發布 ==========

const openPublishModal = (activity) => {
    selectedActivity.value = activity
    showPublishModal.value = true
}

const closePublishModal = () => {
    showPublishModal.value = false
    selectedActivity.value = null
}

const confirmPublish = async () => {
    if (!selectedActivity.value) return
    
    isProcessing.value = true
    
    try {
        await activityApi.publishActivity(selectedActivity.value.id)
        
        // 更新狀態
        const activity = activities.value.find(a => a.id === selectedActivity.value.id)
        if (activity) activity.status = 'PUBLISHED'
        
        alert(`✅ 活動「${selectedActivity.value.title}」發布成功！`)
        closePublishModal()
        
    } catch (error) {
        console.error('發布失敗:', error)
        alert('❌ 發布失敗，請稍後再試。')
    } finally {
        isProcessing.value = false
    }
}

// ========== 單一操作：完成活動 ==========

const openCompleteModal = (activity) => {
    selectedActivity.value = activity
    showCompleteModal.value = true
}

const closeCompleteModal = () => {
    showCompleteModal.value = false
    selectedActivity.value = null
}

const confirmComplete = async () => {
    if (!selectedActivity.value) return
    
    isProcessing.value = true
    
    try {
        await activityApi.completeActivity(selectedActivity.value.id)
        
        // 更新狀態
        const activity = activities.value.find(a => a.id === selectedActivity.value.id)
        if (activity) activity.status = 'COMPLETED'
        
        alert(`✅ 活動「${selectedActivity.value.title}」已完成！`)
        closeCompleteModal()
        
    } catch (error) {
        console.error('完成活動失敗:', error)
        alert('❌ 完成活動失敗，請稍後再試。')
    } finally {
        isProcessing.value = false
    }
}

// ========== 單一操作：取消活動 ==========

const openCancelModal = (activity) => {
    selectedActivity.value = activity
    showCancelModal.value = true
}

const closeCancelModal = () => {
    showCancelModal.value = false
    selectedActivity.value = null
}

const confirmCancel = async () => {
    if (!selectedActivity.value) return
    
    isProcessing.value = true
    
    try {
        await activityApi.cancelActivity(selectedActivity.value.id)
        
        // 更新狀態
        const activity = activities.value.find(a => a.id === selectedActivity.value.id)
        if (activity) activity.status = 'CANCELLED'
        
        alert(`✅ 活動「${selectedActivity.value.title}」已取消！`)
        closeCancelModal()
        
    } catch (error) {
        console.error('取消活動失敗:', error)
        alert('❌ 取消活動失敗，請稍後再試。')
    } finally {
        isProcessing.value = false
    }
}

// ========== 刪除活動 ==========

const openDeleteModal = (activity) => {
    if (activity.status !== 'DRAFT') {
        alert('❌ 只有草稿狀態的活動可以刪除！')
        return
    }
    
    if (confirm(`確定要刪除「${activity.title}」嗎？\n此操作無法復原！`)) {
        deleteActivity(activity.id)
    }
}

const deleteActivity = async (activityId) => {
    try {
        await activityApi.deleteActivity(activityId)
        
        // 從列表移除
        activities.value = activities.value.filter(a => a.id !== activityId)
        
        alert('✅ 活動已刪除')
    } catch (error) {
        console.error('刪除失敗:', error)
        alert('❌ 刪除失敗')
    }
}

// ========== 導航功能 ==========

const goToEditActivity = (activityId) => {
    router.push(`/admin/activities/${activityId}/edit`)
}

const goToViewActivity = (activityId) => {
    router.push(`/admin/activities/${activityId}`)
}

//標準化舞風標籤
const normalizeText = (text) => {
    if (!text) return '';
    return text
        .toLowerCase()
        .replace(/[\s-_]+/g, '') // 將多個空格替換為單個空格
        .trim(); // 去除前後空格
}

//檢查是否有啟用的篩選
const hasActiveFilters = () => {
    return searchKeyword.value || selectedActivityType.value || selectedDanceStyle.value;
}

//篩選後的活動列表
const filteredActivities = computed(() => {
    let result = [...activities.value];//複製原始資料
    const now = new Date();

    //即將到來的活動篩選
    if (activeTab.value === 'upcoming') {
        const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
        const thirtyDaysLater = new Date(today);
        thirtyDaysLater.setDate(thirtyDaysLater.getDate() + 30);
        

        result = result.filter(activity => {
            if (!activity.startTime) return false;
            const activityDate = new Date(activity.startTime);
            return activityDate >= today && activityDate <= thirtyDaysLater;
        });
    }
    //最新發布活動篩選
    else if (activeTab.value === 'news') {
        result = result.filter(activity => {
            if (!activity.startTime) return false;
            const created = new Date(activity.publishedAt);
            const daysDiff = (now - created) / (1000 * 60 * 60 * 24);
            return daysDiff <= 7;
            
        });
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
    //時間範圍篩選邏輯

    

    return result;
});

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
        'CANCELLED': '已取消'
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

/* 表格樣式 */
.activities-table {
    width: 100%;
    border-collapse: collapse;
    background: white;
    margin-top: 20px;
    width: 200%;
}

/* 表頭 */
.activities-table th {
    background: #f5f5f5;
    padding: 12px;
    text-align: left;
    font-weight: 600;
    border-bottom: 2px solid #ddd;
    
}

/* 表格內容 */
.activities-table td {
    padding: 12px;
    border-bottom: 1px solid #e0e0e0;
}

/* 滑鼠移到列上時變色 */
.activities-table tbody tr:hover {
    background: #f9f9f9;
}

/* 👇 新增：狀態標籤基本樣式 */
.status-badge {
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
}

/* 👇 新增：草稿狀態 - 灰色 */
.status-badge.draft {
    background: #9e9e9e;
    color: white;
}

/* 👇 新增：已發布狀態 - 綠色 */
.status-badge.published {
    background: #4caf50;
    color: white;
}

/* 👇 新增：已取消狀態 - 紅色 */
.status-badge.cancelled {
    background: #f44336;
    color: white;
}

.btn-create{
    justify-content: flex-end;
    
}
    </style>