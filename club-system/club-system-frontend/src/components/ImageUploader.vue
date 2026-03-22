<template>
  <div class="uploader">

    <!-- 現有圖片預覽 / 上傳區 -->
    <div
      class="upload-area"
      :class="{
        'has-image': previewUrl && !error,
        'is-dragging': isDragging,
        'has-error': errorMsg
      }"
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @drop.prevent="onDrop"
      @click="triggerInput"
    >
      <!-- 預覽圖 -->
      <img v-if="previewUrl && !error" :src="previewUrl" class="preview-img" alt="封面預覽" />

      <!-- 上傳提示 -->
      <div class="upload-hint" v-if="!previewUrl || error">
        <div class="upload-icon">↑</div>
        <p class="hint-main mono">點擊或拖曳上傳圖片</p>
        <p class="hint-sub mono">JPG / PNG / WebP · 最大 5MB · 不支援 SVG</p>
      </div>

      <!-- 已有圖片的替換提示（hover 顯示） -->
      <div class="replace-hint" v-if="previewUrl && !error">
        <span class="mono">點擊替換圖片</span>
      </div>

      <!-- 上傳中 overlay -->
      <div class="uploading-overlay" v-if="uploading">
        <div class="uploading-bars">
          <span v-for="i in 4" :key="i" :style="{ animationDelay: (i * 0.12) + 's' }"></span>
        </div>
        <p class="mono">上傳中...</p>
      </div>
    </div>

    <!-- 隱藏的 file input -->
    <input
      ref="fileInput"
      type="file"
      accept="image/jpeg,image/png,image/webp,image/gif"
      style="display: none"
      @change="onFileChange"
    />

    <!-- 錯誤訊息 -->
    <p class="error-msg mono" v-if="errorMsg">✕ {{ errorMsg }}</p>

    <!-- 成功 + 清除按鈕 -->
    <div class="actions" v-if="previewUrl && !uploading">
      <p class="success-hint mono" v-if="!errorMsg">✓ 圖片已上傳</p>
      <button type="button" class="clear-btn mono" @click.stop="clearImage">移除圖片 ×</button>
    </div>

  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' }
})
const emit = defineEmits(['update:modelValue'])

const fileInput = ref(null)
const previewUrl = ref(props.modelValue || '')
const uploading = ref(false)
const isDragging = ref(false)
const errorMsg = ref('')
const error = ref(false)

// 同步外部 v-model 變化（例如編輯活動時帶入現有 URL）
watch(() => props.modelValue, (val) => {
  if (val && val !== previewUrl.value) previewUrl.value = val
})

const triggerInput = () => {
  if (uploading.value) return
  fileInput.value?.click()
}

const onDrop = (e) => {
  isDragging.value = false
  const file = e.dataTransfer?.files?.[0]
  if (file) processFile(file)
}

const onFileChange = (e) => {
  const file = e.target.files?.[0]
  if (file) processFile(file)
  // 清空 input 讓同一檔案可以重複選
  e.target.value = ''
}

// ── 資安驗證 ──
const ALLOWED_MIME = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']
const ALLOWED_EXT  = ['.jpg', '.jpeg', '.png', '.webp', '.gif']
const MAX_SIZE_MB  = 5

const validateFile = (file) => {
  // 1. 檢查 MIME type（前端初步驗證）
  if (!ALLOWED_MIME.includes(file.type)) {
    return `不支援此檔案格式（${file.type}），請上傳 JPG、PNG 或 WebP`
  }

  // 2. 檢查副檔名（防止偽裝的 SVG 或其他危險格式）
  const ext = '.' + file.name.split('.').pop().toLowerCase()
  if (!ALLOWED_EXT.includes(ext)) {
    return `不支援副檔名 ${ext}，請上傳 JPG、PNG 或 WebP`
  }

  // 3. 明確拒絕 SVG（XSS 風險）
  if (file.type === 'image/svg+xml' || ext === '.svg') {
    return 'SVG 格式因安全考量不支援上傳'
  }

  // 4. 檢查檔案大小
  if (file.size > MAX_SIZE_MB * 1024 * 1024) {
    return `檔案大小超過 ${MAX_SIZE_MB}MB 限制`
  }

  return null // 通過
}

// ── Magic Bytes 驗證（讀取檔案頭，防止偽裝） ──
const checkMagicBytes = (file) => {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onloadend = (e) => {
      const arr = new Uint8Array(e.target.result)
      const hex = Array.from(arr).map(b => b.toString(16).padStart(2, '0')).join('').toUpperCase()

      // JPEG: FF D8 FF
      if (hex.startsWith('FFD8FF')) { resolve(true); return }
      // PNG: 89 50 4E 47
      if (hex.startsWith('89504E47')) { resolve(true); return }
      // WebP: 52 49 46 46 ... 57 45 42 50
      if (hex.startsWith('52494646') && hex.slice(16, 24) === '57454250') { resolve(true); return }
      // GIF: 47 49 46 38
      if (hex.startsWith('47494638')) { resolve(true); return }

      resolve(false)
    }
    reader.readAsArrayBuffer(file.slice(0, 12))
  })
}

const processFile = async (file) => {
  errorMsg.value = ''
  error.value = false

  // 初步驗證
  const validationError = validateFile(file)
  if (validationError) {
    errorMsg.value = validationError
    return
  }

  // Magic bytes 驗證（防止偽裝）
  const isValidMagic = await checkMagicBytes(file)
  if (!isValidMagic) {
    errorMsg.value = '檔案內容與副檔名不符，請確認上傳的是真實的圖片檔案'
    return
  }

  // 壓縮後上傳到 Supabase Storage
  const compressed = await compressImage(file)
  await uploadToSupabase(compressed)
}

