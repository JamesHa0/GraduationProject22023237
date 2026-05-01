package com.jameshao.gp22023237.controller.academic;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jameshao.gp22023237.DTO.ApprovalRecordDTO;
import com.jameshao.gp22023237.DTO.SubmissionWithDetailsDTO;
import com.jameshao.gp22023237.annotation.Log;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.common.enums.ApprovalAction;
import com.jameshao.gp22023237.common.enums.BusinessType;
import com.jameshao.gp22023237.common.enums.ContentType;

import com.jameshao.gp22023237.common.enums.SubmitterType;
import com.jameshao.gp22023237.po.AcademicSubmission;
import com.jameshao.gp22023237.po.MentorStudent;
import com.jameshao.gp22023237.po.Student;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.service.AcademicSubmissionService;
import com.jameshao.gp22023237.service.MentorStudentService;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.TeacherService;
import com.jameshao.gp22023237.utils.CurrentUserUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 学术管理控制器（重构版）
 * 基于 academic_submission 主表 + 详情子表 + 审批记录子表
 * 保留旧API路径兼容前端，内部切换到新表结构
 * 路径前缀: /academic
 */
@RestController
@RequestMapping("/academic")
public class AcademicManagementController {

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private AcademicSubmissionService submissionService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private MentorStudentService mentorStudentService;

    // ==================== 权限辅助方法 ====================

    private Long getCurrentStudentId() {
        Integer roleId = CurrentUserUtil.getCurrentRoleId();
        if (roleId != null && roleId == 6) {
            Long userId = CurrentUserUtil.getCurrentUserId();
            if (userId != null) {
                LambdaQueryWrapper<Student> qw = new LambdaQueryWrapper<>();
                qw.eq(Student::getUserId, userId);
                Student student = studentService.getOne(qw);
                return student != null ? student.getId() : null;
            }
        }
        return null;
    }

    private Long getCurrentMentorTeacherId() {
        Integer roleId = CurrentUserUtil.getCurrentRoleId();
        if (roleId != null && (roleId == 7 || roleId == 8)) {
            Long userId = CurrentUserUtil.getCurrentUserId();
            if (userId != null) {
                LambdaQueryWrapper<Teacher> qw = new LambdaQueryWrapper<>();
                qw.eq(Teacher::getUserId, userId);
                Teacher teacher = teacherService.getOne(qw);
                return teacher != null ? teacher.getId() : null;
            }
        }
        return null;
    }

    private List<Long> getMentorStudentIds(Long mentorId) {
        List<Long> studentIds = new ArrayList<>();
        if (mentorId == null) return studentIds;
        LambdaQueryWrapper<MentorStudent> qw = new LambdaQueryWrapper<>();
        qw.eq(MentorStudent::getMentorId, mentorId)
          .eq(MentorStudent::getTeacherStatus, 1)
          .eq(MentorStudent::getStudentStatus, 1);
        List<MentorStudent> relations = mentorStudentService.list(qw);
        for (MentorStudent r : relations) {
            if (r.getStudentId() != null) studentIds.add(r.getStudentId());
        }
        return studentIds;
    }

    /**
     * 判断当前用户是否为该提交记录的提交人
     */
    private boolean isOwnerOfSubmission(SubmissionWithDetailsDTO detail) {
        Long currentStudentId = getCurrentStudentId();
        if (currentStudentId != null) {
            // 学生：检查studentId匹配
            return currentStudentId.equals(detail.getStudentId());
        }
        Long mentorTeacherId = getCurrentMentorTeacherId();
        if (mentorTeacherId != null) {
            // 导师：检查submitterId+submitterType匹配
            return mentorTeacherId.equals(detail.getSubmitterId())
                    && detail.getSubmitterType() != null
                    && detail.getSubmitterType() == SubmitterType.MENTOR.getCode();
        }
        Long userId = CurrentUserUtil.getCurrentUserId();
        if (userId != null) {
            // 管理员等其他角色：检查submitterId+submitterType匹配
            return userId.equals(detail.getSubmitterId())
                    && detail.getSubmitterType() != null
                    && detail.getSubmitterType() == SubmitterType.ADMIN.getCode();
        }
        return false;
    }

