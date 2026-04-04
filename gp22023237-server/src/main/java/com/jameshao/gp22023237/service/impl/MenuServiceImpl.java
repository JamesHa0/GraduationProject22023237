package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.Menu;
import com.jameshao.gp22023237.service.MenuService;
import com.jameshao.gp22023237.mapper.MenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author test
* @description 针对表【menu】的数据库操作Service实现
* @createDate 2025-09-21 16:57:09
*/
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu>
    implements MenuService{

    @Autowired
    MenuMapper menuMapper;

    @Override
    public List<Menu> getByroleid(Integer roleid) {
        // 1. 查询该角色有权限的所有菜单
        List<Menu> allMenus = menuMapper.selectbyroleid(roleid);
        if (allMenus == null || allMenus.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 按 parent_id 分组，构建菜单树
        // 先找出所有一级菜单（parent_id = 0）
        List<Menu> rootMenus = allMenus.stream()
                .filter(menu -> menu.getParentId() == null || menu.getParentId() == 0)
                .collect(Collectors.toList());

        // 把子菜单分配给对应的父菜单
        // 注意：parent_id 关联的是 menu.menus_index 字段，不是 id 字段！
        Map<Integer, List<Menu>> childrenMap = allMenus.stream()
                .filter(menu -> menu.getParentId() != null && menu.getParentId() != 0)
                .collect(Collectors.groupingBy(Menu::getParentId));

        for (Menu root : rootMenus) {
            List<Menu> children = childrenMap.get(root.getMenusIndex());
            root.setChildren(children != null ? children : new ArrayList<>());
        }

        return rootMenus;
    }
}