// ── 壓縮圖片（Canvas resize → WebP）──
const compressImage = (file) => {
  return new Promise((resolve) => {
    const MAX_PX  = 1200   // 最長邊上限（px）
    const QUALITY = 0.82   // WebP 品質

    const img = new Image()
    const objectUrl = URL.createObjectURL(file)

    img.onload = () => {
      URL.revokeObjectURL(objectUrl)

      let { width, height } = img
      if (width > MAX_PX || height > MAX_PX) {
        if (width >= height) { height = Math.round(height * MAX_PX / width); width = MAX_PX }
        else                 { width  = Math.round(width  * MAX_PX / height); height = MAX_PX }
      }

      const canvas = document.createElement('canvas')
      canvas.width  = width
      canvas.height = height
      canvas.getContext('2d').drawImage(img, 0, 0, width, height)

      canvas.toBlob(
        (blob) => resolve(new File([blob], file.name.replace(/\.\w+$/, '.webp'), { type: 'image/webp' })),
        'image/webp',
        QUALITY
      )
    }

    img.src = objectUrl
  })
}

const uploadToSupabase = async (file) => {
  uploading.value = true

  const supabaseUrl = import.meta.env.VITE_SUPABASE_URL
  const supabaseKey = import.meta.env.VITE_SUPABASE_ANON_KEY

  if (!supabaseUrl || !supabaseKey) {
    errorMsg.value = '環境變數未設定，請確認 .env 檔案'
    uploading.value = false
    return
  }

  try {
    // 產生唯一檔名（時間戳 + 隨機數，避免覆蓋；壓縮後統一存 webp）
    const timestamp = Date.now()
    const random = Math.random().toString(36).slice(2, 8)
    const fileName = `cover_${timestamp}_${random}.webp`
    const filePath = `activity-covers/${fileName}`

    // 上傳到 Supabase Storage
    const uploadRes = await fetch(
      `${supabaseUrl}/storage/v1/object/${filePath}`,
      {
        method: 'POST',
        headers: {
          'apikey': supabaseKey,
          'Authorization': `Bearer ${supabaseKey}`,
          'Content-Type': file.type,
          'x-upsert': 'false',
        },
        body: file,
      }
    )

    if (!uploadRes.ok) {
      const err = await uploadRes.json()
      throw new Error(err.message || '上傳失敗')
    }

    // 取得公開 URL
    const publicUrl = `${supabaseUrl}/storage/v1/object/public/${filePath}`

    previewUrl.value = publicUrl
    emit('update:modelValue', publicUrl)

  } catch (e) {
    errorMsg.value = `上傳失敗：${e.message}`
    error.value = true
  } finally {
    uploading.value = false
  }
}

const clearImage = () => {
  previewUrl.value = ''
  errorMsg.value = ''
  error.value = false
  emit('update:modelValue', '')
}
</script>

<style scoped>
.uploader { display: flex; flex-direction: column; gap: 0.5rem; }

.upload-area {
  position: relative;
  border: 1.5px dashed #e0e0e0;
  height: 180px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; overflow: hidden;
  transition: border-color 0.2s, background 0.2s;
  background: #fafafa;
}
.upload-area:hover { border-color: #0a0a0a; background: #f5f5f5; }
.upload-area.is-dragging { border-color: #ff2d6b; background: #fff5f7; }
.upload-area.has-error { border-color: #dc2626; background: #fff1f2; }
.upload-area.has-image { border-style: solid; border-color: #e0e0e0; }

.preview-img {
  width: 100%; height: 100%; object-fit: cover; display: block;
}

.upload-hint { text-align: center; }
.upload-icon {
  font-family: 'Space Mono', monospace;
  font-size: 1.5rem; color: #ccc; margin-bottom: 0.5rem; line-height: 1;
}
.hint-main { font-size: 0.9rem; letter-spacing: 0.1em; color: #888; margin: 0 0 0.25rem; }
.hint-sub  { font-size: 0.8rem;  letter-spacing: 0.08em; color: #bbb; margin: 0; }

/* Replace hint on hover */
.replace-hint {
  position: absolute; inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.2s;
}
.upload-area:hover .replace-hint { opacity: 1; }
.replace-hint span { font-size: 0.72rem; letter-spacing: 0.12em; color: #fff; }

/* Uploading overlay */
.uploading-overlay {
  position: absolute; inset: 0;
  background: rgba(255,255,255,0.9);
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 0.75rem;
}
.uploading-bars { display: flex; gap: 4px; }
.uploading-bars span {
  display: block; width: 3px; height: 20px;
  background: #ff2d6b; border-radius: 2px;
  animation: barPulse 0.7s ease-in-out infinite alternate;
}
@keyframes barPulse {
  from { transform: scaleY(0.3); opacity: 0.3; }
  to   { transform: scaleY(1); opacity: 1; }
}
.uploading-overlay p { font-size: 0.65rem; letter-spacing: 0.15em; color: #888; margin: 0; }

.error-msg { font-size: 0.65rem; letter-spacing: 0.06em; color: #dc2626; margin: 0; }

.actions { display: flex; align-items: center; justify-content: space-between; }
.success-hint { font-size: 0.65rem; letter-spacing: 0.08em; color: #16a34a; margin: 0; }
.clear-btn {
  font-size: 0.62rem; letter-spacing: 0.08em;
  background: none; border: none; color: #aaa; cursor: pointer; padding: 0;
  transition: color 0.15s;
}
.clear-btn:hover { color: #dc2626; }
</style>