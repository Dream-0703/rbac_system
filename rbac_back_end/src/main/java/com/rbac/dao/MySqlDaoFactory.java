package com.rbac.dao;

import com.rbac.dao.impl.*;

public class MySqlDaoFactory implements DaoFactory {
    @Override
    public UserDAO createUserDAO() {
        return new MySqlUserDaoImpl();
    }

    @Override
    public RoleDAO createRoleDAO() {
        return new MySqlRoleDaoImpl();
    }

    @Override
    public PermissionDAO createPermissionDAO() {
        return new MySqlPermissionDaoImpl();
    }

    @Override
    public UserRoleDAO createUserRoleDAO() {
        return new MySqlUserRoleDaoImpl();
    }

    @Override
    public RolePermissionDAO createRolePermissionDAO() {
        return new MySqlRolePermissionDaoImpl();
    }
}
