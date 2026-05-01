/**
 * 论文流程类型配置（VGMS风格7环节）
 * 定义7种流程类型的表单字段、验证规则、提交处理等
 */
import {
  listProcess, getProcessDetail, submitProcess,
  approveProcessSupervisor, approveProcessSecretary, approveProcessDean,
  recordProcessResult, getThesisMainByStudent
} from '@/api/degree'

// ==================== 流程类型选项 ====================

export const PROCESS_TYPE_OPTIONS = [
  { label: '选题', value: 1 },
  { label: '任务书', value: 2 },
  { label: '开题报告', value: 3 },
  { label: '中期检查', value: 4 },
  { label: '过程稿', value: 5 },
  { label: '论文答辩稿', value: 6 },
  { label: '毕业论文', value: 7 }
]

export const PROCESS_TYPE_LABELS = {
  1: '选题', 2: '任务书', 3: '开题报告',
  4: '中期检查', 5: '过程稿', 6: '论文答辩稿', 7: '毕业论文'
}

export function getProcessTypeLabel(type) {
  return PROCESS_TYPE_LABELS[type] || '论文流程'
}

/** 流程状态选项 */
export const PROCESS_STATUS_OPTIONS = [
  { label: '未提交', value: 0 },
  { label: '审批中', value: 1 },
  { label: '评审中', value: 2 },
  { label: '已通过', value: 3 },
  { label: '已拒绝', value: 4 },
  { label: '已完成', value: 5 }
]

/** 评审结果选项 */
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

// 选题表单
const TOPIC_FORM = {
  ...COMMON_FORM,
  topicName: undefined,
  topicSource: undefined,
  topicDesc: undefined,
  expectedGoal: undefined
}

// 任务书（学生只读，导师编辑）
const TASK_BOOK_FORM = {
  ...COMMON_FORM,
  background: undefined,
  mainTask: undefined,
  schedule: undefined,
  references: undefined
}

// 开题报告
const PROPOSAL_FORM = {
  ...COMMON_FORM,
  background: undefined,
  researchStatus: undefined,
  researchContent: undefined,
  researchMethod: undefined
}

// 中期检查
const MIDTERM_FORM = {
  ...COMMON_FORM,
  completedWork: undefined,
  remainingWork: undefined,
  problems: undefined,
  nextPlan: undefined
}

// 过程稿
const DRAFT_FORM = {
  ...COMMON_FORM,
  draftDesc: undefined,
  modificationNote: undefined
}

// 论文答辩稿
const DEFENSE_DRAFT_FORM = {
  ...COMMON_FORM,
  defenseDraftDesc: undefined,
  pptAttachment: undefined
}

// 毕业论文
const FINAL_THESIS_FORM = {
  ...COMMON_FORM
}

// ==================== 各类型验证规则 ====================

const COMMON_RULES = {}

const TOPIC_RULES = {
  topicName: [{ required: true, message: '请输入课题名称', trigger: 'blur' }],
  topicSource: [{ required: true, message: '请选择课题来源', trigger: 'change' }]
}

const TASK_BOOK_RULES = {
  background: [{ required: true, message: '请输入课题背景', trigger: 'blur' }],
  mainTask: [{ required: true, message: '请输入主要任务', trigger: 'blur' }],
  schedule: [{ required: true, message: '请输入进度安排', trigger: 'blur' }]
}

const PROPOSAL_RULES = {
  background: [{ required: true, message: '请输入研究背景', trigger: 'blur' }],
  researchStatus: [{ required: true, message: '请输入研究现状', trigger: 'blur' }],
  researchContent: [{ required: true, message: '请输入研究内容', trigger: 'blur' }],
  researchMethod: [{ required: true, message: '请输入研究方法', trigger: 'blur' }]
}

const MIDTERM_RULES = {
  completedWork: [{ required: true, message: '请输入已完成工作', trigger: 'blur' }],
  remainingWork: [{ required: true, message: '请输入未完成工作', trigger: 'blur' }],
  nextPlan: [{ required: true, message: '请输入下一步计划', trigger: 'blur' }]
}

const DRAFT_RULES = {}

const DEFENSE_DRAFT_RULES = {}

const FINAL_THESIS_RULES = {}

// ==================== 提交数据处理 ====================

/**
 * 各类型特有的 contentExtend 字段名列表
 */
const CONTENT_EXTEND_FIELDS = {
  1: ['topicName', 'topicSource', 'topicDesc', 'expectedGoal'],
  2: ['background', 'mainTask', 'schedule', 'references'],
  3: ['background', 'researchStatus', 'researchContent', 'researchMethod'],
  4: ['completedWork', 'remainingWork', 'problems', 'nextPlan'],
  5: ['draftDesc', 'modificationNote'],
  6: ['defenseDraftDesc', 'pptAttachment']
  // 7 毕业论文无 contentExtend
}

/**
 * 将表单数据转换为提交数据
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

/**
 * 从contentExtend JSON中解析各类型特有字段
 */
export function parseContentExtend(record) {
  if (!record || !record.contentExtend) return {}
  try {
    return JSON.parse(record.contentExtend)
  } catch {
    return {}
  }
}

// ==================== 核心配置对象 ====================

export const PROCESS_CONFIG = {
  1: {
    name: '选题',
    typeValue: 1,
    defaultForm: TOPIC_FORM,
    rules: TOPIC_RULES,
    canRecordResult: false
  },
  2: {
    name: '任务书',
    typeValue: 2,
    defaultForm: TASK_BOOK_FORM,
    rules: TASK_BOOK_RULES,
    canRecordResult: false
  },
  3: {
    name: '开题报告',
    typeValue: 3,
    defaultForm: PROPOSAL_FORM,
    rules: PROPOSAL_RULES,
    canRecordResult: false
  },
  4: {
    name: '中期检查',
    typeValue: 4,
    defaultForm: MIDTERM_FORM,
    rules: MIDTERM_RULES,
    canRecordResult: false
  },
  5: {
    name: '过程稿',
    typeValue: 5,
    defaultForm: DRAFT_FORM,
    rules: DRAFT_RULES,
    canRecordResult: false
  },
  6: {
    name: '论文答辩稿',
    typeValue: 6,
    defaultForm: DEFENSE_DRAFT_FORM,
    rules: DEFENSE_DRAFT_RULES,
    canRecordResult: false
  },
  7: {
    name: '毕业论文',
    typeValue: 7,
    defaultForm: FINAL_THESIS_FORM,
    rules: FINAL_THESIS_RULES,
    canRecordResult: false
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
