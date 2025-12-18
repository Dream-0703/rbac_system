package com.rbac.util;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * DBHelper的单元测试类：验证数据库连接是否成功
 */
public class DBHelperTest {

    // 测试方法：验证获取的连接不为null（即连接成功）
    @Test
    public void testConnection() {
        // 1. 获取DBHelper单例实例
        DBHelper dbHelper = DBHelper.getInstance();
        // 2. 获取数据库连接
        Connection connection = dbHelper.getConnection();

        // 3. 断言：连接不能为null（如果为null则测试失败）
        assertNotNull(connection, "数据库连接获取失败！");

        // （可选）打印连接信息，直观验证
        System.out.println("数据库连接成功！连接对象：" + connection);

        // 4. 关闭连接（避免资源泄露）
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}