package com.jameshao.gp22023237.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.SystemConfig;
import com.jameshao.gp22023237.service.SystemConfigService;
import com.jameshao.gp22023237.utils.ConfigUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/system/config")
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private ConfigUtil configUtil;
    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 查询参数配置列表
     */
    @RequestMapping("/list")
    public String list(Integer pageNum, Integer pageSize, String configName, String configKey, String configType) {
        try {
            Page<SystemConfig> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
            LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(!ObjectUtils.isEmpty(configName), SystemConfig::getConfigName, configName)
                    .like(!ObjectUtils.isEmpty(configKey), SystemConfig::getConfigKey, configKey)
                    .eq(!ObjectUtils.isEmpty(configType), SystemConfig::getConfigType, configType);
            Page<SystemConfig> result = systemConfigService.page(page, queryWrapper);
            Map<String, Object> data = new HashMap<>();
            data.put("rows", result.getRecords());
            data.put("total", result.getTotal());
            return jsonReturn.returnSuccess(data);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 根据参数键名查询参数值
     */
    @RequestMapping("/configKey/{configKey}")
    public String getConfigKey(@PathVariable String configKey) {
        try {
            String configValue = systemConfigService.getConfigValue(configKey);
            return jsonReturn.returnSuccess(configValue);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取参数配置详细信息
     */
    @RequestMapping("/{configId}")
    public String getInfo(@PathVariable Long configId) {
        try {
            SystemConfig config = systemConfigService.getById(configId);
            return jsonReturn.returnSuccess(config);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 新增参数配置
     */
    @PostMapping
    public String add(@RequestBody SystemConfig config) {
        try {
            config.setUpdateTime(new Date());
            systemConfigService.save(config);
            // 更新缓存
            if (config.getConfigKey() != null) {
                configUtil.updateConfigCache(config.getConfigKey(), config.getConfigValue());
            }
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 修改参数配置
     */
    @PutMapping
    public String edit(@RequestBody SystemConfig config) {
        try {
            config.setUpdateTime(new Date());
            systemConfigService.updateConfig(config);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 删除参数配置
     */
    @DeleteMapping("/{configIds}")
    public String remove(@PathVariable Long[] configIds) {
        try {
            for (Long configId : configIds) {
                SystemConfig config = systemConfigService.getById(configId);
                if (config != null && config.getConfigKey() != null) {
                    configUtil.clearConfigCache(config.getConfigKey());
                }
                systemConfigService.removeById(configId);
            }
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 刷新参数缓存
     */
    @DeleteMapping("/refreshCache")
    public String refreshCache() {
        try {
            configUtil.clearAllConfigCache();
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
