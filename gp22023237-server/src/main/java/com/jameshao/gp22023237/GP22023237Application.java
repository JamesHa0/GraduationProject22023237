package com.jameshao.gp22023237;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.jameshao.gp22023237.mapper")
public class GP22023237Application {

    public static void main(String[] args) {
        SpringApplication.run(GP22023237Application.class, args);
    }

}
