<template>
    <div class="registrations-overview-container">
        <h1>報名紀錄總覽</h1>
        <!--頂部的KPI卡片總覽(可以加上視覺化)-->
        <div class="stats-cards">
            <div class="stats-card">
                <div class="card-info">
                    <p class="card-label">總報名人次</p>
                    <p class="card-value">{{ totalRegistrations }}</p>
                </div>
            </div>
            <div class="stats-card">
                <div class="card-info">
                    <p class="card-label">有報名紀錄的活動數</p>
                    <p class="card-value">{{activitiesWithRegistrations}}</p>
                </div>
            </div>
            <div class="stats-card">
                <div class="card-info">
                    <p class="card-label">即將額滿</p>
                    <p class="card-value">{{nearlyFullActivities}}</p>
                </div>
            </div>
            <div class="stats-card">
                <div class="card-info">
                    <p class="card-label">本日新增報名數</p>
                    <p class="card-value">{{newRegistrationsToday}}</p>
                </div>
            </div>
        </div>
        <!--篩選器-->
        <div class="filters">
            <div class="filter-group">
                <label>活動類型：</label>
                <select v-model="selectedType">
                    <option value="">全部</option>
                    <option value="REGULAR">社課</option>
                    <option value="SPECIAL">特殊活動</option>
                    <option value="TRAINING">團練</option>
                    <option value="PERFORMANCE">演出</option>
                    <option value="COMPETITION">比賽</option>
                </select>
            </div>
            <div class="filter-group search-right">
                <label>搜尋</label>
                <input v-model="searchKeyword" type="text" placeholder="搜尋活動..." @input="handleSearch" class="search-input" />
            </div>
        </div>
        <!--中間的活動列表-->
        <div class="table-container">
            <p v-if="loading">載入中...</p>
            <table v-else class="activities-table">
                <thead>
                    <tr>
                    <th>活動ID</th>
                    <th>活動名稱</th>
                    <th>活動時間</th>
                    <th>報名截止</th>
                    <th>報名人數</th>
                    <th>報名率</th>
                    <th>狀態</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                    <tr v-for="activity in filteredActivities" :key="activity.id">
                        <td>{{ activity.id }}</td>
                        <td>{{ activity.title }}</td>
                        <td>{{ formatDateTime(activity.startTime) }}</td>
                        <td>
                            <span :class="getDeadlineClass(activity.registrationDeadline)">
                                {{ formatDeadline(activity.registrationDeadline) }}
                            </span>
                        </td>
                        <td>{{ activity.registrationCount || 0 }}
                            <span v-if="activity.maxParticipants">/{{ activity.maxParticipants }}</span>
                        </td>
                        <td>
                            <span :class="getRateClass(activity.registrationRate)">
                                {{ formatRate(activity.registrationRate) }}
                            </span>
                        </td>
                        <td>
                            <span class="status-badge" :class="`status-${activity.status?.toLowerCase()}`">
                                {{getStatusLabel(activity.status)}}
                            </span>
                        </td>
                        <td>
                            <button @click="goToDetail(activity.id)" class="btn-view">🔍查看名單</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        
        <!--空狀態-->
            <div v-if="!loading && filteredActivities.length === 0" class="empty-state">
                <p>目前沒有任何活動喔~</p>
            </div>
        </div>
    </div>
</template>

<script setup>
import { activityApi } from '@/api/activity';
import { registrationApi } from '@/api/registration';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';


const router = useRouter();

//資料
const activities = ref([]);
const loading = ref(false);
const selectedType = ref('');
const searchKeyword = ref('');

