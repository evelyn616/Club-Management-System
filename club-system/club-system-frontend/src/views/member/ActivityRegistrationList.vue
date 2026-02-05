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
                    <td><button v-if="!isRegistered(activity.id)&& !isActivityFull(activity)" class="register-btn" @click="openRegisterModal(activity)">報名</button>
                    <span v-else-if="isActivityFull(activity)" class="full-badge">已額滿</span>
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

        // 批量載入每個活動的報名人數
        for (let activity of activities.value) {
            try {
                const countResponse = await registrationApi.getActivityRegistrationCount(activity.id)
                activity.registrationCount = countResponse.data
            } catch (error) {
                console.error(`載入活動 ${activity.id} 報名數失敗:`, error)
                activity.registrationCount = 0
            }}
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
//檢查是否已額滿
const isActivityFull = (activity) => {
    if(!activity.maxParticipants) return false;

    return (activity.registrationCount || 0) >= activity.maxParticipants;
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
/* ===== 整體容器 ===== */
.activity-registration-list-container {
    min-height: 100vh;
    min-width: 1200px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 40px 60px;
}

/* ===== 標題區 ===== */
h1 {
    color: white;
    font-size: 42px;
    font-weight: 800;
    margin: 0 0 40px 0;
    text-align: center;
    text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.2);
    letter-spacing: 2px;
}

/* ===== 搜尋與篩選區 ===== */
.search-filter {
    background: white;
    padding: 30px;
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    margin-bottom: 30px;
}

/* 基本搜尋 */
.basic-search {
    display: flex;
    gap: 12px;
    margin-bottom: 24px;
    max-width: 600px;
}

.search-input {
    flex: 1;
    padding: 14px 20px;
    border: 2px solid #e0e0e0;
    border-radius: 12px;
    font-size: 16px;
    transition: all 0.3s;
    outline: none;
}

