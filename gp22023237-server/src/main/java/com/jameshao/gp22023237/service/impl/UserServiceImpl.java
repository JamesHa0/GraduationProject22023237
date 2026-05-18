package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.User;
import com.jameshao.gp22023237.service.UserService;
import com.jameshao.gp22023237.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

/**
* @author test
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-09-21 16:25:01
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    private static final String PASSWORD_SALT = "gp22023237_salt";

    /**
     * 对密码进行SHA-256哈希（加salt），避免明文存储
     */
    public static String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((rawPassword + PASSWORD_SALT).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码哈希失败", e);
        }
    }

    /**
     * 校验密码：支持哈希密码比对和明文向后兼容
     */
    public static boolean checkPassword(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) return false;
        // 先尝试哈希比对
        String hashed = hashPassword(rawPassword);
        if (hashed.equals(storedPassword)) return true;
        // 向后兼容：如果是明文存储的旧密码，也能通过
        return rawPassword.equals(storedPassword);
    }

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
        String hashedPassword = hashPassword(newPassword);
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                .set(User::getPassword, hashedPassword)
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




