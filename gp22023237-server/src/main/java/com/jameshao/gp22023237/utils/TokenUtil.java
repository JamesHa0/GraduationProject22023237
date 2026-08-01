package com.jameshao.gp22023237.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TokenUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    //生成Token
    public String createToken(){
        return JWT.create()
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 3))//设置过期时间为3小时
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    //验证Token
    public void authToken(String token){
        // 验证Token
        JWTVerifier jwtverifier = JWT.require(Algorithm.HMAC256(jwtSecret)).build();
        jwtverifier.verify(token);
    }
}