    /**
     * 判断当前用户是否为该提交记录的审批人
     */
    private boolean isApproverOfSubmission(SubmissionWithDetailsDTO detail) {
        Long[] approverInfo = getCurrentApproverInfo();
        if (approverInfo == null) return false;

        // 管理员/秘书/院长可以查看所有待审批记录
        if (CurrentUserUtil.isRoundAdmin() || CurrentUserUtil.isDean() || CurrentUserUtil.isSecretary()) {
            return true;
        }

        // 导师：只能查看自己学生的提交
        if (approverInfo[1] == 2L) {
            Long mentorId = getCurrentMentorTeacherId();
            if (mentorId != null) {
                List<Long> studentIds = getMentorStudentIds(mentorId);
                return studentIds.contains(detail.getStudentId());
            }
        }
        return false;
    }

    /**
     * 获取当前审批人ID和类型
     * @return [approverId, approverType] 或 null
     */
    private Long[] getCurrentApproverInfo() {
        Long approverId = null;
        Integer approverType = null;

        Long mentorTeacherId = getCurrentMentorTeacherId();
        if (mentorTeacherId != null) {
            // 导师(roleId=7)或任课教师(roleId=8)均可作为导师审批人
            approverId = mentorTeacherId;
            approverType = 2; // 导师
        } else if (CurrentUserUtil.isSecretary()) {
            approverId = CurrentUserUtil.getCurrentUserId();
            approverType = 5; // 教学秘书
        } else if (CurrentUserUtil.isDean()) {
            approverId = CurrentUserUtil.getCurrentUserId();
            approverType = 1; // 分管院长
        }

        return (approverId != null && approverType != null) ? new Long[]{approverId, Long.valueOf(approverType)} : null;
    }

    // ==================== 统一提交接口 ====================

