package com.jameshao.gp22023237.controller.student;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jameshao.gp22023237.DTO.StudentStatusChangeWithDetailsDTO;
import com.jameshao.gp22023237.annotation.Log;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.common.enums.BusinessType;
import com.jameshao.gp22023237.mapper.StudentStatusChangeMapper;
import com.jameshao.gp22023237.po.StudentStatusChange;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.service.StudentStatusChangeService;
import com.jameshao.gp22023237.service.TeacherService;
import com.jameshao.gp22023237.utils.CurrentUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生状态管理控制器
 * 负责处理学生学籍异动申请
 * 学籍异动支持：休学、复学、退学、延期毕业四种类型
 * 路径前缀: /student/status
 */
@RestController
@RequestMapping("/student/status")
public class StudentStatusController {

    @Autowired
    private StudentStatusChangeService studentStatusChangeService;

    @Autowired
    private StudentStatusChangeMapper studentStatusChangeMapper;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 提交学籍异动申请
     */
    @Log(title = "学籍变更", businessType = BusinessType.INSERT)
    @PostMapping("/change/submit")
    public String submitApplication(@RequestBody StudentStatusChange application) {
        try {
            boolean success = studentStatusChangeService.submitApplication(application);
            if (success) {
                return jsonReturn.returnSuccess("申请提交成功");
            } else {
                return jsonReturn.returnFailed("申请提交失败");
            }
        } catch (RuntimeException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("系统异常，请稍后重试");
        }
    }

    /**
     * 查询学籍异动申请列表（带学生详情）
     */
    @GetMapping("/change/list")
    public String getChangeList(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) Long studentId,
                                      @RequestParam(required = false) Integer changeType,
                                      @RequestParam(required = false) String studentNo,
                                      @RequestParam(required = false) String studentName,
                                      @RequestParam(required = false) Integer status) {
        try {
            // 导师登录时，只能看到自己是导师的记录
            Long mentorId = null;
            if (CurrentUserUtil.isMentor()) {
                Long userId = CurrentUserUtil.getCurrentUserId();
                LambdaQueryWrapper<Teacher> teacherWrapper = new LambdaQueryWrapper<>();
                teacherWrapper.eq(Teacher::getUserId, userId);
                Teacher teacher = teacherService.getOne(teacherWrapper);
                if (teacher != null) {
                    mentorId = teacher.getId();
                }
            }

            List<StudentStatusChangeWithDetailsDTO> list = studentStatusChangeMapper.listWithDetails(studentId, changeType, studentNo, studentName, status, mentorId);

            // 手动分页
            int total = list.size();
            int fromIndex = Math.min((pageNum - 1) * pageSize, total);
            int toIndex = Math.min(fromIndex + pageSize, total);
            List<StudentStatusChangeWithDetailsDTO> pageList = list.subList(fromIndex, toIndex);

            Map<String, Object> result = new HashMap<>();
            result.put("records", pageList);
            result.put("total", total);
            result.put("size", pageSize);
            result.put("current", pageNum);

            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取学籍异动申请详情（带学生信息）
     */
    @GetMapping("/change/{id}")
    public String getChangeDetail(@PathVariable Long id) {
        try {
            StudentStatusChangeWithDetailsDTO application = studentStatusChangeMapper.getDetailWithDetails(id);
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
    @Log(title = "学籍变更", businessType = BusinessType.UPDATE)
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
        } catch (RuntimeException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("系统异常，请稍后重试");
        }
    }

    /**
     * 教学秘书审批
     */
    @Log(title = "学籍变更", businessType = BusinessType.UPDATE)
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
        } catch (RuntimeException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("系统异常，请稍后重试");
        }
    }

}
