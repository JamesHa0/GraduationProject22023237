package com.jameshao.gp22023237.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jameshao.gp22023237.po.DictData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 字典缓存工具类
 * 按dictType分组缓存字典数据到Redis
 * key格式: system:dict:{dictType}
 * value格式: List<DictData>的JSON字符串
 */
@Component
public class DictUtil {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String DICT_CACHE_PREFIX = "system:dict:";

    private static final long CACHE_EXPIRE_DAYS = 7;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 从Redis获取指定类型的字典数据
     * @param dictType 字典类型
     * @return 字典数据列表，缓存未命中返回null
     */
    public List<DictData> getDictData(String dictType) {
        String json = redisTemplate.opsForValue().get(DICT_CACHE_PREFIX + dictType);
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<DictData>>() {});
        } catch (JsonProcessingException e) {
            // JSON解析失败，删除损坏的缓存
            redisTemplate.delete(DICT_CACHE_PREFIX + dictType);
            return null;
        }
    }

    /**
     * 更新指定类型的字典数据缓存
     * @param dictType 字典类型
     * @param dataList 字典数据列表
     */
    public void updateDictCache(String dictType, List<DictData> dataList) {
        try {
            String json = objectMapper.writeValueAsString(dataList);
            redisTemplate.opsForValue().set(DICT_CACHE_PREFIX + dictType, json, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化字典数据失败: " + dictType, e);
        }
    }

    /**
     * 清除指定类型的字典数据缓存
     * @param dictType 字典类型
     */
    public void clearDictCache(String dictType) {
        redisTemplate.delete(DICT_CACHE_PREFIX + dictType);
    }

    /**
     * 清除所有字典缓存
     */
    public void clearAllDictCache() {
        var keys = redisTemplate.keys(DICT_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
