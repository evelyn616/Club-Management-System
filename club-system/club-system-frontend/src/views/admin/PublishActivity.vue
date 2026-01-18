<template>
  <div class="publish-activity-container">
    <!-- Loading 狀態 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>載入中...</p>
    </div>

    <!-- 主要內容 -->
    <div v-else-if="activity" class="publish-content">
      <!-- 標題 -->
      <div class="page-header">
        <h1>📢 發布活動設定</h1>
        <p class="subtitle">設定活動的發布方式</p>
      </div>

      <!-- 活動資訊預覽 -->
      <div class="activity-preview-card">
        <div class="card-header">
          <h2>📋 活動預覽</h2>
          <span class="status-badge" :class="`status-${activity.status?.toLowerCase()}`">
            {{ getStatusLabel(activity.status) }}
          </span>
        </div>

        <div class="activity-info">
          <div class="info-row">
            <span class="label">活動標題:</span>
            <span class="value">{{ activity.title }}</span>
          </div>
          <div class="info-row">
            <span class="label">活動描述:</span>
            <span class="value">{{ activity.description }}</span>
          </div>
          <div class="info-row">
            <span class="label">活動地點:</span>
            <span class="value">{{ activity.location }}</span>
          </div>
          <div class="info-row">
            <span class="label">活動類型:</span>
            <span class="value">{{ getActivityTypeLabel(activity.activityType) }}</span>
          </div>
          <div class="info-row">
            <span class="label">開始時間:</span>
            <span class="value">{{ formatDateTime(activity.startTime) }}</span>
          </div>
          <div class="info-row">
            <span class="label">結束時間:</span>
            <span class="value">{{ formatDateTime(activity.endTime) }}</span>
          </div>
          <div class="info-row">
            <span class="label">報名截止時間:</span>
            <span class="value">{{ formatDateTime(activity.registrationDeadline) }}</span>
          </div>
          <div class="info-row">
            <span class="label">費用金額:</span>
            <span class="value">{{ activity.feeAmount }}</span>
          </div>
          <div class="info-row">
            <span class="label">最大參與人數:</span>
            <span class="value">{{ activity.maxParticipants || '無上限' }}</span>
          </div>
          <div class="info-row">
            <span class="label">建立者:</span>
            <span class="value">{{ activity.createdBy }}</span>
          </div>
        </div>

        <div class="action-buttons">
          <button @click="goToEdit" class="btn-secondary">
            ✏️ 返回編輯
          </button>
        </div>
      </div>

      <!-- 發布選項 -->
      <div class="publish-options-card">
        <div class="card-header">
          <h2>⚙️ 選擇發布方式</h2>
        </div>

        <div class="publish-type-selector">
          <!-- 立即發布選項 -->
          <div 
            class="option-card" 
            :class="{active: publishType === 'immediate'}" 
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
            <p class="description">活動將會立即對外發布，並開放報名。</p>
          </div>

          <!-- 預約發布選項 -->
          <div 
            class="option-card" 
            :class="{active: publishType === 'scheduled'}" 
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
            <p class="description">選擇未來的日期與時間來發布活動。</p>

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
                  :class="{valid: isScheduleValid, invalid: !isScheduleValid}"
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

      <!-- 操作按鈕 -->
      <div class="action-section">
        <div class="button-group">
          <button 
            @click="saveDraft" 
            class="btn-outline" 
            :disabled="isPublishing"
          >
            💾 儲存為草稿
          </button>

          <button 
            v-if="publishType === 'immediate'" 
            @click="confirmPublish" 
            class="btn-primary" 
            :disabled="isPublishing"
          >
            <span v-if="isPublishing">發布中...</span>
            <span v-else>🚀 確認立即發布</span>
          </button>

          <button 
            v-else 
            @click="confirmSchedulePublish" 
            class="btn-primary" 
            :disabled="isPublishing || !isScheduleValid"
          >
            <span v-if="isPublishing">設定中...</span>
            <span v-else>⏰ 確認預約發布</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 錯誤狀態 -->
    <div v-else class="error-container">
      <p>❌ 無法載入活動資訊</p>
      <button @click="router.push({ name: 'ActivityList' })" class="btn-secondary">
        返回活動列表
      </button>
    </div>
  </div>
