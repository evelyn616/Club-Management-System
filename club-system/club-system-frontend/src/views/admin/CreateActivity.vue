<template>
    <div class="create-activity-container">
        <h2>建立新活動</h2>
        <form @submit.prevent="handlesubmit" class="activity-form">
            <!-- 活動表單 -->
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
                <label for="startTime">開始時間</label>
                <input v-model="form.startTime" type="datetime-local" id="startTime" required />
            </div>
            <div class="form-group">
                <label for="endTime">結束時間</label>
                <input v-model="form.endTime" type="datetime-local" id="endTime" required />
            </div>
            <div class="form-group">
                <label for="location">活動地點</label>
                <input v-model="form.location" type="text" id="location" required maxlength="200" placeholder="請輸入活動地點" />
            </div>
            <div class="form-group">
                <label for="maxParticipants">最大參與人數</label>
                <input v-model.number="form.maxParticipants" type="number" id="maxParticipants" min="1" placeholder="預設：無上限"/>
            </div>
            <div class="form-group">
                <label for="registrationDeadline">報名截止時間</label>
                <input v-model = "form.registrationDeadline"
                       type = "datetime-local"
                       id = "registrationDeadline"
                       required />
            </div>
            <div class = "form-group">
                <label for = "feeAmount">費用金額</label>
                <input v-model.number = "form.feeAmount"
                       type = "number"
                       id = "feeAmount"
                       required />
            </div>
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
            <!-- 活動按鈕-->
            <div class="form-actions">
                <button type="submit"  class="submit-button">儲存活動</button>
                <button type="button" @click="handleCancel" class="cancel-button">取消</button>
            </div>
        </form>
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
    .warning-message {
    background-color: #fff3cd;
    border: 1px solid #ffc107;
    color: #856404;
    padding: 12px;
    border-radius: 4px;
    margin-bottom: 20px;
}

.loading {
    text-align: center;
    padding: 40px;
    font-size: 18px;
    color: #666;
}

.activity-form {
    background: white;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    margin-bottom: 5px;
    font-weight: bold;
    color: #333;
}

.changed-badge {
    color: #ff6b6b;
    font-size: 12px;
    font-weight: normal;
}

.form-group input,
.form-group textarea,
.form-group select {
    width: 100%;
    padding: 8px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 14px;
}

.form-group textarea {
    resize: vertical;
}

.form-actions {
    display: flex;
    gap: 10px;
    justify-content: flex-end;
    margin-top: 20px;
}

button {
    padding: 10px 20px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
}

.submit-button {
    background-color: #4CAF50;
    color: white;
}

.submit-button:hover {
    background-color: #45a049;
}

.submit-button:disabled {
    background-color: #ccc;
    cursor: not-allowed;
}

.cancel-button {
    background-color: #f0f0f0;
    color: #333;
}

.cancel-button:hover {
    background-color: #e0e0e0;
}
</style>