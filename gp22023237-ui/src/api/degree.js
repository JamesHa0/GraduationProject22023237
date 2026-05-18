import request from '@/utils/request'

// ==================== 论文主记录 API ====================

export function listThesisMain(params) {
  return request({
    url: '/thesis/main/list',
    method: 'get',
    params: params
  })
}

export function getThesisMainDetail(id) {
  return request({
    url: `/thesis/main/${id}`,
    method: 'get'
  })
}

export function getThesisMainByStudent(studentId) {
  return request({
    url: `/thesis/main/student/${studentId}`,
    method: 'get'
  })
}

export function updateThesisMain(data) {
  return request({
    url: '/thesis/main/update',
    method: 'post',
    data: data
  })
}

export function archiveThesis(id) {
  return request({
    url: `/thesis/main/archive/${id}`,
    method: 'post'
  })
}

// ==================== 论文流程记录 API ====================

export function listProcess(params) {
  return request({
    url: '/thesis/process/list',
    method: 'get',
    params: params
  })
}

export function getProcessDetail(id) {
  return request({
    url: `/thesis/process/${id}`,
    method: 'get'
  })
}

export function submitProcess(data) {
  return request({
    url: '/thesis/process/submit',
    method: 'post',
    data: data
  })
}

export function approveProcessSupervisor(id, status, comment, approverId) {
  return request({
    url: '/thesis/process/supervisor/approve',
    method: 'post',
    params: { id, status, comment, approverId }
  })
}

export function approveProcessSecretary(id, status, comment, approverId) {
  return request({
    url: '/thesis/process/secretary/approve',
    method: 'post',
    params: { id, status, comment, approverId }
  })
}

export function approveProcessDean(id, status, comment, approverId) {
  return request({
    url: '/thesis/process/dean/approve',
    method: 'post',
    params: { id, status, comment, approverId }
  })
}

export function recordProcessResult(id, result, score, comment, qaRecord) {
  return request({
    url: '/thesis/process/recordResult',
    method: 'post',
    params: { id, result, score, comment, qaRecord }
  })
}

// ==================== 学位申请相关 API ====================

export function listDegreeApplication(params) {
  return request({
    url: '/thesis/degree/list',
    method: 'get',
    params: params
  })
}

export function submitDegreeApplication(data) {
  return request({
    url: '/thesis/degree/submit',
    method: 'post',
    data: data
  })
}

export function committeeApprove(id, status, comment) {
  return request({
    url: '/thesis/degree/committee/approve',
    method: 'post',
    params: { id, status, comment }
  })
}

export function resubmitDegreeApplication(data) {
  return request({
    url: '/thesis/degree/resubmit',
    method: 'post',
    data: data
  })
}

export function grantDegree(id, certificateNo) {
  return request({
    url: '/thesis/degree/grant',
    method: 'post',
    params: { id, certificateNo }
  })
}

export function checkDefenseEligibility(studentId) {
  return request({
    url: '/thesis/degree/checkEligibility',
    method: 'get',
    params: { studentId }
  })
}

// ==================== 统计与归档 API ====================

export function batchArchiveThesis(ids) {
  return request({
    url: '/thesis/main/batchArchive',
    method: 'post',
    data: { ids }
  })
}

export function getThesisStatistics() {
  return request({
    url: '/thesis/main/statistics',
    method: 'get'
  })
}

// ==================== 流程配置 API ====================

export function listProcessConfig() {
  return request({
    url: '/thesis/config/list',
    method: 'get'
  })
}

export function saveProcessConfig(data) {
  return request({
    url: '/thesis/config/save',
    method: 'post',
    data: data
  })
}

export function getGlobalConfig() {
  return request({
    url: '/thesis/config/global',
    method: 'get'
  })
}

export function saveGlobalConfig(data) {
  return request({
    url: '/thesis/config/global/save',
    method: 'post',
    data: data
  })
}

// ==================== 导师端 API ====================

export function getSupervisorStudents(supervisorId) {
  return request({
    url: '/thesis/supervisor/students',
    method: 'get',
    params: { supervisorId }
  })
}

export function getSupervisorTopic(params) {
  return request({
    url: '/thesis/supervisor/topic',
    method: 'get',
    params
  })
}

export function assignTopic(data) {
  return request({
    url: '/thesis/supervisor/topic/assign',
    method: 'post',
    data
  })
}

export function getTopicModifications(params) {
  return request({
    url: '/thesis/supervisor/topic-modification',
    method: 'get',
    params
  })
}

export function approveTopicModification(id, status, comment, approverId) {
  return request({
    url: '/thesis/supervisor/topic-modification/approve',
    method: 'post',
    params: { id, status, comment, approverId }
  })
}

export function getSupervisorTask(params) {
  return request({
    url: '/thesis/supervisor/task',
    method: 'get',
    params
  })
}

export function createTask(data) {
  return request({
    url: '/thesis/supervisor/task/create',
    method: 'post',
    data
  })
}

export function updateTask(data) {
  return request({
    url: '/thesis/supervisor/task/update',
    method: 'put',
    data
  })
}

export function getSupervisorDefenseDraft(params) {
  return request({
    url: '/thesis/supervisor/defense-draft',
    method: 'get',
    params
  })
}

export function commentDefenseDraft(id, status, comment, approverId) {
  return request({
    url: '/thesis/supervisor/defense-draft/comment',
    method: 'post',
    params: { id, status, comment, approverId }
  })
}

export function getSupervisorThesisFinal(params) {
  return request({
    url: '/thesis/supervisor/thesis-final',
    method: 'get',
    params
  })
}

// ==================== 成绩评定 API ====================

export function getThesisGrade(thesisId) {
  return request({
    url: `/thesis/grade/${thesisId}`,
    method: 'get'
  })
}

export function supervisorGrade(data) {
  return request({
    url: '/thesis/grade/supervisor',
    method: 'post',
    data
  })
}

export function reviewerGrade(data) {
  return request({
    url: '/thesis/grade/reviewer',
    method: 'post',
    data
  })
}

export function defenseGrade(data) {
  return request({
    url: '/thesis/grade/defense',
    method: 'post',
    data
  })
}

export function calculateTotalGrade(thesisId) {
  return request({
    url: '/thesis/grade/calculate',
    method: 'post',
    params: { thesisId }
  })
}
