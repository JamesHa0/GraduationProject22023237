import request from '@/utils/request'

// 查询操作日志列表
export function list(query) {
  return request({
    url: '/system/oplog/list',
    method: 'get',
    params: query
  })
}

// 删除操作日志
export function delOplog(operId) {
  return request({
    url: '/system/oplog/' + operId,
    method: 'delete'
  })
}

// 清空操作日志
export function cleanOplog() {
  return request({
    url: '/system/oplog/clean',
    method: 'delete'
  })
}
