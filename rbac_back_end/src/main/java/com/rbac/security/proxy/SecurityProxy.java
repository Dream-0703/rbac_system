package com.rbac.security.proxy;

import com.rbac.security.model.ApiRequest;
import com.rbac.security.model.ApiResponse;

/**
 * 安全代理统一接口：所有代理（认证/授权/参数校验/日志）需实现该接口
 */
public interface SecurityProxy {
    // 执行代理逻辑 + 转发到下一个代理
    ApiResponse execute(ApiRequest request, SecurityChain chain);
}