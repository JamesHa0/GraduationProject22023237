<template>
  <el-row>
    <el-col :span="24">
      <el-form-item label="成果类型" prop="achievementType">
        <el-radio-group v-model="form.achievementType">
          <el-radio :value="1">论文</el-radio>
          <el-radio :value="2">专利</el-radio>
          <el-radio :value="3">科研奖励</el-radio>
          <el-radio :value="4">项目参与</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <el-form-item label="成果标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入成果标题" />
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="12">
      <el-form-item label="作者" prop="authors">
        <el-input v-model="form.authors" placeholder="请输入作者" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="发表/授权时间" prop="publicationDate">
        <el-date-picker v-model="form.publicationDate" type="date" placeholder="选择时间" style="width: 100%" />
      </el-form-item>
    </el-col>
  </el-row>
  <!-- 论文类型字段 -->
  <template v-if="form.achievementType === 1">
    <el-row>
      <el-col :span="12">
        <el-form-item label="期刊名称" prop="journalName">
          <el-input v-model="form.journalName" placeholder="请输入期刊名称" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="期刊级别" prop="journalLevel">
          <el-select v-model="form.journalLevel" placeholder="请选择期刊级别" clearable>
            <el-option label="SCI/EI" :value="1" />
            <el-option label="核心期刊" :value="2" />
            <el-option label="普通期刊" :value="3" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="8">
        <el-form-item label="卷号" prop="volume">
          <el-input v-model="form.volume" placeholder="卷号" />
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="期号" prop="issue">
          <el-input v-model="form.issue" placeholder="期号" />
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="页码" prop="pages">
          <el-input v-model="form.pages" placeholder="页码" />
        </el-form-item>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="12">
        <el-form-item label="DOI" prop="doi">
          <el-input v-model="form.doi" placeholder="请输入DOI" />
        </el-form-item>
      </el-col>
    </el-row>
  </template>
  <!-- 专利类型字段 -->
  <template v-if="form.achievementType === 2">
    <el-row>
      <el-col :span="12">
        <el-form-item label="专利号" prop="patentNo">
          <el-input v-model="form.patentNo" placeholder="请输入专利号" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="专利类型" prop="patentType">
          <el-select v-model="form.patentType" placeholder="请选择专利类型" clearable>
            <el-option label="发明专利" :value="1" />
            <el-option label="实用新型" :value="2" />
            <el-option label="外观设计" :value="3" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="12">
        <el-form-item label="授权状态" prop="patentStatus">
          <el-select v-model="form.patentStatus" placeholder="请选择授权状态" clearable>
            <el-option label="申请中" :value="0" />
            <el-option label="已授权" :value="1" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
  </template>
  <!-- 科研奖励类型字段 -->
  <template v-if="form.achievementType === 3">
    <el-row>
      <el-col :span="12">
        <el-form-item label="奖励名称" prop="awardName">
          <el-input v-model="form.awardName" placeholder="请输入奖励名称" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="奖励级别" prop="awardLevel">
          <el-select v-model="form.awardLevel" placeholder="请选择奖励级别" clearable>
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
        <el-form-item label="发奖单位" prop="awardIssuer">
          <el-input v-model="form.awardIssuer" placeholder="请输入发奖单位" />
        </el-form-item>
      </el-col>
    </el-row>
  </template>
  <!-- 项目参与类型字段 -->
  <template v-if="form.achievementType === 4">
    <el-row>
      <el-col :span="12">
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="form.projectName" placeholder="请输入项目名称" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="项目角色" prop="projectRole">
          <el-select v-model="form.projectRole" placeholder="请选择项目角色" clearable>
            <el-option label="负责人" :value="1" />
            <el-option label="核心成员" :value="2" />
            <el-option label="参与者" :value="3" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
  </template>
  <el-row>
    <el-col :span="24">
      <el-form-item label="摘要/描述" prop="abstractContent">
        <el-input v-model="form.abstractContent" type="textarea" :rows="4" placeholder="请输入摘要或描述" />
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
