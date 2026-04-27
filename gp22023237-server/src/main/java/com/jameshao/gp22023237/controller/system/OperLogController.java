package com.jameshao.gp22023237.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.annotation.Log;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.common.enums.BusinessType;
import com.jameshao.gp22023237.po.OperLog;
import com.jameshao.gp22023237.service.OperLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/system/oplog")
public class OperLogController {

    @Autowired
    private OperLogService operLogService;
    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 查询操作日志列表
     */
    @RequestMapping("/list")
    public String list(Integer pageNum, Integer pageSize,
                       String title, Integer businessType, String operName,
                       Integer status, String beginTime, String endTime,
                       String orderByColumn, String isAsc) {
        try {
            Page<OperLog> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
            LambdaQueryWrapper<OperLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(!ObjectUtils.isEmpty(title), OperLog::getTitle, title)
                    .eq(!ObjectUtils.isEmpty(businessType), OperLog::getBusinessType, businessType)
                    .like(!ObjectUtils.isEmpty(operName), OperLog::getOperName, operName)
                    .eq(!ObjectUtils.isEmpty(status), OperLog::getStatus, status)
                    .ge(!ObjectUtils.isEmpty(beginTime), OperLog::getOperTime, beginTime)
                    .le(!ObjectUtils.isEmpty(endTime), OperLog::getOperTime, endTime);

            // 排序处理
            if (!ObjectUtils.isEmpty(orderByColumn)) {
                boolean asc = !"desc".equalsIgnoreCase(isAsc);
                switch (orderByColumn) {
                    case "operTime":
                        queryWrapper.orderBy(true, asc, OperLog::getOperTime);
                        break;
                    case "costTime":
                        queryWrapper.orderBy(true, asc, OperLog::getCostTime);
                        break;
                    default:
                        queryWrapper.orderByDesc(OperLog::getOperTime);
                }
            } else {
                queryWrapper.orderByDesc(OperLog::getOperTime);
            }

            Page<OperLog> result = operLogService.page(page, queryWrapper);
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
     * 获取操作日志详细信息
     */
    @RequestMapping("/{operId}")
    public String getInfo(@PathVariable Long operId) {
        try {
            OperLog operLog = operLogService.getById(operId);
            return jsonReturn.returnSuccess(operLog);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 删除操作日志
     */
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{operIds}")
    public String remove(@PathVariable Long[] operIds) {
        try {
            for (Long operId : operIds) {
                operLogService.removeById(operId);
            }
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 清空操作日志
     */
    @DeleteMapping("/clean")
    public String clean() {
        try {
            operLogService.remove(new LambdaQueryWrapper<>());
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
