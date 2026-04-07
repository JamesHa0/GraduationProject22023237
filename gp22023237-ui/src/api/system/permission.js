import request from '@/utils/request'

// 获取角色菜单数据（包含菜单树和已选菜单ID）
export function getRoleMenu(roleId) {
  return request({
    url: '/system/menu/roleMenu/' + roleId,
    method: 'get'
  })
}

// 保存角色菜单权限
export function saveRoleMenu(roleId, menuIds) {
  return request({
    url: '/system/menu/roleMenu/' + roleId,
    method: 'post',
    data: menuIds
  })
}

// 查询角色列表
export function listRoles() {
  return request({
    url: '/system/role/listAll',
    method: 'get'
  })
}
