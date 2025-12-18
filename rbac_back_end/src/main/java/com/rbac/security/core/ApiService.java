package com.rbac.security.core;

import com.rbac.security.model.ApiRequest;
import com.rbac.security.model.ApiResponse;

/**
 * 统一业务接口，安全代理将围绕此接口织入。
 */
public interface ApiService {
    ApiResponse execute(ApiRequest request);
}

