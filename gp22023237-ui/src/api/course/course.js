import request from '@/utils/request'

// 查询课程列表
export function listCourse(query) {
    return request({
        url: '/course/list',
        method: 'get',
        params: query
    })
}

// 查询课程详情
export function getCourse(id) {
    return request({
        url: `/course/${id}`,
        method: 'get'
    })
}

// 新增课程
export function addCourse(data) {
    return request({
        url: '/course/add',
        method: 'post',
        data: data
    })
}

// 更新课程
export function updateCourse(data) {
    return request({
        url: '/course/update',
        method: 'put',
        data: data
    })
}

// 删除课程
export function delCourse(id) {
    return request({
        url: `/course/delete/${id}`,
        method: 'delete'
    })
}

// 批量删除课程
export function delCourseBatch(ids) {
    return request({
        url: '/course/deleteBatch',
        method: 'delete',
        data: ids
    })
}
