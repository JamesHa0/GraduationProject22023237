package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.po.ThesisMidterm;

public interface ThesisMidtermService extends IService<ThesisMidterm> {

    boolean submitMidterm(ThesisMidterm midterm);

    boolean mentorApprove(Long id, Integer status, String comment);

    boolean secretaryApprove(Long id, Integer status, String comment);

    boolean deanApprove(Long id, Integer status, String comment);
}
