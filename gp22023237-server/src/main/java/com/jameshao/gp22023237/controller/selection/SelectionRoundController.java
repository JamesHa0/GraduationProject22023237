package com.jameshao.gp22023237.controller.selection;

import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.service.SelectionRoundService;
import com.jameshao.gp22023237.utils.CurrentUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/selection/round")
public class SelectionRoundController {

    @Autowired
    private SelectionRoundService selectionRoundService;

    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 获取当前轮次
     */
    @GetMapping("/current")
    public String getCurrentRound() {
        try {
            int currentRound = selectionRoundService.getCurrentRound();
            return jsonReturn.returnSuccess(currentRound);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 切换轮次（仅教学秘书）
     */
    @PostMapping("/switch")
    public String switchRound(@RequestBody Map<String, Integer> params) {
        try {
            // 校验是否为教学秘书
            if (!CurrentUserUtil.isSecretary()) {
                return jsonReturn.returnError("只有教学秘书可以切换轮次");
            }

            Integer targetRound = params.get("targetRound");
            if (targetRound == null || targetRound < 1 || targetRound > 4) {
                return jsonReturn.returnError("无效的轮次值");
            }

            boolean success = selectionRoundService.switchRound(targetRound);
            if (success) {
                return jsonReturn.returnSuccess("切换成功");
            } else {
                return jsonReturn.returnFailed("切换失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取所有轮次配置
     */
    @GetMapping("/config")
    public String getRoundConfig() {
        try {
            Map<String, String> config = selectionRoundService.getRoundConfig();
            return jsonReturn.returnSuccess(config);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 更新轮次配置（仅教学秘书）
     */
    @PostMapping("/config/update")
    public String updateRoundConfig(@RequestBody Map<String, String> params) {
        try {
            // 校验是否为教学秘书
            if (!CurrentUserUtil.isSecretary()) {
                return jsonReturn.returnError("只有教学秘书可以修改配置");
            }

            String configKey = params.get("configKey");
            String configValue = params.get("configValue");

            if (configKey == null || configKey.isEmpty()) {
                return jsonReturn.returnError("配置键不能为空");
            }

            boolean success = selectionRoundService.updateRoundConfig(configKey, configValue);
            if (success) {
                return jsonReturn.returnSuccess("更新成功");
            } else {
                return jsonReturn.returnFailed("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 推进被拒绝学生到下一轮（仅教学秘书）
     */
    @PostMapping("/advanceRejected")
    public String advanceRejectedStudents() {
        try {
            // 校验是否为教学秘书
            if (!CurrentUserUtil.isSecretary()) {
                return jsonReturn.returnError("只有教学秘书可以执行此操作");
            }

            int count = selectionRoundService.advanceRejectedStudents();
            return jsonReturn.returnSuccess("已推进 " + count + " 名学生的志愿");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取未匹配的学生列表
     */
    @GetMapping("/unmatchedStudents")
    public String getUnmatchedStudents() {
        try {
            List<Map<String, Object>> students = selectionRoundService.getUnmatchedStudents();
            return jsonReturn.returnSuccess(students);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 手动分配导师（仅教学秘书）
     */
    @PostMapping("/manualAssign")
    public String manualAssignMentor(@RequestBody Map<String, Long> params) {
        try {
            // 校验是否为教学秘书
            if (!CurrentUserUtil.isSecretary()) {
                return jsonReturn.returnError("只有教学秘书可以执行此操作");
            }

            Long studentId = params.get("studentId");
            Long mentorId = params.get("mentorId");

            if (studentId == null || mentorId == null) {
                return jsonReturn.returnError("学生ID和导师ID不能为空");
            }

            boolean success = selectionRoundService.manualAssignMentor(studentId, mentorId);
            if (success) {
                return jsonReturn.returnSuccess("分配成功");
            } else {
                return jsonReturn.returnFailed("分配失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取各轮次统计数据
     */
    @GetMapping("/statistics")
    public String getRoundStatistics() {
        try {
            Map<String, Object> stats = selectionRoundService.getRoundStatistics();
            return jsonReturn.returnSuccess(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
