package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.StudentStatusChangeMapper;
import com.jameshao.gp22023237.po.MentorStudent;
import com.jameshao.gp22023237.po.Student;
import com.jameshao.gp22023237.po.StudentStatusChange;
import com.jameshao.gp22023237.service.MentorStudentService;
import com.jameshao.gp22023237.service.NoticeService;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.StudentStatusChangeService;
import com.jameshao.gp22023237.service.TeacherService;
import com.jameshao.gp22023237.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentStatusChangeServiceImpl extends ServiceImpl<StudentStatusChangeMapper, StudentStatusChange>
        implements StudentStatusChangeService {

    private static final Logger logger = LoggerFactory.getLogger(StudentStatusChangeServiceImpl.class);

    private static final String[] CHANGE_TYPES = {"", "休学", "复学", "退学", "延期毕业"};

    private String getChangeTypeText(Integer changeType) {
        if (changeType != null && changeType > 0 && changeType < CHANGE_TYPES.length) {
            return CHANGE_TYPES[changeType];
        }
        return "学籍异动";
    }

    @Autowired
    private StudentService studentService;

    @Autowired
    private MentorStudentService mentorStudentService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitApplication(StudentStatusChange application) {
        // 校验变更类型必填
        if (application.getChangeType() == null) {
            throw new RuntimeException("变更类型不能为空");
        }
        // 校验变更原因必填
        if (application.getReason() == null || application.getReason().trim().isEmpty()) {
            throw new RuntimeException("变更原因不能为空");
        }

        // 根据学号查询学生信息，获取学生ID
        if (application.getStudentId() == null && application.getStudentNo() != null) {
            LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Student::getStudentNo, application.getStudentNo());
            Student student = studentService.getOne(wrapper);
            if (student == null) {
                throw new RuntimeException("学号不存在，无法提交申请");
            }
            application.setStudentId(student.getId());
        }

        // 校验学生ID是否获取成功
        if (application.getStudentId() == null) {
            throw new RuntimeException("无法确定学生身份，请检查学号信息");
        }

        // 查询学生的已确认导师，自动填充 mentorId
        if (application.getMentorId() == null) {
            LambdaQueryWrapper<MentorStudent> mentorWrapper = new LambdaQueryWrapper<>();
            mentorWrapper.eq(MentorStudent::getStudentId, application.getStudentId())
                    .eq(MentorStudent::getStudentStatus, 1)
                    .eq(MentorStudent::getTeacherStatus, 1)
                    .last("LIMIT 1");
            MentorStudent mentorStudent = mentorStudentService.getOne(mentorWrapper);
            if (mentorStudent != null) {
                application.setMentorId(mentorStudent.getMentorId());
            }
        }

        // 检查是否有重复的待审批申请
        LambdaQueryWrapper<StudentStatusChange> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(StudentStatusChange::getStudentId, application.getStudentId())
                .eq(StudentStatusChange::getChangeType, application.getChangeType())
                .eq(StudentStatusChange::getStatus, 0);
        long count = count(checkWrapper);
        if (count > 0) {
            throw new RuntimeException("您已有相同类型的待审批申请，请勿重复提交");
        }

        // 设置申请时间
        application.setCreateTime(new Date());
        application.setUpdateTime(new Date());

        // 初始状态为未审批
        application.setMentorStatus(0);
        application.setSecretaryStatus(0);
        application.setStatus(0); // 整体状态为待审批

        boolean result = save(application);
        // 3.1 通知：异动申请提交 → 通知导师
        if (result && application.getMentorId() != null) {
            try {
                Student student = studentService.getById(application.getStudentId());
                String studentName = student != null ? student.getStudentName() : "学生";
                String changeTypeText = getChangeTypeText(application.getChangeType());
                Long mentorUserId = noticeService.getTeacherUserId(application.getMentorId());
                if (mentorUserId != null) {
                    noticeService.createAndPush("学籍异动申请通知",
                        "学生" + studentName + "申请" + changeTypeText, "1", mentorUserId);
                }
            } catch (Exception e) {
                logger.warn("异动申请通知推送失败: {}", e.getMessage());
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean mentorApprove(Long id, Integer status, String comment) {
        StudentStatusChange application = getById(id);
        if (application == null) {
            throw new RuntimeException("申请记录不存在");
        }
        // 防止重复审批
        if (application.getMentorStatus() != null && application.getMentorStatus() != 0) {
            throw new RuntimeException("该申请导师已审批过，无法重复操作");
        }
        // 校验审批状态值
        if (status == null || (status != 1 && status != 2)) {
            throw new RuntimeException("审批状态值无效，应为1(通过)或2(拒绝)");
        }

        application.setMentorStatus(status);
        application.setMentorApprovalTime(new Date());
        application.setUpdateTime(new Date());

        // 更新整体状态
        if (status == 2) {
            // 导师拒绝，整体状态为已拒绝
            application.setStatus(3);
        } else if (status == 1) {
            // 导师通过，整体状态为审批中
            application.setStatus(1);
        }

        boolean updated = updateById(application);

        // 3.2/3.3 通知：导师审批流转/驳回 → 通知秘书（通过时）或学生（驳回时）
        if (updated) {
            try {
                Long studentUserId = noticeService.getStudentUserId(application.getStudentId());
                if (status == 2 && studentUserId != null) {
                    // 3.3 导师驳回 → 通知学生
                    noticeService.createAndPush("异动审批通知",
                        "异动申请被导师驳回" + (comment != null ? "，原因：" + comment : ""),
                        "1", studentUserId);
                }
                // 3.2 导师通过 → 通知教学秘书
                if (status == 1) {
                    try {
                        List<com.jameshao.gp22023237.po.User> secretaries = userService.list(
                            new LambdaQueryWrapper<com.jameshao.gp22023237.po.User>()
                                .eq(com.jameshao.gp22023237.po.User::getRoleId, 5)
                                .eq(com.jameshao.gp22023237.po.User::getStatus, 1));
                        List<Long> secretaryUserIds = secretaries.stream()
                            .map(com.jameshao.gp22023237.po.User::getId).collect(Collectors.toList());
                        if (!secretaryUserIds.isEmpty()) {
                            String changeTypeText = getChangeTypeText(application.getChangeType());
                            Student studentForName = studentService.getById(application.getStudentId());
                            String studentName = studentForName != null ? studentForName.getStudentName() : "学生";
                            noticeService.createAndPushToUsers(secretaryUserIds, "异动审批通知",
                                "学生" + studentName
                                + "申请" + changeTypeText + "导师已通过，请审批", "1");
                        }
                    } catch (Exception ex) {
                        logger.warn("导师审批流转通知推送失败: {}", ex.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.warn("导师审批通知推送失败: {}", e.getMessage());
            }
        }

        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean secretaryApprove(Long id, Integer status, String comment) {
        StudentStatusChange application = getById(id);
        if (application == null) {
            throw new RuntimeException("申请记录不存在");
        }
        // 前置校验：导师必须已通过
        if (application.getMentorStatus() == null || application.getMentorStatus() != 1) {
            throw new RuntimeException("导师尚未通过审批，教学秘书无法审批");
        }
        // 防止重复审批
        if (application.getSecretaryStatus() != null && application.getSecretaryStatus() != 0) {
            throw new RuntimeException("该申请教学秘书已审批过，无法重复操作");
        }
        // 校验审批状态值
        if (status == null || (status != 1 && status != 2)) {
            throw new RuntimeException("审批状态值无效，应为1(通过)或2(拒绝)");
        }

        application.setSecretaryStatus(status);
        application.setSecretaryApprovalTime(new Date());
        application.setUpdateTime(new Date());

        // 更新整体状态（两级审批：导师→教学秘书，秘书通过即完成审批）
        if (status == 1) {
            // 教学秘书通过，整体状态为已通过
            application.setStatus(2);
            updateStudentStatus(application);
        } else if (status == 2) {
            // 教学秘书拒绝
            application.setStatus(3);
        }

        boolean result = updateById(application);
        // 3.3/3.4 通知：秘书审批驳回/完成 → 通知学生
        if (result) {
            try {
                Long studentUserId = noticeService.getStudentUserId(application.getStudentId());
                if (studentUserId != null) {
                    if (status == 1) {
                        // 3.4 审批完成
                        noticeService.createAndPush("异动审批通知",
                            "异动申请已审批通过，请及时办理手续", "1", studentUserId);
                    } else {
                        // 3.3 秘书驳回
                        noticeService.createAndPush("异动审批通知",
                            "异动申请被教学秘书驳回" + (comment != null ? "，原因：" + comment : ""),
                            "1", studentUserId);
                    }
                }
            } catch (Exception e) {
                logger.warn("秘书审批通知推送失败: {}", e.getMessage());
            }
        }
        return result;
    }

    private void updateStudentStatus(StudentStatusChange application) {
        Student student = studentService.getById(application.getStudentId());
        if (student != null) {
            // 根据异动类型更新学生状态
            // 1-休学 -> 状态2，2-复学 -> 状态1，3-退学 -> 状态4，4-延期毕业 -> 保持状态1
            Integer changeType = application.getChangeType();
            if (changeType != null) {
                switch (changeType) {
                    case 1: // 休学
                        student.setStatus(2);
                        break;
                    case 2: // 复学
                        student.setStatus(1);
                        break;
                    case 3: // 退学
                        student.setStatus(4);
                        break;
                    case 4: // 延期毕业
                        // 保持在读状态
                        break;
                }
            }
            student.setUpdateTime(new Date());
            studentService.updateById(student);
        }
    }
}
