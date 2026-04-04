import request from '@/utils/request'

// 获取导师学生关系列表
export function listRelationship(data) {
  return request({
    url: '/selection/relationship/list',
    method: 'get',
    params: data
  })
}

// 获取关系详情
export function getRelationship(id) {
  return request({
    url: `/selection/relationship/${id}`,
    method: 'get'
  })
}

// 创建关系
export function createRelationship(data) {
  return request({
    url: '/selection/relationship/create',
    method: 'post',
    data: data
  })
}

// 更新关系
export function updateRelationship(data) {
  return request({
    url: '/selection/relationship/update',
    method: 'post',
    data: data
  })
}

// 删除关系
export function deleteRelationship(id) {
  return request({
    url: '/selection/relationship/delete',
    method: 'post',
    params: { id }
  })
}

// 获取可选学生列表
export function listStudents() {
  return request({
    url: '/selection/relationship/students',
    method: 'get'
  })
}

// 获取可选导师列表
export function listMentors() {
  return request({
    url: '/selection/relationship/mentors',
    method: 'get'
  })
}
