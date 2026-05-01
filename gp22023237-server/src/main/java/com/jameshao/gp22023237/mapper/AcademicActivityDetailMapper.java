package com.jameshao.gp22023237.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jameshao.gp22023237.po.AcademicActivityDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AcademicActivityDetailMapper extends BaseMapper<AcademicActivityDetail> {

    /**
     * 根据提交主表ID查询详情
     */
    AcademicActivityDetail selectBySubmissionId(@Param("submissionId") Long submissionId);
}
