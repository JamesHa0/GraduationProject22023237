import request from '@/utils/request'

/**
 * 查询毕业审核列表（带学生详情与学分汇总）
 */
export function listGraduationAudit(params) {
  return request({
    url: '/degree/graduation-audit/list',
    method: 'get',
    params: params
  })
}

/**
 * 获取毕业审核详情
 */
export function getGraduationAuditDetail(id) {
  return request({
    url: `/degree/graduation-audit/${id}`,
    method: 'get'
  })
}

/**
 * 自动审核毕业资格（单条）
 */
export function autoAuditGraduation(studentId) {
  return request({
    url: '/degree/graduation-audit/autoAudit',
    method: 'post',
    data: { studentId }
  })
}

/**
 * 批量自动审核毕业资格
 */
export function batchAutoAuditGraduation(data) {
  return request({
    url: '/degree/graduation-audit/batchAutoAudit',
    method: 'post',
    data: data
  })
}

/**
 * 人工审核毕业资格
 */
export function manualAuditGraduation(data) {
  return request({
    url: '/degree/graduation-audit/manualAudit',
    method: 'post',
    data: data
  })
}

/**
 * 获取毕业审核统计数据
 */
export function getGraduationStats() {
  return request({
    url: '/degree/graduation-audit/stats',
    method: 'get'
  })
}
