import request from '@/utils/request'

export function listScore(query) {
  return request({
    url: '/course/score/list',
    method: 'get',
    params: query
  })
}

export function getScore(id) {
  return request({
    url: '/course/score/' + id,
    method: 'get'
  })
}

export function addScore(data) {
  return request({
    url: '/course/score/add',
    method: 'post',
    data: data
  })
}

export function updateScore(data) {
  return request({
    url: '/course/score/update',
    method: 'put',
    data: data
  })
}

export function delScore(id) {
  return request({
    url: '/course/score/delete/' + id,
    method: 'delete'
  })
}
