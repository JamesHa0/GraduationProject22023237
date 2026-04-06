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
    private static final String CONFIG_ENABLE_EXTRA_ROUND = "enable_extra_round";
    private static final String CONFIG_MAX_ROUND = "selection_rounds";

    // 学生预选轮配置（轮次值=9）
    private static final String CONFIG_STUDENT_PRE_START = "student_pre_start";
    private static final String CONFIG_STUDENT_PRE_END = "student_pre_end";

    // 第一轮配置（导师选择）
    private static final String CONFIG_FIRST_ROUND_START = "first_round_start";
    private static final String CONFIG_FIRST_ROUND_END_TUTOR = "first_round_end_tutor";
    // 第二轮配置（导师选择）
    private static final String CONFIG_SECOND_ROUND_START = "second_round_start";
    private static final String CONFIG_SECOND_ROUND_END_TUTOR = "second_round_end_tutor";
    // 第三轮配置（导师选择）
    private static final String CONFIG_THIRD_ROUND_START = "third_round_start";
    private static final String CONFIG_ROUND_3_END_TUTOR = "round_3_end_tutor";
    // 补选配置
    private static final String CONFIG_SUPPLEMENTARY_START = "supplementary_start";
    private static final String CONFIG_SUPPLEMENTARY_END = "supplementary_end";

    // 轮次配置key映射：轮次号 -> (开始时间key, 截止时间key, 无)
    // 9: 学生预选轮 -> (开始时间, 学生截止时间, null)
    // 1/2/3: 导师轮 -> (开始时间, 导师截止时间, null)
    // 4: 补选 -> (开始时间, 结束时间, null)
    private static final java.util.Map<Integer, String[]> ROUND_CONFIG_KEYS = new java.util.HashMap<>();
    static {
        // 学生预选轮：只有学生截止时间
        ROUND_CONFIG_KEYS.put(9, new String[]{
            CONFIG_STUDENT_PRE_START,
            CONFIG_STUDENT_PRE_END,
            null
        });
        // 第一轮：只有导师截止时间
        ROUND_CONFIG_KEYS.put(1, new String[]{
            CONFIG_FIRST_ROUND_START,
            CONFIG_FIRST_ROUND_END_TUTOR,
            null
        });
        // 第二轮：只有导师截止时间
        ROUND_CONFIG_KEYS.put(2, new String[]{
            CONFIG_SECOND_ROUND_START,
            CONFIG_SECOND_ROUND_END_TUTOR,
            null
        });
        // 第三轮：只有导师截止时间
        ROUND_CONFIG_KEYS.put(3, new String[]{
            CONFIG_THIRD_ROUND_START,
            CONFIG_ROUND_3_END_TUTOR,
            null
        });
        // 补选：只有结束时间
        ROUND_CONFIG_KEYS.put(4, new String[]{
            CONFIG_SUPPLEMENTARY_START,
            CONFIG_SUPPLEMENTARY_END,
            null
        });
    }

    @Override
    public int getCurrentRound() {
        try {
            // 直接从配置获取
            SystemConfig config = systemConfigService.getConfigByKey(CONFIG_CURRENT_ROUND);
            if (config != null && config.getConfigValue() != null) {
                return Integer.parseInt(config.getConfigValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0; // 默认未开始
    }

    /**
     * 获取最大轮次配置
     */
    public int getMaxRound() {
        try {
            SystemConfig config = systemConfigService.getConfigByKey(CONFIG_MAX_ROUND);
            if (config != null && config.getConfigValue() != null) {
                return Integer.parseInt(config.getConfigValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 4; // 默认最大4轮
    }

    /**
     * 推进到下一轮
     */
    @Override
    @Transactional
    public boolean advanceToNextRound(String endTimeStudent, String endTimeTutor) {
        try {
            // 直接从数据库读取当前轮次，不经过时间自动判断逻辑
            int currentRound = readCurrentRoundDirectly();
            int maxRound = getMaxRound();

            int targetRound;
            // 如果当前是中间阶段值，直接推进到下一轮整数
            if (isIntermediatePhase(currentRound)) {
                targetRound = getTargetRoundFromIntermediate(currentRound);
            } else {
                targetRound = currentRound == 0 ? 1 : currentRound + 1;
            }

            // 校验：不能超过最大轮次，且不能超过4轮（只有4轮配置）
            if (targetRound > maxRound || targetRound > 4) {
                return false;
            }

            // 清空之前轮次的时间配置
            clearTimeConfigsForRoundsBefore(targetRound);

            // 获取目标轮次的配置key
            String[] configKeys = ROUND_CONFIG_KEYS.get(targetRound);
            if (configKeys == null) {
                return false;
            }

            // 设置开始时间为当前时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String nowTime = LocalDateTime.now().format(formatter);
            updateConfigValue(configKeys[0], nowTime);

            // 设置截止时间
            if (targetRound == 4) {
                // 补选只有一个结束时间
                updateConfigValue(configKeys[1], endTimeStudent);
            } else {
                // 普通轮次有学生和导师两个截止时间
                updateConfigValue(configKeys[1], endTimeStudent);
                if (configKeys[2] != null) {
                    updateConfigValue(configKeys[2], endTimeTutor);
                }
            }

            // 更新当前轮次
            return switchRound(targetRound);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 直接从数据库读取当前轮次，不经过时间自动判断
     */
    private int readCurrentRoundDirectly() {
        try {
            SystemConfig config = systemConfigService.getConfigByKey(CONFIG_CURRENT_ROUND);
            if (config != null && config.getConfigValue() != null) {
                return Integer.parseInt(config.getConfigValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 重置双选
     */
    @Override
    @Transactional
    public boolean resetRounds() {
        try {
            // 清空所有轮次的时间配置
            for (String[] configKeys : ROUND_CONFIG_KEYS.values()) {
                for (String key : configKeys) {
                    if (key != null) {
                        updateConfigValue(key, "");
                    }
                }
            }

            // 将当前轮次设置为0
            SystemConfig config = systemConfigService.getConfigByKey(CONFIG_CURRENT_ROUND);
            if (config == null) {
                config = new SystemConfig();
                config.setConfigKey(CONFIG_CURRENT_ROUND);
                config.setConfigName("当前轮次");
                config.setConfigType("selection");
            }
            config.setConfigValue("0");
            config.setUpdateTime(java.util.Calendar.getInstance().getTime());
            return systemConfigService.saveOrUpdate(config);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 清空指定轮次之前的所有时间配置
     */
    private void clearTimeConfigsForRoundsBefore(int targetRound) {
        for (int round = 1; round < targetRound; round++) {
            String[] configKeys = ROUND_CONFIG_KEYS.get(round);
            if (configKeys != null) {
                for (String key : configKeys) {
                    if (key != null) {
                        updateConfigValue(key, "");
                    }
                }
            }
        }
    }

    /**
     * 更新单个配置值
     */
    private void updateConfigValue(String configKey, String configValue) {
        SystemConfig config = systemConfigService.getConfigByKey(configKey);
        if (config != null) {
            config.setConfigValue(configValue);
            config.setUpdateTime(java.util.Calendar.getInstance().getTime());
            systemConfigService.saveOrUpdate(config);
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
                CONFIG_ENABLE_EXTRA_ROUND,
                // 学生预选轮
                CONFIG_STUDENT_PRE_START,
                CONFIG_STUDENT_PRE_END,
                // 第一轮（导师选择）
                CONFIG_FIRST_ROUND_START,
                CONFIG_FIRST_ROUND_END_TUTOR,
                // 第二轮（导师选择）
                CONFIG_SECOND_ROUND_START,
                CONFIG_SECOND_ROUND_END_TUTOR,
                // 第三轮（导师选择）
                CONFIG_THIRD_ROUND_START,
                CONFIG_ROUND_3_END_TUTOR,
                // 补选
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

        int currentRoundVal = getCurrentRound();
        stats.put("currentRound", currentRoundVal);

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

    /**
     * 判断当前学生是否可以选择（是否在学生选择时间内）
     */
    @Override
    public boolean canStudentSelect() {
        try {
            int round = readCurrentRoundDirectly();

            // 如果是中间阶段值，直接返回 false
            if (isIntermediatePhase(round)) {
                return false;
            }

            // 学生只能在学生预选轮(9)选择导师
            if (round != 9) {
                return false;
            }

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String[] configKeys = ROUND_CONFIG_KEYS.get(9);
            if (configKeys == null || configKeys.length < 2) {
                return false;
            }

            // 检查开始时间
            String startTimeStr = getConfigValue(configKeys[0]);
            if (startTimeStr == null || startTimeStr.isEmpty()) {
                return false;
            }
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, formatter);
            if (now.isBefore(startTime)) {
                return false;
            }

            // 检查学生截止时间
            String endTimeStr = getConfigValue(configKeys[1]);
            if (endTimeStr == null || endTimeStr.isEmpty()) {
                return false;
            }
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, formatter);
            return !now.isAfter(endTime);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断当前导师是否可以选择（是否在导师确认时间内）
     */
    @Override
    public boolean canMentorSelect() {
        try {
            int round = readCurrentRoundDirectly();

            // 如果是中间阶段值，直接返回 false
            if (isIntermediatePhase(round)) {
                return false;
            }

            // 导师只能在导师轮(1/2/3)选择学生
            if (round != 1 && round != 2 && round != 3 && round != 4) {
                return false;
            }

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String[] configKeys = ROUND_CONFIG_KEYS.get(round);
            if (configKeys == null || configKeys.length < 2) {
                return false;
            }

            // 检查开始时间
            String startTimeStr = getConfigValue(configKeys[0]);
            if (startTimeStr == null || startTimeStr.isEmpty()) {
                return false;
            }
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, formatter);
            if (now.isBefore(startTime)) {
                return false;
            }

            // 检查截止时间
            String endTimeStr = getConfigValue(configKeys[1]);
            if (endTimeStr == null || endTimeStr.isEmpty()) {
                return false;
            }
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, formatter);
            return !now.isAfter(endTime);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取当前阶段状态
     * @return 0-未开始，1-学生选择中，2-导师确认中，3-中间状态（等待推进），4-已结束
     */
    @Override
    public int getCurrentPhase() {
        try {
            int round = readCurrentRoundDirectly();

            // 如果已经是中间阶段值，直接返回等待推进状态
            if (isIntermediatePhase(round)) {
                return 3;
            }

            // 未开始：round = 0
            if (round == 0) {
                return 0; // 未开始
            }

            // 有效轮次：9(学生预选), 1/2/3(导师轮), 4(补选)
            if (round != 9 && round != 1 && round != 2 && round != 3 && round != 4) {
                return 4; // 已结束或无效值
            }

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String[] configKeys = ROUND_CONFIG_KEYS.get(round);
            if (configKeys == null || configKeys.length < 2) {
                return 0;
            }

            // 检查开始时间
            String startTimeStr = getConfigValue(configKeys[0]);
            if (startTimeStr == null || startTimeStr.isEmpty()) {
                return 0;
            }
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, formatter);
            if (now.isBefore(startTime)) {
                return 0; // 未开始
            }

            // 检查截止时间
            String endTimeStr = getConfigValue(configKeys[1]);
            if (endTimeStr != null && !endTimeStr.isEmpty()) {
                LocalDateTime endTime = LocalDateTime.parse(endTimeStr, formatter);
                if (!now.isAfter(endTime)) {
                    // 根据轮次判断阶段
                    if (round == 9) {
                        return 1; // 学生选择中
                    } else {
                        return 2; // 导师确认中
                    }
                }
            }

            // 截止时间已过，检查是否有下一轮
            int maxRound = getMaxRound();
            if (round < maxRound && round < 4) {
                // 不再自动更新轮次值，只返回等待推进状态
                // 由管理员手动推进轮次
                return 3; // 中间状态，等待推进
            }

            return 4; // 已结束
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 判断是否为中间阶段值
     */
    private boolean isIntermediatePhase(int round) {
        return round == 91 || round == 12 || round == 23 || round == 34;
    }

    /**
     * 从中间阶段值提取实际轮次（返回前一个轮次）
     */
    private int getActualRoundFromIntermediate(int intermediateRound) {
        if (intermediateRound == 91) return 9;
        if (intermediateRound == 12) return 1;
        if (intermediateRound == 23) return 2;
        if (intermediateRound == 34) return 3;
        return intermediateRound;
    }

    /**
     * 获取下一个中间阶段值
     */
    private int getNextIntermediatePhase(int round) {
        if (round == 9) return 91;
        if (round == 1) return 12;
        if (round == 2) return 23;
        if (round == 3) return 34;
        return round;
    }

    /**
     * 从中间阶段值获取目标轮次
     */
    private int getTargetRoundFromIntermediate(int intermediateRound) {
        if (intermediateRound == 91) return 1;
        if (intermediateRound == 12) return 2;
        if (intermediateRound == 23) return 3;
        if (intermediateRound == 34) return 4;
        return intermediateRound + 1;
    }

    /**
     * 获取用于数据库查询的实际轮次（处理中间阶段值）
     * @return 实际轮次（0,9,1,2,3,4）
     */
    @Override
    public int getQueryRound() {
        int round = readCurrentRoundDirectly();
        if (isIntermediatePhase(round)) {
            return getActualRoundFromIntermediate(round);
        }
        return round;
    }

    /**
     * 获取配置值
     */
    private String getConfigValue(String configKey) {
        SystemConfig config = systemConfigService.getConfigByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }
}
