package com.jameshao.gp22023237.controller.selection;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.User;
import com.jameshao.gp22023237.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/selection")
public class SelectionController {

    @Autowired
    private UserService userService;
    @Autowired
    private JSONReturn jsonReturn;

    // 学生查询可选导师
    @RequestMapping("/listMentor")
    public String listMentor(String name){
        try{
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(!ObjectUtils.isEmpty(name),User::getName, name)
                    .eq(User::getStatus, 1);
            List<User> list = userService.list(queryWrapper);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
