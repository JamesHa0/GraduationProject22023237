package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.User;
import com.jameshao.gp22023237.service.UserService;
import com.jameshao.gp22023237.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
* @author test
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-09-21 16:25:01
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Override
    public boolean updateProfileFields(Long userId, String name, String phone, String email, Integer gender, Date updateTime) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                .set(User::getName, name)
                .set(User::getPhone, phone)
                .set(User::getEmail, email)
                .set(User::getGender, gender)
                .set(User::getUpdateTime, updateTime);
        return this.update(updateWrapper);
    }

    @Override
    public boolean updatePassword(Long userId, String newPassword, Date updateTime) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                .set(User::getPassword, newPassword)
                .set(User::getUpdateTime, updateTime);
        return this.update(updateWrapper);
    }

    @Override
    public boolean updateSignature(Long userId, String signature, Date updateTime) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                .set(User::getSignature, signature)
                .set(User::getUpdateTime, updateTime);
        return this.update(updateWrapper);
    }

    @Override
    public boolean updateAvatar(Long userId, String avatar, Date updateTime) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                .set(User::getAvatar, avatar)
                .set(User::getUpdateTime, updateTime);
        return this.update(updateWrapper);
    }
}




