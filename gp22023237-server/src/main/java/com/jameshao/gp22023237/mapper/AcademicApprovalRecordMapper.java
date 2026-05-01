package com.jameshao.gp22023237.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jameshao.gp22023237.po.AcademicApprovalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AcademicApprovalRecordMapper extends BaseMapper<AcademicApprovalRecord> {

    /**
     * 查询某条提交记录的审批历史
     */
    List<AcademicApprovalRecord> listBySubmissionId(@Param("submissionId") Long submissionId);
}
