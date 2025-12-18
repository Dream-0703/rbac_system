package com.rbac.dao;

import com.rbac.dao.UserDAO;
import com.rbac.dao.RoleDAO;
import com.rbac.dao.PermissionDAO;

/**
 * 抽象工厂接口：定义创建所有DAO对象的规范
 * 是抽象工厂模式的核心，后续具体工厂（如MySQL工厂）需实现此接口
 */
//抽象工厂核心接口，对所有DAO接口的规范
public interface DaoFactory {
    // 创建UserDAO对象
    UserDAO createUserDAO();

    // 创建RoleDAO对象
    RoleDAO createRoleDAO();

    // 创建PermissionDAO对象
    PermissionDAO createPermissionDAO();
    // 创建UserRoleDAO对象
    UserRoleDAO createUserRoleDAO();

    // 创建RolePermissionDAO对象
    RolePermissionDAO createRolePermissionDAO();
}