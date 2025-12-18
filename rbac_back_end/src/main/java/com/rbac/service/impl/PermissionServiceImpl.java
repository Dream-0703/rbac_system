package com.rbac.service.impl;

import com.rbac.dao.DaoFactory;
import com.rbac.dao.DaoFactoryManager;
import com.rbac.dao.PermissionDAO;
import com.rbac.model.entity.Permission;
import com.rbac.service.PermissionService;
import com.rbac.util.PermissionTreeBuilder;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service // 注册为Spring Service
public class PermissionServiceImpl implements PermissionService {
    DaoFactory daoFactory;
    PermissionDAO permissionDAO;

    // 注入权限树构建器
    @Resource
    private PermissionTreeBuilder permissionTreeBuilder;

    // 构造方法初始化Dao
    public PermissionServiceImpl() {
        this.daoFactory = DaoFactoryManager.getDaoFactory();
        this.permissionDAO = daoFactory.createPermissionDAO();
    }

    @Override
    @Transactional // 事务注解：确保数据库操作和树更新原子性
    public boolean addPermission(Permission permission) {
        if (checkPermissionCodeExists(permission.getCode())) {
            return false;
        }
        boolean success = permissionDAO.insertPermission(permission) > 0;
        // 新增：同步更新权限树
        if (success) {
            permissionTreeBuilder.resetTree();
            System.out.println("📌 新增权限后，权限树已同步更新");
        }
        return success;
    }

    @Override
    @Transactional
    public boolean deletePermissionById(Integer permissionId) {
        if (checkHasChildPermissions(permissionId)) {
            return false;
        }
        boolean success = permissionDAO.deletePermissionById(permissionId) > 0;
        // 新增：同步更新权限树
        if (success) {
            permissionTreeBuilder.resetTree();
            System.out.println("📌 删除权限后，权限树已同步更新");
        }
        return success;
    }

    @Override
    @Transactional
    public boolean updatePermission(Permission permission) {
        if (permissionDAO.selectPermissionById(permission.getId()) == null) {
            return false;
        }
        boolean success = permissionDAO.updatePermission(permission) > 0;
        // 新增：同步更新权限树
        if (success) {
            permissionTreeBuilder.resetTree();
            System.out.println("📌 更新权限后，权限树已同步更新");
        }
        return success;
    }

    @Override
    public Permission getPermissionById(Integer permissionId) {
        return permissionDAO.selectPermissionById(permissionId);
    }

    @Override
    public List<Permission> getAllPermissions() {
        return permissionDAO.selectAllPermissions();
    }

    @Override
    public List<Permission> getPermissionsByParentId(Integer parentId) {
        return permissionDAO.selectPermissionsByParentId(parentId);
    }

    @Override
    public boolean checkPermissionCodeExists(String permissionCode) {
        return permissionDAO.selectPermissionByCode(permissionCode) != null;
    }

    @Override
    public boolean checkHasChildPermissions(Integer permissionId) {
        return !permissionDAO.selectPermissionsByParentId(permissionId).isEmpty();
    }
     @Override
    public boolean checkPermissionExistsByPermissionId(Integer permissionId) {
        return permissionDAO.selectPermissionById(permissionId) != null;
    }
}