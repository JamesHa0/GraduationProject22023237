import request from '@/utils/request'

// 学籍异动申请相关API
export function listStatusChange(params) {
  return request({
    url: '/student/status/change/list',
    method: 'get',
    params: params
  })
}

export function getStatusChangeDetail(id) {
  return request({
    url: `/student/status/change/${id}`,
    method: 'get'
  })
}

export function submitStatusChange(data) {
  return request({
    url: '/student/status/change/submit',
    method: 'post',
    data: data
  })
}

export function approveStatusChangeMentor(id, status, comment) {
  return request({
    url: '/student/status/change/mentor/approve',
    method: 'post',
    data: { id, status, comment }
  })
}

export function approveStatusChangeSecretary(id, status, comment) {
  return request({
    url: '/student/status/change/secretary/approve',
    method: 'post',
    data: { id, status, comment }
  })
}

export function approveStatusChangeDean(id, status, comment) {
  return request({
    url: '/student/status/change/dean/approve',
    method: 'post',
    data: { id, status, comment }
  })
}

// 毕业资格审核相关API
export function listGraduationAudit(params) {
  return request({
    url: '/student/status/graduation/list',
    method: 'get',
    params: params
  })
}

export function autoAuditGraduation(studentId) {
  return request({
    url: '/student/status/graduation/autoAudit',
    method: 'post',
    data: { studentId }
  })
}

export function manualAuditGraduation(id, status, comment, auditorId, auditorName) {
  return request({
    url: '/student/status/graduation/manualAudit',
    method: 'post',
    data: { id, status, comment, auditorId, auditorName }
  })
}

export function getGraduationStats() {
  return request({
    url: '/student/status/graduation/stats',
    method: 'get'
  })
}
