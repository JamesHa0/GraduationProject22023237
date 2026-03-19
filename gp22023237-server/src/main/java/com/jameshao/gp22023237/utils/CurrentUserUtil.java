package com.jameshao.gp22023237.utils;

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
                token = request.getParameter("token");
            }

            if (token == null || token.isEmpty()) {
                return null;
            }

            // 从Redis中获取用户信息
            // 当前项目中，登录时用户信息以token为key存储在Redis中
            if (redisUtils != null && redisUtils.hasKey(token)) {
                return (User) redisUtils.get(token);
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
     * 角色ID: 3
     *
     * @return true: 是导师, false: 不是导师
     */
    public static boolean isMentor() {
        Integer roleId = getCurrentRoleId();
        return roleId != null && roleId == 3;
    }

    /**
     * 判断当前用户是否是教学秘书
     * 角色ID: 2
     *
     * @return true: 是教学秘书, false: 不是教学秘书
     */
    public static boolean isSecretary() {
        Integer roleId = getCurrentRoleId();
        return roleId != null && roleId == 2;
    }

    /**
     * 判断当前用户是否是分管院长
     * 角色ID: 5
     *
     * @return true: 是分管院长, false: 不是分管院长
     */
    public static boolean isDean() {
        Integer roleId = getCurrentRoleId();
        return roleId != null && roleId == 5;
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
