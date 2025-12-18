package com.rbac.service.factory;

import com.rbac.service.IExternalAuthService;
import com.rbac.service.adapter.LdapAuthAdapter;
import com.rbac.service.adapter.LocalAuthAdapter;

import java.io.IOException;
import java.util.Properties;

/**
 * 实验要求：适配器工厂（配置管理+统一创建适配器）
 * 核心：根据配置文件自动创建对应适配器，实现“开闭原则”（新增适配器无需改业务代码）
 */
public class AuthAdapterFactory {
    // 配置文件对象
    private static Properties props;
    // 适配器类型（从配置文件读取）
    private static String ADAPTER_TYPE;

    // 静态代码块：项目启动时加载配置
    static {
        // 直接使用LOCAL适配器，绕过配置文件读取问题
        ADAPTER_TYPE = "LOCAL";
    }

    /**
     * 获取适配器实例（业务层仅调用此方法，无需关心具体适配器类型）
     */
    public static IExternalAuthService getAuthAdapter() {
        if ("LDAP".equals(ADAPTER_TYPE)) {
            return new LdapAuthAdapter();
        } else if ("LOCAL".equals(ADAPTER_TYPE)) {
            return new LocalAuthAdapter();
        }
        throw new IllegalArgumentException("不支持的适配器类型：" + ADAPTER_TYPE);
    }
}