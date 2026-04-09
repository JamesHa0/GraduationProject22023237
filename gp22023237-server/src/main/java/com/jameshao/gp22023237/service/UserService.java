package com.jameshao.gp22023237.service;

import com.jameshao.gp22023237.po.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;

/**
* @author test
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-09-21 16:25:01
*/
public interface UserService extends IService<User> {
    boolean updateProfileFields(Long userId, String name, String phone, String email, Integer gender, Date updateTime);

    boolean updatePassword(Long userId, String newPassword, Date updateTime);

    boolean updateSignature(Long userId, String signature, Date updateTime);
}
