import request from '@/utils/request'

// 查询可选导师列表
export function listMentor(query) {
    return request({
        url: '/student/selection/listMentor',
        method: 'get',
        params: query
    })
}

// 查询学生已选志愿
export function studentChoices(query) {
    return request({
        url: '/student/selection/getStudentChoices',
        method: 'get',
        params: query
    })
}

// 学生提交志愿
export function submitSelection(data) {
    return request({
        url: '/student/selection/studentSubmit',
        method: 'post',
        data: data
    })
}

// 学生放弃志愿
export function deselectSelection(data) {
    return request({
        url: '/student/selection/studentDeselect',
        method: 'post',
        data: data
    })
}