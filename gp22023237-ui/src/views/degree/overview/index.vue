<template>
  <div class="app-container">
    <!-- 待办事项卡片区 -->
    <el-row :gutter="16" style="margin-bottom: 20px" v-if="isStudent">
      <el-col :span="6" v-for="card in todoCards" :key="card.key">
        <el-card
          shadow="hover"
          class="todo-card"
          :class="'todo-card--' + card.type"
          @click="handleTodoClick(card)"
        >
          <div class="todo-card__inner">
            <div class="todo-card__icon">
              <el-icon :size="32"><component :is="card.icon" /></el-icon>
            </div>
            <div class="todo-card__content">
              <div class="todo-card__count">{{ card.count }}</div>
              <div class="todo-card__label">{{ card.label }}</div>
            </div>
          </div>
          <div class="todo-card__action" v-if="card.count > 0">
            <span>{{ card.actionText }} <el-icon><ArrowRight /></el-icon></span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 论文基本信息卡片 -->
    <el-card shadow="never" style="margin-bottom: 20px">
      <template #header>
        <div class="card-header">
          <span class="card-title">论文信息</span>
          <el-button type="primary" size="small" @click="handleEditThesis" v-if="thesisMain">
            编辑信息
          </el-button>
        </div>
      </template>

      <div v-if="pageLoading" style="padding: 20px 0">
        <el-skeleton :rows="3" animated />
      </div>

      <el-empty v-else-if="!thesisMain" description="暂无论文记录">
        <el-button type="primary" @click="handleEditThesis">填写论文信息</el-button>
      </el-empty>

      <el-descriptions :column="3" border v-else>
        <el-descriptions-item label="学号">{{ thesisMain.studentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ thesisMain.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="导师">{{ thesisMain.supervisorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ thesisMain.major || '-' }}</el-descriptions-item>
        <el-descriptions-item label="年级">{{ thesisMain.grade || '-' }}</el-descriptions-item>
        <el-descriptions-item label="论文结果">
          <el-tag :type="getFinalResultType(thesisMain.finalResult)" size="small">
            {{ getFinalResultText(thesisMain.finalResult) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="论文题目" :span="2">{{ thesisMain.thesisTitle || '-' }}</el-descriptions-item>
        <el-descriptions-item label="归档状态">
          <el-tag :type="thesisMain.archiveStatus === 1 ? 'success' : 'info'" size="small">
            {{ thesisMain.archiveStatus === 1 ? '已归档' : '未归档' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="最终论文" :span="3">
          <el-link v-if="thesisMain.thesisFinalUrl" type="primary" :href="thesisMain.thesisFinalUrl" target="_blank">
            查看最终论文
          </el-link>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 流程进度卡片 - 增强版 -->
    <el-card shadow="never" style="margin-bottom: 20px">
      <template #header>
        <div class="card-header">
          <span class="card-title">论文流程进度</span>
          <el-tag v-if="currentStepLabel" type="primary" effect="plain" size="small">
            当前环节: {{ currentStepLabel }}
          </el-tag>
        </div>
      </template>

      <div v-if="pageLoading" style="padding: 20px 0">
        <el-skeleton :rows="2" animated />
      </div>

      <div v-else-if="!thesisMain">
        <el-empty description="暂无论文记录" />
      </div>

      <div v-else class="progress-steps-wrapper">
        <el-steps :active="activeStep" align-center class="enhanced-steps">
          <el-step
            v-for="step in processSteps"
            :key="step.type"
            :title="step.label"
            :description="getStepDescription(step.type)"
            :status="getStepStatus(step.type)"
            class="step-item"
            @click.native="handleStepClick(step)"
          />
        </el-steps>
        <!-- 当前环节引导 -->
        <div class="step-guide" v-if="nextActionGuide">
          <el-alert :title="nextActionGuide" type="info" :closable="false" show-icon>
            <template #default>
              <el-button type="primary" size="small" @click="goToNextAction">
                立即前往
              </el-button>
            </template>
          </el-alert>
        </div>
      </div>
    </el-card>

    <!-- 各环节详情列表 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">各环节详情</span>
        </div>
      </template>

      <div v-if="pageLoading" style="padding: 10px 0">
        <el-skeleton :rows="5" animated />
      </div>

      <el-empty v-else-if="!thesisMain" description="暂无论文记录" />

      <template v-else>
        <el-collapse v-model="activeNames">
          <el-collapse-item
            v-for="step in processSteps"
            :key="step.type"
            :name="step.type"
          >
            <template #title>
              <div class="collapse-title">
                <span>{{ step.label }}</span>
                <el-tag
                  v-if="processMap[step.type] && processMap[step.type].length > 0"
                  :type="getStepStatus(step.type) === 'success' ? 'success' : getStepStatus(step.type) === 'error' ? 'danger' : getStepStatus(step.type) === 'process' ? 'warning' : 'info'"
                  size="small"
                  style="margin-left: 8px"
                >
                  {{ getStepDescription(step.type) }}
                </el-tag>
              </div>
            </template>

            <div v-if="processMap[step.type] && processMap[step.type].length > 0">
              <el-table :data="processMap[step.type]" border size="small">
                <el-table-column label="版本" prop="version" width="70" align="center" />
                <el-table-column label="导师审批" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getApprovalStatusType(row.supervisorStatus)" size="small">
                      {{ getApprovalStatusText(row.supervisorStatus) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="秘书审批" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getApprovalStatusType(row.secretaryStatus)" size="small">
                      {{ getApprovalStatusText(row.secretaryStatus) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="院长审批" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getApprovalStatusType(row.deanStatus)" size="small">
                      {{ getApprovalStatusText(row.deanStatus) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="评审结果" width="100" align="center" v-if="step.type >= 4">
                  <template #default="{ row }">
                    <el-tag :type="getReviewResultType(row.reviewResult)" size="small">
                      {{ getReviewResultText(row.reviewResult) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="环节状态" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getProcessStatusType(row.processStatus)" size="small">
                      {{ getProcessStatusText(row.processStatus) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="提交时间" width="160" align="center">
                  <template #default="{ row }">
                    {{ parseDate(row.submitTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80" align="center">
                  <template #default="{ row }">
                    <el-button link size="small" type="primary" @click="goToProcessPage(step.type)">查看</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <el-empty v-else :description="`暂无${step.label}记录`" :image-size="60" />
          </el-collapse-item>
        </el-collapse>
      </template>
    </el-card>

    <!-- 编辑论文信息对话框 -->
    <el-dialog title="编辑论文信息" v-model="editDialogVisible" width="600px" append-to-body>
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="论文题目" prop="thesisTitle">
          <el-input v-model="editForm.thesisTitle" placeholder="请输入论文题目" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="editForm.major" placeholder="请输入专业" />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="editForm.grade" placeholder="请输入年级" />
        </el-form-item>
        <el-form-item label="最终论文" prop="thesisFinalUrl">
          <file-upload
            v-model="editForm.thesisFinalUrl"
            :limit="1"
            :fileSize="50"
            :fileType="['doc', 'docx', 'pdf']"
            :disabled="!isFinalStage"
          />
          <div class="form-tip" v-if="!isFinalStage">
            <el-text type="info" size="small">仅定稿阶段可上传最终论文</el-text>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ThesisOverview">
import { ref, reactive, computed, onMounted, toRefs } from 'vue'
import { getThesisMainByStudent, updateThesisMain, listProcess } from '@/api/degree'
import useUserStore from '@/store/modules/user'
import FileUpload from '@/components/FileUpload'
import { useRouter } from 'vue-router'
import {
  getFinalResultText, getFinalResultType,
  getApprovalStatusText, getApprovalStatusType,
  getProcessStatusText, getProcessStatusType,
  getReviewResultText, getReviewResultType,
  parseDate, getLatestProcessStatusText, getStepStatus as _getStepStatus
} from '@/composables/useDegreeStatus'
import { getCurrentUserRoleId } from '@/composables/useDegreeApproval'
import { Document, CircleCheck, CircleClose, EditPen, ArrowRight } from '@element-plus/icons-vue'
import PdfPreview from '@/components/PdfPreview/index.vue'

const router = useRouter()
const userStore = useUserStore()

const thesisMain = ref(null)
const processMap = ref({})
const activeNames = ref([1, 2, 3])
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const pageLoading = ref(true)

const processSteps = [
  { type: 1, label: '开题报告' },
  { type: 2, label: '中期检查' },
  { type: 3, label: '预答辩' },
  { type: 4, label: '论文外审' },
  { type: 5, label: '正式答辩' },
  { type: 6, label: '二次答辩' },
  { type: 7, label: '修改后再审' }
]

const isStudent = computed(() => {
  const roleId = getCurrentUserRoleId()
  return roleId === 6
})

// 是否定稿阶段（答辩已通过或完成后）
const isFinalStage = computed(() => {
  if (!thesisMain.value) return false
  // 论文结果已通过 或 答辩环节有已通过记录
  if (thesisMain.value.finalResult === 1) return true
  const defenseRecords = processMap.value[5] || []
  return defenseRecords.some(r => r.processStatus === 3 || r.processStatus === 5)
})

const data = reactive({
  editForm: {
    id: undefined,
    thesisTitle: '',
    major: '',
    grade: '',
    thesisFinalUrl: ''
  },
  editRules: {
    thesisTitle: [{ required: true, message: '请输入论文题目', trigger: 'blur' }]
  }
})

const { editForm, editRules } = toRefs(data)

// ==================== 待办卡片 ====================

const todoCards = computed(() => {
  if (!thesisMain.value) return []

  const pendingSubmit = []
  const rejected = []
  const pendingDefense = []

  processSteps.forEach(step => {
    const records = processMap.value[step.type] || []
    if (records.length === 0) {
      // 只有在前序环节都已通过时才算待提交
      const prevSteps = processSteps.filter(s => s.type < step.type)
      const prevAllDone = prevSteps.every(s => {
        const recs = processMap.value[s.type] || []
        return recs.some(r => r.processStatus === 3 || r.processStatus === 5)
      })
      if (prevAllDone && step.type <= 5) {
        pendingSubmit.push(step)
      }
    } else {
      const latest = records.reduce((a, b) => (a.version > b.version ? a : b))
      if (latest.processStatus === 4) {
        rejected.push(step)
      }
      if (step.type === 5 && (latest.processStatus === 2 || latest.processStatus === 3)) {
        pendingDefense.push(step)
      }
    }
  })

  return [
    {
      key: 'pendingSubmit',
      label: '待提交',
      count: pendingSubmit.length,
      type: 'warning',
      icon: EditPen,
      actionText: '去提交',
      steps: pendingSubmit
    },
    {
      key: 'rejected',
      label: '被驳回',
      count: rejected.length,
      type: 'danger',
      icon: CircleClose,
      actionText: '去修改',
      steps: rejected
    },
    {
      key: 'pendingDefense',
      label: '待答辩',
      count: pendingDefense.length,
      type: 'success',
      icon: CircleCheck,
      actionText: '查看详情',
      steps: pendingDefense
    },
    {
      key: 'progress',
      label: '进行中',
      count: Object.keys(processMap.value).filter(type => {
        const records = processMap.value[type]
        return records && records.length > 0 && records.some(r => r.processStatus === 1 || r.processStatus === 2)
      }).length,
      type: 'primary',
      icon: Document,
      actionText: '查看进度',
      steps: []
    }
  ]
})

// ==================== 计算当前进度步骤 ====================

const activeStep = computed(() => {
  if (!thesisMain.value) return 0
  let step = 0
  for (const s of processSteps) {
    const records = processMap.value[s.type] || []
    const hasPassed = records.some(r => r.processStatus === 3 || r.processStatus === 5)
    if (hasPassed) step = s.type
    else break
  }
  return step
})

const currentStepLabel = computed(() => {
  const step = processSteps.find(s => s.type === activeStep.value)
  if (!step) return null
  const records = processMap.value[step.type] || []
  if (records.some(r => r.processStatus === 3 || r.processStatus === 5)) {
    // 当前步骤已通过，找下一步
    const nextStep = processSteps.find(s => s.type === activeStep.value + 1)
    return nextStep ? `下一步: ${nextStep.label}` : '全部完成'
  }
  return step.label
})

// 下一步操作引导
const nextActionGuide = computed(() => {
  if (!isStudent.value || !thesisMain.value) return null

  // 找到第一个需要操作的环节
  for (const step of processSteps) {
    const records = processMap.value[step.type] || []
    if (records.length === 0) {
      // 检查前序环节是否都已完成
      const prevSteps = processSteps.filter(s => s.type < step.type)
      const prevAllDone = prevSteps.every(s => {
        const recs = processMap.value[s.type] || []
        return recs.some(r => r.processStatus === 3 || r.processStatus === 5)
      })
      if (prevAllDone && step.type <= 5) {
        return `您需要提交「${step.label}」，点击立即前往`
      }
    } else {
      const latest = records.reduce((a, b) => (a.version > b.version ? a : b))
      if (latest.processStatus === 4) {
        return `「${step.label}」已被驳回，请修改后重新提交`
      }
    }
  }
  return null
})

const nextActionStep = computed(() => {
  if (!isStudent.value || !thesisMain.value) return null
  for (const step of processSteps) {
    const records = processMap.value[step.type] || []
    if (records.length === 0) {
      const prevSteps = processSteps.filter(s => s.type < step.type)
      const prevAllDone = prevSteps.every(s => {
        const recs = processMap.value[s.type] || []
        return recs.some(r => r.processStatus === 3 || r.processStatus === 5)
      })
      if (prevAllDone && step.type <= 5) return step
    } else {
      const latest = records.reduce((a, b) => (a.version > b.version ? a : b))
      if (latest.processStatus === 4) return step
    }
  }
  return null
})

// ==================== 文本映射 ====================

function getStepDescription(type) {
  const records = processMap.value[type] || []
  return getLatestProcessStatusText(records)
}

function getStepStatus(type) {
  const records = processMap.value[type] || []
  return _getStepStatus(records)
}

function goToProcessPage(type) {
  const pathMap = {
    1: '/degree/proposal',
    2: '/degree/midterm',
    3: '/degree/predefense',
    4: '/degree/external-review',
    5: '/degree/defense',
    6: '/degree/second-defense',
    7: '/degree/re-submission'
  }
  router.push(pathMap[type] || '/degree/proposal')
}

function handleStepClick(step) {
  if (isStudent.value) {
    goToProcessPage(step.type)
  }
}

function handleTodoClick(card) {
  if (card.count === 0) return
  if (card.steps && card.steps.length > 0) {
    goToProcessPage(card.steps[0].type)
  }
}

function goToNextAction() {
  if (nextActionStep.value) {
    goToProcessPage(nextActionStep.value.type)
  }
}

// ==================== 数据加载（并行请求） ====================

function loadThesisMain() {
  const userId = userStore.user?.id
  if (!userId) {
    pageLoading.value = false
    return
  }

  pageLoading.value = true
  getThesisMainByStudent(userId).then(res => {
    if (res.data) {
      thesisMain.value = res.data
      loadProcessRecords(res.data.id)
    } else {
      pageLoading.value = false
    }
  }).catch(() => {
    pageLoading.value = false
  })
}

function loadProcessRecords(thesisId) {
  // 并行请求所有环节类型
  const promises = processSteps.map(step =>
    listProcess({ thesisId, processType: step.type, pageSize: 100 }).then(res => {
      const list = res.data.records || res.data || []
      processMap.value[step.type] = list.map(item => {
        if (item.contentExtend) {
          try {
            item.contentExtend = typeof item.contentExtend === 'string' ? JSON.parse(item.contentExtend) : item.contentExtend
          } catch (e) { /* ignore */ }
        }
        return item
      })
    })
  )

  Promise.all(promises).finally(() => {
    pageLoading.value = false
  })
}

// ==================== 编辑操作 ====================

function handleEditThesis() {
  if (thesisMain.value) {
    editForm.value = {
      id: thesisMain.value.id,
      thesisTitle: thesisMain.value.thesisTitle || '',
      major: thesisMain.value.major || '',
      grade: thesisMain.value.grade || '',
      thesisFinalUrl: thesisMain.value.thesisFinalUrl || ''
    }
  } else {
    editForm.value = {
      id: undefined,
      thesisTitle: '',
      major: '',
      grade: '',
      thesisFinalUrl: ''
    }
  }
  editDialogVisible.value = true
}

function submitEdit() {
  editFormRef.value.validate(valid => {
    if (valid) {
      updateThesisMain(editForm.value).then(() => {
        editDialogVisible.value = false
        loadThesisMain()
      })
    }
  })
}

onMounted(() => {
  loadThesisMain()
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

/* 待办卡片 */
.todo-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border: none;
}

.todo-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.todo-card__inner {
  display: flex;
  align-items: center;
}

.todo-card__icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
  color: #fff;
}

.todo-card--warning .todo-card__icon { background: linear-gradient(135deg, #f7ba2a, #e6a23c); }
.todo-card--danger .todo-card__icon { background: linear-gradient(135deg, #f56c6c, #e04040); }
.todo-card--success .todo-card__icon { background: linear-gradient(135deg, #67c23a, #529b2e); }
.todo-card--primary .todo-card__icon { background: linear-gradient(135deg, #409eff, #337ecc); }

.todo-card__count {
  font-size: 28px;
  font-weight: bold;
  line-height: 1.2;
}

.todo-card__label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.todo-card__action {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
  font-size: 13px;
  color: #409eff;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 增强版步骤条 */
.progress-steps-wrapper {
  padding: 10px 0;
}

.enhanced-steps .step-item {
  cursor: pointer;
  transition: transform 0.15s;
}

.enhanced-steps .step-item:hover {
  transform: scale(1.02);
}

.step-guide {
  margin-top: 20px;
  padding: 0 40px;
}

/* 折叠面板标题 */
.collapse-title {
  display: flex;
  align-items: center;
}

/* 表单提示 */
.form-tip {
  margin-top: 4px;
}
</style>
