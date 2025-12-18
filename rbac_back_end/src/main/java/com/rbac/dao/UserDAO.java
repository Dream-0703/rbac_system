package com.rbac.dao;

import com.rbac.model.entity.User;
import java.util.List;

/**
 * 用户DAO接口：定义用户数据的访问规范
 */
public interface UserDAO {
    // 1. 新增用户
    int insertUser(User user);

    // 2. 根据ID删除用户
    int deleteUserById(Integer id);

    // 3. 更新用户信息
    int updateUser(User user);

    // 4. 根据ID查询用户
    User selectUserById(Integer id);

    // 5. 查询所有用户
    List<User> selectAllUsers();

    // 6. （可选）根据用户名查询用户（因为username是UNIQUE）
    User selectUserByUsername(String username);
}