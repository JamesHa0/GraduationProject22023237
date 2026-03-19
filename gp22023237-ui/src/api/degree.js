import request from '@/utils/request'

// 开题报告相关API
export function listProposal(params) {
  return request({
    url: '/thesis/proposal/list',
    method: 'get',
    params: params
  })
}

export function getProposalDetail(id) {
  return request({
    url: `/thesis/proposal/${id}`,
    method: 'get'
  })
}

export function submitProposal(data) {
  return request({
    url: '/thesis/proposal/submit',
    method: 'post',
    data: data
  })
}

export function approveProposalMentor(id, status, comment) {
  return request({
    url: '/thesis/proposal/mentor/approve',
    method: 'post',
    data: { id, status, comment }
  })
}

export function approveProposalSecretary(id, status, comment) {
  return request({
    url: '/thesis/proposal/secretary/approve',
    method: 'post',
    data: { id, status, comment }
  })
}

export function approveProposalDean(id, status, comment) {
  return request({
    url: '/thesis/proposal/dean/approve',
    method: 'post',
    data: { id, status, comment }
  })
}

// 中期检查相关API
export function listMidterm(params) {
  return request({
    url: '/thesis/midterm/list',
    method: 'get',
    params: params
  })
}

export function getMidtermDetail(id) {
  return request({
    url: `/thesis/midterm/${id}`,
    method: 'get'
  })
}

export function submitMidterm(data) {
  return request({
    url: '/thesis/midterm/submit',
    method: 'post',
    data: data
  })
}

export function approveMidtermMentor(id, status, comment) {
  return request({
    url: '/thesis/midterm/mentor/approve',
    method: 'post',
    data: { id, status, comment }
  })
}

export function approveMidtermSecretary(id, status, comment) {
  return request({
    url: '/thesis/midterm/secretary/approve',
    method: 'post',
    data: { id, status, comment }
  })
}

export function approveMidtermDean(id, status, comment) {
  return request({
    url: '/thesis/midterm/dean/approve',
    method: 'post',
    data: { id, status, comment }
  })
}

// 预答辩相关API
export function listPreDefense(params) {
  return request({
    url: '/thesis/preDefense/list',
    method: 'get',
    params: params
  })
}

export function getPreDefenseDetail(id) {
  return request({
    url: `/thesis/preDefense/${id}`,
    method: 'get'
  })
}

export function submitPreDefense(data) {
  return request({
    url: '/thesis/preDefense/submit',
    method: 'post',
    data: data
  })
}

export function recordPreDefenseResult(id, result, comment, qaRecord) {
  return request({
    url: '/thesis/preDefense/record',
    method: 'post',
    data: { id, result, comment, qaRecord }
  })
}

export function approvePreDefenseMentor(id, status, comment) {
  return request({
    url: '/thesis/preDefense/mentor/approve',
    method: 'post',
    data: { id, status, comment }
  })
}

export function approvePreDefenseSecretary(id, status, comment) {
  return request({
    url: '/thesis/preDefense/secretary/approve',
    method: 'post',
    data: { id, status, comment }
  })
}

export function approvePreDefenseDean(id, status, comment) {
  return request({
    url: '/thesis/preDefense/dean/approve',
    method: 'post',
    data: { id, status, comment }
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

export function recordDefenseResult(id, result, score, comment, qaRecord) {
  return request({
    url: '/thesis/degree/defense/record',
    method: 'post',
    data: { id, result, score, comment, qaRecord }
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
