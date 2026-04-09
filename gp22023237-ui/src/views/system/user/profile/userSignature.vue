<template>
  <div class="signature-container">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>当前签名</span>
            </div>
          </template>
          <div class="signature-preview">
            <div v-if="imageLoading" class="signature-loading">
              <el-icon class="is-loading" :size="40"><Loading /></el-icon>
              <p>加载中...</p>
            </div>
            <img 
              v-else-if="currentSignature" 
              :src="currentSignature" 
              class="signature-image"
              @load="handleImageLoad"
              @error="handleImageError"
            />
            <div v-else class="no-signature">
              <el-icon :size="60"><Picture /></el-icon>
              <p>暂无签名</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>创建签名</span>
            </div>
          </template>
          <div class="create-signature-buttons">
            <el-button type="primary" size="large" @click="openHandwriteDialog">
              <el-icon><Edit /></el-icon>
              手写签名
            </el-button>
            <el-button type="success" size="large" @click="openUploadDialog">
              <el-icon><Upload /></el-icon>
              上传签名
            </el-button>
          </div>
          <div class="signature-tips">
            <p>您可以选择手写签名或上传签名图片</p>
            <p>支持 JPG、PNG 格式，文件大小不超过 2MB</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="handwriteDialogVisible"
      title="手写签名"
      width="600px"
      :close-on-click-modal="false"
      @opened="handleHandwriteDialogOpened"
    >
      <div class="handwrite-dialog-content">
        <div class="canvas-wrapper">
          <canvas
            ref="canvasRef"
            class="signature-canvas"
            @mousedown="startDrawing"
            @mousemove="draw"
            @mouseup="stopDrawing"
            @mouseleave="stopDrawing"
            @touchstart.prevent="startDrawingTouch"
            @touchmove.prevent="drawTouch"
            @touchend="stopDrawing"
          ></canvas>
        </div>
        <div class="canvas-tools">
          <el-button @click="clearCanvas">清除</el-button>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handwriteDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveHandwrite" :disabled="!hasDrawn">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="uploadDialogVisible"
      title="上传签名"
      width="500px"
      :close-on-click-modal="false"
      @closed="handleUploadDialogClosed"
    >
      <div class="upload-dialog-content">
        <el-upload
          class="signature-uploader"
          :action="uploadAction"
          :show-file-list="false"
          :before-upload="beforeUpload"
          :http-request="customUpload"
          accept="image/*"
        >
          <div v-if="uploadPreview" class="upload-preview">
            <img :src="uploadPreview" class="preview-image" />
          </div>
          <div v-else class="upload-placeholder">
            <el-icon class="upload-icon"><Plus /></el-icon>
            <div class="upload-text">点击上传签名图片</div>
          </div>
        </el-upload>
        <div class="upload-tip">
          <p>支持 JPG、PNG 格式，文件大小不超过 2MB</p>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeUploadDialog">取消</el-button>
          <el-button type="primary" @click="saveUpload" :disabled="!uploadFile">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch, getCurrentInstance } from 'vue'
import { Picture, Plus, Edit, Upload, Loading } from '@element-plus/icons-vue'
import { uploadSignatureBase64, uploadSignatureFile } from '@/api/system/user'
import { ElMessage } from 'element-plus'

const props = defineProps({
  user: {
    type: Object
  }
})

const { proxy } = getCurrentInstance()

const currentSignature = ref('')
const handwriteDialogVisible = ref(false)
const uploadDialogVisible = ref(false)
const canvasRef = ref(null)
const isDrawing = ref(false)
const hasDrawn = ref(false)
const uploadPreview = ref('')
const uploadFile = ref(null)
const uploadAction = ref('')
const imageLoading = ref(false)

let ctx = null

onMounted(() => {
  if (props.user && props.user.signature) {
    imageLoading.value = true
    const signatureUrl = props.user.signature
    const timestamp = Date.now()
    currentSignature.value = signatureUrl.includes('?') 
      ? `${signatureUrl}&t=${timestamp}` 
      : `${signatureUrl}?t=${timestamp}`
  }
})

watch(() => props.user, (newUser) => {
  if (newUser && newUser.signature) {
    imageLoading.value = true
    const signatureUrl = newUser.signature
    const timestamp = Date.now()
    currentSignature.value = signatureUrl.includes('?') 
      ? `${signatureUrl}&t=${timestamp}` 
      : `${signatureUrl}?t=${timestamp}`
  }
}, { immediate: true })

function openHandwriteDialog() {
  handwriteDialogVisible.value = true
}

function handleHandwriteDialogOpened() {
  nextTick(() => {
    initCanvas()
  })
}

function openUploadDialog() {
  uploadDialogVisible.value = true
  clearUpload()
}

function initCanvas() {
  const canvas = canvasRef.value
  if (!canvas) return

  ctx = canvas.getContext('2d')
  canvas.width = 500
  canvas.height = 250

  ctx.fillStyle = '#fff'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  ctx.strokeStyle = '#000'
  ctx.lineWidth = 2
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'
}

function getMousePos(e) {
  const canvas = canvasRef.value
  const rect = canvas.getBoundingClientRect()
  return {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top
  }
}

function getTouchPos(e) {
  const canvas = canvasRef.value
  const rect = canvas.getBoundingClientRect()
  const touch = e.touches[0]
  return {
    x: touch.clientX - rect.left,
    y: touch.clientY - rect.top
  }
}

