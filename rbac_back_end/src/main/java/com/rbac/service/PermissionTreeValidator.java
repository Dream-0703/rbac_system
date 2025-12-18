package com.rbac.service;

import com.rbac.model.entity.Permission;
import com.rbac.service.component.IPermissionComponent;
import com.rbac.service.component.CompositePermission;
import com.rbac.service.impl.UserServiceImpl;
import com.rbac.util.PermissionTreeBuilder;
import com.rbac.util.PermissionCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component; // 注册为Spring组件

import java.util.*;

/**
 * 权限树验证机制——封装通用的权限验证/合法性校验方法
 * 核心场景：用户操作权限校验、权限树合法性检查、批量权限验证
 */
@Slf4j
@Component
public class PermissionTreeValidator {
    // 全局权限树（系统所有权限）
    private IPermissionComponent globalRoot;
    // 防循环引用：记录已遍历节点ID（避免递归死循环）
    private Set<String> traversedNodeCodes;

    // 缓存实例
    private PermissionCache permCache = new PermissionCache();
    // 关键：改为Spring注入，避免手动实例化
    @Autowired
    private UserService userService;
    @Autowired
    private PermissionTreeBuilder permissionTreeBuilder;

    // 权限树构建器实例（只定义一次，避免重复）
    private PermissionTreeBuilder treeBuilder;

    // 构造方法：初始化全局权限树
    @Autowired // 自动注入构造方法
    public PermissionTreeValidator(PermissionTreeBuilder treeBuilder) {
        this.treeBuilder = treeBuilder;
        this.globalRoot = treeBuilder.getRoot();
        this.traversedNodeCodes = new HashSet<>();
    }

    /**
     * 场景1：验证用户权限树是否包含目标权限（实验核心）
     * @param userPermissionRoot 用户的权限树根节点（用户分配的权限树）
     * @param targetCode 目标权限编码（如"user:delete"）
     * @return true=拥有该权限
     */
    public boolean verifyUserPermission(IPermissionComponent userPermissionRoot, String targetCode) {
        // 空值校验
        if (userPermissionRoot == null || targetCode == null || targetCode.trim().isEmpty()) {
            return false; // 改为返回false而非抛异常，更友好
        }
        String trimCode = targetCode.trim();
        // 调用节点自身的verifyPermission（组合模式核心：统一调用）
        return userPermissionRoot.verifyPermission(trimCode);
    }

    /**
     * 场景2：验证全局权限树是否包含目标权限（系统级校验）
     */
    public boolean verifyGlobalPermission(String targetCode) {
        return verifyUserPermission(this.globalRoot, targetCode);
    }

