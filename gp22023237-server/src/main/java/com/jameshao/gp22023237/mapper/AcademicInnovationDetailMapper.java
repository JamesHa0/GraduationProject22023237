package com.jameshao.gp22023237.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jameshao.gp22023237.po.AcademicInnovationDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AcademicInnovationDetailMapper extends BaseMapper<AcademicInnovationDetail> {

    /**
     * 根据提交主表ID查询详情
     */
    AcademicInnovationDetail selectBySubmissionId(@Param("submissionId") Long submissionId);
}
