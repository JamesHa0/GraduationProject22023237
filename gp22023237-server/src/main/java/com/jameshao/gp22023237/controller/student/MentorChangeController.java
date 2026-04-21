package com.jameshao.gp22023237.controller.student;

import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.DTO.MentorChangeApplicationWithDetailsDTO;
import com.jameshao.gp22023237.mapper.MentorChangeApplicationMapper;
import com.jameshao.gp22023237.po.MentorChangeApplication;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.service.MentorChangeApplicationService;
import com.jameshao.gp22023237.service.MentorStudentService;
import com.jameshao.gp22023237.service.TeacherService;
import com.jameshao.gp22023237.utils.CurrentUserUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student/mentor-change")
public class MentorChangeController {

    @Autowired
    private MentorChangeApplicationService mentorChangeApplicationService;

    @Autowired
    private MentorChangeApplicationMapper mentorChangeApplicationMapper;

    @Autowired
    private MentorStudentService mentorStudentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 获取可选导师列表（学生端，用于导师更换申请）
     */
    @GetMapping("/available-mentors")
    public String listAvailableMentors() {
        try {
            List<Map<String, Object>> mentors = mentorStudentService.listAvailableMentorsForStudent();
            return jsonReturn.returnSuccess(mentors);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 提交导师更换申请
     */
    @PostMapping("/submit")
    public String submitApplication(@RequestBody MentorChangeApplication application) {
        try {
            boolean success = mentorChangeApplicationService.submitApplication(application);
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
     * 查询导师更换申请列表（带详情）
     * 老师：只看与自己相关的记录
     * 管理员/教学秘书：看全部记录
     */
    @GetMapping("/list")
    public String getChangeList(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) Long studentId,
                                 @RequestParam(required = false) Integer overallStatus,
                                 @RequestParam(required = false) String studentNo,
                                 @RequestParam(required = false) String studentName,
                                 @RequestParam(required = false) Long originalMentorId,
                                 @RequestParam(required = false) Long newMentorId,
                                 @RequestParam(required = false) Long mentorId) {
        try {
            // 根据角色自动过滤：导师(7)只能看到与自己相关的记录
            Long effectiveMentorId = mentorId;
            if (CurrentUserUtil.isMentor()) {
                Long userId = CurrentUserUtil.getCurrentUserId();
                // 通过userId查找对应的teacher记录获取teacherId
                Teacher teacher = teacherService.lambdaQuery()
                        .eq(Teacher::getUserId, userId).one();
                if (teacher != null) {
                    effectiveMentorId = teacher.getId();
                }
            }

            List<MentorChangeApplicationWithDetailsDTO> list = mentorChangeApplicationMapper.listWithDetails(
                    studentId, studentNo, studentName, overallStatus,
                    originalMentorId, newMentorId, effectiveMentorId);

            // 手动分页
            int total = list.size();
            int fromIndex = Math.min((pageNum - 1) * pageSize, total);
            int toIndex = Math.min(fromIndex + pageSize, total);
            List<MentorChangeApplicationWithDetailsDTO> pageList = list.subList(fromIndex, toIndex);

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
     * 获取导师更换申请详情（带学生和导师信息）
     */
    @GetMapping("/{id}")
    public String getChangeDetail(@PathVariable Long id) {
        try {
            MentorChangeApplicationWithDetailsDTO application = mentorChangeApplicationMapper.getDetailWithDetails(id);
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
     * 原导师审批（仅原导师本人可操作）
     */
    @PostMapping("/original-mentor/approve")
    public String originalMentorApprove(@RequestParam Long id,
                                         @RequestParam Integer status,
                                         @RequestParam(required = false) String comment) {
        try {
            // 权限验证：只有原导师本人可以审批
            MentorChangeApplication app = mentorChangeApplicationService.getById(id);
            if (app == null) {
                return jsonReturn.returnFailed("申请记录不存在");
            }
            if (!CurrentUserUtil.isMentor()) {
                return jsonReturn.returnFailed("只有导师可以审批");
            }
            Long userId = CurrentUserUtil.getCurrentUserId();
            Teacher teacher = teacherService.lambdaQuery()
                    .eq(Teacher::getUserId, userId).one();
            if (teacher == null || !teacher.getId().equals(app.getOriginalMentorId())) {
                return jsonReturn.returnFailed("您不是该申请的原导师，无权审批");
            }

            boolean success = mentorChangeApplicationService.originalMentorApprove(id, status, comment);
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
     * 新导师审批（仅新导师本人可操作）
     */
    @PostMapping("/new-mentor/approve")
    public String newMentorApprove(@RequestParam Long id,
                                      @RequestParam Integer status,
                                      @RequestParam(required = false) String comment) {
        try {
            // 权限验证：只有新导师本人可以审批
            MentorChangeApplication app = mentorChangeApplicationService.getById(id);
            if (app == null) {
                return jsonReturn.returnFailed("申请记录不存在");
            }
            if (!CurrentUserUtil.isMentor()) {
                return jsonReturn.returnFailed("只有导师可以审批");
            }
            Long userId = CurrentUserUtil.getCurrentUserId();
            Teacher teacher = teacherService.lambdaQuery()
                    .eq(Teacher::getUserId, userId).one();
            if (teacher == null || !teacher.getId().equals(app.getNewMentorId())) {
                return jsonReturn.returnFailed("您不是该申请的新导师，无权审批");
            }
            // 前置校验：原导师必须已通过，新导师才能审批
            if (app.getOriginalMentorStatus() == null || app.getOriginalMentorStatus() != 1) {
                return jsonReturn.returnFailed("原导师尚未通过审批，请等待原导师审批");
            }

            boolean success = mentorChangeApplicationService.newMentorApprove(id, status, comment);
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
     * 导出导师更换申请表
     */
    @GetMapping("/export/{id}")
    public void exportMentorChangeApplication(@PathVariable Long id, HttpServletResponse response) {
        try {
            mentorChangeApplicationService.exportMentorChangeApplication(id, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
