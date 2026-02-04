<template>
    <div class="activity-registration-list-container">
        <h1>活動列表</h1>


        <!--搜尋和篩選區-->
        <div class="search-filter">
            <!-- 基本搜尋 -->
             <div class="basic-search">
                <input v-model="searchKeyword" type="text" placeholder="搜尋活動..." @input="handleSearch" class="search-input" />
                <button @click="handleSearch" class="search-btn">🔍</button>

             </div>

            <!--時間篩選-->
            <div class="tab-filters">
                <button :class="['quick-btn', {active: activeTab === 'all'}]" @click="activeTab = 'all'">All</button>
                <button :class="['quick-btn', {active: activeTab === 'upcoming'}]" @click="activeTab = 'upcoming'">Upcoming</button>
                <button :class="['quick-btn', {active: activeTab === 'news'}]" @click="activeTab = 'news'">New Onsales</button>
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
        <!-- 顯示載入狀態 -->
        <p v-if="loading">載入中...</p>
        <!-- 顯示活動列表 -->
         
         <p v-else>目前有{{ filteredActivities.length }}個活動</p>

        
        <div v-if="!loading" class="table-container">
         <table class="activities-table table-refresh" :key="tableKey">
            <thead>
                <tr>
                    <th>活動ID</th>
                    <th>活動標題</th>
                    <th>開始時間</th>
                    <th>結束時間</th>
                    <th>地點</th>
                    <th>活動類型</th>
                    <th>費用</th>
                    <th>報名區</th>
                    </tr>
            </thead>
            
            <tbody>
                <tr v-for="activity in filteredActivities" :key="activity.id">
                    <td>{{ activity.id }}</td>
                    <td>{{ activity.title }}</td>
                    <td>{{ formatDateTime(activity.startTime) }}</td>
                    <td>{{ formatDateTime(activity.endTime) }}</td>
                    <td>{{ activity.location }}</td>
                    <td>{{ getActivityTypeLabel(activity.activityType) }}</td>
                    <td>{{ activity.feeAmount }}</td>
                    <td><button v-if="!isRegistered(activity.id)" class="register-btn" @click="openRegisterModal(activity)">報名</button>
                    <span v-else class="registered-badge">✓ 已報名</span>
                    </td>
                    
                </tr>
            </tbody>
            
         </table>

          <!-- 空狀態 -->
            <div v-if="filteredActivities.length === 0" class="empty-state" >
                <p>😔 找不到符合條件的活動</p>
            </div>
         </div>  
         </div>

         <!-- 報名按鈕模態框 -->
          <div v-if="showRegisterModal" class="modal-overlay">
            <div class="modal-content" @click.stop>
                <div class="modal-header">
                <h2>確認報名活動</h2>
                <button class="close-btn" @click="closeRegisterModal">✕</button>
                </div>
                <div class="modal-body">
                    <div class="activity-info">
                        <h3>{{ selectedActivity?.title }}</h3>
                        <p><strong>時間：</strong>{{ formatDateTime(selectedActivity?.startTime) }}</p>
                        <p><strong>地點：</strong>{{ selectedActivity?.location }}</p>
                        <p><strong>費用：</strong>{{ selectedActivity?.feeAmount }} 元</p>
                    </div>
                    
                <div class="user-info">
                    <p>報名身份：<strong>{{userStore.userName}} {{ userStore.userId }}</strong></p>
                </div>
                    
                <p class="confirm-text">確定要報名這個活動嗎？</p>
                </div>
                <div class="modal-footer">
                    <button class="confirm-btn" @click="confirmRegister" :disabled="isRegistering">{{ isRegistering ? '報名中...' : '確定報名' }}</button>
                    <button class="cancel-btn" @click="closeRegisterModal">取消</button>
            </div>
          </div>
    </div>
    </div>
    </template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { activityApi } from '@/api/activity'
import { useUserStore } from '@/stores/user'
import { registrationApi } from '@/api/registration';

const userStore = useUserStore();

//建立資料參數
const activities = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const selectedActivityType = ref('')
const selectedDanceStyle = ref('')
const activeTab = ref('all')
const tableKey = ref(0);//用於強制刷新表格
const isFirstLoad = ref(true);
const showRegisterModal = ref(false);
const selectedActivity = ref(null);
const isRegistering = ref(false);
const registrations = ref([]);

