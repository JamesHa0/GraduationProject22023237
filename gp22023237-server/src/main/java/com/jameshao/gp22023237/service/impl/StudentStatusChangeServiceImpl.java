package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.StudentStatusChangeMapper;
import com.jameshao.gp22023237.po.Student;
import com.jameshao.gp22023237.po.StudentStatusChange;
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

    @Override
    @Transactional
    public boolean submitApplication(StudentStatusChange application) {
        // 设置申请时间 - 使用数据库存在的字段
        application.setCreateTime(new Date());
        application.setUpdateTime(new Date());

        // 初始状态为未审批 - 使用数据库实际字段名
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
            return false;
        }

        application.setMentorStatus(status);
        application.setUpdateTime(new Date());

        // 如果导师同意，且辅导员已同意，则更新学生状态
        if (status == 1 && application.getSecretaryStatus() == 1) {
            updateStudentStatus(application);
        }

        return updateById(application);
    }

    @Override
    @Transactional
    public boolean secretaryApprove(Long id, Integer status, String comment) {
        StudentStatusChange application = getById(id);
        if (application == null) {
            return false;
        }

        application.setSecretaryStatus(status);
        application.setUpdateTime(new Date());

        // 如果导师同意，且辅导员同意，则更新学生状态
        if (application.getMentorStatus() == 1 && status == 1) {
            updateStudentStatus(application);
        }

        return updateById(application);
    }

    @Override
    @Transactional
    public boolean deanApprove(Long id, Integer status, String comment) {
        StudentStatusChange application = getById(id);
        if (application == null) {
            return false;
        }

        // 数据库中没有院长审批字段，这里简单处理
        application.setUpdateTime(new Date());

        // 如果导师和辅导员都同意，则更新学生状态
        if (application.getMentorStatus() == 1 && application.getSecretaryStatus() == 1 && status == 1) {
            updateStudentStatus(application);
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
