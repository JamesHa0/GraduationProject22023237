<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学术内容审核</span>
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

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="dataList" border>
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="内容类型" prop="type" align="center" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标题" prop="title" align="center" show-overflow-tooltip />
        <el-table-column label="申请人" prop="studentName" align="center" width="120" />
        <el-table-column label="申请时间" prop="createTime" align="center" width="180" />
        <el-table-column label="审批状态" prop="status" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="handleView(row)">详情</el-button>
            <el-button link size="small" type="success" @click="handleApprove(row)" v-if="row.status === 0">通过</el-button>
            <el-button link size="small" type="danger" @click="handleReject(row)" v-if="row.status === 0">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog title="内容详情" v-model="dialogVisible" width="700px" append-to-body>
      <el-descriptions :column="1" border v-if="currentRow">
        <el-descriptions-item label="内容类型">
          <el-tag :type="getTypeTagType(currentRow.type)">{{ getTypeName(currentRow.type) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentRow.title }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentRow.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRow.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="详细描述">
          <div style="white-space: pre-wrap">{{ currentRow.description || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag :type="getStatusType(currentRow.status)">{{ getStatusText(currentRow.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审批意见" v-if="currentRow.approvalComment">
          {{ currentRow.approvalComment }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <template v-if="currentRow && currentRow.status === 0">
            <el-button type="danger" @click="openRejectDialog">拒绝</el-button>
            <el-button type="primary" @click="handleApprove(currentRow)">通过</el-button>
          </template>
        </div>
      </template>
    </el-dialog>

    <!-- 拒绝对话框 -->
    <el-dialog title="拒绝申请" v-model="rejectDialogVisible" width="500px" append-to-body>
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因">
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
import { ref, reactive, getCurrentInstance } from 'vue'
import { ElMessage } from 'element-plus'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const showSearch = ref(true)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const currentRow = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  type: undefined,
  status: undefined
})

const rejectForm = reactive({
  id: undefined,
  comment: ''
})

function getTypeTagType(type) {
  const typeMap = { 1: 'primary', 2: 'success', 3: 'warning' }
  return typeMap[type] || 'info'
}

function getTypeName(type) {
  const textMap = { 1: '学术活动', 2: '创新项目', 3: '学术成果' }
  return textMap[type] || '-'
}

function getStatusText(status) {
  const textMap = { 0: '待审批', 1: '已通过', 2: '已拒绝' }
  return textMap[status] || '-'
}

function getStatusType(status) {
  const typeMap = { 0: 'warning', 1: 'success', 2: 'danger' }
  return typeMap[status] || 'info'
}

function getList() {
  loading.value = true
  // TODO: 调用实际的审核列表 API
  setTimeout(() => {
    dataList.value = [
      { id: 1, type: 1, title: '人工智能学术讲座', studentName: '张三', createTime: '2024-03-15 10:30:00', status: 0, description: '邀请李教授做关于人工智能最新进展的学术讲座' },
      { id: 2, type: 2, title: '智能家居创新项目', studentName: '李四', createTime: '2024-03-14 14:20:00', status: 0, description: '基于物联网技术的智能家居控制系统' },
      { id: 3, type: 3, title: '深度学习论文发表', studentName: '王五', createTime: '2024-03-13 09:15:00', status: 1, description: '在顶级会议上发表的深度学习研究成果', approvalComment: '研究成果优秀，同意通过' },
      { id: 4, type: 1, title: '区块链技术研讨会', studentName: '赵六', createTime: '2024-03-12 16:45:00', status: 2, description: '组织区块链技术交流研讨会', approvalComment: '主题与专业方向不符，建议重新申请' }
    ]
    total.value = dataList.value.length
    loading.value = false
  }, 500)
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.type = undefined
  queryParams.status = undefined
  handleQuery()
}

function handleView(row) {
  currentRow.value = row
  dialogVisible.value = true
}

function handleApprove(row) {
  proxy.$modal.confirm('确认通过该申请？').then(() => {
    // TODO: 调用审批通过 API
    ElMessage.success('审批通过')
    getList()
    dialogVisible.value = false
  }).catch(() => {})
}

function handleReject(row) {
  rejectForm.id = row.id
  rejectForm.comment = ''
  rejectDialogVisible.value = true
}

function openRejectDialog() {
  dialogVisible.value = false
  rejectForm.id = currentRow.value.id
  rejectForm.comment = ''
  rejectDialogVisible.value = true
}

function submitReject() {
  if (!rejectForm.comment.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  // TODO: 调用拒绝 API
  ElMessage.success('已拒绝申请')
  rejectDialogVisible.value = false
  getList()
}

getList()
</script>
