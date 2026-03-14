<template>
  <div class="db-wrap">

    <!-- ── Navbar ── -->
    <nav class="navbar" :class="{ 'navbar-hidden': navHidden }">
      <div class="nav-inner">
        <router-link to="/admin/activity-management-container" class="nav-logo">CLUB SYSTEM</router-link>
        <span class="nav-crumb">ADMIN / <span class="nav-accent">草稿箱</span></span>
      </div>
    </nav>

    <!-- ── Header ── -->
    <div class="page-header">
      <div class="header-left">
        <div class="eyebrow"><span class="eyebrow-line"></span><span class="eyebrow-text">DRAFT BOX</span></div>
        <h1 class="page-title">草稿<span class="title-accent">箱</span></h1>
        <p class="page-sub" v-if="!loading">
          共 <span class="sub-num">{{ draftActivities.length }}</span> 個草稿活動
        </p>
      </div>
      <button class="btn-create" @click="router.push({ name: 'create-activity-container' })">
        ＋ 建立新活動
      </button>
    </div>

    <!-- ── Loading ── -->
    <div class="state-wrap" v-if="loading">
      <div class="loading-bars">
        <span v-for="i in 5" :key="i" :style="{ animationDelay: (i * 0.1) + 's' }"></span>
      </div>
      <p class="mono state-label">LOADING...</p>
    </div>

    <!-- ── Empty ── -->
    <div class="state-wrap" v-else-if="draftActivities.length === 0">
      <div class="empty-big">EMPTY</div>
      <p class="empty-desc">草稿箱是空的，去建立第一個活動吧</p>
      <button class="cta-btn" @click="router.push({ name: 'create-activity-container' })">
        ＋ 建立活動
      </button>
    </div>

    <!-- ── Table ── -->
    <div class="table-section" v-else>
      <div class="table-wrap">
        <table class="draft-table">
          <thead>
            <tr>
              <th class="th-num">#</th>
              <th>活動標題</th>
              <th>類型</th>
              <th>開始時間</th>
              <th>地點</th>
              <th>建立時間</th>
              <th class="th-center">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(draft, index) in draftActivities"
              :key="draft.id"
              class="draft-row"
              :style="{ animationDelay: (index * 0.04) + 's' }"
            >
              <td class="td-num mono">{{ String(index + 1).padStart(2, '0') }}</td>
              <td class="td-title">
                <span class="draft-title">{{ draft.title }}</span>
                <span class="draft-desc" v-if="draft.description">{{ draft.description }}</span>
              </td>
              <td>
                <span class="type-tag">{{ getActivityTypeLabel(draft.activityType) }}</span>
              </td>
              <td class="mono td-time">{{ formatDateTime(draft.startTime) }}</td>
              <td class="td-loc">{{ draft.location || '—' }}</td>
              <td class="mono td-time">{{ formatDateTime(draft.createdAt) }}</td>
              <td class="td-actions">
                <div class="action-group">
                  <button class="act-btn edit" @click="updateActivity(draft.id)">編輯</button>
                  <button class="act-btn publish" @click="goToPublish(draft.id)">發布</button>
                  <button class="act-btn delete" @click="deleteDraft(draft.id, draft.title)">刪除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="table-footer mono">SHOWING {{ draftActivities.length }} DRAFTS</div>
    </div>

    <!-- ── Delete Dialog ── -->
    <Teleport to="body">
      <div v-if="showDeleteDialog" class="modal-overlay" @click.self="cancelDelete">
        <div class="dialog-box">
          <div class="dialog-header">
            <h2 class="dialog-title">確認刪除</h2>
            <button class="dialog-close" @click="cancelDelete">×</button>
          </div>
          <div class="dialog-body">
            <p class="dialog-msg">確定要永久刪除以下草稿嗎？此操作無法復原。</p>
            <div class="dialog-target">
              <span class="target-label mono">DRAFT</span>
              <span class="target-name">{{ deleteTarget.name }}</span>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="modal-btn cancel" @click="cancelDelete">取消</button>
            <button class="modal-btn danger" @click="confirmDelete" :disabled="deleting">
              {{ deleting ? '刪除中...' : '確定刪除' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'

const router = useRouter()

const loading = ref(false)
const draftActivities = ref([])
const showDeleteDialog = ref(false)
const deleteTarget = ref({ id: null, name: '' })
const deleting = ref(false)

// Navbar scroll
const navHidden = ref(false)
let lastY = 0
const onScroll = () => {
  const y = window.scrollY
  navHidden.value = y > lastY && y > 60
  lastY = y
}
onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  loadDraftActivities()
})
onUnmounted(() => window.removeEventListener('scroll', onScroll))

const loadDraftActivities = async () => {
  loading.value = true
  try {
    const response = await activityApi.getDraftActivities()
    draftActivities.value = response.data
  } catch (e) {
    console.error('載入草稿失敗:', e)
    alert('載入草稿失敗，請稍後再試')
  } finally {
    loading.value = false
  }
}

