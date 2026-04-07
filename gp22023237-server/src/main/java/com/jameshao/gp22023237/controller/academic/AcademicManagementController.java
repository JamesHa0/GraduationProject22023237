package com.jameshao.gp22023237.controller.academic;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.DTO.AcademicActivityWithDetailsDTO;
import com.jameshao.gp22023237.DTO.AcademicAchievementWithDetailsDTO;
import com.jameshao.gp22023237.DTO.InnovationProjectWithDetailsDTO;
import com.jameshao.gp22023237.DTO.ReviewItemDTO;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.mapper.AcademicActivityMapper;
import com.jameshao.gp22023237.mapper.AcademicAchievementMapper;
import com.jameshao.gp22023237.mapper.InnovationProjectMapper;
import com.jameshao.gp22023237.utils.CurrentUserUtil;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import com.jameshao.gp22023237.po.AcademicAchievement;
import com.jameshao.gp22023237.po.AcademicActivity;
import com.jameshao.gp22023237.po.InnovationProject;
import com.jameshao.gp22023237.po.MentorStudent;
import com.jameshao.gp22023237.po.Student;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.service.AcademicActivityService;
import com.jameshao.gp22023237.service.AcademicAchievementService;
import com.jameshao.gp22023237.service.InnovationProjectService;
import com.jameshao.gp22023237.service.MentorStudentService;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 学术管理控制器
 * 负责处理学术活动、创新项目、学术成果的提交和审批流程
 * 支持导师、教学秘书、分管院长三级审批体系
 * 路径前缀: /academic
 */
@RestController
@RequestMapping("/academic")
public class AcademicManagementController {

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private AcademicActivityService academicActivityService;

    @Autowired
    private AcademicActivityMapper academicActivityMapper;

    @Autowired
    private InnovationProjectService innovationProjectService;

    @Autowired
    private InnovationProjectMapper innovationProjectMapper;

    @Autowired
    private AcademicAchievementService academicAchievementService;

    @Autowired
    private AcademicAchievementMapper academicAchievementMapper;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private MentorStudentService mentorStudentService;

    /**
     * 权限过滤结果类
     */
    private static class FilterResult {
        Long studentId;
        List<Long> studentIds;

        FilterResult(Long studentId, List<Long> studentIds) {
            this.studentId = studentId;
            this.studentIds = studentIds;
        }
    }

    // ==================== 权限辅助方法 ====================

