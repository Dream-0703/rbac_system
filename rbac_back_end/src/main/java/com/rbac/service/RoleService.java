package com.rbac.service;

import com.rbac.model.entity.Permission;
import com.rbac.model.entity.Role;
import java.util.List;

/**
 * 角色业务服务接口：定义角色相关的业务功能规范
 * 业务层只依赖此接口，不依赖具体实现
 */
public interface RoleService {
    /**
     * 新增角色（含业务规则校验）
     * @param role 角色对象
     * @return true=新增成功，false=新增失败（如角色名重复）
     */
    boolean addRole(Role role);

    /**
     * 根据ID删除角色
     * @param roleId 角色ID
     * @return true=删除成功，false=删除失败（如角色不存在）
     */
    boolean deleteRoleById(Integer roleId);

    /**
     * 修改角色信息
     * @param role 角色对象（需包含ID）
     * @return true=修改成功，false=修改失败
     */
    boolean updateRole(Role role);

    /**
     * 根据ID查询角色详情
     * @param roleId 角色ID
     * @return 角色对象（null=不存在）
     */
    Role getRoleById(Integer roleId);

    /**
     * 查询所有角色
     * @return 角色列表
     */
    List<Role> getAllRoles();

    /**
     * 校验角色名是否已存在（新增/修改时的核心业务规则）
     * @param roleName 角色名
     * @return true=已存在，false=不存在
     */
    boolean checkRoleNameExists(String roleName);

    /**
     * 查询角色的所有权限
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByRoleId(Integer roleId);

    /**
     * 为角色分配权限
     * @param roleId 角色ID
     * @param permIdList 权限ID列表
     * @return true=分配成功，false=分配失败
     */
     boolean assignPermissionsToRole(Integer roleId, List<Integer> permIdList);

    /**
     * 验证角色是否有某权限
     * @param roleId 角色ID
     * @param permissionCode 权限编码
     * @return true=有该权限，false=无该权限
     */
    public boolean hasPermission(Integer roleId, String permissionCode);

    /**
     * 判断角色是否存在
     * @param roleId 角色ID
     * @return true=角色存在
     */
    boolean checkRoleExistsByRoleId(Integer roleId);
     /**
      * 判断角色名是否存在
      * @param roleName 角色名
      * @return true=角色名存在
      */
     boolean checkRoleExistsByRoleName(String roleName);
}