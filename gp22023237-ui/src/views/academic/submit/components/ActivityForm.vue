<template>
  <el-row>
    <el-col :span="12">
      <el-form-item label="活动类型" prop="activityType">
        <el-radio-group v-model="form.activityType">
          <el-radio :value="1">学术讲座</el-radio>
          <el-radio :value="2">研讨会</el-radio>
          <el-radio :value="3">论坛</el-radio>
          <el-radio :value="4">其他</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <el-form-item label="活动名称" prop="activityName">
        <el-input v-model="form.activityName" placeholder="请输入活动名称" />
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="12">
      <el-form-item label="主讲人" prop="speaker">
        <el-input v-model="form.speaker" placeholder="请输入主讲人" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="活动时间" prop="activityTime">
        <el-date-picker v-model="form.activityTime" type="datetime" placeholder="选择时间" style="width: 100%" />
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <el-form-item label="活动地点" prop="location">
        <el-input v-model="form.location" placeholder="请输入活动地点" />
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <el-form-item label="活动内容" prop="content">
        <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入活动内容" />
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <el-form-item label="附件上传">
        <el-upload
          :action="uploadUrl"
          :headers="uploadHeaders"
          :data="uploadData"
          :before-upload="handleBeforeUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :on-progress="handleUploadProgress"
          :on-remove="handleRemove"
          :file-list="fileList"
          :limit="3"
          :on-exceed="handleExceed"
        >
          <el-button type="primary" plain size="small">选取文件</el-button>
          <template #tip>
            <div class="el-upload__tip">支持 doc/docx/xls/xlsx/ppt/pptx/pdf/txt/zip/rar/7z/jpg/jpeg/png/gif，单文件不超过20MB，最多3个</div>
          </template>
        </el-upload>
      </el-form-item>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'

const props = defineProps({
  form: { type: Object, required: true }
})

const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/file/upload-academic'
const uploadHeaders = ref({ Token: getToken() })
const uploadData = ref({})
const fileList = ref([])

// 初始化：如果已有附件路径，回显到文件列表
watch(() => props.form.attachmentPath, (val) => {
  if (val && fileList.value.length === 0) {
    const items = val.split(',').filter(u => u.trim())
    fileList.value = items.map((item, index) => {
      const pipeIdx = item.indexOf('|')
      const url = pipeIdx > -1 ? item.substring(0, pipeIdx) : item
      const name = pipeIdx > -1 ? item.substring(pipeIdx + 1) : (url.substring(url.lastIndexOf('/') + 1) || `附件${index + 1}`)
      return { name, url, status: 'success' }
    })
  }
}, { immediate: true })

function handleBeforeUpload(file) {
  const allowedExt = ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'pdf', 'txt', 'zip', 'rar', '7z', 'jpg', 'jpeg', 'png', 'gif']
  const ext = file.name.split('.').pop().toLowerCase()
  if (!allowedExt.includes(ext)) {
    ElMessage.error('不支持的文件类型')
    return false
  }
  if (file.size > 20 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过20MB')
    return false
  }
  // 通过额外参数传递原始文件名，避免multipart中文文件名乱码
  uploadData.value = { originalFileName: file.name }
  return true
}

function handleUploadProgress(event, file) {
  // 上传进度由 el-upload 内部处理
}

function handleUploadSuccess(response, file) {
  // 后端 JSONReturn 返回格式: { result: "success", data: {...}, error: null }
  const isSuccess = response.result === 'success' || response.code === 200
  if (isSuccess) {
    ElMessage.success('上传成功')
    const url = response.data.url
    // 使用浏览器端file.name作为原文件名，不依赖后端返回的fileName（避免multipart中文乱码）
    const fileName = file.name || ''
    // 拼接到 attachmentPath，格式: url|原文件名
    const currentPaths = props.form.attachmentPath ? props.form.attachmentPath.split(',').filter(u => u.trim()) : []
    currentPaths.push(fileName ? (url + '|' + fileName) : url)
    props.form.attachmentPath = currentPaths.join(',')
  } else {
    ElMessage.error(response.error || response.msg || '上传失败')
    fileList.value = fileList.value.filter(f => f.uid !== file.uid)
  }
}

function handleUploadError() {
  ElMessage.error('上传失败，请重试')
}

function handleRemove(file) {
  // 从 attachmentPath 中移除该文件URL（匹配url部分）
  const currentPaths = props.form.attachmentPath ? props.form.attachmentPath.split(',').filter(u => u.trim()) : []
  const urlToRemove = file.response?.data?.url || file.url
  const updatedPaths = currentPaths.filter(item => {
    const pipeIdx = item.indexOf('|')
    const itemUrl = pipeIdx > -1 ? item.substring(0, pipeIdx) : item
    return itemUrl !== urlToRemove
  })
  props.form.attachmentPath = updatedPaths.length > 0 ? updatedPaths.join(',') : undefined
}

function handleExceed() {
  ElMessage.warning('最多上传3个附件')
}
</script>
