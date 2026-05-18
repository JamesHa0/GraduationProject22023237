package com.jameshao.gp22023237.service;

/**
 * 定时通知调度服务
 * 集中管理所有时间触发类通知
 */
public interface NotificationScheduledService {

    /**
     * 双选阶段截止前24小时提醒（每小时检查）
     */
    void checkSelectionDeadline();

    /**
     * 论文阶段截止前3天/1天提醒（每日检查）
     */
    void checkThesisDeadline();

    /**
     * 论文超时未提交提醒（每日检查）
     */
    void checkThesisOverdue();

    /**
     * 学籍异动到期前30天提醒（每日检查）
     */
    void checkStatusChangeExpiry();

    /**
     * 选课截止前1天提醒（每小时检查）
     */
    void checkCourseSelectionDeadline();

    /**
     * 成绩录入截止前1天提醒（每日检查）
     */
    void checkScoreEntryDeadline();

    /**
     * 教学评价截止前3天/1天提醒（每日检查）
     */
    void checkTeachingEvaluationDeadline();
}
