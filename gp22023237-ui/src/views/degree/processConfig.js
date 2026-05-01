/**
 * 学位流程类型配置（参照 academic/submit/typeConfig.js）
 * 定义7种流程类型的表单字段、验证规则、提交处理等
 */
import {
  listProcess, getProcessDetail, submitProcess,
  approveProcessSupervisor, approveProcessSecretary, approveProcessDean,
  recordProcessResult, getThesisMainByStudent
} from '@/api/degree'

// ==================== 流程类型选项 ====================

export const PROCESS_TYPE_OPTIONS = [
  { label: '论文开题', value: 1 },
  { label: '论文中期检查', value: 2 },
  { label: '论文预答辩', value: 3 },
  { label: '论文外审', value: 4 },
  { label: '正式答辩', value: 5 },
  { label: '二次答辩', value: 6 },
  { label: '修改后再审', value: 7 }
]

export const PROCESS_TYPE_LABELS = {
  1: '论文开题', 2: '论文中期检查', 3: '论文预答辩',
  4: '论文外审', 5: '正式答辩', 6: '二次答辩', 7: '修改后再审'
}

export function getProcessTypeLabel(type) {
  return PROCESS_TYPE_LABELS[type] || '论文流程'
}

/** 流程状态选项（供查询表单使用） */
export const PROCESS_STATUS_OPTIONS = [
  { label: '未提交', value: 0 },
  { label: '审批中', value: 1 },
  { label: '评审中', value: 2 },
  { label: '已通过', value: 3 },
  { label: '已拒绝', value: 4 },
  { label: '已完成', value: 5 }
]

/** 评审结果选项（供录入结果对话框使用） */
export const REVIEW_RESULT_OPTIONS = [
  { label: '通过', value: 1 },
  { label: '修改后通过', value: 2 },
  { label: '未通过', value: 3 }
]

/** 审批API映射（按角色） */
function getApproveApi(roleId) {
  if (roleId === 7 || roleId === 8) return approveProcessSupervisor
  if (roleId === 5 || roleId === 4) return approveProcessSecretary
  return approveProcessDean
}

// ==================== 各类型默认表单值 ====================

const COMMON_FORM = {
  id: undefined,
  thesisId: undefined,
  thesisTitle: undefined,
  version: undefined,
  thesisVersionUrl: undefined,
  attachmentUrl: undefined,
  eventTime: undefined,
  eventLocation: undefined
}

const PROPOSAL_FORM = {
  ...COMMON_FORM,
  background: undefined,
  researchStatus: undefined,
  researchContent: undefined,
  researchMethod: undefined
}

const MIDTERM_FORM = {
  ...COMMON_FORM,
  completedWork: undefined,
  remainingWork: undefined,
  problems: undefined,
  nextPlan: undefined,
  draftProgress: undefined
}

const PREDEFENSE_FORM = {
  ...COMMON_FORM,
  abstractContent: undefined
}

const EXTERNAL_REVIEW_FORM = {
  ...COMMON_FORM,
  reviewerName: undefined,
  reviewerInstitution: undefined,
  reviewField: undefined
}

const DEFENSE_FORM = {
  ...COMMON_FORM,
  reviewCommitteeChair: undefined,
  reviewCommitteeMembers: undefined
}

const RESUBMISSION_FORM = {
  ...COMMON_FORM,
  modificationDescription: undefined,
  modificationDetails: undefined
}

// ==================== 各类型验证规则 ====================

const COMMON_RULES = {
  thesisTitle: [{ required: true, message: '请输入论文题目', trigger: 'blur' }]
}

const PROPOSAL_RULES = {
  ...COMMON_RULES,
  background: [{ required: true, message: '请输入研究背景', trigger: 'blur' }],
  researchStatus: [{ required: true, message: '请输入研究现状', trigger: 'blur' }],
  researchContent: [{ required: true, message: '请输入研究内容', trigger: 'blur' }],
  researchMethod: [{ required: true, message: '请输入研究方法', trigger: 'blur' }]
}

const MIDTERM_RULES = {
  ...COMMON_RULES,
  completedWork: [{ required: true, message: '请输入已完成工作', trigger: 'blur' }],
  remainingWork: [{ required: true, message: '请输入未完成工作', trigger: 'blur' }],
  nextPlan: [{ required: true, message: '请输入下一步计划', trigger: 'blur' }]
}

