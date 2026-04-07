package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.po.InnovationProject;

public interface InnovationProjectService extends IService<InnovationProject> {

    boolean submitProject(InnovationProject project);

    boolean mentorApprove(Long id, Integer status, String comment);

    boolean secretaryApprove(Long id, Integer status, String comment);

    boolean deanApprove(Long id, Integer status, String comment);
}
