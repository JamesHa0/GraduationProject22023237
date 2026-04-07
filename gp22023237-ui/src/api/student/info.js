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
