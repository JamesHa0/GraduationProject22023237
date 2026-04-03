package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.ThesisProgressMapper;
import com.jameshao.gp22023237.po.ThesisProgress;
import com.jameshao.gp22023237.service.ThesisProgressService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ThesisProgressServiceImpl extends ServiceImpl<ThesisProgressMapper, ThesisProgress>
        implements ThesisProgressService {

    @Override
    public boolean submitProgress(ThesisProgress progress) {
        progress.setSubmitTime(new Date());
        progress.setCreateTime(new Date());
        progress.setUpdateTime(new Date());
        progress.setMentorStatus(0);
        progress.setSecretaryStatus(0);
        progress.setDeanStatus(0);
        progress.setOverallStatus(1);
        return save(progress);
    }

    @Override
    public boolean mentorApprove(Long id, Integer status, String comment) {
        ThesisProgress progress = getById(id);
        if (progress == null) return false;

        progress.setMentorStatus(status);
        progress.setMentorComment(comment);
        progress.setMentorTime(new Date());
        progress.setUpdateTime(new Date());

        if (status == 1) {
            progress.setOverallStatus(2);
        } else {
            progress.setOverallStatus(5);
        }

        return updateById(progress);
    }

    @Override
    public boolean secretaryApprove(Long id, Integer status, String comment) {
        ThesisProgress progress = getById(id);
        if (progress == null) return false;

        progress.setSecretaryStatus(status);
        progress.setSecretaryComment(comment);
        progress.setSecretaryTime(new Date());
        progress.setUpdateTime(new Date());

        if (status == 1) {
            progress.setOverallStatus(3);
        } else {
            progress.setOverallStatus(5);
        }

        return updateById(progress);
    }

    @Override
    public boolean deanApprove(Long id, Integer status, String comment) {
        ThesisProgress progress = getById(id);
        if (progress == null) return false;

        progress.setDeanStatus(status);
        progress.setDeanComment(comment);
        progress.setDeanTime(new Date());
        progress.setUpdateTime(new Date());

        if (status == 1) {
            progress.setOverallStatus(4);
        } else {
            progress.setOverallStatus(5);
        }

        return updateById(progress);
    }
}
