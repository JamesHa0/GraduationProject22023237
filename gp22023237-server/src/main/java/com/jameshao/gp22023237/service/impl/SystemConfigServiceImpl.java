package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.SystemConfig;
import com.jameshao.gp22023237.service.SystemConfigService;
import com.jameshao.gp22023237.mapper.SystemConfigMapper;
import com.jameshao.gp22023237.utils.ConfigUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author test
* @description 针对表【system_config(系统配置表)】的数据库操作Service实现
* @createDate 2025-10-08 20:45:40
*/
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig>
    implements SystemConfigService{

    @Autowired
    private ConfigUtil configUtil;
    /**
     * 获取配置值（先查缓存，缓存没有则查数据库并更新缓存）
     */
    @Override
    public String getConfigValue(String configKey) {
    // 参数校验
        if (configKey == null || configKey.isEmpty()) {
        return null;
    }

    // 1. 先从缓存获取
    String value = configUtil.getConfigValue(configKey);

    // 2. 缓存未命中则查数据库
    if (value == null) {
        try {
            LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SystemConfig::getConfigKey, configKey);
            SystemConfig config = this.getOne(queryWrapper);
            if (config != null) {
                value = config.getConfigValue();
                // 更新缓存
                configUtil.updateConfigCache(configKey, value);
            } else {
                // 防止缓存穿透，对于不存在的key也进行缓存标记
                configUtil.updateConfigCache(configKey, null);
            }
        } catch (Exception e) {
            // 记录异常日志，但不影响主流程
            // logger.error("查询系统配置异常，configKey: " + configKey, e);
            return null;
        }
    }
    return value;
}


    /**
     * 更新配置
     */
    @Override
    public boolean updateConfig(SystemConfig config) {
        boolean result = updateById(config);
        if (result) {
            // 更新Redis缓存
            configUtil.updateConfigCache(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }

}




