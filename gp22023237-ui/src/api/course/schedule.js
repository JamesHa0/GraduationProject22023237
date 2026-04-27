import request from '@/utils/request'

export function listSchedule(query) {
  return request({
    url: '/schedule/list',
    method: 'get',
    params: query
  })
}

export function getSchedule(id) {
  return request({
    url: '/schedule/' + id,
    method: 'get'
  })
}

export function addSchedule(data) {
  return request({
    url: '/schedule/add',
    method: 'post',
    data: data
  })
}

export function updateSchedule(data) {
  return request({
    url: '/schedule/update',
    method: 'put',
    data: data
  })
}

export function delSchedule(id) {
  return request({
    url: '/schedule/delete/' + id,
    method: 'delete'
  })
}

export function delScheduleBatch(ids) {
  return request({
    url: '/schedule/deleteBatch',
    method: 'delete',
    data: ids
  })
}

export function copySchedule(data) {
  return request({
    url: '/schedule/copy',
    method: 'post',
    data: data
  })
}

export function checkConflict(query) {
  return request({
    url: '/schedule/checkConflict',
    method: 'get',
    params: query
  })
}

export function listTimeSlots() {
  return request({
    url: '/schedule/timeSlots',
    method: 'get'
  })
}

export function importSchedule(data) {
  return request({
    url: '/schedule/import',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function downloadImportTemplate(courseIds) {
  const params = {}
  if (courseIds && courseIds.length > 0) {
    params.courseIds = courseIds.join(',')
  }
  return request({
    url: '/schedule/importTemplate',
    method: 'get',
    params: params,
    responseType: 'blob'
  })
}
