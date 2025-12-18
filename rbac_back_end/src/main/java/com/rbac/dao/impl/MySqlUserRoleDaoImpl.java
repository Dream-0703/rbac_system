package com.rbac.dao.impl;

import com.rbac.dao.UserRoleDAO;
import com.rbac.model.entity.UserRole;
import com.rbac.util.DBHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 * MySQL用户-角色关联DAO实现类
 */
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MySqlUserRoleDaoImpl implements UserRoleDAO {
    /**
     * 数据库帮助类实例（单例模式）
     */
    private DBHelper dbHelper = DBHelper.getInstance();

    @Override
    public int insertUserRole(UserRole userRole) {
        String sql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userRole.getUserId());
            pstmt.setInt(2, userRole.getRoleId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("MySQL新增用户-角色关联失败", e);
        }
    }

    @Override
    public int deleteUserRole(Integer userId) {
        String sql = "DELETE FROM user_roles WHERE user_id=? ";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("MySQL删除该用户角色关联失败", e);
        }
    }

    @Override
    public Integer selectRoleIdsByUserId(Integer userId) {
        String sql = "SELECT role_id FROM user_roles WHERE user_id=?";
        Integer roleId;
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                roleId = rs.getInt("role_id");
                log.info("查询用户角色DAO层：userId={}, roleId={}", userId, roleId);
            } else {
                roleId = null;
            }
            return roleId;
        } catch (SQLException e) {
            throw new RuntimeException("MySQL查询用户角色失败", e);
        }
    }

    @Override
    public boolean existsUserRole(Integer userId) {
        String sql = "SELECT 1 FROM user_roles WHERE user_id=? ";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("MySQL校验用户角色关联失败", e);
        }
    }
}