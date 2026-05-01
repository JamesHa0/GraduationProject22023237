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
     * 分页获取用户可见通知列表（含已读状态）
     */
    Page<Notice> getUserNoticePage(int pageNum, int pageSize, Long userId, Integer roleId);
}
