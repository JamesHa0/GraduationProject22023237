package com.jameshao.gp22023237.service;

import com.jameshao.gp22023237.utils.SseMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 异步 SSE 通知推送服务
 * 将耗时的 SSE 推送从业务线程剥离，避免阻塞 HTTP 响应和数据库事务
 * 
 * @author System
 */
@Service
public class AsyncNoticePushService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncNoticePushService.class);

    @Autowired
    private SseMessageUtils sseMessageUtils;

    /**
     * 异步推送 SSE 通知给批量用户
     * 在 Notice DB 记录和 NoticeRead 关联写入完成后调用
     * 即使推送失败也不影响业务主流程
     *
     * @param userIds  目标用户 ID 列表
     * @param sseData  SSE 消息 JSON 字符串
     */
    @Async
    public void pushToUsersAsync(List<Long> userIds, String sseData) {
        if (userIds == null || userIds.isEmpty()) return;
        logger.debug("开始异步SSE推送, 目标用户数: {}", userIds.size());
        for (Long userId : userIds) {
            if (userId == null) continue;
            try {
                sseMessageUtils.sendToUser(userId, "notice", sseData);
            } catch (Exception e) {
                // 单条推送失败不影响其他用户
                logger.trace("SSE推送失败(userId={}): {}", userId, e.getMessage());
            }
        }
        logger.debug("异步SSE推送完成, 目标用户数: {}", userIds.size());
    }

    /**
     * 异步推送 SSE 通知给单个用户
     *
     * @param userId  目标用户 ID
     * @param sseData SSE 消息 JSON 字符串
     */
    @Async
    public void pushToUserAsync(Long userId, String sseData) {
        if (userId == null) return;
        try {
            sseMessageUtils.sendToUser(userId, "notice", sseData);
        } catch (Exception e) {
            logger.trace("SSE推送失败(userId={}): {}", userId, e.getMessage());
        }
    }
}
