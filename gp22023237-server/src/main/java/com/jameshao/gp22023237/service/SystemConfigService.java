package com.jameshao.gp22023237.service;

import com.jameshao.gp22023237.po.SystemConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author test
* @description 针对表【system_config(系统配置表)】的数据库操作Service
* @createDate 2025-10-08 20:45:40
*/
public interface SystemConfigService extends IService<SystemConfig> {
    String getConfigValue(String configKey);

    boolean updateConfig(SystemConfig config);

}
