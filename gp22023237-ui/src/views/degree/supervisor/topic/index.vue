<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header><span>学生选题</span></template>
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="thesisId" label="论文ID" width="80" />
        <el-table-column label="课题名称" min-width="180">
          <template #default="{ row }">{{ parseContentExtend(row).topicName || '-' }}</template>
        </el-table-column>
        <el-table-column label="课题来源" width="120">
          <template #default="{ row }">{{ parseContentExtend(row).topicSource || '-' }}</template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getProcessStatusType(row.processStatus)">{{ getProcessStatusText(row.processStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="160">
          <template #default="{ row }">{{ parseDate(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.supervisorStatus === 0" type="success" size="small" @click="handleApprove(row, 1)">通过</el-button>
            <el-button v-if="row.supervisorStatus === 0" type="danger" size="small" @click="handleApprove(row, 2)">退回</el-button>
            <el-button size="small" @click="handleAssign(row)">指定课题</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 16px;" v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" @current-change="loadData" />

      <el-dialog v-model="assignVisible" title="指定课题" width="500px">
        <el-form :model="assignForm" label-width="80px">
          <el-form-item label="课题名称"><el-input v-model="assignForm.topicName" /></el-form-item>
          <el-form-item label="课题说明"><el-input v-model="assignForm.topicDesc" type="textarea" :rows="3" /></el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="assignVisible = false">取消</el-button>
          <el-button type="primary" @click="doAssign">确定</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCurrentInstance } from 'vue'
import { getSupervisorTopic, assignTopic, approveProcessSupervisor } from '@/api/degree'
import { parseContentExtend } from '@/views/degree/processConfig'
import { getProcessStatusText, getProcessStatusType, parseDate } from '@/composables/useDegreeStatus'

const { proxy } = getCurrentInstance()
const userStore = proxy.$pinia._s.get('user')

const loading = ref(false)
const records = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const assignVisible = ref(false)
const assignForm = ref({ thesisId: null, topicName: '', topicDesc: '' })

function handleApprove(row, status) {
  const comment = status === 2 ? prompt('请输入退回原因') : ''
  if (status === 2 && !comment) return
  approveProcessSupervisor(row.id, status, comment, userStore?.id).then(() => {
    proxy.$modal.msgSuccess('操作成功')
    loadData()
  })
}

function handleAssign(row) {
  assignForm.value = { thesisId: row.thesisId, topicName: '', topicDesc: '' }
  assignVisible.value = true
}

function doAssign() {
  assignTopic(assignForm.value).then(() => {
    proxy.$modal.msgSuccess('指定课题成功')
    assignVisible.value = false
    loadData()
  })
}

async function loadData() {
  loading.value = true
  try {
    const res = await getSupervisorTopic({ supervisorId: userStore?.id, pageNum: pageNum.value, pageSize: pageSize.value })
    records.value = res.data?.records || res.rows || []
    total.value = res.data?.total || res.total || 0
  } catch (e) { console.error(e) }
  loading.value = false
}

onMounted(() => loadData())
</script>
