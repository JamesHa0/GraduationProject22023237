import request from '@/utils/request'

export function listCourse(query) {
  return request({
    url: '/course/list',
    method: 'get',
    params: query
  })
}

export function listTeachers() {
  return request({
    url: '/course/teachers',
    method: 'get'
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

export function delCourseBatch(ids) {
  return request({
    url: '/course/deleteBatch',
    method: 'delete',
    data: ids
  })
}

export function importCourse(data) {
  return request({
    url: '/course/import',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function downloadImportTemplate() {
  return request({
    url: '/course/importTemplate',
    method: 'get',
    responseType: 'blob'
  })
}

// 长期任务化导入预留接口（当前后端未启用）
export function createCourseImportTask(data) {
  return request({
    url: '/course/importTask/create',
    method: 'post',
    data
  })
}

export function queryCourseImportTask(taskId) {
  return request({
    url: `/course/importTask/${taskId}`,
    method: 'get'
  })
}

export function downloadCourseImportFailFile(taskId) {
  return request({
    url: `/course/importTask/${taskId}/failFile`,
    method: 'get',
    responseType: 'blob'
  })
}
