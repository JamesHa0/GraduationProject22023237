/**
 * 学位模块统一状态映射工具函数
 * 消除 degree 模块 6+ 文件中重复的状态映射定义
 */

// ==================== 常量映射表 ====================

/** 审批状态文本映射 (supervisorStatus / secretaryStatus / deanStatus) */
const APPROVAL_STATUS_TEXT_MAP = {
  0: '待审批',
  1: '已通过',
  2: '已拒绝',
  null: '-',
  undefined: '-'
}

/** 审批状态 Element Plus Tag 类型映射 */
const APPROVAL_STATUS_TYPE_MAP = {
  0: 'warning',
  1: 'success',
  2: 'danger'
}

/** 流程状态文本映射 (processStatus) */
const PROCESS_STATUS_TEXT_MAP = {
  0: '未提交',
  1: '审批中',
  2: '评审中',
  3: '已通过',
  4: '已拒绝',
  5: '已完成',
  null: '-',
  undefined: '-'
}

/** 流程状态 Element Plus Tag 类型映射 */
const PROCESS_STATUS_TYPE_MAP = {
  0: 'info',
  1: 'warning',
  2: '',
  3: 'success',
  4: 'danger',
  5: 'success'
}

/** 评审结果文本映射 (reviewResult) */
const REVIEW_RESULT_TEXT_MAP = {
  0: '未进行',
  1: '通过',
  2: '修改后通过',
  3: '未通过',
  null: '-',
  undefined: '-'
}

/** 评审结果 Element Plus Tag 类型映射 */
const REVIEW_RESULT_TYPE_MAP = {
  0: 'info',
  1: 'success',
  2: 'warning',
  3: 'danger'
}

/** 最终结果文本映射 (finalResult) */
const FINAL_RESULT_TEXT_MAP = {
  0: '进行中',
  1: '已通过',
  2: '未通过'
}

/** 最终结果 Element Plus Tag 类型映射 */
const FINAL_RESULT_TYPE_MAP = {
  0: 'info',
  1: 'success',
  2: 'danger'
}

// ==================== 导出函数 ====================

/**
 * 获取审批状态文本
 * @param {number|null|undefined} status - 审批状态值 (0=待审批, 1=已通过, 2=已拒绝)
 * @returns {string}
 */
export function getApprovalStatusText(status) {
  return APPROVAL_STATUS_TEXT_MAP[status] !== undefined
    ? APPROVAL_STATUS_TEXT_MAP[status]
    : '-'
}

/**
 * 获取审批状态 Tag 类型
 * @param {number|null|undefined} status
 * @returns {string} Element Plus Tag type
 */
export function getApprovalStatusType(status) {
  return APPROVAL_STATUS_TYPE_MAP[status] || 'info'
}

/**
 * 获取流程状态文本
 * @param {number|null|undefined} status - 流程状态值 (0=未提交, 1=审批中, 2=评审中, 3=已通过, 4=已拒绝, 5=已完成)
 * @returns {string}
 */
export function getProcessStatusText(status) {
  return PROCESS_STATUS_TEXT_MAP[status] !== undefined
    ? PROCESS_STATUS_TEXT_MAP[status]
    : '-'
}

/**
 * 获取流程状态 Tag 类型
 * @param {number|null|undefined} status
 * @returns {string} Element Plus Tag type
 */
export function getProcessStatusType(status) {
  return PROCESS_STATUS_TYPE_MAP[status] || 'info'
}

/**
 * 获取评审结果文本
 * @param {number|null|undefined} result - 评审结果值 (0=未进行, 1=通过, 2=修改后通过, 3=未通过)
 * @returns {string}
 */
export function getReviewResultText(result) {
  return REVIEW_RESULT_TEXT_MAP[result] !== undefined
    ? REVIEW_RESULT_TEXT_MAP[result]
    : '-'
}

/**
 * 获取评审结果 Tag 类型
 * @param {number|null|undefined} result
 * @returns {string} Element Plus Tag type
 */
export function getReviewResultType(result) {
  return REVIEW_RESULT_TYPE_MAP[result] || 'info'
}

/**
 * 获取最终结果文本
 * @param {number|null|undefined} result - 最终结果值 (0=进行中, 1=已通过, 2=未通过)
 * @returns {string}
 */
export function getFinalResultText(result) {
  return FINAL_RESULT_TEXT_MAP[result] !== undefined
    ? FINAL_RESULT_TEXT_MAP[result]
    : '进行中'
}

/**
 * 获取最终结果 Tag 类型
 * @param {number|null|undefined} result
 * @returns {string} Element Plus Tag type
 */
export function getFinalResultType(result) {
  return FINAL_RESULT_TYPE_MAP[result] || 'info'
}

/**
 * 日期格式化
 * @param {string|null|undefined} dateStr - ISO 日期字符串
 * @returns {string} 本地化日期字符串或 '-'
 */
export function parseDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN')
}

/**
 * 逗号分隔文件列表解析
 * @param {string|null|undefined} str - 逗号分隔的文件URL字符串
 * @returns {string[]}
 */
export function parseFileList(str) {
  if (!str) return []
  return str.split(',').filter(s => s.trim())
}

/**
 * 获取流程类型的最新状态文本
 * @param {Array} records - 某流程类型的记录列表
 * @returns {string} 最新版本的状态文本，无记录返回 '未提交'
 */
export function getLatestProcessStatusText(records) {
  if (!records || records.length === 0) return '未提交'
  const latest = records.reduce((a, b) => (a.version > b.version ? a : b))
  return getProcessStatusText(latest.processStatus)
}

/**
 * 获取流程类型时间线节点类型（用于 el-timeline）
 * @param {Array} records - 某流程类型的记录列表
 * @returns {string} Element Plus Timeline type ('success' | 'danger' | 'primary' | 'info')
 */
export function getTimelineType(records) {
  if (!records || records.length === 0) return 'info'
  const latest = records.reduce((a, b) => (a.version > b.version ? a : b))
  if (latest.processStatus === 3 || latest.processStatus === 5) return 'success'
  if (latest.processStatus === 4) return 'danger'
  return 'primary'
}

/**
 * 获取步骤状态（用于 el-steps）
 * @param {Array} records - 某流程类型的记录列表
 * @returns {'wait'|'process'|'finish'|'error'|'success'}
 */
export function getStepStatus(records) {
  if (!records || records.length === 0) return 'wait'
  const latest = records.reduce((a, b) => (a.version > b.version ? a : b))
  if (latest.processStatus === 3 || latest.processStatus === 5) return 'success'
  if (latest.processStatus === 4) return 'error'
  if (latest.processStatus === 1 || latest.processStatus === 2) return 'process'
  return 'wait'
}

/**
 * useDegreeStatus composable
 * 提供所有学位状态映射函数，可在 setup 中解构使用
 */
export function useDegreeStatus() {
  return {
    getApprovalStatusText,
    getApprovalStatusType,
    getProcessStatusText,
    getProcessStatusType,
    getReviewResultText,
    getReviewResultType,
    getFinalResultText,
    getFinalResultType,
    parseDate,
    parseFileList,
    getLatestProcessStatusText,
    getTimelineType,
    getStepStatus
  }
}
