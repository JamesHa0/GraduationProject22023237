package com.jameshao.gp22023237.service.impl;

import com.jameshao.gp22023237.po.SystemConfig;
import com.jameshao.gp22023237.service.CoursePhaseService;
import com.jameshao.gp22023237.service.CourseSelectionService;
import com.jameshao.gp22023237.service.ScoreService;
import com.jameshao.gp22023237.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class CoursePhaseServiceImpl implements CoursePhaseService {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private CourseSelectionService courseSelectionService;

    @Autowired
    private ScoreService scoreService;

    // 配置键常量
    private static final String CONFIG_PHASE_STATUS = "course_phase_status";
    private static final String CONFIG_SELECTION_START = "course_selection_start";
    private static final String CONFIG_SELECTION_END = "course_selection_end";
    private static final String CONFIG_SCORE_ENTRY_START = "score_entry_start";
    private static final String CONFIG_SCORE_ENTRY_END = "score_entry_end";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 所有配置键
    private static final String[] ALL_CONFIG_KEYS = {
            CONFIG_PHASE_STATUS,
            CONFIG_SELECTION_START,
            CONFIG_SELECTION_END,
            CONFIG_SCORE_ENTRY_START,
            CONFIG_SCORE_ENTRY_END
    };

    @Override
    public int getCurrentPhase() {
        try {
            String value = getConfigValue(CONFIG_PHASE_STATUS);
            if (value != null && !value.isEmpty()) {
                return Integer.parseInt(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    @Transactional
    public boolean advancePhase(String endTime) {
        try {
            int currentPhase = getCurrentPhase();
            int targetPhase = currentPhase + 1;

            // 已结课状态不可推进
            if (currentPhase >= 4) {
                return false;
            }

            // 阶段0→1：开启选课阶段
            if (currentPhase == 0) {
                String now = LocalDateTime.now().format(FORMATTER);
                updateConfigValue(CONFIG_SELECTION_START, now);
                updateConfigValue(CONFIG_SELECTION_END, endTime);
            }
            // 阶段1→2：选课结束，进入已开课
            else if (currentPhase == 1) {
                // 将选课截止时间设为当前时间（如果还没到期）
                trimEndTimeIfNeeded(CONFIG_SELECTION_END);
            }
            // 阶段2→3：进入成绩录入阶段
            else if (currentPhase == 2) {
                String now = LocalDateTime.now().format(FORMATTER);
                updateConfigValue(CONFIG_SCORE_ENTRY_START, now);
                updateConfigValue(CONFIG_SCORE_ENTRY_END, endTime);
            }
            // 阶段3→4：成绩录入结束，已结课
            else if (currentPhase == 3) {
                trimEndTimeIfNeeded(CONFIG_SCORE_ENTRY_END);
            }

            // 更新阶段状态
            updateConfigValue(CONFIG_PHASE_STATUS, String.valueOf(targetPhase));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean resetPhase() {
        try {
            // 清空所有时间配置
            for (String key : ALL_CONFIG_KEYS) {
                if (!CONFIG_PHASE_STATUS.equals(key)) {
                    updateConfigValue(key, "");
                }
            }
            // 重置阶段为0
            updateConfigValue(CONFIG_PHASE_STATUS, "0");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateDeadline(String configKey, String configValue) {
        try {
            // 校验配置键合法性
            boolean validKey = CONFIG_SELECTION_END.equals(configKey)
                    || CONFIG_SCORE_ENTRY_END.equals(configKey);
            if (!validKey) {
                return false;
            }
            updateConfigValue(configKey, configValue);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Map<String, String> getPhaseConfig() {
        Map<String, String> config = new HashMap<>();
        for (String key : ALL_CONFIG_KEYS) {
            config.put(key, getConfigValue(key));
        }
        return config;
    }

    @Override
    public boolean isSelectionOpen() {
        try {
            int phase = getCurrentPhase();
            if (phase != 1) {
                return false;
            }
            return isWithinWindow(CONFIG_SELECTION_START, CONFIG_SELECTION_END);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isScoreEntryOpen() {
        try {
            int phase = getCurrentPhase();
            if (phase != 3) {
                return false;
            }
            return isWithinWindow(CONFIG_SCORE_ENTRY_START, CONFIG_SCORE_ENTRY_END);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Map<String, Object> getPhaseStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("currentPhase", getCurrentPhase());
        stats.put("isSelectionOpen", isSelectionOpen());
        stats.put("isScoreEntryOpen", isScoreEntryOpen());

        try {
            // 选课人数统计
            long selectionCount = courseSelectionService.count();
            stats.put("selectionCount", selectionCount);

            // 已录入成绩数
            long scoreCount = scoreService.count();
            stats.put("scoreCount", scoreCount);
        } catch (Exception e) {
            stats.put("selectionCount", 0);
            stats.put("scoreCount", 0);
        }

        return stats;
    }

    // ========== 私有方法 ==========

    private String getConfigValue(String configKey) {
        SystemConfig config = systemConfigService.getConfigByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    private void updateConfigValue(String configKey, String configValue) {
        SystemConfig config = systemConfigService.getConfigByKey(configKey);
        if (config != null) {
            config.setConfigValue(configValue != null ? configValue : "");
            config.setUpdateTime(java.util.Calendar.getInstance().getTime());
            systemConfigService.saveOrUpdate(config);
        } else {
            // 配置不存在，创建新配置
            config = new SystemConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue != null ? configValue : "");
            config.setConfigName(getConfigName(configKey));
            config.setConfigType("course_phase");
            config.setUpdateTime(java.util.Calendar.getInstance().getTime());
            systemConfigService.saveOrUpdate(config);
        }
    }

    private String getConfigName(String configKey) {
        switch (configKey) {
            case CONFIG_PHASE_STATUS: return "课程阶段状态";
            case CONFIG_SELECTION_START: return "选课开始时间";
            case CONFIG_SELECTION_END: return "选课截止时间";
            case CONFIG_SCORE_ENTRY_START: return "成绩录入开始时间";
            case CONFIG_SCORE_ENTRY_END: return "成绩录入截止时间";
            default: return configKey;
        }
    }

    /**
     * 判断当前时间是否在指定窗口内
     */
    private boolean isWithinWindow(String startKey, String endKey) {
        LocalDateTime now = LocalDateTime.now();
        String startStr = getConfigValue(startKey);
        String endStr = getConfigValue(endKey);

        if (startStr == null || startStr.isEmpty() || endStr == null || endStr.isEmpty()) {
            return false;
        }

        LocalDateTime startTime = LocalDateTime.parse(startStr, FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse(endStr, FORMATTER);

        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }

    /**
     * 如果截止时间还未到期，将其截断为当前时间
     */
    private void trimEndTimeIfNeeded(String endKey) {
        String endStr = getConfigValue(endKey);
        if (endStr == null || endStr.isEmpty()) {
            return;
        }
        LocalDateTime endTime = LocalDateTime.parse(endStr, FORMATTER);
        LocalDateTime now = LocalDateTime.now();
        if (endTime.isAfter(now)) {
            updateConfigValue(endKey, now.format(FORMATTER));
        }
    }
}
