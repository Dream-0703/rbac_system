package com.rbac.model.entity;

import java.sql.Timestamp;

/**
 * 用户表实体类
 * 对应数据库表：users
 */
public class User {
    // 字段对应表中列
    private Integer id;         // 主键ID（INT → Integer）
    private String username;    // 用户名（VARCHAR(50) → String）
    private String password;    // 密码（VARCHAR(255) → String）
    private Timestamp createdAt; // 创建时间（TIMESTAMP → Timestamp）

    // 无参构造函数
    public User() {
    }

    // 全参构造函数（按字段顺序）
    public User(Integer id, String username, String password, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.createdAt = createdAt;
    }

    // Getter & Setter 方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // 可选：重写toString，方便调试
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}