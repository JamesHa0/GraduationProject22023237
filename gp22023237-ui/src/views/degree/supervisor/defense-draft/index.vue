<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header><span>论文答辩稿管理</span></template>
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column label="答辩稿说明" min-width="160">
          <template #default="{ row }">{{ (parseContentExtend(row).defenseDraftDesc || '-').substring(0, 50) }}</template>
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
            <el-button v-if="row.supervisorStatus === 0" type="success" size="small" @click="handleComment(row, 1)">通过+评语</el-button>
            <el-button v-if="row.supervisorStatus === 0" type="danger" size="small" @click="handleComment(row, 2)">退回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 16px;" v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" @current-change="loadData" />

      <el-dialog v-model="dialogVisible" title="填写评语" width="500px">
        <el-form label-width="80px">
          <el-form-item label="评语"><el-input v-model="comment" type="textarea" :rows="4" placeholder="请输入评语" /></el-form-item>
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
import { ref, onMounted } from 'vue'
import { getCurrentInstance } from 'vue'
import { getSupervisorDefenseDraft, commentDefenseDraft } from '@/api/degree'
import { parseContentExtend } from '@/views/degree/processConfig'
import { getProcessStatusText, getProcessStatusType, parseDate } from '@/composables/useDegreeStatus'

const { proxy } = getCurrentInstance()
const userStore = proxy.$pinia._s.get('user')

const loading = ref(false)
const records = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const currentRow = ref(null)
const currentStatus = ref(1)
const comment = ref('')

function handleComment(row, status) {
  currentRow.value = row
  currentStatus.value = status
  comment.value = ''
  dialogVisible.value = true
}

function doSubmit() {
  commentDefenseDraft(currentRow.value.id, currentStatus.value, comment.value, userStore?.roleInfo?.[0]?.id || userStore?.userId).then(() => {
    proxy.$modal.msgSuccess('操作成功')
    dialogVisible.value = false
    loadData()
  })
}

async function loadData() {
  loading.value = true
  try {
    const res = await getSupervisorDefenseDraft({ supervisorId: userStore?.roleInfo?.[0]?.id || userStore?.userId, pageNum: pageNum.value, pageSize: pageSize.value })
    records.value = res.data || res.rows || []
    total.value = res.pagination?.total || res.total || 0
  } catch (e) { console.error(e) }
  loading.value = false
}

onMounted(() => loadData())
</script>
