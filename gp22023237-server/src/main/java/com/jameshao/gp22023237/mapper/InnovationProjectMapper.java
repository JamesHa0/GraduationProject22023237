package com.jameshao.gp22023237.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jameshao.gp22023237.DTO.InnovationProjectWithDetailsDTO;
import com.jameshao.gp22023237.po.InnovationProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InnovationProjectMapper extends BaseMapper<InnovationProject> {

    /**
     * 查询创新实践项目列表，关联学生信息
     */
    List<InnovationProjectWithDetailsDTO> listWithDetails(@Param("studentId") Long studentId,
                                                          @Param("studentIds") List<Long> studentIds,
                                                          @Param("projectType") Integer projectType,
                                                          @Param("status") Integer status);

    /**
     * 查询创新实践项目详情，关联学生信息
     */
    InnovationProjectWithDetailsDTO getDetailWithDetails(@Param("id") Long id);

}
