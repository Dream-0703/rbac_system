package com.rbac.service.impl;
import com.rbac.dao.*;
import com.rbac.model.entity.Permission;
import com.rbac.model.entity.Role;
import com.rbac.model.entity.User;
import com.rbac.model.entity.UserRole;
import com.rbac.service.IExternalAuthService;
import com.rbac.service.UserService;
import com.rbac.service.adapter.LocalAuthAdapter;
import com.rbac.service.factory.AuthAdapterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.slf4j.Logger; // 必须引入
import org.slf4j.LoggerFactory; // 必须引入
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service // 注册为Spring Service
@Slf4j
public class UserServiceImpl implements UserService {
    private DaoFactory daoFactory;
    private UserDAO userDAO;
    private RoleDAO roleDAO;
    private UserRoleDAO userRoleDAO;
    private PermissionDAO permissionDAO;
    private RolePermissionDAO rolePermissionDAO;
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    // 构造方法：从DaoFactoryManager获取工厂实例
    public UserServiceImpl() {
        this.daoFactory = DaoFactoryManager.getDaoFactory();
        this.userDAO = daoFactory.createUserDAO();
        this.roleDAO = daoFactory.createRoleDAO();
        this.userRoleDAO = daoFactory.createUserRoleDAO();
        this.permissionDAO = daoFactory.createPermissionDAO();
        this.rolePermissionDAO = daoFactory.createRolePermissionDAO();
    }

    //注册用户方法 false 表示注册失败
    @Override
    public boolean registerUser(User user) {
        // 检查用户名是否已存在
        if (userDAO.selectUserByUsername(user.getUsername()) != null) {
            return false; // 用户名已存在
        }
        // 新增用户
        int result = userDAO.insertUser(user);
        log.info("业务层收到的数据库返回:{}", result);
        log.info("新增用户：username={}, result={}", user.getUsername(), result);
        if (result > 0) {
            return true;
        }
        return false; // 返回是否插入成功
    }

    // 1. 修复根据用户名查询用户（必须查数据库）
    @Override
    public User getUserByUsername(String username) {
        // 增强校验：严格过滤非法用户名（如特殊字符、空值）
        if (username == null || username.trim().isEmpty()
                || username.contains("'") || username.contains(";")) { // 防注入
            log.warn("查询用户失败：非法用户名={}", username);
            return null;
        }
        log.info("查询数据库用户：username={}", username);
        User dbUser = userDAO.selectUserByUsername(username.trim());
        if (dbUser == null) {
            log.error("用户不存在：username={}", username);
        }
        return dbUser;
    }

    @Override
    public Role getRoleByUsername(String username) {
        // 1. 根据用户名查询用户ID
        User user = userDAO.selectUserByUsername(username);
        if (user == null) {
            return null; // 用户不存在
        }
        // 2. 查询用户的所有角色ID
        Integer roleId = userRoleDAO.selectRoleIdsByUserId(user.getId());
        if (roleId == null) {
            return null; // 用户未分配角色
        }
        // 3. 根据角色ID查询角色信息
        Role role = roleDAO.selectRoleById(roleId);
        if (role == null) {
            return null; // 角色不存在
        }
        return role;
    }
    
    @Override
    public Role getRoleByUserId(Integer userid) {
        // 1. 直接查询用户的角色ID
        Integer roleId = userRoleDAO.selectRoleIdsByUserId(userid);
        if (roleId == null) {
            return null; // 用户未分配角色
        }
        // 2. 根据角色ID查询角色信息
        Role role = roleDAO.selectRoleById(roleId);
        log.info("根据用户ID查询角色：userid={}, roleId={}", userid, roleId);
        return role;
    }

    @Override
    public User getUserByUserId(Integer userid) {
        return userDAO.selectUserById(userid);
    }

    @Override
    public List<String> getUserPermissionsByUsername(String username) {
        Role role = getRoleByUsername(username);
        if (role == null) { // 补充判空，避免空指针
            return new ArrayList<>(); // 返回空列表而非null，更符合编程规范
        }
        // 用HashSet自动去重
        Set<String> permissionSet = new HashSet<>();
        List<Integer> permissionIds = rolePermissionDAO.selectPermissionIdsByRoleId(role.getId());
        for (Integer permissionId : permissionIds) {
            Permission permission = permissionDAO.selectPermissionById(permissionId);
            if (permission != null) { // 避免空指针（权限记录可能被删除）
                permissionSet.add(permission.getCode());
            }
        }
        // 转回List返回
        return new ArrayList<>(permissionSet);
    }

    // 新增login方法（集成适配器+降级）
    @Override
    public boolean login(String username, String password) {
        // 1. 从工厂获取适配器（配置驱动）
        IExternalAuthService authAdapter = AuthAdapterFactory.getAuthAdapter();
        try {
            // 2. 优先调用外部适配器验证
            boolean verifyResult = authAdapter.verifyUser(username, password);
            if (verifyResult) {
                // 同步外部权限到本地
                List<String> externalPerms = authAdapter.getUserPermissions(username);
                System.out.println("用户" + username + "的外部权限：" + externalPerms);
                return true;
            }
        } catch (Exception e) {
            // 3. 实验要求：降级策略——外部服务失败，自动切换到本地适配器
            System.out.println("外部权限服务调用失败，降级到本地验证：" + e.getMessage());
            IExternalAuthService localAdapter = new LocalAuthAdapter();
            return localAdapter.verifyUser(username, password);
        }
        return false;
    }

    @Override
    public List<User> queryAllUsers() {
        return userDAO.selectAllUsers();
    }

    @Override
    public boolean updateUserPassword(Integer userid, String newPassword) {
        // 1. 根据用户ID查询用户
        User user = userDAO.selectUserById(userid);
        if (user == null) {
            return false; // 用户不存在
        }
        // 2. 更新用户密码
        user.setPassword(newPassword);
        int result = userDAO.updateUser(user);
        return result > 0; // 返回是否更新成功
    }

    @Override
    public boolean updateUserRole(Integer userid, Integer roleid) {
        // 1. 根据用户ID查询用户
        User user = userDAO.selectUserById(userid);
        if (user == null) {
            return false; // 用户不存在
        }
        // 2. 检查用户是否已分配角色,已分配则删除
        if (userRoleDAO.existsUserRole(userid)) {
            userRoleDAO.deleteUserRole(userid);
            System.out.println("用户" + userid + "的角色已删除");
        }
        // 3. 根据角色ID查询角色
        Role role = roleDAO.selectRoleById(roleid);
        if (role == null) {
            return false; // 角色不存在
        }
        // 3. 更新用户角色
        userRoleDAO.insertUserRole(new UserRole(userid, roleid));
        return true; // 返回是否更新成功
    }

    @Override
    public boolean checkUserExistsByUsername(String username) {
        return userDAO.selectUserByUsername(username) != null;
    }

    @Override
    public boolean checkUserExistsByUserId(Integer userid) {
        return userDAO.selectUserById(userid) != null;
    }

    @Override
    public boolean deleteUser(Integer userid) {
        // 1. 检查用户是否存在
        if (!checkUserExistsByUserId(userid)) {
            log.warn("删除用户失败：用户不存在，userid={}", userid);
            return false;
        }

        // 2. 先删除用户角色关联（维护引用完整性）
        if (userRoleDAO.existsUserRole(userid)) {
            userRoleDAO.deleteUserRole(userid);
            log.info("删除用户角色关联：userid={}", userid);
        }

        // 3. 删除用户
        int result = userDAO.deleteUserById(userid);
        log.info("删除用户结果：userid={}, result={}", userid, result);
        return result > 0;
    }
}
