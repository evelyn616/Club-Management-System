<template>
    <div class="update-activity-container">
        <h2>編輯活動</h2>
        <div v-if="hasRegistrations" class="warning-message">
            此活動已有{{ registrationCount }}個報名紀錄,修改活動會同步通知所有報名者。
        </div>
        
        <!-- Loading 狀態 -->
        <div v-if="loading" class="loading">載入中...</div>
        
        <form v-else @submit.prevent="handlesubmit" class="activity-form">
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
                <input v-model="form.coverImageUrl" type="text" id="coverImageUrl" maxlength="500" placeholder="範例:https://example.com/image.jpg" />
            </div>
            <div class="form-group">
                <label for="startTime">
                    開始時間 
                    <span v-if="hasRegistrations && isTimeChanged" class="changed-badge">(已修改)</span>
                </label>
                <input v-model="form.startTime" type="datetime-local" id="startTime" required @change="checkChanges"/>
            </div>
            <div class="form-group">
                <label for="endTime">結束時間</label>
                <input v-model="form.endTime" type="datetime-local" id="endTime" required />
            </div>
            <div class="form-group">
                <label for="location">
                    活動地點
                    <span v-if="hasRegistrations && isLocationChanged" class="changed-badge">(已修改)</span>
                </label>
                <input v-model="form.location" type="text" id="location" required maxlength="200" placeholder="請輸入活動地點" @change="checkChanges" />
            </div>
            <div class="form-group">
                <label for="maxParticipants">最大參與人數</label>
                <input v-model.number="form.maxParticipants" type="number" id="maxParticipants" min="1" placeholder="預設:無上限"/>
            </div>
            <div class="form-group">
                <label for="registrationDeadline">報名截止時間</label>
                <input v-model="form.registrationDeadline"
                       type="datetime-local"
                       id="registrationDeadline"
                       required />
            </div>
            <div class="form-group">
                <label for="feeAmount">費用金額</label>
                <input v-model.number="form.feeAmount"
                       type="number"
                       id="feeAmount"
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
                <button type="submit" class="submit-button" :disabled="loading">
                    {{ hasRegistrations ? '更新並通知報名者' : '更新活動' }}
                </button>
                <button type="button" @click="handleCancel" class="cancel-button">取消</button>
            </div>
        </form>
    </div>
</template>

<script setup>
    import { computed, onMounted, ref } from 'vue';
    import { activityApi } from '@/api/activity';
    import { useRoute, useRouter } from 'vue-router';
    import { useUserStore } from '@/stores/user';
   
    const userStore = useUserStore();
    const router = useRouter();
    const route = useRoute();
    
    
    const activityId = computed(() => route.params.activityId);

    // 表單資料
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

    const loading = ref(false);
    const hasRegistrations = ref(false);
    const registrationCount = ref(0);
    const originalData = ref({}); // 用於存儲原始活動資料
    
    const isTimeChanged = computed(() => {
        return form.value.startTime !== originalData.value.startTime || 
               form.value.endTime !== originalData.value.endTime;
    });
    
    const isLocationChanged = computed(() => {
        return form.value.location !== originalData.value.location;
    });

    // 檢查變更
    const checkChanges = () => {
        console.log('時間已變更:', isTimeChanged.value);
        console.log('地點已變更:', isLocationChanged.value);
    };

    // 表單提交處理
    const handlesubmit = async () => {
        const hasImportantChanges = isTimeChanged.value || isLocationChanged.value;
        
        if (hasRegistrations.value && hasImportantChanges) {
            const confirmUpdate = confirm(
                `此活動已有${registrationCount.value}個報名紀錄,修改活動時間或地點將會通知所有報名者。是否繼續?`
            );
            if (!confirmUpdate) {
                return; // 使用者取消更新
            }
        }
        
        loading.value = true;
        
        try {
            
            const response = await activityApi.updateActivity(activityId.value, form.value);
            
            console.log('活動更新成功:', response.data);
            alert('活動更新成功' + (hasImportantChanges ? ',已通知所有報名者。' : ''));
            
            // 跳轉回活動列表
            router.push({ name: 'ActivityList' });
        } catch (error) {
            console.error('更新活動失敗:', error);
            
            if (error.response?.status === 400) {
                alert('更新失敗:資料驗證錯誤');
            } else if (error.response?.status === 404) {
                alert('更新失敗:找不到此活動');
            } else {
                alert('更新活動失敗,請稍後再試');
            }
        } finally {
            loading.value = false;
        }
    };

    const handleCancel = () => {
        if (confirm('確定要取消編輯嗎?未保存的更改將丟失。')) {
            router.push({ name: 'ActivityList' });
        }
    };
    
    onMounted(async () => {
        console.log('=== UpdateActivity onMounted ===');
        console.log('Route params:', route.params);
        console.log('activityId:', activityId.value);
        
        // 驗證 activityId
        if (!activityId.value) {
            console.error('❌ Missing activityId');
            alert('缺少活動 ID');
            router.push({ name: 'activity-list-container' });
            return;
        }
        
        loading.value = true;
        
        try {
            // ⭐ 關鍵:調用 API 獲取活動詳情
            const response = await activityApi.getActivityDetails(activityId.value);
            
            console.log('✅ 活動資料載入成功:', response.data);
            
            // 處理時間格式(截取前 16 個字符以符合 datetime-local 格式)
            const data = { 
                ...response.data,
                startTime: response.data.startTime?.slice(0, 16) || '',
                endTime: response.data.endTime?.slice(0, 16) || '',
                registrationDeadline: response.data.registrationDeadline?.slice(0, 16) || ''
            };
            
            form.value = data;
            originalData.value = { ...form.value }; // 存儲原始資料
            
            console.log('活動資料:', form.value);
            
            // 設定報名相關資訊
            hasRegistrations.value = (response.data.hasRegistrations || 0) > 0;
            registrationCount.value = response.data.hasRegistrations || 0;
            
            console.log('報名狀態:', hasRegistrations.value);
            console.log('報名人數:', registrationCount.value);
            
            // 設定更新者
            if (userStore.userId) {
                form.value.updatedBy = userStore.userId;
                console.log('更新者:', form.value.updatedBy);
            }
            
        } catch (error) {
            console.error('❌ 載入活動詳情失敗:', error);
            
            if (error.response?.status === 404) {
                alert(`找不到活動 (ID: ${activityId.value})`);
            } else if (error.response?.status === 403) {
                alert('沒有權限訪問此活動');
            } else {
                alert('載入活動失敗,請稍後再試');
            }
            
            router.push({ name: 'ActivityList' });
        } finally {
            loading.value = false;
        }
    });
</script>

<style scoped>
.update-activity-container {
    max-width: 800px;
    margin: 0 auto;
    padding: 20px;
}

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