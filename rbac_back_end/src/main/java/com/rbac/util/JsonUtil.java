package com.rbac.util;// 在JsonUtil.java中添加这个方法（核心！）
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 原有方法（支持Class类型）
    public static <T> T parseJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON解析失败", e);
        }
    }

    // 新增方法（支持TypeReference，适配Map<String, String>）
    public static <T> T parseJson(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            throw new RuntimeException("JSON解析失败", e);
        }
    }
}