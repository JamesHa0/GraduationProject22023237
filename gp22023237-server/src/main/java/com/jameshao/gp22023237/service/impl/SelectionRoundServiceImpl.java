package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jameshao.gp22023237.po.MentorStudent;
import com.jameshao.gp22023237.po.Student;
import com.jameshao.gp22023237.po.SystemConfig;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.service.MentorStudentService;
import com.jameshao.gp22023237.service.SelectionRoundService;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.SystemConfigService;
import com.jameshao.gp22023237.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SelectionRoundServiceImpl implements SelectionRoundService {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private MentorStudentService mentorStudentService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    private static final String CONFIG_CURRENT_ROUND = "current_round";
    private static final String CONFIG_ROUND_1_END = "round_1_end_tutor";
    private static final String CONFIG_ROUND_2_END = "round_2_end_tutor";
    private static final String CONFIG_ROUND_3_END = "round_3_end_tutor";
    private static final String CONFIG_SUPPLEMENTARY_START = "supplementary_start";
    private static final String CONFIG_SUPPLEMENTARY_END = "supplementary_end";

    @Override
    public int getCurrentRound() {
        try {
            // 首先从配置获取
            SystemConfig config = systemConfigService.getConfigByKey(CONFIG_CURRENT_ROUND);
            if (config != null && config.getConfigValue() != null) {
                int configuredRound = Integer.parseInt(config.getConfigValue());

                // 结合时间自动判断（如果配置了时间）
                LocalDateTime now = LocalDateTime.now();
                Integer autoRound = calculateRoundByTime(now);
                if (autoRound != null && autoRound != configuredRound) {
                    // 如果时间驱动的轮次与配置不同，以时间为准并更新配置
                    switchRound(autoRound);
                    return autoRound;
                }
                return configuredRound;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1; // 默认第一轮
    }

    /**
     * 根据时间计算当前应该是第几轮
     */
    private Integer calculateRoundByTime(LocalDateTime now) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // 检查补选结束时间
            SystemConfig supplementaryEnd = systemConfigService.getConfigByKey(CONFIG_SUPPLEMENTARY_END);
            if (supplementaryEnd != null && supplementaryEnd.getConfigValue() != null && !supplementaryEnd.getConfigValue().isEmpty()) {
                LocalDateTime endTime = LocalDateTime.parse(supplementaryEnd.getConfigValue(), formatter);
                if (now.isAfter(endTime)) {
                    return null; // 已结束
                }
            }

            // 检查补选开始时间
            SystemConfig supplementaryStart = systemConfigService.getConfigByKey(CONFIG_SUPPLEMENTARY_START);
            if (supplementaryStart != null && supplementaryStart.getConfigValue() != null && !supplementaryStart.getConfigValue().isEmpty()) {
                LocalDateTime startTime = LocalDateTime.parse(supplementaryStart.getConfigValue(), formatter);
                if (now.isAfter(startTime)) {
                    return 4; // 补选阶段
                }
            }

            // 检查第三轮结束时间
            SystemConfig round3End = systemConfigService.getConfigByKey(CONFIG_ROUND_3_END);
            if (round3End != null && round3End.getConfigValue() != null && !round3End.getConfigValue().isEmpty()) {
                LocalDateTime endTime = LocalDateTime.parse(round3End.getConfigValue(), formatter);
                if (now.isAfter(endTime)) {
                    return 4; // 进入补选
                }
            }

            // 检查第二轮结束时间
            SystemConfig round2End = systemConfigService.getConfigByKey(CONFIG_ROUND_2_END);
            if (round2End != null && round2End.getConfigValue() != null && !round2End.getConfigValue().isEmpty()) {
                LocalDateTime endTime = LocalDateTime.parse(round2End.getConfigValue(), formatter);
                if (now.isAfter(endTime)) {
                    return 3; // 第三轮
                }
            }

            // 检查第一轮结束时间
            SystemConfig round1End = systemConfigService.getConfigByKey(CONFIG_ROUND_1_END);
            if (round1End != null && round1End.getConfigValue() != null && !round1End.getConfigValue().isEmpty()) {
                LocalDateTime endTime = LocalDateTime.parse(round1End.getConfigValue(), formatter);
                if (now.isAfter(endTime)) {
                    return 2; // 第二轮
                }
            }

            return 1; // 默认第一轮
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Transactional
    public boolean switchRound(int targetRound) {
        try {
            SystemConfig config = systemConfigService.getConfigByKey(CONFIG_CURRENT_ROUND);
            if (config == null) {
                config = new SystemConfig();
                config.setConfigKey(CONFIG_CURRENT_ROUND);
                config.setConfigName("当前轮次");
                config.setConfigType("selection");
            }
            config.setConfigValue(String.valueOf(targetRound));
            config.setUpdateTime(java.util.Calendar.getInstance().getTime());

            return systemConfigService.saveOrUpdate(config);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Map<String, String> getRoundConfig() {
        Map<String, String> config = new HashMap<>();
        String[] keys = {
                CONFIG_CURRENT_ROUND,
                CONFIG_ROUND_1_END,
                CONFIG_ROUND_2_END,
                CONFIG_ROUND_3_END,
                CONFIG_SUPPLEMENTARY_START,
                CONFIG_SUPPLEMENTARY_END
        };
        for (String key : keys) {
            SystemConfig c = systemConfigService.getConfigByKey(key);
            config.put(key, c != null ? c.getConfigValue() : "");
        }
        return config;
    }

    @Override
    @Transactional
    public boolean updateRoundConfig(String configKey, String configValue) {
        try {
            SystemConfig config = systemConfigService.getConfigByKey(configKey);
            if (config == null) {
                return false;
            }
            config.setConfigValue(configValue);
            config.setUpdateTime(java.util.Calendar.getInstance().getTime());
            return systemConfigService.saveOrUpdate(config);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public int advanceRejectedStudents() {
        int count = 0;

        // 查询所有被拒绝的记录（teacherStatus=2），且还有更高优先级志愿未处理的学生
        LambdaQueryWrapper<MentorStudent> rejectedWrapper = new LambdaQueryWrapper<>();
        rejectedWrapper.eq(MentorStudent::getTeacherStatus, 2);
        List<MentorStudent> rejectedList = mentorStudentService.list(rejectedWrapper);

        // 收集需要推进的学生ID（去重）
        List<Long> studentIds = new ArrayList<>();
        for (MentorStudent ms : rejectedList) {
            if (!studentIds.contains(ms.getStudentId())) {
                studentIds.add(ms.getStudentId());
            }
        }

        // 逐个推进学生
        for (Long studentId : studentIds) {
            if (advanceStudentToNextRound(studentId)) {
                count++;
            }
        }

        return count;
    }

    @Override
    @Transactional
    public boolean advanceStudentToNextRound(Long studentId) {
        try {
            // 查询该学生的所有志愿
            LambdaQueryWrapper<MentorStudent> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MentorStudent::getStudentId, studentId)
                    .eq(MentorStudent::getStudentStatus, 1)
                    .orderByAsc(MentorStudent::getStudentChoiceOrder);
            List<MentorStudent> allChoices = mentorStudentService.list(wrapper);

            if (allChoices.isEmpty()) {
                return false;
            }

            // 找到第一个被拒绝的志愿
            MentorStudent rejectedChoice = null;
            int rejectedIndex = -1;
            for (int i = 0; i < allChoices.size(); i++) {
                MentorStudent ms = allChoices.get(i);
                if (ms.getTeacherStatus() == null || ms.getTeacherStatus() == 2) {
                    rejectedChoice = ms;
                    rejectedIndex = i;
                    break;
                }
            }

            if (rejectedChoice == null) {
                return false; // 没有被拒绝的志愿
            }

            // 找到下一个志愿（如果有）
            if (rejectedIndex + 1 < allChoices.size()) {
                MentorStudent nextChoice = allChoices.get(rejectedIndex + 1);
                int currentRound = getCurrentRound();
                int nextRound = Math.min(currentRound + 1, 3); // 最多到第三轮

                // 将下一个志愿的轮次更新为下一轮
                if (nextChoice.getRound() == null || nextChoice.getRound() < nextRound) {
                    UpdateWrapper<MentorStudent> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("id", nextChoice.getId())
                            .set("round", nextRound);
                    mentorStudentService.update(null, updateWrapper);
                    System.out.println("学生 " + studentId + " 的志愿 " + nextChoice.getStudentChoiceOrder() + " 推进到第 " + nextRound + " 轮");
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getUnmatchedStudents() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 查询所有学生
        List<Student> allStudents = studentService.list();

        for (Student student : allStudents) {
            // 检查该学生是否有已同意的志愿
            LambdaQueryWrapper<MentorStudent> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MentorStudent::getStudentId, student.getId())
                    .eq(MentorStudent::getStudentStatus, 1)
                    .eq(MentorStudent::getTeacherStatus, 1);
            List<MentorStudent> accepted = mentorStudentService.list(wrapper);

            if (accepted.isEmpty()) {
                // 没有被接受，加入未匹配列表
                Map<String, Object> map = new HashMap<>();
                map.put("studentId", student.getId());
                map.put("studentName", student.getStudentName());
                map.put("studentNo", student.getStudentNo());
                map.put("department", student.getDepartment());
                map.put("major", student.getMajor());
                map.put("userId", student.getUserId());
                result.add(map);
            }
        }

        return result;
    }

    @Override
    @Transactional
    public boolean manualAssignMentor(Long studentId, Long mentorId) {
        try {
            // 创建新的 mentor_student 记录
            MentorStudent ms = new MentorStudent();
            ms.setStudentId(studentId);
            ms.setMentorId(mentorId);
            ms.setRound(4); // 补选/手动分配轮次
            ms.setStudentChoiceOrder(1);
            ms.setStudentStatus(1);
            ms.setTeacherStatus(1); // 直接设为已同意
            ms.setSelectionTime(new java.util.Date());
            ms.setConfirmTime(new java.util.Date());

            boolean saved = mentorStudentService.save(ms);
            if (!saved) {
                return false;
            }

            // 更新导师已确认名额
            Teacher teacher = teacherService.getById(mentorId);
            if (teacher != null) {
                UpdateWrapper<Teacher> teacherWrapper = new UpdateWrapper<>();
                teacherWrapper.eq("id", mentorId)
                        .set("confirmed_quota", teacher.getConfirmedQuota() + 1);
                teacherService.update(null, teacherWrapper);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Map<String, Object> getRoundStatistics() {
        Map<String, Object> stats = new HashMap<>();

        int currentRound = getCurrentRound();
        stats.put("currentRound", currentRound);

        // 统计各轮次的记录数
        for (int round = 1; round <= 4; round++) {
            LambdaQueryWrapper<MentorStudent> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MentorStudent::getRound, round)
                    .eq(MentorStudent::getStudentStatus, 1);
            long total = mentorStudentService.count(wrapper);

            wrapper.clear();
            wrapper.eq(MentorStudent::getRound, round)
                    .eq(MentorStudent::getStudentStatus, 1)
                    .eq(MentorStudent::getTeacherStatus, 1);
            long accepted = mentorStudentService.count(wrapper);

            wrapper.clear();
            wrapper.eq(MentorStudent::getRound, round)
                    .eq(MentorStudent::getStudentStatus, 1)
                    .eq(MentorStudent::getTeacherStatus, 2);
            long rejected = mentorStudentService.count(wrapper);

            wrapper.clear();
            wrapper.eq(MentorStudent::getRound, round)
                    .eq(MentorStudent::getStudentStatus, 1)
                    .and(w -> w.isNull(MentorStudent::getTeacherStatus)
                            .or().eq(MentorStudent::getTeacherStatus, 0));
            long pending = mentorStudentService.count(wrapper);

            Map<String, Object> roundStats = new HashMap<>();
            roundStats.put("total", total);
            roundStats.put("accepted", accepted);
            roundStats.put("rejected", rejected);
            roundStats.put("pending", pending);
            stats.put("round" + round, roundStats);
        }

        // 未匹配学生数
        stats.put("unmatchedStudents", getUnmatchedStudents().size());

        return stats;
    }
}
