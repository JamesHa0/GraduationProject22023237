package com.jameshao.gp22023237.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.Menu;
import com.jameshao.gp22023237.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/system/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;
    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 获取菜单：当前项目是角色决定菜单，所以查询条件是角色id
     */
    @RequestMapping("/getRouters")
    public String getMenusByRoleId(Integer roleid){
        System.out.println("roleid:"+roleid);
        try {
            return jsonReturn.returnSuccess(menuService.getByroleid(roleid));
        } catch (Exception e){
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取菜单列表
     */
    @RequestMapping("/list")
    public String list(String menuName, String status) {
        try {
            LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(!ObjectUtils.isEmpty(menuName), Menu::getTitle, menuName)
                    .orderByAsc(Menu::getMenusIndex);
            List<Menu> list = menuService.list(queryWrapper);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取菜单下拉树结构
     */
    @RequestMapping("/treeselect")
    public String treeselect() {
        try {
            LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByAsc(Menu::getMenusIndex);
            List<Menu> list = menuService.list(queryWrapper);
            return jsonReturn.returnSuccess(buildMenuTree(list));
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 根据角色ID查询菜单下拉树结构
     */
    @RequestMapping("/roleMenuTreeselect/{roleId}")
    public String roleMenuTreeselect(@PathVariable Integer roleId) {
        try {
            LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByAsc(Menu::getMenusIndex);
            List<Menu> list = menuService.list(queryWrapper);
            return jsonReturn.returnSuccess(buildMenuTree(list));
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取菜单详细信息
     */
    @RequestMapping("/{menuId}")
    public String getInfo(@PathVariable Integer menuId) {
        try {
            Menu menu = menuService.getById(menuId);
            return jsonReturn.returnSuccess(menu);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 新增菜单
     */
    @PostMapping
    public String add(@RequestBody Menu menu) {
        try {
            menuService.save(menu);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 修改菜单
     */
    @PutMapping
    public String edit(@RequestBody Menu menu) {
        try {
            menuService.updateById(menu);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{menuId}")
    public String remove(@PathVariable Integer menuId) {
        try {
            menuService.removeById(menuId);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 构建菜单树
     * 注意：parent_id关联的是menus_index，不是id
     */
    private List<Menu> buildMenuTree(List<Menu> menus) {
        List<Menu> tree = new ArrayList<>();
        for (Menu menu : menus) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                tree.add(menu);
                addChildren(menu, menus);
            }
        }
        return tree;
    }

    /**
     * 添加子菜单
     * 注意：parent_id关联的是menus_index，不是id
     */
    private void addChildren(Menu parent, List<Menu> menus) {
        List<Menu> children = new ArrayList<>();
        for (Menu menu : menus) {
            if (parent.getMenusIndex() != null && parent.getMenusIndex().equals(menu.getParentId())) {
                children.add(menu);
                addChildren(menu, menus);
            }
        }
        parent.setChildren(children);
    }
}
