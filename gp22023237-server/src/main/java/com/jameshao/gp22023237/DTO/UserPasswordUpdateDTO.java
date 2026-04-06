package com.jameshao.gp22023237.DTO;

import lombok.Data;

@Data
public class UserPasswordUpdateDTO {
    private String oldPassword;
    private String newPassword;
}
