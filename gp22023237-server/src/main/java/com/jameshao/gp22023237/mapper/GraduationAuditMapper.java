package com.jameshao.gp22023237.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jameshao.gp22023237.DTO.GraduationAuditWithDetailsDTO;
import com.jameshao.gp22023237.po.GraduationAudit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface GraduationAuditMapper extends BaseMapper<GraduationAudit> {

    /**
     * 查询毕业审核列表（分页），关联学生信息与学分汇总
     */
    IPage<GraduationAuditWithDetailsDTO> listWithDetails(IPage<GraduationAuditWithDetailsDTO> page,
                                                          @Param("studentNo") String studentNo,
                                                          @Param("studentName") String studentName,
                                                          @Param("auditStatus") Integer auditStatus,
                                                          @Param("cohortYear") Integer cohortYear,
                                                          @Param("department") String department,
                                                          @Param("major") String major);

    /**
     * 查询毕业审核详情，关联学生信息与学分汇总
     */
    GraduationAuditWithDetailsDTO getDetailWithDetails(@Param("id") Long id);

    /**
     * 计算学生已修总学分（JOIN查询，替代N+1循环）
     */
    BigDecimal calculateStudentCredits(@Param("studentId") Long studentId);
}
