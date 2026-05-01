package com.jameshao.gp22023237.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jameshao.gp22023237.DTO.SubmissionWithDetailsDTO;
import com.jameshao.gp22023237.po.AcademicSubmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AcademicSubmissionMapper extends BaseMapper<AcademicSubmission> {

    /**
     * 查询提交列表，关联学生信息 + 类型详情
     */
    List<SubmissionWithDetailsDTO> listWithDetails(@Param("studentId") Long studentId,
                                                    @Param("studentIds") List<Long> studentIds,
                                                    @Param("contentType") Integer contentType,
                                                    @Param("approvalStatus") Integer approvalStatus,
                                                    @Param("subType") Integer subType,
                                                    @Param("submitterId") Long submitterId,
                                                    @Param("submitterType") Integer submitterType);

    /**
     * 查询提交详情，关联学生信息 + 类型详情
     */
    SubmissionWithDetailsDTO getDetailWithDetails(@Param("id") Long id);

    /**
     * 查询待审批列表（根据当前审批人角色）
     */
    List<SubmissionWithDetailsDTO> listPendingApproval(@Param("contentType") Integer contentType,
                                                        @Param("approverType") Integer approverType,
                                                        @Param("approverId") Long approverId,
                                                        @Param("studentIds") List<Long> studentIds);
}
