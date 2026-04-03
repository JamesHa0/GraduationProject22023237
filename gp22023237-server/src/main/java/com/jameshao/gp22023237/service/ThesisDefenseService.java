package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.po.ThesisDefense;

public interface ThesisDefenseService extends IService<ThesisDefense> {

    boolean submitDefense(ThesisDefense defense);

    boolean tutorApprove(Long id, Integer status);

    boolean deanApprove(Long id, Integer status);

    boolean recordDefenseResult(Long id, Integer result, Double score, String comment, String qaRecord);
}