const updateActivity = (id) => router.push(`/admin/update-activity-container/${id}`)
const goToPublish = (id) => router.push({ name: 'publish-activity-container', params: { activityId: id } })

const deleteDraft = (id, name) => {
  deleteTarget.value = { id, name }
  showDeleteDialog.value = true
}
const cancelDelete = () => {
  showDeleteDialog.value = false
  deleteTarget.value = { id: null, name: '' }
}
const confirmDelete = async () => {
  deleting.value = true
  try {
    await activityApi.deleteDraftActivity(deleteTarget.value.id)
    draftActivities.value = draftActivities.value.filter(d => d.id !== deleteTarget.value.id)
    showDeleteDialog.value = false
    deleteTarget.value = { id: null, name: '' }
  } catch (e) {
    const s = e.response?.status
    alert(s === 403 ? '沒有權限刪除此草稿'
      : s === 404 ? '此草稿不存在'
      : s === 400 ? '只能刪除草稿狀態的活動'
      : '刪除失敗，請稍後再試')
  } finally {
    deleting.value = false
  }
}

const getActivityTypeLabel = (t) =>
  ({ REGULAR: '社課', SPECIAL: '特殊活動', TRAINING: '團練', PERFORMANCE: '演出', COMPETITION: '比賽' })[t] || t || '—'

