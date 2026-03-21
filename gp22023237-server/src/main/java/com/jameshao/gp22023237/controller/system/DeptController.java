package com.jameshao.gp22023237.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.Dept;
import com.jameshao.gp22023237.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/system/dept")
public class DeptController {

    @Autowired
    private DeptService deptService;
    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 查询部门列表
     */
    @RequestMapping("/list")
    public String list(String deptName, String status) {
        try {
            LambdaQueryWrapper<Dept> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(!ObjectUtils.isEmpty(deptName), Dept::getDeptName, deptName)
                    .eq(!ObjectUtils.isEmpty(status), Dept::getStatus, status)
                    .eq(Dept::getDelFlag, "0")
                    .orderByAsc(Dept::getOrderNum);
            List<Dept> list = deptService.list(queryWrapper);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 查询部门下拉树结构
     */
    @RequestMapping("/list/exclude/{deptId}")
    public String listExcludeChild(@PathVariable Long deptId) {
        try {
            LambdaQueryWrapper<Dept> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Dept::getDelFlag, "0")
                    .orderByAsc(Dept::getOrderNum);
            List<Dept> list = deptService.list(queryWrapper);
            List<Dept> result = new ArrayList<>();
            for (Dept dept : list) {
                if (!dept.getDeptId().equals(deptId)) {
                    result.add(dept);
                }
            }
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取部门详细信息
     */
    @RequestMapping("/{deptId}")
    public String getInfo(@PathVariable Long deptId) {
        try {
            Dept dept = deptService.getById(deptId);
            return jsonReturn.returnSuccess(dept);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 新增部门
     */
    @PostMapping
    public String add(@RequestBody Dept dept) {
        try {
            dept.setCreateTime(new Date());
            dept.setUpdateTime(new Date());
            dept.setDelFlag("0");
            deptService.save(dept);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 修改部门
     */
    @PutMapping
    public String edit(@RequestBody Dept dept) {
        try {
            dept.setUpdateTime(new Date());
            deptService.updateById(dept);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{deptId}")
    public String remove(@PathVariable Long deptId) {
        try {
            Dept dept = new Dept();
            dept.setDeptId(deptId);
            dept.setDelFlag("2");
            dept.setUpdateTime(new Date());
            deptService.updateById(dept);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
