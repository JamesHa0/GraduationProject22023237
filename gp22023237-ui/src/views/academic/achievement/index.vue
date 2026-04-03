<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">学术成果管理</span>
          <el-button type="primary" size="small" @click="handleAdd">新增成果</el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
        <el-form-item label="成果类型" prop="achievementType">
          <el-select v-model="queryParams.achievementType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="论文" :value="1" />
            <el-option label="专利" :value="2" />
            <el-option label="科研奖励" :value="3" />
            <el-option label="项目参与" :value="4" />
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
        <el-table-column label="成果类型" prop="achievementType" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.achievementType)">{{ getTypeName(row.achievementType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="成果标题" prop="title" align="center" show-overflow-tooltip min-width="180" />
        <el-table-column label="作者" prop="authors" align="center" width="120" show-overflow-tooltip />
        <el-table-column label="发表/授权时间" prop="publicationDate" align="center" width="120">
          <template #default="{ row }">
            {{ parseDate(row.publicationDate) }}
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
          <el-col :span="24">
            <el-form-item label="成果类型" prop="achievementType">
              <el-radio-group v-model="form.achievementType">
                <el-radio :value="1">论文</el-radio>
                <el-radio :value="2">专利</el-radio>
                <el-radio :value="3">科研奖励</el-radio>
                <el-radio :value="4">项目参与</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="成果标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入成果标题" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="作者" prop="authors">
              <el-input v-model="form.authors" placeholder="请输入作者" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发表/授权时间" prop="publicationDate">
              <el-date-picker v-model="form.publicationDate" type="date" placeholder="选择时间" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="摘要/描述" prop="abstractContent">
              <el-input v-model="form.abstractContent" type="textarea" :rows="4" placeholder="请输入摘要或描述" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 详情展示 -->
      <el-descriptions :column="1" border v-if="isView && currentRow">
        <el-descriptions-item label="学号">{{ currentRow.studentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentRow.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="成果类型">
          <el-tag :type="getTypeTagType(currentRow.achievementType)">{{ getTypeName(currentRow.achievementType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="成果标题">{{ currentRow.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{ currentRow.authors || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发表/授权时间">{{ parseDate(currentRow.publicationDate) }}</el-descriptions-item>
        <template v-if="currentRow.achievementType === 1">
          <el-descriptions-item label="期刊名称">{{ currentRow.journalName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="期刊级别">{{ getJournalLevelName(currentRow.journalLevel) }}</el-descriptions-item>
          <el-descriptions-item label="卷号/期号">{{ (currentRow.volume || '-') + ' / ' + (currentRow.issue || '-') }}</el-descriptions-item>
          <el-descriptions-item label="页码">{{ currentRow.pages || '-' }}</el-descriptions-item>
          <el-descriptions-item label="DOI">{{ currentRow.doi || '-' }}</el-descriptions-item>
        </template>
        <template v-if="currentRow.achievementType === 2">
          <el-descriptions-item label="专利号">{{ currentRow.patentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="专利类型">{{ getPatentTypeName(currentRow.patentType) }}</el-descriptions-item>
          <el-descriptions-item label="授权状态">{{ getPatentStatusName(currentRow.patentStatus) }}</el-descriptions-item>
        </template>
        <template v-if="currentRow.achievementType === 3">
          <el-descriptions-item label="奖励名称">{{ currentRow.awardName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="奖励级别">{{ getAwardLevelName(currentRow.awardLevel) }}</el-descriptions-item>
          <el-descriptions-item label="发奖单位">{{ currentRow.awardIssuer || '-' }}</el-descriptions-item>
        </template>
        <template v-if="currentRow.achievementType === 4">
          <el-descriptions-item label="项目名称">{{ currentRow.projectName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="项目角色">{{ getProjectRoleName(currentRow.projectRole) }}</el-descriptions-item>
        </template>
        <el-descriptions-item label="摘要/描述">
          <div style="white-space: pre-wrap">{{ currentRow.abstractContent || '-' }}</div>
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

<script setup name="AcademicAchievement">
import { ref, reactive, getCurrentInstance, toRefs } from 'vue'
import { listAchievement, getAchievementDetail, submitAchievement, approveAchievementMentor, approveAchievementSecretary, approveAchievementDean, delAchievement } from '@/api/academic'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const showSearch = ref(true)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const approvalDialogVisible = ref(false)
const dialogTitle = ref('新增成果')
const isView = ref(false)
const currentRow = ref(null)
const approvalType = ref('approve')
const approvalFormRef = ref(null)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    achievementType: undefined,
    status: undefined
  },
  form: {},
  rules: {
    achievementType: [{ required: true, message: '请选择成果类型', trigger: 'change' }],
    title: [{ required: true, message: '请输入成果标题', trigger: 'blur' }],
    authors: [{ required: true, message: '请输入作者', trigger: 'blur' }],
    publicationDate: [{ required: true, message: '请选择发表/授权时间', trigger: 'change' }],
    abstractContent: [{ required: true, message: '请输入摘要或描述', trigger: 'blur' }]
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
  const typeMap = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'info' }
  return typeMap[type] || 'info'
}

function getTypeName(type) {
  const textMap = { 1: '论文', 2: '专利', 3: '科研奖励', 4: '项目参与' }
  return textMap[type] || '-'
}

function getJournalLevelName(level) {
  const textMap = { 1: 'SCI/EI', 2: '核心期刊', 3: '普通期刊' }
  return textMap[level] || '-'
}

function getPatentTypeName(type) {
  const textMap = { 1: '发明专利', 2: '实用新型', 3: '外观设计' }
  return textMap[type] || '-'
}

function getPatentStatusName(status) {
  const textMap = { 0: '申请中', 1: '已授权' }
  return textMap[status] || '-'
}

function getAwardLevelName(level) {
  const textMap = { 1: '国家级', 2: '省级', 3: '市级', 4: '校级' }
  return textMap[level] || '-'
}

function getProjectRoleName(role) {
  const textMap = { 1: '负责人', 2: '核心成员', 3: '参与者' }
  return textMap[role] || '-'
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
  listAchievement(queryParams.value).then(res => {
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
  queryParams.value.achievementType = undefined
  queryParams.value.status = undefined
  handleQuery()
}

function reset() {
  form.value = {
    id: undefined,
    studentId: undefined,
    achievementType: undefined,
    title: undefined,
    authors: undefined,
    publicationDate: new Date(),
    journalName: undefined,
    journalLevel: undefined,
    volume: undefined,
    issue: undefined,
    pages: undefined,
    doi: undefined,
    patentNo: undefined,
    patentType: undefined,
    patentStatus: undefined,
    awardName: undefined,
    awardLevel: undefined,
    awardIssuer: undefined,
    projectName: undefined,
    projectRole: undefined,
    abstractContent: undefined,
    attachmentPath: undefined
  }
  isView.value = false
  proxy.resetForm('formRef')
}

function handleAdd() {
  reset()
  dialogTitle.value = '新增成果'
  dialogVisible.value = true
}

function handleView(row) {
  currentRow.value = row
  isView.value = true
  approvalType.value = 'approve'
  approvalForm.value.id = row.id
  approvalForm.value.comment = ''
  dialogTitle.value = '成果详情'
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
    approveApi = approveAchievementMentor
  }
  // 研究生秘书(5)或综合管理员(4)使用秘书审批API
  else if (roleId === 5 || roleId === 4) {
    approveApi = approveAchievementSecretary
  }
  // 其他角色（分管院长2、超级管理员1）使用院长审批API
  else {
    approveApi = approveAchievementDean
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
      submitAchievement(form.value).then(() => {
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
