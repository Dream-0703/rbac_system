package com.rbac.service.adapter;

import com.rbac.service.IExternalAuthService;

import java.util.ArrayList;
import java.util.List;

/**
 * 实验要求：LDAP外部权限服务适配器（适配器模式的核心实现）
 * 模拟LDAP服务逻辑，重点体现“外部接口→系统接口的适配+数据格式转换”
 */
public class LdapAuthAdapter implements IExternalAuthService {

    // 模拟LDAP服务的用户数据（代替真实LDAP查询）
    private static final String LDAP_TEST_USER = "ldap_test_user";
    private static final String LDAP_TEST_PWD = "ldap@123";
    // 模拟LDAP返回的角色 → 转换为系统标准权限编码（适配器核心：格式转换）
    private static final List<String> LDAP_USER_PERMS = List.of("user:list:view", "role:list:view");

    @Override
    public boolean verifyUser(String username, String password) {
        // 适配器逻辑1：系统用户名密码 → 适配LDAP的验证规则
        // 可故意抛异常测试降级：if (true) throw new RuntimeException("LDAP服务连接超时");
        return LDAP_TEST_USER.equals(username) && LDAP_TEST_PWD.equals(password);
    }

    @Override
    public List<String> getUserPermissions(String username) {
        // 适配器逻辑2：LDAP返回的原始数据 → 系统标准权限编码
        if (LDAP_TEST_USER.equals(username)) {
            return LDAP_USER_PERMS;
        }
        return new ArrayList<>();
    }
}