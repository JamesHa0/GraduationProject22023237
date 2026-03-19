package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.ThesisMidtermMapper;
import com.jameshao.gp22023237.po.ThesisMidterm;
import com.jameshao.gp22023237.service.ThesisMidtermService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ThesisMidtermServiceImpl extends ServiceImpl<ThesisMidtermMapper, ThesisMidterm>
        implements ThesisMidtermService {

    @Override
    public boolean submitMidterm(ThesisMidterm midterm) {
        midterm.setSubmitTime(new Date());
        midterm.setCreateTime(new Date());
        midterm.setUpdateTime(new Date());
        midterm.setMentorStatus(0);
        midterm.setSecretaryStatus(0);
        midterm.setDeanStatus(0);
        midterm.setOverallStatus(1);
        return save(midterm);
    }

    @Override
    public boolean mentorApprove(Long id, Integer status, String comment) {
        ThesisMidterm midterm = getById(id);
        if (midterm == null) return false;

        midterm.setMentorStatus(status);
        midterm.setMentorComment(comment);
        midterm.setUpdateTime(new Date());

        if (status == 1) {
            midterm.setOverallStatus(2);
        } else {
            midterm.setOverallStatus(5);
        }

        return updateById(midterm);
    }

    @Override
    public boolean secretaryApprove(Long id, Integer status, String comment) {
        ThesisMidterm midterm = getById(id);
        if (midterm == null) return false;

        midterm.setSecretaryStatus(status);
        midterm.setSecretaryComment(comment);
        midterm.setUpdateTime(new Date());

        if (status == 1) {
            midterm.setOverallStatus(3);
        } else {
            midterm.setOverallStatus(5);
        }

        return updateById(midterm);
    }

    @Override
    public boolean deanApprove(Long id, Integer status, String comment) {
        ThesisMidterm midterm = getById(id);
        if (midterm == null) return false;

        midterm.setDeanStatus(status);
        midterm.setDeanComment(comment);
        midterm.setUpdateTime(new Date());

        if (status == 1) {
            midterm.setOverallStatus(4);
        } else {
            midterm.setOverallStatus(5);
        }

        return updateById(midterm);
    }
}
