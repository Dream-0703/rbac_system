package com.rbac.model.entity;

/**
 * 角色表实体类
 * 对应数据库表：roles
 */
public class Role {
    // 字段对应表中列
    private Integer id;          // 主键ID（INT → INTEGER）
    private String name;         // 角色名称（VARCHAR(50) → String）
    private String description;  // 角色描述（VARCHAR(255) → String）

    // 无参构造函数
    public Role() {
    }

    // 全参构造函数（按字段顺序）
    public Role(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Getter & Setter 方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // 可选：重写toString，方便调试
    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}