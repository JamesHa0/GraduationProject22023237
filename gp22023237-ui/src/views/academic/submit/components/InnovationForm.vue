<template>
  <el-row>
    <el-col :span="12">
      <el-form-item label="项目类型" prop="projectType">
        <el-radio-group v-model="form.projectType">
          <el-radio :value="1">创新项目</el-radio>
          <el-radio :value="2">创业项目</el-radio>
          <el-radio :value="3">竞赛</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="项目级别" prop="projectLevel">
        <el-select v-model="form.projectLevel" placeholder="请选择">
          <el-option label="国家级" :value="1" />
          <el-option label="省级" :value="2" />
          <el-option label="市级" :value="3" />
          <el-option label="校级" :value="4" />
        </el-select>
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="12">
      <el-form-item label="项目名称" prop="projectName">
        <el-input v-model="form.projectName" placeholder="请输入项目名称" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="项目编号" prop="projectNo">
        <el-input v-model="form.projectNo" placeholder="请输入项目编号" />
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="12">
      <el-form-item label="负责人" prop="leader">
        <el-input v-model="form.leader" placeholder="请输入负责人" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="参与成员" prop="members">
        <el-input v-model="form.members" placeholder="请输入参与成员" />
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="12">
      <el-form-item label="指导老师" prop="advisor">
        <el-input v-model="form.advisor" placeholder="请输入指导老师" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="获奖等级" prop="awardLevel">
        <el-select v-model="form.awardLevel" placeholder="请选择获奖等级" clearable>
          <el-option label="特等奖" :value="1" />
          <el-option label="一等奖" :value="2" />
          <el-option label="二等奖" :value="3" />
          <el-option label="三等奖" :value="4" />
          <el-option label="优秀奖" :value="5" />
        </el-select>
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="12">
      <el-form-item label="开始时间" prop="startDate">
        <el-date-picker v-model="form.startDate" type="date" placeholder="选择开始时间" style="width: 100%" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="结束时间" prop="endDate">
        <el-date-picker v-model="form.endDate" type="date" placeholder="选择结束时间" style="width: 100%" />
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <el-form-item label="项目描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入项目描述" />
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <el-form-item label="项目成果" prop="achievements">
        <el-input v-model="form.achievements" type="textarea" :rows="3" placeholder="请输入项目成果/获奖情况" />
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="12">
      <el-form-item label="资助金额" prop="fundingAmount">
        <el-input-number v-model="form.fundingAmount" :min="0" :precision="2" :step="100" placeholder="请输入资助金额" style="width: 100%" />
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <el-form-item label="附件上传">
        <el-upload
          :action="uploadUrl"
          :headers="uploadHeaders"
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
  return true
}

function handleUploadProgress(event, file) {}

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
