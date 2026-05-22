package com.jameshao.gp22023237.controller.thesis;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.DTO.GraduationAuditWithDetailsDTO;
import com.jameshao.gp22023237.annotation.Log;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.common.enums.BusinessType;
import com.jameshao.gp22023237.po.GraduationAudit;
import com.jameshao.gp22023237.service.GraduationAuditService;
import com.jameshao.gp22023237.utils.CurrentUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 毕业自动审核控制器
 * 路径前缀: /degree/graduation-audit
 * 实现毕业资格的自动审核与人工审核功能
 */
@RestController
@RequestMapping("/degree/graduation-audit")
public class GraduationAuditController {

    @Autowired
    private GraduationAuditService graduationAuditService;

    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 自动审核毕业资格（单条）
     * 检查学分达标、论文完成、实践满足三项条件，三项全部通过则审核通过
     */
    @Log(title = "毕业自动审核", businessType = BusinessType.UPDATE)
    @PostMapping("/autoAudit")
    public String autoAudit(@RequestBody Map<String, Object> params) {
        try {
            Long studentId = Long.valueOf(params.get("studentId").toString());
            GraduationAudit audit = graduationAuditService.autoAudit(studentId);
            return jsonReturn.returnSuccess(audit);
        } catch (RuntimeException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("自动审核失败，请稍后重试");
        }
    }

    /**
     * 批量自动审核毕业资格
     * 按年级/院系/专业筛选学生并逐个执行自动审核
     */
    @Log(title = "毕业批量审核", businessType = BusinessType.UPDATE)
    @PostMapping("/batchAutoAudit")
    public String batchAutoAudit(@RequestBody Map<String, Object> params) {
        try {
            Integer cohortYear = params.get("cohortYear") != null ? Integer.valueOf(params.get("cohortYear").toString()) : null;
            String department = params.get("department") != null ? params.get("department").toString() : null;
            String major = params.get("major") != null ? params.get("major").toString() : null;

            Map<String, Object> result = graduationAuditService.batchAutoAudit(cohortYear, department, major);
            return jsonReturn.returnSuccess(result);
        } catch (RuntimeException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("批量审核失败，请稍后重试");
        }
    }

    /**
     * 人工审核毕业资格
     */
    @Log(title = "毕业人工审核", businessType = BusinessType.UPDATE)
    @PostMapping("/manualAudit")
    public String manualAudit(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Integer status = Integer.valueOf(params.get("status").toString());
            String comment = params.get("comment") != null ? params.get("comment").toString() : null;
            Long auditorId = CurrentUserUtil.getCurrentUserId();
            String auditorName = CurrentUserUtil.getCurrentUsername();

            boolean success = graduationAuditService.manualAudit(id, status, comment, auditorId, auditorName);
            if (success) {
                return jsonReturn.returnSuccess("审核成功");
            } else {
                return jsonReturn.returnFailed("审核失败");
            }
        } catch (RuntimeException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("人工审核失败，请稍后重试");
        }
    }

    /**
     * 查询毕业审核列表（带学生详情与学分汇总，SQL层分页）
     */
    @GetMapping("/list")
    public String getList(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) String studentNo,
                           @RequestParam(required = false) String studentName,
                           @RequestParam(required = false) Integer auditStatus,
                           @RequestParam(required = false) Integer cohortYear,
                           @RequestParam(required = false) String department,
                           @RequestParam(required = false) String major) {
        try {
            Page<GraduationAuditWithDetailsDTO> page = new Page<>(pageNum, pageSize);
            IPage<GraduationAuditWithDetailsDTO> result = graduationAuditService.listWithDetails(
                    page, studentNo, studentName, auditStatus, cohortYear, department, major);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取毕业审核详情（带学生详情与学分汇总）
     */
    @GetMapping("/{id}")
    public String getDetail(@PathVariable Long id) {
        try {
            GraduationAuditWithDetailsDTO detail = graduationAuditService.getDetailWithDetails(id);
            if (detail != null) {
                return jsonReturn.returnSuccess(detail);
            } else {
                return jsonReturn.returnFailed("未找到审核记录");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取毕业审核统计数据
     */
    @GetMapping("/stats")
    public String getStats() {
        try {
            Map<String, Object> stats = graduationAuditService.getStats();
            return jsonReturn.returnSuccess(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
