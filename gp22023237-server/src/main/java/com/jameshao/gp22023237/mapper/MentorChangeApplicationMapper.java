package com.jameshao.gp22023237.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jameshao.gp22023237.DTO.MentorChangeApplicationWithDetailsDTO;
import com.jameshao.gp22023237.po.MentorChangeApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MentorChangeApplicationMapper extends BaseMapper<MentorChangeApplication> {

    /**
     * 查询导师更换申请列表，关联学生和导师信息
     */
    List<MentorChangeApplicationWithDetailsDTO> listWithDetails(@Param("studentId") Long studentId,
                                                                   @Param("studentNo") String studentNo,
                                                                   @Param("studentName") String studentName,
                                                                   @Param("overallStatus") Integer overallStatus);

    /**
     * 查询导师更换申请详情，关联学生和导师信息
     */
    MentorChangeApplicationWithDetailsDTO getDetailWithDetails(@Param("id") Long id);

}
