import request from '@/utils/request'

// 学生信息相关API
export function listStudent(params) {
  return request({
    url: '/student/list',
    method: 'get',
    params: params
  })
}

export function getStudentDetail(id) {
  return request({
    url: `/student/${id}`,
    method: 'get'
  })
}

export function getStudentByNo(studentNo) {
  return request({
    url: `/student/byNo/${studentNo}`,
    method: 'get'
  })
}

export function getCurrentStudent() {
  return request({
    url: '/student/current',
    method: 'get'
  })
}

export function addStudent(data) {
  return request({
    url: '/student/add',
    method: 'post',
    data: data
  })
}

export function updateStudent(data) {
  return request({
    url: '/student/update',
    method: 'put',
    data: data
  })
}

export function deleteStudent(id) {
  return request({
    url: `/student/delete/${id}`,
    method: 'delete'
  })
}

export function deleteStudentBatch(ids) {
  return request({
    url: '/student/deleteBatch',
    method: 'delete',
    data: ids
  })
}

export function updateStudentStatus(id, status) {
  return request({
    url: '/student/updateStatus',
    method: 'put',
    params: { id, status }
  })
}

export function updateSelectionStatus(id, selectionStatus) {
  return request({
    url: '/student/updateSelectionStatus',
    method: 'put',
    params: { id, selectionStatus }
  })
}

// 根据班级ID查询学生列表（分页）
export function listStudentByClass(classId, params) {
  return request({
    url: `/student/listByClass/${classId}`,
    method: 'get',
    params: params
  })
}

// 获取归属年级列表
export function listCohortYears() {
  return request({
    url: '/student/cohortYears',
    method: 'get'
  })
}

// 学生批量导入
export function createStudentImportTask() {
  return request({
    url: '/student/importTask/create',
    method: 'post'
  })
}

export function queryStudentImportTask(taskId) {
  return request({
    url: `/student/importTask/${taskId}`,
    method: 'get'
  })
}

export function importStudent(data) {
  return request({
    url: '/student/import',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function downloadStudentImportTemplate(classId) {
  return request({
    url: '/student/importTemplate',
    method: 'get',
    params: classId ? { classId } : {},
    responseType: 'blob'
  })
}
