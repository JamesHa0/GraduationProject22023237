import request from '@/utils/request'

// 查询可选学生列表
export function listStudents(data) {
    return request({
        url: '/mentor/selection/listStudents',
        method: 'post',
        data: data
    })
}

// 提交选中
export function submitSelection(data) {
    return request({
        url: '/mentor/selection/submitSelection',
        method: 'post',
        data: data
    })
}