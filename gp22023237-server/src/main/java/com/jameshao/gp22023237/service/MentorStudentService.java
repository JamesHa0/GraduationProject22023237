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
    IPage<Map<String, Object>> pageRelationship(Page<Map<String, Object>> page, Long studentId, Long mentorId);

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
     * 获取可选导师列表（还有剩余名额的导师）
     */
    List<Map<String, Object>> listAvailableMentors();
}
