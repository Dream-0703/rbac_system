package com.rbac.model.entity;

/**
 * 权限表实体类
 * 对应数据库表：permissions
 */
public class Permission {
    // 字段对应表中列（MySQL类型 → Java类型）
    private Integer id;          // 主键ID（INT → Integer）
    private String name;         // 权限名称（VARCHAR(100) → String）
    private String code;         // 权限编码（VARCHAR(100) → String）
    private Integer parentId;    // 父权限ID（INT → Integer，下划线转驼峰）

    private boolean inheritParent; // 是否继承父节点权限（默认true）
    private boolean enabled; // 权限是否启用（默认true）

    // 无参构造函数（满足框架反射实例化）
    public Permission() {
        this.inheritParent = true;
        this.enabled = true;
    }

    // 全参构造函数（按字段顺序，方便手动创建对象）
    public Permission(Integer id, String name, String code, Integer parentId) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.parentId = parentId;
        this.inheritParent = true;
        this.enabled = true;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public boolean isInheritParent() { return inheritParent; }
    public void setInheritParent(boolean inheritParent) { this.inheritParent = inheritParent; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }


    // 可选：重写toString，方便调试/日志输出
    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", parentId=" + parentId +
                '}';
    }
}