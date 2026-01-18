<template>
    <div class="activity-management-container">
        <h1>活動管理</h1>
        <p>在此頁面中，您可以查看和管理所有活動。</p>

        <!-- KPI卡片，顯示活動相關數據 -->
         <div class="kpi-cards">
            <div class="kpi-label">本學期活動數</div>
            <div class="kpi-value">{{ stats.semesterActivities }}</div>
             <div class="kpi-change" :class="stats.activityChange >= 0 ? 'positive' : 'negative'">
                    {{ stats.activityChange >= 0 ? '↑' : '↓' }} 
                    {{ Math.abs(stats.activityChange) }} 場
                </div>
         </div>

         <!-- 活動執行率 -->
          <div class="kpi-cards">
            <div class="kpi-label">活動執行率</div>
            <div class="kpi-value">{{ stats.executionRate }}%</div>
            <div class="kpi-change" :class="getExecutionRateLabel(stats.executionRate) === '高' ? 'positive' : 'negative'">
                {{ getExecutionRateLabel(stats.executionRate) }}
            </div>
          </div>

        <!-- 活動參與率 -->
            <div class="kpi-cards">
                <div class="kpi-label">活動參與率</div>
               <div class="kpi-value">{{ stats.participationRate }}%</div>
                <div class="kpi-status" :class="getParticipationClass(stats.participationRate)">
                    {{ getParticipationLabel(stats.participationRate) }}
                </div>
            </div>
        <!-- 活動滿意度 -->
            <div class="kpi-cards">
                <div class="kpi-label">活動滿意度</div>
                <div class="kpi-value">{{ stats.satisfactionRate }}/5.0</div>
                <div class="kpi-status" :class="getSatisfactionClass(stats.satisfactionRate)">
                    {{ getSatisfactionLabel(stats.satisfactionRate) }}
                </div>
            </div>

        <!-- 活動狀態統計 -->
         <div class="status-overview">
            <h3>活動狀態總覽</h3>
            <!-- 狀態卡片 -草稿-->
            <div class="status-card" @click="goToList('DRAFT')">
                <div class="status-icon draft">📄</div>
                <div class="status-label">草稿中</div>
                <div class="status-count">{{ statusStats.DRAFT }}</div>
                <div class="status-action">查看詳情</div>
         </div>
            <!-- 狀態卡片 -已發布-->
            <div class="status-card" @click="goToList('PUBLISHED')">
                <div class="status-icon published">🚀</div>
                <div class="status-label">已發布</div>
                <div class="status-count">{{ statusStats.PUBLISHED }}</div>
                <div class="status-action">查看詳情</div>
            </div>
            <!-- 狀態卡片 -已完成-->
            <div class="status-card" @click="goToList('COMPLETED')">
                <div class="status-icon completed">✅</div>
                <div class="status-label">已完成</div>
                <div class="status-count">{{ statusStats.COMPLETED }}</div>
                <div class="status-action">查看詳情</div>
            </div>
            <!-- 狀態卡片 -已取消-->
            <div class="status-card" @click="goToList('CANCELLED')">
                <div class="status-icon cancelled">❌</div>
                <div class="status-label">已取消</div>
                <div class="status-count">{{ statusStats.CANCELLED }}</div>
                <div class="status-action">查看詳情</div>
            </div>
         </div>
         <div class="pending-tasks">
            <h3>待辦事項</h3>
            <!-- 緊急事件 -->
             <div v-if="pendingTasks.urgent.length > 0" class="task-group urgent">
                <div class="task-header"><span class="task-badge urgent">🔴 緊急{{ pendingTasks.urgent.length }}</span></div>
                  <ul class="task-list">
                    <li v-for="(task, index) in pendingTasks.urgent" :key="index" class="task-item">
                        {{ task }}
                    </li>
                    </ul> 
                </div>
            <!-- 注意事項 -->
            <div v-if="pendingTasks.attention.length > 0" class="task-group attention">
                <div class="task-header"><span class="task-badge attention">🟠 注意{{ pendingTasks.attention.length }}</span></div>
                  <ul class="task-list">
                    <li v-for="(task, index) in pendingTasks.attention" :key="index" class="task-item">
                        {{ task }}
                    </li>
                    </ul> 
                </div>
            <!-- 無待辦事項 -->
             <div v-if="pendingTasks.urgent.length === 0 && pendingTasks.attention.length === 0" class="no-tasks">
                <p>太棒了!目前沒有待辦事項喔~</p>
         </div>
         </div>

         <div class="upcoming-activities">
            <div v-if="upcomingActivities.length > 0" class="activity-quick-actions">
                <h3>近期活動</h3>
                <!-- 最近的活動 -->
                 <div v-if="upcomingActivities[0]" class="quick-activity large" @click="goToDetail(upcomingActivities[0].id)">
                    <div class="activity-time-badge">{{ getTimeLabel(upcomingActivities[0]) }}</div>
                    <h4>{{ upcomingActivities[0].title }}</h4>
                    <p class="activity-time">{{ formatDateTime(upcomingActivities[0].startTime) }} - {{ formatDateTime(upcomingActivities[0].endTime) }}</p>
                    <p class="activity-location">{{ upcomingActivities[0].location }}</p>
                    <button class="btn-qr large" @click.stop="window.open(generateQRCodeLink(upcomingActivities[0].id), '_blank')">產生點名QR Code</button>
                 </div>
                 <!-- 第二近的活動 -->
                  <div v-if="upcomingActivities[1]" class="quick-activity medium" @click="goToDetail(upcomingActivities[1].id)">
                    <div class="activity-time-badge">{{ getTimeLabel(upcomingActivities[1]) }}</div>
                    <h5>{{ upcomingActivities[1].title }}</h5>
                    <p class="activity-time">{{ formatDateTime(upcomingActivities[1].startTime) }} - {{ formatDateTime(upcomingActivities[1].endTime) }}</p>
                    <p class="activity-location">{{ upcomingActivities[1].location }}</p>
                    <button class="btn-qr medium" @click.stop="window.open(generateQRCodeLink(upcomingActivities[1].id), '_blank')">產生點名QR Code</button>
         </div>
            </div>
            <div v-else class="no-activities">
                <p>目前沒有即將舉辦的活動。</p>
            </div>
    </div>
    </div>
