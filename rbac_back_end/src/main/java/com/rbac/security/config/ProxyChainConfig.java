package com.rbac.security.config;

import com.rbac.security.core.RealBusinessService;
import com.rbac.security.proxy.SecurityChain;
import com.rbac.security.proxy.SecurityProxy;
import com.rbac.security.proxy.impl.AuthProxy;
import com.rbac.security.proxy.impl.AuthzProxy;
import com.rbac.security.proxy.impl.LogProxy;
import com.rbac.security.proxy.impl.ParamCheckProxy;
import com.rbac.service.PermissionService;
import com.rbac.service.RoleService;
import com.rbac.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.Arrays;
import java.util.List;

// 必须加这个注解才能打日志
@Slf4j
@Configuration
public class ProxyChainConfig {
    @Resource
    private LogProxy logProxy;
    @Resource
    private AuthProxy authProxy;
    @Resource
    private AuthzProxy authzProxy;
    @Resource
    private ParamCheckProxy paramCheckProxy;

    @Resource
    private UserService userService;
    @Resource
    private RoleService roleService;
    @Resource
    private PermissionService permissionService;

    // 核心修改后的完整方法（适配新的 SecurityChain，移除 index 相关）
    @Bean
    // 强制原型模式 + 代理模式（解决prototype不生效的坑）
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public SecurityChain securityChain() {
        List<SecurityProxy> proxies = Arrays.asList(
                logProxy,       // 1. 日志
                authProxy,      // 2. 认证（核心拦截）
                authzProxy,     // 3. 授权
                paramCheckProxy // 4. 参数校验
        );

        RealBusinessService realBusinessService = new RealBusinessService(
                userService,
                roleService,
                permissionService
        );

        // 关键修改：调用新的 build 方法（无 index 参数，适配改造后的 SecurityChain）
        SecurityChain newChain = SecurityChain.build(proxies, realBusinessService::execute);

        // 加日志：验证每次创建新实例（关键排查点）
        log.error("【SecurityChain创建】新实例hashCode：{}", newChain.hashCode());

        return newChain;
    }
}