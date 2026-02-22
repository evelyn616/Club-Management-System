<template>
    <div class="registration-detail-container">
        <!--返回按鈕-->
        <button @click="goBack()" class="btn-back">返回</button>
        <!--活動資訊-->
        <div v-if="loading" class="loading">載入中...</div>
        <div v-else-if="activity" class="activity-card">
            <h2> {{  activity.title }}</h2>
            <div class="activity-info-grid">
                <div class="info-item">
                    <span class="label">活動時間：</span>
                    <span class="value"> {{ activity.startTime }}</span>
                </div>
                <div class="info-item">
                    <span class="label">截止時間：</span>
                    <span class="value"> {{ formatDateTime(activity.registrationDeadline) }}</span>
                </div>
                <div class="info-item">
                    <span class="label">地點：</span>
                    <span class="value"> {{ activity.location }}</span>
                </div>
                <div class="info-item">
                    <span class="label">費用：</span>
                    <span class="value"> {{ activity.feeAmount }}元</span>
                </div>
            </div>

        <!--報名進度-->
        <div class="progress-section">
            <div class="progress-header">
                <span>報名進度：</span>
                <span class="progress-text">{{ registrations.length}} 
                    <span v-if="activity.maxParticipants"> /{{ activity.maxParticipants }}人</span>
                </span>
            </div>
            <div class="progress-bar">
                <div class="progress-fill" :style="{width: progressPercentage + '%'}" :class="{ 'progress-high': progressPercentage >= 80 }">
                </div>
            </div>
        </div>
        </div>
        
        <!--名單-->
        <div class="registrations-section">
            <div class="registrations-header">
                <button @click="exportList" class="btn-export">匯出表單</button>
            </div>
            <div v-if="loading" class="loading">載入中...</div>
            <div v-else class="table-container">
                <table class="registrations-table">
                    <thead>
                        <tr>
                            <th>報名編號</th>
                            <th>姓名</th>
                            <th>會員id</th>
                            <th>報名時間</th>
                            <th>繳費狀態</th>
                            <th>操作</th>
                            
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(reg, index) in registrations" :key="reg.id">
                            <td>{{ index + 1 }}</td>
                            <td>{{ reg.userName || reg.userId }}</td>
                            <td>{{ reg.userId }}</td>
                            <td>{{ formatDateTime(reg.registrationTime) }}</td>
                            <td>
                                <span class="payment-badge" :class="`payment-${reg.paymentStatus?.toLowerCase()}`">
                                    {{getPaymentStatusLabel(reg.paymentStatus)}}
                                </span>
                            </td>
                            <td>
                                <button @click="cancelRegistration(reg)" class="btn-cancel" :disabled="canceling">取消報名</button>
                                <button v-if="reg.paymentStatus === 'PENDING'" @click ="sendPaymentReminder(reg)" class ="remind" :disable = "sending">提醒繳費</button>
                            </td>
                        </tr>

                    </tbody>
                </table>

                <!--空狀態-->
                <div v-if="!loading && registrations.length === 0" class="empty-state">
                    <p>目前還沒有人報名喔~</p>
                </div>
            </div>
        </div>
        


    </div>

