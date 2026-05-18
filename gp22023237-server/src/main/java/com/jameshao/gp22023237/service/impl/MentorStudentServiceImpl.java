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

import com.jameshao.gp22023237.utils.MutualSelectionExportUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

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
        // 如果获取不到角色ID，默认拒绝（安全原则）
        if (roleId == null) {
            return false;
        }
        return roleId == 1 || roleId == 4 || roleId == 5;
    }

    /**
     * 检查是否有查看权限
     */
    private boolean hasViewPermission() {
        Integer roleId = CurrentUserUtil.getCurrentRoleId();
        System.out.println("Current roleId for view check: " + roleId);
        // 如果获取不到角色ID，默认拒绝（安全原则）
        if (roleId == null) {
            return false;
        }
        return roleId != 6 && roleId != 7 && roleId != 8;
    }

    @Override
    public IPage<Map<String, Object>> pageRelationship(Page<Map<String, Object>> page, Long studentId, Long mentorId, Boolean onlyUndetermined) {
        if (!hasViewPermission()) {
            throw new IllegalStateException("您没有查看权限");
        }
        return baseMapper.pageRelationship(page, studentId, mentorId, onlyUndetermined);
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
            // 更新导师已确认名额（处理null值）
            Teacher teacher = teacherService.getById(mentorStudent.getMentorId());
            if (teacher != null) {
                int currentQuota = teacher.getConfirmedQuota() != null ? teacher.getConfirmedQuota() : 0;
                int currentRemaining = teacher.getRemainingQuota() != null ? teacher.getRemainingQuota() : 0;
                teacher.setConfirmedQuota(currentQuota + 1);
                teacher.setRemainingQuota(Math.max(0, currentRemaining - 1));
                teacherService.updateById(teacher);
            }

            // 更新学生selection_status为3（已确定）
            Student student = studentService.getById(mentorStudent.getStudentId());
            if (student != null) {
                student.setSelectionStatus(3);
                student.setUpdateTime(new Date());
                studentService.updateById(student);
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

    @Override
    public List<Map<String, Object>> listAvailableMentorsForStudent() {
        return baseMapper.listAvailableMentors();
    }

    @Override
    public Map<String, Object> getStudentCurrentMentor(Long studentId) {
        return baseMapper.getStudentCurrentMentor(studentId);
    }

    @Override
    public void exportStudentVolunteer(Long studentId, jakarta.servlet.http.HttpServletResponse response) {
        if (!hasViewPermission()) {
            throw new IllegalStateException("您没有查看权限");
        }

        try {
            // 获取学生基本信息
            Map<String, Object> studentInfoMap = baseMapper.getStudentInfo(studentId);
            if (studentInfoMap == null) {
                throw new IllegalArgumentException("学生不存在");
            }

            // 获取学生志愿列表
            List<Map<String, Object>> volunteers = baseMapper.getStudentVolunteers(studentId);

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setCharacterEncoding("utf-8");
            String studentName = studentInfoMap.get("studentName") != null ? studentInfoMap.get("studentName").toString() : "学生";
            String fileName = java.net.URLEncoder.encode("学生志愿表_" + studentName, java.nio.charset.StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".docx");

            // 准备文本占位符数据
            java.util.Map<String, String> dataMap = new java.util.HashMap<>();
            dataMap.put("studentNo", studentInfoMap.get("studentNo") != null ? studentInfoMap.get("studentNo").toString() : "");
            dataMap.put("studentName", studentName);
            dataMap.put("department", studentInfoMap.get("department") != null ? studentInfoMap.get("department").toString() : "");
            dataMap.put("major", studentInfoMap.get("major") != null ? studentInfoMap.get("major").toString() : "");

            // 填充志愿导师信息
            String firstChoiceTeacher = "";
            String secondChoiceTeacher = "";
            if (volunteers != null) {
                if (volunteers.size() > 0) {
                    Map<String, Object> first = volunteers.get(0);
                    firstChoiceTeacher = first.get("teacherName") != null ? first.get("teacherName").toString() : "";
                }
                if (volunteers.size() > 1) {
                    Map<String, Object> second = volunteers.get(1);
                    secondChoiceTeacher = second.get("teacherName") != null ? second.get("teacherName").toString() : "";
                }
            }
            dataMap.put("firstChoiceTeacher", firstChoiceTeacher);
            dataMap.put("secondChoiceTeacher", secondChoiceTeacher);

            // 加载模板并填充（不使用表格数据）
            try (InputStream templateInputStream = getClass().getClassLoader().getResourceAsStream("templates/word/学生志愿表.docx");
                 OutputStream outputStream = response.getOutputStream()) {

                if (templateInputStream == null) {
                    throw new RuntimeException("模板文件未找到: templates/word/学生志愿表.docx");
                }

                System.out.println("=== 学生志愿表 dataMap ===");
                System.out.println(dataMap.toString());

                com.jameshao.gp22023237.utils.WordExportUtil.fillTemplateAndExport(
                    templateInputStream, outputStream, dataMap, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }

    @Override
    public void exportMentorStudentSummary(jakarta.servlet.http.HttpServletResponse response) {
        if (!hasViewPermission()) {
            throw new IllegalStateException("您没有查看权限");
        }

        try {
            // 1. 获取已确认的关系列表（已按 major, teacherName, studentNo 排序）
            List<Map<String, Object>> relationships = baseMapper.listConfirmedRelationships();

            // 2. 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setCharacterEncoding("utf-8");
            String fileName = java.net.URLEncoder.encode("互选汇总表", java.nio.charset.StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".docx");

            // 3. 准备信息行占位符数据
            Map<String, String> headerInfo = new HashMap<>();
            headerInfo.put("department", "计算机学院");
            headerInfo.put("studentCount", relationships != null ? String.valueOf(relationships.size()) : "0");
            headerInfo.put("admissionYear", "2024");

            // 4. 将平铺数据按 (专业, 导师) 分组聚合
            List<MutualSelectionExportUtil.MentorGroup> groupData =
                    MutualSelectionExportUtil.groupByMentor(relationships);

            // 5. 加载模板并导出
            try (InputStream templateInputStream = getClass().getClassLoader()
                    .getResourceAsStream("templates/word/互选汇总表.docx");
                 OutputStream outputStream = response.getOutputStream()) {

                if (templateInputStream == null) {
                    throw new RuntimeException("模板文件未找到: templates/word/互选汇总表.docx");
                }

                System.out.println("=== 互选汇总表导出 ===");
                System.out.println("headerInfo: " + headerInfo);
                System.out.println("分组数: " + groupData.size());
                System.out.println("总记录数: " + (relationships != null ? relationships.size() : 0));

                MutualSelectionExportUtil.exportSummary(
                        templateInputStream, outputStream, headerInfo, groupData);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }
}





