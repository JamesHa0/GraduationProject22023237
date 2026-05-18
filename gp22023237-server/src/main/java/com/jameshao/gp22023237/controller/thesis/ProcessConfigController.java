package com.jameshao.gp22023237.controller.thesis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.ProcessConfig;
import com.jameshao.gp22023237.po.SystemConfig;
import com.jameshao.gp22023237.annotation.Log;
import com.jameshao.gp22023237.common.enums.BusinessType;
import com.jameshao.gp22023237.service.NoticeService;
import com.jameshao.gp22023237.service.ProcessConfigService;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.SystemConfigService;
import com.jameshao.gp22023237.service.TeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 流程配置管理控制器
 * 路径前缀: /thesis/config
 */
@RestController
@RequestMapping("/thesis/config")
public class ProcessConfigController {

    /** 学位流程全局配置的类型标识 */
    private static final String THESIS_CONFIG_TYPE = "thesis";

    private static final Logger logger = LoggerFactory.getLogger(ProcessConfigController.class);

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private ProcessConfigService processConfigService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @GetMapping("/list")
    public String getList() {
        try {
            List<ProcessConfig> list = processConfigService.list(
                    new LambdaQueryWrapper<ProcessConfig>().orderByAsc(ProcessConfig::getSort));
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @Log(title = "流程配置", businessType = BusinessType.UPDATE)
    @PostMapping("/save")
    public String save(@RequestBody List<ProcessConfig> configs) {
        try {
            processConfigService.batchSaveConfigs(configs);
            // 2.10 通知：论文截止时间变更 → 通知相关学生+导师
            try {
                for (ProcessConfig config : configs) {
                    if (config.getDeadline() != null) {
                        String processDesc = config.getProcessName() != null ? config.getProcessName() : "论文环节";
                        String deadlineStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(config.getDeadline());
                        // 通知所有有论文的学生和导师
                        List<com.jameshao.gp22023237.po.Student> students = studentService.list();
                        List<Long> userIds = new java.util.ArrayList<>();
                        for (com.jameshao.gp22023237.po.Student s : students) {
                            if (s.getUserId() != null && (s.getSelectionStatus() == null || s.getSelectionStatus() == 3)) {
                                userIds.add(s.getUserId());
                            }
                        }
                        if (!userIds.isEmpty()) {
                            noticeService.createAndPushToUsers(userIds, "论文截止时间变更通知",
                                "【" + processDesc + "】截止时间已调整为" + deadlineStr, "1");
                        }
                        // 通知导师
                        List<com.jameshao.gp22023237.po.Teacher> teachers = teacherService.list(
                            new LambdaQueryWrapper<com.jameshao.gp22023237.po.Teacher>()
                                .eq(com.jameshao.gp22023237.po.Teacher::getIsMentor, 1));
                        List<Long> teacherUserIds = new java.util.ArrayList<>();
                        for (com.jameshao.gp22023237.po.Teacher t : teachers) {
                            if (t.getUserId() != null) teacherUserIds.add(t.getUserId());
                        }
                        if (!teacherUserIds.isEmpty()) {
                            noticeService.createAndPushToUsers(teacherUserIds, "论文截止时间变更通知",
                                "【" + processDesc + "】截止时间已调整为" + deadlineStr, "1");
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("论文截止时间变更通知推送失败: {}", e.getMessage());
            }
            return jsonReturn.returnSuccess("保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/global")
    public String getGlobal() {
        try {
            List<SystemConfig> list = systemConfigService.list(
                    new LambdaQueryWrapper<SystemConfig>()
                            .eq(SystemConfig::getConfigType, THESIS_CONFIG_TYPE));
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @Log(title = "流程配置", businessType = BusinessType.UPDATE)
    @PostMapping("/global/save")
    public String saveGlobal(@RequestBody List<Map<String, String>> configs) {
        try {
            for (Map<String, String> item : configs) {
                String configKey = item.get("configKey");
                String configValue = item.get("configValue");
                String configName = item.get("configName");
                String configDesc = item.get("configDesc");

                SystemConfig existing = systemConfigService.getConfigByKey(configKey);
                if (existing != null) {
                    existing.setConfigValue(configValue);
                    if (configName != null) existing.setConfigName(configName);
                    if (configDesc != null) existing.setRemark(configDesc);
                    systemConfigService.updateConfig(existing);
                } else {
                    SystemConfig newConfig = new SystemConfig();
                    newConfig.setConfigKey(configKey);
                    newConfig.setConfigValue(configValue);
                    newConfig.setConfigName(configName != null ? configName : configDesc);
                    newConfig.setConfigType(THESIS_CONFIG_TYPE);
                    newConfig.setRemark(configDesc);
                    systemConfigService.save(newConfig);
                }
            }
            return jsonReturn.returnSuccess("保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
