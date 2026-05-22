package com.jameshao.gp22023237.controller.selection;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.service.TeacherService;
import com.jameshao.gp22023237.utils.CurrentUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 导师名额管理控制器
 * 用于管理导师的招生名额（quota）、剩余名额（remaining_quota）
 * 权限：仅超级管理员(1)、综合管理员(4)、教学秘书(5)可操作
 */
@RestController
@RequestMapping("/selection/quota")
public class QuotaManageController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 分页查询导师名额列表（仅is_mentor=1的导师）
     */
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String teacherName,
                       @RequestParam(required = false) String department) {
        try {
            Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
            IPage<Map<String, Object>> result = teacherService.pageQuotaList(page, teacherName, department);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 单个导师名额更新（仅轮次管理员）
     */
    @PostMapping("/update")
    public String update(@RequestBody Map<String, Object> params) {
        try {
            if (!CurrentUserUtil.isRoundAdmin()) {
                return jsonReturn.returnError("只有超级管理员、综合管理员或教学秘书可以修改名额");
            }

            Long teacherId = Long.valueOf(params.get("teacherId").toString());
            Integer quota = Integer.valueOf(params.get("quota").toString());

            boolean success = teacherService.updateQuota(teacherId, quota);
            return success ? jsonReturn.returnSuccess("更新成功") : jsonReturn.returnError("更新失败");
        } catch (IllegalArgumentException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("更新失败：" + e.getMessage());
        }
    }

    /**
     * 批量导师名额更新（仅轮次管理员）
     */
    @PostMapping("/batchUpdate")
    public String batchUpdate(@RequestBody Map<String, Object> params) {
        try {
            if (!CurrentUserUtil.isRoundAdmin()) {
                return jsonReturn.returnError("只有超级管理员、综合管理员或教学秘书可以修改名额");
            }

            @SuppressWarnings("unchecked")
            List<Number> idList = (List<Number>) params.get("teacherIds");
            Integer quota = Integer.valueOf(params.get("quota").toString());

            if (idList == null || idList.isEmpty()) {
                return jsonReturn.returnError("请选择至少一位导师");
            }

            List<Long> teacherIds = idList.stream().map(Number::longValue).toList();
            Map<String, Object> result = teacherService.batchUpdateQuota(teacherIds, quota);
            return jsonReturn.returnSuccess(result);
        } catch (IllegalArgumentException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("批量更新失败：" + e.getMessage());
        }
    }

    /**
     * 重置所有导师名额为0（仅轮次管理员）
     */
    @PostMapping("/reset")
    public String reset() {
        try {
            if (!CurrentUserUtil.isRoundAdmin()) {
                return jsonReturn.returnError("只有超级管理员、综合管理员或教学秘书可以执行此操作");
            }

            boolean success = teacherService.resetAllQuota();
            return success ? jsonReturn.returnSuccess("重置成功") : jsonReturn.returnError("重置失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("重置失败：" + e.getMessage());
        }
    }
}
