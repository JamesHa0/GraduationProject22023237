package com.jameshao.gp22023237.controller.thesis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.ProcessConfig;
import com.jameshao.gp22023237.po.SystemConfig;
import com.jameshao.gp22023237.annotation.Log;
import com.jameshao.gp22023237.common.enums.BusinessType;
import com.jameshao.gp22023237.service.ProcessConfigService;
import com.jameshao.gp22023237.service.SystemConfigService;
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

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private ProcessConfigService processConfigService;

    @Autowired
    private SystemConfigService systemConfigService;

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
            for (ProcessConfig config : configs) {
                if (config.getId() != null) {
                    processConfigService.updateById(config);
                } else {
                    processConfigService.save(config);
                }
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
