package com.rbac.model.entity;

/**
 * 角色-权限关联实体（对应role_permission表）
 */
public class RolePermission {
    private Integer roleId;     // 角色ID
    private Integer permissionId; // 权限ID

    // 构造+getter/setter
    public RolePermission() {}
    public RolePermission(Integer roleId, Integer permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
    public Integer getPermissionId() { return permissionId; }
    public void setPermissionId(Integer permissionId) { this.permissionId = permissionId; }
}