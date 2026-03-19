package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.ThesisProposalMapper;
import com.jameshao.gp22023237.po.ThesisProposal;
import com.jameshao.gp22023237.service.ThesisProposalService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ThesisProposalServiceImpl extends ServiceImpl<ThesisProposalMapper, ThesisProposal>
        implements ThesisProposalService {

    @Override
    public boolean submitProposal(ThesisProposal proposal) {
        proposal.setSubmitTime(new Date());
        proposal.setCreateTime(new Date());
        proposal.setUpdateTime(new Date());
        proposal.setMentorStatus(0);
        proposal.setSecretaryStatus(0);
        proposal.setDeanStatus(0);
        proposal.setOverallStatus(1);
        return save(proposal);
    }

    @Override
    public boolean mentorApprove(Long id, Integer status, String comment) {
        ThesisProposal proposal = getById(id);
        if (proposal == null) return false;

        proposal.setMentorStatus(status);
        proposal.setMentorComment(comment);
        proposal.setUpdateTime(new Date());

        if (status == 1) {
            proposal.setOverallStatus(2);
        } else {
            proposal.setOverallStatus(5);
        }

        return updateById(proposal);
    }

    @Override
    public boolean secretaryApprove(Long id, Integer status, String comment) {
        ThesisProposal proposal = getById(id);
        if (proposal == null) return false;

        proposal.setSecretaryStatus(status);
        proposal.setSecretaryComment(comment);
        proposal.setUpdateTime(new Date());

        if (status == 1) {
            proposal.setOverallStatus(3);
        } else {
            proposal.setOverallStatus(5);
        }

        return updateById(proposal);
    }

    @Override
    public boolean deanApprove(Long id, Integer status, String comment) {
        ThesisProposal proposal = getById(id);
        if (proposal == null) return false;

        proposal.setDeanStatus(status);
        proposal.setDeanComment(comment);
        proposal.setUpdateTime(new Date());

        if (status == 1) {
            proposal.setOverallStatus(4);
        } else {
            proposal.setOverallStatus(5);
        }

        return updateById(proposal);
    }
}
