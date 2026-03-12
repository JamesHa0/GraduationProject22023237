import request from '@/utils/request'

export function listCourse(query) {
  return request({
    url: '/course/list',
    method: 'get',
    params: query
  })
}

export function getCourse(id) {
  return request({
    url: '/course/' + id,
    method: 'get'
  })
}

export function addCourse(data) {
  return request({
    url: '/course/add',
    method: 'post',
    data: data
  })
}

export function updateCourse(data) {
  return request({
    url: '/course/update',
    method: 'put',
    data: data
  })
}

export function delCourse(id) {
  return request({
    url: '/course/delete/' + id,
    method: 'delete'
  })
}