    /**
     * 统一提交接口（支持学术活动、学术成果、创新创业）
     */
    @PostMapping("/submit")
    public String submitContent(@RequestBody Map<String, Object> params) {
        try {
            AcademicSubmission submission = new AcademicSubmission();
            submission.setContentType(getIntParam(params, "contentType"));
            submission.setTitle(getStringParam(params, "title"));
            submission.setAbstractContent(getStringParam(params, "abstractContent"));

            // 仅允许学生提交
            Long currentStudentId = getCurrentStudentId();
            if (currentStudentId == null) {
                return jsonReturn.returnFailed("仅学生可提交学术内容");
            }
            submission.setStudentId(currentStudentId);
            submission.setSubmitterId(currentStudentId);
            submission.setSubmitterType(SubmitterType.STUDENT.getCode());

            // 附件：前端传 fileUrls 为 JSON 字符串或逗号分隔
            Object fileUrlsObj = params.get("fileUrls");
            if (fileUrlsObj != null) {
                submission.setFileUrls(fileUrlsObj.toString());
            } else {
                // 兼容旧字段 attachmentPath
                String attachmentPath = getStringParam(params, "attachmentPath");
                if (attachmentPath != null && !attachmentPath.isEmpty()) {
                    submission.setFileUrls("[\"" + attachmentPath + "\"]");
                }
            }

            // 设置提交时间
            submission.setSubmitTime(new Date());

            // 子表数据直接传整个 detail 对象
            @SuppressWarnings("unchecked")
            Map<String, Object> detailData = (Map<String, Object>) params.get("detail");
            if (detailData == null) {
                detailData = new HashMap<>();
                // 兼容旧格式：字段直接平铺在 params 中
                copyDetailFields(params, detailData, submission.getContentType());
            }

            boolean success = submissionService.submitContent(submission, detailData);
            return success ? jsonReturn.returnSuccess("提交成功") : jsonReturn.returnFailed("提交失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // ==================== 兼容旧API路径的提交接口 ====================

    @PostMapping("/activity/submit")
    public String submitActivity(@RequestBody Map<String, Object> params) {
        params.put("contentType", ContentType.ACTIVITY.getCode());
        if (!params.containsKey("title")) params.put("title", params.get("activityName"));
        return submitContent(params);
    }

    @PostMapping("/achievement/submit")
    public String submitAchievement(@RequestBody Map<String, Object> params) {
        params.put("contentType", ContentType.ACHIEVEMENT.getCode());
        return submitContent(params);
    }

    @PostMapping("/innovation/submit")
    public String submitProject(@RequestBody Map<String, Object> params) {
        params.put("contentType", ContentType.INNOVATION.getCode());
        if (!params.containsKey("title")) params.put("title", params.get("projectName"));
        return submitContent(params);
    }

    // ==================== 统一列表接口 ====================

    /**
     * 统一列表接口
     */
    @GetMapping("/list")
    public String getSubmissionList(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                     @RequestParam(required = false) Long studentId,
                                     @RequestParam(required = false) Integer contentType,
                                     @RequestParam(required = false) Integer approvalStatus,
                                     @RequestParam(required = false) Integer subType) {
        try {
            // 根据当前用户角色确定过滤方式
            Long filterStudentId = null;
            List<Long> filterStudentIds = null;
            Long filterSubmitterId = null;
            Integer filterSubmitterType = null;

            Long currentStudentId = getCurrentStudentId();
            if (currentStudentId != null) {
                // 学生：按student_id过滤
                filterStudentId = currentStudentId;
            } else {
                Long mentorTeacherId = getCurrentMentorTeacherId();
                if (mentorTeacherId != null) {
                    // 导师：按submitter_id+type过滤
                    filterSubmitterId = mentorTeacherId;
                    filterSubmitterType = SubmitterType.MENTOR.getCode();
                } else {
                    Long userId = CurrentUserUtil.getCurrentUserId();
                    if (userId != null) {
                        // 管理员等其他角色：按submitter_id+type过滤
                        filterSubmitterId = userId;
                        filterSubmitterType = SubmitterType.ADMIN.getCode();
                    } else {
                        // 无法识别身份：返回空
                        filterStudentIds = new ArrayList<>();
                    }
                }
            }

            List<SubmissionWithDetailsDTO> list = submissionService.listWithDetails(
                    filterStudentId, filterStudentIds, contentType, approvalStatus, subType,
                    filterSubmitterId, filterSubmitterType);

            // 手动分页
            int total = list.size();
            int fromIndex = Math.min((pageNum - 1) * pageSize, total);
            int toIndex = Math.min(fromIndex + pageSize, total);
            List<SubmissionWithDetailsDTO> pageList = list.subList(fromIndex, toIndex);

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

    // 兼容旧API路径
    @GetMapping("/activity/list")
    public String getActivityList(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) Long studentId,
                                   @RequestParam(required = false) Integer activityType,
                                   @RequestParam(required = false) Integer status) {
        // 旧 status 映射: 1=待导师审批, 2=待秘书审批, 3=待院长审批
        Integer approvalStatus = mapOldStatusToNew(status);
        return getSubmissionList(pageNum, pageSize, studentId, ContentType.ACTIVITY.getCode(), approvalStatus, activityType);
    }

    @GetMapping("/achievement/list")
    public String getAchievementList(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) Long studentId,
                                      @RequestParam(required = false) Integer achievementType,
                                      @RequestParam(required = false) Integer status) {
        Integer approvalStatus = mapOldStatusToNew(status);
        return getSubmissionList(pageNum, pageSize, studentId, ContentType.ACHIEVEMENT.getCode(), approvalStatus, achievementType);
    }

    @GetMapping("/innovation/list")
    public String getProjectList(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) Long studentId,
                                  @RequestParam(required = false) Integer projectType,
                                  @RequestParam(required = false) Integer status) {
        Integer approvalStatus = mapOldStatusToNew(status);
        return getSubmissionList(pageNum, pageSize, studentId, ContentType.INNOVATION.getCode(), approvalStatus, projectType);
    }

    // ==================== 统一详情接口 ====================

    @GetMapping("/detail/{id}")
    public String getDetail(@PathVariable Long id) {
        try {
            SubmissionWithDetailsDTO detail = submissionService.getFullDetail(id);
            if (detail == null) return jsonReturn.returnFailed("未找到记录");

            // 权限校验：提交人本人 或 审批人可查看
            if (isOwnerOfSubmission(detail) || isApproverOfSubmission(detail)) {
                return jsonReturn.returnSuccess(detail);
            }
            return jsonReturn.returnFailed("无权查看该记录");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/activity/{id}")
    public String getActivityDetail(@PathVariable Long id) {
        return getDetail(id);
    }

    @GetMapping("/achievement/{id}")
    public String getAchievementDetail(@PathVariable Long id) {
        return getDetail(id);
    }

    @GetMapping("/innovation/{id}")
    public String getProjectDetail(@PathVariable Long id) {
        return getDetail(id);
    }

    // ==================== 统一审批接口 ====================

    /**
     * 统一审批接口（新）
     * @param params 包含: id(提交记录ID), action(2=通过,3=驳回), comment(审批意见), reviewerFileUrls(审批人附件URL)
     */
    @PostMapping("/approve")
    public String approve(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Integer action = Integer.valueOf(params.get("action").toString());
            String comment = params.get("comment") != null ? params.get("comment").toString() : null;
            String reviewerFileUrls = params.get("reviewerFileUrls") != null ? params.get("reviewerFileUrls").toString() : null;

            // 获取当前审批人信息
            Long[] approverInfo = getCurrentApproverInfo();
            if (approverInfo == null) {
                return jsonReturn.returnFailed("无审批权限");
            }

            // 校验审批人是否有权审批该提交
            SubmissionWithDetailsDTO detail = submissionService.getFullDetail(id);
            if (detail == null) {
                return jsonReturn.returnFailed("未找到记录");
            }
            if (!isApproverOfSubmission(detail)) {
                return jsonReturn.returnFailed("无权审批该记录");
            }

            boolean success = submissionService.approve(id, approverInfo[0], approverInfo[1].intValue(), action, comment, reviewerFileUrls);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 兼容旧审批接口（前端传 status: 1=通过, 2=驳回）
     */
    @PostMapping("/activity/approve")
    public String approveActivity(@RequestBody Map<String, Object> params) {
        return approveCompat(params);
    }

    @PostMapping("/achievement/approve")
    public String approveAchievement(@RequestBody Map<String, Object> params) {
        return approveCompat(params);
    }

    @PostMapping("/innovation/approve")
    public String approveProject(@RequestBody Map<String, Object> params) {
        return approveCompat(params);
    }

    // 旧的三级审批接口兼容（增加角色校验）
    @PostMapping("/activity/mentor/approve")
    public String activityMentorApprove(@RequestParam Long id, @RequestParam Integer status,
                                         @RequestParam(required = false) String comment) throws JsonProcessingException {
        return mentorApproveCompat(id, status, comment);
    }

    @PostMapping("/activity/secretary/approve")
    public String activitySecretaryApprove(@RequestParam Long id, @RequestParam Integer status,
                                            @RequestParam(required = false) String comment) throws JsonProcessingException {
        return secretaryApproveCompat(id, status, comment);
    }

    @PostMapping("/activity/dean/approve")
    public String activityDeanApprove(@RequestParam Long id, @RequestParam Integer status,
                                       @RequestParam(required = false) String comment) throws JsonProcessingException {
        return deanApproveCompat(id, status, comment);
    }

    @PostMapping("/achievement/mentor/approve")
    public String achievementMentorApprove(@RequestParam Long id, @RequestParam Integer status,
                                            @RequestParam(required = false) String comment) throws JsonProcessingException {
        return mentorApproveCompat(id, status, comment);
    }

    @PostMapping("/achievement/secretary/approve")
    public String achievementSecretaryApprove(@RequestParam Long id, @RequestParam Integer status,
                                               @RequestParam(required = false) String comment) throws JsonProcessingException {
        return secretaryApproveCompat(id, status, comment);
    }

    @PostMapping("/achievement/dean/approve")
    public String achievementDeanApprove(@RequestParam Long id, @RequestParam Integer status,
                                          @RequestParam(required = false) String comment) throws JsonProcessingException {
        return deanApproveCompat(id, status, comment);
    }

    @PostMapping("/innovation/mentor/approve")
    public String projectMentorApprove(@RequestParam Long id, @RequestParam Integer status,
                                        @RequestParam(required = false) String comment) throws JsonProcessingException {
        return mentorApproveCompat(id, status, comment);
    }

    @PostMapping("/innovation/secretary/approve")
    public String projectSecretaryApprove(@RequestParam Long id, @RequestParam Integer status,
                                           @RequestParam(required = false) String comment) throws JsonProcessingException {
        return secretaryApproveCompat(id, status, comment);
    }

    @PostMapping("/innovation/dean/approve")
    public String projectDeanApprove(@RequestParam Long id, @RequestParam Integer status,
                                      @RequestParam(required = false) String comment) throws JsonProcessingException {
        return deanApproveCompat(id, status, comment);
    }

    // ==================== 删除接口 ====================

    @Log(title = "学术管理", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public String deleteContent(@RequestParam Long id) {
        try {
            // 获取记录检查状态和归属
            com.jameshao.gp22023237.DTO.SubmissionWithDetailsDTO detail = submissionService.getFullDetail(id);
            if (detail == null) return jsonReturn.returnFailed("未找到记录");

            // 权限校验：仅允许提交人本人删除
            if (!isOwnerOfSubmission(detail)) {
                return jsonReturn.returnFailed("无权删除该记录");
            }

            // 只有草稿(0)和已驳回(5)状态可删除
            if (detail.getApprovalStatus() != null
                    && detail.getApprovalStatus() != 0
                    && detail.getApprovalStatus() != 5) {
                return jsonReturn.returnFailed("当前状态不允许删除，仅草稿和已驳回状态可删除");
            }

            boolean success = submissionService.softDelete(id);
            return success ? jsonReturn.returnSuccess("删除成功") : jsonReturn.returnFailed("删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/activity/delete")
    public String deleteActivity(@RequestParam Long id) {
        return deleteContent(id);
    }

    @PostMapping("/achievement/delete")
    public String deleteAchievement(@RequestParam Long id) {
        return deleteContent(id);
    }

    @PostMapping("/innovation/delete")
    public String deleteProject(@RequestParam Long id) {
        return deleteContent(id);
    }

    // ==================== 审核管理 ====================

    @GetMapping("/review/list")
    public String getReviewList(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) Integer type,
                                 @RequestParam(required = false) Integer status) {
        try {
            Long currentStudentId = getCurrentStudentId();
            Long mentorId = getCurrentMentorTeacherId();

            Long filterStudentId = null;
            List<Long> filterStudentIds = null;
            boolean isSuperAdmin = CurrentUserUtil.isRoundAdmin() || CurrentUserUtil.isDean() || CurrentUserUtil.isSecretary();

            if (currentStudentId != null) {
                // 学生：只能看到自己提交的
                filterStudentId = currentStudentId;
            } else if (mentorId != null) {
                // 导师：只能看到自己学生的提交
                filterStudentIds = getMentorStudentIds(mentorId);
                if (filterStudentIds.isEmpty()) {
                    // 导师没有关联学生，直接返回空列表
                    Map<String, Object> emptyResult = new HashMap<>();
                    emptyResult.put("records", new ArrayList<>());
                    emptyResult.put("total", 0);
                    emptyResult.put("size", pageSize);
                    emptyResult.put("current", pageNum);
                    return jsonReturn.returnSuccess(emptyResult);
                }
            } else if (!isSuperAdmin) {
                // 非管理员/秘书/院长，且非学生/导师，返回空列表
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("records", new ArrayList<>());
                emptyResult.put("total", 0);
                emptyResult.put("size", pageSize);
                emptyResult.put("current", pageNum);
                return jsonReturn.returnSuccess(emptyResult);
            }
            // 管理员/秘书/院长可以看到所有（filterStudentId 和 filterStudentIds 都为 null）

            // 根据 status 参数筛选
            Integer filterApprovalStatus = status;

            List<SubmissionWithDetailsDTO> allItems = new ArrayList<>();

            // type参数对齐ContentType编码：1=ACTIVITY, 2=ACHIEVEMENT, 3=INNOVATION
            if (type == null || type == ContentType.ACTIVITY.getCode()) {
                allItems.addAll(submissionService.listWithDetails(
                        filterStudentId, filterStudentIds, ContentType.ACTIVITY.getCode(), filterApprovalStatus, null, null, null));
            }
            if (type == null || type == ContentType.ACHIEVEMENT.getCode()) {
                allItems.addAll(submissionService.listWithDetails(
                        filterStudentId, filterStudentIds, ContentType.ACHIEVEMENT.getCode(), filterApprovalStatus, null, null, null));
            }
            if (type == null || type == ContentType.INNOVATION.getCode()) {
                allItems.addAll(submissionService.listWithDetails(
                        filterStudentId, filterStudentIds, ContentType.INNOVATION.getCode(), filterApprovalStatus, null, null, null));
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
            List<SubmissionWithDetailsDTO> pageList = allItems.subList(fromIndex, toIndex);

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

    @PostMapping("/review/approve")
    public String approveReview(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Integer oldStatus = params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : null;
            String comment = params.get("comment") != null ? params.get("comment").toString() : null;
            String reviewerFileUrls = params.get("reviewerFileUrls") != null ? params.get("reviewerFileUrls").toString() : null;

            // 旧 status: 1=通过, 2=驳回 → 新 action: 2=通过, 3=驳回
            Integer action = (oldStatus != null && oldStatus == 2) ? ApprovalAction.REJECT.getCode() : ApprovalAction.APPROVE.getCode();

            Long[] approverInfo = getCurrentApproverInfo();
            if (approverInfo == null) {
                return jsonReturn.returnFailed("无审批权限");
            }

            // 校验审批人是否有权审批该提交
            SubmissionWithDetailsDTO detail = submissionService.getFullDetail(id);
            if (detail == null) {
                return jsonReturn.returnFailed("未找到记录");
            }
            if (!isApproverOfSubmission(detail)) {
                return jsonReturn.returnFailed("无权审批该记录");
            }

            boolean success = submissionService.approve(id, approverInfo[0], approverInfo[1].intValue(), action, comment, reviewerFileUrls);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取审批历史记录
     */
    @GetMapping("/approval/records/{submissionId}")
    public String getApprovalRecords(@PathVariable Long submissionId) {
        try {
            SubmissionWithDetailsDTO detail = submissionService.getFullDetail(submissionId);
            if (detail == null) return jsonReturn.returnFailed("未找到记录");

            // 权限校验：提交人本人 或 审批人可查看审批记录
            if (!isOwnerOfSubmission(detail) && !isApproverOfSubmission(detail)) {
                return jsonReturn.returnFailed("无权查看该审批记录");
            }

            List<ApprovalRecordDTO> records = detail.getApprovalRecords() != null
                    ? detail.getApprovalRecords()
                    : new ArrayList<>();
            return jsonReturn.returnSuccess(records);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    private String approveCompat(Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Integer oldStatus = Integer.valueOf(params.get("status").toString());
            String comment = params.get("comment") != null ? params.get("comment").toString() : null;

            Integer action = (oldStatus == 2) ? ApprovalAction.REJECT.getCode() : ApprovalAction.APPROVE.getCode();

            Long[] approverInfo = getCurrentApproverInfo();
            if (approverInfo == null) {
                return jsonReturn.returnFailed("无审批权限");
            }

            // 校验审批人是否有权审批该提交
            SubmissionWithDetailsDTO detail = submissionService.getFullDetail(id);
            if (detail == null) {
                return jsonReturn.returnFailed("未找到记录");
            }
            if (!isApproverOfSubmission(detail)) {
                return jsonReturn.returnFailed("无权审批该记录");
            }

            boolean success = submissionService.approve(id, approverInfo[0], approverInfo[1].intValue(), action, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    private String approveCompat(Long id, Integer status, String comment) {
        try {
            Integer action = (status == 2) ? ApprovalAction.REJECT.getCode() : ApprovalAction.APPROVE.getCode();
            Long[] approverInfo = getCurrentApproverInfo();
            if (approverInfo == null) {
                return jsonReturn.returnFailed("无审批权限");
            }

            // 校验审批人是否有权审批该提交
            SubmissionWithDetailsDTO detail = submissionService.getFullDetail(id);
            if (detail == null) {
                return jsonReturn.returnFailed("未找到记录");
            }
            if (!isApproverOfSubmission(detail)) {
                return jsonReturn.returnFailed("无权审批该记录");
            }

            boolean success = submissionService.approve(id, approverInfo[0], approverInfo[1].intValue(), action, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 旧版三级审批接口 - 导师审批（仅允许导师角色调用）
     */
    private String mentorApproveCompat(Long id, Integer status, String comment) throws JsonProcessingException {
        if (!CurrentUserUtil.isMentor()) {
            return jsonReturn.returnFailed("仅导师可执行此操作");
        }
        return approveCompat(id, status, comment);
    }

    /**
     * 旧版三级审批接口 - 秘书审批（仅允许教学秘书/综合管理员角色调用）
     */
    private String secretaryApproveCompat(Long id, Integer status, String comment) throws JsonProcessingException {
        if (!CurrentUserUtil.isSecretary() && !(CurrentUserUtil.getCurrentRoleId() != null && CurrentUserUtil.getCurrentRoleId() == 4)) {
            return jsonReturn.returnFailed("仅教学秘书或综合管理员可执行此操作");
        }
        return approveCompat(id, status, comment);
    }

    /**
     * 旧版三级审批接口 - 院长审批（仅允许院长/超级管理员角色调用）
     */
    private String deanApproveCompat(Long id, Integer status, String comment) throws JsonProcessingException {
        if (!CurrentUserUtil.isDean() && !(CurrentUserUtil.getCurrentRoleId() != null && CurrentUserUtil.getCurrentRoleId() == 1)) {
            return jsonReturn.returnFailed("仅院长或超级管理员可执行此操作");
        }
        return approveCompat(id, status, comment);
    }

    /**
     * 旧 status 映射到新 approvalStatus
     * 旧: 1=待导师审批, 2=待秘书审批, 3=待院长审批
     * 新: 1=待导师审批, 2=待秘书审批, 3=待院长审批
     */
    private Integer mapOldStatusToNew(Integer oldStatus) {
        return oldStatus; // 编码恰好一致
    }

    private String getStringParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value != null ? value.toString() : null;
    }

    private Integer getIntParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(value.toString()); }
        catch (NumberFormatException e) { return null; }
    }

    /**
     * 兼容旧格式：将平铺在 params 中的子表字段复制到 detailData
     */
    private void copyDetailFields(Map<String, Object> src, Map<String, Object> dest, Integer contentType) {
        if (contentType == null) return;

        ContentType type = ContentType.fromCode(contentType);
        if (type == null) return;

        switch (type) {
            case ACTIVITY:
                copyIfPresent(src, dest, "activityType");
                copyIfPresent(src, dest, "activityName");
                copyIfPresent(src, dest, "activityTime");
                copyIfPresent(src, dest, "location");
                copyIfPresent(src, dest, "speaker");
                copyIfPresent(src, dest, "content");
                break;
            case ACHIEVEMENT:
                copyIfPresent(src, dest, "achievementType");
                copyIfPresent(src, dest, "authors");
                copyIfPresent(src, dest, "publicationDate");
                copyIfPresent(src, dest, "journalName");
                copyIfPresent(src, dest, "journalLevel");
                copyIfPresent(src, dest, "volume");
                copyIfPresent(src, dest, "issue");
                copyIfPresent(src, dest, "pages");
                copyIfPresent(src, dest, "doi");
                copyIfPresent(src, dest, "patentNo");
                copyIfPresent(src, dest, "patentType");
                copyIfPresent(src, dest, "patentStatus");
                copyIfPresent(src, dest, "awardName");
                copyIfPresent(src, dest, "awardLevel");
                copyIfPresent(src, dest, "awardIssuer");
                copyIfPresent(src, dest, "projectName");
                copyIfPresent(src, dest, "projectRole");
                break;
            case INNOVATION:
                copyIfPresent(src, dest, "projectType");
                copyIfPresent(src, dest, "projectName");
                copyIfPresent(src, dest, "projectLevel");
                copyIfPresent(src, dest, "projectNo");
                copyIfPresent(src, dest, "leader");
                copyIfPresent(src, dest, "members");
                copyIfPresent(src, dest, "advisor");
                copyIfPresent(src, dest, "startDate");
                copyIfPresent(src, dest, "endDate");
                copyIfPresent(src, dest, "description");
                copyIfPresent(src, dest, "achievements");
                copyIfPresent(src, dest, "awardLevel");
                copyIfPresent(src, dest, "fundingAmount");
                break;
        }
    }

    private void copyIfPresent(Map<String, Object> src, Map<String, Object> dest, String key) {
        if (src.containsKey(key)) {
            dest.put(key, src.get(key));
        }
    }
}
