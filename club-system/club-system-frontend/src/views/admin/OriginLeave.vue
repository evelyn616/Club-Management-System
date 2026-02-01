<template>
  <div class="admin-container">
    <div class="header">
      <h1><i class="fas fa-user-shield"></i> 管理員審核後台</h1>
      <p class="stats-tag">總申請數：{{ mockData.length }} | 待處理：{{ pendingCount }}</p>
    </div>

    <section class="filter-section card">
      <div class="filter-controls">
        <div class="btn-group">
          <button 
            :class="['filter-btn', { active: filterType === 'member' }]"
            @click="filterType = 'member'; filterValue = 'all'"
          >按會員</button>
          <button 
            :class="['filter-btn', { active: filterType === 'activity' }]"
            @click="filterType = 'activity'; filterValue = 'all'"
          >按活動</button>
        </div>

        <div class="picker-wrapper">
          <label>{{ filterType === 'member' ? '選擇會員' : '選擇活動' }}</label>
          <select v-model="filterValue" class="picker-select">
            <option value="all">顯示全部</option>
            <option v-for="opt in currentOptions" :key="opt" :value="opt">
              {{ opt }}
            </option>
          </select>
        </div>

        <button @click="resetFilter" class="btn-outline">重置篩選</button>
      </div>
    </section>

    <section class="list-section">
      <div class="card">
        <table class="styled-table">
          <thead>
            <tr>
              <th>單號</th>
              <th>會員/活動</th>
              <th>請假類型</th>
              <th>狀態</th>
              <th>審核操作</th> <th>管理</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in filteredData" :key="item.id">
              <td>#{{ item.id }}</td>
              <td>
                <div class="id-info">
                  <span class="id-badge">👤 {{ item.memberId }}</span>
                  <span class="activity-tag">🎯 {{ item.activityId }}</span>
                </div>
              </td>
              <td>{{ item.leaveType }}</td>
              <td>
                <span :class="['status-badge', item.status.toLowerCase()]">
                  {{ item.status }}
                </span>
              </td>
              
              <td>
                <select 
                  :value="item.status" 
                  @change="(e) => updateStatus(item.id, e.target.value)"
                  class="action-picker"
                  :disabled="item.status !== 'PENDING' && item.status !== 'REJECTED'"
                >
                  <option value="PENDING" disabled>等待處理中</option>
                  <option value="APPROVED">✅ 准假 (Approve)</option>
                  <option value="REJECTED">❌ 駁回 (Reject)</option>
                </select>
              </td>

              <td>
                <button @click="removeData(item.id)" class="btn-delete-icon" title="刪除紀錄">
                  <i class="fas fa-trash-alt"></i> 刪除
                </button>
              </td>
            </tr>
            <tr v-if="filteredData.length === 0">
              <td colspan="6" class="no-data">查無相符的請假紀錄</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
import axios from 'axios'; 
import { ref, computed } from 'vue';

const mockData = ref([
  { id: 101, memberId: 'M0001', activityId: 'ACT_01', leaveType: '事假', reason: '家裡有事', status: 'PENDING' },
  { id: 102, memberId: 'M0005', activityId: 'ACT_02', leaveType: '病假', reason: '感冒發燒', status: 'APPROVED' },
  { id: 103, memberId: 'M0001', activityId: 'ACT_02', leaveType: '公假', reason: '研討會', status: 'PENDING' },
  { id: 104, memberId: 'M0012', activityId: 'ACT_01', leaveType: '事假', reason: '搬家', status: 'REJECTED' },
]);

const filterType = ref('member');
const filterValue = ref('all');

const currentOptions = computed(() => {
  return [...new Set(mockData.value.map(item => filterType.value === 'member' ? item.memberId : item.activityId))];
});

const filteredData = computed(() => {
  if (filterValue.value === 'all') return mockData.value;
  return mockData.value.filter(item => 
    (filterType.value === 'member' ? item.memberId : item.activityId) === filterValue.value
  );
});

const pendingCount = computed(() => mockData.value.filter(i => i.status === 'PENDING').length);

const updateStatus = (id, newStatus) => {
  const item = mockData.value.find(i => i.id === id);
  if (item) {
    item.status = newStatus;
    // 這裡可以加入 API 呼叫
    console.log(`單號 ${id} 狀態更新為: ${newStatus}`);
  }
};

const removeData = (id) => {
  if(confirm('確定要永久刪除此紀錄嗎？')) {
    mockData.value = mockData.value.filter(item => item.id !== id);
  }
};

const resetFilter = () => {
  filterType.value = 'member';
  filterValue.value = 'all';
};
</script>

<style scoped>
.admin-container { padding: 30px; background-color: #f8fafc; min-height: 100vh; }
.card { background: white; padding: 20px; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); margin-bottom: 20px; }

/* 篩選器 */
.filter-controls { display: flex; align-items: center; gap: 20px; }
.btn-group { display: flex; background: #f1f5f9; padding: 4px; border-radius: 8px; }
.filter-btn { border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; background: transparent; transition: 0.2s; }
.filter-btn.active { background: white; color: #10b981; font-weight: bold; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }

.picker-select, .action-picker {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background-color: white;
  cursor: pointer;
}

/* 操作選單樣式 */
.action-picker {
  width: 140px;
  border-left: 4px solid #10b981; /* 綠色左邊線提示這是操作區 */
  font-size: 0.9em;
}
.action-picker:disabled {
  background-color: #f1f5f9;
  border-left-color: #cbd5e1;
  cursor: not-allowed;
}

/* 表格優化 */
.styled-table { width: 100%; border-collapse: collapse; }
.styled-table th { text-align: left; padding: 12px; border-bottom: 2px solid #f1f5f9; color: #64748b; font-size: 0.85em; }
.styled-table td { padding: 16px 12px; border-bottom: 1px solid #f1f5f9; }

.id-info { display: flex; flex-direction: column; gap: 4px; }
.id-badge { font-family: monospace; font-weight: bold; color: #1e293b; }
.activity-tag { font-size: 0.75em; color: #94a3b8; }

.status-badge { padding: 4px 8px; border-radius: 4px; font-size: 0.75em; font-weight: 700; }
.status-badge.pending { background: #fef3c7; color: #92400e; }
.status-badge.approved { background: #d1fae5; color: #065f46; }
.status-badge.rejected { background: #fee2e2; color: #991b1b; }

/* 刪除按鈕 */
.btn-delete-icon {
  background: none;
  border: none;
  color: #ef4444;
  cursor: pointer;
  font-size: 0.9em;
  padding: 8px;
  border-radius: 4px;
}
.btn-delete-icon:hover { background: #fef2f2; }

.no-data { text-align: center; padding: 40px; color: #94a3b8; }
</style>