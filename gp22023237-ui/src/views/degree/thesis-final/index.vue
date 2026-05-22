<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>毕业论文</span>
          <el-button v-if="canSubmit" type="primary" size="small" @click="handleSubmit">提交最终稿</el-button>
        </div>
      </template>

      <div v-if="currentRecord">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="论文文件">{{ currentRecord.thesisVersionUrl || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="statusType">{{ statusText }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ parseDate(currentRecord.submitTime) }}</el-descriptions-item>
          <el-descriptions-item label="锁定状态">
            <el-tag :type="isLocked ? 'danger' : 'info'">{{ isLocked ? '已锁定' : '未锁定' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentRecord.supervisorComment" label="导师意见" :span="2">
            {{ currentRecord.supervisorComment }}
          </el-descriptions-item>
        </el-descriptions>

        <el-alert v-if="isLocked" type="warning" :closable="false" style="margin-top: 16px;">
          论文已定稿锁定，如需修改请联系导师退回
        </el-alert>
      </div>

      <el-empty v-else description="暂未提交最终稿" />

      <el-dialog v-model="dialogVisible" title="提交最终稿" width="600px">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="论文文件" prop="thesisVersionUrl">
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :before-upload="handleBeforeUpload"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :file-list="fileList"
              :limit="1"
              :on-exceed="handleExceed"
            >
              <el-button type="primary" plain size="small">选取文件</el-button>
              <template #tip>
                <div class="el-upload__tip">支持 doc/docx/pdf 等格式，单文件不超过20MB</div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="doSubmit">提交</el-button>
        </template>
        <el-alert type="warning" :closable="false" style="margin-top: 10px;">
          提交后论文将锁定，后续修改只能由导师退回
        </el-alert>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getCurrentInstance } from 'vue'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'
import { getThesisMainByStudent, listProcess, submitProcess } from '@/api/degree'
import { buildSubmitData } from '@/views/degree/processConfig'
import { getProcessStatusText, getProcessStatusType, parseDate } from '@/composables/useDegreeStatus'
import { onRoleInfoReady, getStudentId } from '@/composables/useRoleInfoReady'

const { proxy } = getCurrentInstance()

const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/file/upload-academic'
const uploadHeaders = ref({ Token: getToken() })
const dialogVisible = ref(false)
const formRef = ref(null)
const currentRecord = ref(null)
const thesisId = ref(null)
const fileList = ref([])

const form = ref({ thesisVersionUrl: undefined })
const rules = {
  thesisVersionUrl: [{ required: true, message: '请上传论文文件', trigger: 'change' }]
}
const statusText = computed(() => getProcessStatusText(currentRecord.value?.processStatus))
const statusType = computed(() => getProcessStatusType(currentRecord.value?.processStatus))
const isLocked = computed(() => currentRecord.value?.processStatus === 3 || currentRecord.value?.processStatus === 5)
const canSubmit = computed(() => !currentRecord.value || currentRecord.value.processStatus === 4)

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

function handleUploadSuccess(response) {
  const isSuccess = response.result === 'success' || response.code === 200
  if (isSuccess && response.data && response.data.url) {
    form.value.thesisVersionUrl = response.data.url
    ElMessage.success('文件上传成功')
  } else {
    ElMessage.error(response.error || response.msg || '上传失败')
    fileList.value = []
  }
}

function handleUploadError() {
  ElMessage.error('上传失败，请重试')
  fileList.value = []
}

function handleExceed() {
  ElMessage.warning('只能上传一个文件，请先移除已有文件')
}

function handleSubmit() {
  form.value = { thesisVersionUrl: undefined }
  fileList.value = []
  dialogVisible.value = true
}

function doSubmit() {
  formRef.value.validate((valid) => {
    if (!valid) return
    const data = buildSubmitData({ ...form.value, thesisId: thesisId.value }, 7)
    submitProcess(data).then(() => {
      proxy.$modal.msgSuccess('提交成功')
      dialogVisible.value = false
      fileList.value = []
      loadData()
    })
  })
}

async function loadData() {
  try {
    const studentId = getStudentId()
    if (!studentId) return
    const res = await getThesisMainByStudent(studentId)
    const thesis = res.data || res
    if (thesis && thesis.id) {
      thesisId.value = thesis.id
      const processRes = await listProcess({ thesisId: thesis.id, processType: 7, pageSize: 1 })
      const records = processRes.data || processRes.rows || []
      if (records.length > 0) {
        currentRecord.value = records.reduce((a, b) => a.version > b.version ? a : b)
      }
    }
  } catch (e) {
    console.error(e)
  }
}

onRoleInfoReady(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
