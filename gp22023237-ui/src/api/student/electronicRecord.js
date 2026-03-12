import request from '@/utils/request'

// 添加电子档案
export function addElectronicRecord(data) {
  return request({
    url: '/student/electronicRecord/add',
    method: 'post',
    data: data
  })
}

// 更新电子档案
export function updateElectronicRecord(data) {
  return request({
    url: '/student/electronicRecord/update',
    method: 'post',
    data: data
  })
}

// 删除电子档案
export function deleteElectronicRecord(id) {
  return request({
    url: '/student/electronicRecord/delete/' + id,
    method: 'post'
  })
}

// 查询电子档案列表
export function listElectronicRecord(queryParams) {
  return request({
    url: '/student/electronicRecord/list',
    method: 'get',
    params: queryParams
  })
}

// 查询详情
export function getElectronicRecord(id) {
  return request({
    url: '/student/electronicRecord/' + id,
    method: 'get'
  })
}

// 查询学生的所有档案
export function getElectronicRecordByStudentId(studentId) {
  return request({
    url: '/student/electronicRecord/student/' + studentId,
    method: 'get'
  })
}
