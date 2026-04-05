import request from '@/utils/request'

// 获取当前轮次
export function getCurrentRound() {
  return request({
    url: '/selection/round/current',
    method: 'get'
  })
}

// 获取最大轮次
export function getMaxRound() {
  return request({
    url: '/selection/round/maxRound',
    method: 'get'
  })
}

// 切换轮次（旧接口，保留兼容）
export function switchRound(data) {
  return request({
    url: '/selection/round/switch',
    method: 'post',
    data: data
  })
}

// 推进到下一轮
export function advanceRound(data) {
  return request({
    url: '/selection/round/advance',
    method: 'post',
    data: data
  })
}

// 重置双选
export function resetRounds() {
  return request({
    url: '/selection/round/reset',
    method: 'post'
  })
}

// 获取轮次配置
export function getRoundConfig() {
  return request({
    url: '/selection/round/config',
    method: 'get'
  })
}

// 更新轮次配置
export function updateRoundConfig(data) {
  return request({
    url: '/selection/round/config/update',
    method: 'post',
    data: data
  })
}

// 推进被拒绝学生
export function advanceRejected() {
  return request({
    url: '/selection/round/advanceRejected',
    method: 'post'
  })
}

// 获取未匹配学生列表
export function getUnmatchedStudents() {
  return request({
    url: '/selection/round/unmatchedStudents',
    method: 'get'
  })
}

// 手动分配导师
export function manualAssign(data) {
  return request({
    url: '/selection/round/manualAssign',
    method: 'post',
    data: data
  })
}

// 获取轮次统计数据
export function getRoundStatistics() {
  return request({
    url: '/selection/round/statistics',
    method: 'get'
  })
}

// 判断学生是否可以选择
export function canStudentSelect() {
  return request({
    url: '/selection/round/canStudentSelect',
    method: 'get'
  })
}

// 判断导师是否可以选择
export function canMentorSelect() {
  return request({
    url: '/selection/round/canMentorSelect',
    method: 'get'
  })
}

// 获取当前阶段状态
export function getCurrentPhase() {
  return request({
    url: '/selection/round/currentPhase',
    method: 'get'
  })
}
