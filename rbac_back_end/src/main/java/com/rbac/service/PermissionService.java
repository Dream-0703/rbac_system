package com.rbac.service;

import com.rbac.model.entity.Permission;
import java.util.List;

/**
 * 权限业务服务接口：定义权限相关的业务功能规范
 * 包含权限增删改查、权限树查询、权限编码唯一性校验等核心业务
 */
public interface PermissionService {
    /**
     * 新增权限（含业务规则校验）
     * @param permission 权限对象
     * @return true=新增成功，false=新增失败（编码重复/名称为空等）
     */
    boolean addPermission(Permission permission);

    /**
     * 根据ID删除权限
     * @param permissionId 权限ID
     * @return true=删除成功，false=删除失败（权限不存在/有子权限）
     */
    boolean deletePermissionById(Integer permissionId);

    /**
     * 修改权限信息
     * @param permission 权限对象（需包含ID）
     * @return true=修改成功，false=修改失败
     */
    boolean updatePermission(Permission permission);

    /**
     * 根据ID查询权限详情
     * @param permissionId 权限ID
     * @return 权限对象（null=不存在）
     */
    Permission getPermissionById(Integer permissionId);

    /**
     * 查询所有权限
     * @return 权限列表
     */
    List<Permission> getAllPermissions();

    /**
     * 根据父权限ID查询子权限（适配权限树结构）
     * @param parentId 父权限ID（null=查询一级权限）
     * @return 子权限列表
     */
    List<Permission> getPermissionsByParentId(Integer parentId);

    /**
     * 校验权限编码是否已存在（核心业务规则）
     * @param permissionCode 权限编码
     * @return true=已存在，false=不存在
     */
    boolean checkPermissionCodeExists(String permissionCode);

    /**
     * 校验权限是否有子权限（删除时的业务规则）
     * @param permissionId 权限ID
     * @return true=有子权限，false=无子权限
     */
    boolean checkHasChildPermissions(Integer permissionId);

    /**
     * 判断权限是否存在
     * @param permissionId 权限ID
     * @return true=权限存在
     */
    boolean checkPermissionExistsByPermissionId(Integer permissionId);
}