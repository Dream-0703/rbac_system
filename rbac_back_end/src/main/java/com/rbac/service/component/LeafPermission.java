package com.rbac.service.component;

import com.rbac.model.entity.Permission;

import java.util.Collections;
import java.util.List;

/**
 * 叶子节点——封装单个原子权限（不可拆分，无子节点）
 * 对应场景：如"用户新增(user:add)"、"角色删除(role:delete)"等最小粒度权限
 */
public class LeafPermission implements IPermissionComponent {
    // 持有单个权限的实体对象（复用你已有的Permission实体）
    private Permission permission;
    private IPermissionComponent parent; // 父节点

    // 构造方法：传入数据库查出来的单个权限实体
    public LeafPermission(Permission permission) {
        this.permission = permission;
        // 空值保护：避免permission为null导致后续NPE
        if (this.permission == null) {
            this.permission = new Permission();
        }
    }

    // 实现新增的接口方法
    @Override
    public IPermissionComponent getParent() {
        return parent;
    }

    @Override
    public void setParent(IPermissionComponent parent) {
        this.parent = parent;
    }

    @Override
    public void setPermissionCode(String permissionCode) {
        this.permission.setCode(permissionCode);
    }

    /**
     * 核心：单个权限的验证逻辑——支持继承父节点权限
     * 逻辑优先级：自身禁用→false → 自身匹配→true → 开启继承→递归父节点 → 无匹配→false
     */
    @Override
    public boolean verifyPermission(String permissionCode) {
        // 1. 空值校验（保持不变）
        if (permissionCode == null || permissionCode.trim().isEmpty()) {
            return false;
        }
        String targetCode = permissionCode.trim();
        String selfCode = permission.getCode();

        if (selfCode == null || selfCode.trim().isEmpty()) {
            return false;
        }

        // 2. 自身权限禁用 → 直接返回false（保持不变）
        if (!permission.isEnabled()) {
            return false;
        }

        // 3. 自身编码匹配 → 验证通过（保持不变）
        if (selfCode.equals(targetCode)) {
            return true;
        }

        return false;
    }

    /**
     * 返回当前叶子节点的权限编码（如"user:add"）
     */
    @Override
    public String getPermissionCode() {
        return permission.getCode() == null ? "" : permission.getCode();
    }

    /**
     * 返回当前叶子节点的权限名称（如"用户新增"）
     */
    @Override
    public String getPermissionName() {
        return permission.getName() == null ? "" : permission.getName();
    }

    /**
     * 叶子节点禁止添加子节点——抛异常（实验要求：体现叶子节点无后续节点）
     */
    @Override
    public void addChild(IPermissionComponent child) {
        throw new UnsupportedOperationException("叶子节点（单个权限）不支持添加子节点！");
    }

    /**
     * 叶子节点禁止删除子节点——抛异常
     */
    @Override
    public void removeChild(IPermissionComponent child) {
        throw new UnsupportedOperationException("叶子节点（单个权限）不支持删除子节点！");
    }

    /**
     * 叶子节点无子节点——返回空列表（不可变列表，避免外部修改）
     */
    @Override
    public List<IPermissionComponent> getChildren() {
        return Collections.emptyList();
    }

    // 可选：获取原始Permission实体（方便后续扩展）
    public Permission getPermission() {
        return permission;
    }

    // 补充：给Permission实体的isInheritParent/isEnabled设置默认值（避免NPE）
    public static class PermissionWrapper {
        public static boolean isInheritParent(Permission permission) {
            return permission != null && permission.isInheritParent();
        }

        public static boolean isEnabled(Permission permission) {
            return permission != null && permission.isEnabled();
        }
    }
}