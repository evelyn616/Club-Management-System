<template>
  <div class="dtp-root">

    <!-- 顯示區 / 觸發器 -->
    <div class="dtp-display" @click="open = !open" :class="{ active: open, filled: modelValue }">
      <div class="dtp-display-left">
        <span class="dtp-display-date">{{ displayDate }}</span>
        <span class="dtp-display-time">{{ displayTime }}</span>
      </div>
      <span class="dtp-display-icon">{{ open ? '▲' : '▼' }}</span>
    </div>

    <!-- Picker 面板 -->
    <Teleport to="body">
      <div v-if="open" class="dtp-overlay" @click.self="close">
        <div class="dtp-panel" :style="panelStyle">

          <!-- ① 日期選擇 -->
          <div class="dtp-section date-section">
            <div class="cal-header">
              <button class="cal-nav" @click="prevMonth">‹</button>
              <span class="cal-title mono">{{ calTitle }}</span>
              <button class="cal-nav" @click="nextMonth">›</button>
            </div>

            <div class="cal-weekdays">
              <span v-for="d in weekdays" :key="d" class="cal-wd mono">{{ d }}</span>
            </div>

            <div class="cal-grid">
              <button
                v-for="cell in calCells"
                :key="cell.key"
                class="cal-cell"
                :class="{
                  'other-month': !cell.inMonth,
                  'today': cell.isToday,
                  'selected': cell.isSelected,
                  'disabled': cell.disabled
                }"
                :disabled="cell.disabled"
                @click="selectDate(cell)"
              >{{ cell.day }}</button>
            </div>
          </div>

          <!-- 分隔線 -->
          <div class="dtp-divider"></div>

          <!-- ② 時鐘選時 -->
          <div class="dtp-section clock-section">

            <!-- 時鐘本體 -->
            <div class="clock-left">
              <p class="clock-label mono">{{ clockModeLabel }}</p>
              <svg
                class="clock-svg"
                viewBox="0 0 220 220"
                @mousemove="onClockDrag"
                @mousedown="startDrag"
                @mouseup="endDrag"
                @touchmove.prevent="onClockTouch"
                @touchstart="startDrag"
                @touchend="endDrag"
              >
                <circle cx="110" cy="110" r="105" fill="#0a0a0a" />
                <circle cx="110" cy="110" r="100" fill="#111" />

                <g v-for="i in 60" :key="'m'+i">
                  <line
                    :x1="110 + 90 * Math.sin((i/60) * 2 * Math.PI)"
                    :y1="110 - 90 * Math.cos((i/60) * 2 * Math.PI)"
                    :x2="110 + (i % 5 === 0 ? 80 : 86) * Math.sin((i/60) * 2 * Math.PI)"
                    :y2="110 - (i % 5 === 0 ? 80 : 86) * Math.cos((i/60) * 2 * Math.PI)"
                    :stroke="i % 5 === 0 ? '#555' : '#333'"
                    :stroke-width="i % 5 === 0 ? 2 : 1"
                  />
                </g>

                <g v-for="h in 12" :key="'h'+h">
                  <text
                    :x="110 + 72 * Math.sin((h/12) * 2 * Math.PI)"
                    :y="110 - 72 * Math.cos((h/12) * 2 * Math.PI) + 5"
                    text-anchor="middle"
                    class="clock-num"
                    :class="{ 'clock-num-active': clockMode === 'hour' && (selectedHour % 12 || 12) === h }"
                  >{{ h }}</text>
                </g>

                <circle
                  v-if="clockMode === 'hour'"
                  :cx="110 + 58 * Math.sin((selectedHour % 12 / 12) * 2 * Math.PI)"
                  :cy="110 - 58 * Math.cos((selectedHour % 12 / 12) * 2 * Math.PI)"
                  r="16" fill="#ff2d6b" opacity="0.85"
                />
                <line
                  v-if="clockMode === 'hour'"
                  x1="110" y1="110"
                  :x2="110 + 50 * Math.sin((selectedHour % 12 / 12) * 2 * Math.PI)"
                  :y2="110 - 50 * Math.cos((selectedHour % 12 / 12) * 2 * Math.PI)"
                  stroke="#ff2d6b" stroke-width="2.5" stroke-linecap="round"
                />

                <g v-for="m in [0,5,10,15,20,25,30,35,40,45,50,55]" :key="'mp'+m">
                  <circle
                    v-if="clockMode === 'minute'"
                    :cx="110 + 76 * Math.sin((m/60) * 2 * Math.PI)"
                    :cy="110 - 76 * Math.cos((m/60) * 2 * Math.PI)"
                    r="3" :fill="selectedMinute === m ? '#ff2d6b' : '#444'"
                  />
                </g>

                <circle
                  v-if="clockMode === 'minute'"
                  :cx="110 + 76 * Math.sin((selectedMinute / 60) * 2 * Math.PI)"
                  :cy="110 - 76 * Math.cos((selectedMinute / 60) * 2 * Math.PI)"
                  r="12" fill="#ff2d6b" opacity="0.85"
                />
                <line
                  v-if="clockMode === 'minute'"
                  x1="110" y1="110"
                  :x2="110 + 70 * Math.sin((selectedMinute / 60) * 2 * Math.PI)"
                  :y2="110 - 70 * Math.cos((selectedMinute / 60) * 2 * Math.PI)"
                  stroke="#ff2d6b" stroke-width="2" stroke-linecap="round"
                />

                <circle cx="110" cy="110" r="4" fill="#ff2d6b" />
              </svg>
            </div>

            <!-- 右側：數字選擇 -->
            <div class="clock-right">

              <!-- 大字時間顯示 -->
              <div class="num-time-display">
                <div class="num-col">
                  <button class="num-step" @click="stepHour(1)">▲</button>
                  <button
                    class="num-seg"
                    :class="{ active: clockMode === 'hour' }"
                    @click="clockMode = 'hour'"
                  >{{ String(selectedHour % 12 || 12).padStart(2,'0') }}</button>
                  <button class="num-step" @click="stepHour(-1)">▼</button>
                </div>
                <span class="num-colon">:</span>
                <div class="num-col">
                  <button class="num-step" @click="stepMinute(5)">▲</button>
                  <button
                    class="num-seg"
                    :class="{ active: clockMode === 'minute' }"
                    @click="clockMode = 'minute'"
                  >{{ String(selectedMinute).padStart(2,'0') }}</button>
                  <button class="num-step" @click="stepMinute(-5)">▼</button>
                </div>
              </div>

              <!-- AM / PM -->
              <div class="ampm-wrap">
                <button class="ampm-btn" :class="{ active: !isPM }" @click="isPM = false; commit()">AM</button>
                <button class="ampm-btn" :class="{ active: isPM }" @click="isPM = true; commit()">PM</button>
              </div>

              <!-- 快捷分鐘 -->
              <div class="num-divider"></div>
              <p class="quick-label mono">快捷分鐘</p>
              <div class="minute-quick">
                <button
                  v-for="m in [0, 15, 30, 45]"
                  :key="m"
                  class="mq-btn"
                  :class="{ active: selectedMinute === m }"
                  @click="selectedMinute = m; clockMode = 'minute'; commit()"
                >:{{ String(m).padStart(2,'0') }}</button>
              </div>

              <!-- 整點快捷 -->
              <div class="hour-quick">
                <button
                  v-for="h in [1,2,3,4,5,6,7,8,9,10,11,12]"
                  :key="h"
                  class="hq-btn"
                  :class="{ active: (selectedHour % 12 || 12) === h }"
                  @click="setHour(h); commit()"
                >{{ h }}</button>
              </div>

            </div>

          </div>

          <!-- 底部按鈕 -->
          <div class="dtp-footer">
            <button class="dtp-btn-clear" @click="clearValue">清除</button>
            <button class="dtp-btn-ok" @click="close">確認</button>
          </div>

        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },  // "YYYY-MM-DDTHH:mm"
  label: { type: String, default: '' },
  minDateTime: { type: String, default: '' },
})
const emit = defineEmits(['update:modelValue'])

