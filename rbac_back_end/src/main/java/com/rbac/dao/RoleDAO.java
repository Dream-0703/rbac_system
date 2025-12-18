package com.rbac.dao;

import com.rbac.model.entity.Role;
import java.util.List;

/**
 * 角色DAO接口：定义角色数据的访问规范
 */
public interface RoleDAO {
    // 1. 新增角色
    int insertRole(Role role);

    // 2. 根据ID删除角色
    int deleteRoleById(Integer id);

    // 3. 更新角色信息
    int updateRole(Role role);

    // 4. 根据ID查询角色
    Role selectRoleById(Integer id);

    // 5. 查询所有角色
    List<Role> selectAllRoles();
    // 6. 根据角色名查询角色
    Role selectRoleByRoleName(String roleName);
}