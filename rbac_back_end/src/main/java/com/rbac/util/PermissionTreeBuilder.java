package com.rbac.util;

import com.rbac.dao.DaoFactory;
import com.rbac.dao.PermissionDAO;
import com.rbac.dao.DaoFactoryManager;
import com.rbac.model.entity.Permission;
import com.rbac.service.component.CompositePermission;
import com.rbac.service.component.IPermissionComponent;
import com.rbac.service.component.LeafPermission;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限树构建工具类：Spring单例 + 启动自动加载 + 操作后同步更新
 */
@Component // 注册为Spring Bean
public class PermissionTreeBuilder {
    // 依赖你已有的PermissionDAO
    private PermissionDAO permissionDAO;
    // 缓存所有权限数据（按parent_id分组）
    private Map<Integer, List<Permission>> permissionGroupByParentId = new HashMap<>();
    // 缓存所有权限（按id映射）
    private Map<Integer, Permission> permissionMap = new HashMap<>();
    // 权限树根节点
    private IPermissionComponent root;

    // Spring初始化后自动执行（替代原有构造方法）
    @PostConstruct
    public void init() {
        DaoFactory daoFactory = DaoFactoryManager.getDaoFactory();
        this.permissionDAO = daoFactory.createPermissionDAO();
        // 加载所有权限数据并分组
        loadAllPermissions();
        // 构建完整权限树
        buildTree();
        System.out.println("✅ 权限树初始化完成，根节点：" + (root != null ? root.getPermissionName() : "无"));
    }

    /**
     * 第一步：加载所有权限数据，按parent_id分组+按id映射
     */
    private void loadAllPermissions() {
        // 1. 从数据库查询所有权限
        List<Permission> allPermissions = permissionDAO.selectAllPermissions();

        // 打印所有权限的id、code、parent_id
        System.out.println("===== 读取到的权限数据 =====");
        for (Permission perm : allPermissions) {
            System.out.println("id=" + perm.getId()
                    + ", code=" + perm.getCode()
                    + ", parent_id=" + perm.getParentId());
        }

        if (allPermissions.isEmpty()) {
            throw new RuntimeException("权限表无数据，无法构建权限树！");
        }

        // 2. 清空原有分组和映射
        permissionGroupByParentId.clear();
        permissionMap.clear();

        // 3. 按parent_id分组 + 按id映射权限
        for (Permission perm : allPermissions) {
            permissionMap.put(perm.getId(), perm);

            // 适配parent_id=0 作为顶级节点（映射为key=-1）
            Integer parentId = perm.getParentId() == 0 ? -1 : perm.getParentId();
            if (parentId == null) {
                parentId = -1;
            }

            permissionGroupByParentId.computeIfAbsent(parentId, k -> new ArrayList<>()).add(perm);
        }
    }

    /**
     * 第二步：递归构建权限树
     */
    private void buildTree() {
        // 根节点：parent_id=0 → key=-1
        List<Permission> rootPermissions = permissionGroupByParentId.get(-1);
        if (rootPermissions == null || rootPermissions.isEmpty()) {
            throw new RuntimeException("权限树无顶级节点（parent_id=0），无法构建！");
        }

        // 构建根复合节点
        Permission rootPerm = rootPermissions.get(0);
        this.root = new CompositePermission(rootPerm);
        // 递归构建根节点的所有子节点
        buildChildren((CompositePermission) root, rootPerm.getId());
    }

    /**
     * 递归构建子节点
     */
    private void buildChildren(CompositePermission parentNode, Integer parentId) {
        List<Permission> childPermissions = permissionGroupByParentId.get(parentId);
        if (childPermissions == null || childPermissions.isEmpty()) {
            return;
        }

        for (Permission childPerm : childPermissions) {
            // 新增日志：记录父子关系
            System.out.println("构建父子关系：父节点[" + parentNode.getPermissionCode() +
                    "] → 子节点[" + childPerm.getCode() + "]");

            IPermissionComponent childNode;
            boolean hasGrandChildren = permissionGroupByParentId.containsKey(childPerm.getId())
                    && !permissionGroupByParentId.get(childPerm.getId()).isEmpty();

            if (hasGrandChildren) {
                childNode = new CompositePermission(childPerm);
                buildChildren((CompositePermission) childNode, childPerm.getId());
            } else {
                childNode = new LeafPermission(childPerm);
            }
            childNode.setParent(parentNode);
            parentNode.addChild(childNode);
        }
    }

    /**
     * 对外提供：获取权限树的根节点
     */
    public IPermissionComponent getRoot() {
        return this.root;
    }