</template>
<script setup>
import { activityApi } from '@/api/activity';
import { registrationApi } from '@/api/registration';
import { callWithAsyncErrorHandling, computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';


const route = useRoute();
const router = useRouter();

//資料
const activity = ref(null);
const registrations = ref([]);
const loading = ref(false);
const canceling = ref(false);
const sending = ref(false);

//從路由取得活動id
const activityId = route.params.activityId;

//載入活動資料
const loadActivity = async() => {
    try{
        
        const response = await activityApi.getActivityDetails(activityId);
        activity.value = response.data;
        console.log('活動資訊：',activity.value);
    }
    catch(error){
        console.log('載入活動失敗',error);
        alert('載入活動失敗，請稍後在試..');
    }
}

//載入報名資料
const loadRegistrations = async () => {
    try{
        const response = await registrationApi.getActivityRegistrations(activityId);
        registrations.value = response.data;
        console.log('報名資訊：',registrations.value);
    }
    catch(error){
        console.log('載入報名資料失敗',error);
        alert('載入報名資料失敗，請稍後在試..');
    }
}

//載入所有資料
const loadData = async () => {
    loading.value = true;
    try{
        await Promise.all(
            [loadActivity(),
            loadRegistrations()]
        )
    }
    finally{
        loading.value = false;
    }
}

//計算報名進度百分比
const progressPercentage = computed(() => {
    if(!activity.value || !activity.value.maxParticipants){
        return 0;
    }

    return Math.round((registrations.value.length/activity.value.maxParticipants)*100)
})

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

// 格式化繳費狀態
const getPaymentStatusLabel = (status) => {
  const labels = {
    'NOT_REQUIRED': '無須繳費',
    'PENDING': '待繳費',
    'PAID': '已繳費',
    'CANCELLED': '已取消',
    'REFUNDED': '已退款',
    'PARTIAL_REFUNDED': '部分退款'
  }
  return labels[status] || status
}

//返回列表
const goBack = () => {
    router.push({name: 'registrations-overview-container'});
}

//取消報名
const cancelRegistration = async (registration) => {
    if (!confirm(`確定要取消 ${registration.userId} 的報名嗎？`)) {
    return
  }
  canceling.value = true;
  try{
    await registrationApi.cancel(registration.id);
    alert('取消報名成功');
    //重新載入名單
    await loadRegistrations();
  }
  catch(error){
    console.log('取消報名失敗',error);
    alert('取消報名失敗')

  }
  finally{
    canceling.value = false;
  }

}
//提醒繳費
const sendPaymentReminder = async (registration) => {
  if (!confirm(`確定要發送繳費提醒給 ${registration.userId}嗎?`)) {
    return
    
  }

  sending.value = true;
  try {
    await registrationApi.sendPaymentReminder(registration.id);
    alert('繳費提醒已發送');
    
  } catch (error) {
    console.error('發送繳費提醒失敗', error);
    alert('發送失敗，請稍後再試');
    
    
  }finally{
    sending.value =false;
  }
  
}


// 匯出名單（先留空，未來實作）
const exportList = () => {
  alert('匯出功能開發中...')
}

onMounted(() => {
    loadData();
})
</script>
<style scoped>
.registration-detail-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 返回按鈕 */
.btn-back {
  padding: 10px 20px;
  background: #f5f5f5;
  color: #333;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  margin-bottom: 24px;
  transition: all 0.2s;
}

.btn-back:hover {
  background: #e0e0e0;
  transform: translateX(-4px);
}

/* 載入狀態 */
.loading {
  text-align: center;
  padding: 60px;
  font-size: 18px;
  color: #666;
}

/* 活動資訊卡片 */
.activity-card {
  background: white;
  padding: 32px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 32px;
}

.activity-card h2 {
  margin: 0 0 24px 0;
  color: #333;
  font-size: 28px;
}

.activity-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
}

.info-item .label {
  font-weight: 600;
  color: #555;
  min-width: 120px;
}

.info-item .value {
  color: #333;
}

/* 報名進度條 */
.progress-section {
  margin-top: 24px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 600;
  color: #333;
}

.progress-text {
  font-size: 18px;
  color: #2196f3;
}

.progress-bar {
  width: 100%;
  height: 32px;
  background: #e0e0e0;
  border-radius: 16px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4caf50 0%, #66bb6a 100%);
  transition: width 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
}

.progress-fill.progress-high {
  background: linear-gradient(90deg, #ff9800 0%, #ffa726 100%);
}

/* 報名名單區塊 */
.registrations-section {
  background: white;
  padding: 32px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h3 {
  margin: 0;
  color: #333;
  font-size: 22px;
}

.btn-export {
  padding: 10px 20px;
  background: #4caf50;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-export:hover {
  background: #45a049;
  transform: translateY(-2px);
}

/* 表格 */
.table-container {
  overflow-x: auto;
}

.registrations-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}

.registrations-table th {
  background: #f5f5f5;
  padding: 16px;
  text-align: center;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #ddd;
  white-space: nowrap;
}

.registrations-table td {
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  color: #555;
  text-align: center;
  vertical-align: middle;
}

.registrations-table tbody tr:hover {
  background: #f9f9f9;
}

/* 繳費狀態標籤 */
.payment-badge {
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
}

.payment-not_required {
  background: #e8f5e9;
  color: #2e7d32;
}

.payment-pending {
  background: #fff3e0;
  color: #e65100;
}

.payment-paid {
  background: #e3f2fd;
  color: #1565c0;
}

.payment-cancelled {
  background: #fce4ec;
  color: #c2185b;
}

.payment-refunded {
  background: #f3e5f5;
  color: #7b1fa2;
}

.payment-partial_refunded {
  background: #f3e5f5;
  color: #7b1fa2;
}

/* 取消報名按鈕 */
.btn-cancel {
  padding: 6px 16px;
  background: #f44336;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-cancel:hover:not(:disabled) {
  background: #d32f2f;
  transform: translateY(-2px);
}

.btn-cancel:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 提醒繳費按鈕 */
.btn-remind {
    padding: 6px 16px;
    background: #ff9800;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 600;
    transition: all 0.2s;
    margin-left: 8px;
}

.btn-remind:hover:not(:disabled) {
    background: #f57c00;
    transform: translateY(-2px);
}

.btn-remind:disabled {
    background: #ccc;
    cursor: not-allowed;
}

/* 空狀態 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 18px;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .activity-info-grid {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>