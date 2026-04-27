package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.DictData;
import com.jameshao.gp22023237.service.DictDataService;
import com.jameshao.gp22023237.mapper.DictDataMapper;
import com.jameshao.gp22023237.utils.DictUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author test
* @description 针对表【sys_dict_data(字典数据表)】的数据库操作Service实现
* @createDate 2025-01-01 00:00:00
*/
@Service
public class DictDataServiceImpl extends ServiceImpl<DictDataMapper, DictData>
    implements DictDataService{

    @Autowired
    private DictUtil dictUtil;

    @Override
    public List<DictData> getDictDataByType(String dictType) {
        if (dictType == null || dictType.isEmpty()) {
            return null;
        }

        // 1. 先从缓存获取
        List<DictData> cachedList = dictUtil.getDictData(dictType);
        if (cachedList != null) {
            return cachedList;
        }

        // 2. 缓存未命中，查数据库
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictData::getDictType, dictType)
                .eq(DictData::getStatus, "0")
                .orderByAsc(DictData::getDictSort);
        List<DictData> dataList = this.list(queryWrapper);

        // 3. 更新缓存
        if (dataList != null && !dataList.isEmpty()) {
            dictUtil.updateDictCache(dictType, dataList);
        }

        return dataList;
    }

    @Override
    public void addDictData(DictData dictData) {
        this.save(dictData);
        // 刷新该类型的缓存
        refreshDictCache(dictData.getDictType());
    }

    @Override
    public void updateDictData(DictData dictData) {
        // 先获取旧记录，用于判断dictType是否变更
        DictData oldData = this.getById(dictData.getDictCode());
        String oldDictType = oldData != null ? oldData.getDictType() : null;

        this.updateById(dictData);

        // 刷新新类型的缓存
        refreshDictCache(dictData.getDictType());

        // 如果dictType被修改，还需清理旧类型的缓存
        if (oldDictType != null && !oldDictType.equals(dictData.getDictType())) {
            refreshDictCache(oldDictType);
        }
    }

    @Override
    public void deleteDictDataById(Long dictCode) {
        // 先获取dictType，用于刷新缓存
        DictData dictData = this.getById(dictCode);
        if (dictData == null) {
            return;
        }
        String dictType = dictData.getDictType();
        this.removeById(dictCode);
        // 刷新该类型的缓存
        refreshDictCache(dictType);
    }

    @Override
    public void refreshDictCache(String dictType) {
        if (dictType == null || dictType.isEmpty()) {
            return;
        }
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictData::getDictType, dictType)
                .orderByAsc(DictData::getDictSort);
        List<DictData> dataList = this.list(queryWrapper);
        dictUtil.updateDictCache(dictType, dataList);
    }
}
