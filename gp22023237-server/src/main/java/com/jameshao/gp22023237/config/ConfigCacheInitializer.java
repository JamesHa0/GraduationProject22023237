package com.jameshao.gp22023237.config;

import com.jameshao.gp22023237.po.SystemConfig;
import com.jameshao.gp22023237.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统启动时加载配置项到Redis缓存
 */
@Component
public class ConfigCacheInitializer implements CommandLineRunner {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 配置项在Redis中的前缀
    private static final String CONFIG_CACHE_PREFIX = "system:config:";

    @Override
    public void run(String... args) throws Exception {
        // 1. 查询所有配置项
        List<SystemConfig> configList = systemConfigService.list();

        if (configList != null && !configList.isEmpty()) {
            // 2. 转换为Map便于处理
            Map<String, String> configMap = configList.stream()
                    .collect(Collectors.toMap(
                            SystemConfig::getConfigKey,
                            SystemConfig::getConfigValue
                    ));

            // 3. 批量存入Redis
            redisTemplate.opsForValue().multiSet(
                    configMap.entrySet().stream()
                            .collect(Collectors.toMap(
                                    entry -> CONFIG_CACHE_PREFIX + entry.getKey(),
                                    Map.Entry::getValue
                            ))
            );

            // 4. 可以设置过期时间，也可以永久有效
            // 这里设置为7天过期，到期后可通过定时任务刷新或下次访问时更新
            configMap.keySet().forEach(key -> {
                redisTemplate.expire(CONFIG_CACHE_PREFIX + key, 7, java.util.concurrent.TimeUnit.DAYS);
            });

            System.out.println("系统配置已加载到Redis缓存，共" + configList.size() + "条配置");
        }
    }
}

