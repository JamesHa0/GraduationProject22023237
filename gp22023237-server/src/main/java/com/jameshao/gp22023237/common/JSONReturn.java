package com.jameshao.gp22023237.common;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jameshao.gp22023237.utils.FLAGS;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/*
 * 返回json统一格式：
 * {
 *   result: success/error,
 *   data: [处理结果: 实体类对象/列表/字符串等类型](result是"success"时返回)
 *   error:[处理结果: 字符串](result是"error"时返回)
 * }
 * */

@Component
public class JSONReturn {

    /**
     * JavaScript Number.MAX_SAFE_INTEGER = 2^53 - 1 = 9007199254740991
     * 超出此范围的Long值在JSON.parse时会丢失精度，需序列化为字符串
     */
    private static final long JS_SAFE_INTEGER_LIMIT = 9007199254740991L;

    /**
     * fastjson2 ValueFilter：将超出JS安全整数范围的Long值序列化为字符串
     * 避免19位雪花ID经前端JSON.parse后精度丢失，导致updateById找不到记录
     */
    private static final ValueFilter LONG_SAFE_FILTER = (object, name, value) -> {
        if (value instanceof Long) {
            long longValue = (Long) value;
            if (longValue > JS_SAFE_INTEGER_LIMIT || longValue < -JS_SAFE_INTEGER_LIMIT) {
                return value.toString();
            }
        }
        return value;
    };

    public String returnSuccess() throws JsonProcessingException {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(FLAGS.RETURN_FLAG, FLAGS.RETURN_SUCCESS);
        resultMap.put(FLAGS.FAILED_MSG, null);
        resultMap.put(FLAGS.SUCCESS_MSG, null);
        return JSON.toJSONString(resultMap, LONG_SAFE_FILTER);
    }

    public String returnSuccess(Object obj) throws JsonProcessingException {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(FLAGS.RETURN_FLAG, FLAGS.RETURN_SUCCESS);
        resultMap.put(FLAGS.SUCCESS_MSG, obj);
        resultMap.put(FLAGS.FAILED_MSG, null);
        return JSON.toJSONString(resultMap, LONG_SAFE_FILTER);
    }

    public String returnFailed() throws JsonProcessingException {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(FLAGS.RETURN_FLAG, FLAGS.RETURN_FAILED);
        resultMap.put(FLAGS.SUCCESS_MSG, null);
        resultMap.put(FLAGS.FAILED_MSG, null);
        return JSON.toJSONString(resultMap, LONG_SAFE_FILTER);
    }

    public String returnFailed(String errorMsg) throws JsonProcessingException {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(FLAGS.RETURN_FLAG, FLAGS.RETURN_FAILED);
        resultMap.put(FLAGS.FAILED_MSG, errorMsg);
        resultMap.put(FLAGS.SUCCESS_MSG, null);
        return JSON.toJSONString(resultMap, LONG_SAFE_FILTER);
    }

    public String returnError(String errorMsg) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put(FLAGS.RETURN_FLAG, FLAGS.RETURN_FAILED);
            resultMap.put(FLAGS.FAILED_MSG, errorMsg);
            resultMap.put(FLAGS.SUCCESS_MSG, null);
            return JSON.toJSONString(resultMap, LONG_SAFE_FILTER);
        } catch (Exception e) {
            return "{\"result\":\"failed\",\"error\":\"" + errorMsg + "\"}";
        }
    }
}