//載入活動資料
const loadActivities = async () => {
    loading.value = true;

    try{
        const response = await activityApi.getAllActivities();
        activities.value = response.data;

        //載入報名人數
        for(let activity of activities.value){
            try{

                const countResponse = await registrationApi.getActivityRegistrationCount(activity.id);
                activity.registrationCount = countResponse.data;

                //報名紀錄
                const registrationsResponse = await registrationApi.getActivityRegistrations(activity.id);
                const registrations = registrationsResponse.data;

                //計算今天的報名數
                const today = new Date();
                today.setHours(0,0,0,0); 
                activity.todayRegistrations = registrations.filter(reg => {
                    const regDate = new Date(reg.registrationTime);
                    regDate.setHours(0,0,0,0);
                    //把時分秒過濾，只看日期是否一樣，對比日期，一樣的就選出來

                    return regDate.getTime() === today.getTime();
                }).length;


                //計算報名率
                if(activity.maxParticipants){
                    activity.registrationRate = activity.registrationCount / activity.maxParticipants;

                }
                else{
                    activity.registrationRate = null;
                }
            }
            catch(error){
                console.log(`載入活動${activity.id}報名數失敗`,error)
                activity.registrationCount = 0;
                activity.registrationRate = 0 ;
                activity.todayRegistrations = 0
            }

        }
        console.log('活動列表載入完成:',activities.value)

    }
    catch(error){
        console.log('載入活動失敗',error)
        alert('載入失敗，請稍後在試..')

    }
    finally{
        loading.value = false;
    }
}

//篩選機制
const filteredActivities = computed (() => {
    let result = [...activities.value]; //複製活動列表

    //活動類型篩選
    if(selectedType.value){
        result = result.filter(a => a.activityType === selectedType.value)
    }

    //關鍵字查詢篩選
    if(searchKeyword.value){
        const keyword = searchKeyword.value.toLowerCase();
        result = result.filter(a => {
            const title = a.title ? a.title.toLowerCase() : '';
            const desc = a.description ? a.description.toLowerCase() : '';  // ← 改這裡：describtion → description，並加判斷
            return title.includes(keyword) || desc.includes(keyword);
        })
    }

    return result;
})

//頂部卡片統計
//報名總人數
const totalRegistrations = computed(() => {
    return activities.value.reduce((sum, a) => sum + (a.registrationCount || 0), 0)
     //0為初始值，sum為累加值,加上下一個活動的報名人數
})

//有報名資料的活動數
const activitiesWithRegistrations = computed(() => {
    return activities.value.filter(a => (a.registrationCount || 0) > 0).length;
})

//快要額滿的活動
const nearlyFullActivities = computed(() => {
    return activities.value.filter(a => a.registrationRate && a.registrationRate>= 0.8).length;
})

//本日新增報名數
const newRegistrationsToday = computed(() => {
    const today = new Date();
    today.setHours(0,0,0,0);
    return activities.value.reduce((count , activity) => { 
        return count + (activity.todayRegistrations || 0)},0)
})

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

//格式化報名截止時間(含倒數顯示)
const formatDeadline =(deadline) => {
    if(!deadline) return '-';

    const now = new Date();
    const deadlineDate = new Date(deadline);
    const diffTime = deadlineDate - now; //相差多久
    const diffDays = Math.ceil(diffTime / (1000 * 60 *60 *24)) 
    //差幾天，換算成天數後，取大於該數最近的數字

    const dateStr = formatDateTime(deadline); //顯示的字串

    if(diffDays < 0)return `${dateStr} (已截止)`;
    if(diffDays === 0) return `${dateStr} (今天截止)`;
    if(diffDays > 0) return `${dateStr} (剩下 ${diffDays}天)`;

    return dateStr
}

//報名截止的class
const getDeadlineClass = (deadline) => {
    if(!deadline) return '';

    const now = new Date();
    const deadlineDate = new Date(deadline);
    const diffTime = deadlineDate - now; 
    const diffDays = Math.ceil(diffTime / (1000 * 60 *60 *24)) 

    if(diffDays < 0) return 'deadline-passed';
    if(diffDays <= 3) return 'deadline-urgent';
    if(diffDays <= 7) return 'deadline-warning';

    return 'deadline-normal';
    
}

//格式化報名率
const formatRate = (rate) => {
    if(rate === null || rate === undefined) return '-';

    return `${Math.round(rate * 100)}%`//四捨五入
}

