package com.jameshao.gp22023237.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.DictData;
import com.jameshao.gp22023237.service.DictDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/dict/data")
public class DictDataController {

    @Autowired
    private DictDataService dictDataService;
    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 查询字典数据列表
     */
    @RequestMapping("/list")
    public String list(Integer pageNum, Integer pageSize, String dictType, String dictLabel, String status) {
        try {
            Page<DictData> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
            LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(!ObjectUtils.isEmpty(dictType), DictData::getDictType, dictType)
                    .like(!ObjectUtils.isEmpty(dictLabel), DictData::getDictLabel, dictLabel)
                    .eq(!ObjectUtils.isEmpty(status), DictData::getStatus, status)
                    .orderByAsc(DictData::getDictSort);
            Page<DictData> result = dictDataService.page(page, queryWrapper);
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
     * 根据字典类型查询字典数据
     */
    @RequestMapping("/type/{dictType}")
    public String dictType(@PathVariable String dictType) {
        try {
            LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DictData::getDictType, dictType)
                    .eq(DictData::getStatus, "0")
                    .orderByAsc(DictData::getDictSort);
            List<DictData> list = dictDataService.list(queryWrapper);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取字典数据详细信息
     */
    @RequestMapping("/{dictCode}")
    public String getInfo(@PathVariable Long dictCode) {
        try {
            DictData dictData = dictDataService.getById(dictCode);
            return jsonReturn.returnSuccess(dictData);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 新增字典数据
     */
    @PostMapping
    public String add(@RequestBody DictData dictData) {
        try {
            dictData.setCreateTime(new Date());
            dictData.setUpdateTime(new Date());
            dictDataService.save(dictData);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 修改字典数据
     */
    @PutMapping
    public String edit(@RequestBody DictData dictData) {
        try {
            dictData.setUpdateTime(new Date());
            dictDataService.updateById(dictData);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 删除字典数据
     */
    @DeleteMapping("/{dictCodes}")
    public String remove(@PathVariable Long[] dictCodes) {
        try {
            for (Long dictCode : dictCodes) {
                dictDataService.removeById(dictCode);
            }
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
