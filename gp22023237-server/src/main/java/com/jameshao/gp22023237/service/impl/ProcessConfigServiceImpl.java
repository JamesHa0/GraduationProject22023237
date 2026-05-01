package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.ProcessConfigMapper;
import com.jameshao.gp22023237.po.ProcessConfig;
import com.jameshao.gp22023237.service.ProcessConfigService;
import org.springframework.stereotype.Service;

@Service
public class ProcessConfigServiceImpl extends ServiceImpl<ProcessConfigMapper, ProcessConfig>
        implements ProcessConfigService {
}
