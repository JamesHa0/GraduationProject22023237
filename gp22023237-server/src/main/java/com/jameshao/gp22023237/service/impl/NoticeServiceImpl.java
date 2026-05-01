package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.NoticeMapper;
import com.jameshao.gp22023237.po.Notice;
import com.jameshao.gp22023237.po.NoticeRead;
import com.jameshao.gp22023237.service.NoticeReadService;
import com.jameshao.gp22023237.service.NoticeService;
import com.jameshao.gp22023237.utils.SseMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 针对表【sys_notice(通知公告表)】的数据库操作Service实现
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice>
    implements NoticeService {

    private static final Logger logger = LoggerFactory.getLogger(NoticeServiceImpl.class);

    @Autowired
    private NoticeReadService noticeReadService;

    @Autowired
    private SseMessageUtils sseMessageUtils;

    @Override
    public int getUnreadCount(Long userId, Integer roleId) {
        return baseMapper.selectUnreadCount(userId, roleId);
    }

    @Override
    public List<Notice> getUnreadList(Long userId, Integer roleId) {
        return baseMapper.selectUnreadList(userId, roleId);
    }

    @Override
    public boolean markAsRead(Long userId, Long noticeId) {
        // 检查是否已读
        LambdaQueryWrapper<NoticeRead> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoticeRead::getUserId, userId)
               .eq(NoticeRead::getNoticeId, noticeId);
        long count = noticeReadService.count(wrapper);
        if (count > 0) {
            return true; // 已读，直接返回
        }
        NoticeRead noticeRead = new NoticeRead();
        noticeRead.setNoticeId(noticeId);
        noticeRead.setUserId(userId);
        noticeRead.setReadTime(new Date());
        return noticeReadService.save(noticeRead);
    }

    @Override
    public boolean markAllAsRead(Long userId, Integer roleId) {
        // 获取所有未读通知
        List<Notice> unreadList = baseMapper.selectUnreadList(userId, roleId);
        if (unreadList == null || unreadList.isEmpty()) {
            return true;
        }
        List<NoticeRead> readList = new ArrayList<>();
        Date now = new Date();
        for (Notice notice : unreadList) {
            NoticeRead noticeRead = new NoticeRead();
            noticeRead.setNoticeId(notice.getNoticeId());
            noticeRead.setUserId(userId);
            noticeRead.setReadTime(now);
            readList.add(noticeRead);
        }
        return noticeReadService.saveBatch(readList);
    }

    @Override
    public void createAndPush(String title, String content, String type, Long targetUserId) {
        // 创建通知记录
        Notice notice = new Notice();
        notice.setNoticeTitle(title);
        notice.setNoticeContent(content);
        notice.setNoticeType(type);
        notice.setStatus("0");
        notice.setCreateTime(new Date());
        notice.setUpdateTime(new Date());
        save(notice);

        // 通过SSE推送给目标用户
        try {
            sseMessageUtils.sendToUser(targetUserId, "notice",
                "{\"noticeId\":" + notice.getNoticeId()
                + ",\"title\":\"" + escapeJson(title) + "\""
                + ",\"content\":\"" + escapeJson(content) + "\""
                + ",\"type\":\"" + type + "\"}");
        } catch (Exception e) {
            logger.warn("SSE推送失败，用户可能不在线，不影响业务: {}", e.getMessage());
        }
    }

    @Override
    public Page<Notice> getUserNoticePage(int pageNum, int pageSize, Long userId, Integer roleId) {
        Page<Notice> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectUserNoticePage(page, userId, roleId);
    }

    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
