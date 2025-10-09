import request from '@/utils/request'

// 查询可选学生列表
export function listStudents(data) {
    return request({
        url: '/mentor/selection/listStudents',
        method: 'post',
        data: data
    })
}

// 查询学生已选志愿
export function studentChoices(query) {
    return request({
        url: '/selection/getStudentChoices',
        method: 'get',
        params: query
    })
}

// 学生提交志愿
export function submitSelection(data) {
    return request({
        url: '/selection/studentSubmit',
        method: 'post',
        data: data
    })
}

// 学生放弃志愿
export function deselectSelection(data) {
    return request({
        url: '/selection/studentDeselect',
        method: 'post',
        data: data
    })
}