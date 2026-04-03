<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">创新实践项目</span>
          <el-button type="primary" size="small" @click="handleAdd">新增项目</el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
        <el-form-item label="项目类型" prop="projectType">
          <el-select v-model="queryParams.projectType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="创新项目" :value="1" />
            <el-option label="创业项目" :value="2" />
            <el-option label="竞赛" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待导师审批" :value="1" />
            <el-option label="待秘书审批" :value="2" />
            <el-option label="待院长审批" :value="3" />
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
        <el-table-column label="项目类型" prop="projectType" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.projectType)">{{ getTypeName(row.projectType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="项目名称" prop="projectName" align="center" show-overflow-tooltip min-width="150" />
        <el-table-column label="项目级别" prop="projectLevel" align="center" width="100">
          <template #default="{ row }">
            {{ getLevelName(row.projectLevel) }}
          </template>
        </el-table-column>
        <el-table-column label="负责人" prop="leader" align="center" width="100" show-overflow-tooltip />
        <el-table-column label="指导老师" prop="advisor" align="center" width="100" show-overflow-tooltip />
        <el-table-column label="开始时间" prop="startDate" align="center" width="120">
          <template #default="{ row }">
            {{ parseDate(row.startDate) }}
          </template>
        </el-table-column>
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
        <el-table-column label="院长审批" prop="deanStatus" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.deanStatus)" size="small">
              {{ getStatusText(row.deanStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" prop="submitTime" align="center" width="160">
          <template #default="{ row }">
            {{ parseDate(row.submitTime) }}
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

    <!-- 新增/详情对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="700px" append-to-body>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" v-if="!isView">
        <el-row>
          <el-col :span="12">
            <el-form-item label="项目类型" prop="projectType">
              <el-radio-group v-model="form.projectType">
                <el-radio :value="1">创新项目</el-radio>
                <el-radio :value="2">创业项目</el-radio>
                <el-radio :value="3">竞赛</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目级别" prop="projectLevel">
              <el-select v-model="form.projectLevel" placeholder="请选择">
                <el-option label="国家级" :value="1" />
                <el-option label="省级" :value="2" />
                <el-option label="市级" :value="3" />
                <el-option label="校级" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="项目名称" prop="projectName">
              <el-input v-model="form.projectName" placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="负责人" prop="leader">
              <el-input v-model="form.leader" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="指导老师" prop="advisor">
              <el-input v-model="form.advisor" placeholder="请输入指导老师" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startDate">
              <el-date-picker v-model="form.startDate" type="date" placeholder="选择开始时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endDate">
              <el-date-picker v-model="form.endDate" type="date" placeholder="选择结束时间" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="项目描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入项目描述" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="项目成果" prop="achievements">
              <el-input v-model="form.achievements" type="textarea" :rows="3" placeholder="请输入项目成果/获奖情况" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 详情展示 -->
      <el-descriptions :column="1" border v-if="isView && currentRow">
        <el-descriptions-item label="学号">{{ currentRow.studentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentRow.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="项目类型">
          <el-tag :type="getTypeTagType(currentRow.projectType)">{{ getTypeName(currentRow.projectType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="项目名称">{{ currentRow.projectName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="项目级别">{{ getLevelName(currentRow.projectLevel) }}</el-descriptions-item>
        <el-descriptions-item label="项目编号">{{ currentRow.projectNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ currentRow.leader || '-' }}</el-descriptions-item>
        <el-descriptions-item label="参与成员">{{ currentRow.members || '-' }}</el-descriptions-item>
        <el-descriptions-item label="指导老师">{{ currentRow.advisor || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ parseDate(currentRow.startDate) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ parseDate(currentRow.endDate) }}</el-descriptions-item>
        <el-descriptions-item label="项目描述">
          <div style="white-space: pre-wrap">{{ currentRow.description || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="项目成果">
          <div style="white-space: pre-wrap">{{ currentRow.achievements || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="获奖等级">{{ getAwardLevelName(currentRow.awardLevel) }}</el-descriptions-item>
        <el-descriptions-item label="资助金额">{{ currentRow.fundingAmount ? currentRow.fundingAmount + ' 元' : '-' }}</el-descriptions-item>
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
        <el-descriptions-item label="提交时间">{{ parseDate(currentRow.submitTime) }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ isView ? '关闭' : '取消' }}</el-button>
          <template v-if="!isView">
            <el-button type="primary" @click="handleSubmit">确定</el-button>
          </template>
          <template v-if="isView && currentRow && canApprove(currentRow)">
            <el-button type="danger" @click="openRejectDialog">拒绝</el-button>
            <el-button type="primary" @click="handleApprove(currentRow)">通过</el-button>
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

<script setup name="InnovationProject">
import { ref, reactive, getCurrentInstance, toRefs } from 'vue'
import { listInnovation, getInnovationDetail, submitInnovation, approveInnovationMentor, approveInnovationSecretary, approveInnovationDean, delInnovation } from '@/api/academic'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const showSearch = ref(true)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const approvalDialogVisible = ref(false)
const dialogTitle = ref('新增项目')
const isView = ref(false)
const currentRow = ref(null)
const approvalType = ref('approve')
const approvalFormRef = ref(null)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    projectType: undefined,
    status: undefined
  },
  form: {},
  rules: {
    projectType: [{ required: true, message: '请选择项目类型', trigger: 'change' }],
    projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
    leader: [{ required: true, message: '请输入负责人', trigger: 'blur' }],
    advisor: [{ required: true, message: '请输入指导老师', trigger: 'blur' }],
    startDate: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
    description: [{ required: true, message: '请输入项目描述', trigger: 'blur' }]
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

const { queryParams, form, rules, approvalForm, approvalRules } = toRefs(data)

function getTypeTagType(type) {
  const typeMap = { 1: 'primary', 2: 'success', 3: 'warning' }
  return typeMap[type] || 'info'
}

function getTypeName(type) {
  const textMap = { 1: '创新项目', 2: '创业项目', 3: '竞赛' }
  return textMap[type] || '-'
}

function getLevelName(level) {
  const textMap = { 1: '国家级', 2: '省级', 3: '市级', 4: '校级' }
  return textMap[level] || '-'
}

function getAwardLevelName(level) {
  const textMap = { 1: '特等奖', 2: '一等奖', 3: '二等奖', 4: '三等奖', 5: '优秀奖' }
  return textMap[level] || '-'
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
  return date.toLocaleDateString('zh-CN')
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
  if (!roleId) return false

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
    return row.mentorStatus === 1 && row.secretaryStatus === 1 && (row.deanStatus === 0 || row.deanStatus === null || row.deanStatus === undefined)
  }

  return false
}

function getList() {
  loading.value = true
  listInnovation(queryParams.value).then(res => {
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
  queryParams.value.projectType = undefined
  queryParams.value.status = undefined
  handleQuery()
}

function reset() {
  form.value = {
    id: undefined,
    studentId: undefined,
    projectType: undefined,
    projectName: undefined,
    projectLevel: undefined,
    projectNo: undefined,
    leader: undefined,
    members: undefined,
    advisor: undefined,
    startDate: new Date(),
    endDate: undefined,
    description: undefined,
    achievements: undefined,
    awardLevel: undefined,
    fundingAmount: undefined
  }
  isView.value = false
  proxy.resetForm('formRef')
}

function handleAdd() {
  reset()
  dialogTitle.value = '新增项目'
  dialogVisible.value = true
}

function handleView(row) {
  currentRow.value = row
  isView.value = true
  approvalType.value = 'approve'
  approvalForm.value.id = row.id
  approvalForm.value.comment = ''
  dialogTitle.value = '项目详情'
  dialogVisible.value = true
}

function handleApprove(row) {
  currentRow.value = row
  approvalType.value = 'approve'
  approvalForm.value.id = row.id
  approvalForm.value.comment = ''
  dialogVisible.value = true
}

function handleReject(row) {
  currentRow.value = row
  approvalType.value = 'reject'
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

  const roleId = getCurrentUserRoleId()
  let approveApi

  // 老师/导师(7)或任课教师(8)使用导师审批API
  if (roleId === 7 || roleId === 8) {
    approveApi = approveInnovationMentor
  }
  // 研究生秘书(5)或综合管理员(4)使用秘书审批API
  else if (roleId === 5 || roleId === 4) {
    approveApi = approveInnovationSecretary
  }
  // 其他角色（分管院长2、超级管理员1）使用院长审批API
  else {
    approveApi = approveInnovationDean
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

function handleSubmit() {
  proxy.$refs['formRef'].validate(valid => {
    if (valid) {
      submitInnovation(form.value).then(() => {
        proxy.$modal.msgSuccess('提交成功')
        dialogVisible.value = false
        getList()
      }).catch(() => {
        proxy.$modal.msgError('提交失败')
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
