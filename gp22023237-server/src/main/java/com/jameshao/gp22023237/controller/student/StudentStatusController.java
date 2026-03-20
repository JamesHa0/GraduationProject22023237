package com.jameshao.gp22023237.controller.student;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.GraduationAudit;
import com.jameshao.gp22023237.po.StudentStatusChange;
import com.jameshao.gp22023237.service.GraduationAuditService;
import com.jameshao.gp22023237.service.StudentStatusChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 学生状态管理控制器
 * 负责处理学生学籍异动申请和毕业资格审核
 * 学籍异动支持：休学、复学、退学、延期毕业四种类型
 * 毕业资格审核支持自动审核和人工审核两种模式
 * 路径前缀: /student/status
 */
@RestController
@RequestMapping("/student/status")
public class StudentStatusController {

    @Autowired
    private StudentStatusChangeService studentStatusChangeService;

    @Autowired
    private GraduationAuditService graduationAuditService;

    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 提交学籍异动申请
     */
    @PostMapping("/change/submit")
    public String submitApplication(@RequestBody StudentStatusChange application) {
        try {
            boolean success = studentStatusChangeService.submitApplication(application);
            if (success) {
                return jsonReturn.returnSuccess("申请提交成功");
            } else {
                return jsonReturn.returnFailed("申请提交失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 查询学籍异动申请列表
     */
    @GetMapping("/change/list")
    public String getChangeList(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) Long studentId,
                                      @RequestParam(required = false) Integer changeType) {
        try {
            Page<StudentStatusChange> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<StudentStatusChange> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(StudentStatusChange::getCreateTime); // 使用数据库实际字段进行排序

            if (studentId != null) {
                wrapper.eq(StudentStatusChange::getStudentId, studentId);
            }
            if (changeType != null) {
                wrapper.eq(StudentStatusChange::getChangeType, changeType);
            }

            IPage<StudentStatusChange> result = studentStatusChangeService.page(page, wrapper);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取学籍异动申请详情

     */
    @GetMapping("/change/{id}")
    public String getChangeDetail(@PathVariable Long id) {
        try {
            StudentStatusChange application = studentStatusChangeService.getById(id);
            if (application != null) {
                return jsonReturn.returnSuccess(application);
            } else {
                return jsonReturn.returnFailed("未找到申请记录");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 导师审批
     */
    @PostMapping("/change/mentor/approve")
    public String mentorApprove(@RequestParam Long id,
                                    @RequestParam Integer status,
                                    @RequestParam(required = false) String comment) {
        try {
            boolean success = studentStatusChangeService.mentorApprove(id, status, comment);
            if (success) {
                return jsonReturn.returnSuccess("审批成功");
            } else {
                return jsonReturn.returnFailed("审批失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 教学秘书审批
     */
    @PostMapping("/change/secretary/approve")
    public String secretaryApprove(@RequestParam Long id,
                                      @RequestParam Integer status,
                                      @RequestParam(required = false) String comment) {
        try {
            boolean success = studentStatusChangeService.secretaryApprove(id, status, comment);
            if (success) {
                return jsonReturn.returnSuccess("审批成功");
            } else {
                return jsonReturn.returnFailed("审批失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 分管院长审批
     */
    @PostMapping("/change/dean/approve")
    public String deanApprove(@RequestParam Long id,
                                  @RequestParam Integer status,
                                  @RequestParam(required = false) String comment) {
        try {
            boolean success = studentStatusChangeService.deanApprove(id, status, comment);
            if (success) {
                return jsonReturn.returnSuccess("审批成功");
            } else {
                return jsonReturn.returnFailed("审批失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 自动审核毕业资格
     */
    @PostMapping("/graduation/autoAudit")
    public String autoAuditGraduation(@RequestParam Long studentId) {
        try {
            GraduationAudit audit = graduationAuditService.autoAudit(studentId);
            return jsonReturn.returnSuccess(audit);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 查询毕业审核列表
     */
    @GetMapping("/graduation/list")
    public String getGraduationList(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) Long studentId,
                                         @RequestParam(required = false) Integer auditStatus) {
        try {
            Page<GraduationAudit> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<GraduationAudit> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(GraduationAudit::getCreateTime); // 使用数据库实际字段

            if (studentId != null) {
                wrapper.eq(GraduationAudit::getStudentId, studentId);
            }
            if (auditStatus != null) {
                wrapper.eq(GraduationAudit::getAuditStatus, auditStatus);
            }

            IPage<GraduationAudit> result = graduationAuditService.page(page, wrapper);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 人工审核毕业资格
     */
    @PostMapping("/graduation/manualAudit")
    public String manualAudit(@RequestParam Long id,
                                  @RequestParam Integer status,
                                  @RequestParam(required = false) String comment,
                                  @RequestParam Long auditorId,
                                  @RequestParam String auditorName) {
        try {
            boolean success = graduationAuditService.manualAudit(id, status, comment, auditorId, auditorName);
            if (success) {
                return jsonReturn.returnSuccess("审核成功");
            } else {
                return jsonReturn.returnFailed("审核失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取毕业审核统计
     */
    @GetMapping("/graduation/stats")
    public String getGraduationStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            long total = graduationAuditService.count();
            long passed = graduationAuditService.count(
                    new LambdaQueryWrapper<GraduationAudit>().eq(GraduationAudit::getAuditStatus, 1));
            long auditing = graduationAuditService.count(
                    new LambdaQueryWrapper<GraduationAudit>().eq(GraduationAudit::getAuditStatus, 0));
            long failed = graduationAuditService.count(
                    new LambdaQueryWrapper<GraduationAudit>().eq(GraduationAudit::getAuditStatus, 2));

            stats.put("total", total);
            stats.put("passed", passed);
            stats.put("auditing", auditing);
            stats.put("failed", failed);

            return jsonReturn.returnSuccess(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
