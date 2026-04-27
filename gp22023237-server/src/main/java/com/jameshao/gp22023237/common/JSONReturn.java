package com.jameshao.gp22023237.common;

import com.alibaba.fastjson.JSONObject;
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

    public String returnSuccess() throws JsonProcessingException {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(FLAGS.RETURN_FLAG, FLAGS.RETURN_SUCCESS);
        resultMap.put(FLAGS.FAILED_MSG, null);
        resultMap.put(FLAGS.SUCCESS_MSG, null);
        return JSONObject.toJSONString(resultMap);
    }

    public String returnSuccess(Object obj) throws JsonProcessingException {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(FLAGS.RETURN_FLAG, FLAGS.RETURN_SUCCESS);
        resultMap.put(FLAGS.SUCCESS_MSG, obj);
        resultMap.put(FLAGS.FAILED_MSG, null);
        return JSONObject.toJSONString(resultMap);
    }

    public String returnFailed() throws JsonProcessingException {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(FLAGS.RETURN_FLAG, FLAGS.RETURN_FAILED);
        resultMap.put(FLAGS.SUCCESS_MSG, null);
        resultMap.put(FLAGS.FAILED_MSG, null);
        return JSONObject.toJSONString(resultMap);
    }

    public String returnFailed(String errorMsg) throws JsonProcessingException {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(FLAGS.RETURN_FLAG, FLAGS.RETURN_FAILED);
        resultMap.put(FLAGS.FAILED_MSG, errorMsg);
        resultMap.put(FLAGS.SUCCESS_MSG, null);
        return JSONObject.toJSONString(resultMap);
    }

    public String returnError(String errorMsg) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put(FLAGS.RETURN_FLAG, FLAGS.RETURN_FAILED);
            resultMap.put(FLAGS.FAILED_MSG, errorMsg);
            resultMap.put(FLAGS.SUCCESS_MSG, null);
            return JSONObject.toJSONString(resultMap);
        } catch (Exception e) {
            return "{\"result\":\"failed\",\"error\":\"" + errorMsg + "\"}";
        }
    }
}