// ===== Panel open/close =====
const open = ref(false)
const triggerEl = ref(null)
const panelStyle = ref({})

const close = () => { open.value = false }

// ===== Calendar state =====
const today = new Date()
const viewYear = ref(today.getFullYear())
const viewMonth = ref(today.getMonth())   // 0-indexed
const selectedYear = ref(null)
const selectedMonth = ref(null)
const selectedDay = ref(null)

// ===== Clock state =====
const selectedHour = ref(9)   // 0-23
const selectedMinute = ref(0)
const isPM = ref(false)
const clockMode = ref('hour') // 'hour' | 'minute'
let dragging = false

const weekdays = ['日','一','二','三','四','五','六']
const calTitle = computed(() => `${viewYear.value} / ${String(viewMonth.value + 1).padStart(2,'0')}`)
const clockModeLabel = computed(() => clockMode.value === 'hour' ? 'SELECT HOUR' : 'SELECT MINUTE')

// ===== Init from modelValue =====
watch(() => props.modelValue, (val) => {
  if (val) {
    const d = new Date(val)
    if (!isNaN(d)) {
      selectedYear.value = d.getFullYear()
      selectedMonth.value = d.getMonth()
      selectedDay.value = d.getDate()
      viewYear.value = d.getFullYear()
      viewMonth.value = d.getMonth()
      selectedHour.value = d.getHours()
      selectedMinute.value = d.getMinutes()
      isPM.value = d.getHours() >= 12
    }
  }
}, { immediate: true })

