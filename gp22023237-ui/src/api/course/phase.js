import request from '@/utils/request'

// 获取当前课程阶段
export function getCurrentPhase() {
  return request({
    url: '/course/phase/current',
    method: 'get'
  })
}

// 推进到下一阶段
export function advancePhase(data) {
  return request({
    url: '/course/phase/advance',
    method: 'post',
    data: data
  })
}

// 重置课程阶段
export function resetPhase() {
  return request({
    url: '/course/phase/reset',
    method: 'post'
  })
}

// 更新截止时间
export function updateDeadline(data) {
  return request({
    url: '/course/phase/updateDeadline',
    method: 'post',
    data: data
  })
}

// 获取课程阶段配置
export function getPhaseConfig() {
  return request({
    url: '/course/phase/config',
    method: 'get'
  })
}

// 判断是否在选课时间窗口内
export function isSelectionOpen() {
  return request({
    url: '/course/phase/isSelectionOpen',
    method: 'get'
  })
}

// 判断是否在成绩录入时间窗口内
export function isScoreEntryOpen() {
  return request({
    url: '/course/phase/isScoreEntryOpen',
    method: 'get'
  })
}

// 获取课程阶段统计信息
export function getPhaseStatistics() {
  return request({
    url: '/course/phase/statistics',
    method: 'get'
  })
}
