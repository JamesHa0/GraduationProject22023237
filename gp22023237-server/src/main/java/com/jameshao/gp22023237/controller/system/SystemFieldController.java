package com.jameshao.gp22023237.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.SystemField;
import com.jameshao.gp22023237.service.SystemFieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/field")
public class SystemFieldController {

    @Autowired
    private SystemFieldService systemFieldService;
    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 查询系统字段列表
     */
    @RequestMapping("/list")
    public String list(Integer pageNum, Integer pageSize, String fieldName, String fieldCode, String module, String status) {
        try {
            Page<SystemField> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
            LambdaQueryWrapper<SystemField> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(!ObjectUtils.isEmpty(fieldName), SystemField::getFieldName, fieldName)
                    .like(!ObjectUtils.isEmpty(fieldCode), SystemField::getFieldCode, fieldCode)
                    .eq(!ObjectUtils.isEmpty(module), SystemField::getModule, module)
                    .eq(!ObjectUtils.isEmpty(status), SystemField::getStatus, status)
                    .orderByAsc(SystemField::getOrderNum);
            Page<SystemField> result = systemFieldService.page(page, queryWrapper);
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
     * 根据模块查询系统字段
     */
    @RequestMapping("/module/{module}")
    public String getByModule(@PathVariable String module) {
        try {
            LambdaQueryWrapper<SystemField> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SystemField::getModule, module)
                    .eq(SystemField::getStatus, "0")
                    .orderByAsc(SystemField::getOrderNum);
            List<SystemField> list = systemFieldService.list(queryWrapper);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取系统字段详细信息
     */
    @RequestMapping("/{fieldId}")
    public String getInfo(@PathVariable Long fieldId) {
        try {
            SystemField systemField = systemFieldService.getById(fieldId);
            return jsonReturn.returnSuccess(systemField);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 新增系统字段
     */
    @PostMapping
    public String add(@RequestBody SystemField systemField) {
        try {
            systemField.setCreateTime(new Date());
            systemField.setUpdateTime(new Date());
            systemFieldService.save(systemField);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 修改系统字段
     */
    @PutMapping
    public String edit(@RequestBody SystemField systemField) {
        try {
            systemField.setUpdateTime(new Date());
            systemFieldService.updateById(systemField);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 删除系统字段
     */
    @DeleteMapping("/{fieldIds}")
    public String remove(@PathVariable Long[] fieldIds) {
        try {
            for (Long fieldId : fieldIds) {
                systemFieldService.removeById(fieldId);
            }
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
