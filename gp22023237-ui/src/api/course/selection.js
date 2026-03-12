import request from '@/utils/request'

export function listCourseSelection(query) {
  return request({
    url: '/course/selection/list',
    method: 'get',
    params: query
  })
}

export function getCourseSelection(id) {
  return request({
    url: '/course/selection/' + id,
    method: 'get'
  })
}

export function addCourseSelection(data) {
  return request({
    url: '/course/selection/add',
    method: 'post',
    data: data
  })
}

export function updateCourseSelection(data) {
  return request({
    url: '/course/selection/update',
    method: 'put',
    data: data
  })
}

export function delCourseSelection(id) {
  return request({
    url: '/course/selection/delete/' + id,
    method: 'delete'
  })
}
