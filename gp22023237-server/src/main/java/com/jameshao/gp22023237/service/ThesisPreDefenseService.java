package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.po.ThesisPreDefense;

public interface ThesisPreDefenseService extends IService<ThesisPreDefense> {

    boolean submitPreDefense(ThesisPreDefense preDefense);

    boolean mentorApprove(Long id, Integer status, String comment);

    boolean secretaryApprove(Long id, Integer status, String comment);

    boolean deanApprove(Long id, Integer status, String comment);

    boolean recordDefenseResult(Long id, Integer result, String comment, String qaRecord);
}
