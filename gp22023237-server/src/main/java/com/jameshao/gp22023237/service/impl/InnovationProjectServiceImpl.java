package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.InnovationProjectMapper;
import com.jameshao.gp22023237.po.InnovationProject;
import com.jameshao.gp22023237.service.InnovationProjectService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class InnovationProjectServiceImpl extends ServiceImpl<InnovationProjectMapper, InnovationProject>
        implements InnovationProjectService {

    @Override
    public boolean submitProject(InnovationProject project) {
        project.setSubmitTime(new Date());
        project.setCreateTime(new Date());
        project.setUpdateTime(new Date());
        project.setMentorStatus(0);
        project.setSecretaryStatus(0);
        project.setDeanStatus(0);
        return save(project);
    }

    @Override
    public boolean mentorApprove(Long id, Integer status, String comment) {
        InnovationProject project = getById(id);
        if (project == null) return false;
        project.setMentorStatus(status);
        project.setMentorComment(comment);
        project.setUpdateTime(new Date());
        return updateById(project);
    }

    @Override
    public boolean secretaryApprove(Long id, Integer status, String comment) {
        InnovationProject project = getById(id);
        if (project == null) return false;
        project.setSecretaryStatus(status);
        project.setSecretaryComment(comment);
        project.setUpdateTime(new Date());
        return updateById(project);
    }

    @Override
    public boolean deanApprove(Long id, Integer status, String comment) {
        InnovationProject project = getById(id);
        if (project == null) return false;
        project.setDeanStatus(status);
        project.setDeanComment(comment);
        project.setUpdateTime(new Date());
        return updateById(project);
    }
}
