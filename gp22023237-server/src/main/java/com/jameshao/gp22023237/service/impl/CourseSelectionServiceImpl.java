package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.CourseSelection;
import com.jameshao.gp22023237.service.CourseSelectionService;
import com.jameshao.gp22023237.mapper.CourseSelectionMapper;
import org.springframework.stereotype.Service;

/**
* @author test
* @description 针对表【course_selection(选课记录表)】的数据库操作Service实现
* @createDate 2025-10-08 17:09:36
*/
@Service
public class CourseSelectionServiceImpl extends ServiceImpl<CourseSelectionMapper, CourseSelection>
    implements CourseSelectionService{

}