    /**
     * 对外提供：遍历权限树（测试用）
     */
    public void traverseTree(IPermissionComponent node, int level) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < level; i++) {
            indent.append("  ");
        }

        String nodeType = node instanceof LeafPermission ? "[叶子节点]" : "[复合节点]";
        System.out.println(indent + nodeType + " " + node.getPermissionName() + "(" + node.getPermissionCode() + ")");

        List<IPermissionComponent> children = node.getChildren();
        if (!children.isEmpty()) {
            for (IPermissionComponent child : children) {
                traverseTree(child, level + 1);
            }
        }
    }

    // 递归查找节点
    private IPermissionComponent findNodeRecursive(IPermissionComponent node, String targetCode) {
        if (node.getPermissionCode().equals(targetCode)) {
            return node;
        }
        for (IPermissionComponent child : node.getChildren()) {
            IPermissionComponent found = findNodeRecursive(child, targetCode);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    // 动态添加节点
    public IPermissionComponent addNode(String parentCode, Permission newPerm) {
        if (permissionMap.values().stream().anyMatch(p -> p.getCode().equals(newPerm.getCode()))) {
            System.out.println("⚠️ 权限编码「" + newPerm.getCode() + "」已存在，跳过插入");
            Permission existPerm = permissionMap.values().stream()
                    .filter(p -> p.getCode().equals(newPerm.getCode()))
                    .findFirst().get();
            return findNodeRecursive(root, existPerm.getCode());
        }

        IPermissionComponent parentNode = findNodeRecursive(root, parentCode);
        if (parentNode == null || !(parentNode instanceof CompositePermission)) {
            throw new IllegalArgumentException("父节点不存在或不是复合节点！");
        }

        IPermissionComponent newNode;
        boolean hasChildren = permissionGroupByParentId.containsKey(newPerm.getId())
                && !permissionGroupByParentId.get(newPerm.getId()).isEmpty();
        if (hasChildren) {
            newNode = new CompositePermission(newPerm);
        } else {
            newNode = new LeafPermission(newPerm);
        }

        newNode.setParent(parentNode);
        ((CompositePermission) parentNode).addChild(newNode);
        permissionDAO.insertPermission(newPerm);
        permissionMap.put(newPerm.getId(), newPerm);

        Integer parentId = newPerm.getParentId() == 0 ? -1 : newPerm.getParentId();
        if (parentId == null) {
            parentId = -1;
        }
        permissionGroupByParentId.computeIfAbsent(parentId, k -> new ArrayList<>()).add(newPerm);

        return newNode;
    }

    // 动态删除节点
    public void deleteNode(String nodeCode) {
        IPermissionComponent node = findNodeRecursive(root, nodeCode);
        if (node == null) {
            throw new IllegalArgumentException("节点不存在！");
        }

        IPermissionComponent parent = node.getParent();
        if (parent != null && parent instanceof CompositePermission) {
            ((CompositePermission) parent).removeChild(node);
        }

        Permission perm = (node instanceof LeafPermission)
                ? ((LeafPermission) node).getPermission()
                : ((CompositePermission) node).getPermission();
        permissionDAO.deletePermissionById(perm.getId());
        permissionMap.remove(perm.getId());

        Integer parentId = perm.getParentId() == 0 ? -1 : perm.getParentId();
        if (parentId == null) {
            parentId = -1;
        }
        permissionGroupByParentId.get(parentId).remove(perm);
    }

    // 动态移动节点
    public void moveNode(String nodeCode, String newParentCode) {
        IPermissionComponent node = findNodeRecursive(root, nodeCode);
        IPermissionComponent newParent = findNodeRecursive(root, newParentCode);
        if (node == null || newParent == null || !(newParent instanceof CompositePermission)) {
            throw new IllegalArgumentException("节点/新父节点不存在！");
        }

        IPermissionComponent oldParent = node.getParent();
        if (oldParent != null && oldParent instanceof CompositePermission) {
            ((CompositePermission) oldParent).removeChild(node);
        }

        node.setParent(newParent);
        ((CompositePermission) newParent).addChild(node);

        Permission perm = (node instanceof LeafPermission)
                ? ((LeafPermission) node).getPermission()
                : ((CompositePermission) node).getPermission();
        perm.setParentId(((CompositePermission) newParent).getPermission().getId());
        permissionDAO.updatePermission(perm);

        Integer oldParentId = perm.getParentId() == 0 ? -1 : perm.getParentId();
        if (oldParentId == null) {
            oldParentId = -1;
        }
        permissionGroupByParentId.get(oldParentId).remove(perm);

        Integer newParentId = ((CompositePermission) newParent).getPermission().getId();
        permissionGroupByParentId.computeIfAbsent(newParentId, k -> new ArrayList<>()).add(perm);
    }

    /**
     * 核心：重置权限树（增删改后同步更新）
     */
    public synchronized void resetTree() {
        loadAllPermissions();
        buildTree();
        System.out.println("✅ 权限树已重置更新");
    }
}