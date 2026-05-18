package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.po.Teacher;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
* @author test
* @description 针对表【teacher(教师信息表)】的数据库操作Service
* @createDate 2026-01-20 13:12:32
*/
public interface TeacherService extends IService<Teacher> {

    /**
     * 分页查询导师名额列表（仅is_mentor=1的导师）
     */
    IPage<Map<String, Object>> pageQuotaList(Page<Map<String, Object>> page, String teacherName, String department);

    /**
     * 单个导师名额更新
     * @param teacherId 导师ID
     * @param quota 新的招生名额
     */
    boolean updateQuota(Long teacherId, Integer quota);

    /**
     * 批量导师名额更新
     * @param teacherIds 导师ID列表
     * @param quota 统一设置的招生名额
     * @return 更新结果（成功数、失败数、失败详情）
     */
    Map<String, Object> batchUpdateQuota(List<Long> teacherIds, Integer quota);

    /**
     * 重置所有导师名额为0
     */
    boolean resetAllQuota();
}
