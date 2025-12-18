package com.rbac.dao;

import com.rbac.model.entity.UserRole;
import java.util.List;

/**
 * 用户-角色关联DAO接口（实验要求：多数据库适配的基础）
 */
public interface UserRoleDAO {
    // 分配角色：新增用户-角色关联
    int insertUserRole(UserRole userRole);
    // 取消角色：删除用户的角色关联
    int deleteUserRole(Integer userId);
    // 查询用户的角色ID
    Integer selectRoleIdsByUserId(Integer userId);
    // 校验用户是否已分配角色
    boolean existsUserRole(Integer userId);
}