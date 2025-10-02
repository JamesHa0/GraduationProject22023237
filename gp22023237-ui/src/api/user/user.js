import request from '@/utils/request'

// 获取用户详细信息
export function getInfo(id) {
    return request({
        url: '/user/getInfo',
        method: 'get',
        params: { id }
    })
}

// 查询用户列表
export function list(data) {
    return request({
        url: '/user/list',
        method: 'get',
        params: data
    })
}


// 根据id查用户信息
export function listById(data) {
    return request({
        url: '/user/listById',
        method: 'get',
        params: data
    })
}

// 根据roleId查用户信息
export function listByRoleId(data) {
    return request({
        url: '/user/listByRoleId',
        method: 'get',
        params: data
    })
}
