package com.jameshao.gp22023237.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.User;
import com.jameshao.gp22023237.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/user")
public class SystemUserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JSONReturn jsonReturn;

    @RequestMapping("/list")
    public String list(Integer pageNum, Integer pageSize, String userName, String phonenumber, String status) {
        try {
            Page<User> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(!ObjectUtils.isEmpty(userName), User::getName, userName)
                    .like(!ObjectUtils.isEmpty(phonenumber), User::getPhone, phonenumber)
                    .eq(!ObjectUtils.isEmpty(status), User::getStatus, status);
            Page<User> result = userService.page(page, queryWrapper);
            Map<String, Object> data = new HashMap<>();
            data.put("rows", result.getRecords());
            data.put("total", result.getTotal());
            return jsonReturn.returnSuccess(data);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @RequestMapping("/{userId}")
    public String getInfo(@PathVariable Long userId) {
        try {
            User user = userService.getById(userId);
            return jsonReturn.returnSuccess(user);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping
    public String add(@RequestBody User user) {
        try {
            LambdaQueryWrapper<User> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(User::getUsername, user.getUsername());
            User existUser = userService.getOne(checkWrapper);
            if (existUser != null) {
                return jsonReturn.returnFailed("用户名已存在");
            }
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            if (user.getStatus() == null) {
                user.setStatus(1);
            }
            userService.save(user);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PutMapping
    public String edit(@RequestBody User user) {
        try {
            user.setUpdateTime(new Date());
            userService.updateById(user);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @DeleteMapping("/{userIds}")
    public String remove(@PathVariable Long[] userIds) {
        try {
            for (Long userId : userIds) {
                userService.removeById(userId);
            }
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PutMapping("/changeStatus")
    public String changeStatus(@RequestBody User user) {
        try {
            user.setUpdateTime(new Date());
            userService.updateById(user);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PutMapping("/resetPwd")
    public String resetPwd(@RequestBody User user) {
        try {
            user.setUpdateTime(new Date());
            userService.updateById(user);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @RequestMapping("/listAll")
    public String listAll() {
        try {
            List<User> list = userService.list();
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
