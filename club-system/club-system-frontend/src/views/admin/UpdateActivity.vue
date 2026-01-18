<template>
    <div class="update-activity-container">
        <h2>編輯活動</h2>
        <div v-if="hasRegistrations" class="warning-message">此活動已有{{ registrationCount }}個報名紀錄，修改活動會同步通知所有報名者。</div>
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
                <label for="startTime">開始時間 <span v-if="hasRegistrations && isTimeChanged" class="changed-badge"></span></label>
                <input v-model="form.startTime" type="datetime-local" id="startTime" required @change="checkChanges"/>
            </div>
            <div class="form-group">
                <label for="endTime">結束時間</label>
                <input v-model="form.endTime" type="datetime-local" id="endTime" required />
            </div>
            <div class="form-group">
                <label for="location">活動地點
                    <span v-if="hasRegistrations && isLocationChanged" class="changed-badge">(已修改)</span>
                </label>
                <input v-model="form.location" type="text" id="location" required maxlength="200" placeholder="請輸入活動地點" @change="checkChanges" />
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
                <button type="submit"  class="submit-button">{{ hasRegistrations ? '更新並通知報名者' : '更新活動' }}</button>
                <button type="button" @click="handleCancel" class="cancel-button">取消</button>
            </div>
        </form>
    </div>
</template>

<script setup>
    import { computed, onMounted, ref } from 'vue';
    import { activityApi } from '@/api/activity';
    import { useRoute, useRouter } from 'vue-router';
    import {useUserStore} from '@/stores/user';
   
    const userStore = useUserStore();
    const router = useRouter();
    const route = useRoute();
    const activityId = route.params.id;

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
        updatedBy: ''
        
    });

    const hasRegistrations = ref(true);
    const registrationCount = ref(15);
    const originalData = ref({}); // 用於存儲原始活動資料
    const isTimeChanged = computed(() => {
        return form.value.startTime !== originalData.value.startTime || 
        form.value.endTime !== originalData.value.endTime;
    });
    const isLocationChanged = computed(() => {
        return form.value.location !== originalData.value.location;
    });

    //表單提交處理
    const handlesubmit = async () => {
        const hasImportantChanges = isTimeChanged.value || isLocationChanged.value;
            if (hasRegistrations.value && hasImportantChanges) {
                const confirmUpdate = confirm(`此活動已有${registrationCount.value}個報名紀錄，修改活動時間或地點將會通知所有報名者。是否繼續？`);
                if (!confirmUpdate) {
                    return; // 使用者取消更新
                }
            }
        try {
            
            // 呼叫 API 更新活動
            const response = await activityApi.updateActivity(activityId, form.value);
            alert('活動更新成功') + (hasImportantChanges ? '，已通知所有報名者。' : '');
            console.log('活動更新成功:', response.data);
            alert('活動更新成功');
        } catch (error) {
            console.error('更新活動失敗:', error);
            alert('更新活動失敗，請稍後再試');
        }
    };

    const handleCancel = () => {
        // 返回上一頁或清空表單
        window.history.back();
    };
    
    
    onMounted(async() => {
        // 初始化表單或其他操作
        
            const response = await activityApi.getActivityDetails(activityId);

            const data = { ...response.data , 
            startTime: response.data.startTime.slice(0,16),
            endTime: response.data.endTime.slice(0,16),
            registrationDeadline: response.data.registrationDeadline.slice(0,16)
            };
            form.value = data;
            originalData.value = { ...form.value }; // 存儲原始資料
            console.log('活動資料:', form.value);

            hasRegistrations.value = response.data.hasRegistrations > 0 ;
            registrationCount.value = response.data.hasRegistrations || 0;

            if(userStore.userId){
                form.value.updatedBy = userStore.userId;
                console.log('更新者:', form.value.updatedBy);
            }
        
    });

</script>