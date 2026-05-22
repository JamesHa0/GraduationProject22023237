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
    params: { id, status, comment }
  })
}

export function approveStatusChangeSecretary(id, status, comment) {
  return request({
    url: '/student/status/change/secretary/approve',
    method: 'post',
    params: { id, status, comment }
  })
}
