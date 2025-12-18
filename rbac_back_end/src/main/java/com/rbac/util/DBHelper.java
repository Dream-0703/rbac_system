package com.rbac.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBHelper {
    private static volatile DBHelper instance;
    private static Properties props = new Properties();

    // 静态代码块：加载配置文件（只执行一次）
    static {
        try {
            // 读取resources下的db.properties
            props.load(DBHelper.class.getClassLoader().getResourceAsStream("db.properties"));
            // 加载驱动
            Class.forName(props.getProperty("db.driver"));
        } catch (Exception e) {
            throw new RuntimeException("配置文件/驱动加载失败", e);
        }
    }

    private DBHelper() {} // 私有构造

    public static DBHelper getInstance() {
        if (instance == null) {
            synchronized (DBHelper.class) {
                if (instance == null) {
                    instance = new DBHelper();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.user"),
                    props.getProperty("db.password")
            );
        } catch (SQLException e) {
            throw new RuntimeException("获取连接失败", e);
        }
    }
    // 测试连接的简易代码（可写在DBHelper类的main方法中）
    public static void main(String[] args) {
        try {
            Connection conn = DBHelper.getInstance().getConnection();
            System.out.println("数据库连接成功 ✅");
            conn.close();
        } catch (Exception e) {
            System.err.println("数据库连接失败 ❌：" + e.getMessage());
        }
    }
}