package com.jameshao.gp22023237.controller.thesis;

import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.annotation.Log;
import com.jameshao.gp22023237.common.enums.BusinessType;
import com.jameshao.gp22023237.po.ThesisGrade;
import com.jameshao.gp22023237.service.ThesisGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 成绩评定控制器
 * 路径前缀: /thesis/grade
 */
@RestController
@RequestMapping("/thesis/grade")
public class ThesisGradeController {

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private ThesisGradeService thesisGradeService;

    /** 获取某论文的成绩评定 */
    @GetMapping("/{thesisId}")
    public String getGrade(@PathVariable Long thesisId) {
        try {
            ThesisGrade grade = thesisGradeService.getByThesisId(thesisId);
            return grade != null ? jsonReturn.returnSuccess(grade) : jsonReturn.returnSuccess(null);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /** 指导教师评分 */
    @Log(title = "成绩评定", businessType = BusinessType.UPDATE)
    @PostMapping("/supervisor")
    public String supervisorScore(@RequestBody Map<String, Object> params) {
        try {
            Long thesisId = Long.valueOf(params.get("thesisId").toString());
            BigDecimal score = new BigDecimal(params.get("score").toString());
            String comment = (String) params.getOrDefault("comment", "");
            boolean success = thesisGradeService.supervisorScore(thesisId, score, comment);
            return success ? jsonReturn.returnSuccess("评分成功") : jsonReturn.returnError("评分失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /** 评阅教师评分 */
    @Log(title = "成绩评定", businessType = BusinessType.UPDATE)
    @PostMapping("/reviewer")
    public String reviewerScore(@RequestBody Map<String, Object> params) {
        try {
            Long thesisId = Long.valueOf(params.get("thesisId").toString());
            BigDecimal score = new BigDecimal(params.get("score").toString());
            String comment = (String) params.getOrDefault("comment", "");
            boolean success = thesisGradeService.reviewerScore(thesisId, score, comment);
            return success ? jsonReturn.returnSuccess("评分成功") : jsonReturn.returnError("评分失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /** 答辩评分 */
    @Log(title = "成绩评定", businessType = BusinessType.UPDATE)
    @PostMapping("/defense")
    public String defenseScore(@RequestBody Map<String, Object> params) {
        try {
            Long thesisId = Long.valueOf(params.get("thesisId").toString());
            BigDecimal score = new BigDecimal(params.get("score").toString());
            String comment = (String) params.getOrDefault("comment", "");
            boolean success = thesisGradeService.defenseScore(thesisId, score, comment);
            return success ? jsonReturn.returnSuccess("评分成功") : jsonReturn.returnError("评分失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /** 计算总评 */
    @Log(title = "成绩评定", businessType = BusinessType.UPDATE)
    @PostMapping("/calculate")
    public String calculateTotal(@RequestParam Long thesisId) {
        try {
            ThesisGrade grade = thesisGradeService.calculateTotal(thesisId);
            return grade != null ? jsonReturn.returnSuccess(grade) : jsonReturn.returnError("计算失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
