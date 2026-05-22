package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.po.ProcessConfig;

import java.util.List;

public interface ProcessConfigService extends IService<ProcessConfig> {

    /**
     * 批量保存流程配置（事务化）
     */
    boolean batchSaveConfigs(List<ProcessConfig> configs);
}
