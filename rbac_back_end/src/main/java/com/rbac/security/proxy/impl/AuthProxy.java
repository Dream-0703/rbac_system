package com.rbac.security.proxy.impl;

import com.rbac.model.entity.User;
import com.rbac.security.model.ApiRequest;
import com.rbac.security.model.ApiResponse;
import com.rbac.security.proxy.SecurityChain;
import com.rbac.security.proxy.SecurityProxy;
import com.rbac.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;

import java.util.regex.Pattern;

@Slf4j
@Component
public class AuthProxy implements SecurityProxy {
    @Resource
    private UserService userService;

    // 非法用户名正则（禁止特殊字符和空值）
    private static final Pattern ILLEGAL_USERNAME = Pattern.compile("[^a-zA-Z0-9_]");

    @Override
    public ApiResponse execute(ApiRequest request, SecurityChain chain) {
        log.info("执行认证校验...");
        log.info("请求参数：{}", request);
        
        // 特殊处理：登录请求绕过认证检查
        String permCode = request.getPermCode();
        if ("system:user:own:login".equals(permCode)) {
            log.info("登录请求，绕过认证检查");
            return chain.proceed(request);
        }
        
        User reqUser = request.getUser();

        // 1. 拦截空用户或空用户名
        if (reqUser == null || reqUser.getUsername() == null || reqUser.getUsername().trim().isEmpty()) {
            log.warn("认证失败：用户未登录或用户名为空");
            return new ApiResponse(false, "认证失败：请先登录", null);
        }

        String username = reqUser.getUsername().trim();

        // 2. 拦截含特殊字符的非法用户名（如???）
        if (ILLEGAL_USERNAME.matcher(username).find()) {
            log.warn("认证失败：用户名{}包含非法字符", username);
            return new ApiResponse(false, "认证失败：用户名格式非法", null);
        }

        // 3. 查库校验用户是否存在
        User dbUser = userService.getUserByUsername(username);
        if (dbUser == null) {
            log.error("认证失败：用户{}不存在于数据库", username);
            return new ApiResponse(false, "认证失败：用户不存在", null);
        }

        // 4. 认证通过，替换为数据库用户（防止伪造信息）
        request.setUser(dbUser);
        log.info("用户{}认证成功", username);
        return chain.proceed(request);
    }
}