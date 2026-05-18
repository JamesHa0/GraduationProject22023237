<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header><span>流程审批</span></template>

      <!-- 筛选栏 -->
      <el-form :inline="true" :model="queryForm" style="margin-bottom: 16px;">
        <el-form-item label="流程类型">
          <el-select v-model="queryForm.processType" placeholder="全部" clearable>
            <el-option v-for="opt in PROCESS_TYPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态">
          <el-select v-model="queryForm.processStatus" placeholder="全部" clearable>
            <el-option label="审批中" :value="1" />
            <el-option label="评审中" :value="2" />
            <el-option label="已通过" :value="3" />
            <el-option label="已拒绝" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
      </el-form>

      <!-- 审批表格 -->
      <el-table :data="records" v-loading="loading" stripe border>
        <el-table-column prop="thesisId" label="论文ID" width="80" />
        <el-table-column label="流程类型" width="110">
          <template #default="{ row }">
            <el-tag>{{ getProcessTypeLabel(row.processType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="课题/内容" min-width="180">
          <template #default="{ row }">
            {{ getContentSummary(row) }}
          </template>
        </el-table-column>
        <el-table-column label="流程状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getProcessStatusType(row.processStatus)" size="small">
              {{ getProcessStatusText(row.processStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="导师审批" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusType(row.supervisorStatus)" size="small">
              {{ getApprovalStatusText(row.supervisorStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="秘书审批" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusType(row.secretaryStatus)" size="small">
              {{ getApprovalStatusText(row.secretaryStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="院长审批" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusType(row.deanStatus)" size="small">
              {{ getApprovalStatusText(row.deanStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="160">
          <template #default="{ row }">{{ parseDate(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="canApprove(row)">
              <el-button type="success" size="small" @click="handleApprove(row, 1)">通过</el-button>
              <el-button type="danger" size="small" @click="handleApprove(row, 2)">退回</el-button>
            </template>
            <el-button size="small" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 16px;" v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" @current-change="loadData" />

      <!-- 退回原因弹窗 -->
      <el-dialog v-model="rejectVisible" title="退回原因" width="500px">
        <el-form label-width="80px">
          <el-form-item label="退回原因">
            <el-input v-model="rejectComment" type="textarea" :rows="3" placeholder="请输入退回原因" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="rejectVisible = false">取消</el-button>
          <el-button type="danger" @click="doReject">确认退回</el-button>
        </template>
      </el-dialog>

      <!-- 详情弹窗 -->
      <el-dialog v-model="detailVisible" title="流程记录详情" width="650px">
        <el-descriptions :column="2" border v-if="detailRow">
          <el-descriptions-item label="流程类型">{{ getProcessTypeLabel(detailRow.processType) }}</el-descriptions-item>
          <el-descriptions-item label="论文ID">{{ detailRow.thesisId }}</el-descriptions-item>
          <el-descriptions-item label="流程状态">
            <el-tag :type="getProcessStatusType(detailRow.processStatus)">{{ getProcessStatusText(detailRow.processStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="版本">{{ detailRow.version }}</el-descriptions-item>
          <el-descriptions-item label="导师审批">
            <el-tag :type="getApprovalStatusType(detailRow.supervisorStatus)">{{ getApprovalStatusText(detailRow.supervisorStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="秘书审批">
            <el-tag :type="getApprovalStatusType(detailRow.secretaryStatus)">{{ getApprovalStatusText(detailRow.secretaryStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="院长审批">
            <el-tag :type="getApprovalStatusType(detailRow.deanStatus)">{{ getApprovalStatusText(detailRow.deanStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ parseDate(detailRow.submitTime) }}</el-descriptions-item>
        </el-descriptions>
        <!-- contentExtend 解析内容 -->
        <el-divider v-if="detailContent && Object.keys(detailContent).length > 0">环节内容</el-divider>
        <el-descriptions :column="1" border v-if="detailContent && Object.keys(detailContent).length > 0">
          <el-descriptions-item v-for="(val, key) in detailContent" :key="key" :label="CONTENT_FIELD_LABELS[key] || key">
            {{ val || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCurrentInstance } from 'vue'
import { listProcess, approveProcessSecretary, approveProcessDean } from '@/api/degree'
import { PROCESS_TYPE_OPTIONS, parseContentExtend } from '@/views/degree/processConfig'
import { getProcessTypeLabel } from '@/views/degree/processConfig'
import {
  getProcessStatusText, getProcessStatusType,
  getApprovalStatusText, getApprovalStatusType,
  parseDate
} from '@/composables/useDegreeStatus'
import { getCurrentUserRoleId, getCurrentUserId, ROLE, SECRETARY_ROLES, DEAN_ROLES, useProcessApproval } from '@/composables/useDegreeApproval'

const { proxy } = getCurrentInstance()

// 使用 composable 中的 canApprove（已考虑流程配置）
const { canApprove, loadProcessConfig } = useProcessApproval(proxy)

// contentExtend 字段中文标签映射
const CONTENT_FIELD_LABELS = {
  topicName: '课题名称', topicSource: '课题来源', topicDesc: '课题说明', expectedGoal: '预期目标',
  background: '课题背景', mainTask: '主要任务', schedule: '进度安排', references: '参考文献',
  researchStatus: '研究现状', researchContent: '研究内容', researchMethod: '研究方法',
  completedWork: '已完成工作', remainingWork: '未完成工作', problems: '存在问题', nextPlan: '下一步计划',
  draftDesc: '稿件说明', modificationNote: '修改说明',
  defenseDraftDesc: '答辩稿说明', pptAttachment: 'PPT附件'
}

const loading = ref(false)
const records = ref([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const queryForm = ref({ processType: null, processStatus: 1 })

// 退回相关
const rejectVisible = ref(false)
const rejectComment = ref('')
const rejectRow = ref(null)

// 详情相关
const detailVisible = ref(false)
const detailRow = ref(null)
const detailContent = ref({})

/** 获取内容摘要（用于表格显示） */
function getContentSummary(row) {
  const content = parseContentExtend(row)
  if (row.processType === 1) return content.topicName || '-'
  if (row.processType === 2) return content.background ? content.background.substring(0, 50) + '...' : '-'
  if (row.processType === 3) return content.background ? content.background.substring(0, 50) + '...' : '-'
  if (row.processType === 4) return content.completedWork ? content.completedWork.substring(0, 50) + '...' : '-'
  if (row.processType === 5) return content.draftDesc || '-'
  if (row.processType === 6) return content.defenseDraftDesc || '-'
  return row.thesisTitle || '-'
}

/** 获取当前角色对应的审批API */
function getApproveApi() {
  const roleId = getCurrentUserRoleId()
  if (SECRETARY_ROLES.includes(roleId)) return approveProcessSecretary
  return approveProcessDean
}

/** 审批操作 */
function handleApprove(row, status) {
  if (status === 2) {
    rejectRow.value = row
    rejectComment.value = ''
    rejectVisible.value = true
    return
  }
  const approverId = getCurrentUserId()
  getApproveApi()(row.id, 1, '', approverId).then(() => {
    proxy.$modal.msgSuccess('审批通过')
    loadData()
  }).catch(() => {
    proxy.$modal.msgError('审批失败')
  })
}

/** 确认退回 */
function doReject() {
  if (!rejectComment.value.trim()) {
    proxy.$modal.msgWarning('请输入退回原因')
    return
  }
  const approverId = getCurrentUserId()
  getApproveApi()(rejectRow.value.id, 2, rejectComment.value, approverId).then(() => {
    proxy.$modal.msgSuccess('已退回')
    rejectVisible.value = false
    loadData()
  }).catch(() => {
    proxy.$modal.msgError('退回失败')
  })
}

/** 查看详情 */
function handleDetail(row) {
  detailRow.value = row
  detailContent.value = parseContentExtend(row)
  detailVisible.value = true
}

/** 加载数据 */
async function loadData() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (queryForm.value.processType) params.processType = queryForm.value.processType
    if (queryForm.value.processStatus) params.processStatus = queryForm.value.processStatus

    const res = await listProcess(params)
    records.value = res.data || res.rows || []
    total.value = res.pagination?.total || res.total || 0
  } catch (e) { console.error(e) }
  loading.value = false
}

onMounted(() => {
  loadProcessConfig().then(() => loadData())
})
</script>
