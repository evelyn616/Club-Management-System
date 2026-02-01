<template>
  <div class="publish-activity-container">
    <!-- Loading 狀態 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>載入中...</p>
    </div>

    <!-- 主要內容 -->
    <div v-else class="publish-content">
      <!-- 標題 -->
      <div class="page-header">
        <h1>📢 {{ pageTitle }}</h1>
        <p class="subtitle">{{ pageSubtitle }}</p>
      </div>

      <!-- 活動選擇區域 (只有獨立進入時顯示) -->
      <div v-if="!fromRoute && (!hasSelection || batchMode)" class="activity-selector-card">
        <div class="card-header">
          <h2>選擇要發布的活動</h2>
          <div class="mode-toggle">
            <label>
              <input type="checkbox" v-model="batchMode" />
              啟用批量選擇
            </label>
          </div>
        </div>

        <!-- 草稿活動列表 -->
        <div v-if="draftActivities.length > 0" class="draft-list">
          <!-- 批量模式工具欄 -->
          <div v-if="batchMode" class="batch-toolbar">
            <span class="selected-count">
              已選擇 {{ selectedActivityId.size }} 個
            </span>
            <button @click="toggleSelectAll" class="btn-select-all">
              {{ isAllSelected ? '取消全選' : '全選' }}
            </button>
          </div>

          <!-- 活動列表 -->
          <div 
            v-for="draft in draftActivities" 
            :key="draft.id" 
            class="draft-item" 
            :class="{ 
              selected: isSelected(draft.id),
              'batch-mode': batchMode 
            }" 
            @click="handleActivitiesClick(draft.id)"
          >
            <!-- 批量模式的複選框 -->
            <div v-if="batchMode" class="checkbox-wrapper">
              <input 
                type="checkbox"
                :checked="selectedActivityId.has(draft.id)"
                @click.stop="toggleSelection(draft.id)"
              />
            </div>

            <!-- 活動資訊 -->
            <div class="draft-info">
              <h3>{{ draft.title }}</h3>
              <div class="meta">
                <span>📅 {{ formatDateTime(draft.startTime) }}</span>
                <span>📍 {{ draft.location }}</span>
                <span class="type-badge">{{ getActivityTypeLabel(draft.activityType) }}</span>
              </div>
            </div>

            <!-- 單一模式的選擇按鈕 -->
            <button v-if="!batchMode" class="btn-select" @click.stop="handleActivitiesClick(draft.id)">
              {{ selectedActivityId.has(draft.id) ? '✓ 已選擇' : '選擇此活動' }}
            </button>
          </div>
        </div>

        <!-- 沒有草稿活動 -->
        <div v-else class="empty-state">
          <p>📭 目前沒有任何草稿喔~</p>
          <button @click="router.push({ name: 'create-activity-container' })" class="btn-primary">
            點我創建新活動!
          </button>
        </div>
      </div>

      <!-- 活動預覽與發布設定 (有選擇活動後顯示) -->
      <div v-if="hasSelection" class="publish-section">
        <!-- 單一活動預覽 (單一模式) -->
        <div v-if="currentMode === 'single' && currentActivity" class="activity-preview-card">
          <div class="card-header">
            <h2>📋 活動預覽</h2>
            <div class="header-actions">
              <span class="status-badge" :class="`status-${currentActivity.status?.toLowerCase()}`">
                {{ getStatusLabel(currentActivity.status) }}
              </span>
              <button v-if="!fromRoute" @click="clearSelection" class="btn-text">
                ← 重新選擇
              </button>
            </div>
          </div>

          <div class="activity-info">
            <div class="info-row">
              <span class="label">活動標題:</span>
              <span class="value">{{ currentActivity.title }}</span>
            </div>
            <div class="info-row">
              <span class="label">活動描述:</span>
              <span class="value">{{ currentActivity.description }}</span>
            </div>
            <div class="info-row">
              <span class="label">活動地點:</span>
              <span class="value">{{ currentActivity.location }}</span>
            </div>
            <div class="info-row">
              <span class="label">活動類型:</span>
              <span class="value">{{ getActivityTypeLabel(currentActivity.activityType) }}</span>
            </div>
            <div class="info-row">
              <span class="label">開始時間:</span>
              <span class="value">{{ formatDateTime(currentActivity.startTime) }}</span>
            </div>
            <div class="info-row">
              <span class="label">結束時間:</span>
              <span class="value">{{ formatDateTime(currentActivity.endTime) }}</span>
            </div>
            <div class="info-row">
              <span class="label">報名截止時間:</span>
              <span class="value">{{ formatDateTime(currentActivity.registrationDeadline) }}</span>
            </div>
            <div class="info-row">
              <span class="label">費用金額:</span>
              <span class="value">NT$ {{ currentActivity.feeAmount }}</span>
            </div>
            <div class="info-row">
              <span class="label">最大參與人數:</span>
              <span class="value">{{ currentActivity.maxParticipants || '無上限' }}</span>
            </div>
          </div>

          <div class="action-buttons" v-if="fromRoute">
            <button @click="goBack" class="btn-secondary">
              ✏️ 返回編輯
            </button>
          </div>
        </div>

        <!-- 批量活動列表預覽 (批量模式) -->
        <div v-if="currentMode === 'batch'" class="batch-preview-card">
          <div class="card-header">
            <h2>📦 批量發布預覽</h2>
            <div class="header-actions">
              <span class="count-badge">
                {{ selectedActivityId.size }} 個活動
              </span>
              <button @click="clearSelection" class="btn-text">
                ← 重新選擇
              </button>
            </div>
          </div>

          <div class="selected-activities-list">
            <div 
              v-for="activity in selectedActivities" 
              :key="activity.id"
              class="selected-activity-item"
            >
              <div class="item-info">
                <h4>{{ activity.title }}</h4>
                <span class="meta">{{ formatDateTime(activity.startTime) }}</span>
              </div>
              <button @click="removeFromSelection(activity.id)" class="btn-remove">
                ✕
              </button>
            </div>
          </div>
        </div>

        <!-- 發布選項 -->
        <div class="publish-options-card">
          <div class="card-header">
            <h2>⚙️ 選擇發布方式</h2>
            <p class="hint" v-if="currentMode === 'batch'">
              所有活動將使用相同的發布設定
            </p>
          </div>

          <div class="publish-type-selector">
            <!-- 立即發布選項 -->
            <div 
              class="option-card" 
              :class="{ active: publishType === 'immediate' }" 
              @click="publishType = 'immediate'"
            >
              <div class="option-header">
                <input 
                  type="radio" 
                  id="immediate" 
                  value="immediate" 
                  v-model="publishType" 
                />
                <label for="immediate">
                  <span class="icon">⚡</span>
                  <span class="title">立即發布</span>
                </label>
              </div>
              <p class="description">
                {{ currentMode === 'batch' ? '所有活動立即發布' : '活動將會立即對外發布,並開放報名' }}
              </p>
            </div>

            <!-- 預約發布選項 -->
            <div 
              class="option-card" 
              :class="{ active: publishType === 'scheduled' }" 
              @click="publishType = 'scheduled'"
            >
              <div class="option-header">
                <input 
                  type="radio" 
                  id="scheduled" 
                  value="scheduled" 
                  v-model="publishType" 
                />
                <label for="scheduled">
                  <span class="icon">⏰</span>
                  <span class="title">預約發布</span>
                </label>
              </div>
              <p class="description">
                {{ currentMode === 'batch' ? '統一設定發布時間' : '選擇未來的日期與時間來發布活動' }}
              </p>

              <!-- 預約時間選擇 -->
              <div v-if="publishType === 'scheduled'" class="schedule-settings">
                <div class="input-group">
                  <label for="scheduleDate">預約發布日期:</label>
                  <input 
                    type="date" 
                    id="scheduleDate" 
                    v-model="scheduleDate" 
                    :min="minDate" 
                    required
                  />
                </div>

                <div class="input-group">
                  <label for="scheduleTime">預約發布時間:</label>
                  <input 
                    type="time" 
                    id="scheduleTime" 
                    v-model="scheduleTime" 
                    required 
                  />
                </div>

                <!-- 預約時間預覽 -->
                <div v-if="scheduleDate && scheduleTime" class="schedule-preview">
                  <div 
                    class="preview-item" 
                    :class="{ valid: isScheduleValid, invalid: !isScheduleValid }"
                  >
                    <span class="icon">{{ isScheduleValid ? '✅' : '❌' }}</span>
                    <div class="text">
                      <p class="label">預約發布時間</p>
                      <p class="value">{{ formatScheduleDateTime }}</p>
                      <p v-if="!isScheduleValid" class="error">
                        ⚠️ 發布時間必須是未來的時間
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 批量發布進度 -->
        <div v-if="isPublishing && currentMode === 'batch'" class="publish-progress-card">
          <h3>📊 發布進度</h3>
          <div class="progress-bar">
            <div 
              class="progress-fill" 
              :style="{ width: `${publishProgress}%` }"
            ></div>
          </div>
          <p class="progress-text">
            {{ publishedCount }} / {{ selectedActivityId.size }} 個活動已發布
          </p>
        </div>

        <!-- 操作按鈕 -->
        <div class="action-section">
          <div class="button-group">
            <button 
              @click="saveDraft" 
              class="btn-outline" 
              :disabled="isPublishing"
            >
              💾 保持為草稿
            </button>

            <button 
              v-if="publishType === 'immediate'" 
              @click="confirmPublish" 
              class="btn-primary" 
              :disabled="isPublishing"
            >
              <span v-if="isPublishing">發布中...</span>
              <span v-else>
                🚀 確認立即發布{{ currentMode === 'batch' ? ` (${selectedActivityId.size} 個)` : '' }}
              </span>
            </button>

            <button 
              v-else 
              @click="confirmSchedulePublish" 
              class="btn-primary" 
              :disabled="isPublishing || !isScheduleValid"
            >
              <span v-if="isPublishing">設定中...</span>
              <span v-else>
                ⏰ 確認預約發布{{ currentMode === 'batch' ? ` (${selectedActivityId.size} 個)` : '' }}
              </span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 批量發布結果對話框 -->
    <div v-if="showBatchResult" class="dialog-overlay" @click="closeBatchResult">
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h2>📊 批量發布結果</h2>
        </div>

        <div class="dialog-body">
          <div class="result-summary">
            <div class="success-count">
              ✅ 成功: {{ batchResult.success }} 個
            </div>
            <div v-if="batchResult.failed > 0" class="fail-count">
              ❌ 失敗: {{ batchResult.failed }} 個
            </div>
          </div>

          <!-- 失敗詳情 -->
          <div v-if="batchResult.errors && batchResult.errors.length > 0" class="error-details">
  <h4>失敗原因:</h4>
  <ul>
    <li v-for="error in batchResult.errors" :key="error.id">
      {{ error.title }}: {{ error.message }}
    </li>
  </ul>
