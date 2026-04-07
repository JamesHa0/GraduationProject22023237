package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.po.RoleMenu;

import java.util.List;

/**
* @author test
* @description 针对表【role_menu】的数据库操作Service
* @createDate 2025-10-08 18:16:25
*/
public interface RoleMenuService extends IService<RoleMenu> {

    /**
     * 获取角色已选菜单ID列表
     * @param roleId 角色ID
     * @return 菜单ID列表（menus_index）
     */
    List<Integer> getMenuIdsByRoleId(Integer roleId);

    /**
     * 保存角色菜单权限
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表（menus_index）
     * @return 是否保存成功
     */
    boolean saveRoleMenu(Integer roleId, List<Integer> menuIds);
}