</template>

<script setup>
    import { ref, onMounted } from 'vue';
    import { activityApi } from '@/api/activity';
    import { useRouter } from 'vue-router';


    const router = useRouter();    //活動列表
    
    //載入狀態
    const loading = ref(false);
    
    //統計資料
    const stats = ref({
        semesterActivities: 0,//本學期活動數
        activityChange: 0,//比上學期增減
        plannedActivities: 0,//預計下學期活動數
        completeActivities: 0,//已完成活動數
        executionRate: 0,//活動執行率
        participationRate: 0,//活動參與率
        satisfactionRate: 0//活動滿意度

    });

      //活動狀態統計
    const statusStats = ref({
        DRAFT: 0,
        PUBLISHED: 0,
        COMPLETED: 0,
        CANCELLED: 0,
        
    });

    //待辦事項
    const pendingTasks = ref({
        urgent: [], //緊急事項
        attention: [] //注意事項
    });

    //近期活動(最多兩個，然後可以快速點名)
    const upcomingActivities = ref([]);

    

    const calculatePendingTasks = (activities) => {
        const urgent = [];
        const attention = [];
        const now = new Date();
    
        activities.forEach(activity => {
            if (activity.status !== 'PUBLISHED') return; //只關注已發布的活動

            const startTime = new Date(activity.startTime);
            const hoursUntilStart = (startTime - now) / (1000 * 60 * 60 * 24); //以天為單位的時間差
    
            //24小時內開始的活動
            if (hoursUntilStart > 0 && hoursUntilStart <= 24 && activity.status === 'PUBLISHED') {
                pendingTasks.value.urgent.push(`活動 "${activity.title}" 將於 ${startTime.toLocaleString()} 開始`);
            }
    
            //報名即將額滿的活動
            if(activity.maxPartcipants && activity.registrationCount){
                const rate = activity.registrationCount / activity.maxParticipants;
                if (rate > 0.9) {
                    urgent.push(`活動 "${activity.title}" 報名即將額滿 (${Math.round(rate * 100)}% 已報名)`);
                }
            }
            // 3天內即將開始（但超過24小時）
            if(hoursUntilStart > 24 && hoursUntilStart <= 72){
                attention.push(`活動 "${activity.title}" 將於 ${startTime.toLocaleString()} 開始`);
            }

            //草稿過多提醒
            const draftActivitiesCount = activities.filter(a => a.status === 'DRAFT').length;
            if(draftActivitiesCount > 5){
                attention.push(`目前有 ${draftActivitiesCount} 個草稿活動，請盡快完成發布。`);
                 
            }
            pendingTasks.value = { urgent, attention };
        });
    };

    // 時間標籤（今天、明天、後天）
    const getTimeLabel = (activity) => {
        const today = new Date();
        const startTime = new Date(activity.startTime);
        const diffDays = Math.floor((startTime - today) / (1000 * 60 * 60 * 24));

        if (diffDays === 0) return '今天';
        if (diffDays === 1) return '明天';
        if (diffDays === 2) return '後天';
        return `${diffDays} 天後`;
    };

    //格式化日期
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
   })};

   //產生qrcode連結
   const generateQRCodeLink = (activityId) => {
       return `https://api.qrserver.com/v1/create-qr-code/?data=https://club-system-frontend.example.com/activities/${activityId}/checkin&size=150x150`};


    // 👇 前往活動詳情
    const goToDetail = (activityId) => {
    router.push(`/admin/activities/${activityId}`);
    };


    // 跳轉到活動列表並篩選
    const goToList = (status) => {
    router.push({
        path: '/admin/activities/list',
        query: { status: status }
    });
};

