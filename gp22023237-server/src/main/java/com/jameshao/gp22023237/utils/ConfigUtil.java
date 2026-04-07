package com.jameshao.gp22023237.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 配置获取工具类
 */
@Component
public class ConfigUtil {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String CONFIG_CACHE_PREFIX = "system:config:";

    /**
     * 从Redis获取配置值
     */
    public String getConfigValue(String configKey) {
        return redisTemplate.opsForValue().get(CONFIG_CACHE_PREFIX + configKey);
    }

    /**
     * 获取整数类型的配置值
     */
    public Integer getIntConfigValue(String configKey) {
        String value = getConfigValue(configKey);
        return value != null ? Integer.parseInt(value) : null;
    }

    /**
     * 更新配置缓存（当管理员修改配置时调用）
     */
    public void updateConfigCache(String configKey, String configValue) {
        redisTemplate.opsForValue().set(
                CONFIG_CACHE_PREFIX + configKey,
                configValue,
                7,
                java.util.concurrent.TimeUnit.DAYS
        );
    }

    /**
     * 清除单个配置缓存
     */
    public void clearConfigCache(String configKey) {
        redisTemplate.delete(CONFIG_CACHE_PREFIX + configKey);
    }

    /**
     * 移除配置缓存（别名方法）
     */
    public void removeConfigCache(String configKey) {
        clearConfigCache(configKey);
    }

    /**
     * 清除所有配置缓存
     */
    public void clearAllConfigCache() {
        // 使用keys模式删除所有配置缓存（生产环境建议使用scan）
        redisTemplate.delete(redisTemplate.keys(CONFIG_CACHE_PREFIX + "*"));
    }
}
