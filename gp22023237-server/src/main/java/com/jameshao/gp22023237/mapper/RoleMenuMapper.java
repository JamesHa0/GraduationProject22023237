package com.jameshao.gp22023237.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jameshao.gp22023237.po.RoleMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author test
* @description 针对表【role_menu】的数据库操作Mapper
* @createDate 2025-10-08 18:16:25
* @Entity com.jameshao.gp22023237.po.RoleMenu
*/
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    /**
     * 根据角色ID查询菜单ID列表
     * @param roleId 角色ID
     * @return 菜单ID列表（menus_index）
     */
    List<Integer> selectMenuIdsByRoleId(@Param("roleId") Integer roleId);

    /**
     * 批量插入角色菜单关联
     * @param list 角色菜单关联列表
     * @return 插入数量
     */
    int batchInsert(@Param("list") List<RoleMenu> list);

    /**
     * 根据角色ID删除关联
     * @param roleId 角色ID
     * @return 删除数量
     */
    int deleteByRoleId(@Param("roleId") Integer roleId);
}
