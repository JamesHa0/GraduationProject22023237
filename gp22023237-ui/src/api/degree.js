import request from '@/utils/request'

// 论文进展相关API（开题、中期、预答辩统一接口）
export function listProgress(params) {
  return request({
    url: '/thesis/progress/list',
    method: 'get',
    params: params
  })
}

export function getProgressDetail(id) {
  return request({
    url: `/thesis/progress/${id}`,
    method: 'get'
  })
}

export function submitProgress(data) {
  return request({
    url: '/thesis/progress/submit',
    method: 'post',
    data: data
  })
}

export function approveProgressMentor(id, status, comment) {
  return request({
    url: '/thesis/progress/mentor/approve',
    method: 'post',
    data: { id, status, comment }
  })
}

export function approveProgressSecretary(id, status, comment) {
  return request({
    url: '/thesis/progress/secretary/approve',
    method: 'post',
    data: { id, status, comment }
  })
}

export function approveProgressDean(id, status, comment) {
  return request({
    url: '/thesis/progress/dean/approve',
    method: 'post',
    data: { id, status, comment }
  })
}

// 论文答辩相关API
export function listDefense(params) {
  return request({
    url: '/thesis/defense/list',
    method: 'get',
    params: params
  })
}

export function getDefenseDetail(id) {
  return request({
    url: `/thesis/defense/${id}`,
    method: 'get'
  })
}

export function submitDefense(data) {
  return request({
    url: '/thesis/defense/submit',
    method: 'post',
    data: data
  })
}

export function approveDefenseTutor(id, status) {
  return request({
    url: '/thesis/defense/tutor/approve',
    method: 'post',
    data: { id, status }
  })
}

export function approveDefenseDean(id, status) {
  return request({
    url: '/thesis/defense/dean/approve',
    method: 'post',
    data: { id, status }
  })
}

export function recordDefenseResult(id, result, score, comment, qaRecord) {
  return request({
    url: '/thesis/defense/record',
    method: 'post',
    data: { id, result, score, comment, qaRecord }
  })
}

// 论文外审相关API
export function listExternalReview(params) {
  return request({
    url: '/thesis/externalReview/list',
    method: 'get',
    params: params
  })
}

export function getExternalReviewDetail(id) {
  return request({
    url: `/thesis/externalReview/${id}`,
    method: 'get'
  })
}

export function submitExternalReview(data) {
  return request({
    url: '/thesis/externalReview/submit',
    method: 'post',
    data: data
  })
}

export function recordExternalReviewResult(id, result, comments) {
  return request({
    url: '/thesis/externalReview/record',
    method: 'post',
    data: { id, result, comments }
  })
}

// 学位申请相关API
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
    data: { id, status, comment }
  })
}

export function grantDegree(id, certificateNo) {
  return request({
    url: '/thesis/degree/grant',
    method: 'post',
    data: { id, certificateNo }
  })
}

// 学位申请答辩条件检查
export function checkDefenseEligibility(studentId) {
  return request({
    url: '/thesis/degree/checkEligibility',
    method: 'get',
    params: { studentId }
  })
}
