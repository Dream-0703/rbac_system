package com.rbac.security.model;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一API响应结果模型（兼容原有调用逻辑 + 适配前端精细化错误处理）
 */
@Data
@Schema(name = "ApiResponse", description = "API响应结果")
public class ApiResponse {
    // ========== 原有核心字段（完全保留，兼容旧逻辑） ==========
    @Schema(description = "是否成功", example = "true")
    private boolean success;

    @Schema(description = "响应消息", example = "操作成功")
    private String msg;

    @Schema(description = "响应数据", example = "{}")
    private Object data;

    @Schema(description = "请求耗时（ms）", example = "50")
    private Long costTime;

    // ========== 新增前端对接核心字段 ==========
    @Schema(description = "业务错误码（前端判断场景）", example = "ROLE_NAME_DUPLICATE")
    private String errorCode;

    @Schema(description = "链路追踪ID（排查问题）", example = "REQ_1734567890123")
    private String traceId;

    @Schema(description = "响应时间戳", example = "2025-12-18T16:00:00")
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "扩展字段（存储辅助数据）", example = "{\"invalidPermCodes\":[\"system:user:delete\"]}")
    private Map<String, Object> ext = new HashMap<>();

    // ========== 原有构造器（完全保留，兼容 new ApiResponse(boolean, String, Object)） ==========
    public ApiResponse(boolean success, String msg, Object data) {
        this.success = success;
        this.msg = msg;
        this.data = data;
    }

    // ========== 新增构造器（适配错误码场景） ==========
    public ApiResponse(boolean success, String errorCode, String msg, Object data) {
        this.success = success;
        this.errorCode = errorCode;
        this.msg = msg;
        this.data = data;
    }

    // ========== 无参构造器（保留） ==========
    public ApiResponse() {
    }

    // ========== 原有快捷方法（保留，兼容旧调用） ==========
    public static ApiResponse success(Object data, Long costTime) {
        ApiResponse res = new ApiResponse(true, "操作成功", data);
        res.setCostTime(costTime);
        return res;
    }

    public static ApiResponse fail(String msg, Long costTime) {
        ApiResponse res = new ApiResponse(false, msg, null);
        res.setCostTime(costTime);
        return res;
    }

    // ========== 新增快捷方法（适配前端精细化错误） ==========
    public static ApiResponse success(String msg, Object data, String traceId) {
        ApiResponse res = new ApiResponse(true, null, msg, data);
        res.setTraceId(traceId);
        return res;
    }

    public static ApiResponse fail(String errorCode, String msg, String traceId) {
        ApiResponse res = new ApiResponse(false, errorCode, msg, null);
        res.setTraceId(traceId);
        return res;
    }

    public static ApiResponse fail(String errorCode, String msg, Object data, String traceId) {
        ApiResponse res = new ApiResponse(false, errorCode, msg, data);
        res.setTraceId(traceId);
        return res;
    }

    // ========== 链式设置（简化代码） ==========
    public ApiResponse traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public ApiResponse errorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public ApiResponse ext(Map<String, Object> ext) {
        this.ext = ext;
        return this;
    }
}