<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">{{ currentConfig.name }}</span>
          <div>
            <el-button type="primary" size="small" @click="handleAdd" v-if="isStudent">
              新增
            </el-button>
            <el-button size="small" @click="showVersionHistory = !showVersionHistory">
              {{ showVersionHistory ? '隐藏版本' : '版本历史' }}
            </el-button>
          </div>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
        <el-form-item label="审批状态" prop="processStatus">
          <el-select v-model="queryParams.processStatus" placeholder="请选择" clearable style="width: 140px">
            <el-option v-for="opt in PROCESS_STATUS_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>

      <!-- 版本历史抽屉 -->
      <el-drawer v-model="showVersionHistory" title="版本历史" size="400px" append-to-body>
        <el-timeline v-if="versionList.length > 0">
          <el-timeline-item
            v-for="item in versionList"
            :key="item.id"
            :timestamp="parseDate(item.submitTime)"
            placement="top"
            :type="getProcessStatusType(item.processStatus)"
          >
            <el-card shadow="hover" style="cursor: pointer" @click="handleView(item)">
              <p><strong>版本 {{ item.version }}</strong></p>
              <p>状态: <el-tag :type="getProcessStatusType(item.processStatus)" size="small">{{ getProcessStatusText(item.processStatus) }}</el-tag></p>
              <p v-if="item.thesisVersionUrl">论文: <el-link type="primary" :href="item.thesisVersionUrl" target="_blank">查看</el-link></p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无版本记录" />
      </el-drawer>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="dataList" border>
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="学号" align="center" width="120">
          <template #default="{ row }">
            {{ row.thesisMain?.studentNo || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="姓名" align="center" width="100">
          <template #default="{ row }">
            {{ row.thesisMain?.studentName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="论文题目" align="center" show-overflow-tooltip min-width="180">
          <template #default="{ row }">
            {{ row.thesisMain?.thesisTitle || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="版本" prop="version" align="center" width="70" />
        <el-table-column label="导师审批" prop="supervisorStatus" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusType(row.supervisorStatus)" size="small">
              {{ getApprovalStatusText(row.supervisorStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="秘书审批" prop="secretaryStatus" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusType(row.secretaryStatus)" size="small">
              {{ getApprovalStatusText(row.secretaryStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="院长审批" prop="deanStatus" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusType(row.deanStatus)" size="small">
              {{ getApprovalStatusText(row.deanStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评审结果" align="center" width="100" v-if="currentConfig.canRecordResult">
          <template #default="{ row }">
            <el-tag :type="getReviewResultType(row.reviewResult)" size="small">
              {{ getReviewResultText(row.reviewResult) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="环节状态" prop="processStatus" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getProcessStatusType(row.processStatus)" size="small">
              {{ getProcessStatusText(row.processStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" prop="submitTime" align="center" width="160">
          <template #default="{ row }">
            {{ parseDate(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="260" class-name="small-padding fixed-width">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="handleView(row)">详情</el-button>
            <el-button link size="small" type="success" @click="handleApprove(row)" v-if="canApprove(row)">通过</el-button>
            <el-button link size="small" type="danger" @click="handleReject(row)" v-if="canApprove(row)">拒绝</el-button>
            <el-button link size="small" type="warning" @click="handleRecordResult(row)" v-if="canRecordResult(row)">录入结果</el-button>
            <el-button link size="small" type="primary" @click="handleResubmit(row)" v-if="canResubmit(row)">重新提交</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="800px" append-to-body>
      <el-form :model="form" :rules="currentConfig.rules" ref="formRef" label-width="110px" v-if="!isView">
        <el-row>
          <el-col :span="24">
            <el-form-item label="论文题目" prop="thesisTitle">
              <el-input v-model="form.thesisTitle" placeholder="请输入论文题目" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 动态类型专属表单 -->
        <ProposalForm v-if="processType === 1" :form="form" />
        <MidtermForm v-if="processType === 2" :form="form" />
        <PreDefenseForm v-if="processType === 3" :form="form" />
        <ExternalReviewForm v-if="processType === 4" :form="form" />
        <DefenseForm v-if="processType === 5 || processType === 6" :form="form" />
        <ReSubmissionForm v-if="processType === 7" :form="form" />

        <!-- 通用字段：时间、地点 -->
        <el-row>
          <el-col :span="12">
            <el-form-item label="时间" prop="eventTime">
              <el-date-picker v-model="form.eventTime" type="datetime" placeholder="选择时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地点" prop="eventLocation">
              <el-input v-model="form.eventLocation" placeholder="请输入地点" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 论文版本上传 -->
        <el-row>
          <el-col :span="24">
            <el-form-item label="论文版本" prop="thesisVersionUrl">
              <file-upload v-model="form.thesisVersionUrl" :limit="1" :fileSize="50" :fileType="['doc', 'docx', 'pdf']" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 附件上传 -->
        <el-row>
          <el-col :span="24">
            <el-form-item label="附件" prop="attachmentUrl">
              <file-upload v-model="form.attachmentUrl" :limit="3" :fileSize="50" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 详情展示 -->
      <ProcessDetail v-if="isView && currentRow" :row="currentRow" :process-type="processType" />

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ isView ? '关闭' : '取消' }}</el-button>
          <template v-if="!isView">
            <el-button type="primary" @click="handleSubmit">确定</el-button>
          </template>
          <template v-if="isView && currentRow && canApprove(currentRow)">
            <el-button type="danger" @click="openRejectDialogFromDetail">拒绝</el-button>
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

    <!-- 评审结果录入对话框 -->
    <el-dialog v-model="resultDialogVisible" title="录入评审结果" width="600px" append-to-body>
      <el-form :model="resultForm" :rules="resultRules" ref="resultFormRef" label-width="100px">
        <el-form-item label="评审结果" prop="result">
          <el-select v-model="resultForm.result" placeholder="请选择">
            <el-option v-for="opt in REVIEW_RESULT_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="评分" prop="score">
          <el-input v-model="resultForm.score" placeholder="请输入评分（如：85）" />
        </el-form-item>
        <el-form-item label="评语" prop="comment">
          <el-input v-model="resultForm.comment" type="textarea" :rows="3" placeholder="请输入评语" />
        </el-form-item>
        <el-form-item label="问答记录" prop="qaRecord" v-if="currentConfig.hasQaRecord">
          <el-input v-model="resultForm.qaRecord" type="textarea" :rows="4" placeholder="请输入答辩问答记录" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resultDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitResult">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ThesisProgress">
import { ref, reactive, getCurrentInstance, toRefs, computed, onMounted } from 'vue'
import { listProcess, submitProcess, getThesisMainByStudent, recordProcessResult } from '@/api/degree'
import { useProcessApproval, getCurrentUserRoleId } from '@/composables/useDegreeApproval'
import { getApprovalStatusText, getApprovalStatusType, getProcessStatusText, getProcessStatusType, getReviewResultText, getReviewResultType, parseDate, parseFileList } from '@/composables/useDegreeStatus'
import { PROCESS_CONFIG, PROCESS_STATUS_OPTIONS, REVIEW_RESULT_OPTIONS, buildSubmitData } from '../processConfig'
import useUserStore from '@/store/modules/user'
import FileUpload from '@/components/FileUpload'
import ProposalForm from './components/ProposalForm.vue'
import MidtermForm from './components/MidtermForm.vue'
import PreDefenseForm from './components/PreDefenseForm.vue'
import ExternalReviewForm from './components/ExternalReviewForm.vue'
import DefenseForm from './components/DefenseForm.vue'
import ReSubmissionForm from './components/ReSubmissionForm.vue'
import ProcessDetail from './components/ProcessDetail.vue'
import PdfPreview from '@/components/PdfPreview/index.vue'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()

const {
  approvalDialogVisible, approvalType, approvalFormRef,
  approvalForm, canApprove, openRejectDialog, submitApproval: submitApprovalLogic
} = useProcessApproval(proxy)

const loading = ref(false)
const showSearch = ref(true)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const resultDialogVisible = ref(false)
const dialogTitle = ref('')
const isView = ref(false)
const currentRow = ref(null)
const resultFormRef = ref(null)
const showVersionHistory = ref(false)
const versionList = ref([])

const route = getCurrentInstance().proxy.$route

const processType = computed(() => {
  const path = route.path
  if (path.includes('proposal')) return 1
  if (path.includes('midterm')) return 2
  if (path.includes('predefense')) return 3
  if (path.includes('external-review') || path.includes('externalReview')) return 4
  if (path.includes('second-defense')) return 6
  if (path.includes('re-submission') || path.includes('reSubmission')) return 7
  if (path.includes('defense')) return 5
  return 1
})

const currentConfig = computed(() => PROCESS_CONFIG[processType.value] || PROCESS_CONFIG[1])

const isStudent = computed(() => {
  const roleId = getCurrentUserRoleId()
  return roleId === 6
})

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    processType: undefined,
    processStatus: undefined
  },
  form: {},
  approvalRules: {
    comment: [{ required: false, message: '请输入审批意见', trigger: 'blur' }]
  },
  resultForm: {
    id: undefined,
    result: undefined,
    score: '',
    comment: '',
    qaRecord: ''
  },
  resultRules: {
    result: [{ required: true, message: '请选择评审结果', trigger: 'change' }]
  }
})

const { queryParams, form, approvalRules, resultForm, resultRules } = toRefs(data)

// ==================== 权限判断 ====================

function canRecordResult(row) {
  if (!row) return false
  const roleId = getCurrentUserRoleId()
  if (!roleId) return false
  if (!currentConfig.value.canRecordResult) return false
  if (row.processStatus !== 2) return false
  return roleId === 5 || roleId === 2 || roleId === 1
}

function canResubmit(row) {
  if (!row) return false
  if (!isStudent.value) return false
  // processStatus=4 表示已拒绝/已驳回
  return row.processStatus === 4
}

// ==================== 数据操作 ====================

function getList() {
  loading.value = true
  listProcess({ ...queryParams.value, processType: processType.value }).then(res => {
    loading.value = false
    let list = res.data.records || res.data || []
    list = list.map(item => {
      const itemData = { ...item }
      if (item.contentExtend) {
        try {
          itemData.contentExtend = typeof item.contentExtend === 'string' ? JSON.parse(item.contentExtend) : item.contentExtend
        } catch (e) {
          console.warn('解析contentExtend失败', e)
        }
      }
      return itemData
    })
    dataList.value = list
    total.value = res.data.total || dataList.value.length
    versionList.value = list.sort((a, b) => (b.version || 0) - (a.version || 0))
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
  queryParams.value.processStatus = undefined
  handleQuery()
}

function reset() {
  form.value = { ...currentConfig.value.defaultForm, processType: processType.value }
  isView.value = false
  proxy.resetForm('formRef')
}

function handleAdd() {
  reset()
  const roleId = getCurrentUserRoleId()
  if (roleId === 6 && userStore.user && userStore.user.id) {
    getThesisMainByStudent(userStore.user.id).then(res => {
      if (res.data) {
        form.value.thesisId = res.data.id
        form.value.thesisTitle = res.data.thesisTitle
        const existingVersions = versionList.value.filter(v => v.thesisId === res.data.id)
        form.value.version = existingVersions.length > 0 ? Math.max(...existingVersions.map(v => v.version || 0)) + 1 : 1
      }
    })
  }
  dialogTitle.value = `新增${currentConfig.value.name}`
  dialogVisible.value = true
}

function handleResubmit(row) {
  reset()
  // 预填被驳回记录的数据
  form.value.thesisId = row.thesisId
  form.value.thesisTitle = row.thesisTitle || ''
  // 版本号自动+1
  const existingVersions = versionList.value.filter(v => v.thesisId === row.thesisId && v.processType === row.processType)
  form.value.version = existingVersions.length > 0 ? Math.max(...existingVersions.map(v => v.version || 0)) + 1 : (row.version || 1) + 1
  // 预填环节通用字段
  if (row.eventTime) form.value.eventTime = row.eventTime
  if (row.eventLocation) form.value.eventLocation = row.eventLocation
  // 预填contentExtend中的个性化字段
  if (row.contentExtend) {
    try {
      const ext = typeof row.contentExtend === 'string' ? JSON.parse(row.contentExtend) : row.contentExtend
      Object.assign(form.value, ext)
    } catch (e) {
      console.warn('解析contentExtend失败', e)
    }
  }
  dialogTitle.value = `重新提交${currentConfig.value.name}`
  dialogVisible.value = true
}

function handleView(row) {
  const rowData = { ...row }
  if (row.contentExtend) {
    try {
      rowData.contentExtend = typeof row.contentExtend === 'string' ? JSON.parse(row.contentExtend) : row.contentExtend
    } catch (e) {
      console.warn('解析contentExtend失败', e)
    }
  }
  currentRow.value = rowData
  isView.value = true
  approvalForm.value.id = row.id
  approvalForm.value.comment = ''
  dialogTitle.value = `${currentConfig.value.name}详情`
  dialogVisible.value = true
}

function handleApprove(row) {
  currentRow.value = row
  openApproveDialog(row)
}

function handleReject(row) {
  currentRow.value = row
  openRejectDialog()
}

function openRejectDialogFromDetail() {
  dialogVisible.value = false
  openRejectDialog()
}

function openApproveDialog(row) {
  approvalType.value = 'approve'
  approvalForm.value.id = row.id
  approvalForm.value.comment = ''
  approvalDialogVisible.value = true
}

function handleRecordResult(row) {
  currentRow.value = row
  resultForm.value = {
    id: row.id,
    result: undefined,
    score: '',
    comment: '',
    qaRecord: ''
  }
  resultDialogVisible.value = true
}

// ==================== 提交逻辑 ====================

function submitApproval() {
  submitApprovalLogic(() => {
    approvalDialogVisible.value = false
    dialogVisible.value = false
    getList()
  })
}

function submitResult() {
  resultFormRef.value.validate(valid => {
    if (valid) {
      recordProcessResult(
        resultForm.value.id,
        resultForm.value.result,
        resultForm.value.score,
        resultForm.value.comment,
        resultForm.value.qaRecord
      ).then(() => {
        proxy.$modal.msgSuccess('录入成功')
        resultDialogVisible.value = false
        getList()
      }).catch(() => {
        proxy.$modal.msgError('录入失败')
      })
    }
  })
}

function handleSubmit() {
  proxy.$refs['formRef'].validate(valid => {
    if (valid) {
      const submitData = buildSubmitData(form.value, processType.value)

      // 答辩类环节直接设置 committee 字段
      if (processType.value === 5 || processType.value === 6) {
        submitData.reviewCommitteeChair = form.value.reviewCommitteeChair
        submitData.reviewCommitteeMembers = form.value.reviewCommitteeMembers
      }

      submitProcess(submitData).then(() => {
        proxy.$modal.msgSuccess('提交成功')
        dialogVisible.value = false
        getList()
      }).catch(() => {
        proxy.$modal.msgError('提交失败')
      })
    }
  })
}

onMounted(() => {
  getList()
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
