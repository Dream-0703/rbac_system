package com.rbac.service;

import java.util.List;

/**
 * 系统内部统一的外部权限服务接口（适配器模式的目标接口）
 * 所有外部服务（LDAP/OAuth）必须通过适配器实现此接口，保证系统内部调用统一
 */
public interface IExternalAuthService {
    /**
     * 验证用户身份（外部服务→系统适配）
     * @param username 系统用户名
     * @param password 系统密码
     * @return true=验证通过
     */
    boolean verifyUser(String username, String password);

    /**
     * 获取用户权限列表（外部格式→系统权限编码格式）
     * @param username 系统用户名
     * @return 系统标准权限编码列表（如["user:list:view", "role:add"]）
     */
    List<String> getUserPermissions(String username);
}