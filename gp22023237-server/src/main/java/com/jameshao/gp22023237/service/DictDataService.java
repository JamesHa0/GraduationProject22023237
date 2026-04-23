package com.jameshao.gp22023237.service;

import com.jameshao.gp22023237.po.DictData;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author test
* @description 针对表【sys_dict_data(字典数据表)】的数据库操作Service
* @createDate 2025-01-01 00:00:00
*/
public interface DictDataService extends IService<DictData> {

    /**
     * 根据字典类型查询字典数据（先查缓存，未命中查数据库）
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    List<DictData> getDictDataByType(String dictType);

    /**
     * 新增字典数据并刷新缓存
     * @param dictData 字典数据
     */
    void addDictData(DictData dictData);

    /**
     * 修改字典数据并刷新缓存
     * @param dictData 字典数据
     */
    void updateDictData(DictData dictData);

    /**
     * 删除字典数据并刷新缓存
     * @param dictCode 字典数据编码
     */
    void deleteDictDataById(Long dictCode);

    /**
     * 刷新指定类型的字典缓存
     * @param dictType 字典类型
     */
    void refreshDictCache(String dictType);
}
