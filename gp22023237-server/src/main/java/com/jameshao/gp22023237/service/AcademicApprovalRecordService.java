package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.DTO.ApprovalRecordDTO;
import com.jameshao.gp22023237.po.AcademicApprovalRecord;

import java.util.List;

public interface AcademicApprovalRecordService extends IService<AcademicApprovalRecord> {

    /**
     * 查询某条提交记录的审批历史（含审批人姓名）
     */
    List<ApprovalRecordDTO> listBySubmissionId(Long submissionId);

    /**
     * 新增审批记录
     */
    boolean addRecord(Long submissionId, Long approverId, Integer approverType,
                      Integer action, String comment);

    /**
     * 新增审批记录（含附件）
     */
    boolean addRecord(Long submissionId, Long approverId, Integer approverType,
                      Integer action, String comment, String reviewerFileUrls);
}
