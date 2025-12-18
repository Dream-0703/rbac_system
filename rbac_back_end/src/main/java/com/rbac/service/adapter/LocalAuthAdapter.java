package com.rbac.service.adapter;

import com.rbac.model.entity.User;
import com.rbac.service.IExternalAuthService;
import com.rbac.service.UserService;
import com.rbac.service.impl.UserServiceImpl;

import java.util.List;

/**
 * 实验要求：本地权限服务适配器（降级策略用）
 * 外部服务失败时，切换到本地业务层的权限逻辑
 */
public class LocalAuthAdapter implements IExternalAuthService {
    // 依赖你已完成的业务层
    private UserService userService = new UserServiceImpl();

    @Override
    public boolean verifyUser(String username, String password) {
        // 直接查询数据库验证用户，避免无限递归
        User user = userService.getUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return true;
        }
        return false;
    }

    @Override
    public List<String> getUserPermissions(String username) {
        // 适配本地业务层：调用你已写的用户权限查询方法
        return userService.getUserPermissionsByUsername(username);
    }
}