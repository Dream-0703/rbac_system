package com.rbac.dao.impl;
import com.rbac.dao.UserDAO;
import com.rbac.model.entity.User;
import com.rbac.util.DBHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
@Slf4j
public class MySqlUserDaoImpl implements UserDAO {
    //复用单例DBHelper
    private final DBHelper dbHelper = DBHelper.getInstance();

    @Override
    public int insertUser(User user) {
        //采用预编译定义SQL语句，防止SQL注入
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        //使用try-with-resources自动关闭连接和语句
        try (var conn = dbHelper.getConnection();
             var pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            int result = pstmt.executeUpdate();
            log.info("插入执行后数据库返回{}", result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int deleteUserById(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (var conn = dbHelper.getConnection();
             var pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int updateUser(User user) {
        //采用预编译定义SQL语句，防止SQL注入
        String sql = "UPDATE users SET username = ?, password = ?, created_at = ? WHERE id = ?";
        try (var conn = dbHelper.getConnection();
             var pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setTimestamp(3, user.getCreatedAt());
            pstmt.setInt(4, user.getId());
            return pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public User selectUserById(Integer id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (var conn = dbHelper.getConnection();
             var pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            // 执行查询并处理结果集
            try (var rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getTimestamp("created_at")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<User> selectAllUsers() {
        String sql = "SELECT * FROM users";
        try (var conn = dbHelper.getConnection();
             var pstmt = conn.prepareStatement(sql);
             // 执行查询并处理结果集
             var rs = pstmt.executeQuery()) {
            var users = new java.util.ArrayList<User>();
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getTimestamp("created_at")
                ));
            }
            return users;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    @Override
    public User selectUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (var conn = dbHelper.getConnection();
             var pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            // 执行查询并处理结果集
            try (var rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getTimestamp("created_at")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
