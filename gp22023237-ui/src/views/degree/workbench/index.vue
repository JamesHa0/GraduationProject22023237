<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 待我审批 Tab -->
      <el-tab-pane label="待我审批" name="pending">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">待审批列表</span>
              <el-badge :value="pendingList.length" :max="99" type="danger" />
            </div>
          </template>

          <el-form :inline="true" :model="pendingQuery" style="margin-bottom: 16px">
            <el-form-item label="流程类型">
              <el-select v-model="pendingQuery.processType" placeholder="全部" clearable style="width: 140px">
                <el-option v-for="step in processSteps" :key="step.type" :label="step.label" :value="step.type" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="loadPendingList">搜索</el-button>
              <el-button icon="Refresh" @click="resetPendingQuery">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="pendingLoading" :data="filteredList('pending')" border>
            <el-table-column label="学号" align="center" width="110">
              <template #default="{ row }">{{ row.thesisMain?.studentNo || '-' }}</template>
            </el-table-column>
            <el-table-column label="姓名" align="center" width="90">
              <template #default="{ row }">{{ row.thesisMain?.studentName || '-' }}</template>
            </el-table-column>
            <el-table-column label="论文题目" align="center" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">{{ row.thesisMain?.thesisTitle || '-' }}</template>
            </el-table-column>
            <el-table-column label="流程类型" align="center" width="110">
              <template #default="{ row }">
                <el-tag size="small">{{ getProcessTypeName(row.processType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="版本" prop="version" align="center" width="70" />
            <el-table-column label="提交时间" align="center" width="160">
              <template #default="{ row }">{{ parseDate(row.submitTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="220" fixed="right">
              <template #default="{ row }">
                <el-button link size="small" type="primary" @click="handleViewDetail(row)">详情</el-button>
                <el-button link size="small" type="success" @click="handleQuickApprove(row)">通过</el-button>
                <el-button link size="small" type="danger" @click="handleQuickReject(row)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!pendingLoading && pendingList.length === 0" description="暂无待审批项" />
        </el-card>
      </el-tab-pane>

      <!-- 已审批 Tab -->
      <el-tab-pane label="已审批" name="approved">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">已审批记录</span>
            </div>
          </template>

          <el-form :inline="true" :model="approvedQuery" style="margin-bottom: 16px">
            <el-form-item label="流程类型">
              <el-select v-model="approvedQuery.processType" placeholder="全部" clearable style="width: 140px">
                <el-option v-for="step in processSteps" :key="step.type" :label="step.label" :value="step.type" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="loadApprovedList">搜索</el-button>
              <el-button icon="Refresh" @click="resetApprovedQuery">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="approvedLoading" :data="filteredList('approved')" border>
            <el-table-column label="学号" align="center" width="110">
              <template #default="{ row }">{{ row.thesisMain?.studentNo || '-' }}</template>
            </el-table-column>
            <el-table-column label="姓名" align="center" width="90">
              <template #default="{ row }">{{ row.thesisMain?.studentName || '-' }}</template>
            </el-table-column>
            <el-table-column label="论文题目" align="center" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">{{ row.thesisMain?.thesisTitle || '-' }}</template>
            </el-table-column>
            <el-table-column label="流程类型" align="center" width="110">
              <template #default="{ row }">
                <el-tag size="small">{{ getProcessTypeName(row.processType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="我的审批" align="center" width="100">
              <template #default="{ row }">
                <el-tag :type="getMyApprovalType(row)" size="small">{{ getMyApprovalText(row) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="环节状态" align="center" width="100">
              <template #default="{ row }">
                <el-tag :type="getProcessStatusType(row.processStatus)" size="small">
                  {{ getProcessStatusText(row.processStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="提交时间" align="center" width="160">
              <template #default="{ row }">{{ parseDate(row.submitTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="80">
              <template #default="{ row }">
                <el-button link size="small" type="primary" @click="handleViewDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!approvedLoading && approvedList.length === 0" description="暂无已审批记录" />
        </el-card>
      </el-tab-pane>

      <!-- 我的学生 Tab -->
      <el-tab-pane label="我的学生" name="myStudents">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">我指导的学生</span>
              <el-button type="primary" size="small" icon="Refresh" @click="loadMyStudents">刷新</el-button>
            </div>
          </template>

          <el-table v-loading="studentsLoading" :data="studentsList" border>
            <el-table-column label="学号" prop="studentNo" align="center" width="120" />
            <el-table-column label="姓名" prop="studentName" align="center" width="100" />
            <el-table-column label="专业" prop="major" align="center" width="120" />
            <el-table-column label="论文题目" align="center" show-overflow-tooltip min-width="200">
              <template #default="{ row }">{{ row.thesisTitle || '未填写' }}</template>
            </el-table-column>
            <el-table-column label="论文状态" align="center" width="100">
              <template #default="{ row }">
                <el-tag :type="getFinalResultType(row.finalResult)" size="small">
                  {{ getFinalResultText(row.finalResult) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="归档状态" align="center" width="100">
              <template #default="{ row }">
                <el-tag :type="row.archiveStatus === 1 ? 'success' : 'info'" size="small">
                  {{ row.archiveStatus === 1 ? '已归档' : '未归档' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="150">
              <template #default="{ row }">
                <el-button link size="small" type="primary" @click="viewStudentProgress(row)">流程详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <pagination
            v-show="studentsTotal > 0"
            :total="studentsTotal"
            v-model:page="studentsQuery.pageNum"
            v-model:limit="studentsQuery.pageSize"
            @pagination="loadMyStudents"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 右侧抽屉：审批详情 -->
    <el-drawer v-model="detailDrawerVisible" title="审批详情" size="520px" append-to-body>
      <template v-if="currentRow">
        <el-descriptions :column="2" border style="margin-bottom: 20px">
          <el-descriptions-item label="学号">{{ currentRow.thesisMain?.studentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentRow.thesisMain?.studentName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="论文题目" :span="2">{{ currentRow.thesisMain?.thesisTitle || '-' }}</el-descriptions-item>
          <el-descriptions-item label="流程类型">{{ getProcessTypeName(currentRow.processType) }}</el-descriptions-item>
          <el-descriptions-item label="版本">{{ currentRow.version }}</el-descriptions-item>
          <el-descriptions-item label="导师审批">
            <el-tag :type="getApprovalStatusType(currentRow.supervisorStatus)" size="small">
              {{ getApprovalStatusText(currentRow.supervisorStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="秘书审批">
            <el-tag :type="getApprovalStatusType(currentRow.secretaryStatus)" size="small">
              {{ getApprovalStatusText(currentRow.secretaryStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="院长审批">
            <el-tag :type="getApprovalStatusType(currentRow.deanStatus)" size="small">
              {{ getApprovalStatusText(currentRow.deanStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="环节状态">
            <el-tag :type="getProcessStatusType(currentRow.processStatus)" size="small">
              {{ getProcessStatusText(currentRow.processStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ parseDate(currentRow.submitTime) }}</el-descriptions-item>
        </el-descriptions>

        <!-- 附件 -->
        <div v-if="currentRow.thesisVersionUrl" style="margin-bottom: 16px">
          <el-text tag="div" style="margin-bottom: 8px; font-weight: bold">论文版本</el-text>
          <PdfPreview :url="currentRow.thesisVersionUrl" button-text="查看论文" />
        </div>
        <div v-if="currentRow.attachmentUrl" style="margin-bottom: 16px">
          <el-text tag="div" style="margin-bottom: 8px; font-weight: bold">附件</el-text>
          <div v-for="(url, idx) in parseFileList(currentRow.attachmentUrl)" :key="idx">
            <el-link type="primary" :href="url" target="_blank">附件 {{ idx + 1 }}</el-link>
          </div>
        </div>

        <!-- 审批操作区 -->
        <el-divider />
        <div v-if="canApproveRow(currentRow)">
          <el-text tag="div" style="margin-bottom: 12px; font-weight: bold">审批操作</el-text>
          <el-input
            v-model="approvalComment"
            type="textarea"
            :rows="3"
            placeholder="请输入审批意见（可选）"
            style="margin-bottom: 12px"
          />
          <div style="display: flex; gap: 8px; justify-content: flex-end">
            <el-button @click="detailDrawerVisible = false">取消</el-button>
            <el-button type="danger" @click="submitDrawerApproval('reject')">拒绝</el-button>
            <el-button type="primary" @click="submitDrawerApproval('approve')">通过</el-button>
          </div>
        </div>
      </template>
    </el-drawer>

    <!-- 学生流程详情抽屉 -->
    <el-drawer v-model="studentDrawerVisible" :title="`${currentStudent.studentName} - 论文流程进度`" size="600px" append-to-body>
      <el-steps :active="studentActiveStep" align-center style="margin-bottom: 20px">
        <el-step v-for="step in processSteps.slice(0, 5)" :key="step.type" :title="step.label" :status="getStudentStepStatus(step.type)" />
      </el-steps>

      <el-descriptions :column="2" border style="margin-bottom: 20px" v-if="currentStudent">
        <el-descriptions-item label="学号">{{ currentStudent.studentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentStudent.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="论文题目" :span="2">{{ currentStudent.thesisTitle || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-collapse v-model="studentProcessActiveNames">
        <el-collapse-item v-for="step in processSteps" :key="step.type" :title="step.label" :name="step.type">
          <div v-if="studentProcessMap[step.type] && studentProcessMap[step.type].length > 0">
            <el-table :data="studentProcessMap[step.type]" border size="small">
              <el-table-column label="版本" prop="version" width="60" align="center" />
              <el-table-column label="导师" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="getApprovalStatusType(row.supervisorStatus)" size="small">
                    {{ getApprovalStatusText(row.supervisorStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="秘书" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="getApprovalStatusType(row.secretaryStatus)" size="small">
                    {{ getApprovalStatusText(row.secretaryStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="院长" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="getApprovalStatusType(row.deanStatus)" size="small">
                    {{ getApprovalStatusText(row.deanStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="getProcessStatusType(row.processStatus)" size="small">
                    {{ getProcessStatusText(row.processStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="时间" width="150" align="center">
                <template #default="{ row }">{{ parseDate(row.submitTime) }}</template>
              </el-table-column>
            </el-table>
          </div>
          <el-text type="info" size="small" v-else>暂无记录</el-text>
        </el-collapse-item>
      </el-collapse>
    </el-drawer>

    <!-- 审批确认对话框 -->
    <el-dialog v-model="approvalDialogVisible" :title="approvalAction === 'approve' ? '通过确认' : '拒绝确认'" width="450px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="approvalComment" type="textarea" :rows="3" :placeholder="approvalAction === 'approve' ? '审批意见（可选）' : '请输入拒绝原因'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button :type="approvalAction === 'approve' ? 'primary' : 'danger'" @click="submitApproval">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MentorWorkbench">
import { ref, reactive, computed, onMounted, toRefs } from 'vue'
import { listProcess, listThesisMain, approveProcessSupervisor, approveProcessSecretary } from '@/api/degree'
import useUserStore from '@/store/modules/user'
import {
  getApprovalStatusText, getApprovalStatusType,
  getProcessStatusText, getProcessStatusType,
  getFinalResultText, getFinalResultType,
  parseDate, parseFileList, getStepStatus
} from '@/composables/useDegreeStatus'
import { getCurrentUserRoleId } from '@/composables/useDegreeApproval'

const userStore = useUserStore()
const activeTab = ref('pending')

const processSteps = [
  { type: 1, label: '开题报告' },
  { type: 2, label: '中期检查' },
  { type: 3, label: '预答辩' },
  { type: 4, label: '论文外审' },
  { type: 5, label: '正式答辩' },
  { type: 6, label: '二次答辩' },
  { type: 7, label: '修改后再审' }
]

// ==================== 待审批 ====================
const pendingLoading = ref(false)
const pendingList = ref([])
const pendingQuery = reactive({ processType: undefined })

// ==================== 已审批 ====================
const approvedLoading = ref(false)
const approvedList = ref([])
const approvedQuery = reactive({ processType: undefined })

// ==================== 我的学生 ====================
const studentsLoading = ref(false)
const studentsList = ref([])
const studentsTotal = ref(0)
const studentsQuery = reactive({ pageNum: 1, pageSize: 10 })

// ==================== 详情/审批 ====================
const detailDrawerVisible = ref(false)
const currentRow = ref(null)
const approvalComment = ref('')
const approvalAction = ref('approve')
const approvalDialogVisible = ref(false)
const approvalTargetRow = ref(null)

// ==================== 学生流程详情 ====================
const studentDrawerVisible = ref(false)
const currentStudent = ref({})
const studentProcessMap = ref({})
const studentProcessActiveNames = ref([1, 2, 3])

const roleId = computed(() => getCurrentUserRoleId())

// ==================== 工具函数 ====================

function getProcessTypeName(type) {
  const step = processSteps.find(s => s.type === type)
  return step ? step.label : '未知'
}

function filteredList(tab) {
  const list = tab === 'pending' ? pendingList.value : approvedList.value
  const query = tab === 'pending' ? pendingQuery : approvedQuery
  if (!query.processType) return list
  return list.filter(item => item.processType === query.processType)
}

function getMyApprovalType(row) {
  if (roleId.value === 7 || roleId.value === 8) {
    return row.supervisorStatus === 1 ? 'success' : row.supervisorStatus === 2 ? 'danger' : 'warning'
  }
  if (roleId.value === 5) {
    return row.secretaryStatus === 1 ? 'success' : row.secretaryStatus === 2 ? 'danger' : 'warning'
  }
  if (roleId.value === 2 || roleId.value === 1) {
    return row.deanStatus === 1 ? 'success' : row.deanStatus === 2 ? 'danger' : 'warning'
  }
  return 'info'
}

function getMyApprovalText(row) {
  if (roleId.value === 7 || roleId.value === 8) {
    return getApprovalStatusText(row.supervisorStatus)
  }
  if (roleId.value === 5) {
    return getApprovalStatusText(row.secretaryStatus)
  }
  if (roleId.value === 2 || roleId.value === 1) {
    return getApprovalStatusText(row.deanStatus)
  }
  return '-'
}

function canApproveRow(row) {
  if (!row) return false
  if (roleId.value === 7 || roleId.value === 8) return row.supervisorStatus === 0
  if (roleId.value === 5) return row.secretaryStatus === 0 && row.supervisorStatus === 1
  if (roleId.value === 2 || roleId.value === 1) return row.deanStatus === 0 && row.secretaryStatus === 1
  return false
}

// ==================== 数据加载 ====================

function loadPendingList() {
  pendingLoading.value = true
  // 加载所有流程类型的待审批项
  const promises = processSteps.map(step =>
    listProcess({ processType: step.type, processStatus: 1, pageSize: 200 }).then(res => {
      return (res.data.records || res.data || []).map(item => {
        if (item.contentExtend) {
          try {
            item.contentExtend = typeof item.contentExtend === 'string' ? JSON.parse(item.contentExtend) : item.contentExtend
          } catch (e) { /* ignore */ }
        }
        return item
      })
    }).catch(() => [])
  )

  Promise.all(promises).then(results => {
    let allItems = results.flat()
    // 根据角色过滤真正需要审批的
    allItems = allItems.filter(item => canApproveRow(item))
    pendingList.value = allItems
  }).finally(() => {
    pendingLoading.value = false
  })
}

function loadApprovedList() {
  approvedLoading.value = true
  // 加载我已审批的（不限制状态，让前端过滤）
  const promises = processSteps.map(step =>
    listProcess({ processType: step.type, pageSize: 200 }).then(res => {
      return (res.data.records || res.data || []).map(item => {
        if (item.contentExtend) {
          try {
            item.contentExtend = typeof item.contentExtend === 'string' ? JSON.parse(item.contentExtend) : item.contentExtend
          } catch (e) { /* ignore */ }
        }
        return item
      })
    }).catch(() => [])
  )

  Promise.all(promises).then(results => {
    let allItems = results.flat()
    // 过滤：我已审批过的（不是待审批的）
    allItems = allItems.filter(item => !canApproveRow(item) && item.processStatus !== 0)
    approvedList.value = allItems
  }).finally(() => {
    approvedLoading.value = false
  })
}

function loadMyStudents() {
  studentsLoading.value = true
  listThesisMain({
    pageNum: studentsQuery.pageNum,
    pageSize: studentsQuery.pageSize,
    supervisorId: userStore.user?.id
  }).then(res => {
    studentsList.value = res.data.records || res.data || []
    studentsTotal.value = res.data.total || studentsList.value.length
  }).finally(() => {
    studentsLoading.value = false
  })
}

function resetPendingQuery() {
  pendingQuery.processType = undefined
}

function resetApprovedQuery() {
  approvedQuery.processType = undefined
}

// ==================== 操作 ====================

function handleViewDetail(row) {
  currentRow.value = row
  approvalComment.value = ''
  detailDrawerVisible.value = true
}

function handleQuickApprove(row) {
  approvalTargetRow.value = row
  approvalAction.value = 'approve'
  approvalComment.value = ''
  approvalDialogVisible.value = true
}

function handleQuickReject(row) {
  approvalTargetRow.value = row
  approvalAction.value = 'reject'
  approvalComment.value = ''
  approvalDialogVisible.value = true
}

function submitDrawerApproval(action) {
  approvalTargetRow.value = currentRow.value
  approvalAction.value = action
  doApproval()
}

function submitApproval() {
  if (approvalAction.value === 'reject' && !approvalComment.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  doApproval()
}

function doApproval() {
  const row = approvalTargetRow.value
  const status = approvalAction.value === 'approve' ? 1 : 2
  const userId = userStore.user?.id
  const comment = approvalComment.value

  let approvalFn
  if (roleId.value === 7 || roleId.value === 8) {
    approvalFn = approveProcessSupervisor
  } else if (roleId.value === 5) {
    approvalFn = approveProcessSecretary
  } else if (roleId.value === 2 || roleId.value === 1) {
    approvalFn = approveProcessDean
  }

  if (!approvalFn) return

  approvalFn(row.id, status, comment, userId).then(() => {
    ElMessage.success(approvalAction.value === 'approve' ? '审批通过' : '已拒绝')
    approvalDialogVisible.value = false
    detailDrawerVisible.value = false
    loadPendingList()
    loadApprovedList()
  }).catch(() => {
    ElMessage.error('审批失败')
  })
}

// ==================== 学生流程详情 ====================

const studentActiveStep = computed(() => {
  let step = 0
  for (const s of processSteps) {
    const records = studentProcessMap.value[s.type] || []
    const hasPassed = records.some(r => r.processStatus === 3 || r.processStatus === 5)
    if (hasPassed) step = s.type
    else break
  }
  return step
})

function getStudentStepStatus(type) {
  const records = studentProcessMap.value[type] || []
  return getStepStatus(records)
}

function viewStudentProcess(row) {
  currentStudent.value = row
  studentProcessMap.value = {}
  studentDrawerVisible.value = true

  const promises = processSteps.map(step =>
    listProcess({ thesisId: row.id, processType: step.type, pageSize: 100 }).then(res => {
      studentProcessMap.value[step.type] = res.data.records || res.data || []
    })
  )
  Promise.all(promises)
}

// ==================== 初始化 ====================

import { ElMessage } from 'element-plus'

onMounted(() => {
  loadPendingList()
  loadApprovedList()
  loadMyStudents()
})
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
