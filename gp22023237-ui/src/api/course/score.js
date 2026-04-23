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

export function getMyScores(query) {
  return request({
    url: '/course/score/myScores',
    method: 'get',
    params: query
  })
}

export function listScoreWithDetails(query) {
  return request({
    url: '/course/score/listWithDetails',
    method: 'get',
    params: query
  })
}

// 批量更新成绩
export function batchUpdateScore(data) {
  return request({
    url: '/course/score/batchUpdate',
    method: 'put',
    data: data
  })
}

// Excel导入成绩
export function importScore(data) {
  return request({
    url: '/course/score/import',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 下载成绩导入模板
export function downloadScoreTemplate() {
  return request({
    url: '/course/score/importTemplate',
    method: 'get',
    responseType: 'blob'
  })
}

// 导出成绩
export function exportScore(query) {
  return request({
    url: '/course/score/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
