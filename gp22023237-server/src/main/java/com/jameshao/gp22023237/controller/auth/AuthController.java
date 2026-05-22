package com.jameshao.gp22023237.controller.auth;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jameshao.gp22023237.annotation.Log;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.common.enums.BusinessType;
import com.jameshao.gp22023237.utils.FLAGS;
import com.jameshao.gp22023237.po.User;
import com.jameshao.gp22023237.service.UserService;
import com.jameshao.gp22023237.utils.RedisUtils;
import com.jameshao.gp22023237.utils.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private JSONReturn jsonReturn;
    @Autowired
    private RedisUtils redisUtils;

    //登录验证
    @Log(title = "用户登录", businessType = BusinessType.LOGIN)
    @RequestMapping("/login")
    public String login(@RequestBody User user){
        System.out.println(user);
        try {
            // 验证码校验
            String captchaCode = user.getCode();
            String captchaUuid = user.getUuid();
            if (captchaUuid != null && !captchaUuid.isEmpty()) {
                String redisKey = "captcha:" + captchaUuid;
                Object storedCode = redisUtils.get(redisKey);
                if (storedCode == null) {
                    return jsonReturn.returnFailed(FLAGS.CAPTCHA_EXPIRED);
                }
                if (!storedCode.toString().equalsIgnoreCase(captchaCode)) {
                    return jsonReturn.returnFailed(FLAGS.CAPTCHA_ERROR);
                }
                // 验证通过，删除已使用的验证码
                redisUtils.del(redisKey);
            }

            // 根据用户名查询用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, user.getUsername());
            User dbUser = userService.getOne(queryWrapper);

            if (dbUser != null && com.jameshao.gp22023237.service.impl.UserServiceImpl.checkPassword(user.getPassword(), dbUser.getPassword())){
                // 登录成功，如果是明文旧密码，自动升级为哈希存储
                if (user.getPassword().equals(dbUser.getPassword())) {
                    userService.updatePassword(dbUser.getId(), user.getPassword(), new java.util.Date());
                    dbUser = userService.getById(dbUser.getId());
                }
                //生成Token
                String token = TokenUtil.createToken();
                redisUtils.set(token, dbUser, 60*60*3);
                redisUtils.set(dbUser.getUsername(), dbUser, 60*60*3);

                dbUser.setToken(token);
                return jsonReturn.returnSuccess(dbUser);
            } else {//登陆失败
                return jsonReturn.returnFailed(FLAGS.LOGIN_FAIL);
            }
        } catch(Exception e){//程序错误，直接返回错误信息
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

}
