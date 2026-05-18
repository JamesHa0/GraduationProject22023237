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

      <el-dialog v-model="rejectVisible" title="退回原因" width="500px">
        <el-form label-width="80px">
          <el-form-item label="退回原因">
            <el-input v-model="rejectComment" type="textarea" :rows="3" placeholder="请输入退回原因" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="rejectVisible = false">取消</el-button>
          <el-button type="danger" @click="doReject">确认退回</el-button>
        </template>
      </el-dialog>
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
const rejectVisible = ref(false)
const rejectComment = ref('')
const rejectRow = ref(null)

function handleApprove(row, status) {
  if (status === 2) {
    rejectRow.value = row
    rejectComment.value = ''
    rejectVisible.value = true
    return
  }
  approveProcessSupervisor(row.id, status, '', userStore?.roleInfo?.[0]?.id || userStore?.userId).then(() => {
    proxy.$modal.msgSuccess('操作成功')
    loadData()
  })
}

function doReject() {
  if (!rejectComment.value.trim()) {
    proxy.$modal.msgWarning('请输入退回原因')
    return
  }
  approveProcessSupervisor(rejectRow.value.id, 2, rejectComment.value, userStore?.roleInfo?.[0]?.id || userStore?.userId).then(() => {
    proxy.$modal.msgSuccess('已退回')
    rejectVisible.value = false
    loadData()
  })
}

async function loadData() {
  loading.value = true
  try {
    const res = await getSupervisorThesisFinal({ supervisorId: userStore?.roleInfo?.[0]?.id || userStore?.userId, pageNum: pageNum.value, pageSize: pageSize.value })
    records.value = res.data || res.rows || []
    total.value = res.pagination?.total || res.total || 0
  } catch (e) { console.error(e) }
  loading.value = false
}

onMounted(() => loadData())
</script>
