package com.rbac.service.component;

import com.rbac.model.entity.Permission;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CompositePermission implements IPermissionComponent {
    private Permission permission;
    private IPermissionComponent parent;
    private List<IPermissionComponent> children = new ArrayList<>();

    public CompositePermission(Permission permission) {
        this.permission = permission;
        if (this.permission == null) {
            this.permission = new Permission();
        }
    }

    @Override
    public boolean verifyPermission(String permissionCode) {
        // 1. 空值+空格处理（解决编码空格问题）
        if (permissionCode == null || permissionCode.trim().isEmpty()) {
            return false;
        }
        String targetCode = permissionCode.trim(); // 去空格
        String selfCode = this.permission.getCode() == null ? "" : this.permission.getCode().trim();

        log.info("当前节点的selfcode:{}",selfCode);

        // 2. 自身权限禁用 → 直接失败
        if (!this.permission.isEnabled()) {
            log.info("当前节点的权限被禁用:{}",selfCode);
            return false;
        }

        // 3. 直接匹配自身编码（解决直接关联权限校验）
        if (selfCode.equals(targetCode)) {
            log.debug("直接匹配权限：{}", targetCode);
            return true;
        }

        // 4. 父权限包含子权限逻辑（解决父子继承问题）
        // 规则：如果目标权限是当前节点的子权限（如system:user:list是system:user的子），直接返回true
        if (targetCode.startsWith(selfCode + ":")) {
            log.debug("父权限{}包含子权限{}，校验通过", selfCode, targetCode);
            return true;
        }

        // 5. 递归检查子节点（子节点直接关联的权限也生效）
        if (children != null && !children.isEmpty()) {
            for (IPermissionComponent child : children) {
                log.info("当前节点的子节点:{}",child);
                if (child.verifyPermission(targetCode)) {
                    return true;
                }
            }
        }

        return false;
    }


    // 其他方法（getter/setter/addChild/removeChild等）保持不变
    @Override
    public String getPermissionCode() {
        return permission.getCode() == null ? "" : permission.getCode().trim();
    }

    @Override
    public String getPermissionName() {
        return permission.getName() == null ? "" : permission.getName();
    }

    @Override
    public void addChild(IPermissionComponent child) {
        children.add(child);
        child.setParent(this);
    }

    @Override
    public void removeChild(IPermissionComponent child) {
        children.remove(child);
        child.setParent(null);
    }

    @Override
    public List<IPermissionComponent> getChildren() {
        return new ArrayList<>(children); // 返回副本，避免外部修改
    }

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

    public Permission getPermission() {
        return permission;
    }
}