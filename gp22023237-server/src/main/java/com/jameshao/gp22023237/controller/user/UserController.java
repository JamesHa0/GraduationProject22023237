package com.jameshao.gp22023237.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.User;
import com.jameshao.gp22023237.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JSONReturn jsonReturn;


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
    @RequestMapping("/list")
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
    @RequestMapping("/listById")
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

    // 根据roleId查询用户
    @RequestMapping("/listByRoleId")
    public String listByRole(Integer roleId){
        try{
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getRoleId, roleId);
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
