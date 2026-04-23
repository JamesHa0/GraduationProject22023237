import request from '@/utils/request'

export function listClass(query) {
  return request({
    url: '/class/list',
    method: 'get',
    params: query
  })
}

export function listAllClass() {
  return request({
    url: '/class/all',
    method: 'get'
  })
}

export function getClass(id) {
  return request({
    url: '/class/' + id,
    method: 'get'
  })
}

export function addClass(data) {
  return request({
    url: '/class/add',
    method: 'post',
    data: data
  })
}

export function updateClass(data) {
  return request({
    url: '/class/update',
    method: 'put',
    data: data
  })
}

export function delClass(id) {
  return request({
    url: '/class/delete/' + id,
    method: 'delete'
  })
}

export function delClassBatch(ids) {
  return request({
    url: '/class/deleteBatch',
    method: 'delete',
    data: ids
  })
}
