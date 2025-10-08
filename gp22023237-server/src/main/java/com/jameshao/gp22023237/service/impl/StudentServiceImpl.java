package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.Student;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.mapper.StudentMapper;
import org.springframework.stereotype.Service;

/**
* @author test
* @description 针对表【student(学生信息表)】的数据库操作Service实现
* @createDate 2025-10-08 17:09:36
*/
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student>
    implements StudentService{

}




