package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jameshao.gp22023237.po.*;
import com.jameshao.gp22023237.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 定时通知调度服务实现
 * 使用notification_key去重，同一事件同一天只推送一次
 */
@Service
public class NotificationScheduledServiceImpl implements NotificationScheduledService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduledServiceImpl.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private SelectionRoundService selectionRoundService;

    @Autowired
    private ProcessConfigService processConfigService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentStatusChangeService studentStatusChangeService;

    @Autowired
    private CoursePhaseService coursePhaseService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private ThesisProcessRecordService thesisProcessRecordService;

    @Autowired
    private ThesisMainService thesisMainService;

    // ==================== 每小时任务 ====================

    /**
     * 1.T 双选阶段截止前24小时提醒
     */
    @Override
    @Scheduled(cron = "0 0 * * * ?")
    public void checkSelectionDeadline() {
        try {
            int currentRound = selectionRoundService.getCurrentRound();
            if (currentRound == 0) return;

            // 检查当前轮次是否在有效范围内
            if (currentRound != 9 && currentRound != 1 && currentRound != 2 && currentRound != 3
                    && currentRound != 7 && currentRound != 8) return;

            Map<String, String> roundConfig = selectionRoundService.getRoundConfig();
            String endTimeKey = getEndTimeKey(currentRound);
            if (endTimeKey == null) return;

            String endTimeStr = roundConfig.get(endTimeKey);
            if (endTimeStr == null || endTimeStr.isEmpty()) return;

            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, FORMATTER);
            LocalDateTime now = LocalDateTime.now();
            long hoursUntilEnd = java.time.Duration.between(now, endTime).toHours();

            if (hoursUntilEnd > 0 && hoursUntilEnd <= 24) {
                String today = LocalDate.now().toString();
                String notificationKey = "selection:deadline:" + currentRound + ":" + today;
                String roundText = getRoundText(currentRound);
                String content = "双选【" + roundText + "】将于" + hoursUntilEnd + "小时后截止";

                List<Long> userIds = getSelectionParticipantUserIds();
                if (!userIds.isEmpty()) {
                    noticeService.createAndPushToUsersWithKey(notificationKey, userIds,
                        "双选截止提醒", content, "1");
                }
            }
        } catch (Exception e) {
            logger.warn("双选截止提醒检查失败: {}", e.getMessage());
        }
    }

    /**
     * 5.T1 选课截止前1天提醒
     */
    @Override
    @Scheduled(cron = "0 0 * * * ?")
    public void checkCourseSelectionDeadline() {
        try {
            if (!coursePhaseService.isSelectionOpen()) return;

            Map<String, String> phaseConfig = coursePhaseService.getPhaseConfig();
            String endTimeStr = phaseConfig.get("course_selection_end");
            if (endTimeStr == null || endTimeStr.isEmpty()) return;

            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, FORMATTER);
            LocalDateTime now = LocalDateTime.now();
            long hoursUntilEnd = java.time.Duration.between(now, endTime).toHours();

            if (hoursUntilEnd > 0 && hoursUntilEnd <= 24) {
                String today = LocalDate.now().toString();
                String notificationKey = "course:selection_deadline:" + today;

                List<Student> students = studentService.list();
                List<Long> userIds = new ArrayList<>();
                for (Student s : students) {
                    if (s.getUserId() != null) userIds.add(s.getUserId());
                }
                if (!userIds.isEmpty()) {
                    noticeService.createAndPushToUsersWithKey(notificationKey, userIds,
                        "选课截止提醒", "选课将于明天截止，请尽快完成选课", "1");
                }
            }
        } catch (Exception e) {
            logger.warn("选课截止提醒检查失败: {}", e.getMessage());
        }
    }

    // ==================== 每日任务 ====================

    /**
     * 2.T1 论文阶段截止前3天/1天提醒
     */
    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkThesisDeadline() {
        try {
            List<ProcessConfig> configs = processConfigService.list(
                new LambdaQueryWrapper<ProcessConfig>().isNotNull(ProcessConfig::getDeadline));
            LocalDate today = LocalDate.now();

            for (ProcessConfig config : configs) {
                if (config.getDeadline() == null) continue;
                LocalDate deadline = config.getDeadline().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
                long daysUntil = java.time.Period.between(today, deadline).getDays();

                if (daysUntil == 3 || daysUntil == 1) {
                    String notificationKey = "thesis:deadline:" + config.getProcessType()
                        + ":" + today;
                    String processName = config.getProcessName() != null
                        ? config.getProcessName() : "论文环节";
                    String content = "论文【" + processName + "】将于" + daysUntil + "天后截止";

                    // 只通知有论文记录的学生
                    List<ThesisMain> thesisList = thesisMainService.list();
                    java.util.Set<Long> studentIdsWithThesis = thesisList.stream()
                        .map(ThesisMain::getStudentId).collect(java.util.stream.Collectors.toSet());
                    List<Long> userIds = new ArrayList<>();
                    for (Long sid : studentIdsWithThesis) {
                        Long uid = noticeService.getStudentUserId(sid);
                        if (uid != null) userIds.add(uid);
                    }
                    if (!userIds.isEmpty()) {
                        noticeService.createAndPushToUsersWithKey(notificationKey, userIds,
                            "论文截止提醒", content, "1");
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("论文截止提醒检查失败: {}", e.getMessage());
        }
    }

    /**
     * 2.T2 论文超时未提交提醒
     */
    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkThesisOverdue() {
        try {
            List<ProcessConfig> configs = processConfigService.list(
                new LambdaQueryWrapper<ProcessConfig>().isNotNull(ProcessConfig::getDeadline));
            Date now = new Date();
            LocalDate today = LocalDate.now();

            for (ProcessConfig config : configs) {
                if (config.getDeadline() == null || !now.after(config.getDeadline())) continue;

                String notificationKey = "thesis:overdue:" + config.getProcessType()
                    + ":" + today;
                String processName = config.getProcessName() != null
                    ? config.getProcessName() : "论文环节";

                List<ThesisMain> thesisListOverdue = thesisMainService.list();
                java.util.Set<Long> studentIdsWithThesisOverdue = thesisListOverdue.stream()
                    .map(ThesisMain::getStudentId).collect(java.util.stream.Collectors.toSet());
                List<Long> userIds = new ArrayList<>();
                for (Long sid : studentIdsWithThesisOverdue) {
                    Long uid = noticeService.getStudentUserId(sid);
                    if (uid != null) userIds.add(uid);
                }
                if (!userIds.isEmpty()) {
                    noticeService.createAndPushToUsersWithKey(notificationKey, userIds,
                        "论文超时提醒",
                        "论文【" + processName + "】已截止，您尚未提交", "1");
                }
            }
        } catch (Exception e) {
            logger.warn("论文超时提醒检查失败: {}", e.getMessage());
        }
    }

    /**
     * 3.T 学籍异动到期前30天提醒
     */
    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkStatusChangeExpiry() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate targetDate = today.plusDays(30);

            List<StudentStatusChange> changes = studentStatusChangeService.list(
                new LambdaQueryWrapper<StudentStatusChange>()
                    .eq(StudentStatusChange::getStatus, 2) // 已通过
                    .in(StudentStatusChange::getChangeType, 1, 4) // 休学或延期
                    .isNotNull(StudentStatusChange::getEndDate));

            for (StudentStatusChange change : changes) {
                if (change.getEndDate() == null) continue;
                LocalDate endDate = change.getEndDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();

                if (!endDate.isAfter(targetDate) && !endDate.isBefore(today)) {
                    String notificationKey = "status:expiry:" + change.getId() + ":" + today;
                    Long studentUserId = noticeService.getStudentUserId(change.getStudentId());
                    if (studentUserId != null) {
                        String[] changeTypes = {"", "休学", "复学", "退学", "延期"};
                        String changeTypeText = (change.getChangeType() != null && change.getChangeType() > 0 && change.getChangeType() < changeTypes.length)
                            ? changeTypes[change.getChangeType()] : "学籍异动";
                        long daysLeft = java.time.Period.between(today, endDate).getDays();
                        noticeService.createAndPushWithKey(notificationKey,
                            "学籍异动到期提醒",
                            "【" + changeTypeText + "】将于" + daysLeft + "天后到期",
                            "1", studentUserId);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("学籍异动到期提醒检查失败: {}", e.getMessage());
        }
    }

    /**
     * 6.T 成绩录入截止前1天提醒
     */
    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkScoreEntryDeadline() {
        try {
            if (!coursePhaseService.isScoreEntryOpen()) return;

            Map<String, String> phaseConfig = coursePhaseService.getPhaseConfig();
            String endTimeStr = phaseConfig.get("score_entry_end");
            if (endTimeStr == null || endTimeStr.isEmpty()) return;

            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, FORMATTER);
            LocalDateTime now = LocalDateTime.now();
            long daysUntilEnd = java.time.Duration.between(now, endTime).toDays();

            if (daysUntilEnd >= 0 && daysUntilEnd <= 1) {
                String today = LocalDate.now().toString();
                String notificationKey = "course:score_deadline:" + today;

                List<Teacher> teachers = teacherService.list();
                List<Long> userIds = new ArrayList<>();
                for (Teacher t : teachers) {
                    if (t.getUserId() != null && t.getIsMentor() != null && t.getIsMentor() == 1) {
                        userIds.add(t.getUserId());
                    }
                }
                if (!userIds.isEmpty()) {
                    noticeService.createAndPushToUsersWithKey(notificationKey, userIds,
                        "成绩录入截止提醒",
                        "请在" + endTimeStr + "前完成成绩录入", "1");
                }
            }
        } catch (Exception e) {
            logger.warn("成绩录入截止提醒检查失败: {}", e.getMessage());
        }
    }

    /**
     * 5.T2 教学评价截止前3天/1天提醒
     */
    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkTeachingEvaluationDeadline() {
        try {
            Map<String, String> phaseConfig = coursePhaseService.getPhaseConfig();
            String endTimeStr = phaseConfig.get("evaluation_end");
            if (endTimeStr == null || endTimeStr.isEmpty()) return;

            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, FORMATTER);
            LocalDateTime now = LocalDateTime.now();
            long daysUntilEnd = java.time.Duration.between(now, endTime).toDays();

            if (daysUntilEnd == 3 || daysUntilEnd == 1) {
                String today = LocalDate.now().toString();
                String notificationKey = "course:evaluation_deadline:" + today;

                List<Student> students = studentService.list();
                List<Long> userIds = new ArrayList<>();
                for (Student s : students) {
                    if (s.getUserId() != null) userIds.add(s.getUserId());
                }
                if (!userIds.isEmpty()) {
                    noticeService.createAndPushToUsersWithKey(notificationKey, userIds,
                        "教学评价截止提醒",
                        "请在" + daysUntilEnd + "天内完成教学评价", "1");
                }
            }
        } catch (Exception e) {
            logger.warn("教学评价截止提醒检查失败: {}", e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    private String getEndTimeKey(int round) {
        switch (round) {
            case 9: return "student_select_end";
            case 1: return "first_round_end_tutor";
            case 2: return "second_round_end_tutor";
            case 3: return "third_round_end_tutor";
            case 7: return "supplementary_student_end";
            case 8: return "supplementary_tutor_end";
            default: return null;
        }
    }

    private String getRoundText(int round) {
        switch (round) {
            case 9: return "学生预选轮";
            case 1: return "第1轮";
            case 2: return "第2轮";
            case 3: return "第3轮";
            case 7: return "补选学生选择轮";
            case 8: return "补选导师选择轮";
            default: return "第" + round + "轮";
        }
    }

    private List<Long> getSelectionParticipantUserIds() {
        List<Long> userIds = new ArrayList<>();
        String cohortYearConfig = selectionRoundService.getSelectionCohortYear();
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        if (cohortYearConfig != null && !cohortYearConfig.isEmpty()) {
            try {
                studentWrapper.eq(Student::getCohortYear, Integer.parseInt(cohortYearConfig));
            } catch (NumberFormatException ignored) {}
        }
        studentWrapper.select(Student::getUserId);
        List<Student> students = studentService.list(studentWrapper);
        for (Student s : students) {
            if (s.getUserId() != null) userIds.add(s.getUserId());
        }
        LambdaQueryWrapper<Teacher> teacherWrapper = new LambdaQueryWrapper<>();
        teacherWrapper.eq(Teacher::getIsMentor, 1).select(Teacher::getUserId);
        List<Teacher> teachers = teacherService.list(teacherWrapper);
        for (Teacher t : teachers) {
            if (t.getUserId() != null) userIds.add(t.getUserId());
        }
        return userIds;
    }
}
