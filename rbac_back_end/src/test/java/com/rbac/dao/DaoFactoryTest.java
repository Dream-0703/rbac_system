package com.rbac.dao;

import com.rbac.dao.DaoFactory;
import com.rbac.dao.UserDAO;
import com.rbac.dao.MySqlDaoFactory;
import com.rbac.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Timestamp;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 抽象工厂+DAO的单元测试：验证工厂创建的DAO能否正常工作
 */
public class DaoFactoryTest {
    // 抽象工厂对象（所有测试共用）
    private DaoFactory daoFactory;
    // 待测试的UserDAO（通过工厂创建）
    private UserDAO userDAO;

    // 每个测试方法执行前，初始化工厂和DAO
    @BeforeEach
    void init() {
        // 创建MySQL具体工厂
        daoFactory = new MySqlDaoFactory();
        // 通过工厂创建UserDAO（不直接new，验证工厂功能）
        userDAO = daoFactory.createUserDAO();
    }

    /**
     * 测试1：验证工厂能否正确创建DAO对象
     */
    @Test
    void testCreateDao() {
        // 验证UserDAO不为null，且是MySQL实现类
        assertNotNull(userDAO);
        assertEquals("com.rbac.dao.impl.MySqlUserDaoImpl", userDAO.getClass().getName());

        // （可选）验证RoleDAO/PermissionDAO的创建
        assertNotNull(daoFactory.createRoleDAO());
        assertNotNull(daoFactory.createPermissionDAO());
    }

    /**
     * 测试2：验证UserDAO的新增+查询功能（核心业务验证）
     */
    @Test
    void testUserDaoInsertAndSelect() {
        // 1. 构造一个测试用户对象
        User testUser = new User(
                null, // id自增，传null
                "test_factory_user",
                "test123",
                new Timestamp(System.currentTimeMillis())
        );

        // 2. 调用UserDAO新增用户（验证插入功能）
        int affectedRows = userDAO.insertUser(testUser);
        assertEquals(1, affectedRows, "新增用户失败，受影响行数应为1");

        // 3. 调用UserDAO查询新增的用户（验证查询功能）
        User savedUser = userDAO.selectUserByUsername("test_factory_user");
        assertNotNull(savedUser, "查询不到新增的用户");
        assertEquals("test_factory_user", savedUser.getUsername(), "用户名不匹配");
        assertEquals("test123", savedUser.getPassword(), "密码不匹配");
    }

    /**
     * 测试3：验证UserDAO的查询所有功能
     */
    @Test
    void testSelectAllUsers() {
        // 调用查询所有用户
        List<User> userList = userDAO.selectAllUsers();
        // 验证结果不为空（假设数据库已有数据，若为空可先插入测试数据）
        assertTrue(userList.size() > 0, "查询所有用户结果为空");
    }

    /**
     * （可选）测试4：验证删除功能（测试后清理数据）
     */
    @Test
    void testDeleteUser() {
        // 先查询到测试用户
        User testUser = userDAO.selectUserByUsername("test_factory_user");
        assertNotNull(testUser, "测试用户不存在，无法删除");

        // 调用删除
        int affectedRows = userDAO.deleteUserById(testUser.getId());
        assertEquals(1, affectedRows, "删除用户失败");

        // 验证删除后查询不到
        assertNull(userDAO.selectUserByUsername("test_factory_user"), "用户未被成功删除");
    }
}