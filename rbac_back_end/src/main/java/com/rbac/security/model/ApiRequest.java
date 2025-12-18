package com.rbac.security.model;

import com.rbac.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * API请求参数封装（供Web接口和代理链使用）
 */
@Data
@Schema(description = "RBAC权限系统API请求参数", title = "ApiRequest")
public class ApiRequest {
    @Schema(
            description = "用户身份Token（登录后获取）",
            example = "eyJhbGciOiJIUzI1NiJ9.admin.123456789",
            required = true // 必填
    )
    private String token;          // 认证用

    @Schema(
            description = "要验证的权限编码（如system:user:list）",
            example = "system:user:list",
            required = true // 必填
    )
    private String permCode;       // 授权用

    @Schema(
            description = "业务参数JSON字符串（不同接口传对应参数）",
            example = "{\"pageNum\":1,\"pageSize\":10,\"username\":\"admin\"}",
            required = false // 可选
    )
    private String bizParams;      // 业务逻辑用

    @Schema(
            description = "请求时间戳（毫秒，默认自动填充）",
            example = "1735689600000",
            required = false // 可选
    )
    private Long timestamp;        // 参数校验用

    @Schema(
            description = "当前的用户对象",
            example = "{\"username\": \"admin\"}",
            required = true // 可选
    )
    private User user;
}