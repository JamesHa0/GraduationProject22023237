package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.po.DegreeApplication;

public interface DegreeApplicationService extends IService<DegreeApplication> {

    boolean submitApplication(DegreeApplication application);

    boolean recordDefenseResult(Long id, Integer result, Double score, String comment, String qaRecord);

    boolean committeeApprove(Long id, Integer status, String comment);

    boolean grantDegree(Long id, String certificateNo);
}
