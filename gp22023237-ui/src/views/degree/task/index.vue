<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <span>任务书</span>
      </template>

      <div v-if="currentRecord">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="课题背景">{{ extendData.background || '-' }}</el-descriptions-item>
          <el-descriptions-item label="主要任务">{{ extendData.mainTask || '-' }}</el-descriptions-item>
          <el-descriptions-item label="进度安排">{{ extendData.schedule || '-' }}</el-descriptions-item>
          <el-descriptions-item label="参考文献要求">{{ extendData.references || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="statusType">{{ statusText }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下达时间">{{ parseDate(currentRecord.submitTime) }}</el-descriptions-item>
        </el-descriptions>

      </div>

      <el-empty v-else description="导师尚未下达任务书" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getCurrentInstance } from 'vue'
import { getThesisMainByStudent, listProcess } from '@/api/degree'
import { parseContentExtend } from '@/views/degree/processConfig'
import { getProcessStatusText, getProcessStatusType, parseDate } from '@/composables/useDegreeStatus'
import { onRoleInfoReady, getStudentId } from '@/composables/useRoleInfoReady'

const { proxy } = getCurrentInstance()

const currentRecord = ref(null)
const thesisId = ref(null)

const extendData = computed(() => parseContentExtend(currentRecord.value))
const statusText = computed(() => getProcessStatusText(currentRecord.value?.processStatus))
const statusType = computed(() => getProcessStatusType(currentRecord.value?.processStatus))

async function loadData() {
  try {
    const studentId = getStudentId()
    if (!studentId) return
    const res = await getThesisMainByStudent(studentId)
    const thesis = res.data || res
    if (thesis && thesis.id) {
      thesisId.value = thesis.id
      const processRes = await listProcess({ thesisId: thesis.id, processType: 2, pageSize: 1 })
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