.search-input:focus {
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-btn {
    padding: 14px 28px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    border-radius: 12px;
    font-size: 20px;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.search-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* Tab 篩選按鈕 */
.tab-filters {
    display: flex;
    gap: 0;
    border: 2px solid #667eea;
    border-radius: 12px;
    overflow: hidden;
    margin-bottom: 24px;
    width: fit-content;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.tab-filters .quick-btn {
    padding: 14px 40px;
    background: white;
    color: #667eea;
    border: none;
    cursor: pointer;
    font-weight: 600;
    font-size: 15px;
    transition: all 0.3s ease;
    border-right: 2px solid #667eea;
    position: relative;
}

.tab-filters .quick-btn:last-child {
    border-right: none;
}

.tab-filters .quick-btn.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
}

.tab-filters .quick-btn:hover:not(.active) {
    background: #f0f4ff;
}

/* 進階篩選 */
.filters {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    align-items: flex-end;
}

.filter-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.filter-group label {
    font-weight: 600;
    color: #333;
    font-size: 14px;
}

.filter-group select {
    padding: 12px 16px;
    border: 2px solid #e0e0e0;
    border-radius: 10px;
    font-size: 15px;
    min-width: 180px;
    cursor: pointer;
    transition: all 0.3s;
    background: white;
    outline: none;
}

.filter-group select:focus {
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.clear-btn {
    padding: 12px 24px;
    background: #ff6b6b;
    color: white;
    border: none;
    border-radius: 10px;
    cursor: pointer;
    font-weight: 600;
    transition: all 0.3s;
    box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
}

.clear-btn:hover {
    background: #ee5a52;
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(255, 107, 107, 0.4);
}

/* ===== 狀態訊息 ===== */
.activity-registration-list-container > p {
    color: white;
    font-size: 18px;
    font-weight: 600;
    text-align: center;
    margin: 20px 0;
    text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.2);
}

/* ===== 表格容器 ===== */
.table-container {
    background: white;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

/* 表格樣式 */
.activities-table {
    width: 100%;
    border-collapse: collapse;
    background: white;
}

/* 表頭 */
.activities-table th {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 18px 16px;
    text-align: left;
    font-weight: 700;
    font-size: 15px;
    letter-spacing: 0.5px;
    border: none;
}

/* 表格內容 */
.activities-table td {
    padding: 16px;
    border-bottom: 1px solid #f0f0f0;
    color: #333;
    font-size: 14px;
}

/* 滑鼠移到列上時變色 */
.activities-table tbody tr {
    transition: all 0.3s;
}

.activities-table tbody tr:hover {
    background: #f8f9ff;
    transform: scale(1.01);
}

/* 報名按鈕樣式 */
.register-btn {
    padding: 10px 24px;
    background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
    color: white;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-weight: 600;
    font-size: 14px;
    transition: all 0.3s;
    box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.register-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(76, 175, 80, 0.4);
}

/* 已報名標記 */
.registered-badge {
    color: #4CAF50;
    font-weight: 700;
    padding: 6px 16px;
    background: #e8f5e9;
    border-radius: 12px;
    font-size: 13px;
    display: inline-block;
}

/* 已額滿標記 */
.full-badge {
    color: #f44336;
    font-weight: 700;
    padding: 6px 16px;
    background: #ffebee;
    border-radius: 12px;
    font-size: 13px;
    display: inline-block;
}

/* 空狀態 */
.empty-state {
    text-align: center;
    color: #888;
    padding: 60px 20px;
    font-size: 20px;
    animation: flashWhite 1.5s ease-in;
}

/* ===== 動畫效果 ===== */
/* 刷新動畫 */
.table-refresh {
    animation: flashWhite 0.8s ease-in !important;
}

@keyframes flashWhite {
    0% {
        opacity: 0;
        transform: translateY(10px);
    }
    50% {
        opacity: 0.6;
    }
    100% {
        opacity: 1;
        transform: translateY(0);
    }
}

/* 彈出動畫 */
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

/* ===== 模態框樣式 ===== */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.6);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
    backdrop-filter: blur(4px);
    transition: all 0.3s ease-in;
}

.modal-content {
    background: white;
    border-radius: 20px;
    width: 90%;
    max-width: 560px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    animation: popUp 0.3s ease-in-out;
    overflow: hidden;
    border: 3px solid #667eea;
}

/* 彈窗標題區 */
.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 28px 32px;
    border-bottom: none;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.modal-header h2 {
    margin: 0;
    font-size: 24px;
    color: white;
    font-weight: 800;
    letter-spacing: 1px;
}

/* 關閉按鈕 */
.close-btn {
    background: rgba(255, 255, 255, 0.2);
    border: none;
    font-size: 28px;
    color: white;
    cursor: pointer;
    padding: 0;
    width: 40px;
    height: 40px;
    border-radius: 50%;
    transition: all 0.3s;
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
}

.close-btn:hover {
    background: rgba(255, 255, 255, 0.35);
    transform: rotate(90deg);
}

/* 彈窗內容區 */
.modal-body {
    padding: 32px;
    background: #fafafa;
}

/* 活動資訊卡片 */
.activity-info {
    background: white;
    padding: 24px;
    border-radius: 16px;
    margin-bottom: 20px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    border-left: 5px solid #667eea;
}

.activity-info h3 {
    margin: 0 0 20px 0;
    color: #667eea;
    font-size: 22px;
    font-weight: 800;
}

.activity-info p {
    margin: 12px 0;
    color: #555;
    font-size: 16px;
    line-height: 1.8;
}

.activity-info p strong {
    color: #333;
    font-weight: 700;
    min-width: 90px;
    display: inline-block;
}

/* 使用者資訊卡片 */
.user-info {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 18px 24px;
    border-radius: 16px;
    margin-bottom: 24px;
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.user-info p {
    margin: 0;
    color: #ffffff;
    font-size: 16px;
    font-weight: 600;
}

.user-info p strong {
    color: #ffffff;
    font-weight: 800;
}

/* 確認文字 */
.confirm-text {
    text-align: center;
    font-size: 18px;
    color: #333;
    margin: 28px 0 0 0;
    font-weight: 700;
}

/* 彈窗底部按鈕區 */
.modal-footer {
    padding: 24px 32px;
    border-top: 2px solid #f0f0f0;
    display: flex;
    justify-content: flex-end;
    gap: 16px;
    background: white;
}

/* 按鈕基礎樣式 */
.cancel-btn,
.confirm-btn {
    padding: 14px 32px;
    border: none;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 取消按鈕 */
.cancel-btn {
    background: #f5f5f5;
    color: #666;
}

.cancel-btn:hover {
    background: #e0e0e0;
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

/* 確定按鈕 */
.confirm-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
}

.confirm-btn:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.confirm-btn:active:not(:disabled) {
    transform: translateY(0);
}

.confirm-btn:disabled {
    background: #ccc;
    cursor: not-allowed;
    box-shadow: none;
}

/* ===== 響應式設計 ===== */
@media (max-width: 768px) {
    .activity-registration-list-container {
        padding: 20px;
    }
    
    h1 {
        font-size: 32px;
    }
    
    .search-filter {
        padding: 20px;
    }
    
    .filters {
        flex-direction: column;
    }
    
    .filter-group select {
        width: 100%;
    }
    
    .activities-table {
        font-size: 12px;
    }
    
    .activities-table th,
    .activities-table td {
        padding: 10px 8px;
    }
}
</style>