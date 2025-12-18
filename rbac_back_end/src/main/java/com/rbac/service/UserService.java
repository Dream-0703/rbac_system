package com.rbac.service;

import com.rbac.model.entity.Role;
import com.rbac.model.entity.User;

import java.util.List;

public interface UserService {
    /**
     * 注册用户
     * @param user 用户对象
     * @return 注册成功返回true，否则返回false
     */
    boolean registerUser(User user);

    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return 用户对象
     */
    User getUserByUsername(String username);

     /**
     * 根据用户名获取用户角色
     * @param username 用户名
     * @return 角色对象
     */
    public Role getRoleByUsername(String username);
    
    /**
     * 根据用户ID获取用户角色
     * @param userid 用户ID
     * @return 角色对象
     */
    public Role getRoleByUserId(Integer userid);

    /**
     * 根据用户id获取用户对象
     * @param userid 用户id
     * @return 用户对象
     */
    public User getUserByUserId(Integer userid);

    /**
     * 根据用户名获取用户权限列表
     * @param username 用户名
     * @return 权限字符串列表
     */
    public List<String> getUserPermissionsByUsername(String username);

     /**
     * 登录逻辑
     * @param username 用户名
     * @param password 密码
     * @return true=登录成功
     */
    boolean login(String username, String password);

    /**
     * 查询所有用户
     * @return 用户对象列表
     */
    List<User> queryAllUsers();

    /**
     * 修改指定用户密码
     * @param userid 用户id
     * @param newPassword 新密码
     * @return true=修改成功
     */
    boolean updateUserPassword(Integer userid, String newPassword);

    /**
     * 修改指定用户角色
     * @param userid 用户名id
     * @param roleid 角色id
     * @return true=修改成功
     */
    boolean updateUserRole(Integer userid, Integer roleid);
    /**
     * 判断用户是否存在
     * @param username 用户名
     * @return true=用户存在
     */
    boolean checkUserExistsByUsername(String username);
    /**
     * 判断用户是否存在
     * @param userid 用户id
     * @return true=用户存在
     */
    boolean checkUserExistsByUserId(Integer userid);
    
    /**
     * 删除指定用户
     * @param userid 用户id
     * @return true=删除成功
     */
    boolean deleteUser(Integer userid);
}
