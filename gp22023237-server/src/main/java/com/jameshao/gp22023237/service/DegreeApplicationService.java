package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.po.DegreeApplication;

public interface DegreeApplicationService extends IService<DegreeApplication> {

    boolean submitApplication(DegreeApplication application);

    boolean committeeApprove(Long id, Integer status, String comment);

    boolean grantDegree(Long id, String certificateNo);
}
