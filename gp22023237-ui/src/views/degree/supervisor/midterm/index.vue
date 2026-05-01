<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header><span>学生中期检查</span></template>
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column label="已完成工作" min-width="160">
          <template #default="{ row }">{{ (parseContentExtend(row).completedWork || '-').substring(0, 50) }}</template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getProcessStatusType(row.processStatus)">{{ getProcessStatusText(row.processStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="160">
          <template #default="{ row }">{{ parseDate(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.supervisorStatus === 0" type="success" size="small" @click="handleApprove(row, 1)">通过</el-button>
            <el-button v-if="row.supervisorStatus === 0" type="danger" size="small" @click="handleApprove(row, 2)">退回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 16px;" v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" @current-change="loadData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCurrentInstance } from 'vue'
import { listProcess, approveProcessSupervisor, getSupervisorStudents } from '@/api/degree'
import { parseContentExtend } from '@/views/degree/processConfig'
import { getProcessStatusText, getProcessStatusType, parseDate } from '@/composables/useDegreeStatus'

const { proxy } = getCurrentInstance()
const userStore = proxy.$pinia._s.get('user')

const loading = ref(false)
const records = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

function handleApprove(row, status) {
  const comment = status === 2 ? prompt('请输入退回原因') : ''
  approveProcessSupervisor(row.id, status, comment, userStore?.id).then(() => {
    proxy.$modal.msgSuccess('操作成功')
    loadData()
  })
}

async function loadData() {
  loading.value = true
  try {
    const studentRes = await getSupervisorStudents(userStore?.id)
    const thesisList = studentRes.data || studentRes || []
    const thesisIds = thesisList.map(t => t.id)
    if (thesisIds.length === 0) { loading.value = false; return }
    const res = await listProcess({ thesisId: thesisIds.join(','), processType: 4, pageNum: pageNum.value, pageSize: pageSize.value })
    records.value = res.data?.records || res.rows || []
    total.value = res.data?.total || res.total || 0
  } catch (e) { console.error(e) }
  loading.value = false
}

onMounted(() => loadData())
</script>
