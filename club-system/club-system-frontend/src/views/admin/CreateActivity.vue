<template>
  <div class="create-activity-container">
    <div class="page-wrapper">

      <!-- 標題區 -->
      <div class="page-header">
        <h1>建立新活動</h1>
        <p>請填寫以下活動資訊</p>
      </div>

      <form @submit.prevent="handlesubmit" class="activity-form">

        <!-- 基本資訊 -->
        <div class="form-section">
          <h3 class="section-title">基本資訊</h3>

          <div class="form-group">
            <label for="title">活動標題</label>
            <input v-model="form.title" type="text" id="title" required maxlength="100" placeholder="請輸入活動名稱"/>
          </div>

          <div class="form-group">
            <label for="description">活動描述</label>
            <textarea v-model="form.description" id="description" rows="4" maxlength="5000" placeholder="請輸入活動描述"></textarea>
          </div>

          <div class="form-group">
            <label for="coverImageUrl">封面圖片 URL</label>
            <input v-model="form.coverImageUrl" type="text" id="coverImageUrl" maxlength="500" placeholder="範例：https://example.com/image.jpg" />
          </div>

          <div class="form-group">
            <label for="location">活動地點</label>
            <input v-model="form.location" type="text" id="location" required maxlength="200" placeholder="請輸入活動地點" />
          </div>
        </div>

        <!-- 時間與人數 -->
        <div class="form-section">
          <h3 class="section-title">時間與人數設定</h3>

          <div class="form-group">
            <label for="startTime">開始時間</label>
            <input v-model="form.startTime" type="datetime-local" id="startTime" required />
          </div>

          <div class="form-group">
            <label for="endTime">結束時間</label>
            <input v-model="form.endTime" type="datetime-local" id="endTime" required />
          </div>

          <div class="form-group">
            <label for="registrationDeadline">報名截止時間</label>
            <input v-model="form.registrationDeadline"
                   type="datetime-local"
                   id="registrationDeadline"
                   required />
          </div>

          <div class="form-group">
            <label for="maxParticipants">最大參與人數</label>
            <input v-model.number="form.maxParticipants"
                   type="number"
                   id="maxParticipants"
                   min="1"
                   placeholder="預設：無上限"/>
          </div>

          <div class="form-group">
            <label for="feeAmount">費用金額</label>
            <input v-model.number="form.feeAmount"
                   type="number"
                   id="feeAmount"
                   required />
          </div>
        </div>

        <!-- 類型與對象 -->
        <div class="form-section">
          <h3 class="section-title">活動分類設定</h3>

          <div class="form-group">
            <label for="activityType">活動類型</label>
            <select v-model="form.activityType" id="activityType" required>
              <option value="">請選擇活動類型</option>
              <option value="REGULAR">社課</option>
              <option value="SPECIAL">特別活動</option>
              <option value="TRAINING">團練</option>
              <option value="PERFORMANCE">演出</option>
              <option value="COMPETITION">比賽</option>
            </select>
          </div>

          <div class="form-group">
            <label for="targetAudience">目標對象</label>
            <select v-model="form.targetAudience" id="targetAudience" required>
              <option value="">請選擇目標對象</option>
              <option value="ALL">所有人</option>
              <option value="MEMBER_ONLY">社員限定</option>
              <option value="MANAGER_ONLY">幹部限定</option>
            </select>
          </div>
        </div>

        <!-- 操作按鈕 -->
        <div class="action-bar">
          <button type="button" @click="handleCancel" class="primary-cancel-btn">
            取消
          </button>
          <button type="submit" class="primary-btn">
            儲存活動
          </button>
        </div>

      </form>
    </div>
  </div>
</template>
<script setup>
    import { onMounted, ref } from 'vue';
    import { activityApi } from '@/api/activity';
    import {useUserStore} from '@/stores/user';
    import { useRouter } from 'vue-router';
   
    const router = useRouter();

    //表單
    const form = ref({
        title: '',
        description: '',
        coverImageUrl: '',
        startTime: '',
        endTime: '',
        location: '',
        maxParticipants: null,
        registrationDeadline: '',
        feeAmount: null,
        activityType: '',
        targetAudience: '',
        createdBy: '' ,
    });

    //表單提交處理
    const handlesubmit = async () => {
        try {
            // 呼叫 API 建立活動
            const response = await activityApi.createActivity(form.value);
            const newActivityId = response.data.id;
            console.log('活動建立成功:', response.data);
            alert(`活動-${form.value.title}-建立成功`);

            router.push({
                name: 'publish-activity-container',
                params: { activityId: newActivityId },
            });
        } catch (error) {
            console.error('建立活動失敗:', error);
            alert('建立活動失敗，請稍後再試');
        }
    };

    const handleCancel = () => {
        // 返回上一頁或清空表單
        window.history.back();
    };
    const userStore = useUserStore();
    onMounted(() => {
        // 初始化表單或其他操作
        if(userStore.userId){
            form.value.createdBy = userStore.userId;
            console.log('建立者:', form.value.createdBy);
        }
    });

</script>

<style scoped>

.create-activity-container {
  min-height: 100vh;
  background: #fafafa;
  padding: 3rem 2rem;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 2.5rem;
}

.page-header h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.5rem;
  margin-top: 0.5rem;
}

.page-header p {
  color: #6b7280;
  font-size: 0.95rem;
}

/* 卡片 */
.form-card {
  background: #fafafa;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 2rem;
  margin-bottom: 2rem;
}

.section-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  color: #374151;
}

/* 表單 */
.form-group {
  margin-bottom: 1.5rem;
}

.form-row {
  display: flex;
  gap: 1.5rem;
}

.form-row .form-group {
  flex: 1;
}

label {
  display: block;
  font-weight: 500;
  margin-bottom: 0.5rem;
  color: #374151;
}

input,
select,
textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.95rem;
  transition: border-color 0.2s, box-shadow 0.2s;
  background: white;
}

input:focus,
select:focus,
textarea:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.1);
  outline: none;
}

textarea {
  min-height: 120px;
  resize: vertical;
}

/* 按鈕 */
.action-bar {
  display: flex;
  justify-content: flex-end;
}

.primary-btn {
  background: #2563eb;
  color: white;
  padding: 0.8rem 1.6rem;
  border-radius: 6px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  margin-bottom: 1rem;
  margin-left: 1rem;
}

.primary-cancel-btn {
  background: #6b7280;
  color: white;
  padding: 0.8rem 1.6rem;
  border-radius: 6px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  margin-bottom: 1rem;
}

.primary-cancel-btn:hover {
  background: #4b5563;
}

.primary-btn:hover {
  background: #1e40af;
}
</style>