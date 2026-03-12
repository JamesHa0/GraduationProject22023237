import request from '@/utils/request'

// 自动审核毕业资格
export function autoReview(studentId, reviewerId) {
  return request({
    url: '/student/graduation/autoReview',
    method: 'post',
    params: { studentId, reviewerId }
  })
}

// 手动审核毕业资格
export function manualReview(data) {
  return request({
    url: '/student/graduation/manualReview',
    method: 'post',
    params: data
  })
}

// 查询毕业资格列表
export function listGraduation(queryParams) {
  return request({
    url: '/student/graduation/list',
    method: 'get',
    params: queryParams
  })
}

// 查询详情
export function getGraduation(id) {
  return request({
    url: '/student/graduation/' + id,
    method: 'get'
  })
}

// 查询学生毕业资格
export function getGraduationByStudentId(studentId) {
  return request({
    url: '/student/graduation/student/' + studentId,
    method: 'get'
  })
}
