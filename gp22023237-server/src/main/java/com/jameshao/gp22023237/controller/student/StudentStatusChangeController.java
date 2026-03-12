package com.jameshao.gp22023237.controller.student;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.StudentStatusChange;
import com.jameshao.gp22023237.service.StudentStatusChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 学籍异动管理Controller
 */
@RestController
@RequestMapping("/student/statusChange")
public class StudentStatusChangeController {

    @Autowired
    private StudentStatusChangeService studentStatusChangeService;

    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 提交学籍异动申请
     */
    @PostMapping("/submit")
    public JSONReturn submitApplication(@RequestBody StudentStatusChange application) {
        try {
            boolean success = studentStatusChangeService.submitSuspensionApplication(application);
            if (success) {
                return jsonReturn.success("申请提交成功", application);
            } else {
                return jsonReturn.fail("申请提交失败");
            }
        } catch (Exception e) {
            return jsonReturn.fail("申请提交失败：" + e.getMessage());
        }
    }

    /**
     * 导师审核
     */
    @PostMapping("/tutorApproval")
    public JSONReturn tutorApproval(@RequestParam Long id,
                                    @RequestParam Integer approval,
                                    @RequestParam Long tutorId) {
        try {
            boolean success = studentStatusChangeService.tutorApproval(id, approval, tutorId);
            if (success) {
                return jsonReturn.success("导师审核成功");
            } else {
                return jsonReturn.fail("导师审核失败");
            }
        } catch (Exception e) {
            return jsonReturn.fail("导师审核失败：" + e.getMessage());
        }
    }

    /**
     * 教学秘书审核
     */
    @PostMapping("/counselorApproval")
    public JSONReturn counselorApproval(@RequestParam Long id,
                                         @RequestParam Integer approval,
                                         @RequestParam Long counselorId) {
        try {
            boolean success = studentStatusChangeService.counselorApproval(id, approval, counselorId);
            if (success) {
                return jsonReturn.success("教学秘书审核成功");
            } else {
                return jsonReturn.fail("教学秘书审核失败");
            }
        } catch (Exception e) {
            return jsonReturn.fail("教学秘书审核失败：" + e.getMessage());
        }
    }

    /**
     * 查询学籍异动列表（分页）
     */
    @GetMapping("/list")
    public JSONReturn list(@RequestParam(defaultValue = "1") int pageNum,
                           @RequestParam(defaultValue = "10") int pageSize,
                           @RequestParam(required = false) Long studentId,
                           @RequestParam(required = false) Integer status,
                           @RequestParam(required = false) Integer type) {
        try {
            QueryWrapper<StudentStatusChange> queryWrapper = new QueryWrapper<>();
            if (studentId != null) {
                queryWrapper.eq("student_id", studentId);
            }
            if (status != null) {
                queryWrapper.eq("status", status);
            }
            if (type != null) {
                queryWrapper.eq("type", type);
            }
            queryWrapper.orderByDesc("create_time");

            Page<StudentStatusChange> page = studentStatusChangeService.page(
                    new Page<>(pageNum, pageSize), queryWrapper);

            return jsonReturn.success("查询成功", page);
        } catch (Exception e) {
            return jsonReturn.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询详情
     */
    @GetMapping("/{id}")
    public JSONReturn getById(@PathVariable Long id) {
        try {
            StudentStatusChange application = studentStatusChangeService.getById(id);
            if (application != null) {
                return jsonReturn.success("查询成功", application);
            } else {
                return jsonReturn.fail("未找到申请记录");
            }
        } catch (Exception e) {
            return jsonReturn.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 取消申请
     */
    @PostMapping("/cancel/{id}")
    public JSONReturn cancel(@PathVariable Long id) {
        try {
            StudentStatusChange application = studentStatusChangeService.getById(id);
            if (application == null) {
                return jsonReturn.fail("未找到申请记录");
            }
            if (application.getStatus() != 0) {
                return jsonReturn.fail("只有待审核的申请才能取消");
            }
            application.setStatus(4); // 已取消
            application.setUpdateTime(new Date());
            boolean success = studentStatusChangeService.updateById(application);
            if (success) {
                return jsonReturn.success("取消成功");
            } else {
                return jsonReturn.fail("取消失败");
            }
        } catch (Exception e) {
            return jsonReturn.fail("取消失败：" + e.getMessage());
        }
    }
}
