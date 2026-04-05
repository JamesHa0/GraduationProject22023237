package com.jameshao.gp22023237.service;

import java.util.List;
import java.util.Map;

/**
 * 分轮次选择服务接口
 */
public interface SelectionRoundService {

    /**
     * 获取当前轮次
     * @return 当前轮次（0=未开始, 1=第一轮, 2=第二轮, 3=第三轮, 4=补选）
     */
    int getCurrentRound();

    /**
     * 获取最大轮次配置
     * @return 最大轮次
     */
    int getMaxRound();

    /**
     * 推进到下一轮
     * @param endTimeStudent 学生截止时间
     * @param endTimeTutor 导师截止时间
     * @return 是否成功
     */
    boolean advanceToNextRound(String endTimeStudent, String endTimeTutor);

    /**
     * 重置双选
     * @return 是否成功
     */
    boolean resetRounds();

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

    /**
     * 判断当前学生是否可以选择（是否在学生选择时间内）
     * @return true-可以选择，false-不能选择
     */
    boolean canStudentSelect();

    /**
     * 判断当前导师是否可以选择（是否在导师确认时间内）
     * @return true-可以选择，false-不能选择
     */
    boolean canMentorSelect();

    /**
     * 获取当前阶段状态
     * @return 0-未开始，1-学生选择中，2-导师确认中，3-中间状态（等待推进），4-已结束
     */
    int getCurrentPhase();

    /**
     * 获取用于数据库查询的实际轮次（处理中间阶段值）
     * @return 实际轮次（0,1,2,3,4）
     */
    int getQueryRound();
}
