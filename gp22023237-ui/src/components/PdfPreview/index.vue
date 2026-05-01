<template>
  <!-- 弹窗模式 -->
  <template v-if="mode === 'dialog'">
    <el-link type="primary" @click="openPreview" :underline="false">
      <el-icon><View /></el-icon>
      {{ buttonText || '预览' }}
    </el-link>
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="80%"
      top="5vh"
      destroy-on-close
      append-to-body
      class="pdf-preview-dialog"
    >
      <div class="pdf-preview-container" v-loading="loading">
        <div v-if="error" class="pdf-error">
          <el-icon :size="48" color="#909399"><Document /></el-icon>
          <p>PDF预览加载失败</p>
          <p style="font-size: 12px; color: #999;">{{ error }}</p>
          <el-link type="primary" :href="url" target="_blank">下载文件</el-link>
        </div>
        <template v-else>
          <div class="pdf-toolbar">
            <el-button :disabled="currentPage <= 1" @click="currentPage--" size="small">
              <el-icon><ArrowLeft /></el-icon>
            </el-button>
            <span>{{ currentPage }} / {{ totalPages }}</span>
            <el-button :disabled="currentPage >= totalPages" @click="currentPage++" size="small">
              <el-icon><ArrowRight /></el-icon>
            </el-button>
            <el-button @click="zoomOut" size="small" :disabled="scale <= 0.5">
              <el-icon><ZoomOut /></el-icon>
            </el-button>
            <span>{{ Math.round(scale * 100) }}%</span>
            <el-button @click="zoomIn" size="small" :disabled="scale >= 3">
              <el-icon><ZoomIn /></el-icon>
            </el-button>
            <el-button @click="downloadPdf" size="small" type="primary" link>
              <el-icon><Download /></el-icon> 下载
            </el-button>
          </div>
          <div class="pdf-canvas-wrapper" ref="canvasWrapperRef">
            <canvas ref="canvasRef"></canvas>
          </div>
        </template>
      </div>
    </el-dialog>
  </template>

  <!-- 嵌入模式 -->
  <template v-else>
    <div class="pdf-embed-container" v-loading="loading">
      <div v-if="error" class="pdf-error">
        <el-icon :size="48" color="#909399"><Document /></el-icon>
        <p>PDF预览加载失败</p>
        <el-link type="primary" :href="url" target="_blank">下载文件</el-link>
      </div>
      <template v-else>
        <div class="pdf-toolbar">
          <el-button :disabled="currentPage <= 1" @click="currentPage--" size="small">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <span>{{ currentPage }} / {{ totalPages }}</span>
          <el-button :disabled="currentPage >= totalPages" @click="currentPage++" size="small">
            <el-icon><ArrowRight /></el-icon>
          </el-button>
          <el-button @click="zoomOut" size="small" :disabled="scale <= 0.5">
            <el-icon><ZoomOut /></el-icon>
          </el-button>
          <span>{{ Math.round(scale * 100) }}%</span>
          <el-button @click="zoomIn" size="small" :disabled="scale >= 3">
            <el-icon><ZoomIn /></el-icon>
          </el-button>
        </div>
        <div class="pdf-canvas-wrapper" ref="embedCanvasWrapperRef">
          <canvas ref="embedCanvasRef"></canvas>
        </div>
      </template>
    </div>
  </template>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { getSignedUrl } from '@/api/academic'
import { View, ArrowLeft, ArrowRight, ZoomIn, ZoomOut, Download, Document } from '@element-plus/icons-vue'

const props = defineProps({
  url: { type: String, required: true },
  mode: { type: String, default: 'dialog' }, // 'dialog' | 'embed'
  buttonText: { type: String, default: '' },
  dialogTitle: { type: String, default: 'PDF预览' }
})

const dialogVisible = ref(false)
const loading = ref(false)
const error = ref('')
const currentPage = ref(1)
const totalPages = ref(0)
const scale = ref(1.5)
const signedUrl = ref('')

// canvas refs
const canvasRef = ref(null)
const embedCanvasRef = ref(null)
const canvasWrapperRef = ref(null)
const embedCanvasWrapperRef = ref(null)

let pdfDoc = null

async function getSignedUrlForPdf() {
  if (!props.url) return null
  // 如果url已经是http开头且包含签名参数，直接使用
  if (props.url.includes('?sign=') || props.url.includes('&sign=')) {
    return props.url
  }
  try {
    const res = await getSignedUrl(props.url)
    if (res.data && res.data.url) {
      return res.data.url
    }
    return props.url
  } catch (e) {
    console.warn('获取签名URL失败，使用原始URL', e)
    return props.url
  }
}

async function loadPdf() {
  if (!props.url) return

  loading.value = true
  error.value = ''

  try {
    // 动态导入pdfjs-dist
    const pdfjsLib = await import('pdfjs-dist')
    pdfjsLib.GlobalWorkerOptions.workerSrc = new URL(
      'pdfjs-dist/build/pdf.worker.mjs',
      import.meta.url
    ).toString()

    signedUrl.value = await getSignedUrlForPdf()

    const loadingTask = pdfjsLib.getDocument(signedUrl.value)
    pdfDoc = await loadingTask.promise
    totalPages.value = pdfDoc.numPages
    currentPage.value = 1

    await renderPage(currentPage.value)
  } catch (e) {
    console.warn('PDF加载失败:', e)
    error.value = e.message || '无法加载PDF文件'
  } finally {
    loading.value = false
  }
}

async function renderPage(pageNum) {
  if (!pdfDoc) return

  const canvas = props.mode === 'dialog' ? canvasRef.value : embedCanvasRef.value
  if (!canvas) return

  try {
    const page = await pdfDoc.getPage(pageNum)
    const viewport = page.getViewport({ scale: scale.value })
    const context = canvas.getContext('2d')

    canvas.height = viewport.height
    canvas.width = viewport.width

    await page.render({
      canvasContext: context,
      viewport: viewport
    }).promise
  } catch (e) {
    console.warn('PDF渲染失败:', e)
    error.value = '页面渲染失败'
  }
}

function openPreview() {
  dialogVisible.value = true
  nextTick(() => {
    loadPdf()
  })
}

function zoomIn() {
  scale.value = Math.min(scale.value + 0.25, 3)
  renderPage(currentPage.value)
}

function zoomOut() {
  scale.value = Math.max(scale.value - 0.25, 0.5)
  renderPage(currentPage.value)
}

function downloadPdf() {
  const link = document.createElement('a')
  link.href = signedUrl.value || props.url
  link.target = '_blank'
  link.download = ''
  link.click()
}

// 监听页码变化
watch(currentPage, (val) => {
  if (pdfDoc) renderPage(val)
})

// 嵌入模式下自动加载
if (props.mode === 'embed' && props.url) {
  nextTick(() => loadPdf())
}
</script>

<style scoped>
.pdf-preview-dialog :deep(.el-dialog__body) {
  padding: 0;
  max-height: 80vh;
  overflow: auto;
}

.pdf-preview-container,
.pdf-embed-container {
  min-height: 400px;
}

.pdf-toolbar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  border-bottom: 1px solid #ebeef5;
  background: #f5f7fa;
  position: sticky;
  top: 0;
  z-index: 1;
}

.pdf-toolbar span {
  font-size: 14px;
  color: #606266;
  min-width: 60px;
  text-align: center;
}

.pdf-canvas-wrapper {
  display: flex;
  justify-content: center;
  padding: 16px;
  overflow: auto;
}

.pdf-canvas-wrapper canvas {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.pdf-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
}

.pdf-error p {
  margin: 8px 0;
}
</style>