// ===== Display =====
const displayDate = computed(() => {
  if (selectedYear.value === null) return props.label || '選擇日期'
  const d = new Date(selectedYear.value, selectedMonth.value, selectedDay.value)
  const days = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat']
  return `${selectedYear.value}/${String(selectedMonth.value+1).padStart(2,'0')}/${String(selectedDay.value).padStart(2,'0')} ${days[d.getDay()]}`
})
const displayTime = computed(() => {
  if (selectedYear.value === null) return '—'
  return `${String(selectedHour.value).padStart(2,'0')}:${String(selectedMinute.value).padStart(2,'0')}`
})

// ===== Calendar cells =====
const calCells = computed(() => {
  const cells = []
  const first = new Date(viewYear.value, viewMonth.value, 1)
  const startDow = first.getDay()
  const daysInMonth = new Date(viewYear.value, viewMonth.value + 1, 0).getDate()
  const daysInPrev = new Date(viewYear.value, viewMonth.value, 0).getDate()

  // Previous month filler
  for (let i = startDow - 1; i >= 0; i--) {
    const d = daysInPrev - i
    cells.push({ key: `p${d}`, day: d, inMonth: false, isToday: false, isSelected: false, disabled: true })
  }
  // Current month
  const tY = today.getFullYear(), tM = today.getMonth(), tD = today.getDate()
  for (let d = 1; d <= daysInMonth; d++) {
    const isToday = viewYear.value === tY && viewMonth.value === tM && d === tD
    const isSelected = selectedYear.value === viewYear.value && selectedMonth.value === viewMonth.value && selectedDay.value === d
    // Min date check
    let disabled = false
    if (props.minDateTime) {
      const min = new Date(props.minDateTime)
      const cellDate = new Date(viewYear.value, viewMonth.value, d)
      cellDate.setHours(23,59,59)
      if (cellDate < min) disabled = true
    }
    cells.push({ key: `c${d}`, day: d, inMonth: true, isToday, isSelected, disabled })
  }
  // Next month filler
  const remaining = 42 - cells.length
  for (let d = 1; d <= remaining; d++) {
    cells.push({ key: `n${d}`, day: d, inMonth: false, isToday: false, isSelected: false, disabled: true })
  }
  return cells
})

const prevMonth = () => {
  if (viewMonth.value === 0) { viewMonth.value = 11; viewYear.value-- }
  else viewMonth.value--
}
const nextMonth = () => {
  if (viewMonth.value === 11) { viewMonth.value = 0; viewYear.value++ }
  else viewMonth.value++
}

const selectDate = (cell) => {
  if (!cell.inMonth || cell.disabled) return
  selectedYear.value = viewYear.value
  selectedMonth.value = viewMonth.value
  selectedDay.value = cell.day
  clockMode.value = 'hour'
  commit()
}

