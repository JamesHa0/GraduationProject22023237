package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.NoticeMapper;
import com.jameshao.gp22023237.po.Notice;
import com.jameshao.gp22023237.po.NoticeRead;
import com.jameshao.gp22023237.po.Student;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.service.NoticeReadService;
import com.jameshao.gp22023237.service.NoticeService;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.TeacherService;
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

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

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

        // 为目标用户建立未读关联（确保离线用户上线后能看到通知）
        if (targetUserId != null) {
            try {
                NoticeRead noticeRead = new NoticeRead();
                noticeRead.setNoticeId(notice.getNoticeId());
                noticeRead.setUserId(targetUserId);
                noticeRead.setReadTime(null); // 未读
                noticeReadService.save(noticeRead);
            } catch (Exception e) {
                logger.warn("通知未读记录创建失败: {}", e.getMessage());
            }
        }

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
    public void createAndPushToUsers(List<Long> userIds, String title, String content, String type) {
        if (userIds == null || userIds.isEmpty()) return;

        // 创建通知记录
        Notice notice = new Notice();
        notice.setNoticeTitle(title);
        notice.setNoticeContent(content);
        notice.setNoticeType(type);
        notice.setStatus("0");
        notice.setCreateTime(new Date());
        notice.setUpdateTime(new Date());
        save(notice);

        // 批量为目标用户建立未读关联
        List<NoticeRead> readList = new ArrayList<>();
        for (Long userId : userIds) {
            if (userId == null) continue;
            NoticeRead noticeRead = new NoticeRead();
            noticeRead.setNoticeId(notice.getNoticeId());
            noticeRead.setUserId(userId);
            noticeRead.setReadTime(null); // 未读
            readList.add(noticeRead);
        }
        if (!readList.isEmpty()) {
            try {
                noticeReadService.saveBatch(readList, 100);
            } catch (Exception e) {
                logger.warn("批量通知未读记录创建失败: {}", e.getMessage());
            }
        }

        // 逐条SSE推送（异常隔离）
        String sseData = "{\"noticeId\":" + notice.getNoticeId()
            + ",\"title\":\"" + escapeJson(title) + "\""
            + ",\"content\":\"" + escapeJson(content) + "\""
            + ",\"type\":\"" + type + "\"}";
        for (Long userId : userIds) {
            if (userId == null) continue;
            try {
                sseMessageUtils.sendToUser(userId, "notice", sseData);
            } catch (Exception e) {
                // 单条推送失败不影响其他用户
            }
        }
    }

    @Override
    public boolean createAndPushWithKey(String notificationKey, String title, String content, String type, Long targetUserId) {
        // 去重检查：如果已存在相同key的通知，跳过
        if (notificationKey != null) {
            LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Notice::getNotificationKey, notificationKey);
            if (count(wrapper) > 0) {
                logger.debug("通知已存在，跳过推送: key={}", notificationKey);
                return false;
            }
        }

        // 创建通知记录
        Notice notice = new Notice();
        notice.setNoticeTitle(title);
        notice.setNoticeContent(content);
        notice.setNoticeType(type);
        notice.setStatus("0");
        notice.setNotificationKey(notificationKey);
        notice.setCreateTime(new Date());
        notice.setUpdateTime(new Date());
        save(notice);

        // 为目标用户建立未读关联
        if (targetUserId != null) {
            try {
                NoticeRead noticeRead = new NoticeRead();
                noticeRead.setNoticeId(notice.getNoticeId());
                noticeRead.setUserId(targetUserId);
                noticeRead.setReadTime(null);
                noticeReadService.save(noticeRead);
            } catch (Exception e) {
                logger.warn("通知未读记录创建失败: {}", e.getMessage());
            }
        }

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
        return true;
    }

    @Override
    public boolean createAndPushToUsersWithKey(String notificationKey, List<Long> userIds, String title, String content, String type) {
        if (userIds == null || userIds.isEmpty()) return false;

        // 去重检查：如果已存在相同key的通知，跳过
        if (notificationKey != null) {
            LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Notice::getNotificationKey, notificationKey);
            if (count(wrapper) > 0) {
                logger.debug("通知已存在，跳过推送: key={}", notificationKey);
                return false;
            }
        }

        // 创建通知记录
        Notice notice = new Notice();
        notice.setNoticeTitle(title);
        notice.setNoticeContent(content);
        notice.setNoticeType(type);
        notice.setStatus("0");
        notice.setNotificationKey(notificationKey);
        notice.setCreateTime(new Date());
        notice.setUpdateTime(new Date());
        save(notice);

        // 批量为目标用户建立未读关联
        List<NoticeRead> readList = new ArrayList<>();
        for (Long userId : userIds) {
            if (userId == null) continue;
            NoticeRead noticeRead = new NoticeRead();
            noticeRead.setNoticeId(notice.getNoticeId());
            noticeRead.setUserId(userId);
            noticeRead.setReadTime(null); // 未读
            readList.add(noticeRead);
        }
        if (!readList.isEmpty()) {
            try {
                noticeReadService.saveBatch(readList, 100);
            } catch (Exception e) {
                logger.warn("批量通知未读记录创建失败: {}", e.getMessage());
            }
        }

        // 逐条SSE推送（异常隔离）
        String sseData = "{\"noticeId\":" + notice.getNoticeId()
            + ",\"title\":\"" + escapeJson(title) + "\""
            + ",\"content\":\"" + escapeJson(content) + "\""
            + ",\"type\":\"" + type + "\"}";
        for (Long userId : userIds) {
            if (userId == null) continue;
            try {
                sseMessageUtils.sendToUser(userId, "notice", sseData);
            } catch (Exception e) {
                // 单条推送失败不影响其他用户
            }
        }
        return true;
    }

    @Override
    public Long getStudentUserId(Long studentId) {
        if (studentId == null) return null;
        Student student = studentService.getById(studentId);
        return student != null ? student.getUserId() : null;
    }

    @Override
    public Long getTeacherUserId(Long teacherId) {
        if (teacherId == null) return null;
        Teacher teacher = teacherService.getById(teacherId);
        return teacher != null ? teacher.getUserId() : null;
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
