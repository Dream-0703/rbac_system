package com.rbac.model.entity;

/**
 * 用户-角色关联实体（对应user_role表）
 */
public class UserRole {
    private Integer userId;  // 用户ID
    private Integer roleId;  // 角色ID

    // 构造+getter/setter
    public UserRole() {}
    public UserRole(Integer userId, Integer roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
}