package com.rbac.service.impl;

import com.rbac.dao.*;
import com.rbac.model.entity.Permission;
import com.rbac.model.entity.Role;
import com.rbac.model.entity.RolePermission;
import com.rbac.service.RoleService;
import com.rbac.service.component.CompositePermission;
import com.rbac.service.component.IPermissionComponent;
import com.rbac.service.component.LeafPermission;
import com.rbac.util.PermissionTreeBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service // 注册为Spring Service
public class RoleServiceImpl implements RoleService {
    private DaoFactory daoFactory;
    private RoleDAO roleDAO;
    private PermissionDAO permissionDAO;
    private RolePermissionDAO rolePermissionDAO;
    private PermissionTreeBuilder permissionTreeBuilder;
    // 构造方法：从DaoFactoryManager获取工厂实例
    public RoleServiceImpl() {
        this.daoFactory = DaoFactoryManager.getDaoFactory();
        this.roleDAO = daoFactory.createRoleDAO();
        this.permissionDAO = daoFactory.createPermissionDAO();
        this.rolePermissionDAO = daoFactory.createRolePermissionDAO();
        this.permissionTreeBuilder = new PermissionTreeBuilder();
    }

    @Override
    public boolean addRole(Role role) {
        // 校验角色名是否已存在
        if (checkRoleNameExists(role.getName())) {
            return false;
        }
        // 新增角色
        int result = roleDAO.insertRole(role);
        return result > 0;
    }

    @Override
    public boolean deleteRoleById(Integer roleId) {
        // 校验角色是否存在
        if (roleDAO.selectRoleById(roleId) == null) {
            return false;
        }
        // 删除角色
        int result = roleDAO.deleteRoleById(roleId);
        return result > 0;
    }

    @Override
    public boolean updateRole(Role role) {
        // 校验角色是否存在
        if (!checkRoleNameExists(role.getName())) {
            return false;
        }
        // 更新角色
        int result = roleDAO.updateRole(role);
        return result > 0;
    }

    @Override
    public Role getRoleById(Integer roleId) {
        return roleDAO.selectRoleById(roleId);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleDAO.selectAllRoles();
    }

    @Override
    public boolean checkRoleNameExists(String roleName) {
        Role role = roleDAO.selectRoleByRoleName(roleName);
        return role != null;
    }

    @Override
    public List<Permission> getPermissionsByRoleId(Integer roleId) {
        List<Integer> permissionIds = rolePermissionDAO.selectPermissionIdsByRoleId(roleId);
        if(permissionIds == null || permissionIds.size() == 0) {
            return null;
        }
        Set<Permission> permissions = new HashSet<Permission>();
        for (Integer permissionId : permissionIds) {
            Permission permission = permissionDAO.selectPermissionById(permissionId);
            if (permission != null) {
                permissions.add(permission);
            }
        }
        return new ArrayList<Permission>(permissions);
    }

    @Override
    public boolean assignPermissionsToRole(Integer roleId, List<Integer> permIdList) {
        // 校验角色是否存在
        if (roleDAO.selectRoleById(roleId) == null) {
            return false;
        }
        // 校验权限是否存在
        for (Integer permId : permIdList) {
            if (permissionDAO.selectPermissionById(permId) == null) {
                return false;
            }
        }
        // 清空角色原有权限
        rolePermissionDAO.deletePermissionsByRoleId(roleId);
        // 新增角色权限
        for (Integer permId : permIdList) {
            rolePermissionDAO.insertRolePermission(new RolePermission(roleId, permId));
        }
        return true;
    }

    @Override
    public boolean hasPermission(Integer roleId, String permissionCode) {
        // 1. 校验入参合法性
        if (roleId == null || permissionCode == null || permissionCode.trim().isEmpty()) {
            return false;
        }
        String targetCode = permissionCode.trim();

        // 2. 校验角色是否存在
        Role role = roleDAO.selectRoleById(roleId);
        if (role == null) {
            return false;
        }

        // 3. 获取角色关联的所有权限编码（直接验证编码，修复树遍历问题）
        List<Integer> permissionIds = rolePermissionDAO.selectPermissionIdsByRoleId(roleId);
        if (permissionIds.isEmpty()) {
            return false;
        }

        // 4. 直接验证权限编码是否匹配（支持通配符）
        for (Integer permId : permissionIds) {
            Permission permission = permissionDAO.selectPermissionById(permId);
            if (permission == null) continue;

            String permCode = permission.getCode();
            // 支持通配符匹配（如system:user:* 匹配 system:user:list）
            if (permCode.endsWith("*")) {
                String prefix = permCode.substring(0, permCode.length() - 1);
                if (targetCode.startsWith(prefix)) {
                    return true;
                }
            } else if (permCode.equals(targetCode)) {
                return true;
            }
        }

        return false;
    }
    /**
     * 辅助方法：从权限树中找到指定权限对应的节点（基于PermissionTreeBuilder的根节点遍历）
     */
    private IPermissionComponent findPermissionNodeInTree(String targetCode, Permission permission) {
        // 获取权限树根节点（通过Spring注入的PermissionTreeBuilder）
        IPermissionComponent root = permissionTreeBuilder.getRoot();
        if (root == null) {
            return null;
        }

        // 递归遍历权限树查找目标节点
        return traverseTreeForNode(root, targetCode, permission.getId());
    }
    /**
     * 递归遍历权限树，根据权限ID和编码查找节点
     */
    private IPermissionComponent traverseTreeForNode(IPermissionComponent currentNode, String targetCode, Integer permId) {
        // 1. 检查当前节点是否匹配（ID或编码匹配）
        Permission currentPerm = getPermissionFromComponent(currentNode);
        if (currentPerm != null && currentPerm.getId().equals(permId)) {
            return currentNode;
        }

        // 2. 递归遍历子节点
        for (IPermissionComponent child : currentNode.getChildren()) {
            IPermissionComponent foundNode = traverseTreeForNode(child, targetCode, permId);
            if (foundNode != null) {
                return foundNode;
            }
        }

        return null;
    }

    /**
     * 辅助方法：从组件中提取Permission实体（兼容Composite和Leaf）
     */
    private Permission getPermissionFromComponent(IPermissionComponent component) {
        if (component instanceof CompositePermission) {
            return ((CompositePermission) component).getPermission();
        } else if (component instanceof LeafPermission) {
            return ((LeafPermission) component).getPermission();
        }
        return null;
    }
    @Override
    public boolean checkRoleExistsByRoleId(Integer roleId) {
        return roleDAO.selectRoleById(roleId) != null;
    }
    @Override
    public boolean checkRoleExistsByRoleName(String roleName) {
        return roleDAO.selectRoleByRoleName(roleName) != null;
    }
}
