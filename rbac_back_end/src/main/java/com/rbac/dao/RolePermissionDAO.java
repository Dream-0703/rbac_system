package com.rbac.dao;

import com.rbac.model.entity.RolePermission;
import java.util.List;

/**
 * 角色-权限关联DAO接口
 */
public interface RolePermissionDAO {
    // 分配权限：新增角色-权限关联
    int insertRolePermission(RolePermission rolePermission);
    // 取消权限：删除角色-权限关联
    int deleteRolePermission(Integer roleId, Integer permissionId);
    // 查询角色的所有权限ID
    List<Integer> selectPermissionIdsByRoleId(Integer roleId);
    // 校验角色是否已分配该权限
    boolean existsRolePermission(Integer roleId, Integer permissionId);
    // 清空角色所有权限
    int deletePermissionsByRoleId(Integer roleId);
}