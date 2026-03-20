package com.jameshao.gp22023237.controller.academic;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.utils.CurrentUserUtil;
import java.util.Map;
import com.jameshao.gp22023237.po.AcademicAchievement;
import com.jameshao.gp22023237.po.AcademicActivity;
import com.jameshao.gp22023237.po.InnovationProject;
import com.jameshao.gp22023237.service.AcademicActivityService;
import com.jameshao.gp22023237.service.AcademicAchievementService;
import com.jameshao.gp22023237.service.InnovationProjectService;
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
    private InnovationProjectService innovationProjectService;

    @Autowired
    private AcademicAchievementService academicAchievementService;

    // ==================== 学术活动管理 ====================

    /**
     * 学生提交学术活动申请
     * @param activity 学术活动实体对象，包含活动类型、时间、地点、内容等信息
     * @return 提交结果JSON
     */
    @PostMapping("/activity/submit")
    public String submitActivity(@RequestBody AcademicActivity activity) {
        try {
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
            Page<AcademicActivity> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<AcademicActivity> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(AcademicActivity::getSubmitTime);

            if (studentId != null) {
                wrapper.eq(AcademicActivity::getStudentId, studentId);
            }
            if (activityType != null) {
                wrapper.eq(AcademicActivity::getActivityType, activityType);
            }
            // status参数控制返回哪个审批状态的数据：1-导师，2-秘书，3-院长
            if (status != null) {
                if (status == 1) {
                    wrapper.eq(AcademicActivity::getMentorStatus, 0);
                } else if (status == 2) {
                    wrapper.eq(AcademicActivity::getSecretaryStatus, 0);
                } else if (status == 3) {
                    wrapper.eq(AcademicActivity::getDeanStatus, 0);
                }
            }

            IPage<AcademicActivity> result = academicActivityService.page(page, wrapper);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/activity/{id}")
    public String getActivityDetail(@PathVariable Long id) {
        try {
            AcademicActivity activity = academicActivityService.getById(id);
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
            Page<InnovationProject> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<InnovationProject> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(InnovationProject::getSubmitTime);

            if (studentId != null) {
                wrapper.eq(InnovationProject::getStudentId, studentId);
            }
            if (projectType != null) {
                wrapper.eq(InnovationProject::getProjectType, projectType);
            }
            if (status != null) {
                if (status == 1) {
                    wrapper.eq(InnovationProject::getMentorStatus, 0);
                } else if (status == 2) {
                    wrapper.eq(InnovationProject::getSecretaryStatus, 0);
                } else if (status == 3) {
                    wrapper.eq(InnovationProject::getDeanStatus, 0);
                }
            }

            IPage<InnovationProject> result = innovationProjectService.page(page, wrapper);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/innovation/{id}")
    public String getProjectDetail(@PathVariable Long id) {
        try {
            InnovationProject project = innovationProjectService.getById(id);
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
            Page<AcademicAchievement> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<AcademicAchievement> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(AcademicAchievement::getSubmitTime);

            if (studentId != null) {
                wrapper.eq(AcademicAchievement::getStudentId, studentId);
            }
            if (achievementType != null) {
                wrapper.eq(AcademicAchievement::getAchievementType, achievementType);
            }
            if (status != null) {
                if (status == 1) {
                    wrapper.eq(AcademicAchievement::getMentorStatus, 0);
                } else if (status == 2) {
                    wrapper.eq(AcademicAchievement::getSecretaryStatus, 0);
                } else if (status == 3) {
                    wrapper.eq(AcademicAchievement::getDeanStatus, 0);
                }
            }

            IPage<AcademicAchievement> result = academicAchievementService.page(page, wrapper);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/achievement/{id}")
    public String getAchievementDetail(@PathVariable Long id) {
        try {
            AcademicAchievement achievement = academicAchievementService.getById(id);
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
}
