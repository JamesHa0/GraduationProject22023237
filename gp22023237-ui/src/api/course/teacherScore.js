import request from '@/utils/request'

// 查询某课程下所有选课学生及成绩状态（教师专用）
export function listCourseStudents(query) {
  return request({
    url: '/course/score/courseStudents',
    method: 'get',
    params: query
  })
}

// 下载预填充学生数据的成绩导入模板
export function downloadTemplateWithStudents(query) {
  return request({
    url: '/course/score/importTemplateWithStudents',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
