<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">学术内容审核</span>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
        <el-form-item label="内容类型" prop="type">
          <el-select v-model="queryParams.type" placeholder="请选择" clearable style="width: 150px">
            <el-option label="学术活动" :value="1" />
            <el-option label="创新项目" :value="2" />
            <el-option label="学术成果" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px">
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
        <el-table-column label="内容类型" prop="type" align="center" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标题" prop="title" align="center" show-overflow-tooltip min-width="180" />
        <el-table-column label="申请人" prop="studentName" align="center" width="100" />
        <el-table-column label="申请时间" prop="submitTime" align="center" width="160">
          <template #default="{ row }">
            {{ parseDate(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="审批状态" prop="status" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
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
    <el-dialog title="内容详情" v-model="dialogVisible" width="700px" append-to-body>
      <el-descriptions :column="1" border v-if="currentRow">
        <el-descriptions-item label="内容类型">
          <el-tag :type="getTypeTagType(currentRow.type)">{{ getTypeName(currentRow.type) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentRow.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentRow.studentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentRow.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ parseDate(currentRow.submitTime) }}</el-descriptions-item>
        <el-descriptions-item label="详细描述">
          <div style="white-space: pre-wrap">{{ currentRow.description || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="导师审批">
          <el-tag :type="getStatusType(currentRow.mentorStatus)">
            {{ getStatusText(currentRow.mentorStatus) }}
          </el-tag>
          <span v-if="currentRow.mentorComment" style="margin-left: 10px">意见: {{ currentRow.mentorComment }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="教学秘书审批">
          <el-tag :type="getStatusType(currentRow.secretaryStatus)">
            {{ getStatusText(currentRow.secretaryStatus) }}
          </el-tag>
          <span v-if="currentRow.secretaryComment" style="margin-left: 10px">意见: {{ currentRow.secretaryComment }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="分管院长审批">
          <el-tag :type="getStatusType(currentRow.deanStatus)">
            {{ getStatusText(currentRow.deanStatus) }}
          </el-tag>
          <span v-if="currentRow.deanComment" style="margin-left: 10px">意见: {{ currentRow.deanComment }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag :type="getStatusType(currentRow.status)">
            {{ getStatusText(currentRow.status) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <template v-if="currentRow && canApprove(currentRow)">
            <el-button type="danger" @click="openRejectDialog">拒绝</el-button>
            <el-button type="primary" @click="handleApprove(currentRow)">通过</el-button>
          </template>
        </div>
      </template>
    </el-dialog>

    <!-- 拒绝对话框 -->
    <el-dialog title="拒绝申请" v-model="rejectDialogVisible" width="500px" append-to-body>
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef" label-width="80px">
        <el-form-item label="拒绝原因" prop="comment">
          <el-input v-model="rejectForm.comment" type="textarea" :rows="4" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReject">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AcademicReview">
import { ref, reactive, getCurrentInstance, toRefs } from 'vue'
import { listReview, approveReview } from '@/api/academic'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const showSearch = ref(true)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const currentRow = ref(null)
const rejectFormRef = ref(null)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    type: undefined,
    status: undefined
  },
  rejectForm: {
    id: undefined,
    type: undefined,
    comment: ''
  },
  rejectRules: {
    comment: [
      { required: true, message: '请输入拒绝原因', trigger: 'blur' }
    ]
  }
})

const { queryParams, rejectForm, rejectRules } = toRefs(data)

function getTypeTagType(type) {
  const typeMap = { 1: 'primary', 2: 'success', 3: 'warning' }
  return typeMap[type] || 'info'
}

function getTypeName(type) {
  const textMap = { 1: '学术活动', 2: '创新项目', 3: '学术成果' }
  return textMap[type] || '-'
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
    return row.mentorStatus === 1 && row.secretaryStatus === 1 && (row.deanStatus === 0 || row.deanStatus === null || row.deanStatus === undefined)
  }

  return false
}

function getList() {
  loading.value = true
  listReview(queryParams.value).then(res => {
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
  queryParams.value.type = undefined
  queryParams.value.status = undefined
  handleQuery()
}

function handleView(row) {
  currentRow.value = row
  dialogVisible.value = true
}

function handleApprove(row) {
  proxy.$modal.confirm('确认通过该申请？').then(() => {
    const params = {
      id: row.id,
      type: row.type,
      status: 1,
      comment: '审批通过'
    }
    approveReview(params).then(() => {
      proxy.$modal.msgSuccess('审批通过')
      dialogVisible.value = false
      getList()
    }).catch(() => {
      proxy.$modal.msgError('审批失败')
    })
  }).catch(() => {})
}

function handleReject(row) {
  rejectForm.value.id = row.id
  rejectForm.value.type = row.type
  rejectForm.value.comment = ''
  rejectDialogVisible.value = true
}

function openRejectDialog() {
  dialogVisible.value = false
  rejectForm.value.id = currentRow.value.id
  rejectForm.value.type = currentRow.value.type
  rejectForm.value.comment = ''
  rejectDialogVisible.value = true
}

function submitReject() {
  proxy.$refs['rejectFormRef'].validate(valid => {
    if (valid) {
      const params = {
        id: rejectForm.value.id,
        type: rejectForm.value.type,
        status: 2,
        comment: rejectForm.value.comment
      }
      approveReview(params).then(() => {
        proxy.$modal.msgSuccess('已拒绝申请')
        rejectDialogVisible.value = false
        getList()
      }).catch(() => {
        proxy.$modal.msgError('操作失败')
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
