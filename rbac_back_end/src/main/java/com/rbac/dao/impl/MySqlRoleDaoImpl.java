package com.rbac.dao.impl;

import com.rbac.dao.RoleDAO;
import com.rbac.model.entity.Role;
import com.rbac.util.DBHelper;

import java.sql.Connection;
import java.util.List;

public class MySqlRoleDaoImpl implements RoleDAO {
    //复用单例DBHelper
    private final DBHelper dbHelper = DBHelper.getInstance();

    @Override
    public int insertRole(Role role) {
        String sql = "insert into roles (name, description) values(?, ?)";
        try (Connection conn = dbHelper.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role.getName());
            pstmt.setString(2, role.getDescription());
            return pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int deleteRoleById(Integer id) {
        String sql = "delete from roles where id = ?";
        try (Connection conn = dbHelper.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int updateRole(Role role) {
        String sql = "update roles set name = ?, description = ? where id = ?";
        try (Connection conn = dbHelper.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role.getName());
            pstmt.setString(2, role.getDescription());
            pstmt.setInt(3, role.getId());
            return pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public Role selectRoleById(Integer id) {
        String sql = "select id, name, description from roles where id = ?";
        try (Connection conn = dbHelper.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Role(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Role> selectAllRoles() {
        String sql = "select id, name, description from roles";
        try (Connection conn = dbHelper.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            List<Role> roles =  new java.util.ArrayList<Role>();
            while (rs.next()) {
                roles.add(new Role(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                ));
            }
            return roles;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public Role selectRoleByRoleName(String roleName) {
        String sql = "select id, name, description from roles where name = ?";
        try (Connection conn = dbHelper.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roleName);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Role(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
