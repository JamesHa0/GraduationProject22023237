package com.jameshao.gp22023237.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jameshao.gp22023237.DTO.StudentStatusChangeWithDetailsDTO;
import com.jameshao.gp22023237.po.StudentStatusChange;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudentStatusChangeMapper extends BaseMapper<StudentStatusChange> {

    /**
     * 查询学籍异动列表，关联学生信息
     */
    List<StudentStatusChangeWithDetailsDTO> listWithDetails(@Param("studentId") Long studentId,
                                                              @Param("changeType") Integer changeType,
                                                              @Param("studentNo") String studentNo,
                                                              @Param("studentName") String studentName);

    /**
     * 查询学籍异动详情，关联学生信息
     */
    StudentStatusChangeWithDetailsDTO getDetailWithDetails(@Param("id") Long id);

}
