package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.ElectronicRecordMapper;
import com.jameshao.gp22023237.po.ElectronicRecord;
import com.jameshao.gp22023237.service.ElectronicRecordService;
import org.springframework.stereotype.Service;

/**
 * 学生电子档案Service实现
 */
@Service
public class ElectronicRecordServiceImpl extends ServiceImpl<ElectronicRecordMapper, ElectronicRecord>
        implements ElectronicRecordService {
}
