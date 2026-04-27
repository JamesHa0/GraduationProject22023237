package com.jameshao.gp22023237.config;

import com.jameshao.gp22023237.po.DictData;
import com.jameshao.gp22023237.service.DictDataService;
import com.jameshao.gp22023237.utils.DictUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统启动时加载字典数据到Redis缓存
 */
@Component
public class DictCacheInitializer implements CommandLineRunner {

    @Autowired
    private DictDataService dictDataService;

    @Autowired
    private DictUtil dictUtil;

    @Override
    public void run(String... args) throws Exception {
        // 1. 查询所有字典数据
        List<DictData> dictDataList = dictDataService.list();

        if (dictDataList != null && !dictDataList.isEmpty()) {
            // 2. 按dictType分组
            Map<String, List<DictData>> dictTypeMap = dictDataList.stream()
                    .filter(data -> data.getDictType() != null)
                    .collect(Collectors.groupingBy(DictData::getDictType));

            // 3. 按类型批量存入Redis
            dictTypeMap.forEach((dictType, dataList) -> {
                dictUtil.updateDictCache(dictType, dataList);
            });

            System.out.println("字典数据已加载到Redis缓存，共" + dictTypeMap.size() + "种字典类型，" + dictDataList.size() + "条数据");
        }
    }
}
