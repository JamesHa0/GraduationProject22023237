package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.User;
import com.jameshao.gp22023237.service.UserService;
import com.jameshao.gp22023237.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author test
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-09-21 16:25:01
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




