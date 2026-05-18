<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>开题报告</span>
          <el-button v-if="canSubmit" type="primary" size="small" @click="handleSubmit">提交开题报告</el-button>
        </div>
      </template>

      <div v-if="currentRecord">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="研究背景" :span="2">{{ extendData.background || '-' }}</el-descriptions-item>
          <el-descriptions-item label="研究现状" :span="2">{{ extendData.researchStatus || '-' }}</el-descriptions-item>
          <el-descriptions-item label="研究内容" :span="2">{{ extendData.researchContent || '-' }}</el-descriptions-item>
          <el-descriptions-item label="研究方法" :span="2">{{ extendData.researchMethod || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="statusType">{{ statusText }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ parseDate(currentRecord.submitTime) }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRecord.supervisorComment" label="导师意见" :span="2">
            {{ currentRecord.supervisorComment }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <el-empty v-else description="暂未提交开题报告" />

      <el-dialog v-model="dialogVisible" title="提交开题报告" width="700px">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="研究背景" prop="background">
            <el-input v-model="form.background" type="textarea" :rows="4" placeholder="请输入研究背景" />
          </el-form-item>
          <el-form-item label="研究现状" prop="researchStatus">
            <el-input v-model="form.researchStatus" type="textarea" :rows="4" placeholder="请输入研究现状" />
          </el-form-item>
          <el-form-item label="研究内容" prop="researchContent">
            <el-input v-model="form.researchContent" type="textarea" :rows="4" placeholder="请输入研究内容" />
          </el-form-item>
          <el-form-item label="研究方法" prop="researchMethod">
            <el-input v-model="form.researchMethod" type="textarea" :rows="3" placeholder="请输入研究方法" />
          </el-form-item>
          <el-form-item label="附件">
            <el-input v-model="form.attachmentUrl" placeholder="附件地址" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="doSubmit">提交</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getCurrentInstance } from 'vue'
import { getThesisMainByStudent, listProcess, submitProcess } from '@/api/degree'
import { PROCESS_CONFIG, buildSubmitData, parseContentExtend } from '@/views/degree/processConfig'
import { getProcessStatusText, getProcessStatusType, parseDate } from '@/composables/useDegreeStatus'
import { onRoleInfoReady, getStudentId } from '@/composables/useRoleInfoReady'

const { proxy } = getCurrentInstance()

const dialogVisible = ref(false)
const formRef = ref(null)
const currentRecord = ref(null)
const thesisId = ref(null)

const config = PROCESS_CONFIG[3]
const form = ref({ ...config.defaultForm })
const rules = config.rules

const extendData = computed(() => parseContentExtend(currentRecord.value))
const statusText = computed(() => getProcessStatusText(currentRecord.value?.processStatus))
const statusType = computed(() => getProcessStatusType(currentRecord.value?.processStatus))
const canSubmit = computed(() => !currentRecord.value || currentRecord.value.processStatus === 4)

function handleSubmit() {
  form.value = { ...config.defaultForm }
  dialogVisible.value = true
}

function doSubmit() {
  formRef.value?.validate(valid => {
    if (!valid) return
    const data = buildSubmitData({ ...form.value, thesisId: thesisId.value }, 3)
    submitProcess(data).then(() => {
      proxy.$modal.msgSuccess('提交成功')
      dialogVisible.value = false
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
      const processRes = await listProcess({ thesisId: thesis.id, processType: 3, pageSize: 1 })
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
