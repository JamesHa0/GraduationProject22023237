package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.ProcessConfigMapper;
import com.jameshao.gp22023237.po.ProcessConfig;
import com.jameshao.gp22023237.service.ProcessConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProcessConfigServiceImpl extends ServiceImpl<ProcessConfigMapper, ProcessConfig>
        implements ProcessConfigService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSaveConfigs(List<ProcessConfig> configs) {
        for (ProcessConfig config : configs) {
            if (config.getId() != null) {
                updateById(config);
            } else {
                save(config);
            }
        }
        return true;
    }
}
