package com.rbac.dao.impl;

import com.rbac.dao.PermissionDAO;
import com.rbac.model.entity.Permission;
import com.rbac.util.DBHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MySqlPermissionDaoImpl implements PermissionDAO {
    private final DBHelper dbHelper = DBHelper.getInstance();

    @Override
    public int insertPermission(Permission permission) {
        String sql = "insert into permissions (name, code, parent_id) values(?, ?, ?)";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, permission.getName());
            pstmt.setString(2, permission.getCode());
            // 修复：parent_id为null时设为0
            pstmt.setInt(3, permission.getParentId() == null ? 0 : permission.getParentId());
            int affected = pstmt.executeUpdate();

            // 回填自增ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                permission.setId(rs.getInt(1));
            }
            return affected;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int deletePermissionById(Integer id) {
        String sql = "delete from permissions where id = ?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int updatePermission(Permission permission) {
        String sql = "update permissions set name = ?, code = ?, parent_id = ? where id = ?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, permission.getName());
            pstmt.setString(2, permission.getCode());
            pstmt.setInt(3, permission.getParentId() == null ? 0 : permission.getParentId());
            pstmt.setInt(4, permission.getId());
            return pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public Permission selectPermissionById(Integer id) {
        String sql = "select id, name, code, parent_id from permissions where id = ?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Permission(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("code"),
                            rs.getInt("parent_id")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Permission> selectAllPermissions() {
        String sql = "select id, name, code, parent_id from permissions";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            List<Permission> permissions = new ArrayList<>();
            while (rs.next()) {
                permissions.add(new Permission(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("code"),
                        rs.getInt("parent_id")
                ));
            }
            return permissions;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public List<Permission> selectPermissionsByParentId(Integer parentId) {
        String sql = "select id, name, code, parent_id from permissions where parent_id = ?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parentId == null ? 0 : parentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Permission> permissions = new ArrayList<>();
                while (rs.next()) {
                    permissions.add(new Permission(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("code"),
                            rs.getInt("parent_id")
                    ));
                }
                return permissions;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public Permission selectPermissionByCode(String code) {
        String sql = "select id, name, code, parent_id from permissions where code = ?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Permission(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("code"),
                            rs.getInt("parent_id")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}