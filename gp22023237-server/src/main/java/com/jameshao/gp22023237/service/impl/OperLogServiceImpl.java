package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.OperLog;
import com.jameshao.gp22023237.service.OperLogService;
import com.jameshao.gp22023237.mapper.OperLogMapper;
import org.springframework.stereotype.Service;

/**
 * 针对表【sys_oper_log(操作日志记录)】的数据库操作Service实现
 */
@Service
public class OperLogServiceImpl extends ServiceImpl<OperLogMapper, OperLog>
    implements OperLogService {

}