// ===== Commit =====
const commit = () => {
  if (selectedYear.value === null) return
  const h = selectedHour.value
  const dt = `${selectedYear.value}-${String(selectedMonth.value+1).padStart(2,'0')}-${String(selectedDay.value).padStart(2,'0')}T${String(h).padStart(2,'0')}:${String(selectedMinute.value).padStart(2,'0')}:00`  // ← 加 :00
  emit('update:modelValue', dt)
}

const clearValue = () => {
  selectedYear.value = null
  selectedMonth.value = null
  selectedDay.value = null
  emit('update:modelValue', '')
  open.value = false
}

// ===== Number stepper helpers =====
const stepHour = (delta) => {
  const h12 = (selectedHour.value % 12 || 12) + delta
  const wrapped = ((h12 - 1 + 12) % 12) + 1
  selectedHour.value = isPM.value ? (wrapped === 12 ? 12 : wrapped + 12) : (wrapped === 12 ? 0 : wrapped)
  clockMode.value = 'hour'
  commit()
}
const stepMinute = (delta) => {
  selectedMinute.value = (selectedMinute.value + delta + 60) % 60
  clockMode.value = 'minute'
  commit()
}
const setHour = (h12) => {
  selectedHour.value = isPM.value ? (h12 === 12 ? 12 : h12 + 12) : (h12 === 12 ? 0 : h12)
  clockMode.value = 'hour'
}

// ===== Clock drag =====
const startDrag = () => { dragging = true }
const endDrag = () => {
  dragging = false
  if (clockMode.value === 'hour') clockMode.value = 'minute'
  commit()
}

const getAngleFromEvent = (e, svgEl) => {
  const rect = svgEl.getBoundingClientRect()
  const cx = rect.left + rect.width / 2
  const cy = rect.top + rect.height / 2
  const clientX = e.touches ? e.touches[0].clientX : e.clientX
  const clientY = e.touches ? e.touches[0].clientY : e.clientY
  let angle = Math.atan2(clientX - cx, cy - clientY) // radians
  if (angle < 0) angle += 2 * Math.PI
  return angle
}

const onClockDrag = (e) => {
  if (!dragging) return
  const svgEl = e.currentTarget
  const angle = getAngleFromEvent(e, svgEl)
  applyAngle(angle)
  commit()
}
const onClockTouch = (e) => {
  if (!dragging) return
  const svgEl = e.currentTarget
  const angle = getAngleFromEvent(e, svgEl)
  applyAngle(angle)
  commit()
}

const applyAngle = (angle) => {
  if (clockMode.value === 'hour') {
    let h = Math.round(angle / (2 * Math.PI / 12)) % 12
    selectedHour.value = isPM.value ? (h === 0 ? 12 : h + 12) : (h === 0 ? 0 : h)
  } else {
    let m = Math.round(angle / (2 * Math.PI / 60)) % 60
    selectedMinute.value = m
  }
}

// AM/PM sync
watch(isPM, (pm) => {
  const h = selectedHour.value % 12
  selectedHour.value = pm ? (h === 0 ? 12 : h + 12) : h
  commit()
})

// Panel position
watch(open, async (val) => {
  if (val) {
    clockMode.value = 'hour'
    await nextTick()
    // Just center it on screen
    panelStyle.value = {}
  }
})

// Close on outside click
const onKeydown = (e) => { if (e.key === 'Escape') close() }
onMounted(() => { window.addEventListener('keydown', onKeydown) })
onUnmounted(() => { window.removeEventListener('keydown', onKeydown) })
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Space+Mono:wght@400;700&family=Noto+Sans+TC:wght@400;700&display=swap');

/* ===== Root ===== */
.dtp-root { position: relative; width: 100%; }

