package com.rbac.dao;

import com.rbac.model.entity.Permission;
import java.util.List;

/**
 * 权限DAO接口：定义权限数据的访问规范
 */
public interface PermissionDAO {
    // 1. 新增权限
    int insertPermission(Permission permission);

    // 2. 根据ID删除权限
    int deletePermissionById(Integer id);

    // 3. 更新权限信息
    int updatePermission(Permission permission);

    // 4. 根据ID查询权限
    Permission selectPermissionById(Integer id);

    // 5. 查询所有权限
    List<Permission> selectAllPermissions();

    // 6. （可选）根据父权限ID查询子权限（适配权限树结构）
    List<Permission> selectPermissionsByParentId(Integer parentId);

    // 7. （可选）根据权限编码查询（code是UNIQUE）
    Permission selectPermissionByCode(String code);
}