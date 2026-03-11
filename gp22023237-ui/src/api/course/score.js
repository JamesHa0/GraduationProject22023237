import request from '@/utils/request'

// 查询成绩列表
export function listScore(query) {
    return request({
        url: '/course/score/list',
        method: 'get',
        params: query
    })
}

// 查询成绩详情
export function getScore(id) {
    return request({
        url: `/course/score/${id}`,
        method: 'get'
    })
}

// 新增成绩
export function addScore(data) {
    return request({
        url: '/course/score/add',
        method: 'post',
        data: data
    })
}

// 更新成绩
export function updateScore(data) {
    return request({
        url: '/course/score/update',
        method: 'put',
        data: data
    })
}

// 删除成绩
export function delScore(id) {
    return request({
        url: `/course/score/delete/${id}`,
        method: 'delete'
    })
}

// 批量删除成绩
export function delScoreBatch(ids) {
    return request({
        url: '/course/score/deleteBatch',
        method: 'delete',
        data: ids
    })
}
