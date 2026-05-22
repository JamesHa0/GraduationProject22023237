<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>过程稿</span>
          <el-button v-if="canSubmit" type="primary" size="small" @click="handleSubmit">提交修改稿</el-button>
        </div>
      </template>

      <!-- 版本时间线 -->
      <div v-if="records.length > 0" style="display: flex; gap: 20px;">
        <div style="width: 220px;">
          <el-timeline>
            <el-timeline-item
              v-for="r in records" :key="r.id"
              :timestamp="parseDate(r.submitTime)"
              :type="getTimelineType([r])"
              @click="selectRecord(r)"
              style="cursor: pointer;"
            >
              版本{{ r.version }} - {{ getProcessStatusText(r.processStatus) }}
            </el-timeline-item>
          </el-timeline>
        </div>
        <div style="flex: 1;">
          <el-descriptions v-if="selectedRecord" :column="2" border>
            <el-descriptions-item label="版本">{{ selectedRecord.version }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getProcessStatusType(selectedRecord.processStatus)">{{ getProcessStatusText(selectedRecord.processStatus) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="论文文件">{{ selectedRecord.thesisVersionUrl || '-' }}</el-descriptions-item>
            <el-descriptions-item label="修改说明">{{ parseContentExtend(selectedRecord).modificationNote || '-' }}</el-descriptions-item>
            <el-descriptions-item v-if="selectedRecord.supervisorComment" label="导师意见" :span="2">
              {{ selectedRecord.supervisorComment }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <el-empty v-else description="暂未提交过程稿" />

      <el-dialog v-model="dialogVisible" title="提交修改稿" width="600px">
        <el-form ref="formRef" :model="form" label-width="100px">
          <el-form-item label="论文文件">
            <el-input v-model="form.thesisVersionUrl" placeholder="请输入论文文件地址" />
          </el-form-item>
          <el-form-item label="修改说明">
            <el-input v-model="form.modificationNote" type="textarea" :rows="3" placeholder="请输入本次修改说明" />
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
import { getProcessStatusText, getProcessStatusType, getTimelineType, parseDate } from '@/composables/useDegreeStatus'
import { onRoleInfoReady, getStudentId } from '@/composables/useRoleInfoReady'

const { proxy } = getCurrentInstance()

const dialogVisible = ref(false)
const formRef = ref(null)
const records = ref([])
const selectedRecord = ref(null)
const thesisId = ref(null)

const config = PROCESS_CONFIG[5]
const form = ref({ ...config.defaultForm })

const canSubmit = computed(() => {
  if (records.value.length === 0) return true
  const latest = records.value.reduce((a, b) => a.version > b.version ? a : b)
  return latest.processStatus === 4
})

function selectRecord(r) { selectedRecord.value = r }

function handleSubmit() {
  form.value = { ...config.defaultForm }
  dialogVisible.value = true
}

function doSubmit() {
  const data = buildSubmitData({ ...form.value, thesisId: thesisId.value }, 5)
  submitProcess(data).then(() => {
    proxy.$modal.msgSuccess('提交成功')
    dialogVisible.value = false
    loadData()
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
      const processRes = await listProcess({ thesisId: thesis.id, processType: 5, pageSize: 50 })
      records.value = processRes.data || processRes.rows || []
      if (records.value.length > 0) {
        selectedRecord.value = records.value.reduce((a, b) => a.version > b.version ? a : b)
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
