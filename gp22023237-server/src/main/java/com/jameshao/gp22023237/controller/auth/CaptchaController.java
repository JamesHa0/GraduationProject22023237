package com.jameshao.gp22023237.controller.auth;

import com.google.code.kaptcha.Producer;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class CaptchaController {

    @Autowired
    private Producer producer;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private JSONReturn jsonReturn;

    @GetMapping("/captchaImage")
    public String getCaptchaImage() throws IOException {
        // 生成验证码文本
        String capText = producer.createText();
        // 生成验证码图片
        BufferedImage image = producer.createImage(capText);

        // 图片转 Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "gif", baos);
        String base64Img = Base64.getEncoder().encodeToString(baos.toByteArray());
        baos.close();

        // 生成 UUID 并存入 Redis（2分钟有效期）
        String uuid = UUID.randomUUID().toString();
        redisUtils.set("captcha:" + uuid, capText, 120);

        // 构造返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("captchaEnabled", true);
        result.put("img", base64Img);
        result.put("uuid", uuid);

        return jsonReturn.returnSuccess(result);
    }
}
