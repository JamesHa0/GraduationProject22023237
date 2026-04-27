package com.jameshao.gp22023237.service;

import java.util.Map;

/**
 * 课程阶段管理服务接口
 * 阶段定义：0=未开始, 1=选课阶段, 2=已开课, 3=成绩录入阶段, 4=已结课
 */
public interface CoursePhaseService {

    /**
     * 获取当前课程阶段
     * @return 阶段值（0/1/2/3/4）
     */
    int getCurrentPhase();

    /**
     * 推进到下一阶段
     * @param endTime 截止时间（选课阶段为学生截止时间，成绩录入阶段为教师截止时间）
     * @return 是否成功
     */
    boolean advancePhase(String endTime);

    /**
     * 重置课程阶段（回到未开始）
     * @return 是否成功
     */
    boolean resetPhase();

    /**
     * 更新当前阶段的截止时间
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 是否成功
     */
    boolean updateDeadline(String configKey, String configValue);

    /**
     * 获取课程阶段所有配置
     * @return 配置Map
     */
    Map<String, String> getPhaseConfig();

    /**
     * 判断当前是否在选课时间窗口内
     * @return true=可选课，false=不可选课
     */
    boolean isSelectionOpen();

    /**
     * 判断当前是否在成绩录入时间窗口内
     * @return true=可录入，false=不可录入
     */
    boolean isScoreEntryOpen();

    /**
     * 获取课程阶段统计信息
     * @return 统计数据Map
     */
    Map<String, Object> getPhaseStatistics();
}
