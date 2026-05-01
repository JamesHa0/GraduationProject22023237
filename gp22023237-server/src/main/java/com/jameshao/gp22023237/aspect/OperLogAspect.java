package com.jameshao.gp22023237.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jameshao.gp22023237.annotation.Log;
import com.jameshao.gp22023237.common.enums.BusinessType;
import com.jameshao.gp22023237.po.OperLog;
import com.jameshao.gp22023237.po.Role;
import com.jameshao.gp22023237.po.User;
import com.jameshao.gp22023237.service.OperLogService;
import com.jameshao.gp22023237.service.RoleService;
import com.jameshao.gp22023237.utils.CurrentUserUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * 操作日志AOP切面
 */
@Aspect
@Component
public class OperLogAspect {

    @Autowired
    private OperLogService operLogService;

    @Autowired
    private RoleService roleService;

    /**
     * 记录开始时间
     */
    private final ThreadLocal<Long> startTime = new ThreadLocal<>();

    /**
     * 处理请求前执行
     */
    @Before(value = "@annotation(com.jameshao.gp22023237.annotation.Log)")
    public void doBefore() {
        startTime.set(System.currentTimeMillis());
    }

    /**
     * 处理完请求后执行（正常返回）
     */
    @AfterReturning(pointcut = "@annotation(com.jameshao.gp22023237.annotation.Log)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
        handleLog(joinPoint, null, jsonResult);
    }

    /**
     * 拦截异常操作
     */
    @AfterThrowing(pointcut = "@annotation(com.jameshao.gp22023237.annotation.Log)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, null);
    }

    /**
     * 处理日志记录
     */
    private void handleLog(JoinPoint joinPoint, Exception e, Object jsonResult) {
        try {
            // 获取注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Log logAnnotation = method.getAnnotation(Log.class);

            OperLog operLog = new OperLog();
            operLog.setStatus(0); // 正常

            // 请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operLog.setOperUrl(request.getRequestURI());
                operLog.setOperIp(getIpAddr(request));
                operLog.setRequestMethod(request.getMethod());
            }

            // 异常处理
            if (e != null) {
                operLog.setStatus(1); // 异常
                operLog.setErrorMsg(e.getMessage());
            } else if (jsonResult != null) {
                // 检查业务返回结果，如果返回状态为"failed"则标记为异常
                try {
                    String resultStr = jsonResult instanceof String ? (String) jsonResult : JSON.toJSONString(jsonResult);
                    JSONObject jsonObj = JSON.parseObject(resultStr);
                    if ("failed".equals(jsonObj.getString("result"))) {
                        operLog.setStatus(1); // 异常
                        String error = jsonObj.getString("error");
                        if (error != null && !error.isEmpty()) {
                            operLog.setErrorMsg(error);
                        }
                    }
                } catch (Exception ex) {
                    // 非JSON格式的返回结果，忽略解析失败
                }
            }

            // 注解信息
            operLog.setTitle(logAnnotation.title());
            operLog.setBusinessType(logAnnotation.businessType().ordinal());

            // 方法信息
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = signature.getName();
            operLog.setMethod(className + "." + methodName + "()");

            // 请求参数
            if (logAnnotation.isSaveRequestData()) {
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    // 过滤掉HttpServletRequest等不可序列化对象
                    StringBuilder params = new StringBuilder();
                    for (Object arg : args) {
                        if (arg instanceof HttpServletRequest) {
                            continue;
                        }
                        try {
                            params.append(JSON.toJSONString(arg));
                        } catch (Exception ex) {
                            // 忽略序列化失败
                        }
                    }
                    String paramStr = params.toString();
                    if (paramStr.length() > 2000) {
                        paramStr = paramStr.substring(0, 2000);
                    }
                    operLog.setOperParam(paramStr);
                }
            }

            // 返回参数
            if (logAnnotation.isSaveResponseData() && jsonResult != null) {
                try {
                    String resultStr = JSON.toJSONString(jsonResult);
                    if (resultStr.length() > 2000) {
                        resultStr = resultStr.substring(0, 2000);
                    }
                    operLog.setJsonResult(resultStr);
                } catch (Exception ex) {
                    // 忽略序列化失败
                }
            }

            // 操作人信息
            User user = CurrentUserUtil.getCurrentUser();
            if (user != null) {
                operLog.setOperName(user.getName());
                // 获取角色名称
                if (user.getRoleId() != null) {
                    try {
                        Role role = roleService.getById(user.getRoleId());
                        if (role != null) {
                            operLog.setOperRole(role.getName());
                        }
                    } catch (Exception ex) {
                        // 忽略角色查询失败
                    }
                }
            } else {
                // 未登录状态（如登录操作），从方法参数中提取用户信息
                Object[] args = joinPoint.getArgs();
                if (args != null) {
                    for (Object arg : args) {
                        if (arg instanceof User argUser) {
                            if (argUser.getUsername() != null && !argUser.getUsername().isEmpty()) {
                                operLog.setOperName(argUser.getUsername());
                            }
                            if (argUser.getRoleId() != null) {
                                try {
                                    Role role = roleService.getById(argUser.getRoleId());
                                    if (role != null) {
                                        operLog.setOperRole(role.getName());
                                    }
                                } catch (Exception ex) {
                                    // 忽略角色查询失败
                                }
                            }
                            break;
                        }
                    }
                }
            }

            // 耗时
            Long start = startTime.get();
            if (start != null) {
                operLog.setCostTime(System.currentTimeMillis() - start);
            }

            operLog.setOperTime(new Date());

            // 异步保存日志
            operLogService.save(operLog);
        } catch (Exception ex) {
            // 记录日志本身不应影响业务流程
            ex.printStackTrace();
        } finally {
            startTime.remove();
        }
    }

    /**
     * 获取IP地址
     */
    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
