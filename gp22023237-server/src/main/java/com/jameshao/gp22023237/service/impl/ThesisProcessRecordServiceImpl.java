package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.common.enums.ApprovalStatus;
import com.jameshao.gp22023237.common.enums.ProcessStatus;
import com.jameshao.gp22023237.common.enums.ProcessType;
import com.jameshao.gp22023237.common.enums.ReviewResult;
import com.jameshao.gp22023237.mapper.ThesisProcessRecordMapper;
import com.jameshao.gp22023237.po.ThesisMain;
import com.jameshao.gp22023237.po.ThesisProcessRecord;
import com.jameshao.gp22023237.service.ThesisMainService;
import com.jameshao.gp22023237.service.ThesisProcessRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class ThesisProcessRecordServiceImpl extends ServiceImpl<ThesisProcessRecordMapper, ThesisProcessRecord>
        implements ThesisProcessRecordService {

    @Autowired
    private ThesisMainService thesisMainService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitProcess(ThesisProcessRecord record) {
        // 确保thesisId对应的论文主记录存在
        if (record.getThesisId() == null && record.getProcessType() == null) {
            throw new IllegalArgumentException("thesisId和processType不能为空");
        }

        // 检查processType是否有效
        ProcessType processType = ProcessType.fromCode(record.getProcessType());
        if (processType == null) {
            throw new IllegalArgumentException("无效的流程类型: " + record.getProcessType());
        }

        // 自动计算version：同一论文同一环节的最大version + 1
        if (record.getVersion() == null) {
            LambdaQueryWrapper<ThesisProcessRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ThesisProcessRecord::getThesisId, record.getThesisId());
            wrapper.eq(ThesisProcessRecord::getProcessType, record.getProcessType());
            wrapper.orderByDesc(ThesisProcessRecord::getVersion);
            ThesisProcessRecord latest = getOne(wrapper, false);
            record.setVersion(latest == null ? 1 : latest.getVersion() + 1);
        }

        // 初始化审批状态
        record.setSupervisorStatus(ApprovalStatus.UNAPPROVED.getCode());
        record.setSecretaryStatus(ApprovalStatus.UNAPPROVED.getCode());
        record.setDeanStatus(ApprovalStatus.UNAPPROVED.getCode());
        record.setProcessStatus(ProcessStatus.APPROVING.getCode());
        record.setReviewResult(ReviewResult.NOT_STARTED.getCode());
        record.setSubmitTime(new Date());

        return save(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean supervisorApprove(Long id, Integer status, String comment, Long approverId) {
        ThesisProcessRecord record = getById(id);
        if (record == null) {
            throw new IllegalArgumentException("未找到记录");
        }

        // 校验当前状态是否允许导师审批（审批中状态）
        ProcessStatus currentStatus = ProcessStatus.fromCode(record.getProcessStatus());
        if (currentStatus != ProcessStatus.APPROVING) {
            throw new IllegalStateException("当前状态不允许导师审批，当前状态: " + currentStatus.getDesc());
        }

        // 校验导师是否已审批
        if (!ApprovalStatus.UNAPPROVED.getCode().equals(record.getSupervisorStatus())) {
            throw new IllegalStateException("该记录已由导师审批过");
        }

        record.setSupervisorStatus(status);
        record.setSupervisorComment(comment);
        record.setSupervisorTime(new Date());
        record.setSupervisorApproverId(approverId);

        if (ApprovalStatus.APPROVED.getCode().equals(status)) {
            // 导师通过 → 进入秘书审批（仍在审批中状态）
            // processStatus保持APPROVING，等待秘书审批
        } else if (ApprovalStatus.REJECTED.getCode().equals(status)) {
            record.setProcessStatus(ProcessStatus.REJECTED.getCode());
        }

        return updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean secretaryApprove(Long id, Integer status, String comment, Long approverId) {
        ThesisProcessRecord record = getById(id);
        if (record == null) {
            throw new IllegalArgumentException("未找到记录");
        }

        // 校验当前状态
        ProcessStatus currentStatus = ProcessStatus.fromCode(record.getProcessStatus());
        if (currentStatus != ProcessStatus.APPROVING) {
            throw new IllegalStateException("当前状态不允许秘书审批，当前状态: " + currentStatus.getDesc());
        }

        // 校验导师是否已通过
        if (!ApprovalStatus.APPROVED.getCode().equals(record.getSupervisorStatus())) {
            throw new IllegalStateException("导师尚未通过，不能进行秘书审批");
        }

        // 校验秘书是否已审批
        if (!ApprovalStatus.UNAPPROVED.getCode().equals(record.getSecretaryStatus())) {
            throw new IllegalStateException("该记录已由秘书审批过");
        }

        record.setSecretaryStatus(status);
        record.setSecretaryComment(comment);
        record.setSecretaryTime(new Date());
        record.setSecretaryApproverId(approverId);

        if (ApprovalStatus.APPROVED.getCode().equals(status)) {
            // 秘书通过 → 进入院长审批（仍在审批中状态）
        } else if (ApprovalStatus.REJECTED.getCode().equals(status)) {
            record.setProcessStatus(ProcessStatus.REJECTED.getCode());
        }

        return updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deanApprove(Long id, Integer status, String comment, Long approverId) {
        ThesisProcessRecord record = getById(id);
        if (record == null) {
            throw new IllegalArgumentException("未找到记录");
        }

        // 校验当前状态
        ProcessStatus currentStatus = ProcessStatus.fromCode(record.getProcessStatus());
        if (currentStatus != ProcessStatus.APPROVING) {
            throw new IllegalStateException("当前状态不允许院长审批，当前状态: " + currentStatus.getDesc());
        }

        // 校验导师和秘书是否已通过
        if (!ApprovalStatus.APPROVED.getCode().equals(record.getSupervisorStatus())) {
            throw new IllegalStateException("导师尚未通过，不能进行院长审批");
        }
        if (!ApprovalStatus.APPROVED.getCode().equals(record.getSecretaryStatus())) {
            throw new IllegalStateException("秘书尚未通过，不能进行院长审批");
        }

        // 校验院长是否已审批
        if (!ApprovalStatus.UNAPPROVED.getCode().equals(record.getDeanStatus())) {
            throw new IllegalStateException("该记录已由院长审批过");
        }

        record.setDeanStatus(status);
        record.setDeanComment(comment);
        record.setDeanTime(new Date());
        record.setDeanApproverId(approverId);

        if (ApprovalStatus.APPROVED.getCode().equals(status)) {
            // 院长通过后，对于开题/中期/预答辩：直接标记为已通过
            // 对于外审/答辩：标记为评审中
            ProcessType processType = ProcessType.fromCode(record.getProcessType());
            if (processType == ProcessType.EXTERNAL_REVIEW || processType == ProcessType.DEFENSE || processType == ProcessType.SECOND_DEFENSE) {
                record.setProcessStatus(ProcessStatus.REVIEWING.getCode());
            } else {
                record.setProcessStatus(ProcessStatus.PASSED.getCode());
            }
        } else if (ApprovalStatus.REJECTED.getCode().equals(status)) {
            record.setProcessStatus(ProcessStatus.REJECTED.getCode());
        }

        return updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recordReviewResult(Long id, Integer result, String score, String comment, String qaRecord) {
        ThesisProcessRecord record = getById(id);
        if (record == null) {
            throw new IllegalArgumentException("未找到记录");
        }

        // 校验当前状态必须是评审中
        ProcessStatus currentStatus = ProcessStatus.fromCode(record.getProcessStatus());
        if (currentStatus != ProcessStatus.REVIEWING) {
            throw new IllegalStateException("当前状态不允许记录评审结果，当前状态: " + currentStatus.getDesc());
        }

        ReviewResult reviewResult = ReviewResult.fromCode(result);
        if (reviewResult == null) {
            throw new IllegalArgumentException("无效的评审结果: " + result);
        }

        record.setReviewResult(result);
        if (score != null && !score.isEmpty()) {
            record.setReviewScore(new BigDecimal(score));
        }
        record.setReviewComment(comment);
        record.setQaRecord(qaRecord);

        // 根据评审结果更新流程状态
        if (reviewResult == ReviewResult.PASSED) {
            record.setProcessStatus(ProcessStatus.COMPLETED.getCode());

            // 如果是正式答辩通过，更新论文主表的最终结果和评分
            ProcessType processType = ProcessType.fromCode(record.getProcessType());
            if (processType == ProcessType.DEFENSE || processType == ProcessType.SECOND_DEFENSE) {
                ThesisMain thesisMain = thesisMainService.getById(record.getThesisId());
                if (thesisMain != null) {
                    thesisMain.setFinalResult(1); // 通过
                    if (record.getReviewScore() != null) {
                        thesisMain.setFinalScore(record.getReviewScore());
                    }
                    thesisMainService.updateById(thesisMain);
                }
            }
        } else if (reviewResult == ReviewResult.MODIFY_PASSED) {
            record.setProcessStatus(ProcessStatus.COMPLETED.getCode());

            // 修改后通过也更新主表
            ProcessType processType = ProcessType.fromCode(record.getProcessType());
            if (processType == ProcessType.DEFENSE || processType == ProcessType.SECOND_DEFENSE) {
                ThesisMain thesisMain = thesisMainService.getById(record.getThesisId());
                if (thesisMain != null) {
                    thesisMain.setFinalResult(1);
                    if (record.getReviewScore() != null) {
                        thesisMain.setFinalScore(record.getReviewScore());
                    }
                    thesisMainService.updateById(thesisMain);
                }
            }
        } else if (reviewResult == ReviewResult.FAILED) {
            record.setProcessStatus(ProcessStatus.REJECTED.getCode());

            // 如果是正式答辩未通过，更新主表
            ProcessType processType = ProcessType.fromCode(record.getProcessType());
            if (processType == ProcessType.DEFENSE || processType == ProcessType.SECOND_DEFENSE) {
                ThesisMain thesisMain = thesisMainService.getById(record.getThesisId());
                if (thesisMain != null) {
                    thesisMain.setFinalResult(2); // 未通过
                    thesisMainService.updateById(thesisMain);
                }
            }
        }

        return updateById(record);
    }

    @Override
    public ThesisProcessRecord getLatestRecord(Long thesisId, Integer processType) {
        LambdaQueryWrapper<ThesisProcessRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThesisProcessRecord::getThesisId, thesisId);
        wrapper.eq(ThesisProcessRecord::getProcessType, processType);
        wrapper.orderByDesc(ThesisProcessRecord::getVersion);
        wrapper.last("LIMIT 1");
        return getOne(wrapper, false);
    }

    @Override
    public boolean isProcessPassed(Long thesisId, Integer processType) {
        LambdaQueryWrapper<ThesisProcessRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThesisProcessRecord::getThesisId, thesisId);
        wrapper.eq(ThesisProcessRecord::getProcessType, processType);
        wrapper.eq(ThesisProcessRecord::getProcessStatus, ProcessStatus.PASSED.getCode())
                .or()
                .eq(ThesisProcessRecord::getProcessStatus, ProcessStatus.COMPLETED.getCode());
        return count(wrapper) > 0;
    }
}
