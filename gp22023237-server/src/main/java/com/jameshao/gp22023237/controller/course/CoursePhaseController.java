package com.jameshao.gp22023237.controller.course;

import com.jameshao.gp22023237.annotation.Log;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.common.enums.BusinessType;
import com.jameshao.gp22023237.service.CoursePhaseService;
import com.jameshao.gp22023237.utils.CurrentUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/course/phase")
public class CoursePhaseController {

    @Autowired
    private CoursePhaseService coursePhaseService;

    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 获取当前课程阶段
     */
    @GetMapping("/current")
    public String getCurrentPhase() {
        try {
            int phase = coursePhaseService.getCurrentPhase();
            return jsonReturn.returnSuccess(phase);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 推进到下一阶段（仅管理员）
     */
    @PostMapping("/advance")
    public String advancePhase(@RequestBody Map<String, String> params) {
        try {
            if (!CurrentUserUtil.isRoundAdmin()) {
                return jsonReturn.returnError("只有超级管理员、综合管理员或教学秘书可以执行此操作");
            }

            String endTime = params.get("endTime");
            boolean success = coursePhaseService.advancePhase(endTime);
            if (success) {
                return jsonReturn.returnSuccess("推进成功");
            } else {
                return jsonReturn.returnFailed("推进失败，当前已是最后阶段");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 重置课程阶段（仅管理员）
     */
    @Log(title = "课程阶段", businessType = BusinessType.UPDATE)
    @PostMapping("/reset")
    public String resetPhase() {
        try {
            if (!CurrentUserUtil.isRoundAdmin()) {
                return jsonReturn.returnError("只有超级管理员、综合管理员或教学秘书可以执行此操作");
            }

            boolean success = coursePhaseService.resetPhase();
            if (success) {
                return jsonReturn.returnSuccess("重置成功");
            } else {
                return jsonReturn.returnFailed("重置失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 更新截止时间（仅管理员）
     */
    @PostMapping("/updateDeadline")
    public String updateDeadline(@RequestBody Map<String, String> params) {
        try {
            if (!CurrentUserUtil.isRoundAdmin()) {
                return jsonReturn.returnError("只有超级管理员、综合管理员或教学秘书可以执行此操作");
            }

            String configKey = params.get("configKey");
            String configValue = params.get("configValue");

            if (configKey == null || configKey.isEmpty()) {
                return jsonReturn.returnError("配置键不能为空");
            }
            if (configValue == null || configValue.isEmpty()) {
                return jsonReturn.returnError("配置值不能为空");
            }

            boolean success = coursePhaseService.updateDeadline(configKey, configValue);
            if (success) {
                return jsonReturn.returnSuccess("更新成功");
            } else {
                return jsonReturn.returnFailed("更新失败，无效的配置键");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取课程阶段所有配置
     */
    @GetMapping("/config")
    public String getPhaseConfig() {
        try {
            Map<String, String> config = coursePhaseService.getPhaseConfig();
            return jsonReturn.returnSuccess(config);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 判断当前是否在选课时间窗口内
     */
    @GetMapping("/isSelectionOpen")
    public String isSelectionOpen() {
        try {
            boolean open = coursePhaseService.isSelectionOpen();
            return jsonReturn.returnSuccess(open);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 判断当前是否在成绩录入时间窗口内
     */
    @GetMapping("/isScoreEntryOpen")
    public String isScoreEntryOpen() {
        try {
            boolean open = coursePhaseService.isScoreEntryOpen();
            return jsonReturn.returnSuccess(open);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取课程阶段统计信息
     */
    @GetMapping("/statistics")
    public String getPhaseStatistics() {
        try {
            Map<String, Object> stats = coursePhaseService.getPhaseStatistics();
            return jsonReturn.returnSuccess(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
