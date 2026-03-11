package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.Course;
import com.jameshao.gp22023237.service.CourseService;
import com.jameshao.gp22023237.mapper.CourseMapper;
import org.springframework.stereotype.Service;

/**
* @author test
* @description 针对表【course(课程表)】的数据库操作Service实现
* @createDate 2025-10-08 17:09:36
*/
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course>
    implements CourseService{

}
