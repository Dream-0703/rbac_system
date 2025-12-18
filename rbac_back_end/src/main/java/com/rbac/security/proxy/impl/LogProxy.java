package com.rbac.security.proxy.impl;

import com.rbac.security.model.ApiRequest;
import com.rbac.security.model.ApiResponse;
import com.rbac.security.proxy.SecurityChain;
import com.rbac.security.proxy.SecurityProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 安全代理1：日志代理（记录请求/响应全链路日志）
 * 特点：必须放在调用链第一个，保证所有请求都被记录
 */
@Slf4j
@Component
public class LogProxy implements SecurityProxy {
    @Override
    public ApiResponse execute(ApiRequest request, SecurityChain chain) {
        String requestId = "REQ_" + System.currentTimeMillis();
        log.info("【{}】请求开始 - 时间：{}，用户：{}，权限编码：{}，业务参数：{}",
                requestId, LocalDateTime.now(),
                request.getUser() != null ? request.getUser().getUsername() : "未登录",
                request.getPermCode(),
                request.getBizParams());

        long startTime = System.currentTimeMillis();
        try {
            ApiResponse response = chain.proceed(request);
            long costTime = System.currentTimeMillis() - startTime;
            // 关键修改：将 response.getMessage() 改为 response.getMsg()
            log.info("【{}】请求结束 - 耗时：{}ms，执行结果：{}，提示信息：{}",
                    requestId, costTime,
                    response.isSuccess() ? "成功" : "失败",
                    response.getMsg()); // 这里是修改点
            return response;
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("【{}】请求异常 - 耗时：{}ms，异常信息：{}",
                    requestId, costTime, e.getMessage(), e);
            return new ApiResponse(false, "系统异常：" + e.getMessage(), null);
        }
    }
}