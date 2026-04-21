package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.StudentStatusChangeMapper;
import com.jameshao.gp22023237.po.MentorStudent;
import com.jameshao.gp22023237.po.Student;
import com.jameshao.gp22023237.po.StudentStatusChange;
import com.jameshao.gp22023237.service.MentorStudentService;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.StudentStatusChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class StudentStatusChangeServiceImpl extends ServiceImpl<StudentStatusChangeMapper, StudentStatusChange>
        implements StudentStatusChangeService {

    @Autowired
    private StudentService studentService;

    @Autowired
    private MentorStudentService mentorStudentService;

    @Override
    @Transactional
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

        return save(application);
    }

    @Override
    @Transactional
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

        return updated;
    }

    @Override
    @Transactional
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

        return updateById(application);
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
