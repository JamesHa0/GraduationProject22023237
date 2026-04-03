import request from '@/utils/request'

// 学术活动相关API
export function listActivity(params) {
  return request({
    url: '/academic/activity/list',
    method: 'get',
    params: params
  })
}

export function getActivityDetail(id) {
  return request({
    url: `/academic/activity/${id}`,
    method: 'get'
  })
}

export function submitActivity(data) {
  return request({
    url: '/academic/activity/submit',
    method: 'post',
    data: data
  })
}

export function approveActivity(data) {
  return request({
    url: '/academic/activity/approve',
    method: 'post',
    data: data
  })
}

export function approveActivityMentor(id, status, comment) {
  return request({
    url: '/academic/activity/mentor/approve',
    method: 'post',
    params: { id, status, comment }
  })
}

export function approveActivitySecretary(id, status, comment) {
  return request({
    url: '/academic/activity/secretary/approve',
    method: 'post',
    params: { id, status, comment }
  })
}

export function approveActivityDean(id, status, comment) {
  return request({
    url: '/academic/activity/dean/approve',
    method: 'post',
    params: { id, status, comment }
  })
}

// 创新实践项目相关API
export function listInnovation(params) {
  return request({
    url: '/academic/innovation/list',
    method: 'get',
    params: params
  })
}

export function getInnovationDetail(id) {
  return request({
    url: `/academic/innovation/${id}`,
    method: 'get'
  })
}

export function submitInnovation(data) {
  return request({
    url: '/academic/innovation/submit',
    method: 'post',
    data: data
  })
}

export function approveInnovation(data) {
  return request({
    url: '/academic/innovation/approve',
    method: 'post',
    data: data
  })
}

export function approveInnovationMentor(id, status, comment) {
  return request({
    url: '/academic/innovation/mentor/approve',
    method: 'post',
    params: { id, status, comment }
  })
}

export function approveInnovationSecretary(id, status, comment) {
  return request({
    url: '/academic/innovation/secretary/approve',
    method: 'post',
    params: { id, status, comment }
  })
}

export function approveInnovationDean(id, status, comment) {
  return request({
    url: '/academic/innovation/dean/approve',
    method: 'post',
    params: { id, status, comment }
  })
}

export function delInnovation(id) {
  return request({
    url: '/academic/innovation/delete',
    method: 'post',
    data: { id }
  })
}

// 学术成果相关API
export function listAchievement(params) {
  return request({
    url: '/academic/achievement/list',
    method: 'get',
    params: params
  })
}

export function getAchievementDetail(id) {
  return request({
    url: `/academic/achievement/${id}`,
    method: 'get'
  })
}

export function submitAchievement(data) {
  return request({
    url: '/academic/achievement/submit',
    method: 'post',
    data: data
  })
}

export function approveAchievement(data) {
  return request({
    url: '/academic/achievement/approve',
    method: 'post',
    data: data
  })
}

export function approveAchievementMentor(id, status, comment) {
  return request({
    url: '/academic/achievement/mentor/approve',
    method: 'post',
    params: { id, status, comment }
  })
}

export function approveAchievementSecretary(id, status, comment) {
  return request({
    url: '/academic/achievement/secretary/approve',
    method: 'post',
    params: { id, status, comment }
  })
}

export function approveAchievementDean(id, status, comment) {
  return request({
    url: '/academic/achievement/dean/approve',
    method: 'post',
    params: { id, status, comment }
  })
}

export function delAchievement(id) {
  return request({
    url: '/academic/achievement/delete',
    method: 'post',
    data: { id }
  })
}

// 统一审核相关API
export function listReview(params) {
  return request({
    url: '/academic/review/list',
    method: 'get',
    params: params
  })
}

export function approveReview(data) {
  return request({
    url: '/academic/review/approve',
    method: 'post',
    data: data
  })
}
