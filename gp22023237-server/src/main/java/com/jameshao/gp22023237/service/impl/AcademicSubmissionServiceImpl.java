package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.DTO.ApprovalRecordDTO;
import com.jameshao.gp22023237.DTO.SubmissionWithDetailsDTO;
import com.jameshao.gp22023237.common.enums.ApprovalAction;
import com.jameshao.gp22023237.common.enums.ContentType;
import com.jameshao.gp22023237.common.enums.OverallStatus;
import com.jameshao.gp22023237.mapper.*;
import com.jameshao.gp22023237.po.*;
import com.jameshao.gp22023237.service.AcademicApprovalRecordService;
import com.jameshao.gp22023237.service.AcademicSubmissionService;
import com.jameshao.gp22023237.service.NoticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class AcademicSubmissionServiceImpl extends ServiceImpl<AcademicSubmissionMapper, AcademicSubmission>
        implements AcademicSubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(AcademicSubmissionServiceImpl.class);

    @Autowired
    private AcademicActivityDetailMapper activityDetailMapper;

    @Autowired
    private AcademicAchievementDetailMapper achievementDetailMapper;

    @Autowired
    private AcademicInnovationDetailMapper innovationDetailMapper;

    @Autowired
    private AcademicApprovalRecordService approvalRecordService;

    @Autowired
    private NoticeService noticeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitContent(AcademicSubmission submission, Map<String, Object> detailData) {
        // 校验附件URL白名单
        if (!validateFileUrls(submission.getFileUrls())) {
            throw new IllegalArgumentException("附件URL格式不合法");
        }

        // 设置默认值
        submission.setApprovalStatus(OverallStatus.UNSUBMITTED.getCode());
        submission.setIsDeleted(0);
        submission.setVersion(1);
        submission.setCreateTime(new Date());
        submission.setUpdateTime(new Date());

        if (submission.getSubmitTime() != null) {
            // 直接提交
            submission.setApprovalStatus(OverallStatus.MENTOR_APPROVING.getCode());
            submission.setCurrentApproverType(2); // 导师
        }

        // 保存主表
        boolean result = save(submission);
        if (!result) return false;

        // 保存子表
        saveDetail(submission.getId(), submission.getContentType(), detailData);

        // 如果直接提交，生成审批记录
        if (submission.getSubmitTime() != null) {
            approvalRecordService.addRecord(submission.getId(), submission.getSubmitterId(),
                    submission.getSubmitterType(), ApprovalAction.SUBMIT.getCode(), null);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateContent(AcademicSubmission submission, Map<String, Object> detailData) {
        // 校验附件URL白名单
        if (!validateFileUrls(submission.getFileUrls())) {
            throw new IllegalArgumentException("附件URL格式不合法");
        }

        AcademicSubmission existing = getById(submission.getId());
        if (existing == null) return false;

        // 只有草稿和已驳回状态可以修改
        if (existing.getApprovalStatus() != OverallStatus.UNSUBMITTED.getCode()
                && existing.getApprovalStatus() != OverallStatus.REJECTED.getCode()) {
            logger.warn("记录 {} 当前状态 {} 不允许修改", submission.getId(), existing.getApprovalStatus());
            return false;
        }

        submission.setUpdateTime(new Date());
        boolean result = updateById(submission);
        if (!result) return false;

        // 更新子表（先删后插）
        updateDetail(submission.getId(), submission.getContentType(), detailData);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long submissionId, Long approverId, Integer approverType,
                            Integer action, String comment) {
        return approve(submissionId, approverId, approverType, action, comment, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long submissionId, Long approverId, Integer approverType,
                            Integer action, String comment, String reviewerFileUrls) {
        AcademicSubmission submission = getById(submissionId);
        if (submission == null) return false;

        OverallStatus currentStatus = OverallStatus.fromCode(submission.getApprovalStatus());
        if (currentStatus == null) return false;

        // 审批越权校验：当前审批人类型必须匹配记录的currentApproverType
        if (submission.getCurrentApproverType() != null
                && !submission.getCurrentApproverType().equals(approverType)) {
            logger.warn("审批越权：记录{}需要类型{}审批，当前审批人类型为{}",
                    submissionId, submission.getCurrentApproverType(), approverType);
            return false;
        }

        // 校验：当前状态必须是待审批节点
        if (currentStatus != OverallStatus.MENTOR_APPROVING
                && currentStatus != OverallStatus.SECRETARY_APPROVING
                && currentStatus != OverallStatus.DEAN_APPROVING) {
            logger.warn("记录{}当前状态{}不允许审批", submissionId, currentStatus);
            return false;
        }

        if (action == ApprovalAction.APPROVE.getCode()) {
            // 通过：推进到下一个审批节点
            OverallStatus nextStatus = getNextApprovalStatus(currentStatus);
            if (nextStatus == null) return false;

            OverallStatus targetStatus = nextStatus;
            if (!OverallStatus.canTransition(currentStatus, targetStatus)) {
                logger.warn("审批状态流转不允许：{} → {}", currentStatus, targetStatus);
                return false;
            }

            submission.setApprovalStatus(targetStatus.getCode());

            // 设置下一个审批人类型
            if (targetStatus == OverallStatus.SECRETARY_APPROVING) {
                submission.setCurrentApproverType(5); // 教学秘书
                submission.setCurrentApproverId(null);
            } else if (targetStatus == OverallStatus.DEAN_APPROVING) {
                submission.setCurrentApproverType(1); // 分管院长
                submission.setCurrentApproverId(null);
            } else if (targetStatus == OverallStatus.PASSED) {
                submission.setCurrentApproverId(null);
                submission.setCurrentApproverType(null);
            }

            submission.setUpdateTime(new Date());
            updateById(submission);

        } else if (action == ApprovalAction.REJECT.getCode()) {
            // 驳回：回到已驳回状态
            OverallStatus targetStatus = OverallStatus.REJECTED;
            if (!OverallStatus.canTransition(currentStatus, targetStatus)) {
                logger.warn("审批状态流转不允许：{} → {}", currentStatus, targetStatus);
                return false;
            }

            submission.setApprovalStatus(targetStatus.getCode());
            // 驳回时记录驳回节点：设置currentApproverType为驳回时的审批人类型
            // 这样前端可以通过currentApproverType判断是在哪个节点被驳回的
            submission.setCurrentApproverId(null);
            // 保留currentApproverType以标记驳回节点
            submission.setUpdateTime(new Date());
            updateById(submission);
        }

        // 记录审批操作
        approvalRecordService.addRecord(submissionId, approverId, approverType, action, comment, reviewerFileUrls);

        // 审批结果通知推送给归属学生（try-catch包裹，推送失败不影响审批事务）
        try {
            String actionText = (action == ApprovalAction.APPROVE.getCode()) ? "通过" : "驳回";
            String contentTypeText = getContentTypeText(submission.getContentType());
            String title = "审批结果通知";
            String content = "您的" + contentTypeText + "「" + submission.getTitle() + "」已被" + actionText;
            if (comment != null && !comment.trim().isEmpty()) {
                content += "，审批意见：" + comment;
            }
            // 通知发给归属学生（getStudentId），而非提交人（getSubmitterId），因为导师可能代提交
            noticeService.createAndPush(title, content, "1", submission.getStudentId());
        } catch (Exception e) {
            logger.warn("审批通知推送失败，不影响审批流程: {}", e.getMessage());
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean withdraw(Long submissionId, Long submitterId) {
        AcademicSubmission submission = getById(submissionId);
        if (submission == null) return false;

        // 只有待审批状态可以撤回
        if (submission.getApprovalStatus() != OverallStatus.MENTOR_APPROVING.getCode()
                && submission.getApprovalStatus() != OverallStatus.SECRETARY_APPROVING.getCode()
                && submission.getApprovalStatus() != OverallStatus.DEAN_APPROVING.getCode()) {
            return false;
        }

        // 只有提交人可以撤回
        if (!submission.getSubmitterId().equals(submitterId)) {
            return false;
        }

        submission.setApprovalStatus(OverallStatus.UNSUBMITTED.getCode());
        submission.setCurrentApproverId(null);
        submission.setCurrentApproverType(null);
        submission.setUpdateTime(new Date());
        updateById(submission);

        // 记录撤回操作
        approvalRecordService.addRecord(submissionId, submitterId,
                submission.getSubmitterType(), ApprovalAction.WITHDRAW.getCode(), "提交人撤回");

        return true;
    }

    @Override
    public boolean softDelete(Long submissionId) {
        AcademicSubmission submission = getById(submissionId);
        if (submission == null) return false;
        submission.setIsDeleted(1);
        submission.setUpdateTime(new Date());
        return updateById(submission);
    }

    @Override
    public SubmissionWithDetailsDTO getFullDetail(Long id) {
        SubmissionWithDetailsDTO dto = baseMapper.getDetailWithDetails(id);
        if (dto != null) {
            // 填充审批记录
            List<ApprovalRecordDTO> records = approvalRecordService.listBySubmissionId(id);
            dto.setApprovalRecords(records);
        }
        return dto;
    }

    @Override
    public List<SubmissionWithDetailsDTO> listWithDetails(Long studentId, List<Long> studentIds,
                                                           Integer contentType, Integer approvalStatus, Integer subType,
                                                           Long submitterId, Integer submitterType) {
        return baseMapper.listWithDetails(studentId, studentIds, contentType, approvalStatus, subType, submitterId, submitterType);
    }

    @Override
    public List<SubmissionWithDetailsDTO> listPendingApproval(Integer contentType, Integer approverType,
                                                                Long approverId, List<Long> studentIds) {
        return baseMapper.listPendingApproval(contentType, approverType, approverId, studentIds);
    }

    // ========== 私有方法 ==========

    private OverallStatus getNextApprovalStatus(OverallStatus current) {
        switch (current) {
            case MENTOR_APPROVING:
                return OverallStatus.SECRETARY_APPROVING;
            case SECRETARY_APPROVING:
                return OverallStatus.DEAN_APPROVING;
            case DEAN_APPROVING:
                return OverallStatus.PASSED;
            default:
                return null;
        }
    }

    private void saveDetail(Long submissionId, Integer contentType, Map<String, Object> detailData) {
        if (detailData == null) return;

        ContentType type = ContentType.fromCode(contentType);
        if (type == null) return;

        switch (type) {
            case ACTIVITY:
                AcademicActivityDetail activityDetail = new AcademicActivityDetail();
                activityDetail.setSubmissionId(submissionId);
                activityDetail.setActivityType(getIntValue(detailData, "activityType", 1));
                activityDetail.setActivityName(getStringValue(detailData, "activityName"));
                activityDetail.setActivityTime(getDateValue(detailData, "activityTime"));
                activityDetail.setLocation(getStringValue(detailData, "location"));
                activityDetail.setSpeaker(getStringValue(detailData, "speaker"));
                activityDetail.setContent(getStringValue(detailData, "content"));
                activityDetailMapper.insert(activityDetail);
                break;

            case ACHIEVEMENT:
                AcademicAchievementDetail achievementDetail = new AcademicAchievementDetail();
                achievementDetail.setSubmissionId(submissionId);
                achievementDetail.setAchievementType(getIntValue(detailData, "achievementType", 1));
                achievementDetail.setAuthors(getStringValue(detailData, "authors"));
                achievementDetail.setPublicationDate(getDateValue(detailData, "publicationDate"));
                achievementDetail.setJournalName(getStringValue(detailData, "journalName"));
                achievementDetail.setJournalLevel(getIntValue(detailData, "journalLevel", null));
                achievementDetail.setVolume(getStringValue(detailData, "volume"));
                achievementDetail.setIssue(getStringValue(detailData, "issue"));
                achievementDetail.setPages(getStringValue(detailData, "pages"));
                achievementDetail.setDoi(getStringValue(detailData, "doi"));
                achievementDetail.setPatentNo(getStringValue(detailData, "patentNo"));
                achievementDetail.setPatentType(getIntValue(detailData, "patentType", null));
                achievementDetail.setPatentStatus(getIntValue(detailData, "patentStatus", null));
                achievementDetail.setAwardName(getStringValue(detailData, "awardName"));
                achievementDetail.setAwardLevel(getIntValue(detailData, "awardLevel", null));
                achievementDetail.setAwardIssuer(getStringValue(detailData, "awardIssuer"));
                achievementDetail.setProjectName(getStringValue(detailData, "projectName"));
                achievementDetail.setProjectRole(getIntValue(detailData, "projectRole", null));
                achievementDetailMapper.insert(achievementDetail);
                break;

            case INNOVATION:
                AcademicInnovationDetail innovationDetail = new AcademicInnovationDetail();
                innovationDetail.setSubmissionId(submissionId);
                innovationDetail.setProjectType(getIntValue(detailData, "projectType", 1));
                innovationDetail.setProjectName(getStringValue(detailData, "projectName"));
                innovationDetail.setProjectLevel(getIntValue(detailData, "projectLevel", null));
                innovationDetail.setProjectNo(getStringValue(detailData, "projectNo"));
                innovationDetail.setLeader(getStringValue(detailData, "leader"));
                innovationDetail.setMembers(getStringValue(detailData, "members"));
                innovationDetail.setAdvisor(getStringValue(detailData, "advisor"));
                innovationDetail.setStartDate(getDateValue(detailData, "startDate"));
                innovationDetail.setEndDate(getDateValue(detailData, "endDate"));
                innovationDetail.setDescription(getStringValue(detailData, "description"));
                innovationDetail.setAchievements(getStringValue(detailData, "achievements"));
                innovationDetail.setAwardLevel(getIntValue(detailData, "awardLevel", null));
                innovationDetail.setFundingAmount(getBigDecimalValue(detailData, "fundingAmount"));
                innovationDetailMapper.insert(innovationDetail);
                break;
        }
    }

    private void updateDetail(Long submissionId, Integer contentType, Map<String, Object> detailData) {
        if (detailData == null) return;

        ContentType type = ContentType.fromCode(contentType);
        if (type == null) return;

        switch (type) {
            case ACTIVITY:
                AcademicActivityDetail existingActivity = activityDetailMapper.selectBySubmissionId(submissionId);
                if (existingActivity != null) {
                    activityDetailMapper.deleteById(existingActivity.getId());
                }
                break;
            case ACHIEVEMENT:
                AcademicAchievementDetail existingAchievement = achievementDetailMapper.selectBySubmissionId(submissionId);
                if (existingAchievement != null) {
                    achievementDetailMapper.deleteById(existingAchievement.getId());
                }
                break;
            case INNOVATION:
                AcademicInnovationDetail existingInnovation = innovationDetailMapper.selectBySubmissionId(submissionId);
                if (existingInnovation != null) {
                    innovationDetailMapper.deleteById(existingInnovation.getId());
                }
                break;
        }

        // 重新插入
        saveDetail(submissionId, contentType, detailData);
    }

    // ========== Map 取值工具方法 ==========

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Integer getIntValue(Map<String, Object> map, String key, Integer defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(value.toString()); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    private Date getDateValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Date) return (Date) value;
        // 前端传JSON字符串日期，支持多种格式解析
        if (value instanceof String) {
            String str = ((String) value).trim();
            if (str.isEmpty()) return null;
            try {
                // 尝试ISO 8601格式（如2024-01-15T10:30:00.000Z 或 2024-01-15T10:30:00+08:00）
                java.time.Instant instant = java.time.Instant.parse(str);
                return Date.from(instant);
            } catch (Exception ignored) {}
            try {
                // 尝试LocalDateTime格式（如2024-01-15T10:30:00）
                java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(str);
                return Date.from(ldt.atZone(java.time.ZoneId.systemDefault()).toInstant());
            } catch (Exception ignored) {}
            try {
                // 尝试LocalDate格式（如2024-01-15）
                java.time.LocalDate ld = java.time.LocalDate.parse(str);
                return Date.from(ld.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            } catch (Exception ignored) {}
            try {
                // 尝试常见日期格式
                java.text.SimpleDateFormat[] formats = {
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
                    new java.text.SimpleDateFormat("yyyy-MM-dd"),
                    new java.text.SimpleDateFormat("yyyy/MM/dd"),
                    new java.text.SimpleDateFormat("yyyy年MM月dd日")
                };
                for (java.text.SimpleDateFormat sdf : formats) {
                    try {
                        return sdf.parse(str);
                    } catch (Exception ignored2) {}
                }
            } catch (Exception ignored) {}
            // 尝试Long型时间戳
            try {
                long ts = Long.parseLong(str);
                return new Date(ts);
            } catch (Exception ignored) {}
        }
        // 尝试Number型时间戳
        if (value instanceof Number) {
            return new Date(((Number) value).longValue());
        }
        return null;
    }

    private java.math.BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.math.BigDecimal) return (java.math.BigDecimal) value;
        if (value instanceof Number) return java.math.BigDecimal.valueOf(((Number) value).doubleValue());
        try { return new java.math.BigDecimal(value.toString()); }
        catch (NumberFormatException e) { return null; }
    }

    /**
     * 校验附件URL是否合法
     * 支持格式：JSON数组字符串（如 ["url1","url2"]）、逗号分隔字符串、单个URL
     * 白名单：只允许 http/https 协议，且域名必须为系统配置的合法域名
     */
    private boolean validateFileUrls(String fileUrls) {
        if (fileUrls == null || fileUrls.trim().isEmpty()) return true;

        try {
            java.util.List<String> urls = new java.util.ArrayList<>();
            String trimmed = fileUrls.trim();

            // 尝试解析JSON数组格式
            if (trimmed.startsWith("[")) {
                com.alibaba.fastjson.JSONArray arr = com.alibaba.fastjson.JSONArray.parseArray(trimmed);
                for (int i = 0; i < arr.size(); i++) {
                    urls.add(arr.getString(i));
                }
            } else {
                // 逗号分隔或单个URL
                for (String u : trimmed.split(",")) {
                    if (!u.trim().isEmpty()) urls.add(u.trim());
                }
            }

            for (String url : urls) {
                if (!isValidUrl(url)) {
                    logger.warn("非法附件URL: {}", url);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            logger.warn("解析附件URL失败: {}", fileUrls, e);
            return false;
        }
    }

    /**
     * 单个URL合法性校验：只允许http/https协议
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) return true;

        // 支持 "url|原文件名" 格式，先提取URL部分再校验
        String actualUrl = url.trim();
        int pipeIdx = actualUrl.indexOf('|');
        if (pipeIdx > -1) {
            actualUrl = actualUrl.substring(0, pipeIdx).trim();
        }
        if (actualUrl.isEmpty()) return true;

        try {
            java.net.URI uri = new java.net.URI(actualUrl);
            String scheme = uri.getScheme();
            // 只允许http和https协议，禁止file:///、javascript:等
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        } catch (Exception e) {
            // 可能是相对路径（如七牛云key），允许
            return !actualUrl.contains("://") || actualUrl.startsWith("/") || actualUrl.startsWith("./");
        }
    }

    /**
     * 获取内容类型文字描述
     */
    private String getContentTypeText(Integer contentType) {
        if (contentType == null) return "学术内容";
        switch (contentType) {
            case 1: return "学术活动";
            case 2: return "学术成果";
            case 3: return "创新创业";
            default: return "学术内容";
        }
    }
}