//載入活動資料
const loadActivities = async () => {
    loading.value = true//開始載入
    try {
        const response = await activityApi.getRegistrableActivities()
        activities.value = response.data
        console.log('可報名活動列表:', activities.value);
    } catch (error) {
        console.error('載入活動資料失敗:', error)
        alert('載入活動資料失敗，請稍後再試。')
    }
    finally {
        loading.value = false//結束載入
    }
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

watch([activeTab, searchKeyword, selectedActivityType, selectedDanceStyle], () => {
    if (isFirstLoad.value) {
        isFirstLoad.value = false;
        return; // 跳過第一次加載時的刷新
    }
    tableKey.value ++; // 每次篩選條件改變時，更新 key 以強制刷新表格
});

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

//打開報名模態框
const openRegisterModal = (activity) => {
    // 沒有 token 的話，跳到登入頁
    if (!userStore.token) {
        return null;
    }
    selectedActivity.value = activity;
    showRegisterModal.value = true;
    console.log('打開報名視窗:', activity);
}
//關閉報名模態框
const closeRegisterModal = () => {
    showRegisterModal.value = false;
    selectedActivity.value = null;
}
//確認報名
const confirmRegister = async () => {
    if (!selectedActivity.value) return
    isRegistering.value = true;
    try {

        const registrationData = {
            userId: userStore.userId,
            activityId: selectedActivity.value.id,
            
        }

       //呼叫API報名活動
        const response = await registrationApi.createRegistration(registrationData);
        console.log('報名成功:', response.data)
        alert(`✅ 報名成功！\n活動：${selectedActivity.value.title}\n報名者：${userStore.userName}`);
        closeRegisterModal()
        loadRegistrations(); // 重新載入報名紀錄
}
catch (error) {
        console.error('報名活動失敗:', error);
        if (error.reponse?.status === 409) {
            alert('⚠️ 您已經報名過此活動，請勿重複報名。');
        } 
        else if (error.response?.status === 400) {
        const errorMsg = error.response.data?.message || error.response.data || '報名資料有誤'
            alert(`❌ ${errorMsg}`)
        } else if (error.response?.status === 404) {
            alert('❌ 找不到此活動，可能已被取消。')
        } else {
            alert('❌ 報名失敗，請稍後再試。')
        }
    }
    finally {
        isRegistering.value = false;
    }
}

//載入報名紀錄
const loadRegistrations = async () => {
    try {
        const response = await registrationApi.getMyRegistrations(userStore.userId);
        registrations.value = response.data.map(reg => reg.activityId);
        console.log('報名紀錄:', registrations.value);
    } catch (error) {
        console.error('載入報名紀錄失敗:', error);
    }
}
//檢查是否已報名
const isRegistered = (activityId) => {
    return registrations.value.includes(activityId);
}
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
//處理搜尋函數
const handleSearch = () => {
    //搜尋邏輯已整合在 computed 屬性中
}
const handleFilterChange = () => {
    //篩選邏輯已整合在 computed 屬性中
}
//清除篩選
const clearFilters = () => {
    searchKeyword.value = ''
    selectedActivityType.value = ''
    selectedDanceStyle.value = ''
}

onMounted(() => {
    loadActivities();
    loadRegistrations();
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

/* 報名按鈕樣式 */
.register-btn{
    padding: 8px 16px;
    background-color: #4CAF50;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    
}
.register-btn:hover {
    background-color: #8ea18f;
    transition: all 0.2s ease-in-out;
    color: rgb(0, 0, 0);
}

/*刷新*/
.table-refresh {
    animation: flashWhite 0.8s ease-in !important;
}

.empty-state {
    text-align: center;
    color: #888;
    margin-top: 40px;
    font-size: 18px;
    animation: flashWhite 1.5s ease-in;
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
    backdrop-filter: blur(2px);
    transition: all 0.1s ease-in;
}

.modal-content {
    background: white;
    padding: 20px;
    border-radius: 16px;
    width: 90%;
    max-width: 520px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    animation: popUp 0.3s ease-in-out;
    overflow: hidden;
    border: 2px solid #000000;
}

/* 👇 閃爍動畫 - 白色閃爍效果 */
@keyframes flashWhite {
    0% {
        opacity: 0;
        background-color: #ffffff;
        
    }
    50% {
        opacity: 0.6;
    }
    
    100% {
        opacity: 1;
        background-color: white;
        
        
    }
}
/* 👇 彈出動畫 - 從縮小到正常大小 + 從下往上 */
@keyframes popUp {
    0% {
        transform: scale(0.7) translateY(100px);
        opacity: 0;
    }
    60% {
        transform: scale(1.05) translateY(-10px);
    }
    100% {
        transform: scale(1) translateY(0);
        opacity: 1;
    }
}


/* 彈窗標題區 */
.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px 28px;
    border-bottom: 2px solid #e0e0e0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.modal-header h2 {
    margin: 0;
    font-size: 22px;
    color: white;
    font-weight: 700;
}

/* 關閉按鈕 */
.close-btn {
    background: rgba(255, 255, 255, 0.2);
    border: none;
    font-size: 28px;
    color: white;
    cursor: pointer;
    padding: 0;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    transition: all 0.3s;
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
}

.close-btn:hover {
    background: rgba(255, 255, 255, 0.3);
    transform: rotate(90deg);
}

/* 彈窗內容區 */
.modal-body {
    padding: 28px;
    background: #fafafa;
}

/* 活動資訊卡片 */
.activity-info {
    background: white;
    padding: 20px;
    border-radius: 12px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    border-left: 4px solid #2196F3;
}

.activity-info h3 {
    margin: 0 0 16px 0;
    color: #2196F3;
    font-size: 20px;
    font-weight: 700;
}

.activity-info p {
    margin: 10px 0;
    color: #555;
    font-size: 15px;
    line-height: 1.6;
}

.activity-info p strong {
    color: #333;
    font-weight: 600;
    min-width: 80px;
    display: inline-block;
}

/* 使用者資訊卡片 */
.user-info {
    background: #2196F3;
    padding: 16px 20px;
    border-radius: 12px;
    margin-bottom: 24px;
    border-left: 4px solid #2196F3;
    box-shadow: 0 2px 8px rgba(255, 152, 0, 0.2);
}

.user-info p {
    margin: 0;
    color: #ffffff;
    font-size: 15px;
    font-weight: 500;
}

.user-info p strong {
    color: #ffffff;
    font-weight: 700;
}

/* 確認文字 */
.confirm-text {
    text-align: center;
    font-size: 17px;
    color: #333;
    margin: 24px 0 0 0;
    font-weight: 600;
}

/* 彈窗底部按鈕區 */
.modal-footer {
    padding: 20px 28px;
    border-top: 2px solid #e0e0e0;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    background: white;
}

/* 按鈕基礎樣式 */
.cancel-btn,
.confirm-btn {
    padding: 12px 28px;
    border: none;
    border-radius: 8px;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 取消按鈕 */
.cancel-btn {
    background: #f5f5f5;
    color: #666;
}

.cancel-btn:hover {
    background: #e0e0e0;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 確定按鈕 */
.confirm-btn {
    background: #764ba2;
    color: white;
}

.confirm-btn:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(135, 76, 175, 0.4);
}

.confirm-btn:active:not(:disabled) {
    transform: translateY(0);
}

.confirm-btn:disabled {
    background: #ccc;
    cursor: not-allowed;
    box-shadow: none;
}
/* Tab按鈕樣式 */
.tab-filters {
    display: flex;
    gap: 0;
    border: 1px solid #2196F3;
    border-radius: 4px;
    overflow: hidden;
    margin-bottom: 20px;
    width: fit-content;
}
.tab-filters .quick-btn {
    padding: 12px 32px;
    background: white;
    color: #2196F3;
    border: none;
    cursor: pointer;
    font-weight: 600;
    transition: all 0.1s ease;
    border-right: 1px solid #2196F3;
}
.tab-filters .quick-btn:last-child {
    border-right: none;
}
.tab-filters .quick-btn.active{
    background: #2196F3;
    color: white;
}
.tab-filters .quick-btn:hover {
    background: #e3f2fd;
}
    </style>