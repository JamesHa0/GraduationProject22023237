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
    params: { id }
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
    params: { id }
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

// 学术附件上传至七牛云
export function uploadAcademicFile(formData) {
  return request({
    url: '/file/upload-academic',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 删除七牛云学术附件
export function deleteAcademicFile(fileUrl) {
  return request({
    url: '/file/delete-academic',
    method: 'post',
    params: { fileUrl }
  })
}

// 获取七牛云私有文件签名URL（用于图片预览等场景）
export function getSignedUrl(fileUrl) {
  return request({
    url: '/file/signed-url',
    method: 'get',
    params: { fileUrl }
  })
}

// 代理下载七牛云私有文件（通过后端转发，支持中文原文件名）
export function downloadFile(fileUrl, fileName) {
  return request({
    url: '/file/download',
    method: 'get',
    params: { fileUrl, fileName },
    responseType: 'blob',
    timeout: 60000
  })
}

// ==================== 新统一接口（v2） ====================

// 统一提交
export function submitContent(data) {
  return request({
    url: '/academic/submit',
    method: 'post',
    data: data
  })
}

// 统一列表
export function listSubmissions(params) {
  return request({
    url: '/academic/list',
    method: 'get',
    params: params
  })
}

// 统一详情
export function getSubmissionDetail(id) {
  return request({
    url: `/academic/detail/${id}`,
    method: 'get'
  })
}

// 统一审批（新接口：action=2通过, 3驳回）
export function approveSubmission(data) {
  return request({
    url: '/academic/approve',
    method: 'post',
    data: data
  })
}

// 统一软删除
export function deleteSubmission(id) {
  return request({
    url: '/academic/delete',
    method: 'post',
    params: { id }
  })
}

// 获取审批历史记录
export function getApprovalRecords(submissionId) {
  return request({
    url: `/academic/approval/records/${submissionId}`,
    method: 'get'
  })
}