/* ===== Display trigger ===== */
.dtp-display {
  display: flex; align-items: center; justify-content: space-between;
  padding: 0.7rem 0.9rem; border: 1px solid #e0e0e0;
  cursor: pointer; transition: border-color 0.2s; background: #fff;
  user-select: none;
}
.dtp-display:hover, .dtp-display.active { border-color: #0a0a0a; }
.dtp-display.filled { border-color: #0a0a0a; }
.dtp-display-left { display: flex; flex-direction: column; gap: 0.1rem; }
.dtp-display-date {
  font-family: 'Noto Sans TC', sans-serif; font-size: 0.88rem; color: #0a0a0a;
}
.dtp-display-time {
  font-family: 'Space Mono', monospace; font-size: 0.75rem; color: #ff2d6b; letter-spacing: 0.05em;
}
.dtp-display-icon {
  font-family: 'Space Mono', monospace; font-size: 0.55rem; color: #aaa;
}

/* ===== Overlay ===== */
.dtp-overlay {
  position: fixed; inset: 0; z-index: 9999;
  background: rgba(0,0,0,0.45); backdrop-filter: blur(2px);
  display: flex; align-items: center; justify-content: center;
}

/* ===== Panel ===== */
.dtp-panel {
  background: #fff; display: flex; flex-direction: row;
  box-shadow: 0 24px 64px rgba(0,0,0,0.22);
  border: 1px solid #e0e0e0;
  max-height: 95vh; overflow-y: auto;
  animation: panelIn 0.18s ease;
}
@keyframes panelIn {
  from { opacity: 0; transform: scale(0.96) translateY(8px); }
  to   { opacity: 1; transform: scale(1) translateY(0); }
}

.dtp-section { padding: 1.5rem; }
.dtp-divider { width: 1px; background: #f0f0f0; }

/* ===== Calendar ===== */
.date-section { min-width: 252px; }

.cal-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem;
}
.cal-nav {
  width: 28px; height: 28px; border: 1px solid #e0e0e0; background: #fff;
  cursor: pointer; font-size: 1rem; color: #555; display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
}
.cal-nav:hover { background: #0a0a0a; color: #fff; border-color: #0a0a0a; }
.cal-title { font-family: 'Space Mono', monospace; font-size: 0.75rem; letter-spacing: 0.1em; color: #0a0a0a; }

.cal-weekdays {
  display: grid; grid-template-columns: repeat(7, 1fr);
  margin-bottom: 0.4rem;
}
.cal-wd {
  text-align: center; font-family: 'Space Mono', monospace;
  font-size: 0.58rem; letter-spacing: 0.05em; color: #bbb; padding: 0.25rem 0;
}

.cal-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 2px; }
.cal-cell {
  height: 32px; display: flex; align-items: center; justify-content: center;
  font-family: 'Noto Sans TC', sans-serif; font-size: 0.78rem; cursor: pointer;
  border: none; background: transparent; transition: all 0.12s; border-radius: 0;
  color: #0a0a0a;
}
.cal-cell:hover:not(:disabled):not(.selected) { background: #f5f5f5; }
.cal-cell.other-month { color: #ddd; }
.cal-cell.today { font-weight: 700; color: #ff2d6b; }
.cal-cell.selected { background: #0a0a0a; color: #fff; }
.cal-cell.selected.today { background: #ff2d6b; }
.cal-cell:disabled { cursor: default; opacity: 0.3; }

/* ===== Clock ===== */
.clock-section {
  display: flex; flex-direction: row; gap: 1.25rem;
  align-items: flex-start; padding: 1.25rem 1.25rem 1rem;
}

/* 左：時鐘 */
.clock-left { display: flex; flex-direction: column; align-items: center; gap: 0.5rem; }
.clock-label { font-family: 'Space Mono', monospace; font-size: 0.55rem; letter-spacing: 0.2em; color: #ff2d6b; font-weight: 700; margin: 0; align-self: flex-start; }
.clock-svg { width: 190px; height: 190px; cursor: pointer; touch-action: none; }

/* SVG clock text */
.clock-num { font-family: 'Space Mono', monospace; font-size: 12px; fill: #666; user-select: none; }
.clock-num-active { fill: #ff2d6b; font-weight: 700; }

/* 右：數字控制 */
.clock-right {
  display: flex; flex-direction: column; gap: 0.75rem;
  padding-top: 1.4rem; min-width: 140px;
}

/* 大字時間 + 上下箭頭 */
.num-time-display { display: flex; align-items: center; gap: 6px; }
.num-col { display: flex; flex-direction: column; align-items: center; gap: 2px; }
.num-step {
  width: 36px; height: 22px; border: none; background: transparent;
  font-family: 'Space Mono', monospace; font-size: 0.65rem; color: #ccc;
  cursor: pointer; transition: color 0.12s; line-height: 1;
}
.num-step:hover { color: #ff2d6b; }
.num-seg {
  font-family: 'Space Mono', monospace; font-size: 2.2rem; font-weight: 700;
  width: 64px; text-align: center;
  border: 2px solid transparent; background: transparent; cursor: pointer;
  color: #ccc; transition: all 0.15s; letter-spacing: 0.02em; line-height: 1.1; padding: 0.1rem 0;
}
.num-seg.active { border-color: #ff2d6b; color: #ff2d6b; }
.num-seg:hover:not(.active) { color: #0a0a0a; border-color: #e0e0e0; }
.num-colon { font-family: 'Space Mono', monospace; font-size: 2rem; color: #ccc; margin-top: 22px; }

/* AM/PM */
.ampm-wrap { display: flex; gap: 4px; }
.ampm-btn {
  font-family: 'Space Mono', monospace; font-size: 0.62rem; letter-spacing: 0.08em;
  padding: 0.3rem 0.7rem; border: 1px solid #e0e0e0; cursor: pointer;
  background: #fff; color: #aaa; transition: all 0.15s;
}
.ampm-btn.active { background: #0a0a0a; color: #fff; border-color: #0a0a0a; }

/* 分隔線 */
.num-divider { height: 1px; background: #f0f0f0; }
.quick-label { font-family: 'Space Mono', monospace; font-size: 0.52rem; letter-spacing: 0.15em; color: #bbb; margin: 0; }

/* 快捷分鐘 */
.minute-quick { display: flex; gap: 4px; flex-wrap: wrap; }
.mq-btn {
  font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.04em;
  padding: 0.22rem 0.45rem; border: 1px solid #e0e0e0; cursor: pointer;
  background: #fff; color: #aaa; transition: all 0.15s;
}
.mq-btn.active, .mq-btn:hover { background: #0a0a0a; color: #fff; border-color: #0a0a0a; }

/* 整點快捷 grid */
.hour-quick { display: grid; grid-template-columns: repeat(4, 1fr); gap: 3px; }
.hq-btn {
  font-family: 'Space Mono', monospace; font-size: 0.6rem;
  padding: 0.2rem 0; border: 1px solid #e0e0e0; cursor: pointer;
  background: #fff; color: #aaa; transition: all 0.15s; text-align: center;
}
.hq-btn.active { background: #ff2d6b; color: #fff; border-color: #ff2d6b; }
.hq-btn:hover:not(.active) { background: #f5f5f5; color: #0a0a0a; }

/* ===== Footer ===== */
.dtp-footer {
  grid-column: 1 / -1; display: flex; justify-content: space-between; align-items: center;
  padding: 1rem 1.5rem; border-top: 1px solid #f0f0f0;
  background: #fafafa;
}
/* Override: footer span full row */
.dtp-panel { flex-wrap: wrap; }
.dtp-footer { width: 100%; flex-basis: 100%; }

.dtp-btn-clear {
  font-family: 'Space Mono', monospace; font-size: 0.65rem; letter-spacing: 0.08em;
  background: transparent; border: 1px solid #e0e0e0; color: #aaa; cursor: pointer;
  padding: 0.45rem 1rem; transition: all 0.15s;
}
.dtp-btn-clear:hover { border-color: #0a0a0a; color: #0a0a0a; }
.dtp-btn-ok {
  font-family: 'Space Mono', monospace; font-size: 0.65rem; letter-spacing: 0.08em;
  background: #0a0a0a; border: none; color: #fff; cursor: pointer;
  padding: 0.5rem 1.4rem; transition: background 0.15s;
}
.dtp-btn-ok:hover { background: #ff2d6b; }

/* ===== Responsive ===== */
@media (max-width: 560px) {
  .dtp-panel { flex-direction: column; }
  .dtp-divider { width: 100%; height: 1px; }
  .date-section, .clock-section { min-width: unset; width: 100%; }
}
</style>