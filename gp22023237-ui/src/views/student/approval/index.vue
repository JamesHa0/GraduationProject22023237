<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="申请状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 200px">
          <el-option label="待导师审批" :value="0" />
          <el-option label="待教学秘书审批" :value="1" />
          <el-option label="已通过" :value="2" />
          <el-option label="已拒绝" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="学号" prop="studentNo">
        <el-input v-model="queryParams.studentNo" placeholder="请输入学号" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item label="学生姓名" prop="studentName">
        <el-input v-model="queryParams.studentName" placeholder="请输入学生姓名" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="dataList" border>
      <el-table-column label="学号" align="center" prop="studentNo" width="120" />
      <el-table-column label="姓名" align="center" prop="studentName" :show-overflow-tooltip="true" />
      <el-table-column label="变更类型" align="center" prop="changeType" width="100">
        <template #default="{ row }">
          <el-tag>{{ getTypeName(row.changeType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="导师审批" align="center" prop="mentorStatus" width="120">
        <template #default="{ row }">
          <el-tag :type="getApprovalStatusType(row.mentorStatus)">
            {{ getApprovalStatusText(row.mentorStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="教学秘书审批" align="center" prop="secretaryStatus" width="120">
        <template #default="{ row }">
          <el-tag :type="getApprovalStatusType(row.secretaryStatus)">
            {{ getApprovalStatusText(row.secretaryStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="整体状态" align="center" prop="status" width="140">
        <template #default="{ row }">
          <el-tag :type="getOverallStatusType(row.status)">
            {{ getOverallStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="申请时间" align="center" prop="createTime" width="170">
        <template #default="scope">
          {{ parseDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="250" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <!-- 导师审批按钮：导师角色 + 导师未审批 -->
          <el-button v-if="isTeacher && canMentorApprove(row)"
            link type="primary" icon="Edit" @click="handleApprove(row, 'mentor')">导师审批</el-button>
          <!-- 教学秘书审批按钮：秘书角色 + 导师已通过 + 秘书未审批 -->
          <el-button v-if="isSecretary && canSecretaryApprove(row)"
            link type="primary" icon="Edit" @click="handleApprove(row, 'secretary')">秘书审批</el-button>
          <el-button link type="primary" icon="View" @click="handleView(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 查看详情对话框 -->
    <el-dialog title="学籍变更详情" v-model="detailDialogVisible" width="700px" append-to-body>
      <el-form :model="detailForm" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="学号">
              <el-input :model-value="detailForm.studentNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input :model-value="detailForm.studentName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="变更类型">
              <el-tag>{{ getTypeName(detailForm.changeType) }}</el-tag>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="变更原因">
              <el-input :model-value="detailForm.reason" type="textarea" :rows="3" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="导师审批">
              <span v-if="detailForm.mentorName" style="margin-right: 6px;">{{ detailForm.mentorName }}</span>
              <el-tag :type="getApprovalStatusType(detailForm.mentorStatus)">
                {{ getApprovalStatusText(detailForm.mentorStatus) }}
              </el-tag>
              <span v-if="detailForm.mentorApprovalTime" style="margin-left: 10px; color: #909399; font-size: 12px;">{{ parseDate(detailForm.mentorApprovalTime) }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="教学秘书审批">
              <el-tag :type="getApprovalStatusType(detailForm.secretaryStatus)">
                {{ getApprovalStatusText(detailForm.secretaryStatus) }}
              </el-tag>
              <span v-if="detailForm.secretaryApprovalTime" style="margin-left: 10px; color: #909399; font-size: 12px;">{{ parseDate(detailForm.secretaryApprovalTime) }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="整体状态">
              <el-tag :type="getOverallStatusType(detailForm.status)">
                {{ getOverallStatusText(detailForm.status) }}
              </el-tag>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申请时间">
              <span>{{ parseDate(detailForm.createTime) }}</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog title="学籍异动审批" v-model="approveDialogVisible" width="500px" append-to-body>
      <el-form :model="approveForm" label-width="100px">
        <el-form-item label="学生姓名">
          <el-input :model-value="approveRow?.studentName" disabled />
        </el-form-item>
        <el-form-item label="变更类型">
          <el-tag>{{ getTypeName(approveRow?.changeType) }}</el-tag>
        </el-form-item>
        <el-form-item label="审批类型">
          <el-input :model-value="approveType === 'mentor' ? '导师审批' : '教学秘书审批'" disabled />
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="approveForm.comment" type="textarea" :rows="3" placeholder="请输入审批意见（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="danger" @click="submitApprove(2)">拒 绝</el-button>
          <el-button type="primary" @click="submitApprove(1)">通 过</el-button>
          <el-button @click="approveDialogVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="StudentApproval">
import { listStatusChange, getStatusChangeDetail, approveStatusChangeMentor, approveStatusChangeSecretary } from "@/api/student/status"
import { getCurrentInstance, ref, reactive, toRefs } from 'vue'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()

const loading = ref(false)
const showSearch = ref(true)
const dataList = ref([])
const total = ref(0)
const detailDialogVisible = ref(false)
const approveDialogVisible = ref(false)
const approveType = ref('')
const approveRow = ref(null)
const approveForm = reactive({ comment: '' })
const detailForm = ref({})

// 判断当前用户角色
const roleId = getRoleId()
const isTeacher = roleId === 7 || roleId === 8
const isSecretary = roleId === 5 || roleId === 4

const columns = ref([
  { key: 0, label: `学号`, visible: true },
  { key: 1, label: `姓名`, visible: true },
  { key: 2, label: `变更类型`, visible: true },
  { key: 3, label: `导师审批`, visible: true },
  { key: 4, label: `教学秘书审批`, visible: true },
  { key: 5, label: `整体状态`, visible: true },
  { key: 6, label: `申请时间`, visible: true }
])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 20,
    status: undefined,
    studentNo: undefined,
    studentName: undefined
  }
})

const { queryParams } = toRefs(data)

function getRoleId() {
  if (userStore.roles !== undefined && userStore.roles !== null) {
    if (Array.isArray(userStore.roles)) {
      return userStore.roles.length > 0 ? userStore.roles[0] : null
    }
    return Number(userStore.roles)
  }
  return null
}

function getTypeName(type) {
  const map = { 1: '休学', 2: '复学', 3: '退学', 4: '延期毕业' }
  return map[type] || '-'
}

// 单级审批状态：0-待审批、1-已通过、2-已拒绝
function getApprovalStatusText(status) {
  const map = { 0: '待审批', 1: '已通过', 2: '已拒绝' }
  return map[status] || '-'
}

function getApprovalStatusType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

// 整体状态：0-待导师审批、1-待教学秘书审批、2-已通过、3-已拒绝
function getOverallStatusText(status) {
  const map = { 0: '待导师审批', 1: '待秘书审批', 2: '已通过', 3: '已拒绝' }
  return map[status] || '-'
}

function getOverallStatusType(status) {
  const map = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

function parseDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN')
}

// 导师是否可以审批：整体状态为待导师审批 + 导师未审批
function canMentorApprove(row) {
  return row.status === 0 && row.mentorStatus === 0
}

// 教学秘书是否可以审批：整体状态为待秘书审批 + 导师已通过 + 秘书未审批
function canSecretaryApprove(row) {
  return row.status === 1 && row.mentorStatus === 1 && row.secretaryStatus === 0
}

function getList() {
  loading.value = true
  listStatusChange(queryParams.value).then(res => {
    loading.value = false
    dataList.value = res.data || []
    total.value = res.pagination?.total || res.data?.total || dataList.value.length
  }).catch(() => {
    loading.value = false
    proxy.$modal.msgError("获取审批列表失败")
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleView(row) {
  getStatusChangeDetail(row.id).then(res => {
    detailForm.value = res.data
    detailDialogVisible.value = true
  }).catch(() => {
    proxy.$modal.msgError("获取申请详情失败")
  })
}

// 打开审批对话框
function handleApprove(row, type) {
  approveRow.value = row
  approveType.value = type
  approveForm.comment = ''
  approveDialogVisible.value = true
}

// 提交审批
function submitApprove(status) {
  if (status === 2 && !approveForm.comment.trim()) {
    proxy.$modal.msgWarning('请输入拒绝原因')
    return
  }

  const id = approveRow.value.id
  const comment = approveForm.comment
  const apiCall = approveType.value === 'mentor'
    ? approveStatusChangeMentor
    : approveStatusChangeSecretary

  apiCall(id, status, comment).then(() => {
    proxy.$modal.msgSuccess(status === 1 ? '审批通过' : '已拒绝')
    approveDialogVisible.value = false
    getList()
  }).catch(() => {
    proxy.$modal.msgError('审批操作失败')
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
