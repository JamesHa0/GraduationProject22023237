package com.jameshao.gp22023237.controller.student;

import com.jameshao.gp22023237.common.JSONReturn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private JSONReturn jsonReturn;

}
