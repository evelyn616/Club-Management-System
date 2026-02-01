<template>
  <div class="draft-box-container">
    <!-- 頁面標題 -->
    <div class="page-header">
      <div class="header-content">
        <h1>📝 草稿箱</h1>
        <p class="subtitle">管理您的草稿活動 (共 {{ draftActivities.length }} 個草稿)</p>
      </div>
      <button @click="createActivity" class="btn-create">
         創建新活動
      </button>
    </div>

    <!-- Loading 狀態 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>載入中...</p>
    </div>

    <!-- 草稿列表 -->
    <div v-else-if="draftActivities.length > 0" class="draft-table-container">
      <table class="draft-table">
        <thead>
          <tr>
            <th class="col-title">活動標題</th>
            <th class="col-type">類型</th>
            <th class="col-time">開始時間</th>
            <th class="col-location">地點</th>
            <th class="col-created">創建時間</th>
            <th class="col-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr 
            v-for="draft in draftActivities" 
            :key="draft.id"
            class="draft-row"
          >
            <!-- 活動標題 -->
            <td class="col-title">
              <div class="title-content">
                <h3 class="draft-title">{{ draft.title }}</h3>
                <p class="draft-description" v-if="draft.description">
                  {{ draft.description }}
                </p>
              </div>
            </td>

            <!-- 活動類型 -->
            <td class="col-type">
              <span class="type-badge" :class="`type-${draft.activityType?.toLowerCase()}`">
                {{ getActivityTypeLabel(draft.activityType) }}
              </span>
            </td>

            <!-- 開始時間 -->
            <td class="col-time">
              <div class="time-info">
                <span class="time-primary">{{ formatDateTime(draft.startTime) }}</span>
                </div>
            </td>

            <!-- 地點 -->
            <td class="col-location">
              <span class="location-text">{{ draft.location }}</span>
            </td>

            <!-- 創建時間 -->
            <td class="col-created">
              <div class="created-info">
                <span class="created-time">{{ formatDateTime(draft.createdAt) }}</span>
                <span class="updated-time" v-if="draft.updatedAt !== draft.createdAt">
                  更新: {{ formatDateTime(draft.updatedAt) }}
                </span>
              </div>
            </td>

            <!-- 操作按鈕 -->
            <td class="col-actions">
              <div class="action-buttons">
                <button 
                  @click="updateActivity(draft.id)" class="btn-action btn-edit"title="編輯草稿">
                  <span class="icon">✏️</span>
                  <span>編輯</span>
                </button>
                
                <button @click="goToPublish(draft.id)"class="btn-action btn-publish"title="發布活動">
                  <span class="icon">🚀</span>
                  <span>發布</span>
                </button>
                
                <button @click="deleteDraft(draft.id, draft.title)" class="btn-action btn-delete"title="刪除草稿"
                >
                  <span class="icon">🗑️</span>
                  <span>刪除</span>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 空狀態 -->
    <div v-else class="empty-state">
      <div class="empty-icon">📭</div>
      <h2>草稿箱是空的</h2>
      <p>您還沒有任何草稿活動</p>
      <button @click="createNewActivity" class="btn-primary">
        ➕ 創建第一個活動
      </button>
    </div>

    <!-- 刪除確認對話框 -->
    <div v-if="showDeleteDialog" class="dialog-overlay" @click="cancelDelete">
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>⚠️ 確認刪除</h3>
        </div>
        
        <div class="dialog-body">
          <p>確定要刪除草稿活動嗎?</p>
          <p class="activity-name">「{{ deleteTarget.name }}」</p>
          <p class="warning">此操作無法復原!</p>
        </div>
        
        <div class="dialog-actions">
          <button @click="cancelDelete" class="btn-cancel">
            取消
          </button>
          <button @click="confirmDelete" class="btn-confirm-delete">
            確定刪除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>

import { onMounted, ref, stop } from 'vue';
import { useRouter } from 'vue-router';
import { activityApi } from '@/api/activity';

