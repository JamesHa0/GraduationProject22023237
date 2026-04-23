import request from '@/utils/request'

// 提交教学评价
export function submitEvaluation(data) {
  return request({
    url: '/course/evaluation/submit',
    method: 'post',
    params: { studentId: data.studentId },
    data: data
  })
}

// 查询待评价课程列表
export function listPendingCourses(studentId) {
  return request({
    url: '/course/evaluation/pending',
    method: 'get',
    params: { studentId }
  })
}

// 查询已评价课程列表
export function listCompletedCourses(studentId) {
  return request({
    url: '/course/evaluation/completed',
    method: 'get',
    params: { studentId }
  })
}

// 检查某学生某课程是否已评价
export function checkEvaluated(studentId, courseId) {
  return request({
    url: '/course/evaluation/check',
    method: 'get',
    params: { studentId, courseId }
  })
}

// 教师查看自己收到的评价统计
export function getTeacherStats(teacherId) {
  return request({
    url: '/course/evaluation/teacherStats',
    method: 'get',
    params: { teacherId }
  })
}
