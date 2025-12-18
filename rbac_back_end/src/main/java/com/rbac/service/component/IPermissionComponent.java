package com.rbac.service.component;

import java.util.List;

/**
 * 实验要求：统一的权限组件接口（组合模式的核心接口）
 * 叶子节点（单个权限）和复合节点（权限组）都要实现此接口，保证操作统一
 */
public interface IPermissionComponent {
    // 1. 权限验证：检查是否拥有该权限（实验要求的“权限验证”）
    boolean verifyPermission(String permissionCode);

    // 2. 获取权限信息：返回当前节点的权限编码/名称（实验要求的“权限获取”）
    String getPermissionCode();
    String getPermissionName();

    // 3. 子节点管理（复合节点需要，叶子节点可抛异常/空实现）
    // 新增子节点（实验要求的“子节点添加”）
    void addChild(IPermissionComponent child);
    // 删除子节点（实验要求的“子节点删除”）
    void removeChild(IPermissionComponent child);
    // 获取所有子节点（实验要求的“子节点遍历”）
    List<IPermissionComponent> getChildren();

    IPermissionComponent getParent(); // 获取父节点
    void setParent(IPermissionComponent parent); // 设置父节点

    void setPermissionCode(String permissionCode);
}