package com.jameshao.gp22023237.controller.system;

import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/config")
public class SystemConfig {

    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private JSONReturn jsonReturn;

    // 根据参数键名查询参数值
    @RequestMapping("/configKey/{configKey}")
    public String getConfigKey(@PathVariable String configKey){
        try {
            String configValue = systemConfigService.getConfigValue(configKey);
            return jsonReturn.returnSuccess(configValue);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

}
