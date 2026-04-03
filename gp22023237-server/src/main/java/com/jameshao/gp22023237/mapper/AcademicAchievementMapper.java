package com.jameshao.gp22023237.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jameshao.gp22023237.DTO.AcademicAchievementWithDetailsDTO;
import com.jameshao.gp22023237.po.AcademicAchievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AcademicAchievementMapper extends BaseMapper<AcademicAchievement> {

    /**
     * 查询学术成果列表，关联学生信息
     */
    List<AcademicAchievementWithDetailsDTO> listWithDetails(@Param("studentId") Long studentId,
                                                            @Param("studentIds") List<Long> studentIds,
                                                            @Param("achievementType") Integer achievementType,
                                                            @Param("status") Integer status);

    /**
     * 查询学术成果详情，关联学生信息
     */
    AcademicAchievementWithDetailsDTO getDetailWithDetails(@Param("id") Long id);

}
