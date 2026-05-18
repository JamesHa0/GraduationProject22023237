import request from '@/utils/request'

// 分页查询导师名额列表
export function listQuota(query) {
  return request({
    url: '/selection/quota/list',
    method: 'get',
    params: query
  })
}

// 单个导师名额更新
export function updateQuota(data) {
  return request({
    url: '/selection/quota/update',
    method: 'post',
    data: data
  })
}

// 批量导师名额更新
export function batchUpdateQuota(data) {
  return request({
    url: '/selection/quota/batchUpdate',
    method: 'post',
    data: data
  })
}

// 重置所有导师名额
export function resetAllQuota() {
  return request({
    url: '/selection/quota/reset',
    method: 'post'
  })
}
