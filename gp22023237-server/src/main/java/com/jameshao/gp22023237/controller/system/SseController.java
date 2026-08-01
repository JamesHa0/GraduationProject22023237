package com.jameshao.gp22023237.controller.system;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.alibaba.fastjson.JSONObject;
import com.jameshao.gp22023237.po.User;
import com.jameshao.gp22023237.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 推送控制器
 * 管理用户SSE连接，支持实时消息推送
 */
@RestController
@RequestMapping("/system/sse")
public class SseController {

    private static final Logger logger = LoggerFactory.getLogger(SseController.class);

    /**
     * 在线用户SSE连接映射表
     * key: userId, value: SseEmitter
     */
    private static final ConcurrentHashMap<Long, SseEmitter> SSE_EMITTERS = new ConcurrentHashMap<>();

    @Autowired
    private RedisUtils redisUtils;

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * SSE 订阅端点
     * 通过URL参数传递token进行认证（EventSource不支持自定义请求头）
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam(required = false) String token) {
        // 1. 空 token 检查
        if (token == null || token.trim().isEmpty()) {
            logger.debug("SSE连接失败：token为空");
            return null;
        }

        // 2. Redis 存在校验
        if (!redisUtils.hasKey(token)) {
            logger.debug("SSE连接失败：token不存在于Redis，可能已过期或未登录");
            return null;
        }

        // 3. JWT 签名校验
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(jwtSecret)).build();
            jwtVerifier.verify(token);
        } catch (JWTVerificationException e) {
            logger.debug("SSE连接失败：JWT校验不通过 - {}", e.getMessage());
            return null;
        }

        // 4. 获取 userId
        Object obj = redisUtils.get(token);
        User user = JSONObject.parseObject(JSONObject.toJSONString(obj), User.class);
        if (user == null || user.getId() == null) {
            logger.warn("SSE连接失败：无法解析用户信息，token={}", token.substring(0, Math.min(20, token.length())));
            return null;
        }
        Long userId = user.getId();

        // 5. 如果已有旧连接，先关闭
        SseEmitter oldEmitter = SSE_EMITTERS.get(userId);
        if (oldEmitter != null) {
            oldEmitter.complete();
            SSE_EMITTERS.remove(userId);
        }

        // 6. 创建 SseEmitter
        SseEmitter emitter = new SseEmitter(0L); // 0表示不超时

        emitter.onCompletion(() -> {
            logger.info("SSE连接完成，userId={}", userId);
            SSE_EMITTERS.remove(userId);
        });

        emitter.onTimeout(() -> {
            logger.info("SSE连接超时，userId={}", userId);
            SSE_EMITTERS.remove(userId);
        });

        emitter.onError((ex) -> {
            logger.warn("SSE连接异常，userId={} - {}", userId, ex.getMessage());
            SSE_EMITTERS.remove(userId);
        });

        SSE_EMITTERS.put(userId, emitter);
        logger.info("SSE连接建立成功，userId={}", userId);

        // 发送初始连接成功消息
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException e) {
            logger.warn("SSE初始消息发送失败，userId={}", userId);
        }

        return emitter;
    }

    /**
     * 获取SSE连接映射表（供SseMessageUtils使用）
     */
    public static ConcurrentHashMap<Long, SseEmitter> getEmitters() {
        return SSE_EMITTERS;
    }
}
