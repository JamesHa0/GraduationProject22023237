<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">学籍变更审批</span>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
        <el-form-item label="审批状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 150px">
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
        <el-table-column label="姓名" prop="studentName" align="center" width="120" />
        <el-table-column label="变更类型" prop="changeType" align="center" width="120">
          <template #default="{ row }">
            <el-tag>{{ getTypeName(row.changeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="原因" prop="reason" align="center" show-overflow-tooltip min-width="200" />
        <el-table-column label="提交时间" prop="createTime" align="center" width="180" />
        <el-table-column label="导师审批" prop="mentorStatus" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.mentorStatus)" size="small">
              {{ getStatusText(row.mentorStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="秘书审批" prop="secretaryStatus" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.secretaryStatus)" size="small">
              {{ getStatusText(row.secretaryStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="整体状态" prop="status" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="handleView(row)">详情</el-button>
            <el-button link size="small" type="success" @click="handleApprove(row)" v-if="canApprove(row)">通过</el-button>
            <el-button link size="small" type="danger" @click="handleReject(row)" v-if="canApprove(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog title="学籍变更详情" v-model="dialogVisible" width="700px" append-to-body>
      <el-descriptions :column="1" border v-if="currentRow">
        <el-descriptions-item label="学号">{{ currentRow.studentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentRow.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="变更类型">
          <el-tag>{{ getTypeName(currentRow.changeType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ parseDate(currentRow.startDate) }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ parseDate(currentRow.endDate) }}</el-descriptions-item>
        <el-descriptions-item label="变更原因">
          <div style="white-space: pre-wrap">{{ currentRow.reason || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="导师审批">
          <el-tag :type="getStatusType(currentRow.mentorStatus)">
            {{ getStatusText(currentRow.mentorStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="教学秘书审批">
          <el-tag :type="getStatusType(currentRow.secretaryStatus)">
            {{ getStatusText(currentRow.secretaryStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="整体状态">
          <el-tag :type="getStatusType(currentRow.status)">
            {{ getStatusText(currentRow.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ parseDate(currentRow.createTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <template v-if="currentRow && canApprove(currentRow)">
            <el-button type="danger" @click="openRejectDialog">拒绝</el-button>
            <el-button type="primary" @click="openApproveDialog">通过</el-button>
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
  </div>
</template>

<script setup name="StudentApproval">
import { ref, reactive, getCurrentInstance, toRefs } from 'vue'
import { listStatusChange, approveStatusChangeMentor, approveStatusChangeSecretary, approveStatusChangeDean } from "@/api/student/status"
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const showSearch = ref(true)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const approvalDialogVisible = ref(false)
const currentRow = ref(null)
const approvalType = ref('approve')
const approvalFormRef = ref(null)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    status: 0
  },
  approvalForm: {
    id: undefined,
    comment: ''
  },
  approvalRules: {
    comment: [
      { required: false, message: '请输入审批意见', trigger: 'blur' }
    ]
  }
})

const { queryParams, approvalForm, approvalRules } = toRefs(data)

function getTypeName(type) {
  const typeMap = { 1: '休学', 2: '复学', 3: '退学', 4: '延期毕业' }
  return typeMap[type] || '-'
}

function getStatusText(status) {
  const textMap = { 0: '待审批', 1: '已通过', 2: '已拒绝' }
  return textMap[status] || '-'
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
  if (userStore.roles !== undefined && userStore.roles !== null) {
    // 兼容数组和单个数字两种情况
    if (Array.isArray(userStore.roles)) {
      return userStore.roles.length > 0 ? userStore.roles[0] : null
    } else {
      return userStore.roles
    }
  }
  return null
}

function canApprove(row) {
  if (!row || row.status !== 0) return false

  const roleId = getCurrentUserRoleId()
  if (!roleId) return false

  console.log('当前用户roleId:', roleId, '记录状态:', {
    mentorStatus: row.mentorStatus,
    secretaryStatus: row.secretaryStatus
  })

  // 角色ID映射：
  // 1-超级管理员, 2-分管院长, 3-学位分委员会主席, 4-综合管理员
  // 5-研究生秘书, 6-学生, 7-老师/导师, 8-任课教师

  // 老师/导师(7)或任课教师(8)可以审批导师阶段
  if (roleId === 7 || roleId === 8) {
    return row.mentorStatus === 0
  }

  // 研究生秘书(5)或综合管理员(4)可以审批秘书阶段（需导师已通过）
  if (roleId === 5 || roleId === 4) {
    return row.mentorStatus === 1 && row.secretaryStatus === 0
  }

  // 分管院长(2)或超级管理员(1)可以最终审批（需导师、秘书已通过）
  if (roleId === 2 || roleId === 1) {
    return row.mentorStatus === 1 && row.secretaryStatus === 1
  }

  return false
}

function getList() {
  loading.value = true
  listStatusChange(queryParams.value).then(res => {
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
  queryParams.value.status = 0
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
  approvalDialogVisible.value = true
}

function handleReject(row) {
  currentRow.value = row
  approvalType.value = 'reject'
  approvalForm.value.id = row.id
  approvalForm.value.comment = ''
  approvalDialogVisible.value = true
}

function openRejectDialog() {
  dialogVisible.value = false
  approvalType.value = 'reject'
  approvalForm.value.id = currentRow.value.id
  approvalForm.value.comment = ''
  approvalDialogVisible.value = true
}

function openApproveDialog() {
  dialogVisible.value = false
  approvalType.value = 'approve'
  approvalForm.value.id = currentRow.value.id
  approvalForm.value.comment = ''
  approvalDialogVisible.value = true
}

function submitApproval() {
  if (approvalType.value === 'reject' && !approvalForm.value.comment.trim()) {
    proxy.$modal.msgWarning('请输入拒绝原因')
    return
  }

  const roleId = getCurrentUserRoleId()
  let approveApi

  // 角色ID映射：
  // 1-超级管理员, 2-分管院长, 3-学位分委员会主席, 4-综合管理员
  // 5-研究生秘书, 6-学生, 7-老师/导师, 8-任课教师

  // 老师/导师(7)或任课教师(8)使用导师审批API
  if (roleId === 7 || roleId === 8) {
    approveApi = approveStatusChangeMentor
  }
  // 研究生秘书(5)或综合管理员(4)使用秘书审批API
  else if (roleId === 5 || roleId === 4) {
    approveApi = approveStatusChangeSecretary
  }
  // 其他角色（分管院长2、超级管理员1）使用院长审批API
  else {
    approveApi = approveStatusChangeDean
  }

  const status = approvalType.value === 'approve' ? 1 : 2

  approveApi(approvalForm.value.id, status, approvalForm.value.comment).then(() => {
    proxy.$modal.msgSuccess('审批成功')
    approvalDialogVisible.value = false
    dialogVisible.value = false
    getList()
  }).catch(() => {
    proxy.$modal.msgError('审批失败')
  })
}

// 默认查询待审批的记录
queryParams.value.status = 0
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