//router
const router = useRouter();

//設定變數
const loading = ref(false);
const draftActivities = ref([]);
const showDeleteDialog = ref(false);
const deleteTarget = ref({id: null, name: ''});

//載入草稿活動
const loadDraftActivities = async() =>{
    loading.value = true;
    try{
        const response = await activityApi.getDraftActivities();
        draftActivities.value = response.data;
        console.log("顯示草稿列表：",draftActivities);
        console.log(`總共${draftActivities.value.length}個活動`)
    }
    catch(error){
        console.log("載入草稿錯誤", error);
        alert("載入草稿失敗，請稍後在試");
    }
    finally{
        loading.value = false;
    }
    ;
}

//創建新活動快捷
const createActivity = () => {
    router.push({name: 'create-activity-container'})
}

//編輯草稿快捷
const updateActivity = (activityId) => {
  console.log('編輯草稿 ID:', activityId);
  router.push(`/admin/update-activity-container/${activityId}`);
};

//跳轉到發布頁面
const goToPublish = (activityId) => {
  console.log('前往發布頁面,活動 ID:', activityId);
  router.push({
    name: 'publish-activity-container',
    params: { activityId }
  });
};

//刪除草稿(顯示提示對話框)
const deleteDraft = (activityId, activityName) =>{
    deleteTarget.value = {
        id: activityId,
        name: activityName
    }
    showDeleteDialog.value = true;
} 

//取消刪除
const cancelDelete = () =>{
    showDeleteDialog.value = false;
    deleteTarget.value = {
        id: null,
        name: ''
    }
    
}
//確認刪除
const confirmDelete = async() => {
    const activityId = deleteTarget.value.id;

    try{
        console.log('刪除草稿 ID:', activityId);
        await activityApi.deleteDraftActivity(activityId);
        alert("草稿已刪除")

        // 從列表中移除
        draftActivities.value = draftActivities.value.filter(draft => draft.id !== activityId);
        showDeleteDialog.value = false
        deleteTarget.value = {
            id: null,
            name: ''

        }

    }
    catch(error){
        console.log('刪除草稿 ID:',activityId,'失敗');
        if(error.response?.status === 403){
            alert("沒有權限刪除此草稿");
        }
        else if(error.response?.status === 404){
            alert("此草稿不存在");
        }
        else if(error.response?.status === 400){
            alert("只能刪除草稿狀態的活動");
        }
        else{
            alert("刪除失敗，請稍後在試..")
        }

    }

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

//初始狀態
onMounted(() => {
    loadDraftActivities();
}) ;


</script>

<style scoped>
.draft-box-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px;
}

/* ============ 頁面標題 ============ */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.header-content h1 {
  font-size: 32px;
  margin-bottom: 8px;
  color: #333;
}

.subtitle {
  color: #666;
  font-size: 15px;
}

.btn-create {
  padding: 14px 28px;
  background: #4CAF50;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.2);
  margin-right: -80%;
}

