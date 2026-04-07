package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.RoleMenuMapper;
import com.jameshao.gp22023237.po.RoleMenu;
import com.jameshao.gp22023237.service.RoleMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
* @author test
* @description 针对表【role_menu】的数据库操作Service实现
* @createDate 2025-10-08 18:16:25
*/
@Service
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, RoleMenu>
    implements RoleMenuService{

    @Autowired
    private RoleMenuMapper roleMenuMapper;

    @Override
    public List<Integer> getMenuIdsByRoleId(Integer roleId) {
        if (roleId == null) {
            return new ArrayList<>();
        }
        return roleMenuMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRoleMenu(Integer roleId, List<Integer> menuIds) {
        if (roleId == null) {
            return false;
        }

        // 1. 删除该角色原有的所有菜单关联
        roleMenuMapper.deleteByRoleId(roleId);

        // 2. 如果 menuIds 为空，直接返回成功
        if (menuIds == null || menuIds.isEmpty()) {
            return true;
        }

        // 3. 批量插入新的菜单关联
        List<RoleMenu> roleMenuList = new ArrayList<>();
        for (Integer menuId : menuIds) {
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenuList.add(roleMenu);
        }

        int inserted = roleMenuMapper.batchInsert(roleMenuList);
        return inserted > 0;
    }
}
