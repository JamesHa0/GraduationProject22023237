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

export function getStudentCourseChoices(query) {
  return request({
    url: '/course/selection/getStudentCourseChoices',
    method: 'get',
    params: query
  })
}

export function saveCourseChoice(data) {
  return request({
    url: '/course/selection/saveCourseChoice',
    method: 'post',
    data: data
  })
}

export function removeCourseChoice(data) {
  return request({
    url: '/course/selection/removeCourseChoice',
    method: 'post',
    data: data
  })
}

export function batchSubmitCourses(data) {
  return request({
    url: '/course/selection/batchSubmitCourses',
    method: 'post',
    data: data
  })
}

export function saveCourseSelections(data) {
  return request({
    url: '/course/selection/save',
    method: 'post',
    data: data
  })
}
