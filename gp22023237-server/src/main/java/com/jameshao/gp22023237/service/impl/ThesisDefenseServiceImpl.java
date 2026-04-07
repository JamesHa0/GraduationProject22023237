package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.ThesisDefenseMapper;
import com.jameshao.gp22023237.po.ThesisDefense;
import com.jameshao.gp22023237.service.ThesisDefenseService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ThesisDefenseServiceImpl extends ServiceImpl<ThesisDefenseMapper, ThesisDefense>
        implements ThesisDefenseService {

    @Override
    public boolean submitDefense(ThesisDefense defense) {
        defense.setCreateTime(new Date());
        defense.setUpdateTime(new Date());
        defense.setTutorApproval(0);
        defense.setDeanApproval(0);
        defense.setStatus(1);
        return save(defense);
    }

    @Override
    public boolean tutorApprove(Long id, Integer status) {
        ThesisDefense defense = getById(id);
        if (defense == null) return false;

        defense.setTutorApproval(status);
        defense.setTutorApprovalTime(new Date());
        defense.setUpdateTime(new Date());

        if (status == 1) {
            defense.setStatus(1);
        }

        return updateById(defense);
    }

    @Override
    public boolean deanApprove(Long id, Integer status) {
        ThesisDefense defense = getById(id);
        if (defense == null) return false;

        defense.setDeanApproval(status);
        defense.setDeanApprovalTime(new Date());
        defense.setUpdateTime(new Date());

        if (status == 1) {
            defense.setStatus(1);
        }

        return updateById(defense);
    }

    @Override
    public boolean recordDefenseResult(Long id, Integer result, Double score, String comment, String qaRecord) {
        ThesisDefense defense = getById(id);
        if (defense == null) return false;

        defense.setDefenseResult(result);
        defense.setDefenseScore(score);
        defense.setDefenseComments(comment);
        defense.setQaRecord(qaRecord);
        defense.setUpdateTime(new Date());
        defense.setStatus(2);

        return updateById(defense);
    }
}