const PREDEFENSE_RULES = {
  ...COMMON_RULES,
  abstractContent: [{ required: true, message: '请输入论文摘要', trigger: 'blur' }]
}

const EXTERNAL_REVIEW_RULES = {
  ...COMMON_RULES,
  reviewerName: [{ required: true, message: '请输入外审专家', trigger: 'blur' }],
  reviewerInstitution: [{ required: true, message: '请输入专家单位', trigger: 'blur' }],
  reviewField: [{ required: true, message: '请输入评审领域', trigger: 'blur' }]
}

const DEFENSE_RULES = {
  ...COMMON_RULES,
  reviewCommitteeChair: [{ required: true, message: '请输入答辩委员会主席', trigger: 'blur' }],
  reviewCommitteeMembers: [{ required: true, message: '请输入答辩委员', trigger: 'blur' }]
}

const RESUBMISSION_RULES = {
  ...COMMON_RULES,
  modificationDescription: [{ required: true, message: '请输入修改说明', trigger: 'blur' }],
  modificationDetails: [{ required: true, message: '请输入修改详情', trigger: 'blur' }]
}

// ==================== 提交数据处理 ====================

/**
 * 各类型特有的 contentExtend 字段名列表
 * 不在此列表中的字段将直接提交到主表
 */
const CONTENT_EXTEND_FIELDS = {
  1: ['background', 'researchStatus', 'researchContent', 'researchMethod'],
  2: ['completedWork', 'remainingWork', 'problems', 'nextPlan', 'draftProgress'],
  3: ['abstractContent'],
  4: ['reviewerName', 'reviewerInstitution', 'reviewField'],
  7: ['modificationDescription', 'modificationDetails']
  // 5/6 没有 contentExtend 字段，committee 字段直接提交主表
}

/**
 * 将表单数据转换为提交数据
 * @param {Object} formData - 表单数据
 * @param {number} processType - 流程类型
 * @returns {Object} 提交数据
 */
export function buildSubmitData(formData, processType) {
  const contentFields = CONTENT_EXTEND_FIELDS[processType] || []
  const contentData = {}

  contentFields.forEach(field => {
    if (formData[field] !== undefined) {
      contentData[field] = formData[field]
    }
  })

  const submitData = {
    ...formData,
    processType,
    contentExtend: contentFields.length > 0 ? JSON.stringify(contentData) : undefined
  }

  // 移除已序列化到 contentExtend 的字段
  contentFields.forEach(field => {
    delete submitData[field]
  })

  return submitData
}

// ==================== 核心配置对象 ====================

export const PROCESS_CONFIG = {
  1: {
    name: '论文开题',
    typeValue: 1,
    component: 'ProposalForm',
    defaultForm: PROPOSAL_FORM,
    rules: PROPOSAL_RULES,
    canRecordResult: false
  },
  2: {
    name: '论文中期检查',
    typeValue: 2,
    component: 'MidtermForm',
    defaultForm: MIDTERM_FORM,
    rules: MIDTERM_RULES,
    canRecordResult: false
  },
  3: {
    name: '论文预答辩',
    typeValue: 3,
    component: 'PreDefenseForm',
    defaultForm: PREDEFENSE_FORM,
    rules: PREDEFENSE_RULES,
    canRecordResult: false
  },
  4: {
    name: '论文外审',
    typeValue: 4,
    component: 'ExternalReviewForm',
    defaultForm: EXTERNAL_REVIEW_FORM,
    rules: EXTERNAL_REVIEW_RULES,
    canRecordResult: true
  },
  5: {
    name: '正式答辩',
    typeValue: 5,
    component: 'DefenseForm',
    defaultForm: DEFENSE_FORM,
    rules: DEFENSE_RULES,
    canRecordResult: true,
    hasQaRecord: true
  },
  6: {
    name: '二次答辩',
    typeValue: 6,
    component: 'DefenseForm',
    defaultForm: DEFENSE_FORM,
    rules: DEFENSE_RULES,
    canRecordResult: true,
    hasQaRecord: true
  },
  7: {
    name: '修改后再审',
    typeValue: 7,
    component: 'ReSubmissionForm',
    defaultForm: RESUBMISSION_FORM,
    rules: RESUBMISSION_RULES,
    canRecordResult: true
  }
}

/** 共享API */
export const sharedApi = {
  listProcess,
  getProcessDetail,
  submitProcess,
  getThesisMainByStudent,
  getApproveApi,
  recordProcessResult
}
