package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.po.MentorStudent;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
* @author test
* @description 针对表【mentor_student(双选关系表)】的数据库操作Service
* @createDate 2025-10-08 18:16:25
*/
public interface MentorStudentService extends IService<MentorStudent> {

    /**
     * 分页查询导师学生关系（关联学生和导师信息）
     */
    IPage<Map<String, Object>> pageRelationship(Page<Map<String, Object>> page, Long studentId, Long mentorId, Boolean onlyUndetermined);

    /**
     * 创建导师学生关系
     */
    boolean createRelationship(MentorStudent mentorStudent);

    /**
     * 更新导师学生关系
     */
    boolean updateRelationship(MentorStudent mentorStudent);

    /**
     * 删除导师学生关系
     */
    boolean deleteRelationship(Long id);

    /**
     * 获取可选学生列表（还没有确定导师的学生）
     */
    List<Map<String, Object>> listAvailableStudents();

    /**
     * 获取可选导师列表（还有剩余名额的导师）- 管理端，需查看权限
     */
    List<Map<String, Object>> listAvailableMentors();

    /**
     * 获取可选导师列表（学生端，用于导师更换申请，无需查看权限检查）
     */
    List<Map<String, Object>> listAvailableMentorsForStudent();

    /**
     * 获取学生当前的导师信息
     */
    Map<String, Object> getStudentCurrentMentor(Long studentId);

    /**
     * 导出单个学生志愿表
     */
    void exportStudentVolunteer(Long studentId, jakarta.servlet.http.HttpServletResponse response);

    /**
     * 导出导师学生关系汇总表
     */
    void exportMentorStudentSummary(jakarta.servlet.http.HttpServletResponse response);
}