    /**
     * 场景3：批量验证——检查是否拥有权限组下的所有权限
     * @param root 权限树根节点
     * @param targetCodes 目标权限编码列表（如["user:add", "user:delete"]）
     * @return true=拥有所有权限
     */
    public boolean verifyAllPermissions(IPermissionComponent root, List<String> targetCodes) {
        if (targetCodes == null || targetCodes.isEmpty()) {
            return false;
        }
        for (String code : targetCodes) {
            if (!verifyUserPermission(root, code)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 场景4：批量验证——检查是否拥有权限组下的任意一个权限
     */
    public boolean verifyAnyPermission(IPermissionComponent root, List<String> targetCodes) {
        if (targetCodes == null || targetCodes.isEmpty()) {
            return false;
        }
        for (String code : targetCodes) {
            if (verifyUserPermission(root, code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 场景5：权限树合法性校验——检查是否有循环引用（实验要求：安全性校验）
     * @return true=权限树合法（无循环引用）
     */
    public boolean validateTreeLegality(IPermissionComponent root) {
        if (root == null) {
            throw new IllegalArgumentException("权限树根节点不能为空！");
        }
        // 每次校验前清空已遍历节点
        traversedNodeCodes.clear();
        try {
            checkCircularReference(root);
            return true;
        } catch (IllegalStateException e) {
            System.err.println("权限树存在循环引用：" + e.getMessage());
            return false;
        }
    }

    /**
     * 递归检查循环引用（核心：记录已遍历的节点编码，重复则说明循环）
     */
    private void checkCircularReference(IPermissionComponent node) {
        String nodeCode = node.getPermissionCode();
        // 空编码校验
        if (nodeCode == null || nodeCode.trim().isEmpty()) {
            throw new IllegalStateException("权限节点编码不能为空！");
        }
        nodeCode = nodeCode.trim();

        // 发现循环引用（同一节点编码被重复遍历）
        if (traversedNodeCodes.contains(nodeCode)) {
            throw new IllegalStateException("发现循环引用节点：" + nodeCode);
        }
        // 标记当前节点为已遍历
        traversedNodeCodes.add(nodeCode);

        // 递归检查子节点（仅复合节点有子节点）
        List<IPermissionComponent> children = node.getChildren();
        if (!children.isEmpty()) {
            for (IPermissionComponent child : children) {
                checkCircularReference(child);
            }
        }
        // 回溯：移除当前节点（不影响同层级其他节点校验）
        traversedNodeCodes.remove(nodeCode);
    }

    /**
     * 辅助方法：构建用户权限树（模拟用户分配的权限，实验测试用）
     * @param userRoot 用户的根权限组
     * @param assignedCodes 分配的权限编码列表
     */
    public void buildUserPermissionTree(CompositePermission userRoot, List<String> assignedCodes) {
        if (userRoot == null || assignedCodes == null) {
            return;
        }
        // 从全局树中查找对应权限，挂载到用户树
        for (String code : assignedCodes) {
            IPermissionComponent targetNode = findNodeInGlobalTree(code);
            if (targetNode != null) {
                userRoot.addChild(targetNode);
            }
        }
    }

    /**
     * 核心方法：根据用户名，从数据库构建用户的真实权限树
     * @param username 用户名
     * @return 用户的权限树根节点
     */
    // 核心修复：构建用户真实权限树（确保权限编码正确挂载）
    public IPermissionComponent buildRealUserPermissionTree(String username) {
        // 1. 查询用户直接关联的所有权限编码（去重+去空格）
        List<String> directPermCodes = userService.getUserPermissionsByUsername(username);
        Set<String> validPermCodes = new HashSet<>();
        for (String code : directPermCodes) {
            if (code != null && !code.trim().isEmpty()) {
                validPermCodes.add(code.trim()); // 去空格后存入Set，避免重复
            }
        }
        log.info("用户{}的有效直接权限:{}",username,validPermCodes);

        // 2. 获取全局权限树根节点
        IPermissionComponent globalRoot = permissionTreeBuilder.getRoot();

        // 3. 为用户构建专属权限树（只包含用户拥有的权限节点）
        CompositePermission userRoot = new CompositePermission(new Permission());
        userRoot.setPermissionCode(""); // 标记用户根节点

        // 4. 遍历用户权限，从全局树复制对应节点（包括子树）
        for (String permCode : validPermCodes) {
            IPermissionComponent targetNode = findNodeInGlobalTree(permCode);
            if (targetNode != null) {
                // 创建节点副本，避免修改全局树结构
                IPermissionComponent userNode = copyPermissionNode(targetNode);
                userRoot.addChild(userNode);
                log.info("✅ 成功给用户「{}」挂载权限：{}", username, permCode);
            } else {
                log.warn("❌ 用户「{}」的权限{}在全局树中不存在", username, permCode);
            }
        }

        return userRoot;
    }
    
    /**
     * 复制权限节点及其子树（避免修改全局树结构）
     */
    private IPermissionComponent copyPermissionNode(IPermissionComponent original) {
        if (original == null) {
            return null;
        }
        
        // 创建新的Permission对象
        Permission originalPerm = null;
        if (original instanceof CompositePermission) {
            originalPerm = ((CompositePermission) original).getPermission();
        } else if (original instanceof com.rbac.service.component.LeafPermission) {
            // 注意：需要根据实际LeafPermission类的名称调整
            originalPerm = ((com.rbac.service.component.LeafPermission) original).getPermission();
        }
        
        if (originalPerm == null) {
            return null;
        }
        
        // 创建权限副本
        Permission permCopy = new Permission();
        permCopy.setId(originalPerm.getId());
        permCopy.setCode(originalPerm.getCode());
        permCopy.setName(originalPerm.getName());
        permCopy.setParentId(originalPerm.getParentId());
        permCopy.setEnabled(originalPerm.isEnabled());
        permCopy.setInheritParent(originalPerm.isInheritParent());
        
        // 根据原节点类型创建新节点
        IPermissionComponent newNode;
        if (original instanceof CompositePermission) {
            newNode = new CompositePermission(permCopy);
            // 递归复制子节点
            for (IPermissionComponent child : original.getChildren()) {
                IPermissionComponent childCopy = copyPermissionNode(child);
                if (childCopy != null) {
                    ((CompositePermission) newNode).addChild(childCopy);
                }
            }
        } else {
            newNode = new com.rbac.service.component.LeafPermission(permCopy);
        }
        
        return newNode;
    }

    /**
     * 辅助方法：从全局树中查找指定编码的节点（增强版：支持模糊匹配+空值防护）
     */
    private IPermissionComponent findNodeInGlobalTree(String targetCode) {
        if (targetCode == null || targetCode.trim().isEmpty() || globalRoot == null) {
            return null;
        }
        String trimCode = targetCode.trim();
        IPermissionComponent found = findNodeRecursive(globalRoot, trimCode);
        if (found == null) {
            System.out.println("⚠️ 全局树中未找到权限编码：" + trimCode + "（当前全局编码：system/content等）");
        }
        return found;
    }

    /**
     * 递归查找节点的工具方法（增强版：修复递归逻辑+空值防护）
     */
    private IPermissionComponent findNodeRecursive(IPermissionComponent node, String targetCode) {
        // 空值防护
        if (node == null || node.getPermissionCode() == null) {
            return null;
        }

        // 当前节点匹配（忽略大小写+空格）
        String nodeCode = node.getPermissionCode().trim();
        if (nodeCode.equalsIgnoreCase(targetCode)) {
            return node;
        }

        // 递归查找子节点（所有节点都要查，包括叶子节点的父节点链）
        List<IPermissionComponent> children = node.getChildren();
        if (!children.isEmpty()) {
            for (IPermissionComponent child : children) {
                IPermissionComponent found = findNodeRecursive(child, targetCode);
                if (found != null) {
                    return found;
                }
            }
        }

        // 未找到
        return null;
    }

    /**
     * 新增：带缓存的用户权限验证
     */
    public boolean verifyUserPermissionWithCache(String username, String targetCode) {
        // 1. 从缓存获取用户权限树
        IPermissionComponent userTree = permCache.getUserPermTree(username, this);
        // 2. 验证权限
        return verifyUserPermission(userTree, targetCode);
    }

    // 新增：获取全局根节点（供外部测试使用）
    public IPermissionComponent getGlobalRoot() {
        return this.globalRoot;
    }
}