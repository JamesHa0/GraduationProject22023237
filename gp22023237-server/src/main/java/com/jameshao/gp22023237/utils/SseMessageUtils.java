package com.jameshao.gp22023237.utils;

import com.jameshao.gp22023237.controller.system.SseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 消息推送工具类
 * 提供向指定用户推送和全体广播功能
 * 所有推送方法内部均用try-catch包裹，确保推送异常不会向上传播导致业务事务回滚
 */
@Component
public class SseMessageUtils {

    private static final Logger logger = LoggerFactory.getLogger(SseMessageUtils.class);

    /**
     * 向指定用户推送消息
     *
     * @param userId 目标用户ID
     * @param event  事件名称
     * @param data   消息数据
     */
    public void sendToUser(Long userId, String event, String data) {
        try {
            ConcurrentHashMap<Long, SseEmitter> emitters = SseController.getEmitters();
            SseEmitter emitter = emitters.get(userId);
            if (emitter == null) {
                logger.debug("用户{}不在线，跳过SSE推送", userId);
                return;
            }
            try {
                emitter.send(SseEmitter.event().name(event).data(data));
                logger.debug("SSE推送成功，userId={}, event={}", userId, event);
            } catch (IOException e) {
                // 推送失败，移除失效连接
                logger.warn("SSE推送失败，移除连接，userId={} - {}", userId, e.getMessage());
                emitters.remove(userId);
                try {
                    emitter.complete();
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            // 外层兜底，确保不影响业务事务
            logger.warn("SSE推送异常（不影响业务），userId={} - {}", userId, e.getMessage());
        }
    }

    /**
     * 向所有在线用户广播消息
     *
     * @param event 事件名称
     * @param data  消息数据
     */
    public void publishAll(String event, String data) {
        try {
            ConcurrentHashMap<Long, SseEmitter> emitters = SseController.getEmitters();
            for (Map.Entry<Long, SseEmitter> entry : emitters.entrySet()) {
                try {
                    entry.getValue().send(SseEmitter.event().name(event).data(data));
                } catch (IOException e) {
                    logger.warn("SSE广播失败，移除连接，userId={} - {}", entry.getKey(), e.getMessage());
                    emitters.remove(entry.getKey());
                    try {
                        entry.getValue().complete();
                    } catch (Exception ignored) {
                    }
                }
            }
            logger.debug("SSE广播完成，event={}", event);
        } catch (Exception e) {
            logger.warn("SSE广播异常（不影响业务）- {}", e.getMessage());
        }
    }
}
