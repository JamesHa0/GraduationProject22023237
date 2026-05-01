package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.DTO.SubmissionWithDetailsDTO;
import com.jameshao.gp22023237.po.AcademicSubmission;

import java.util.List;
import java.util.Map;

public interface AcademicSubmissionService extends IService<AcademicSubmission> {

    /**
     * 提交学术内容（创建主表+子表记录）
     * @param submission 主表数据
     * @param detailData 子表数据（Map格式，key为字段名，value为字段值）
     * @return 是否成功
     */
    boolean submitContent(AcademicSubmission submission, Map<String, Object> detailData);

    /**
     * 更新学术内容（主表+子表）
     */
    boolean updateContent(AcademicSubmission submission, Map<String, Object> detailData);

    /**
     * 审批操作（通过/驳回）
     * @param submissionId 提交记录ID
     * @param approverId 审批人ID
     * @param approverType 审批人类型
     * @param action 审批动作：2=通过，3=驳回
     * @param comment 审批意见
     * @return 是否成功
     */
    boolean approve(Long submissionId, Long approverId, Integer approverType, Integer action, String comment);

    /**
     * 审批操作（通过/驳回，含附件）
     */
    boolean approve(Long submissionId, Long approverId, Integer approverType, Integer action, String comment, String reviewerFileUrls);

    /**
     * 撤回提交
     */
    boolean withdraw(Long submissionId, Long submitterId);

    /**
     * 软删除
     */
    boolean softDelete(Long submissionId);

    /**
     * 查询详情（含子表+审批记录）
     */
    SubmissionWithDetailsDTO getFullDetail(Long id);

    /**
     * 查询列表
     */
    List<SubmissionWithDetailsDTO> listWithDetails(Long studentId, List<Long> studentIds,
                                                     Integer contentType, Integer approvalStatus, Integer subType,
                                                     Long submitterId, Integer submitterType);

    /**
     * 查询待审批列表
     */
    List<SubmissionWithDetailsDTO> listPendingApproval(Integer contentType, Integer approverType,
                                                         Long approverId, List<Long> studentIds);
}
