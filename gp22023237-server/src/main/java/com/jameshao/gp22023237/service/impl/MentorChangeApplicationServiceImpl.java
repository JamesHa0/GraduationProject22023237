package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.MentorChangeApplicationMapper;
import com.jameshao.gp22023237.po.MentorChangeApplication;
import com.jameshao.gp22023237.po.MentorStudent;
import com.jameshao.gp22023237.service.MentorChangeApplicationService;
import com.jameshao.gp22023237.service.MentorStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

@Service
public class MentorChangeApplicationServiceImpl extends ServiceImpl<MentorChangeApplicationMapper, MentorChangeApplication>
        implements MentorChangeApplicationService {

    @Autowired
    private MentorStudentService mentorStudentService;

    @Override
    @Transactional
    public boolean submitApplication(MentorChangeApplication application) {
        // 设置申请时间
        Date now = new Date();
        application.setApplyTime(now);
        application.setCreateTime(now);
        application.setUpdateTime(now);

        // 初始状态
        application.setOriginalMentorStatus(0);
        application.setNewMentorStatus(0);
        application.setOverallStatus(0);

        return save(application);
    }

    @Override
    @Transactional
    public boolean originalMentorApprove(Long id, Integer status, String comment) {
        MentorChangeApplication application = getById(id);
        if (application == null) {
            return false;
        }

        Date now = new Date();
        application.setOriginalMentorStatus(status);
        application.setOriginalMentorComment(comment);
        application.setOriginalMentorTime(now);
        application.setUpdateTime(now);

        if (status == 1) {
            // 原导师通过，进入新导师审批阶段
            application.setOverallStatus(1);
        } else if (status == 2) {
            // 原导师拒绝，流程结束
            application.setOverallStatus(3);
        }

        return updateById(application);
    }

    @Override
    @Transactional
    public boolean newMentorApprove(Long id, Integer status, String comment) {
        MentorChangeApplication application = getById(id);
        if (application == null) {
            return false;
        }

        Date now = new Date();
        application.setNewMentorStatus(status);
        application.setNewMentorComment(comment);
        application.setNewMentorTime(now);
        application.setUpdateTime(now);

        if (status == 1) {
            // 新导师通过，审批完成，更新导师学生关系
            application.setOverallStatus(2);
            updateMentorStudentRelationship(application);
        } else if (status == 2) {
            // 新导师拒绝，流程结束
            application.setOverallStatus(3);
        }

        return updateById(application);
    }

    private void updateMentorStudentRelationship(MentorChangeApplication application) {
        // 1. 删除学生与原导师的关系
        LambdaQueryWrapper<MentorStudent> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(MentorStudent::getStudentId, application.getStudentId())
                .eq(MentorStudent::getMentorId, application.getOriginalMentorId());
        mentorStudentService.remove(deleteWrapper);

        // 2. 添加学生与新导师的关系
        MentorStudent newRelationship = new MentorStudent();
        newRelationship.setStudentId(application.getStudentId());
        newRelationship.setMentorId(application.getNewMentorId());
        newRelationship.setMentorType(1); // 第一导师
        newRelationship.setCreateTime(new Date());
        newRelationship.setUpdateTime(new Date());
        mentorStudentService.save(newRelationship);
    }

    @Override
    public void exportMentorChangeApplication(Long id, jakarta.servlet.http.HttpServletResponse response) {
        try {
            // 获取导师更换申请详情
            com.jameshao.gp22023237.DTO.MentorChangeApplicationWithDetailsDTO application = baseMapper.getExportDetail(id);
            if (application == null) {
                throw new IllegalArgumentException("申请记录不存在");
            }

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setCharacterEncoding("utf-8");
            String studentName = application.getStudentName() != null ? application.getStudentName() : "学生";
            String fileName = java.net.URLEncoder.encode("导师更换申请表_" + studentName, java.nio.charset.StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".docx");

            // 准备文本占位符数据
            java.util.Map<String, String> dataMap = new java.util.HashMap<>();
            dataMap.put("studentNo", application.getStudentNo() != null ? application.getStudentNo() : "");
            dataMap.put("studentName", studentName);
            dataMap.put("studentDepartment", application.getStudentDepartment() != null ? application.getStudentDepartment() : "");
            dataMap.put("major", application.getMajor() != null ? application.getMajor() : "");
            dataMap.put("originalMentorName", application.getOriginalMentorName() != null ? application.getOriginalMentorName() : "");
            dataMap.put("originalMentorTitle", application.getOriginalMentorTitle() != null ? application.getOriginalMentorTitle() : "");
            dataMap.put("originalMentorDepartment", application.getOriginalMentorDepartment() != null ? application.getOriginalMentorDepartment() : "");
            dataMap.put("newMentorName", application.getNewMentorName() != null ? application.getNewMentorName() : "");
            dataMap.put("newMentorTitle", application.getNewMentorTitle() != null ? application.getNewMentorTitle() : "");
            dataMap.put("newMentorDepartment", application.getNewMentorDepartment() != null ? application.getNewMentorDepartment() : "");
            dataMap.put("changeReason", application.getChangeReason() != null ? application.getChangeReason() : "");
            dataMap.put("applyTime", application.getApplyTime() != null ? application.getApplyTime().toString() : "");

            // 状态转换
            Integer overallStatus = application.getOverallStatus();
            String statusText = "待审批";
            if (overallStatus != null) {
                switch (overallStatus) {
                    case 0:
                        statusText = "待原导师审批";
                        break;
                    case 1:
                        statusText = "待新导师审批";
                        break;
                    case 2:
                        statusText = "已通过";
                        break;
                    case 3:
                        statusText = "已拒绝";
                        break;
                }
            }
            dataMap.put("overallStatus", statusText);

            // 加载模板并填充（不需要表格数据）- 使用类加载器加载资源
            try (InputStream templateInputStream = getClass().getClassLoader().getResourceAsStream("templates/word/导师更换申请表.docx");
                 OutputStream outputStream = response.getOutputStream()) {

                if (templateInputStream == null) {
                    throw new RuntimeException("模板文件未找到: templates/word/导师更换申请表.docx");
                }

                System.out.println("=== 导师更换申请表 dataMap ===");
                System.out.println(dataMap.toString());

                com.jameshao.gp22023237.utils.WordExportUtil.fillTemplateAndExport(
                    templateInputStream, outputStream, dataMap, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }
}
