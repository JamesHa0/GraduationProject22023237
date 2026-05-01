<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header><span>论文最终稿管理</span></template>
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column label="论文文件" min-width="160">
          <template #default="{ row }">{{ row.thesisVersionUrl || '-' }}</template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getProcessStatusType(row.processStatus)">{{ getProcessStatusText(row.processStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="锁定" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.processStatus === 3 || row.processStatus === 5" type="danger" size="small">已锁定</el-tag>
            <el-tag v-else type="info" size="small">未锁定</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="160">
          <template #default="{ row }">{{ parseDate(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.supervisorStatus === 0" type="success" size="small" @click="handleApprove(row, 1)">同意定稿</el-button>
            <el-button v-if="row.supervisorStatus === 0" type="danger" size="small" @click="handleApprove(row, 2)">退回修改</el-button>
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
import { getSupervisorThesisFinal, approveProcessSupervisor } from '@/api/degree'
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
    const res = await getSupervisorThesisFinal({ supervisorId: userStore?.id, pageNum: pageNum.value, pageSize: pageSize.value })
    records.value = res.data?.records || res.rows || []
    total.value = res.data?.total || res.total || 0
  } catch (e) { console.error(e) }
  loading.value = false
}

onMounted(() => loadData())
</script>
