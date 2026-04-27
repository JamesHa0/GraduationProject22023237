package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.DictData;
import com.jameshao.gp22023237.po.DictType;
import com.jameshao.gp22023237.service.DictDataService;
import com.jameshao.gp22023237.service.DictTypeService;
import com.jameshao.gp22023237.mapper.DictTypeMapper;
import com.jameshao.gp22023237.utils.DictUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author test
* @description 针对表【sys_dict_type(字典类型表)】的数据库操作Service实现
* @createDate 2025-01-01 00:00:00
*/
@Service
public class DictTypeServiceImpl extends ServiceImpl<DictTypeMapper, DictType>
    implements DictTypeService{

    @Autowired
    private DictDataService dictDataService;

    @Autowired
    private DictUtil dictUtil;

    @Override
    public void deleteDictTypeById(Long dictId) {
        // 1. 获取字典类型信息，得到dictType值
        DictType dictType = this.getById(dictId);
        if (dictType == null) {
            return;
        }
        String dictTypeStr = dictType.getDictType();

        // 2. 级联删除该类型下所有字典数据
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictData::getDictType, dictTypeStr);
        dictDataService.remove(queryWrapper);

        // 3. 清理该类型的Redis缓存
        dictUtil.clearDictCache(dictTypeStr);

        // 4. 删除字典类型本身
        this.removeById(dictId);
    }

    @Override
    public void refreshCache() {
        // 1. 清除所有字典缓存
        dictUtil.clearAllDictCache();

        // 2. 重新加载所有字典数据到缓存
        List<DictData> dictDataList = dictDataService.list();
        if (dictDataList != null && !dictDataList.isEmpty()) {
            dictDataList.stream()
                    .filter(data -> data.getDictType() != null)
                    .collect(java.util.stream.Collectors.groupingBy(DictData::getDictType))
                    .forEach((dictType, dataList) -> {
                        dictUtil.updateDictCache(dictType, dataList);
                    });
        }
    }
}
