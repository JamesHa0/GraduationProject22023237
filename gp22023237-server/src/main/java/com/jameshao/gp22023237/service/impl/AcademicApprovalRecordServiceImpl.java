package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.DTO.ApprovalRecordDTO;
import com.jameshao.gp22023237.common.enums.ApprovalAction;
import com.jameshao.gp22023237.mapper.AcademicApprovalRecordMapper;
import com.jameshao.gp22023237.po.AcademicApprovalRecord;
import com.jameshao.gp22023237.service.AcademicApprovalRecordService;
import com.jameshao.gp22023237.mapper.TeacherMapper;
import com.jameshao.gp22023237.mapper.UserMapper;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.po.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class AcademicApprovalRecordServiceImpl extends ServiceImpl<AcademicApprovalRecordMapper, AcademicApprovalRecord>
        implements AcademicApprovalRecordService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<ApprovalRecordDTO> listBySubmissionId(Long submissionId) {
        List<AcademicApprovalRecord> records = baseMapper.listBySubmissionId(submissionId);
        List<ApprovalRecordDTO> dtos = new ArrayList<>();
        if (records.isEmpty()) return dtos;

        // 批量收集审批人ID，按类型分组
        Map<Integer, List<Long>> approverIdsByType = new java.util.HashMap<>();
        for (AcademicApprovalRecord record : records) {
            if (record.getApproverId() != null && record.getApproverType() != null) {
                approverIdsByType.computeIfAbsent(record.getApproverType(), k -> new ArrayList<>())
                        .add(record.getApproverId());
            }
        }

        // 批量查询审批人姓名，结果缓存到Map
        Map<String, String> approverNameCache = new java.util.HashMap<>();
        for (Map.Entry<Integer, List<Long>> entry : approverIdsByType.entrySet()) {
            Integer type = entry.getKey();
            List<Long> ids = entry.getValue();
            if (type == 2) {
                // 导师 → 批量查teacher表
                List<Teacher> teachers = teacherMapper.selectBatchIds(ids);
                for (Teacher t : teachers) {
                    approverNameCache.put("2_" + t.getId(), t.getTeacherName() != null ? t.getTeacherName() : "未知导师");
                }
            } else {
                // 管理员 → 批量查user表
                List<User> users = userMapper.selectBatchIds(ids);
                for (User u : users) {
                    approverNameCache.put(type + "_" + u.getId(), u.getUsername() != null ? u.getUsername() : "未知管理员");
                }
            }
        }

        // 组装DTO
        for (AcademicApprovalRecord record : records) {
            ApprovalRecordDTO dto = new ApprovalRecordDTO();
            dto.setId(record.getId());
            dto.setSubmissionId(record.getSubmissionId());
            dto.setApproverId(record.getApproverId());
            dto.setApproverType(record.getApproverType());
            dto.setApprovalAction(record.getApprovalAction());
            dto.setApprovalComment(record.getApprovalComment());
            dto.setReviewerFileUrls(record.getReviewerFileUrls());
            dto.setApprovalTime(record.getApprovalTime());
            dto.setCreateTime(record.getCreateTime());

            // 从缓存获取审批人姓名
            if (record.getApproverId() != null && record.getApproverType() != null) {
                String cacheKey = record.getApproverType() + "_" + record.getApproverId();
                dto.setApproverName(approverNameCache.getOrDefault(cacheKey, "未知"));
            }

            // 填充审批人类型名称
            dto.setApproverTypeName(getApproverTypeName(record.getApproverType()));

            // 填充审批动作名称
            ApprovalAction action = ApprovalAction.fromCode(record.getApprovalAction());
            dto.setApprovalActionName(action != null ? action.getDesc() : "未知");

            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public boolean addRecord(Long submissionId, Long approverId, Integer approverType,
                              Integer action, String comment) {
        return addRecord(submissionId, approverId, approverType, action, comment, null);
    }

    @Override
    public boolean addRecord(Long submissionId, Long approverId, Integer approverType,
                              Integer action, String comment, String reviewerFileUrls) {
        AcademicApprovalRecord record = new AcademicApprovalRecord();
        record.setSubmissionId(submissionId);
        record.setApproverId(approverId);
        record.setApproverType(approverType);
        record.setApprovalAction(action);
        record.setApprovalComment(comment);
        // JSON类型列必须存null或有效JSON数组
        if (reviewerFileUrls == null || reviewerFileUrls.isBlank()) {
            record.setReviewerFileUrls(null);
        } else if (reviewerFileUrls.trim().startsWith("[")) {
            // 已经是JSON数组格式，直接使用
            record.setReviewerFileUrls(reviewerFileUrls);
        } else {
            // 非JSON格式，转为JSON数组
            record.setReviewerFileUrls(com.alibaba.fastjson2.JSON.toJSONString(java.util.Arrays.stream(reviewerFileUrls.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()));
        }
        record.setApprovalTime(new Date());
        record.setCreateTime(new Date());
        return save(record);
    }

    private String getApproverName(Long approverId, Integer approverType) {
        if (approverType == 2) {
            // 导师 → 查teacher表
            Teacher teacher = teacherMapper.selectById(approverId);
            return teacher != null ? teacher.getTeacherName() : "未知导师";
        } else {
            // 管理员 → 查user表
            User user = userMapper.selectById(approverId);
            return user != null ? user.getUsername() : "未知管理员";
        }
    }

    private String getApproverTypeName(Integer approverType) {
        if (approverType == null) return "未知";
        switch (approverType) {
            case 2: return "导师";
            case 5: return "教学秘书";
            case 1: return "分管院长";
            default: return "未知";
        }
    }
}