//報名率class
const getRateClass = (rate) => {
    if(!rate) return '';
    if(rate >= 0.9) return 'rate-high';
    if(rate >= 0.6) return 'rate-medium';
    return 'rate-low';
}

//跳轉detail頁面
const goToDetail = (activityId) => {
    router.push({
        name: 'registration-detail-container',
        params: { activityId }
    })
}

//初始化
onMounted(() => {
    loadActivities();
})

</script>

<style scoped>
.registrations-overview-container {
  padding: 24px;
  max-width: 100%;
  margin: 0 auto;
}

h1 {
  margin-bottom: 24px;
  color: #333;
}

/* 統計卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  font-size: 36px;
}

.stat-info {
  flex: 1;
}

.stat-label {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.stat-value {
  margin: 4px 0 0 0;
  font-size: 28px;
  font-weight: 700;
  color: #333;
}

/* 篩選器 */
.filters {
  display: flex;
  justify-content: space-between;  /* ← 左右分開 */
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 搜尋框推到右邊 */
.search-right {
  margin-left: auto;  /* ← 推到最右邊 */
}

.filter-group label {
  font-weight: 600;
  color: #555;
}

.filter-group select,
.filter-group input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.filter-group input {
  width: 300px;
}

/* 表格 */
.activities-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;  /* 固定表格佈局 */
  min-width: 1150px;  /* 設定最小寬度，避免欄位被壓縮 */
}

.activities-table th {
  background: #f5f5f5;
  padding: 16px 8px;  /* 左右padding改小一點 */
  text-align: center;  /* 表頭置中 */
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #ddd;
  white-space: nowrap;  /* 不換行 */
  vertical-align: middle;  /* 垂直置中 */
}

.activities-table td {
  padding: 16px 8px;  /* 左右padding改小一點 */
  border-bottom: 1px solid #e0e0e0;
  color: #555;
  vertical-align: middle;  /* ← 加這個，讓所有內容垂直置中對齊 */
}

/* 活動名稱靠左對齊 */
.activities-table th:nth-child(2),
.activities-table td:nth-child(2) {
  text-align: left;
}

/* 其他欄位置中 */
.activities-table th:nth-child(1),
.activities-table td:nth-child(1),
.activities-table th:nth-child(3),
.activities-table td:nth-child(3),
.activities-table th:nth-child(4),
.activities-table td:nth-child(4),
.activities-table th:nth-child(5),
.activities-table td:nth-child(5),
.activities-table th:nth-child(6),
.activities-table td:nth-child(6),
.activities-table th:nth-child(7),
.activities-table td:nth-child(7),
.activities-table th:nth-child(8),
.activities-table td:nth-child(8) {
  text-align: center;
}

/* 狀態標籤 - 確保在同一水平線 */
.status-badge {
  padding: 6px 12px;  /* 上下padding改成固定的 */
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block; 
  vertical-align: middle; 
}

/* 報名截止樣式 */
.deadline-normal {
  color: #666;
}

.deadline-warning {
  color: #ff9800;
  font-weight: 600;
}

.deadline-urgent {
  color: #f44336;
  font-weight: 700;
}

.deadline-passed {
  color: #999;
  text-decoration: line-through;
}

/* 報名率樣式 */
.rate-low {
  color: #4caf50;
  font-weight: 600;
}

.rate-medium {
  color: #ff9800;
  font-weight: 600;
}

.rate-high {
  color: #f44336;
  font-weight: 700;
}

/* 狀態標籤 */
.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-draft {
  background: #e0e0e0;
  color: #666;
}

.status-published {
  background: #e3f2fd;
  color: #1976d2;
}

.status-completed {
  background: #f1f8e9;
  color: #689f38;
}

.status-cancelled {
  background: #ffebee;
  color: #d32f2f;
}

/* 按鈕 */
.btn-view {
  padding: 8px 16px;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-view:hover {
  background: #1976d2;
  transform: translateY(-2px);
}

/* 空狀態 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 18px;
}

/* 響應式設計 */
@media (max-width: 1024px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .filters {
    flex-direction: column;
  }
  
  .filter-group input {
    width: 100%;
  }
}
</style>