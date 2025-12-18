package com.rbac.security.proxy;

import com.rbac.security.model.ApiRequest;
import com.rbac.security.model.ApiResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 标准责任链实现：索引推进 + 业务执行器（解决重复执行核心）
 */
@RequiredArgsConstructor
public class SecurityChain {
    // 代理列表（如LogProxy、AuthProxy）
    @Getter
    private final List<SecurityProxy> proxies;
    // 真实业务执行器
    @Getter
    private final RealBusinessExecutor executor;
    // 当前执行索引（核心：避免重复执行代理）
    private int currentIndex = 0;

    /**
     * 核心推进方法：执行下一个代理，或执行业务
     */
    public ApiResponse proceed(ApiRequest request) {
        // 1. 索引未到末尾 → 执行下一个代理
        if (currentIndex < proxies.size()) {
            SecurityProxy currentProxy = proxies.get(currentIndex);
            currentIndex++; // 索引+1，推进到下一个代理（关键！）
            return currentProxy.execute(request, this); // 让当前代理执行，并传入链
        }

        // 2. 所有代理执行完 → 执行真实业务（仅1次）
        return executor.execute(request);
    }

    /**
     * 重置索引（复用链时调用，比如多请求场景）
     */
    public void reset() {
        currentIndex = 0;
    }

    /**
     * 静态构建器
     */
    public static SecurityChain build(List<SecurityProxy> proxies, RealBusinessExecutor executor) {
        return new SecurityChain(proxies, executor);
    }

    /**
     * 真实业务执行器
     */
    @FunctionalInterface
    public interface RealBusinessExecutor {
        ApiResponse execute(ApiRequest request);
    }
}