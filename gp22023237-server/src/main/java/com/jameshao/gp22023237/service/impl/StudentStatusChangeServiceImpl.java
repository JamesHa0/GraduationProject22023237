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
        // 设置申请时间
        application.setApplyTime(new Date());
        application.setCreateTime(new Date());
        application.setUpdateTime(new Date());

        // 初始状态为未审批
        application.setMentorStatus(0);
        application.setSecretaryStatus(0);
        application.setDeanStatus(0);

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
        application.setMentorComment(comment);
        application.setUpdateTime(new Date());

        // 如果导师同意，且教学秘书已同意，且院长已同意，则更新学生状态
        if (status == 1 && application.getSecretaryStatus() == 1 && application.getDeanStatus() == 1) {
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
        application.setSecretaryComment(comment);
        application.setUpdateTime(new Date());

        // 如果导师同意，教学秘书同意，且院长已同意，则更新学生状态
        if (application.getMentorStatus() == 1 && status == 1 && application.getDeanStatus() == 1) {
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

        application.setDeanStatus(status);
        application.setDeanComment(comment);
        application.setUpdateTime(new Date());

        // 如果导师同意，教学秘书同意，且院长同意，则更新学生状态
        if (application.getMentorStatus() == 1 && application.getSecretaryStatus() == 1 && status == 1) {
            updateStudentStatus(application);
        }

        return updateById(application);
    }

    private void updateStudentStatus(StudentStatusChange application) {
        Student student = studentService.getById(application.getStudentId());
        if (student != null) {
            student.setStatus(application.getNewStatus());
            student.setUpdateTime(new Date());
            studentService.updateById(student);
        }
    }
}
