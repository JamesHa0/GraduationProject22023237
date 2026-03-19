package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.ThesisPreDefenseMapper;
import com.jameshao.gp22023237.po.ThesisPreDefense;
import com.jameshao.gp22023237.service.ThesisPreDefenseService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ThesisPreDefenseServiceImpl extends ServiceImpl<ThesisPreDefenseMapper, ThesisPreDefense>
        implements ThesisPreDefenseService {

    @Override
    public boolean submitPreDefense(ThesisPreDefense preDefense) {
        preDefense.setSubmitTime(new Date());
        preDefense.setCreateTime(new Date());
        preDefense.setUpdateTime(new Date());
        preDefense.setMentorStatus(0);
        preDefense.setSecretaryStatus(0);
        preDefense.setDeanStatus(0);
        preDefense.setOverallStatus(1);
        return save(preDefense);
    }

    @Override
    public boolean mentorApprove(Long id, Integer status, String comment) {
        ThesisPreDefense preDefense = getById(id);
        if (preDefense == null) return false;

        preDefense.setMentorStatus(status);
        preDefense.setMentorComment(comment);
        preDefense.setUpdateTime(new Date());

        if (status == 1) {
            preDefense.setOverallStatus(2);
        } else {
            preDefense.setOverallStatus(5);
        }

        return updateById(preDefense);
    }

    @Override
    public boolean secretaryApprove(Long id, Integer status, String comment) {
        ThesisPreDefense preDefense = getById(id);
        if (preDefense == null) return false;

        preDefense.setSecretaryStatus(status);
        preDefense.setSecretaryComment(comment);
        preDefense.setUpdateTime(new Date());

        if (status == 1) {
            preDefense.setOverallStatus(3);
        } else {
            preDefense.setOverallStatus(5);
        }

        return updateById(preDefense);
    }

    @Override
    public boolean deanApprove(Long id, Integer status, String comment) {
        ThesisPreDefense preDefense = getById(id);
        if (preDefense == null) return false;

        preDefense.setDeanStatus(status);
        preDefense.setDeanComment(comment);
        preDefense.setUpdateTime(new Date());

        if (status == 1) {
            preDefense.setOverallStatus(3);
        } else {
            preDefense.setOverallStatus(5);
        }

        return updateById(preDefense);
    }

    @Override
    public boolean recordDefenseResult(Long id, Integer result, String comment, String qaRecord) {
        ThesisPreDefense preDefense = getById(id);
        if (preDefense == null) return false;

        preDefense.setDefenseResult(result);
        preDefense.setCommitteeComment(comment);
        preDefense.setQaRecord(qaRecord);
        preDefense.setUpdateTime(new Date());

        return updateById(preDefense);
    }
}
