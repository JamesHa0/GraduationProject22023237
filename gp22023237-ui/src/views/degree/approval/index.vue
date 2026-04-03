<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">学位审批</span>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="queryParams.studentNo" placeholder="请输入学号" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="姓名" prop="studentName">
          <el-input v-model="queryParams.studentName" placeholder="请输入姓名" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="分委审批" prop="committeeStatus">
          <el-select v-model="queryParams.committeeStatus" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待审批" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="dataList" border>
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="学号" prop="studentNo" align="center" width="120" />
        <el-table-column label="姓名" prop="studentName" align="center" width="100" />
        <el-table-column label="学位类型" prop="degreeType" align="center" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.degreeType === 1 ? '硕士学位' : '博士学位' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="论文题目" prop="thesisTitle" align="center" show-overflow-tooltip min-width="180" />
        <el-table-column label="答辩结果" prop="defenseResult" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getResultType(row.defenseResult)" size="small">
              {{ getResultText(row.defenseResult) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="答辩评分" prop="defenseScore" align="center" width="100" />
        <el-table-column label="分委审批" prop="committeeStatus" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.committeeStatus)" size="small">
              {{ getStatusText(row.committeeStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="学位授予" prop="degreeGranted" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="row.degreeGranted === 1 ? 'success' : 'info'" size="small">
              {{ row.degreeGranted === 1 ? '已授予' : '未授予' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="handleView(row)">详情</el-button>
            <el-button link size="small" type="success" @click="handleApprove(row)" v-if="canApprove(row)">审批</el-button>
            <el-button link size="small" type="warning" @click="handleGrant(row)" v-if="canGrant(row)">授予</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog title="详情" v-model="dialogVisible" width="800px" append-to-body>
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="学号" :span="1">{{ currentRow.studentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名" :span="1">{{ currentRow.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学位类型" :span="1">
          <el-tag>{{ currentRow.degreeType === 1 ? '硕士学位' : '博士学位' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="导师姓名" :span="1">{{ currentRow.mentorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="论文题目" :span="2">{{ currentRow.thesisTitle || '-' }}</el-descriptions-item>
        <el-descriptions-item label="答辩时间" :span="1">{{ parseDate(currentRow.defenseTime) }}</el-descriptions-item>
        <el-descriptions-item label="答辩地点" :span="1">{{ currentRow.defenseLocation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="答辩委员会主席" :span="1">{{ currentRow.committeeChair || '-' }}</el-descriptions-item>
        <el-descriptions-item label="答辩结果" :span="1">
          <el-tag :type="getResultType(currentRow.defenseResult)">
            {{ getResultText(currentRow.defenseResult) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="答辩评分" :span="1">{{ currentRow.defenseScore || '-' }}</el-descriptions-item>
        <el-descriptions-item label="分委审批" :span="1">
          <el-tag :type="getStatusType(currentRow.committeeStatus)">
            {{ getStatusText(currentRow.committeeStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="学位授予" :span="2">
          <el-tag :type="currentRow.degreeGranted === 1 ? 'success' : 'info'">
            {{ currentRow.degreeGranted === 1 ? '已授予' : '未授予' }}
          </el-tag>
          <span v-if="currentRow.degreeGranted === 1" style="margin-left: 10px">
            证书编号: {{ currentRow.certificateNo || '-' }}, 授予日期: {{ parseDate(currentRow.degreeGrantDate) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="分委审批意见" :span="2">{{ currentRow.committeeComment || '-' }}</el-descriptions-item>
        <el-descriptions-item label="答辩委员会评语" :span="2">{{ currentRow.defenseCommitteeComment || '-' }}</el-descriptions-item>
        <el-descriptions-item label="问答记录" :span="2">
          <div style="white-space: pre-wrap">{{ currentRow.qaRecord || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <template v-if="currentRow">
            <template v-if="canApprove(currentRow)">
              <el-button type="danger" @click="openRejectDialog">拒绝</el-button>
              <el-button type="primary" @click="handleApprove(currentRow)">通过</el-button>
            </template>
            <template v-if="canGrant(currentRow)">
              <el-button type="warning" @click="handleGrant(currentRow)">授予学位</el-button>
            </template>
          </template>
        </div>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog v-model="approvalDialogVisible" :title="approvalType === 'approve' ? '通过申请' : '拒绝申请'" width="500px" append-to-body>
      <el-form :model="approvalForm" :rules="approvalRules" ref="approvalFormRef" label-width="80px">
        <el-form-item label="审批意见" prop="comment">
          <el-input v-model="approvalForm.comment" type="textarea" :rows="4" :placeholder="approvalType === 'approve' ? '请输入审批意见（可选）' : '请输入拒绝原因'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="approvalDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitApproval">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 学位授予对话框 -->
    <el-dialog v-model="grantDialogVisible" title="授予学位" width="500px" append-to-body>
      <el-form :model="grantForm" :rules="grantRules" ref="grantFormRef" label-width="100px">
        <el-form-item label="学位证书编号" prop="certificateNo">
          <el-input v-model="grantForm.certificateNo" placeholder="请输入学位证书编号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="grantDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitGrant">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DegreeApproval">
import { ref, reactive, getCurrentInstance, toRefs } from 'vue'
import { listDegreeApplication, committeeApprove, grantDegree } from '@/api/degree'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const showSearch = ref(true)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const approvalDialogVisible = ref(false)
const grantDialogVisible = ref(false)
const currentRow = ref(null)
const approvalType = ref('approve')
const approvalFormRef = ref(null)
const grantFormRef = ref(null)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentNo: undefined,
    studentName: undefined,
    committeeStatus: undefined
  },
  approvalForm: {
    id: undefined,
    comment: ''
  },
  approvalRules: {
    comment: [
      { required: false, message: '请输入审批意见', trigger: 'blur' }
    ]
  },
  grantForm: {
    id: undefined,
    certificateNo: ''
  },
  grantRules: {
    certificateNo: [{ required: true, message: '请输入学位证书编号', trigger: 'blur' }]
  }
})

const { queryParams, approvalForm, approvalRules, grantForm, grantRules } = toRefs(data)

function getResultText(result) {
  const textMap = { 0: '未答辩', 1: '通过', 2: '修改后通过', 3: '不通过', null: '-', undefined: '-' }
  return textMap[result] !== undefined ? textMap[result] : '-'
}

function getResultType(result) {
  const typeMap = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return typeMap[result] || 'info'
}

function getStatusText(status) {
  const textMap = { 0: '待审批', 1: '已通过', 2: '已拒绝', null: '-', undefined: '-' }
  return textMap[status] !== undefined ? textMap[status] : '-'
}

function getStatusType(status) {
  const typeMap = { 0: 'warning', 1: 'success', 2: 'danger' }
  return typeMap[status] || 'info'
}

function parseDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN')
}

function getCurrentUserRoleId() {
  const userStore = useUserStore()
  if (userStore.roles && userStore.roles.length > 0) {
    return userStore.roles[0]
  }
  return null
}

function canApprove(row) {
  if (!row) return false
  const roleId = getCurrentUserRoleId()
  // 学位分委员会主席(3)或超级管理员(1)可以审批
  return (roleId === 3 || roleId === 1) && row.committeeStatus === 0
}

function canGrant(row) {
  if (!row) return false
  const roleId = getCurrentUserRoleId()
  // 分管院长(2)或超级管理员(1)可以授予学位，需要分委会已通过且未授予
  return (roleId === 2 || roleId === 1) && row.committeeStatus === 1 && row.degreeGranted === 0
}

function getList() {
  loading.value = true
  listDegreeApplication(queryParams.value).then(res => {
    loading.value = false
    dataList.value = res.data.records || res.data || []
    total.value = res.data.total || dataList.value.length
  }).catch(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.value.studentNo = undefined
  queryParams.value.studentName = undefined
  queryParams.value.committeeStatus = undefined
  handleQuery()
}

function handleView(row) {
  currentRow.value = row
  approvalType.value = 'approve'
  approvalForm.value.id = row.id
  approvalForm.value.comment = ''
  dialogVisible.value = true
}

function handleApprove(row) {
  currentRow.value = row
  approvalType.value = 'approve'
  approvalForm.value.id = row.id
  approvalForm.value.comment = ''
  dialogVisible.value = true
}

function openRejectDialog() {
  dialogVisible.value = false
  approvalType.value = 'reject'
  approvalForm.value.id = currentRow.value.id
  approvalForm.value.comment = ''
  approvalDialogVisible.value = true
}

function submitApproval() {
  if (approvalType.value === 'reject' && !approvalForm.value.comment.trim()) {
    proxy.$modal.msgWarning('请输入拒绝原因')
    return
  }

  const status = approvalType.value === 'approve' ? 1 : 2

  committeeApprove(approvalForm.value.id, status, approvalForm.value.comment).then(() => {
    proxy.$modal.msgSuccess('审批成功')
    approvalDialogVisible.value = false
    dialogVisible.value = false
    getList()
  }).catch(() => {
    proxy.$modal.msgError('审批失败')
  })
}

function handleGrant(row) {
  currentRow.value = row
  grantForm.value.id = row.id
  grantForm.value.certificateNo = ''
  grantDialogVisible.value = true
}

function submitGrant() {
  proxy.$refs['grantFormRef'].validate(valid => {
    if (valid) {
      grantDegree(grantForm.value.id, grantForm.value.certificateNo).then(() => {
        proxy.$modal.msgSuccess('学位授予成功')
        grantDialogVisible.value = false
        dialogVisible.value = false
        getList()
      }).catch(() => {
        proxy.$modal.msgError('学位授予失败')
      })
    }
  })
}

getList()
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
}
</style>
