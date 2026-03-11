import request from '@/utils/request'

// 查询选课记录列表
export function listCourseSelection(query) {
    return request({
        url: '/course/selection/list',
        method: 'get',
        params: query
    })
}

// 查询选课记录详情
export function getCourseSelection(id) {
    return request({
        url: `/course/selection/${id}`,
        method: 'get'
    })
}

// 新增选课记录
export function addCourseSelection(data) {
    return request({
        url: '/course/selection/add',
        method: 'post',
        data: data
    })
}

// 更新选课记录
export function updateCourseSelection(data) {
    return request({
        url: '/course/selection/update',
        method: 'put',
        data: data
    })
}

// 删除选课记录
export function delCourseSelection(id) {
    return request({
        url: `/course/selection/delete/${id}`,
        method: 'delete'
    })
}

// 批量删除选课记录
export function delCourseSelectionBatch(ids) {
    return request({
        url: '/course/selection/deleteBatch',
        method: 'delete',
        data: ids
    })
}
