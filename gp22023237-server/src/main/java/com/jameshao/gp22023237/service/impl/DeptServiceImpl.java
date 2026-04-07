package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.Dept;
import com.jameshao.gp22023237.service.DeptService;
import com.jameshao.gp22023237.mapper.DeptMapper;
import org.springframework.stereotype.Service;

/**
* @author test
* @description 针对表【dept(部门表)】的数据库操作Service实现
* @createDate 2025-01-01 00:00:00
*/
@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept>
    implements DeptService{

}
