package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.DTO.GraduationAuditWithDetailsDTO;
import com.jameshao.gp22023237.po.GraduationAudit;

import java.math.BigDecimal;
import java.util.Map;

public interface GraduationAuditService extends IService<GraduationAudit> {

    /**
     * 自动审核毕业资格（单条）
     * 检查学分达标、论文完成、实践满足三项条件，三项全部通过则审核通过
     */
    GraduationAudit autoAudit(Long studentId);

    /**
     * 在独立事务中执行自动审核（REQUIRES_NEW传播）
     * 确保autoAudit失败时自身事务回滚，不影响调用方主事务
     */
    GraduationAudit autoAuditInNewTransaction(Long studentId);

    /**
     * 批量自动审核毕业资格
     * 按年级/院系/专业筛选学生并逐个执行自动审核
     */
    Map<String, Object> batchAutoAudit(Integer cohortYear, String department, String major);

    /**
     * 人工审核毕业资格
     */
    boolean manualAudit(Long id, Integer status, String comment, Long auditorId, String auditorName);

    /**
     * 查询毕业审核列表（带学生详情与学分汇总，SQL层分页）
     */
    IPage<GraduationAuditWithDetailsDTO> listWithDetails(IPage<GraduationAuditWithDetailsDTO> page,
                                                          String studentNo, String studentName,
                                                          Integer auditStatus, Integer cohortYear,
                                                          String department, String major);

    /**
     * 查询毕业审核详情（带学生详情与学分汇总）
     */
    GraduationAuditWithDetailsDTO getDetailWithDetails(Long id);

    /**
     * 获取毕业审核统计数据
     */
    Map<String, Object> getStats();

    /**
     * 计算学生已修总学分（从score表+course表汇总，排除不及格成绩）
     * 公共方法，供其他服务复用
     */
    BigDecimal calculateTotalCredits(Long studentId);
}
