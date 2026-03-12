package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.StudentStatusChangeMapper;
import com.jameshao.gp22023237.po.StudentStatusChange;
import com.jameshao.gp22023237.service.StudentStatusChangeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 学籍异动申请Service实现
 */
@Service
public class StudentStatusChangeServiceImpl extends ServiceImpl<StudentStatusChangeMapper, StudentStatusChange>
        implements StudentStatusChangeService {

    @Override
    @Transactional
    public boolean submitSuspensionApplication(StudentStatusChange application) {
        application.setStatus(0); // 待审核
        application.setTutorApproval(0); // 导师未审核
        application.setCounselorApproval(0); // 教学秘书未审核
        application.setCreateTime(new Date());
        application.setUpdateTime(new Date());
        return save(application);
    }

    @Override
    @Transactional
    public boolean tutorApproval(Long id, Integer approval, Long tutorId) {
        StudentStatusChange application = getById(id);
        if (application == null) {
            return false;
        }

        application.setTutorApproval(approval);
        application.setTutorId(tutorId);
        application.setTutorApprovalTime(new Date());

        // 更新申请状态
        if (approval == 2) { // 导师拒绝
            application.setStatus(3); // 已拒绝
        } else if (approval == 1 && application.getCounselorApproval() == 1) {
            // 导师通过且教学秘书已通过
            application.setStatus(2); // 已通过
        } else if (approval == 1) {
            application.setStatus(1); // 审核中
        }

        application.setUpdateTime(new Date());
        return updateById(application);
    }

    @Override
    @Transactional
    public boolean counselorApproval(Long id, Integer approval, Long counselorId) {
        StudentStatusChange application = getById(id);
        if (application == null) {
            return false;
        }

        application.setCounselorApproval(approval);
        application.setCounselorId(counselorId);
        application.setCounselorApprovalTime(new Date());

        // 更新申请状态
        if (approval == 2) { // 教学秘书拒绝
            application.setStatus(3); // 已拒绝
        } else if (approval == 1 && application.getTutorApproval() == 1) {
            // 教学秘书通过且导师已通过
            application.setStatus(2); // 已通过
        } else if (approval == 1) {
            application.setStatus(1); // 审核中
        }

        application.setUpdateTime(new Date());
        return updateById(application);
    }
}