</div>
        </div>

        <div class="dialog-actions">
          <button @click="closeBatchResult" class="btn-primary">
            {{ batchResult.failed === 0 ? '完成' : '知道了' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>


<script setup>
    import { onMounted, ref ,computed} from 'vue';
    import { activityApi } from '@/api/activity';
    import { useRoute, useRouter } from 'vue-router';


    
    
    const router = useRouter();
    const route = useRoute();

    //判斷是否為其他頁面帶入的id
    const fromRoute = computed(()=> route.params.activityId)

    //資料狀態
    const loading = ref(false);
    const draftActivities = ref([]);
    const currentActivity = ref(null);

    //發布狀態
    const isPublishing = ref(false);
    const publishedCount = ref(0);
    const showBatchResult = ref(false);
    const batchResult = ref({ success: 0, failed: 0, errors: []});
    
    
    //選中的活動id(來自route或是管理員選擇的)
    const selectedActivityId = ref(new Set());

    //批量模式開關(只有不帶有其他id值才能使用)
    const batchMode = ref(false);
    

    //發布選項
    const publishType = ref('immediate'); //立即發布 or 預約發布(immediate / scheduled)
    const scheduleTime = ref(''); //預約發布時間
    const scheduleDate = ref(''); //預約發布日期

    //當前模式辨別
    const currentMode = computed(() => {
        //如果有從其他葉面帶入id，進入單一發布
        if(fromRoute.value) return 'single';
        
        //單獨進入，沒有帶值，則依照選擇數量判斷是否切換模式
        if(selectedActivityId.value.size > 1) return 'batch';
        if(selectedActivityId.value.size === 1) return 'single';

        return null;
    });

    //是否有選擇活動
    const hasSelection = computed(() => {
        return selectedActivityId.value.size > 0;
    });

    const pageTitle = computed(() => {
  if (currentMode.value === 'batch') {
    return '📦 批量發布活動';
  }
  return '📢 發布活動設定';
});

const pageSubtitle = computed(() => {
  if (currentMode.value === 'batch') {
    return `同時發布 ${selectedActivityId.value.size} 個活動`;
  }
  return '設定活動的發布方式';
});

    //載入草稿活動
    const loadDraftActivities = async () => {
        loading.value = true;
        try{
            const response = await activityApi.getAllActivities();//只顯示草稿
            draftActivities.value = response.data.filter(act => act.status === 'DRAFT')
            console.log('草稿活動列表',draftActivities.value);

        }
        catch(error){
            console.log('載入草稿活動失敗：',error)
            alert('載入活動列表失敗，請稍後再試')
        }
        finally{
            loading.value = false;
        }
    }
    //已選擇的活動
    const selectedActivities =computed(() => {
      return draftActivities.value.filter(act => selectedActivityId.value.has(act.id));
    });

    //是否全選
    const isAllSelected = computed(() => {
      return draftActivities.value.length > 0 && selectedActivityId.value.size === draftActivities.value.length;
    });

   

    //載入活動詳情
    const loadActivityDetails = async (activityId) => {
        loading.value = true;
        try {
            const response = await activityApi.getActivityDetails(activityId);
            currentActivity.value = response.data;
            console.log('活動詳情載入成功:', currentActivity.value);

            // 如果活動不是草稿狀態，跳回列表
        if (currentActivity.value.status !== 'DRAFT') {
            alert('⚠️ 此活動已經發布過了')
            if(fromRoute.value){
                router.push('/admin/activity-list-container')
            }
            else{
                clearSelection();
            }
            router.push('/admin/activities')
        }
        } catch (error) {
            console.error('載入活動詳情失敗:', error);
            alert('載入活動詳情失敗，請稍後再試');
        } finally {
            loading.value = false;
        }
    };

    //最小日期
    const minDate = computed(() => {
        const today = new Date();
        return today.toISOString().split('T')[0];
    })

    //預約時間格式化
    const formatScheduleDateTime = computed(() => {
        if(!scheduleDate.value || !scheduleTime.value){
            
            return null;
        }
        const date = new Date(`${scheduleDate.value}T${scheduleTime.value}`);
        return date.toLocaleDateString('zh-TW', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
        });
    });

    //驗證預約時間是否有效
    const isScheduleValid = computed(() => {
        if(!scheduleDate.value || !scheduleTime.value){
            return false;
        }
        const scheduleDateTime = new Date(`${scheduleDate.value}T${scheduleTime.value}`);
        const now = new Date();
        return scheduleDateTime > now;
    })

    //活動點擊
    const handleActivitiesClick = (activityId) => {
      //批量
      if(batchMode.value){
        toggleSelection(activityId);
      }
      else{
        //單一模式
        selectedActivityId.value.clear();
        selectedActivityId.value.add(activityId);
        loadActivityDetails(activityId);
      }
    };

    //切換選擇狀態狀態
    const toggleSelection = (activityId) => {
      //取消選擇
      if(selectedActivityId.value.has(activityId)){
        selectedActivityId.value.delete(activityId);

        //如果取消選擇後，沒有選取到任何一個活動
        if(selectedActivityId.value.size === 0){
          currentActivity.value = null;
        }
      }
      //選取活動
      else{
        selectedActivityId.value.add(activityId);

        //如果是單一模式，會載入活動詳情
        if(!batchMode.value){
          loadActivityDetails(activityId);
        }
      }
    };

    //判斷活動是否被選中
    const isSelected = (activityId) => {
      return selectedActivityId.value.has(activityId);
    };

    //從選擇中刪除
    const removeFromSelection = (activityId) => {
      selectedActivityId.value.delete(activityId);
      if(selectedActivityId.value.size === 0){
        currentActivity.value =null;
      }
    };

    //全選/全取消
    const toggleSelectAll = () => {
      //全取消
      if(isAllSelected.value) {
        selectedActivityId.value.clear();
        }
      //全選
      else{
        draftActivities.value.forEach(act => {
          selectedActivityId.value.add(act.id);
        });
      }
    };

    //清除選擇
    const clearSelection = () => {
        selectedActivityId.value.clear();
        currentActivity.value = null;
        publishType.value = 'immediate';
        scheduleDate.value = '';
        scheduleTime.value = '';

    }
    // 批量發布等待的進度條
    const publishProgress = computed (() =>{
      if(selectedActivityId.value.size === 0) return 0;
      return Math.round((publishedCount.value/selectedActivityId.value.size)*100);
    })


    //立即發布設定
    const confirmPublish = async () => {

      const activityCount = selectedActivityId.value.size;
      const activityText = currentMode.value === 'batch' ? `${activityCount}個活動` : currentActivity.value?.title;

        if(!confirm(`確定要立即發布-${activityText}-活動嗎？`)){
            return;
        }

        isPublishing.value = true;
        try {
          if(currentMode.value === 'batch'){
            //批量發布
            await handleBatchPublish();
          }
          else{
            const activityId = Array.from(selectedActivityId.value)[0];
            await activityApi.publishActivity(activityId);
            alert(`活動-${currentActivity.value.title}-已成功發布`);
          };
            
            
            if(fromRoute.value){
                router.push('/admin/activity-list-container');
            }
            else{
                //重新載入列表並清除選擇
                await loadDraftActivities();
                clearSelection();
            }
        } catch (error) {
            console.error('發布活動失敗:', error);
            if (error.response?.status === 400) {
            alert('❌ 發布失敗：活動資料不完整，請檢查必填欄位。')
        } else if (error.response?.status === 409) {
            alert('❌ 此活動已經發布過了。')
            router.push('/admin/activity-list-container');

        } else {
            alert('❌ 發布失敗，請稍後再試。')
        }
            
        }
        finally {
            isPublishing.value = false;
        }
    }

    //預約發布設定
    const confirmSchedulePublish = async () => {
        if(!isScheduleValid.value){
            alert('預約發布時間無效，請選擇未來的日期與時間');
            return;
        }

        const scheduleDateTime = new Date(`${scheduleDate.value}T${scheduleTime.value}`);
        const displayTime = formatScheduleDateTime.value;
        const activityCount = selectedActivityId.value.size;
        const activityText = currentMode.value === 'batch' ? `${activityCount}個活動` : currentActivity.value?.title;

        if(!confirm(`確定要預約發布-${activityText}-活動於${displayTime}嗎？`)){
            return;
        }

        isPublishing.value = true;
        try {
          if(currentMode.value === 'batch'){
            //批量預約發布
            handleBatchSchedulePublish();
          }
          else{
            const activityId = Array.from(selectedActivityId.value)[0];
            await activityApi.schedulePublishActivity(activityId, {
                publishedAt: scheduleDateTime.toISOString()
            });
            alert(`活動-${currentActivity.value.title}-已成功預約發布於${displayTime}`);
          }
            
            if(fromRoute.value){
                router.push('admin/activity-list-container');
            }
            else{
                await loadDraftActivities();
                clearSelection();
            }
        } catch (error) {
            console.error('預約發布活動失敗:', error);
            alert('預約發布活動失敗，請稍後再試');
            isPublishing.value = false;
        }
    }

    //批量立即發布
    const handleBatchPublish = async () => {
      publishedCount.value = 0;
      batchResult.value = {success: 0, failed: 0, error: []};

      for(const activityId of selectedActivityId.value){
        try{
          await activityApi.publishActivity(activityId);
          batchResult.value.success++;
        }
        catch(error){
          console.error(`發布活動${activityId}失敗`,error);
          batchResult.value.failed++;

        const activity = draftActivities.value.find(a => a.id === activityId);
        batchResult.value.errors.push({
          id: activityId,
          title: activity?.title || `活動${activityId}`,
          message: error.response?.data?.message || '發布失敗'
        })
        }
        publishedCount.value++;
      }
      showBatchResult.value = true;
      await loadDraftActivities();
      selectedActivityId.value.clear();
    };

    //批量預約發布
    const handleBatchSchedulePublish = async () => {
      publishedCount.value = 0;
      batchResult.value = {success: 0, failed: 0, error: []};

      const scheduleDateTime = new Date(`${scheduleDate.value}T${scheduleTime.value}`);

      for(const activityId of selectedActivityId.value){
        try{
          await activityApi.schedulePublishActivity(activityId, {
            publishedAt: scheduleDateTime.toISOString()
          });
          batchResult.value.success++;
        }
        catch(error){
          console.log(`預約發布內容${activityId}失敗`,error);
          batchResult.value.failed++;

          const activity =draftActivities.value.find(a => a.id === activityId);
          batchResult.value.errors.push({
            id: activityId,
            title: activity?.title || `活動${activityId}`,
            message: error.response?.data?.message || '發布失敗'
          })
        }
        publishedCount.value++;
      }
      showBatchResult.value = true;
      await loadDraftActivities();
      selectedActivityId.value.clear();

    }

    //關閉批量結果對話框後
    const closeBatchResult = () => {
      showBatchResult.value = false;
      if(batchResult.value.failed === 0){
        router.push({name: 'activity-list-container'})
      }
    };

    //保存為草稿
    const saveDraft = () => {
        
        alert(`活動-${currentActivity.value.title}-已保存為草稿`);
        if(fromRoute.value){
                router.push( {name: 'activity-list-container'});
        }
        else{
            clearSelection();
        }
    }

    //返回編輯
    const goBack = () => {
    const activityId = Array.from(selectedActivityId.value)[0];
    router.push(`/admin/update-activity-container/${activityId}`)
    }
    //格式化活動狀態
    const getStatusLabel = (status) => {
        const labels = {
            'DRAFT': '草稿',
            'SCHEDULED': '預約發布',
            'PUBLISHED': '已發布',
            'CANCELLED': '已取消'
        }
        return labels[status] || status
    }
    // 格式化活動類型
    const getActivityTypeLabel = (type) => {
        const labels = {
            'REGULAR': '社課',
            'SPECIAL': '特殊活動',
            'TRAINING': '團練',
            'PERFORMANCE': '演出',
            'COMPETITION': '比賽'
        }
        return labels[type] || type
    }

    // 格式化時間
    const formatDateTime = (dateTime) => {
        if (!dateTime) return '-'
        const date = new Date(dateTime)
        return date.toLocaleString('zh-TW', {
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
        })
    }

    onMounted(async() => {
        if(fromRoute.value) {
            //從其他頁面帶id進來
            selectedActivityId.value.add(route.params.activityId);
            await loadActivityDetails(route.params.activityId);

        }
        else{
            await loadDraftActivities();
        }
    });
</script>

<style scoped>
.publish-activity-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  margin-bottom: 8px;
}

.subtitle {
  color: #666;
  font-size: 14px;
}

/* 活動選擇器 */
.activity-selector-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 24px;
}

.draft-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

.draft-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.draft-item:hover {
  border-color: #4CAF50;
  background: #f8fff9;
}

.draft-item.selected {
  border-color: #4CAF50;
  background: #e8f5e9;
}

.draft-info h3 {
  font-size: 18px;
  margin-bottom: 8px;
}

.meta {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #666;
}

.type-badge {
  background: #e3f2fd;
  color: #1976d2;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.btn-select {
  padding: 8px 16px;
  background: #4CAF50;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  white-space: nowrap;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.btn-text {
  background: none;
  border: none;
  color: #1976d2;
  cursor: pointer;
  padding: 4px 8px;
}

.btn-text:hover {
  text-decoration: underline;
}
.batch-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
}
.batch-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
}
.option-card {
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.option-card:hover {
  border-color: #4CAF50;
  background: #f8fff9;
}

.option-card.active {
  border-color: #4CAF50;
  background: #e8f5e9;
}
.progress-bar {
  height: 10px;
  background: #eee;
  border-radius: 6px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50, #81C784);
  transition: width 0.3s ease;
}


/* 其他樣式保持不變 ... */
</style>