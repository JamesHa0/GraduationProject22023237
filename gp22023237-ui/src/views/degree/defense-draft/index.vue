<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>论文答辩稿</span>
          <el-button v-if="canSubmit" type="primary" size="small" @click="handleSubmit">提交答辩稿</el-button>
        </div>
      </template>

      <div v-if="currentRecord">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="答辩稿说明">{{ parseContentExtend(currentRecord).defenseDraftDesc || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="statusType">{{ statusText }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ parseDate(currentRecord.submitTime) }}</el-descriptions-item>
          <el-descriptions-item label="论文文件">{{ currentRecord.thesisVersionUrl || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRecord.supervisorComment" label="导师评语" :span="2">
            {{ currentRecord.supervisorComment }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <el-empty v-else description="暂未提交答辩稿" />

      <el-dialog v-model="dialogVisible" title="提交答辩稿" width="600px">
        <el-form ref="formRef" :model="form" label-width="100px">
          <el-form-item label="论文文件">
            <el-input v-model="form.thesisVersionUrl" placeholder="请输入论文文件地址" />
          </el-form-item>
          <el-form-item label="答辩稿说明">
            <el-input v-model="form.defenseDraftDesc" type="textarea" :rows="3" placeholder="请输入答辩稿说明" />
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
import { ref, computed, onMounted } from 'vue'
import { getCurrentInstance } from 'vue'
import { getThesisMainByStudent, listProcess, submitProcess } from '@/api/degree'
import { PROCESS_CONFIG, buildSubmitData, parseContentExtend } from '@/views/degree/processConfig'
import { getProcessStatusText, getProcessStatusType, parseDate } from '@/composables/useDegreeStatus'

const { proxy } = getCurrentInstance()
const userStore = proxy.$pinia._s.get('user')

const dialogVisible = ref(false)
const formRef = ref(null)
const currentRecord = ref(null)
const thesisId = ref(null)

const config = PROCESS_CONFIG[6]
const form = ref({ ...config.defaultForm })

const statusText = computed(() => getProcessStatusText(currentRecord.value?.processStatus))
const statusType = computed(() => getProcessStatusType(currentRecord.value?.processStatus))
const canSubmit = computed(() => !currentRecord.value || currentRecord.value.processStatus === 4)

function handleSubmit() {
  form.value = { ...config.defaultForm }
  dialogVisible.value = true
}

function doSubmit() {
  const data = buildSubmitData({ ...form.value, thesisId: thesisId.value }, 6)
  submitProcess(data).then(() => {
    proxy.$modal.msgSuccess('提交成功')
    dialogVisible.value = false
    loadData()
  })
}

async function loadData() {
  try {
    const studentId = userStore?.id
    if (!studentId) return
    const res = await getThesisMainByStudent(studentId)
    const thesis = res.data || res
    if (thesis && thesis.id) {
      thesisId.value = thesis.id
      const processRes = await listProcess({ thesisId: thesis.id, processType: 6, pageSize: 1 })
      const records = processRes.data?.records || processRes.rows || []
      if (records.length > 0) {
        currentRecord.value = records.reduce((a, b) => a.version > b.version ? a : b)
      }
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
