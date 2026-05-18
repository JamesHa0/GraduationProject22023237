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
        <el-form ref="formRef" :model="form" label-width="100px">
          <el-form-item label="论文文件">
            <el-input v-model="form.thesisVersionUrl" placeholder="请输入论文文件地址" />
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
import { getThesisMainByStudent, listProcess, submitProcess } from '@/api/degree'
import { buildSubmitData } from '@/views/degree/processConfig'
import { getProcessStatusText, getProcessStatusType, parseDate } from '@/composables/useDegreeStatus'
import { onRoleInfoReady, getStudentId } from '@/composables/useRoleInfoReady'

const { proxy } = getCurrentInstance()

const dialogVisible = ref(false)
const formRef = ref(null)
const currentRecord = ref(null)
const thesisId = ref(null)

const form = ref({ thesisVersionUrl: undefined })
const statusText = computed(() => getProcessStatusText(currentRecord.value?.processStatus))
const statusType = computed(() => getProcessStatusType(currentRecord.value?.processStatus))
const isLocked = computed(() => currentRecord.value?.processStatus === 3 || currentRecord.value?.processStatus === 5)
const canSubmit = computed(() => !currentRecord.value || currentRecord.value.processStatus === 4)

function handleSubmit() {
  form.value = { thesisVersionUrl: undefined }
  dialogVisible.value = true
}

function doSubmit() {
  const data = buildSubmitData({ ...form.value, thesisId: thesisId.value }, 7)
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
