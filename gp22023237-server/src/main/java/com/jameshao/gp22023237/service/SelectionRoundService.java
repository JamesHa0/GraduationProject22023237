package com.jameshao.gp22023237.service;

import java.util.List;
import java.util.Map;

/**
 * 分轮次选择服务接口
 */
public interface SelectionRoundService {

    /**
     * 获取当前轮次
     * @return 当前轮次（1=第一轮, 2=第二轮, 3=第三轮, 4=补选）
     */
    int getCurrentRound();

    /**
     * 切换到指定轮次
     * @param targetRound 目标轮次
     * @return 是否成功
     */
    boolean switchRound(int targetRound);

    /**
     * 获取所有轮次配置
     * @return 配置Map
     */
    Map<String, String> getRoundConfig();

    /**
     * 更新轮次配置
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 是否成功
     */
    boolean updateRoundConfig(String configKey, String configValue);

    /**
     * 将被拒绝学生的志愿推进到下一轮
     * @return 推进的记录数
     */
    int advanceRejectedStudents();

    /**
     * 将指定学生的志愿推进到下一轮
     * @param studentId 学生ID
     * @return 是否成功
     */
    boolean advanceStudentToNextRound(Long studentId);

    /**
     * 获取三轮后仍未匹配的学生列表
     * @return 未匹配学生列表
     */
    List<Map<String, Object>> getUnmatchedStudents();

    /**
     * 手动为学生分配导师
     * @param studentId 学生ID
     * @param mentorId 导师ID
     * @return 是否成功
     */
    boolean manualAssignMentor(Long studentId, Long mentorId);

    /**
     * 获取各轮次统计数据
     * @return 统计数据Map
     */
    Map<String, Object> getRoundStatistics();
}
