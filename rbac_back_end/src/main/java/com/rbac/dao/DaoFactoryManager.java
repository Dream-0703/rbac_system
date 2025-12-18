package com.rbac.dao;

import com.rbac.dao.DaoFactory;
import com.rbac.dao.MySqlDaoFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * 工厂管理器：从配置文件读取数据库类型，创建对应的抽象工厂
 * 核心：切换数据库只需改db.properties的db.type，无需改代码
 */
public class DaoFactoryManager {
    // 配置文件对象
    private static Properties props;
    // 数据库类型（从配置文件读取）
    private static String DB_TYPE;

    // 静态代码块：项目启动时加载配置文件
    static {
        props = new Properties();
        try {
            // 读取resources下的db.properties
            props.load(DaoFactoryManager.class.getClassLoader().getResourceAsStream("db.properties"));
            // 获取数据库类型配置
            DB_TYPE = props.getProperty("db.type");
            // 校验配置是否合法
            if (DB_TYPE == null || (!"mysql".equals(DB_TYPE) && !"postgresql".equals(DB_TYPE))) {
                throw new RuntimeException("db.properties中db.type配置错误，仅支持mysql/postgresql");
            }
        } catch (IOException e) {
            throw new RuntimeException("加载db.properties配置文件失败", e);
        }
    }

    /**
     * 获取抽象工厂（Service层仅调用此方法，无需关心具体数据库）
     */
    public static DaoFactory getDaoFactory() {
        if ("mysql".equals(DB_TYPE)) {
            return new MySqlDaoFactory();
        } else if ("postgresql".equals(DB_TYPE)) {
            // 可扩展其他数据库类型的工厂
        }
        // 理论上不会走到这里（静态代码块已校验）
        throw new IllegalArgumentException("不支持的数据库类型：" + DB_TYPE);
    }
}