</template>

<script setup>
    import { onMounted, ref ,computed} from 'vue';
    import { activityApi } from '@/api/activity';
    import { useRoute, useRouter } from 'vue-router';


    const router = useRouter();
    const route = useRoute();
    const activityId = route.params.id;

    const activity = ref(null);
    const loading = ref(false);
    const isPublishing = ref(false);

    //props
    

    //發布選項
    const publishType = ref('immediate'); //立即發布 or 預約發布(immediate / scheduled)
    const scheduledTime = ref(''); //預約發布時間
    const scheduledDate = ref(''); //預約發布日期

    //載入活動詳情
    const loadActivityDetails = async () => {
        loading.value = true;
        try {
            const response = await activityApi.getActivityDetails(activityId.value);
            activity.value = response.data;
            console.log('活動詳情載入成功:', activity.value);

            // 如果活動不是草稿狀態，跳回列表
        if (activity.value.status !== 'DRAFT') {
            alert('⚠️ 此活動已經發布過了')
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
    const formatScheduledDateTime = () => {
        if(!scheduledDate.value || !scheduledTime.value){
            alert('請選擇完整的預約發布日期與時間');
            return null;
        }
        const date = new Date(`${scheduledDate.value}T${scheduledTime.value}`);
        return date.toLocaleDateString('zh-TW', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    //驗證預約時間是否有效
    const isScheduledValid = computed(() => {
        if(!scheduledDate.value || !scheduledTime.value){
            return false;
        }
        const scheduledDateTime = new Date(`${scheduledDate.value}T${scheduledTime.value}`);
        const now = new Date();
        return scheduledDateTime > now;
    })

    //立即發布設定
    const confirmPublish = async () => {
        if(!confirm(`確定要立即發布-${activity.value.title}-活動嗎？`)){
            return;
        }

        isPublishing.value = true;
        try {
            await activityApi.publishActivity(activityId.value)
            alert(`活動-${activity.value.title}-已成功發布`);
            router.push({name: 'ActivityList'});
        } catch (error) {
            console.error('發布活動失敗:', error);
            if (error.response?.status === 400) {
            alert('❌ 發布失敗：活動資料不完整，請檢查必填欄位。')
        } else if (error.response?.status === 409) {
            alert('❌ 此活動已經發布過了。')
        } else {
            alert('❌ 發布失敗，請稍後再試。')
        }
            
        }
        finally {
            isPublishing.value = false;
        }
    }

    //預約發布設定
    const confirmScheduledPublish = async () => {
        if(!isScheduledValid.value){
            alert('預約發布時間無效，請選擇未來的日期與時間');
            return;
        }

        const scheduledDateTime = new Date(`${scheduledDate.value}T${scheduledTime.value}`);
        const displayTime = formatScheduledDateTime.value;

        if(!confirm(`確定要預約發布-${activity.value.title}-活動於${displayTime}嗎？`)){
            return;
        }

        isPublishing.value = true;
        try {
            await activityApi.schedulePublishActivity(activityId.value, {
                publishAt: scheduledDateTime.toISOString()
            });
            alert(`活動-${activity.value.title}-已成功預約發布於${displayTime}`);
            router.push({name: 'ActivityList'});
        } catch (error) {
            console.error('預約發布活動失敗:', error);
            alert('預約發布活動失敗，請稍後再試');
            isPublishing.value = false;
        }
    }

    //保存為草稿
    const saveDraft = () => {
        if(!confirm(`確定要將-${activity.value.title}-活動保存為草稿嗎？`)){
            return;
        }
        alert(`活動-${activity.value.title}-已保存為草稿`);
        router.push({name: 'ActivityList'});
    }

    //返回編輯
    const goBack = () => {
    router.push(`/update-activity-container/${id}`)
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

    onMounted(() => {
        loadActivityDetails();
    });
</script>