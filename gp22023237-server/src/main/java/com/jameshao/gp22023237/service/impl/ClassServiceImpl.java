package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.ClassEntity;
import com.jameshao.gp22023237.service.ClassService;
import com.jameshao.gp22023237.mapper.ClassMapper;
import org.springframework.stereotype.Service;

/**
 * 班级Service实现
 */
@Service
public class ClassServiceImpl extends ServiceImpl<ClassMapper, ClassEntity>
    implements ClassService {

}
