package com.jameshao.gp22023237.service;

import com.jameshao.gp22023237.po.Notice;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 针对表【sys_notice(通知公告表)】的数据库操作Service
 */
public interface NoticeService extends IService<Notice> {

    /**
     * 获取用户未读通知数量
     */
    int getUnreadCount(Long userId, Integer roleId);

    /**
     * 获取用户未读通知列表
     */
    List<Notice> getUnreadList(Long userId, Integer roleId);

    /**
     * 标记指定通知为已读
     */
    boolean markAsRead(Long userId, Long noticeId);

    /**
     * 标记用户所有通知为已读
     */
    boolean markAllAsRead(Long userId, Integer roleId);

    /**
     * 创建通知并推送给指定用户
     */
    void createAndPush(String title, String content, String type, Long targetUserId);

    /**
     * 批量创建通知并推送给多个用户
     * @param userIds 目标用户ID列表（sys_user表ID）
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     */
    void createAndPushToUsers(List<Long> userIds, String title, String content, String type);

    /**
     * 创建通知并推送给指定用户（带去重键，防止重复推送）
     * @param notificationKey 去重键（格式：模块:事件:关联ID:日期）
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @param targetUserId 目标用户ID（sys_user表ID）
     * @return true=推送成功，false=已存在相同key的通知（去重跳过）
     */
    boolean createAndPushWithKey(String notificationKey, String title, String content, String type, Long targetUserId);

    /**
     * 批量创建通知并推送给多个用户（带去重键，防止重复推送）
     * @param notificationKey 去重键（格式：模块:事件:关联ID:日期）
     * @param userIds 目标用户ID列表（sys_user表ID）
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @return true=推送成功，false=已存在相同key的通知（去重跳过）
     */
    boolean createAndPushToUsersWithKey(String notificationKey, List<Long> userIds, String title, String content, String type);

    /**
     * 根据学生实体ID获取对应的系统用户ID
     * @param studentId 学生表ID
     * @return sys_user表ID，不存在则返回null
     */
    Long getStudentUserId(Long studentId);

    /**
     * 根据教师实体ID获取对应的系统用户ID
     * @param teacherId 教师表ID
     * @return sys_user表ID，不存在则返回null
     */
    Long getTeacherUserId(Long teacherId);

    /**
     * 分页获取用户可见通知列表（含已读状态）
     */
    Page<Notice> getUserNoticePage(int pageNum, int pageSize, Long userId, Integer roleId);
}
