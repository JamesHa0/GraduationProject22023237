package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.SystemField;
import com.jameshao.gp22023237.service.SystemFieldService;
import com.jameshao.gp22023237.mapper.SystemFieldMapper;
import org.springframework.stereotype.Service;

/**
* @author test
* @description 针对表【sys_field(系统字段管理表)】的数据库操作Service实现
* @createDate 2025-01-01 00:00:00
*/
@Service
public class SystemFieldServiceImpl extends ServiceImpl<SystemFieldMapper, SystemField>
    implements SystemFieldService{

}
