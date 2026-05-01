package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.common.enums.ApprovalStatus;
import com.jameshao.gp22023237.common.enums.ProcessStatus;
import com.jameshao.gp22023237.common.enums.ProcessType;
import com.jameshao.gp22023237.common.enums.ReviewResult;
import com.jameshao.gp22023237.mapper.ThesisProcessRecordMapper;
import com.jameshao.gp22023237.po.ProcessConfig;
import com.jameshao.gp22023237.po.ThesisMain;
import com.jameshao.gp22023237.po.ThesisProcessRecord;
import com.jameshao.gp22023237.service.ProcessConfigService;
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

    @Autowired
    private ProcessConfigService processConfigService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitProcess(ThesisProcessRecord record) {
        // 确保thesisId和processType不为空
        if (record.getThesisId() == null || record.getProcessType() == null) {
            throw new IllegalArgumentException("thesisId和processType不能为空");
        }

        // 检查processType是否有效
        ProcessType processType = ProcessType.fromCode(record.getProcessType());
        if (processType == null) {
            throw new IllegalArgumentException("无效的流程类型: " + record.getProcessType());
        }

        // === ProcessConfig生效检查 ===
        // 1. 查询该环节的配置
        ProcessConfig config = processConfigService.getOne(
            new LambdaQueryWrapper<ProcessConfig>().eq(ProcessConfig::getProcessType, record.getProcessType()));
        if (config == null) {
            throw new IllegalArgumentException("未找到流程配置: " + processType.getDesc());
        }

        // 2. 检查环节是否启用
        if (config.getEnabled() != null && config.getEnabled() == 0) {
            throw new IllegalStateException("该环节已禁用: " + processType.getDesc());
        }

        // 3. 检查是否超过截止时间
        if (config.getDeadline() != null && new Date().after(config.getDeadline())) {
            throw new IllegalStateException("已超过截止时间: " + config.getDeadline());
        }

        // 4. 串行模式下检查前序环节是否通过
        // 读取system_config中的thesis_serial_mode配置（默认开启）
        // 前序环节：processType比当前小的最大环节
        if (record.getProcessType() > 1) {
            Integer prevProcessType = record.getProcessType() - 1;
            boolean prevPassed = isProcessPassed(record.getThesisId(), prevProcessType);
            if (!prevPassed) {
                ProcessType prevType = ProcessType.fromCode(prevProcessType);
                throw new IllegalStateException("前序环节尚未通过: " + (prevType != null ? prevType.getDesc() : prevProcessType));
            }
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

        // 5. 根据ProcessConfig动态设置初始审批状态
        // 如果不需要导师审批，直接标记为"跳过"（设为APPROVED）
        if (config.getNeedSupervisorApproval() != null && config.getNeedSupervisorApproval() == 0) {
            record.setSupervisorStatus(ApprovalStatus.APPROVED.getCode());
        } else {
            record.setSupervisorStatus(ApprovalStatus.UNAPPROVED.getCode());
        }

        if (config.getNeedSecretaryApproval() != null && config.getNeedSecretaryApproval() == 0) {
            record.setSecretaryStatus(ApprovalStatus.APPROVED.getCode());
        } else {
            record.setSecretaryStatus(ApprovalStatus.UNAPPROVED.getCode());
        }

        if (config.getNeedDeanApproval() != null && config.getNeedDeanApproval() == 0) {
            record.setDeanStatus(ApprovalStatus.APPROVED.getCode());
        } else {
            record.setDeanStatus(ApprovalStatus.UNAPPROVED.getCode());
        }

        // 判断是否所有需要审批的环节都已通过（即直接标记为PASSED）
        boolean allApprovalPassed = 
            (config.getNeedSupervisorApproval() == null || config.getNeedSupervisorApproval() == 0 || ApprovalStatus.APPROVED.getCode().equals(record.getSupervisorStatus())) &&
            (config.getNeedSecretaryApproval() == null || config.getNeedSecretaryApproval() == 0 || ApprovalStatus.APPROVED.getCode().equals(record.getSecretaryStatus())) &&
            (config.getNeedDeanApproval() == null || config.getNeedDeanApproval() == 0 || ApprovalStatus.APPROVED.getCode().equals(record.getDeanStatus()));

        if (allApprovalPassed && (config.getNeedReviewResult() == null || config.getNeedReviewResult() == 0)) {
            // 不需要任何审批也不需要评审，直接标记为已通过
            record.setProcessStatus(ProcessStatus.PASSED.getCode());
        } else {
            record.setProcessStatus(ProcessStatus.APPROVING.getCode());
        }

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
            // 院长通过后，对于需要录入评审结果的环节：标记为评审中
            // 其他环节：直接标记为已通过
            ProcessConfig config = processConfigService.getOne(
                new LambdaQueryWrapper<ProcessConfig>().eq(ProcessConfig::getProcessType, record.getProcessType()));
            if (config != null && config.getNeedReviewResult() != null && config.getNeedReviewResult() == 1) {
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

            // 如果是毕业论文通过，更新论文主表的最终结果和评分
            ProcessType processType = ProcessType.fromCode(record.getProcessType());
            if (processType == ProcessType.FINAL_THESIS) {
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
            if (processType == ProcessType.FINAL_THESIS) {
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

            // 如果是毕业论文未通过，更新主表
            ProcessType processType = ProcessType.fromCode(record.getProcessType());
            if (processType == ProcessType.FINAL_THESIS) {
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
