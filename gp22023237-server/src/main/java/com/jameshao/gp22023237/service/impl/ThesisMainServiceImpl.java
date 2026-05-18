package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.ThesisMainMapper;
import com.jameshao.gp22023237.po.MentorStudent;
import com.jameshao.gp22023237.po.Student;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.po.ThesisMain;
import com.jameshao.gp22023237.service.MentorStudentService;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.TeacherService;
import com.jameshao.gp22023237.service.ThesisMainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class ThesisMainServiceImpl extends ServiceImpl<ThesisMainMapper, ThesisMain>
        implements ThesisMainService {

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private MentorStudentService mentorStudentService;

    @Override
    public ThesisMain getByStudentId(Long studentId) {
        LambdaQueryWrapper<ThesisMain> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThesisMain::getStudentId, studentId);
        return getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ThesisMain getOrCreateByStudentId(Long studentId) {
        ThesisMain existing = getByStudentId(studentId);
        if (existing != null) {
            return existing;
        }

        // 从Student表获取学生基本信息
        Student student = studentService.getById(studentId);
        if (student == null) {
            throw new IllegalArgumentException("学生不存在，ID: " + studentId);
        }

        ThesisMain thesisMain = new ThesisMain();
        thesisMain.setStudentId(studentId);
        thesisMain.setStudentNo(student.getStudentNo());
        thesisMain.setStudentName(student.getStudentName());
        thesisMain.setMajor(student.getMajor());
        thesisMain.setGrade(student.getCohortYear() != null ? student.getCohortYear().toString() : null);
        thesisMain.setFinalResult(0);
        thesisMain.setArchiveStatus(0);

        // 从MentorStudent表查找学生的第一导师
        LambdaQueryWrapper<MentorStudent> msWrapper = new LambdaQueryWrapper<>();
        msWrapper.eq(MentorStudent::getStudentId, studentId);
        msWrapper.eq(MentorStudent::getMentorType, 1); // 第一导师
        msWrapper.eq(MentorStudent::getTeacherStatus, 1); // 导师已同意
        msWrapper.orderByDesc(MentorStudent::getConfirmTime);
        msWrapper.last("LIMIT 1");
        MentorStudent mentorStudent = mentorStudentService.getOne(msWrapper, false);

        if (mentorStudent != null) {
            thesisMain.setSupervisorId(mentorStudent.getMentorId());
            // 获取导师姓名
            Teacher mentor = teacherService.getById(mentorStudent.getMentorId());
            if (mentor != null) {
                thesisMain.setSupervisorName(mentor.getTeacherName());
            }
        }

        save(thesisMain);
        return thesisMain;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateThesisTitle(Long id, String thesisTitle) {
        ThesisMain thesisMain = getById(id);
        if (thesisMain == null) {
            throw new IllegalArgumentException("论文主记录不存在");
        }
        thesisMain.setThesisTitle(thesisTitle);
        return updateById(thesisMain);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateThesisFinalUrl(Long id, String thesisFinalUrl) {
        ThesisMain thesisMain = getById(id);
        if (thesisMain == null) {
            throw new IllegalArgumentException("论文主记录不存在");
        }
        thesisMain.setThesisFinalUrl(thesisFinalUrl);
        return updateById(thesisMain);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean archiveThesis(Long id) {
        ThesisMain thesisMain = getById(id);
        if (thesisMain == null) {
            throw new IllegalArgumentException("论文主记录不存在");
        }
        thesisMain.setArchiveStatus(1);
        thesisMain.setArchiveTime(new Date());
        return updateById(thesisMain);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFinalResult(Long id, Integer finalResult) {
        ThesisMain thesisMain = getById(id);
        if (thesisMain == null) {
            throw new IllegalArgumentException("论文主记录不存在");
        }
        thesisMain.setFinalResult(finalResult);
        return updateById(thesisMain);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchArchiveThesis(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("ID列表不能为空");
        }
        Date now = new Date();
        // 使用LambdaUpdateWrapper批量更新，避免N+1查询问题
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ThesisMain> updateWrapper =
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        updateWrapper.in(ThesisMain::getId, ids)
                     .ne(ThesisMain::getArchiveStatus, 1)
                     .set(ThesisMain::getArchiveStatus, 1)
                     .set(ThesisMain::getArchiveTime, now);
        return update(updateWrapper);
    }
}
