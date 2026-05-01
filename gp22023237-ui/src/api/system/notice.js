import request from '@/utils/request'

// 查询公告列表
export function listNotice(query) {
  return request({
    url: '/system/notice/list',
    method: 'get',
    params: query
  })
}

// 查询公告详细
export function getNotice(noticeId) {
  return request({
    url: '/system/notice/' + noticeId,
    method: 'get'
  })
}

// 新增公告
export function addNotice(data) {
  return request({
    url: '/system/notice',
    method: 'post',
    data: data
  })
}

// 修改公告
export function updateNotice(data) {
  return request({
    url: '/system/notice',
    method: 'put',
    data: data
  })
}

// 删除公告
export function delNotice(noticeId) {
  return request({
    url: '/system/notice/' + noticeId,
    method: 'delete'
  })
}

// 获取未读通知数量
export function getUnreadCount() {
  return request({
    url: '/system/notice/unread-count',
    method: 'get'
  })
}

// 获取未读通知列表
export function getUnreadList() {
  return request({
    url: '/system/notice/unread-list',
    method: 'get'
  })
}

// 标记通知为已读
export function markAsRead(noticeId) {
  return request({
    url: '/system/notice/read/' + noticeId,
    method: 'put'
  })
}

// 标记所有通知为已读
export function markAllAsRead() {
  return request({
    url: '/system/notice/read-all',
    method: 'put'
  })
}

// 获取当前用户可见通知列表（含已读状态，分页）
export function getUserNoticeList(query) {
  return request({
    url: '/system/notice/user-list',
    method: 'get',
    params: query
  })
}
