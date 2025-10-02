package com.jameshao.gp22023237.controller.auth;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
public class AuthController {

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

}
