import request from '@/utils/request'

// 查询系统字段列表
export function listField(query) {
  return request({
    url: '/system/field/list',
    method: 'get',
    params: query
  })
}

// 根据模块查询系统字段
export function getFieldByModule(module) {
  return request({
    url: '/system/field/module/' + module,
    method: 'get'
  })
}

// 查询系统字段详细
export function getField(fieldId) {
  return request({
    url: '/system/field/' + fieldId,
    method: 'get'
  })
}

// 新增系统字段
export function addField(data) {
  return request({
    url: '/system/field',
    method: 'post',
    data: data
  })
}

// 修改系统字段
export function updateField(data) {
  return request({
    url: '/system/field',
    method: 'put',
    data: data
  })
}

// 删除系统字段
export function delField(fieldId) {
  return request({
    url: '/system/field/' + fieldId,
    method: 'delete'
  })
}
