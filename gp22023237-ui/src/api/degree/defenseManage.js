import request from '@/utils/request'

/**
 * 查询答辩记录列表（复用学位申请列表，默认筛选有答辩安排的记录）
 */
export function listDefenseRecord(params) {
  return request({
    url: '/thesis/degree/list',
    method: 'get',
    params: params
  })
}

/**
 * 录入答辩结果
 * @param {Object} data - { id, defenseResult, defenseScore, defenseCommitteeComment, qaRecord }
 */
export function updateDefenseResult(data) {
  return request({
    url: '/thesis/degree/defense/updateResult',
    method: 'post',
    data: data
  })
}

/**
 * 获取答辩详情（含学生信息+答辩信息）
 */
export function getDefenseDetail(id) {
  return request({
    url: `/thesis/degree/${id}`,
    method: 'get'
  })
}
