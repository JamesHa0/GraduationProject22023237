package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.MentorStudentMapper;
import com.jameshao.gp22023237.po.MentorStudent;
import com.jameshao.gp22023237.po.Student;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.service.MentorStudentService;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.TeacherService;
import com.jameshao.gp22023237.utils.CurrentUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
* @author test
* @description 针对表【mentor_student(双选关系表)】的数据库操作Service实现
* @createDate 2025-10-08 18:16:25
*/
@Service
public class MentorStudentServiceImpl extends ServiceImpl<MentorStudentMapper, MentorStudent>
    implements MentorStudentService{

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    /**
     * 检查是否有修改权限
     */
    private boolean hasModifyPermission() {
        Integer roleId = CurrentUserUtil.getCurrentRoleId();
        System.out.println("Current roleId for modify check: " + roleId);
        // 如果获取不到角色ID，默认允许（为了兼容）
        if (roleId == null) {
            return true;
        }
        return roleId == 1 || roleId == 4 || roleId == 5;
    }

    /**
     * 检查是否有查看权限
     */
    private boolean hasViewPermission() {
        Integer roleId = CurrentUserUtil.getCurrentRoleId();
        System.out.println("Current roleId for view check: " + roleId);
        // 如果获取不到角色ID，默认允许（为了兼容）
        if (roleId == null) {
            return true;
        }
        return roleId != 6 && roleId != 7 && roleId != 8;
    }

    @Override
    public IPage<Map<String, Object>> pageRelationship(Page<Map<String, Object>> page, Long studentId, Long mentorId) {
        if (!hasViewPermission()) {
            throw new IllegalStateException("您没有查看权限");
        }
        return baseMapper.pageRelationship(page, studentId, mentorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRelationship(MentorStudent mentorStudent) {
        if (!hasModifyPermission()) {
            throw new IllegalStateException("您没有修改权限");
        }

        // 检查学生是否已有导师
        if (mentorStudent.getStudentId() != null) {
            LambdaQueryWrapper<MentorStudent> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MentorStudent::getStudentId, mentorStudent.getStudentId())
                    .eq(MentorStudent::getStudentStatus, 1)
                    .eq(MentorStudent::getTeacherStatus, 1);
            long count = count(wrapper);
            if (count > 0) {
                throw new IllegalStateException("该学生已有导师");
            }
        }

        // 设置默认值
        if (mentorStudent.getRound() == null) {
            mentorStudent.setRound(4); // 手动分配轮次
        }
        if (mentorStudent.getStudentChoiceOrder() == null) {
            mentorStudent.setStudentChoiceOrder(1);
        }
        if (mentorStudent.getStudentStatus() == null) {
            mentorStudent.setStudentStatus(1);
        }
        if (mentorStudent.getTeacherStatus() == null) {
            mentorStudent.setTeacherStatus(1); // 直接设为已同意
        }
        mentorStudent.setSelectionTime(new Date());
        mentorStudent.setConfirmTime(new Date());

        boolean saved = save(mentorStudent);
        if (saved && mentorStudent.getMentorId() != null) {
            // 更新导师已确认名额
            Teacher teacher = teacherService.getById(mentorStudent.getMentorId());
            if (teacher != null && teacher.getConfirmedQuota() != null) {
                teacher.setConfirmedQuota(teacher.getConfirmedQuota() + 1);
                if (teacher.getRemainingQuota() != null && teacher.getRemainingQuota() > 0) {
                    teacher.setRemainingQuota(teacher.getRemainingQuota() - 1);
                }
                teacherService.updateById(teacher);
            }
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRelationship(MentorStudent mentorStudent) {
        if (!hasModifyPermission()) {
            throw new IllegalStateException("您没有修改权限");
        }
        return updateById(mentorStudent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRelationship(Long id) {
        if (!hasModifyPermission()) {
            throw new IllegalStateException("您没有修改权限");
        }

        MentorStudent mentorStudent = getById(id);
        if (mentorStudent == null) {
            throw new IllegalArgumentException("记录不存在");
        }

        boolean removed = removeById(id);
        if (removed && mentorStudent.getMentorId() != null &&
            mentorStudent.getTeacherStatus() != null && mentorStudent.getTeacherStatus() == 1) {
            // 恢复导师名额
            Teacher teacher = teacherService.getById(mentorStudent.getMentorId());
            if (teacher != null && teacher.getConfirmedQuota() != null && teacher.getConfirmedQuota() > 0) {
                teacher.setConfirmedQuota(teacher.getConfirmedQuota() - 1);
                if (teacher.getRemainingQuota() != null) {
                    teacher.setRemainingQuota(teacher.getRemainingQuota() + 1);
                }
                teacherService.updateById(teacher);
            }
        }
        return removed;
    }

    @Override
    public List<Map<String, Object>> listAvailableStudents() {
        if (!hasViewPermission()) {
            throw new IllegalStateException("您没有查看权限");
        }
        return baseMapper.listAvailableStudents();
    }

    @Override
    public List<Map<String, Object>> listAvailableMentors() {
        if (!hasViewPermission()) {
            throw new IllegalStateException("您没有查看权限");
        }
        return baseMapper.listAvailableMentors();
    }
}




