package com.jameshao.gp22023237.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.Role;
import com.jameshao.gp22023237.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/role")
public class RoleController {

    @Autowired
    private RoleService roleService;
    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 查询角色列表
     */
    @RequestMapping("/list")
    public String list(Integer pageNum, Integer pageSize, String roleName, String roleKey, String status) {
        try {
            Page<Role> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
            LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(!ObjectUtils.isEmpty(roleName), Role::getName, roleName)
                    .eq(!ObjectUtils.isEmpty(status), Role::getIsDeleted, status);
            Page<Role> result = roleService.page(page, queryWrapper);
            Map<String, Object> data = new HashMap<>();
            data.put("rows", result.getRecords());
            data.put("total", result.getTotal());
            return jsonReturn.returnSuccess(data);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取角色详细信息
     */
    @RequestMapping("/{roleId}")
    public String getInfo(@PathVariable Long roleId) {
        try {
            Role role = roleService.getById(roleId);
            return jsonReturn.returnSuccess(role);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 新增角色
     */
    @PostMapping
    public String add(@RequestBody Role role) {
        try {
            role.setCreateTime(new Date());
            role.setUpdateTime(new Date());
            role.setIsDeleted(0);
            roleService.save(role);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 修改角色
     */
    @PutMapping
    public String edit(@RequestBody Role role) {
        try {
            role.setUpdateTime(new Date());
            roleService.updateById(role);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{roleIds}")
    public String remove(@PathVariable Long[] roleIds) {
        try {
            for (Long roleId : roleIds) {
                roleService.removeById(roleId);
            }
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 查询所有角色
     */
    @RequestMapping("/listAll")
    public String listAll() {
        try {
            List<Role> list = roleService.list();
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
