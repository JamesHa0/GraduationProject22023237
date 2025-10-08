import request from '@/utils/request'

// 查询可选导师列表
export function listMentor(query) {
    return request({
        url: '/selection/listMentor',
        method: 'get',
        params: query
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

// 提交志愿
export function submitSelection(data) {
    return request({
        url: '/selection/studentSubmit',
        method: 'post',
        data: data
    })
}

// // 提交修改内容
// export function update(data) {
//     return request({
//         url: '/nurse/nurseItem/update',
//         method: 'post',
//         data: data
//     })
// }

// // 删除护理项目
// export function deleteItem(id) {
//     return request({
//         url: '/nurse/nurseItem/delete',
//         method: 'delete',
//         params: { id }
//     })
// }
