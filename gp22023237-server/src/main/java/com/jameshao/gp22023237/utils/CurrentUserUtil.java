package com.jameshao.gp22023237.utils;

import com.alibaba.fastjson.JSONObject;
import com.jameshao.gp22023237.po.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 当前用户信息获取工具类
 * 用于从请求中获取当前登录用户的信息
 *
 * @author System
 */
@Component
public class CurrentUserUtil {

    private static RedisUtils redisUtils;

    @Autowired
    public void setRedisUtils(RedisUtils redisUtils) {
        CurrentUserUtil.redisUtils = redisUtils;
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    public static User getCurrentUser() {
        try {
            HttpServletRequest request = getRequest();
            if (request == null) {
                return null;
            }

            // 从请求头获取token
            String token = request.getHeader("token");
            if (token == null || token.isEmpty()) {
                // 如果请求头中没有，尝试从参数中获取
                token = request.getHeader("Token");
            }

            if (token == null || token.isEmpty()) {
                return null;
            }

            if (redisUtils != null && redisUtils.hasKey(token)) {
                Object obj = redisUtils.get(token);
                if (obj instanceof User) {
                    return (User) obj;
                } else if (obj instanceof JSONObject) {
                    // 兼容 fastjson 序列化的情况
                    JSONObject jsonObj = (JSONObject) obj;
                    return jsonObj.toJavaObject(User.class);
                } else {
                    // 兼容旧数据：如果Redis中存的不是User对象，删除旧key
                    redisUtils.del(token);
                }
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名
     */
    public static String getCurrentUsername() {
        User user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取当前登录用户角色ID
     *
     * @return 角色ID
     */
    public static Integer getCurrentRoleId() {
        User user = getCurrentUser();
        return user != null ? user.getRoleId() : null;
    }

    /**
     * 判断当前用户是否是导师
     * 角色ID: 7
     *
     * @return true: 是导师, false: 不是导师
     */
    public static boolean isMentor() {
        Integer roleId = getCurrentRoleId();
        return roleId != null && roleId == 7;
    }

    /**
     * 判断当前用户是否是教学秘书
     * 角色ID: 5
     *
     * @return true: 是教学秘书, false: 不是教学秘书
     */
    public static boolean isSecretary() {
        Integer roleId = getCurrentRoleId();
        return roleId != null && roleId == 5;
    }

    /**
     * 判断当前用户是否是轮次管理员（超级管理员、综合管理员或教学秘书）
     * 角色ID: 1=超级管理员, 4=综合管理员, 5=教学秘书
     *
     * @return true: 是轮次管理员, false: 不是轮次管理员
     */
    public static boolean isRoundAdmin() {
        Integer roleId = getCurrentRoleId();
        return roleId != null && (roleId == 1 || roleId == 4 || roleId == 5);
    }

    /**
     * 判断当前用户是否是分管院长
     * 角色ID: 2
     *
     * @return true: 是分管院长, false: 不是分管院长
     */
    public static boolean isDean() {
        Integer roleId = getCurrentRoleId();
        return roleId != null && roleId == 2;
    }

    /**
     * 判断当前用户是否是学生
     * 角色ID: 6
     *
     * @return true: 是学生, false: 不是学生
     */
    public static boolean isStudent() {
        Integer roleId = getCurrentRoleId();
        return roleId != null && roleId == 6;
    }

    /**
     * 获取Request对象
     *
     * @return HttpServletRequest
     */
    private static HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return requestAttributes != null ? requestAttributes.getRequest() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
