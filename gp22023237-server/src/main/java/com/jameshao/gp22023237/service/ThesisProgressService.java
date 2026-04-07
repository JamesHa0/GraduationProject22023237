package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.po.ThesisProgress;

public interface ThesisProgressService extends IService<ThesisProgress> {

    boolean submitProgress(ThesisProgress progress);

    boolean mentorApprove(Long id, Integer status, String comment);

    boolean secretaryApprove(Long id, Integer status, String comment);

    boolean deanApprove(Long id, Integer status, String comment);
}