//取得執行率標籤
    const getExecutionRateLabel = (rate) => {
        if (rate >= 80) return '高';
        if (rate >= 50) return '中';
        return '低';
    };



    //參與率樣式
    const getParticipationClass = (rate) => {
        if (rate >= 80) return 'status-good';
        if (rate >= 50) return 'status-warning';
        return 'status-bad';
    };

    //取得參與率標籤
    const getParticipationLabel = (rate) => {
        if (rate >= 80) return '良好';
        if (rate >= 50) return '普通';
        return '需改善';
    };
    //滿意度樣式
    const getSatisfactionClass = (satisfaction) => {
        if (satisfaction >= 4) return 'status-good';
        if (satisfaction >= 3) return 'status-warning';
        return 'status-bad';
    };

    //取得滿意度標籤
    const getSatisfactionLabel = (satisfaction) => {
        if (satisfaction >= 4) return '高';
        if (satisfaction >= 3) return '中';
        return '低';
    };

    //載入活動統計資料
    const loadstats = () => {
        //模擬從API獲取數據
        stats.value.semesterActivities = 12;
        stats.value.activityChanges = 2;
        stats.value.plannedActivities = 15;
        stats.value.completeActivities = 10;
        stats.value.executionRate = 83;
        stats.value.participationRate = 75;
        stats.value.satisfactionRate = 4;
    };

    //載入活動狀態統計資料
    const loadStatusStats = async () => {
        try {
            loading.value = true;
            const response = await activityApi.getAllActivities();
            const activities = response.data;
            console.log('取得活動資料:', activities);

            if(!Array.isArray(activities)){
                throw new Error('活動資料格式錯誤，預期為陣列格式');
                return;
            }
            //計算每個活動狀態的數量
            statusStats.value = {
                DRAFT: activities.filter(a => a.status === 'DRAFT').length,
                PUBLISHED: activities.filter(a => a.status === 'PUBLISHED').length,
                COMPLETED: activities.filter(a => a.status === 'COMPLETED').length,
                CANCELLED: activities.filter(a => a.status === 'CANCELLED').length
            };
            console.log('活動狀態統計:', statusStats.value);

            //計算待處理事項的數量
            calculatePendingTasks(activities);   

            //取得近期兩個月的活動
            const now = new Date();
            upcomingActivities.value = activities
                .filter(a => a.status === 'PUBLISHED' && a.startTime && new Date(a.startTime) >= now)
                .sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
                .slice(0, 2);
            console.log('近期活動:', upcomingActivities.value);
        } catch (error) {
            console.error('載入活動狀態統計失敗:', error);
            alert('載入活動狀態統計失敗，請稍後再試。');
        }
        finally {
            loading.value = false;
        }
    };


    onMounted(() => {
        loadstats();
        loadStatusStats();
    });
</script>

<style scoped>
.activity-management-container {
    padding: 20px;
    max-width: 1400px;
    margin: 0 auto;
}   

.activity-management-container h1 {
    font-size: 24px;
    margin-bottom: 10px;
}
.activity-management-container p {
    font-size: 16px;
    color: #555;
}
.bottom-section {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 24px;
    margin-top: 40px;
}
/* KPI卡片樣式 */
.kpi-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-top: 24px;
}
.kpi-cards {
    background: white;
    padding: 24px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    text-align: center;
    transition: transform 0.2s;
}
.kpi-cards:hover {
    transform: translateY(-4px);
    box-shadow: inset 0 4px 12px rgba(0, 0, 0, 0.15);
}
.kpi-label {
    font-size: 14px;
    color: #666;
    margin-bottom: 12px;
    font-weight: 500;
}

