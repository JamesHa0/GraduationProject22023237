package com.jameshao.gp22023237.DTO;

import lombok.Data;

import java.util.Date;

@Data
public class UserProfileDTO {
    private Long userId;
    private String username;
    private String name;
    private String phone;
    private String email;
    private Integer gender;
    private Date createTime;
    private String avatar;
    private String signature;
}
