package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.DegreeApplicationMapper;
import com.jameshao.gp22023237.po.DegreeApplication;
import com.jameshao.gp22023237.service.DegreeApplicationService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DegreeApplicationServiceImpl extends ServiceImpl<DegreeApplicationMapper, DegreeApplication>
        implements DegreeApplicationService {

    @Override
    public boolean submitApplication(DegreeApplication application) {
        application.setSubmitTime(new Date());
        application.setCreateTime(new Date());
        application.setUpdateTime(new Date());
        application.setCommitteeStatus(0);
        application.setDegreeGranted(0);
        return save(application);
    }

    @Override
    public boolean recordDefenseResult(Long id, Integer result, Double score, String comment, String qaRecord) {
        DegreeApplication application = getById(id);
        if (application == null) return false;

        application.setDefenseResult(result);
        application.setDefenseScore(score);
        application.setDefenseCommitteeComment(comment);
        application.setQaRecord(qaRecord);
        application.setUpdateTime(new Date());

        return updateById(application);
    }

    @Override
    public boolean committeeApprove(Long id, Integer status, String comment) {
        DegreeApplication application = getById(id);
        if (application == null) return false;

        application.setCommitteeStatus(status);
        application.setCommitteeComment(comment);
        application.setUpdateTime(new Date());

        return updateById(application);
    }

    @Override
    public boolean grantDegree(Long id, String certificateNo) {
        DegreeApplication application = getById(id);
        if (application == null) return false;

        application.setDegreeGranted(1);
        application.setCertificateNo(certificateNo);
        application.setDegreeGrantDate(new Date());
        application.setUpdateTime(new Date());

        return updateById(application);
    }
}