const formatDateTime = (dt) => {
  if (!dt) return '—'
  const d = new Date(dt)
  return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Space+Mono:wght@400;700&family=Noto+Sans+TC:wght@300;400;500;700&display=swap');

* { box-sizing: border-box; }
.db-wrap { min-height: 100vh; background: #fff; font-family: 'Noto Sans TC', sans-serif; color: #0a0a0a; }
.mono { font-family: 'Space Mono', monospace; }

/* Navbar */
.navbar {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  padding: 1rem 3rem; background: rgba(255,255,255,0.92);
  backdrop-filter: blur(12px); border-bottom: 1px solid rgba(0,0,0,0.08);
  transform: translateY(0); transition: transform 0.3s ease;
}
.navbar-hidden { transform: translateY(-100%); }
.nav-inner { max-width: 1400px; margin: 0 auto; display: flex; justify-content: space-between; align-items: center; }
.nav-logo { font-family: 'Space Mono', monospace; font-size: 0.9rem; font-weight: 700; letter-spacing: 0.18em; color: #0a0a0a; text-decoration: none; }
.nav-logo:hover { color: #ff2d6b; }
.nav-crumb { font-family: 'Space Mono', monospace; font-size: 0.62rem; letter-spacing: 0.15em; color: #aaa; }
.nav-accent { color: #ff2d6b; }

/* Header */
.page-header {
  max-width: 1400px; margin: 0 auto;
  padding: 8rem 3rem 2.5rem;
  display: flex; justify-content: space-between; align-items: flex-end;
  border-bottom: 2px solid #0a0a0a;
}
.eyebrow { display: flex; align-items: center; gap: 0.6rem; margin-bottom: 0.6rem; }
.eyebrow-line { display: block; width: 24px; height: 2px; background: #ff2d6b; }
.eyebrow-text { font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.2em; color: #ff2d6b; }
.page-title { font-family: 'Bebas Neue', sans-serif; font-size: 4rem; letter-spacing: 0.06em; line-height: 1; margin: 0; }
.title-accent { color: #ff2d6b; }
.page-sub { font-size: 0.82rem; color: #888; margin: 0.4rem 0 0; }
.sub-num { font-family: 'Space Mono', monospace; font-weight: 700; color: #ff2d6b; }

.btn-create {
  background: #0a0a0a; color: #fff; border: none;
  font-family: 'Space Mono', monospace; font-size: 0.72rem; font-weight: 700; letter-spacing: 0.08em;
  padding: 0.75rem 1.75rem; cursor: pointer; transition: background 0.2s;
  align-self: flex-end; margin-bottom: 0.25rem;
}
.btn-create:hover { background: #ff2d6b; }

/* States */
.state-wrap { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 50vh; gap: 1rem; }
.loading-bars { display: flex; gap: 4px; }
.loading-bars span { display: block; width: 3px; height: 28px; background: #ff2d6b; border-radius: 2px; animation: barPulse 0.8s ease-in-out infinite alternate; }
@keyframes barPulse { from { transform: scaleY(0.3); opacity: 0.3; } to { transform: scaleY(1); opacity: 1; } }
.state-label { font-size: 0.68rem; letter-spacing: 0.2em; color: #aaa; margin: 0; }
.empty-big { font-family: 'Bebas Neue', sans-serif; font-size: 8rem; color: transparent; -webkit-text-stroke: 1px #eee; line-height: 1; }
.empty-desc { font-size: 0.85rem; color: #aaa; text-align: center; margin: 0; }
.cta-btn { background: #0a0a0a; color: #fff; border: none; font-family: 'Space Mono', monospace; font-size: 0.68rem; letter-spacing: 0.08em; padding: 0.7rem 1.5rem; cursor: pointer; transition: background 0.2s; }
.cta-btn:hover { background: #ff2d6b; }

/* Table */
.table-section { max-width: 1400px; margin: 0 auto; padding: 2.5rem 3rem 4rem; }
.table-wrap { overflow-x: auto; border: 1px solid #e8e8e8; }
.draft-table { width: 100%; border-collapse: collapse; background: #fff; }

.draft-table thead tr { background: #0a0a0a; }
.draft-table th {
  padding: 0.75rem 1rem; text-align: left;
  font-family: 'Space Mono', monospace; font-size: 0.58rem;
  letter-spacing: 0.15em; color: #fff; font-weight: 400; white-space: nowrap;
}
.th-num { width: 48px; }
.th-center { text-align: center !important; }

@keyframes rowFadeUp { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
.draft-row { border-bottom: 1px solid #f5f5f5; animation: rowFadeUp 0.35s ease both; transition: background 0.15s; }
.draft-row:hover { background: #fafafa; }
.draft-row:last-child { border-bottom: none; }
.draft-table td { padding: 1rem 1rem; vertical-align: middle; }

.td-num { font-size: 0.68rem; color: #ccc; letter-spacing: 0.1em; }
.td-title { display: flex; flex-direction: column; gap: 3px; }
.draft-title { font-weight: 700; font-size: 0.95rem; }
.draft-desc { font-size: 0.75rem; color: #aaa; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 280px; }
.td-time { font-size: 0.72rem; color: #888; letter-spacing: 0.04em; white-space: nowrap; }
.td-loc { font-size: 0.82rem; color: #666; }

.type-tag {
  font-family: 'Space Mono', monospace; font-size: 0.58rem; letter-spacing: 0.08em;
  background: #f5f5f5; color: #555; padding: 0.25rem 0.75rem;
}

.td-actions { text-align: center; }
.action-group { display: flex; gap: 0.4rem; justify-content: center; flex-wrap: wrap; }
.act-btn {
  font-family: 'Space Mono', monospace; font-size: 0.6rem; font-weight: 700; letter-spacing: 0.06em;
  padding: 0.4rem 0.85rem; border: 1px solid; cursor: pointer; transition: all 0.15s; white-space: nowrap;
}
.act-btn.edit    { border-color: #0a0a0a; background: transparent; color: #0a0a0a; }
.act-btn.edit:hover    { background: #0a0a0a; color: #fff; }
.act-btn.publish { border-color: #ff2d6b; background: transparent; color: #ff2d6b; }
.act-btn.publish:hover { background: #ff2d6b; color: #fff; }
.act-btn.delete  { border-color: #e0e0e0; background: transparent; color: #aaa; }
.act-btn.delete:hover  { border-color: #dc2626; color: #dc2626; }

.table-footer { margin-top: 1rem; text-align: right; font-size: 0.6rem; letter-spacing: 0.15em; color: #ccc; }

/* Dialog */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); backdrop-filter: blur(6px); z-index: 200; display: flex; align-items: center; justify-content: center; }
.dialog-box { background: #fff; width: 400px; max-width: 90vw; }
.dialog-header { display: flex; justify-content: space-between; align-items: center; padding: 1.25rem 1.5rem; border-bottom: 1px solid #f0f0f0; }
.dialog-title { font-family: 'Bebas Neue', sans-serif; font-size: 1.5rem; letter-spacing: 0.06em; margin: 0; }
.dialog-close { background: none; border: none; font-size: 1.3rem; cursor: pointer; color: #aaa; }
.dialog-close:hover { color: #0a0a0a; }
.dialog-body { padding: 1.5rem; }
.dialog-msg { font-size: 0.88rem; color: #666; margin: 0 0 1rem; }
.dialog-target { background: #fafafa; border-left: 3px solid #dc2626; padding: 0.75rem 1rem; }
.target-label { font-size: 0.58rem; letter-spacing: 0.15em; color: #aaa; display: block; margin-bottom: 4px; }
.target-name { font-weight: 700; font-size: 0.95rem; }
.dialog-footer { display: flex; gap: 0.75rem; padding: 1rem 1.5rem; border-top: 1px solid #f0f0f0; justify-content: flex-end; }
.modal-btn { font-family: 'Space Mono', monospace; font-size: 0.68rem; letter-spacing: 0.08em; padding: 0.6rem 1.25rem; cursor: pointer; border: none; transition: all 0.2s; }
.modal-btn.cancel { background: #f5f5f5; color: #555; }
.modal-btn.cancel:hover { background: #e8e8e8; }
.modal-btn.danger { background: #dc2626; color: #fff; }
.modal-btn.danger:hover:not(:disabled) { background: #b91c1c; }
.modal-btn:disabled { opacity: 0.4; cursor: not-allowed; }

@media (max-width: 768px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 1.5rem; }
  .page-header, .table-section { padding-left: 1.25rem; padding-right: 1.25rem; }
  .navbar { padding-left: 1.25rem; padding-right: 1.25rem; }
}
</style>