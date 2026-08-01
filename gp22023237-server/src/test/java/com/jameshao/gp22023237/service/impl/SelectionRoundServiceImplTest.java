package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jameshao.gp22023237.po.MentorStudent;
import com.jameshao.gp22023237.po.SystemConfig;
import com.jameshao.gp22023237.service.MentorStudentService;
import com.jameshao.gp22023237.service.NoticeService;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.SystemConfigService;
import com.jameshao.gp22023237.service.TeacherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * SelectionRoundServiceImpl 核心状态转换单元测试
 * 覆盖：正常轮次推进、补选触发、非法状态转换拒绝
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SelectionRoundServiceImplTest {

    @InjectMocks
    private SelectionRoundServiceImpl selectionRoundService;

    @Mock
    private SystemConfigService systemConfigService;

    @Mock
    private MentorStudentService mentorStudentService;

    @Mock
    private StudentService studentService;

    @Mock
    private TeacherService teacherService;

    @Mock
    private NoticeService noticeService;

    private SystemConfig currentRoundConfig;
    private SystemConfig maxChoicesConfig;

    @BeforeEach
    void setUp() {
        currentRoundConfig = new SystemConfig();
        currentRoundConfig.setConfigKey("current_round");
        currentRoundConfig.setConfigValue("0");

        maxChoicesConfig = new SystemConfig();
        maxChoicesConfig.setConfigKey("student_max_choices");
        maxChoicesConfig.setConfigValue("3");
    }

    // ==================== 正常轮次推进 ====================

    @Test
    @DisplayName("正常推进：未开始(0) → 学生预选(9)")
    void advanceFromZeroToStudentPreselection() {
        // 当前轮次=0
        when(systemConfigService.getConfigByKey("current_round")).thenReturn(currentRoundConfig);
        when(systemConfigService.getConfigByKey("student_max_choices")).thenReturn(maxChoicesConfig);
        // 目标轮次9的配置key存在
        SystemConfig startConfig = new SystemConfig();
        startConfig.setConfigKey("student_select_start");
        startConfig.setConfigValue("");
        SystemConfig endConfig = new SystemConfig();
        endConfig.setConfigKey("student_select_end");
        endConfig.setConfigValue("");
        when(systemConfigService.getConfigByKey("student_select_start")).thenReturn(startConfig);
        when(systemConfigService.getConfigByKey("student_select_end")).thenReturn(endConfig);
        when(systemConfigService.saveOrUpdate(any(SystemConfig.class))).thenReturn(true);

        boolean result = selectionRoundService.advanceToNextRound("2026-08-01 12:00:00", null);

        assertTrue(result, "从0推进到9应成功");
        verify(systemConfigService, atLeastOnce()).saveOrUpdate(any(SystemConfig.class));
    }

    @Test
    @DisplayName("正常推进：学生预选(9) → 第一轮(1)")
    void advanceFromStudentPreselectionToRound1() {
        currentRoundConfig.setConfigValue("9");
        when(systemConfigService.getConfigByKey("current_round")).thenReturn(currentRoundConfig);
        when(systemConfigService.getConfigByKey("student_max_choices")).thenReturn(maxChoicesConfig);
        // 当前轮次9的结束时间配置（用于updateCurrentRoundEndTimeIfNeeded）
        SystemConfig selectEnd = new SystemConfig();
        selectEnd.setConfigKey("student_select_end");
        selectEnd.setConfigValue("2020-01-01 00:00:00"); // 已过期
        when(systemConfigService.getConfigByKey("student_select_end")).thenReturn(selectEnd);
        // 目标轮次1的配置
        SystemConfig round1Start = new SystemConfig();
        round1Start.setConfigKey("first_round_start");
        round1Start.setConfigValue("");
        SystemConfig round1End = new SystemConfig();
        round1End.setConfigKey("first_round_end_tutor");
        round1End.setConfigValue("");
        when(systemConfigService.getConfigByKey("first_round_start")).thenReturn(round1Start);
        when(systemConfigService.getConfigByKey("first_round_end_tutor")).thenReturn(round1End);
        when(systemConfigService.saveOrUpdate(any(SystemConfig.class))).thenReturn(true);

        boolean result = selectionRoundService.advanceToNextRound(null, "2026-08-05 12:00:00");

        assertTrue(result, "从9推进到1应成功");
    }

    @Test
    @DisplayName("正常推进：第一轮(1) → 第二轮(2)")
    void advanceFromRound1ToRound2() {
        currentRoundConfig.setConfigValue("1");
        when(systemConfigService.getConfigByKey("current_round")).thenReturn(currentRoundConfig);
        when(systemConfigService.getConfigByKey("student_max_choices")).thenReturn(maxChoicesConfig);
        // 当前轮次1的结束时间
        SystemConfig round1End = new SystemConfig();
        round1End.setConfigKey("first_round_end_tutor");
        round1End.setConfigValue("2020-01-01 00:00:00");
        when(systemConfigService.getConfigByKey("first_round_end_tutor")).thenReturn(round1End);
        // 目标轮次2的配置
        SystemConfig round2Start = new SystemConfig();
        round2Start.setConfigKey("second_round_start");
        round2Start.setConfigValue("");
        SystemConfig round2End = new SystemConfig();
        round2End.setConfigKey("second_round_end_tutor");
        round2End.setConfigValue("");
        when(systemConfigService.getConfigByKey("second_round_start")).thenReturn(round2Start);
        when(systemConfigService.getConfigByKey("second_round_end_tutor")).thenReturn(round2End);
        when(systemConfigService.saveOrUpdate(any(SystemConfig.class))).thenReturn(true);

        boolean result = selectionRoundService.advanceToNextRound(null, "2026-08-10 12:00:00");

        assertTrue(result, "从1推进到2应成功");
    }

    @Test
    @DisplayName("正常推进：第二轮(2) → 第三轮(3)")
    void advanceFromRound2ToRound3() {
        currentRoundConfig.setConfigValue("2");
        when(systemConfigService.getConfigByKey("current_round")).thenReturn(currentRoundConfig);
        when(systemConfigService.getConfigByKey("student_max_choices")).thenReturn(maxChoicesConfig);
        SystemConfig round2End = new SystemConfig();
        round2End.setConfigKey("second_round_end_tutor");
        round2End.setConfigValue("2020-01-01 00:00:00");
        when(systemConfigService.getConfigByKey("second_round_end_tutor")).thenReturn(round2End);
        SystemConfig round3Start = new SystemConfig();
        round3Start.setConfigKey("third_round_start");
        round3Start.setConfigValue("");
        SystemConfig round3End = new SystemConfig();
        round3End.setConfigKey("third_round_end_tutor");
        round3End.setConfigValue("");
        when(systemConfigService.getConfigByKey("third_round_start")).thenReturn(round3Start);
        when(systemConfigService.getConfigByKey("third_round_end_tutor")).thenReturn(round3End);
        when(systemConfigService.saveOrUpdate(any(SystemConfig.class))).thenReturn(true);

        boolean result = selectionRoundService.advanceToNextRound(null, "2026-08-15 12:00:00");

        assertTrue(result, "从2推进到3应成功");
    }

    // ==================== 补选触发 ====================

    @Test
    @DisplayName("补选触发：中间态(34) → 补选学生选择轮(7)")
    void advanceFromIntermediate34ToSupplementary7() {
        currentRoundConfig.setConfigValue("34");
        when(systemConfigService.getConfigByKey("current_round")).thenReturn(currentRoundConfig);
        when(systemConfigService.getConfigByKey("student_max_choices")).thenReturn(maxChoicesConfig);
        // 目标轮次7的配置
        SystemConfig suppStart = new SystemConfig();
        suppStart.setConfigKey("supplementary_student_start");
        suppStart.setConfigValue("");
        SystemConfig suppEnd = new SystemConfig();
        suppEnd.setConfigKey("supplementary_student_end");
        suppEnd.setConfigValue("");
        when(systemConfigService.getConfigByKey("supplementary_student_start")).thenReturn(suppStart);
        when(systemConfigService.getConfigByKey("supplementary_student_end")).thenReturn(suppEnd);
        when(systemConfigService.saveOrUpdate(any(SystemConfig.class))).thenReturn(true);

        boolean result = selectionRoundService.advanceToNextRound("2026-08-20 12:00:00", null);

        assertTrue(result, "从中间态34推进到补选轮7应成功");
    }

    @Test
    @DisplayName("补选触发：startSupplementaryRound 直接进入轮次7")
    void startSupplementaryRoundDirectly() {
        currentRoundConfig.setConfigValue("3");
        when(systemConfigService.getConfigByKey("current_round")).thenReturn(currentRoundConfig);
        // markUnmatchedStudentsForSupplementary 需要的 mock
        when(mentorStudentService.list(any(LambdaQueryWrapper.class))).thenReturn(java.util.Collections.emptyList());
        // 轮次7配置
        SystemConfig suppStart = new SystemConfig();
        suppStart.setConfigKey("supplementary_student_start");
        suppStart.setConfigValue("");
        SystemConfig suppEnd = new SystemConfig();
        suppEnd.setConfigKey("supplementary_student_end");
        suppEnd.setConfigValue("");
        when(systemConfigService.getConfigByKey("supplementary_student_start")).thenReturn(suppStart);
        when(systemConfigService.getConfigByKey("supplementary_student_end")).thenReturn(suppEnd);
        when(systemConfigService.saveOrUpdate(any(SystemConfig.class))).thenReturn(true);

        boolean result = selectionRoundService.startSupplementaryRound("2026-08-20 12:00:00", "2026-08-25 12:00:00");

        assertTrue(result, "startSupplementaryRound 应成功进入轮次7");
    }

    // ==================== 非法状态转换拒绝 ====================

    @Test
    @DisplayName("非法转换：超过最大轮次时 advanceToNextRound 返回 false")
    void advanceBeyondMaxRoundReturnsFalse() {
        // 当前轮次=3，最大轮次=3，目标=4 > maxRound → 拒绝
        currentRoundConfig.setConfigValue("3");
        when(systemConfigService.getConfigByKey("current_round")).thenReturn(currentRoundConfig);
        when(systemConfigService.getConfigByKey("student_max_choices")).thenReturn(maxChoicesConfig);
        // 当前轮次3的结束时间
        SystemConfig round3End = new SystemConfig();
        round3End.setConfigKey("third_round_end_tutor");
        round3End.setConfigValue("2020-01-01 00:00:00");
        when(systemConfigService.getConfigByKey("third_round_end_tutor")).thenReturn(round3End);

        boolean result = selectionRoundService.advanceToNextRound(null, "2026-08-20 12:00:00");

        assertFalse(result, "超过最大轮次3后推进到4应被拒绝");
    }

    @Test
    @DisplayName("非法转换：maxRound=1 时从轮次1推进到轮次2被拒绝")
    void advanceBeyondMaxRound1ReturnsFalse() {
        currentRoundConfig.setConfigValue("1");
        maxChoicesConfig.setConfigValue("1"); // 最大只有1轮
        when(systemConfigService.getConfigByKey("current_round")).thenReturn(currentRoundConfig);
        when(systemConfigService.getConfigByKey("student_max_choices")).thenReturn(maxChoicesConfig);
        SystemConfig round1End = new SystemConfig();
        round1End.setConfigKey("first_round_end_tutor");
        round1End.setConfigValue("2020-01-01 00:00:00");
        when(systemConfigService.getConfigByKey("first_round_end_tutor")).thenReturn(round1End);

        boolean result = selectionRoundService.advanceToNextRound(null, "2026-08-10 12:00:00");

        assertFalse(result, "maxRound=1时从1推进到2应被拒绝");
    }

    @Test
    @DisplayName("switchRound：正常切换轮次持久化成功")
    void switchRoundPersistsSuccessfully() {
        when(systemConfigService.getConfigByKey("current_round")).thenReturn(currentRoundConfig);
        when(systemConfigService.saveOrUpdate(any(SystemConfig.class))).thenReturn(true);

        boolean result = selectionRoundService.switchRound(2);

        assertTrue(result, "switchRound应成功持久化");
        verify(systemConfigService).saveOrUpdate(argThat(config ->
            "2".equals(((SystemConfig) config).getConfigValue())
        ));
    }

    @Test
    @DisplayName("switchRound：配置不存在时自动创建")
    void switchRoundCreatesConfigWhenMissing() {
        when(systemConfigService.getConfigByKey("current_round")).thenReturn(null);
        when(systemConfigService.saveOrUpdate(any(SystemConfig.class))).thenReturn(true);

        boolean result = selectionRoundService.switchRound(9);

        assertTrue(result, "配置不存在时switchRound应创建并成功");
        verify(systemConfigService).saveOrUpdate(argThat(config -> {
            SystemConfig sc = (SystemConfig) config;
            return "current_round".equals(sc.getConfigKey()) && "9".equals(sc.getConfigValue());
        }));
    }

    @Test
    @DisplayName("getCurrentRound：配置不存在时默认返回0")
    void getCurrentRoundDefaultsToZero() {
        when(systemConfigService.getConfigByKey("current_round")).thenReturn(null);

        int round = selectionRoundService.getCurrentRound();

        assertEquals(0, round, "配置不存在时应默认返回0");
    }
}
