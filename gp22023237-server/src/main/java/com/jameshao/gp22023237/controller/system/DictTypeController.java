package com.jameshao.gp22023237.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.DictType;
import com.jameshao.gp22023237.service.DictTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/dict/type")
public class DictTypeController {

    @Autowired
    private DictTypeService dictTypeService;
    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 查询字典类型列表
     */
    @RequestMapping("/list")
    public String list(Integer pageNum, Integer pageSize, String dictName, String dictType, String status) {
        try {
            Page<DictType> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
            LambdaQueryWrapper<DictType> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(!ObjectUtils.isEmpty(dictName), DictType::getDictName, dictName)
                    .like(!ObjectUtils.isEmpty(dictType), DictType::getDictType, dictType)
                    .eq(!ObjectUtils.isEmpty(status), DictType::getStatus, status);
            Page<DictType> result = dictTypeService.page(page, queryWrapper);
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
     * 获取字典类型详细信息
     */
    @RequestMapping("/{dictId}")
    public String getInfo(@PathVariable Long dictId) {
        try {
            DictType dictType = dictTypeService.getById(dictId);
            return jsonReturn.returnSuccess(dictType);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 新增字典类型
     */
    @PostMapping
    public String add(@RequestBody DictType dictType) {
        try {
            dictType.setCreateTime(new Date());
            dictType.setUpdateTime(new Date());
            dictTypeService.save(dictType);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 修改字典类型
     */
    @PutMapping
    public String edit(@RequestBody DictType dictType) {
        try {
            dictType.setUpdateTime(new Date());
            dictTypeService.updateById(dictType);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 删除字典类型
     */
    @DeleteMapping("/{dictIds}")
    public String remove(@PathVariable Long[] dictIds) {
        try {
            for (Long dictId : dictIds) {
                dictTypeService.removeById(dictId);
            }
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 刷新字典缓存
     */
    @DeleteMapping("/refreshCache")
    public String refreshCache() {
        try {
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取字典选择框列表
     */
    @RequestMapping("/optionselect")
    public String optionselect() {
        try {
            List<DictType> list = dictTypeService.list();
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