.btn-create:hover {
  background: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

/* ============ Loading ============ */
.loading-container {
  text-align: center;
  padding: 80px 20px;
}

.loading-spinner {
  width: 60px;
  height: 60px;
  border: 5px solid #f3f3f3;
  border-top: 5px solid #4CAF50;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* ============ 表格容器 ============ */
.draft-table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-left:auto; 
  margin-right:auto;
  border: 1px solid #e8e8e8;
  width: 180%;
}

/* ============ 表格 ============ */
.draft-table {
  
  border-collapse: collapse;
  margin-left:auto; 
  margin-right:auto;
}

.draft-table thead {
  background: linear-gradient(to bottom, #f8f9fa, #f0f1f3);
  border-bottom: 2px solid #e0e0e0;
}

.draft-table th {
  padding: 18px 16px;
  text-align: left;
  font-weight: 600;
  color: #333;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.draft-table tbody tr {
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.2s;
}

.draft-table tbody tr:hover {
  background: #f8fff9;
  box-shadow: inset 0 0 0 1px #4CAF50;
}

.draft-table tbody tr:last-child {
  border-bottom: none;
}

.draft-table td {
  padding: 20px 16px;
  vertical-align: middle;
}

/* ============ 欄位樣式 ============ */

/* 標題欄 */
.col-title {
  width: 30%;
}

.title-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.draft-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
}

.draft-description {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 類型欄 */
.col-type {
  width: 10%;
}

.type-badge {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
}

.type-badge.type-regular {
  background: #e3f2fd;
  color: #1976d2;
}

.type-badge.type-special {
  background: #f3e5f5;
  color: #7b1fa2;
}

.type-badge.type-training {
  background: #fff3e0;
  color: #f57c00;
}

.type-badge.type-performance {
  background: #fce4ec;
  color: #c2185b;
}

.type-badge.type-competition {
  background: #e8f5e9;
  color: #388e3c;
}

/* 時間欄 */
.col-time {
  width: 12%;
}

.time-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.time-primary {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.time-secondary {
  font-size: 12px;
  color: #999;
}

/* 地點欄 */
.col-location {
  width: 12%;
}

.location-text {
  font-size: 14px;
  color: #666;
}

/* 創建時間欄 */
.col-created {
  width: 13%;
}

.created-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.created-time {
  font-size: 13px;
  color: #666;
}

.updated-time {
  font-size: 11px;
  color: #999;
}

/* 操作欄 */
.col-actions {
  width: 23%;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-action {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-action .icon {
  font-size: 15px;
}

.btn-edit {
  background: #e3f2fd;
  color: #1976d2;
}

.btn-edit:hover {
  background: #2196F3;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(33, 150, 243, 0.3);
}

.btn-publish {
  background: #e8f5e9;
  color: #388e3c;
}

.btn-publish:hover {
  background: #4CAF50;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
}

.btn-delete {
  background: #ffebee;
  color: #d32f2f;
}

.btn-delete:hover {
  background: #f44336;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(244, 67, 54, 0.3);
}

/* ============ 空狀態 ============ */
.empty-state {
  text-align: center;
  padding: 100px 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e8e8e8;
}

.empty-icon {
  font-size: 100px;
  margin-bottom: 24px;
  opacity: 0.5;
}

.empty-state h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 12px;
}

.empty-state p {
  font-size: 18px;
  color: #666;
  margin-bottom: 40px;
}

.btn-primary {
  padding: 16px 40px;
  background: #4CAF50;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 18px;
  font-weight: 500;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.2);
}

.btn-primary:hover {
  background: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(76, 175, 80, 0.4);
}

/* ============ 刪除確認對話框 ============ */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.dialog-content {
  background: white;
  border-radius: 16px;
  padding: 32px;
  width: 480px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  animation: dialogFadeIn 0.3s ease-out;
}

@keyframes dialogFadeIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.dialog-header h3 {
  font-size: 24px;
  margin-bottom: 8px;
  color: #333;
}

.dialog-body {
  margin-bottom: 32px;
}

.dialog-body p {
  margin-bottom: 12px;
  color: #666;
  font-size: 16px;
  line-height: 1.6;
}

.activity-name {
  font-weight: 600;
  color: #333;
  font-size: 18px;
  padding: 12px;
  background: #f8f8f8;
  border-radius: 6px;
  margin: 16px 0;
}

.warning {
  color: #d32f2f;
  font-weight: 600;
  margin-top: 16px;
  font-size: 15px;
}

.dialog-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
}

.btn-cancel,
.btn-confirm-delete {
  padding: 12px 32px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-cancel {
  background: #f0f0f0;
  color: #333;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-confirm-delete {
  background: #d32f2f;
  color: white;
}

.btn-confirm-delete:hover {
  background: #c62828;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(211, 47, 47, 0.4);
}
</style>