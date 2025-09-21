package com.jameshao.gp22023237.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.utils.FLAGS;
import com.jameshao.gp22023237.po.User;
import com.jameshao.gp22023237.service.UserService;
import com.jameshao.gp22023237.utils.RedisUtils;
import com.jameshao.gp22023237.utils.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JSONReturn jsonReturn;
    @Autowired
    private RedisUtils redisUtils;

    //登录验证
    @RequestMapping("/login")
    public String login(@RequestBody User user){
        System.out.println(user);
        try {
            //查询信息
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, user.getUsername())
                    .eq(User::getPassword, user.getPassword()).select(User::getId, User::getName, User::getUsername, User::getRoleId);
            List<User> users = userService.list(queryWrapper);

            if (users != null && !users.isEmpty()){//登录成功
                //生成Token
                String token = TokenUtil.createToken();
                redisUtils.set(token, "token token", 60*60*3);
                User loginUser = users.get(0);
                redisUtils.set(loginUser.getUsername(), loginUser, 60*60*3);

                loginUser.setToken(token);
                return jsonReturn.returnSuccess(loginUser);
            } else {//登陆失败
                return jsonReturn.returnFailed(FLAGS.LOGIN_FAIL);
            }
        } catch(Exception e){//程序错误，直接返回错误信息
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }


    //获取用户信息
    @RequestMapping("/getInfo")
    public String getInfo(String id){
        try{
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getId, id)
                    .select(User::getName, User::getUsername, User::getRoleId);
            List<User> users = userService.list(queryWrapper);
            return jsonReturn.returnSuccess(users);
        }catch(Exception e){
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // 查询所有用户
    @RequestMapping("/user/list")
    public String list(String nickname){
        try{
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(!ObjectUtils.isEmpty(nickname),User::getName, nickname)
                    .eq(User::getStatus, 1);
            List<User> list = userService.list(queryWrapper);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // 根据id查询用户
    @RequestMapping("/user/listById")
    public String listById(Integer id){
        try{
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getId, id);
            List<User> list = userService.list(queryWrapper);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    //添加用户
    @RequestMapping("adduser")
    public String addOne(User user){
        try{
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            //ObjectUtils.isEmpty(conds.getUsername())
            queryWrapper.eq(User::getUsername, user.getUsername());
            List<User> users = userService.list(queryWrapper);

            if (users != null && users.size() > 0){
                return jsonReturn.returnFailed("添加失败：登录名重复！");
            }

            userService.save(user);
            return jsonReturn.returnSuccess();
        }catch(Exception e){
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    //更新用户
    @RequestMapping("modifyuser")
    public String modifyOne(User user){
        try{
            if (user == null || user.getId() == null){
                return jsonReturn.returnFailed("添加失败：未能获取用户！");
            }

            userService.updateById(user);
            return jsonReturn.returnSuccess();
        }catch(Exception e){
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }


    //删除用户
    @RequestMapping("removeuser")
    public String removeOne(Integer id){
        try{
            if (id == null){
                return jsonReturn.returnFailed("添加失败：未能获取用户信息！");
            }

            userService.removeById(id);
            return jsonReturn.returnSuccess();
        }catch(Exception e){
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }


    //退出登录
    @RequestMapping("/logout")
    public String logout() throws JsonProcessingException {

        return jsonReturn.returnSuccess();
    }
}
