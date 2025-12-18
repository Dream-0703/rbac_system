package com.rbac.dao.impl;

import com.rbac.dao.RolePermissionDAO;
import com.rbac.model.entity.RolePermission;
import com.rbac.util.DBHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySqlRolePermissionDaoImpl implements RolePermissionDAO {
    private DBHelper dbHelper = DBHelper.getInstance();

    @Override
    public int insertRolePermission(RolePermission rolePermission) {
        String sql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, rolePermission.getRoleId());
            pstmt.setInt(2, rolePermission.getPermissionId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("MySQL新增角色-权限关联失败", e);
        }
    }

    @Override
    public int deleteRolePermission(Integer roleId, Integer permissionId) {
        String sql = "DELETE FROM role_permissions WHERE role_id=? AND permission_id=?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roleId);
            pstmt.setInt(2, permissionId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("MySQL删除角色-权限关联失败", e);
        }
    }

    @Override
    public List<Integer> selectPermissionIdsByRoleId(Integer roleId) {
        String sql = "SELECT permission_id FROM role_permissions WHERE role_id=?";
        List<Integer> permissionIds = new ArrayList<>();
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roleId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                permissionIds.add(rs.getInt("permission_id"));
            }
            return permissionIds;
        } catch (SQLException e) {
            throw new RuntimeException("MySQL查询角色权限失败", e);
        }
    }

    @Override
    public boolean existsRolePermission(Integer roleId, Integer permissionId) {
        String sql = "SELECT 1 FROM role_permissions WHERE role_id=? AND permission_id=?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roleId);
            pstmt.setInt(2, permissionId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("MySQL校验角色权限关联失败", e);
        }
    }

    @Override
    public int deletePermissionsByRoleId(Integer roleId) {
        String sql = "DELETE FROM role_permissions WHERE role_id=?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roleId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("MySQL清空角色所有权限失败", e);
        }
    }
}