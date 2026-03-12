import request from '@/utils/request'

// 提交学籍异动申请
export function submitApplication(data) {
  return request({
    url: '/student/statusChange/submit',
    method: 'post',
    data: data
  })
}

// 导师审核
export function tutorApproval(id, approval, tutorId) {
  return request({
    url: '/student/statusChange/tutorApproval',
    method: 'post',
    params: { id, approval, tutorId }
  })
}

// 教学秘书审核
export function counselorApproval(id, approval, counselorId) {
  return request({
    url: '/student/statusChange/counselorApproval',
    method: 'post',
    params: { id, approval, counselorId }
  })
}

// 查询学籍异动列表
export function listStatusChange(queryParams) {
  return request({
    url: '/student/statusChange/list',
    method: 'get',
    params: queryParams
  })
}

// 查询详情
export function getStatusChange(id) {
  return request({
    url: '/student/statusChange/' + id,
    method: 'get'
  })
}

// 取消申请
export function cancelApplication(id) {
  return request({
    url: '/student/statusChange/cancel/' + id,
    method: 'post'
  })
}
