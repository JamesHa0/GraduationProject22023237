import request from '@/utils/request'

// 导师更换申请相关API
export function listMentorChange(params) {
  return request({
    url: '/student/mentor-change/list',
    method: 'get',
    params: params
  })
}

export function getMentorChangeDetail(id) {
  return request({
    url: `/student/mentor-change/${id}`,
    method: 'get'
  })
}

export function submitMentorChange(data) {
  return request({
    url: '/student/mentor-change/submit',
    method: 'post',
    data: data
  })
}

export function approveMentorChangeOriginalMentor(id, status, comment) {
  return request({
    url: '/student/mentor-change/original-mentor/approve',
    method: 'post',
    params: { id, status, comment }
  })
}

export function approveMentorChangeNewMentor(id, status, comment) {
  return request({
    url: '/student/mentor-change/new-mentor/approve',
    method: 'post',
    params: { id, status, comment }
  })
}

// 获取学生当前的导师
export function getStudentCurrentMentor(studentId) {
  return request({
    url: `/selection/relationship/student-current-mentor/${studentId}`,
    method: 'get'
  })
}