    /**
     * 获取当前登录学生的ID
     * 如果当前用户是学生角色，则返回对应的studentId
     * 否则返回null
     * @return 学生ID或null
     */
    private Long getCurrentStudentId() {
        Integer roleId = CurrentUserUtil.getCurrentRoleId();
        // 角色ID: 1-学生
        if (roleId != null && roleId == 1) {
            Long userId = CurrentUserUtil.getCurrentUserId();
            if (userId != null) {
                // 根据userId查询对应的student记录
                LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Student::getUserId, userId);
                Student student = studentService.getOne(queryWrapper);
                if (student != null) {
                    return student.getId();
                }
            }
        }
        return null;
    }

    /**
     * 获取当前登录导师的教师ID
     * @return 教师ID或null
     */
    private Long getCurrentMentorTeacherId() {
        Integer roleId = CurrentUserUtil.getCurrentRoleId();
        // 角色ID: 3-指导教师
        if (roleId != null && roleId == 3) {
            Long userId = CurrentUserUtil.getCurrentUserId();
            if (userId != null) {
                // 根据userId查询对应的teacher记录
                LambdaQueryWrapper<Teacher> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Teacher::getUserId, userId);
                Teacher teacher = teacherService.getOne(queryWrapper);
                if (teacher != null) {
                    return teacher.getId();
                }
            }
        }
        return null;
    }

    /**
     * 获取导师的学生ID列表
     * @param mentorId 导师ID
     * @return 学生ID列表
     */
    private List<Long> getMentorStudentIds(Long mentorId) {
        List<Long> studentIds = new ArrayList<>();
        if (mentorId == null) {
            return studentIds;
        }
        // 查询导师确认的学生关系（teacherStatus = 1 表示已确认）
        LambdaQueryWrapper<MentorStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MentorStudent::getMentorId, mentorId)
                .eq(MentorStudent::getTeacherStatus, 1);
        List<MentorStudent> relations = mentorStudentService.list(queryWrapper);
        for (MentorStudent relation : relations) {
            if (relation.getStudentId() != null) {
                studentIds.add(relation.getStudentId());
            }
        }
        return studentIds;
    }

    /**
     * 智能过滤学生ID参数
     * - 学生角色：只能看到自己的数据
     * - 导师角色：只能看到自己学生的数据
     * - 其他角色：可以看到所有数据
     * @param requestedStudentId 请求传入的studentId
     * @return 过滤结果（包含单个studentId和studentIds列表）
     */
    private FilterResult filterStudentIds(Long requestedStudentId) {
        // 首先检查是否是学生
        Long currentStudentId = getCurrentStudentId();
        if (currentStudentId != null) {
            // 学生角色，只能看到自己的数据
            return new FilterResult(currentStudentId, null);
        }

        // 然后检查是否是导师
        Long mentorTeacherId = getCurrentMentorTeacherId();
        if (mentorTeacherId != null) {
            // 导师角色，获取自己的学生列表
            List<Long> studentIds = getMentorStudentIds(mentorTeacherId);
            return new FilterResult(null, studentIds);
        }

        // 其他角色，使用请求传入的参数（可以为null表示查看所有）
        return new FilterResult(requestedStudentId, null);
    }

    // ==================== 学术活动管理 ====================

    /**
     * 学生提交学术活动申请
     * @param activity 学术活动实体对象，包含活动类型、时间、地点、内容等信息
     * @return 提交结果JSON
     */
    @PostMapping("/activity/submit")
    public String submitActivity(@RequestBody AcademicActivity activity) {
        try {
            // 学生提交时自动设置studentId
            Long currentStudentId = getCurrentStudentId();
            if (currentStudentId != null) {
                activity.setStudentId(currentStudentId);
            }
            boolean success = academicActivityService.submitActivity(activity);
            return success ? jsonReturn.returnSuccess("提交成功") : jsonReturn.returnFailed("提交失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取学术活动列表（支持分页和条件查询）
     * @param pageNum 页码（默认1）
     * @param pageSize 每页大小（默认10）
     * @param studentId 学生ID（可选，用于查询特定学生的活动）
     * @param activityType 活动类型（可选）
     * @param status 审批状态过滤器（可选）：1-待导师审批，2-待秘书审批，3-待院长审批
     * @return 分页活动列表
     */
    @GetMapping("/activity/list")
    public String getActivityList(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) Long studentId,
                                 @RequestParam(required = false) Integer activityType,
                                 @RequestParam(required = false) Integer status) {
        try {
            // 权限过滤：学生只能看到自己的数据，导师只能看到自己学生的数据
            FilterResult filterResult = filterStudentIds(studentId);
            List<AcademicActivityWithDetailsDTO> list = academicActivityMapper.listWithDetails(filterResult.studentId, filterResult.studentIds, activityType, status);

            // 手动分页
            int total = list.size();
            int fromIndex = Math.min((pageNum - 1) * pageSize, total);
            int toIndex = Math.min(fromIndex + pageSize, total);
            List<AcademicActivityWithDetailsDTO> pageList = list.subList(fromIndex, toIndex);

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

    @GetMapping("/activity/{id}")
    public String getActivityDetail(@PathVariable Long id) {
        try {
            AcademicActivityWithDetailsDTO activity = academicActivityMapper.getDetailWithDetails(id);
            return activity != null ? jsonReturn.returnSuccess(activity) : jsonReturn.returnFailed("未找到记录");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/activity/mentor/approve")
    public String activityMentorApprove(@RequestParam Long id,
                                        @RequestParam Integer status,
                                        @RequestParam(required = false) String comment) {
        try {
            boolean success = academicActivityService.mentorApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/activity/secretary/approve")
    public String activitySecretaryApprove(@RequestParam Long id,
                                         @RequestParam Integer status,
                                         @RequestParam(required = false) String comment) {
        try {
            boolean success = academicActivityService.secretaryApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/activity/dean/approve")
    public String activityDeanApprove(@RequestParam Long id,
                                     @RequestParam Integer status,
                                     @RequestParam(required = false) String comment) {
        try {
            boolean success = academicActivityService.deanApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // 统一的学术活动审批接口（根据用户角色自动调用对应的审批方法）
    @PostMapping("/activity/approve")
    public String approveActivity(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Integer status = Integer.valueOf(params.get("status").toString());
            String comment = params.get("comment") != null ? params.get("comment").toString() : null;

            boolean success;
            if (CurrentUserUtil.isMentor()) {
                // 导师审批
                success = academicActivityService.mentorApprove(id, status, comment);
            } else if (CurrentUserUtil.isSecretary()) {
                // 教学秘书审批
                success = academicActivityService.secretaryApprove(id, status, comment);
            } else if (CurrentUserUtil.isDean()) {
                // 分管院长审批
                success = academicActivityService.deanApprove(id, status, comment);
            } else {
                // 其他角色不允许审批
                return jsonReturn.returnFailed("无审批权限");
            }

            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // ==================== 创新创业项目管理 ====================

    @PostMapping("/innovation/submit")
    public String submitProject(@RequestBody InnovationProject project) {
        try {
            // 学生提交时自动设置studentId
            Long currentStudentId = getCurrentStudentId();
            if (currentStudentId != null) {
                project.setStudentId(currentStudentId);
            }
            boolean success = innovationProjectService.submitProject(project);
            return success ? jsonReturn.returnSuccess("提交成功") : jsonReturn.returnFailed("提交失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/innovation/list")
    public String getProjectList(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) Long studentId,
                                  @RequestParam(required = false) Integer projectType,
                                  @RequestParam(required = false) Integer status) {
        try {
            // 权限过滤：学生只能看到自己的数据，导师只能看到自己学生的数据
            FilterResult filterResult = filterStudentIds(studentId);
            List<InnovationProjectWithDetailsDTO> list = innovationProjectMapper.listWithDetails(filterResult.studentId, filterResult.studentIds, projectType, status);

            // 手动分页
            int total = list.size();
            int fromIndex = Math.min((pageNum - 1) * pageSize, total);
            int toIndex = Math.min(fromIndex + pageSize, total);
            List<InnovationProjectWithDetailsDTO> pageList = list.subList(fromIndex, toIndex);

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

    @GetMapping("/innovation/{id}")
    public String getProjectDetail(@PathVariable Long id) {
        try {
            InnovationProjectWithDetailsDTO project = innovationProjectMapper.getDetailWithDetails(id);
            return project != null ? jsonReturn.returnSuccess(project) : jsonReturn.returnFailed("未找到记录");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/innovation/mentor/approve")
    public String projectMentorApprove(@RequestParam Long id,
                                         @RequestParam Integer status,
                                         @RequestParam(required = false) String comment) {
        try {
            boolean success = innovationProjectService.mentorApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/innovation/secretary/approve")
    public String projectSecretaryApprove(@RequestParam Long id,
                                        @RequestParam Integer status,
                                        @RequestParam(required = false) String comment) {
        try {
            boolean success = innovationProjectService.secretaryApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/innovation/dean/approve")
    public String projectDeanApprove(@RequestParam Long id,
                                     @RequestParam Integer status,
                                     @RequestParam(required = false) String comment) {
        try {
            boolean success = innovationProjectService.deanApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // ==================== 学术成果管理 ====================

    @PostMapping("/achievement/submit")
    public String submitAchievement(@RequestBody AcademicAchievement achievement) {
        try {
            // 学生提交时自动设置studentId
            Long currentStudentId = getCurrentStudentId();
            if (currentStudentId != null) {
                achievement.setStudentId(currentStudentId);
            }
            boolean success = academicAchievementService.submitAchievement(achievement);
            return success ? jsonReturn.returnSuccess("提交成功") : jsonReturn.returnFailed("提交失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/achievement/list")
    public String getAchievementList(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) Long studentId,
                                      @RequestParam(required = false) Integer achievementType,
                                      @RequestParam(required = false) Integer status) {
        try {
            // 权限过滤：学生只能看到自己的数据，导师只能看到自己学生的数据
            FilterResult filterResult = filterStudentIds(studentId);
            List<AcademicAchievementWithDetailsDTO> list = academicAchievementMapper.listWithDetails(filterResult.studentId, filterResult.studentIds, achievementType, status);

            // 手动分页
            int total = list.size();
            int fromIndex = Math.min((pageNum - 1) * pageSize, total);
            int toIndex = Math.min(fromIndex + pageSize, total);
            List<AcademicAchievementWithDetailsDTO> pageList = list.subList(fromIndex, toIndex);

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

    @GetMapping("/achievement/{id}")
    public String getAchievementDetail(@PathVariable Long id) {
        try {
            AcademicAchievementWithDetailsDTO achievement = academicAchievementMapper.getDetailWithDetails(id);
            return achievement != null ? jsonReturn.returnSuccess(achievement) : jsonReturn.returnFailed("未找到记录");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/achievement/mentor/approve")
    public String achievementMentorApprove(@RequestParam Long id,
                                            @RequestParam Integer status,
                                            @RequestParam(required = false) String comment) {
        try {
            boolean success = academicAchievementService.mentorApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/achievement/secretary/approve")
    public String achievementSecretaryApprove(@RequestParam Long id,
                                             @RequestParam Integer status,
                                             @RequestParam(required = false) String comment) {
        try {
            boolean success = academicAchievementService.secretaryApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/achievement/dean/approve")
    public String achievementDeanApprove(@RequestParam Long id,
                                       @RequestParam Integer status,
            @RequestParam(required = false) String comment) {
        try {
            boolean success = academicAchievementService.deanApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // 统一的创新项目审批接口（根据用户角色自动调用对应的审批方法）
    @PostMapping("/innovation/approve")
    public String approveProject(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Integer status = Integer.valueOf(params.get("status").toString());
            String comment = params.get("comment") != null ? params.get("comment").toString() : null;

            boolean success;
            if (CurrentUserUtil.isMentor()) {
                success = innovationProjectService.mentorApprove(id, status, comment);
            } else if (CurrentUserUtil.isSecretary()) {
                success = innovationProjectService.secretaryApprove(id, status, comment);
            } else if (CurrentUserUtil.isDean()) {
                success = innovationProjectService.deanApprove(id, status, comment);
            } else {
                return jsonReturn.returnFailed("无审批权限");
            }

            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 统一的学术成果审批接口（根据用户角色自动调用对应的审批方法）
     * 自动识别当前登录用户角色：
     * - 导师角色调用导师审批方法
     * - 教学秘书角色调用秘书审批方法
     * - 分管院长角色调用院长审批方法
     * - 其他角色返回无权限错误
     * @param params 审批参数，包含：
     *               - id: 成果ID
     *               - status: 审批状态（1-同意，2-拒绝）
     *               - comment: 审批意见（可选）
     * @return 审批结果JSON
     */
    @PostMapping("/achievement/approve")
    public String approveAchievement(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Integer status = Integer.valueOf(params.get("status").toString());
            String comment = params.get("comment") != null ? params.get("comment").toString() : null;

            boolean success;
            if (CurrentUserUtil.isMentor()) {
                // 导师审批
                success = academicAchievementService.mentorApprove(id, status, comment);
            } else if (CurrentUserUtil.isSecretary()) {
                // 教学秘书审批
                success = academicAchievementService.secretaryApprove(id, status, comment);
            } else if (CurrentUserUtil.isDean()) {
                // 分管院长审批
                success = academicAchievementService.deanApprove(id, status, comment);
            } else {
                // 其他角色不允许审批
                return jsonReturn.returnFailed("无审批权限");
            }

            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/achievement/delete")
    public String deleteAchievement(@RequestParam Long id) {
        try {
            boolean success = academicAchievementService.removeById(id);
            return success ? jsonReturn.returnSuccess("删除成功") : jsonReturn.returnFailed("删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/innovation/delete")
    public String deleteProject(@RequestParam Long id) {
        try {
            boolean success = innovationProjectService.removeById(id);
            return success ? jsonReturn.returnSuccess("删除成功") : jsonReturn.returnFailed("删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/activity/delete")
    public String deleteActivity(@RequestParam Long id) {
        try {
            boolean success = academicActivityService.removeById(id);
            return success ? jsonReturn.returnSuccess("删除成功") : jsonReturn.returnFailed("删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // ==================== 统一审核管理 ====================

    /**
     * 获取统一审核列表
     * 汇总学术活动、创新项目、学术成果三类待审批内容
     * 根据当前用户角色返回对应待审批项
     */
    @GetMapping("/review/list")
    public String getReviewList(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) Integer type,
                                 @RequestParam(required = false) Integer status) {
        try {
            java.util.List<ReviewItemDTO> allItems = new java.util.ArrayList<>();

            // 权限过滤：学生只能看到自己的数据，导师只能看到自己学生的数据
            FilterResult filterResult = filterStudentIds(null);

            // 查询学术活动
            if (type == null || type == 1) {
                List<AcademicActivityWithDetailsDTO> activities = academicActivityMapper.listWithDetails(filterResult.studentId, filterResult.studentIds, null, null);
                for (AcademicActivityWithDetailsDTO activity : activities) {
                    ReviewItemDTO item = convertToReviewItem(activity, 1);
                    if (item != null && (status == null || item.getStatus() == status)) {
                        allItems.add(item);
                    }
                }
            }

            // 查询创新项目
            if (type == null || type == 2) {
                List<InnovationProjectWithDetailsDTO> projects = innovationProjectMapper.listWithDetails(filterResult.studentId, filterResult.studentIds, null, null);
                for (InnovationProjectWithDetailsDTO project : projects) {
                    ReviewItemDTO item = convertToReviewItem(project, 2);
                    if (item != null && (status == null || item.getStatus() == status)) {
                        allItems.add(item);
                    }
                }
            }

            // 查询学术成果
            if (type == null || type == 3) {
                List<AcademicAchievementWithDetailsDTO> achievements = academicAchievementMapper.listWithDetails(filterResult.studentId, filterResult.studentIds, null, null);
                for (AcademicAchievementWithDetailsDTO achievement : achievements) {
                    ReviewItemDTO item = convertToReviewItem(achievement, 3);
                    if (item != null && (status == null || item.getStatus() == status)) {
                        allItems.add(item);
                    }
                }
            }

            // 按提交时间排序
            allItems.sort((a, b) -> {
                if (a.getSubmitTime() == null) return 1;
                if (b.getSubmitTime() == null) return -1;
                return b.getSubmitTime().compareTo(a.getSubmitTime());
            });

            // 手动分页
            int total = allItems.size();
            int fromIndex = Math.min((pageNum - 1) * pageSize, total);
            int toIndex = Math.min(fromIndex + pageSize, total);
            List<ReviewItemDTO> pageList = allItems.subList(fromIndex, toIndex);

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
     * 统一审批接口
     */
    @PostMapping("/review/approve")
    public String approveReview(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Integer type = Integer.valueOf(params.get("type").toString());
            Integer status = Integer.valueOf(params.get("status").toString());
            String comment = params.get("comment") != null ? params.get("comment").toString() : null;

            boolean success = false;

            if (type == 1) {
                // 学术活动审批
                if (CurrentUserUtil.isMentor()) {
                    success = academicActivityService.mentorApprove(id, status, comment);
                } else if (CurrentUserUtil.isSecretary()) {
                    success = academicActivityService.secretaryApprove(id, status, comment);
                } else if (CurrentUserUtil.isDean()) {
                    success = academicActivityService.deanApprove(id, status, comment);
                }
            } else if (type == 2) {
                // 创新项目审批
                if (CurrentUserUtil.isMentor()) {
                    success = innovationProjectService.mentorApprove(id, status, comment);
                } else if (CurrentUserUtil.isSecretary()) {
                    success = innovationProjectService.secretaryApprove(id, status, comment);
                } else if (CurrentUserUtil.isDean()) {
                    success = innovationProjectService.deanApprove(id, status, comment);
                }
            } else if (type == 3) {
                // 学术成果审批
                if (CurrentUserUtil.isMentor()) {
                    success = academicAchievementService.mentorApprove(id, status, comment);
                } else if (CurrentUserUtil.isSecretary()) {
                    success = academicAchievementService.secretaryApprove(id, status, comment);
                } else if (CurrentUserUtil.isDean()) {
                    success = academicAchievementService.deanApprove(id, status, comment);
                }
            }

            if (success) {
                return jsonReturn.returnSuccess("审批成功");
            } else {
                return jsonReturn.returnFailed("审批失败或无权限");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // 转换学术活动为审核项
    private ReviewItemDTO convertToReviewItem(AcademicActivityWithDetailsDTO activity, Integer type) {
        ReviewItemDTO item = new ReviewItemDTO();
        item.setId(activity.getId());
        item.setType(type);
        item.setTitle(activity.getActivityName());
        item.setStudentId(activity.getStudentId());
        item.setStudentNo(activity.getStudentNo());
        item.setStudentName(activity.getStudentName());
        item.setSubmitTime(activity.getSubmitTime());
        item.setCreateTime(activity.getCreateTime());
        item.setDescription(activity.getContent());
        item.setMentorStatus(activity.getMentorStatus());
        item.setMentorComment(activity.getMentorComment());
        item.setSecretaryStatus(activity.getSecretaryStatus());
        item.setSecretaryComment(activity.getSecretaryComment());
        item.setDeanStatus(activity.getDeanStatus());
        item.setDeanComment(activity.getDeanComment());

        // 计算统一状态
        calculateUnifiedStatus(item);
        return item;
    }

    // 转换创新项目为审核项
    private ReviewItemDTO convertToReviewItem(InnovationProjectWithDetailsDTO project, Integer type) {
        ReviewItemDTO item = new ReviewItemDTO();
        item.setId(project.getId());
        item.setType(type);
        item.setTitle(project.getProjectName());
        item.setStudentId(project.getStudentId());
        item.setStudentNo(project.getStudentNo());
        item.setStudentName(project.getStudentName());
        item.setSubmitTime(project.getSubmitTime());
        item.setCreateTime(project.getCreateTime());
        item.setDescription(project.getDescription());
        item.setMentorStatus(project.getMentorStatus());
        item.setMentorComment(project.getMentorComment());
        item.setSecretaryStatus(project.getSecretaryStatus());
        item.setSecretaryComment(project.getSecretaryComment());
        item.setDeanStatus(project.getDeanStatus());
        item.setDeanComment(project.getDeanComment());

        // 计算统一状态
        calculateUnifiedStatus(item);
        return item;
    }

    // 转换学术成果为审核项
    private ReviewItemDTO convertToReviewItem(AcademicAchievementWithDetailsDTO achievement, Integer type) {
        ReviewItemDTO item = new ReviewItemDTO();
        item.setId(achievement.getId());
        item.setType(type);
        item.setTitle(achievement.getTitle());
        item.setStudentId(achievement.getStudentId());
        item.setStudentNo(achievement.getStudentNo());
        item.setStudentName(achievement.getStudentName());
        item.setSubmitTime(achievement.getSubmitTime());
        item.setCreateTime(achievement.getCreateTime());
        item.setDescription(achievement.getAbstractContent());
        item.setMentorStatus(achievement.getMentorStatus());
        item.setMentorComment(achievement.getMentorComment());
        item.setSecretaryStatus(achievement.getSecretaryStatus());
        item.setSecretaryComment(achievement.getSecretaryComment());
        item.setDeanStatus(achievement.getDeanStatus());
        item.setDeanComment(achievement.getDeanComment());

        // 计算统一状态
        calculateUnifiedStatus(item);
        return item;
    }

    // 计算统一状态（根据当前用户角色和审批流程）
    private void calculateUnifiedStatus(ReviewItemDTO item) {
        // 检查是否已拒绝（任意阶段拒绝即算拒绝）
        if (item.getMentorStatus() != null && item.getMentorStatus() == 2) {
            item.setStatus(2);
            item.setApprovalComment(item.getMentorComment());
            return;
        }
        if (item.getSecretaryStatus() != null && item.getSecretaryStatus() == 2) {
            item.setStatus(2);
            item.setApprovalComment(item.getSecretaryComment());
            return;
        }
        if (item.getDeanStatus() != null && item.getDeanStatus() == 2) {
            item.setStatus(2);
            item.setApprovalComment(item.getDeanComment());
            return;
        }

        // 检查是否全部通过
        boolean allApproved = (item.getMentorStatus() != null && item.getMentorStatus() == 1)
                && (item.getSecretaryStatus() != null && item.getSecretaryStatus() == 1)
                && (item.getDeanStatus() != null && item.getDeanStatus() == 1);
        if (allApproved) {
            item.setStatus(1);
            item.setApprovalComment("审批通过");
            return;
        }

        // 否则为待审批
        item.setStatus(0);
    }
}