.kpi-value {
    font-size: 36px;
    font-weight: 700;
    color: #333;
    margin-bottom: 8px;
}

/* 變化量（增減） */
.kpi-change {
    font-size: 14px;
    font-weight: 600;
}

.kpi-change.positive {
    color: #4caf50;
}

.kpi-change.negative {
    color: #f44336;
}

/* 狀態標籤 */
.kpi-status {
    font-size: 13px;
    font-weight: 600;
    padding: 4px 12px;
    border-radius: 12px;
    display: inline-block;
}

.status-good {
    background: #e8f5e9;
    color: #2e7d32;
}

.status-warning {
    background: #fff3e0;
    color: #f57c00;
}

.status-bad {
    background: #ffebee;
    color: #c62828;
}

/* 活動狀態總覽樣式 */
status-overview {
    margin-top: 40px;
}
.status-overview h3 {
    font-size: 20px;
    margin-bottom: 20px;
    color: #333;
}
.status-card {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 14px;
    margin-top: 20px;
}
.status-card {
    background: white;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;
}
.status-card:hover {
    transform: translateY(-4px);
    box-shadow:  0 4px 12px rgba(0, 0, 0, 0.2);
}
.status-icon {
    font-size: 32px;
    margin-bottom: 8px;
}
.status-label {
    font-size: 14px;
    font-weight: 600;
    color: #666;
    margin-bottom: 8px;
}
.status-count {
    font-size: 28px;
    font-weight: 700;
    color: #333;
    margin-bottom: 8px;
}
.status-action {
    font-size: 13px;
    color: #1e88e5;
    font-weight: 500;
}
/* 待處理事項 */
.pending-tasks {
    background: white;
    padding: 24px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    margin-top: 40px;
}

.pending-tasks h3 {
    margin: 0 0 20px 0;
    font-size: 18px;
    font-weight: 600;
}

.task-group {
    margin-bottom: 20px;
}

.task-header {
    margin-bottom: 12px;
}

.task-badge {
    padding: 6px 12px;
    border-radius: 16px;
    font-size: 13px;
    font-weight: 600;
}

.task-badge.urgent {
    background: #ffebee;
    color: #c62828;
}

.task-badge.warning {
    background: #fff3e0;
    color: #f57c00;
}

.task-list {
    list-style: none;
    padding: 0;
    margin: 0;
}

.task-list li {
    padding: 8px 12px;
    margin-bottom: 8px;
    background: #f5f5f5;
    border-radius: 4px;
    font-size: 14px;
}

.no-tasks {
    text-align: center;
    padding: 40px 20px;
    color: #4caf50;
}

/* 近期活動 */
.upcoming-activities {
    background: white;
    padding: 24px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    margin-top: 40px;
}

.upcoming-activities h3 {
    margin: 0 0 20px 0;
    font-size: 18px;
    font-weight: 600;
}

.activity-quick-actions {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.quick-activity {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 20px;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s;
    position: relative;
    margin-top: -10px;
}

.quick-activity:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

/* 大格活動 */
.quick-activity.large {
    min-height: 180px;
}

.quick-activity.large h4 {
    margin: 12px 0 8px 0;
    font-size: 24px;
    font-weight: 600;
}

/* 中格活動 */
.quick-activity.medium {
    min-height: 140px;
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.quick-activity.medium h5 {
    margin: 12px 0 8px 0;
    font-size: 18px;
}

.activity-time-badge {
    display: inline-block;
    background: rgba(255, 255, 255, 0.3);
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 600;
}

.activity-time {
    font-size: 16px;
    margin: 4px 0;
    color: white !important;
}


.activity-location {
    font-size: 14px;
    margin: 4px 0 12px 0;
    color: white !important;
}

.btn-qr {
    background: white;
    color: #7480b4;
    border: none;
    padding: 10px 20px;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s;
    width: 100%;
}

.btn-qr:hover {
    background: #f5f5f5;
    transform: scale(1.02);
}

.btn-qr.large {
    font-size: 16px;
}

.btn-qr.medium {
    font-size: 14px;
    padding: 8px 16px;
}

.no-activities {
    text-align: center;
    padding: 40px 20px;
    color: #999;
}

/* 響應式 */
@media (max-width: 1024px) {
    .bottom-section {
        grid-template-columns: 1fr;
    }
     .kpi-cards {
        grid-template-columns: repeat(2, 1fr);
        gap: 16px;
    }
    
    .kpi-value {
        font-size: 28px;
    }
    .status-cards {
        grid-template-columns: repeat(2, 1fr);
    }
}



</style>