function startDrawing(e) {
  isDrawing.value = true
  const pos = getMousePos(e)
  ctx.beginPath()
  ctx.moveTo(pos.x, pos.y)
}

function draw(e) {
  if (!isDrawing.value) return
  hasDrawn.value = true
  const pos = getMousePos(e)
  ctx.lineTo(pos.x, pos.y)
  ctx.stroke()
}

function stopDrawing() {
  isDrawing.value = false
}

function startDrawingTouch(e) {
  isDrawing.value = true
  const pos = getTouchPos(e)
  ctx.beginPath()
  ctx.moveTo(pos.x, pos.y)
}

function drawTouch(e) {
  if (!isDrawing.value) return
  hasDrawn.value = true
  const pos = getTouchPos(e)
  ctx.lineTo(pos.x, pos.y)
  ctx.stroke()
}

function clearCanvas() {
  const canvas = canvasRef.value
  if (!canvas || !ctx) return
  ctx.fillStyle = '#fff'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  hasDrawn.value = false
}

async function saveHandwrite() {
  if (!hasDrawn.value) {
    ElMessage.warning('请先手写签名')
    return
  }

  try {
    const canvas = canvasRef.value
    const base64Data = canvas.toDataURL('image/png')

    const response = await uploadSignatureBase64({ signature: base64Data })
    
    if (response.code === 200) {
      imageLoading.value = true
      const signatureUrl = response.data.signatureUrl
      const timestamp = Date.now()
      currentSignature.value = signatureUrl.includes('?') 
        ? `${signatureUrl}&t=${timestamp}` 
        : `${signatureUrl}?t=${timestamp}`
      ElMessage.success('手写签名保存成功')
      handwriteDialogVisible.value = false
      clearCanvas()
    } else {
      ElMessage.error(response.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存手写签名失败:', error)
    ElMessage.error('保存失败，请重试')
  }
}

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }

  uploadFile.value = file

  const reader = new FileReader()
  reader.onload = (e) => {
    uploadPreview.value = e.target.result
  }
  reader.readAsDataURL(file)

  return false
}

function customUpload() {
}

async function saveUpload() {
  if (!uploadFile.value) {
    ElMessage.warning('请先选择要上传的签名图片')
    return
  }

  try {
    const formData = new FormData()
    formData.append('file', uploadFile.value)

    const response = await uploadSignatureFile(formData)
    
    if (response.code === 200) {
      imageLoading.value = true
      const signatureUrl = response.data.signatureUrl
      const timestamp = Date.now()
      currentSignature.value = signatureUrl.includes('?') 
        ? `${signatureUrl}&t=${timestamp}` 
        : `${signatureUrl}?t=${timestamp}`
      ElMessage.success('签名上传成功')
      uploadDialogVisible.value = false
      clearUpload()
    } else {
      ElMessage.error(response.msg || '上传失败')
    }
  } catch (error) {
    console.error('上传签名失败:', error)
    ElMessage.error('上传失败，请重试')
  }
}

function clearUpload() {
  uploadPreview.value = ''
  uploadFile.value = null
}

function closeUploadDialog() {
  uploadDialogVisible.value = false
  clearUpload()
}

function handleUploadDialogClosed() {
  clearUpload()
}

function handleImageLoad() {
  imageLoading.value = false
}

function handleImageError() {
  imageLoading.value = false
  ElMessage.error('签名图片加载失败')
}
</script>

<style lang="scss" scoped>
.signature-container {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.signature-preview {
  width: 100%;
  height: 200px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
  margin-bottom: 20px;

  .signature-image {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
  }

  .signature-loading {
    text-align: center;
    color: #409eff;

    .el-icon {
      margin-bottom: 10px;
    }

    p {
      margin: 0;
      font-size: 14px;
    }
  }

  .no-signature {
    text-align: center;
    color: #909399;

    p {
      margin-top: 10px;
      font-size: 14px;
    }
  }
}

.signature-actions {
  text-align: center;
}

.create-signature-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-bottom: 20px;

  .el-button {
    flex: 1;
    height: 80px;
    font-size: 16px;
    
    .el-icon {
      margin-right: 8px;
      font-size: 20px;
    }
  }
}

.signature-tips {
  text-align: center;
  color: #909399;
  font-size: 14px;
  
  p {
    margin: 5px 0;
  }
}

.handwrite-dialog-content {
  .canvas-wrapper {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
  }

  .signature-canvas {
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    background-color: #fff;
    cursor: crosshair;
    touch-action: none;
  }

  .canvas-tools {
    display: flex;
    justify-content: center;
    gap: 10px;
  }
}

.upload-dialog-content {
  .signature-uploader {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      width: 100%;
      height: 300px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: border-color 0.3s;

      &:hover {
        border-color: #409eff;
      }
    }

    .upload-placeholder {
      text-align: center;

      .upload-icon {
        font-size: 60px;
        color: #8c939d;
      }

      .upload-text {
        margin-top: 10px;
        color: #606266;
        font-size: 14px;
      }
    }

    .upload-preview {
      width: 100%;
      height: 100%;

      .preview-image {
        width: 100%;
        height: 100%;
        object-fit: contain;
      }
    }
  }

  .upload-tip {
    text-align: center;
    margin-top: 10px;
    color: #909399;
    font-size: 12px;
  }
}
</style>
