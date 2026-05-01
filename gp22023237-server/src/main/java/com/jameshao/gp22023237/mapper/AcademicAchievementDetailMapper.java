package com.jameshao.gp22023237.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jameshao.gp22023237.po.AcademicAchievementDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AcademicAchievementDetailMapper extends BaseMapper<AcademicAchievementDetail> {

    /**
     * 根据提交主表ID查询详情
     */
    AcademicAchievementDetail selectBySubmissionId(@Param("submissionId") Long submissionId);
}
