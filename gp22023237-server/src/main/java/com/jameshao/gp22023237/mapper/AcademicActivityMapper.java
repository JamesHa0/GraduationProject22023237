package com.jameshao.gp22023237.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jameshao.gp22023237.DTO.AcademicActivityWithDetailsDTO;
import com.jameshao.gp22023237.po.AcademicActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AcademicActivityMapper extends BaseMapper<AcademicActivity> {

    /**
     * 查询学术活动列表，关联学生信息
     */
    List<AcademicActivityWithDetailsDTO> listWithDetails(@Param("studentId") Long studentId,
                                                          @Param("studentIds") List<Long> studentIds,
                                                          @Param("activityType") Integer activityType,
                                                          @Param("status") Integer status);

    /**
     * 查询学术活动详情，关联学生信息
     */
    AcademicActivityWithDetailsDTO getDetailWithDetails(@Param("id") Long id);

}
