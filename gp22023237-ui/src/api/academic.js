import request from '@/utils/request'

// 学术活动相关API
export function listActivity(params) {
  return request({
    url: '/academic/activity/list',
    method: 'get',
    params: params
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

// 创新实践项目相关API
export function listInnovation(params) {
  return request({
    url: '/academic/innovation/list',
    method: 'get',
    params: params
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

export function delAchievement(id) {
  return request({
    url: '/academic/achievement/delete',
    method: 'post',
    data: { id }
  })
}
