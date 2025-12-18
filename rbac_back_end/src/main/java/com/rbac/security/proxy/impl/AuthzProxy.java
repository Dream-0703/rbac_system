package com.rbac.security.proxy.impl;

import com.rbac.model.entity.User;
import com.rbac.security.model.ApiRequest;
import com.rbac.security.model.ApiResponse;
import com.rbac.security.proxy.SecurityChain;
import com.rbac.security.proxy.SecurityProxy;
import com.rbac.service.PermissionTreeValidator;
import com.rbac.service.RoleService;
import com.rbac.service.UserService;
import com.rbac.service.component.IPermissionComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;

@Slf4j
@Component
public class AuthzProxy implements SecurityProxy {
    @Resource
    private UserService userService;
    @Resource
    private RoleService roleService;
    @Resource // 关键：注入Spring管理的实例
    private PermissionTreeValidator permissionTreeValidator;

    @Override
    public ApiResponse execute(ApiRequest request, SecurityChain chain) {
        log.info("执行授权校验...");
        String permCode = request.getPermCode();
        
        // 特殊处理：登录请求绕过授权检查
        if ("system:user:own:login".equals(permCode)) {
            log.info("登录请求，绕过授权检查");
            return chain.proceed(request);
        }
        
        User user = request.getUser();

        // 1. 空值校验（依赖认证代理已过滤非法用户）
        if (user == null || permCode == null || permCode.trim().isEmpty()) {
            log.warn("授权失败：用户/权限编码为空");
            return new ApiResponse(false, "授权失败：无权访问", null);
        }

        try {
            // 使用注入的validator构建权限树
            IPermissionComponent userPermTree = permissionTreeValidator.buildRealUserPermissionTree(user.getUsername());
            log.info("用户查询权限{}",permCode.trim());
            boolean hasPermission = userPermTree.verifyPermission(permCode.trim());

            if (hasPermission) {
                return chain.proceed(request);
            } else {
                log.warn("授权失败：用户{}无权限访问{}", user.getUsername(), permCode);
                return new ApiResponse(false, "授权失败：无权访问", null);
            }
        } catch (Exception e) {
            log.error("授权校验异常", e);
            return new ApiResponse(false, "授权失败：系统异常", null);
        }
    }
}