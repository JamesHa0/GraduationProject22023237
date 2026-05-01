package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.po.ThesisProcessRecord;

public interface ThesisProcessRecordService extends IService<ThesisProcessRecord> {

    /**
     * 提交流程记录（开题/中期/预答辩/外审/答辩等）
     * 自动处理version递增、状态初始化
     */
    boolean submitProcess(ThesisProcessRecord record);

    /**
     * 导师审批
     */
    boolean supervisorApprove(Long id, Integer status, String comment, Long approverId);

    /**
     * 教学秘书审批
     */
    boolean secretaryApprove(Long id, Integer status, String comment, Long approverId);

    /**
     * 院长审批
     */
    boolean deanApprove(Long id, Integer status, String comment, Long approverId);

    /**
     * 记录评审/答辩结果
     */
    boolean recordReviewResult(Long id, Integer result, String score, String comment, String qaRecord);

    /**
     * 获取某论文某环节的最新版本记录
     */
    ThesisProcessRecord getLatestRecord(Long thesisId, Integer processType);

    /**
     * 检查学生某环节是否已通过
     */
    boolean isProcessPassed(Long thesisId, Integer processType);
}
