package com.rbac.security.core;

import com.rbac.model.entity.Permission;
import com.rbac.model.entity.Role;
import com.rbac.service.*;
import com.rbac.model.entity.User;
import com.rbac.security.model.ApiRequest;
import com.rbac.security.model.ApiResponse;
import com.rbac.util.JsonUtil;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 真实业务逻辑类（完全兼容原有Service调用 + 适配前端精细化错误返回）
 */
@Slf4j
public class RealBusinessService implements ApiService {
    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;

    // 构造方法注入（原有逻辑保留）
    public RealBusinessService(UserService userService, RoleService roleService, PermissionService permissionService) {
        this.userService = userService;
        this.roleService = roleService;
        this.permissionService = permissionService;
    }

    // 生成全局唯一追踪ID（便于排查问题）
    private String generateTraceId() {
        return "REQ_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    // 解析异常信息（分离错误码和提示语）
    private Map<String, String> parseExceptionMsg(String exMsg) {
        Map<String, String> result = new HashMap<>();
        if (exMsg.contains("|")) {
            String[] parts = exMsg.split("\\|", 2);
            result.put("errorCode", parts[0]);
            result.put("msg", parts[1]);
        } else {
            result.put("errorCode", "UNKNOWN_ERROR");
            result.put("msg", exMsg);
        }
        return result;
    }

    @Override
    public ApiResponse execute(ApiRequest request) {
        // 1. 生成追踪ID（贯穿整个请求生命周期）
        String traceId = generateTraceId();

        // 2. 解析请求参数（原有逻辑完全保留）
        String permCode = request.getPermCode();
        String bizParams = request.getBizParams();
        User loginUser = request.getUser();
        // 处理登录请求等特殊情况，用户可能为null
        String username = (loginUser != null) ? loginUser.getUsername() : "未认证用户";
        log.info("【{}】用户{}调用业务：{}，参数：{}", traceId, username, permCode, bizParams);

        // 3. 调度Service层（仅修改返回值格式，业务逻辑不变）
        try {
            Object businessResult = dispatchToService(permCode, bizParams, loginUser);
            // 成功响应：保留原有返回数据，新增追踪ID
            return ApiResponse.success("业务执行成功", businessResult, traceId);
        } catch (IllegalArgumentException e) {
            // 不支持的权限编码
            log.warn("【{}】权限编码异常：{}", traceId, e.getMessage());
            return ApiResponse.fail("UNSUPPORTED_PERM_CODE", e.getMessage(), traceId);
        } catch (Exception e) {
            // 业务异常：解析错误码和提示语
            Map<String, String> exMap = parseExceptionMsg(e.getMessage());
            log.error("【{}】业务执行失败：{}", traceId, e.getMessage(), e);
            // 失败响应：返回错误码+提示+追踪ID（兼容前端判断）
            return ApiResponse.fail(exMap.get("errorCode"), exMap.get("msg"), traceId);
        }
    }

    /**
     * 按权限编码调度Service方法（核心：仅修改失败返回的错误码，业务逻辑完全保留）
     */
    private Object dispatchToService(String permCode, String bizParams, User loginUser) throws Exception {
        switch (permCode) {
            // ========== 1. 用户管理（原有逻辑保留，新增错误码） ==========
            case "system:user:list":
                return userService.queryAllUsers();

            case "system:user:create": {
                Map<String, String> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, String>>() {});
                String newUsername = paramMap.get("username");
                String newPassword = paramMap.get("password");

                // 前置校验：用户已存在 → 返回错误码 USER_NAME_DUPLICATE
                if (userService.checkUserExistsByUsername(newUsername)) {
                    throw new RuntimeException("USER_NAME_DUPLICATE|用户名【" + newUsername + "】已存在");
                }

                User user = new User();
                user.setUsername(newUsername);
                user.setPassword(newPassword);
                Boolean isSuccess = userService.registerUser(user);
                log.info("【{}】用户{}新增用户{},结果:{}", generateTraceId(), loginUser.getUsername(), newUsername, isSuccess);

                if (isSuccess) {
                    return "新增用户[" + newUsername + "]成功";
                } else {
                    throw new RuntimeException("USER_CREATE_FAILED|新增用户[" + newUsername + "]失败");
                }
            }

            case "system:user:password":{
                Map<String, Object> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, Object>>() {});
                Integer userid = (Integer) paramMap.get("userid");
                String newPassword = "123456";

                // 前置校验：用户不存在 → 返回错误码 USER_NOT_EXIST
                if (!userService.checkUserExistsByUserId(userid)) {
                    throw new RuntimeException("USER_NOT_EXIST|用户ID【" + userid + "】不存在");
                }

                Boolean isSuccess = userService.updateUserPassword(userid, newPassword);
                if (isSuccess) {
                    return "用户[" + userid + "]密码重置为[" + newPassword + "]成功";
                } else {
                    throw new RuntimeException("USER_PASSWORD_RESET_FAILED|用户[" + userid + "]密码重置失败");
                }
            }

            case "system:user:role": {
                Map<String, Integer> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, Integer>>() {});
                Integer userid = (Integer) paramMap.get("userid");
                Integer roleid = (Integer) paramMap.get("roleid");

                // 前置校验：用户不存在
                if (!userService.checkUserExistsByUserId(userid)) {
                    throw new RuntimeException("USER_NOT_EXIST|用户ID【" + userid + "】不存在");
                }
                // 前置校验：角色不存在
                if (!roleService.checkRoleExistsByRoleId(roleid)) {
                    throw new RuntimeException("ROLE_NOT_EXIST|角色ID【" + roleid + "】不存在");
                }

                Boolean isSuccess = userService.updateUserRole(userid, roleid);
                if (isSuccess) {
                    return "用户[" + userid + "]角色更新为[" + roleid + "]成功";
                } else {
                    throw new RuntimeException("USER_ROLE_UPDATE_FAILED|用户[" + userid + "]角色更新失败");
                }
            }
            case "system:user:getrole":{
                Map<String, Integer> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, Integer>>() {});
                Integer userid = (Integer) paramMap.get("userid");
                // 前置校验：用户不存在
                if (!userService.checkUserExistsByUserId(userid)) {
                    throw new RuntimeException("USER_NOT_EXIST|用户ID【" + userid + "】不存在");
                }
                Role role = userService.getRoleByUserId(userid);
                if (role != null) {
                    return role.getName();
                } else {
                    throw new RuntimeException("USER_ROLE_EMPTY|用户[" + userid + "]暂无分配角色");
                }
            }

            case "system:user:own:permission":{
                Map<String, Integer> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, Integer>>() {});
                Integer userid = (Integer) paramMap.get("userid");

                // 前置校验：用户不存在
                if (!userService.checkUserExistsByUserId(userid)) {
                    throw new RuntimeException("USER_NOT_EXIST|用户ID【" + userid + "】不存在");
                }

                User user=userService.getUserByUserId(userid);
                List<String> permissions = userService.getUserPermissionsByUsername(user.getUsername());
                if (permissions != null && !permissions.isEmpty()) {
                    return permissions;
                } else {
                    throw new RuntimeException("USER_PERMISSION_EMPTY|用户[" + userid + "]暂无分配权限");
                }
            }

            case "system:user:own:password":{
                Map<String, Object> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, Object>>() {});
                String newPassword = (String) paramMap.get("newPassword");

                Boolean isSuccess = userService.updateUserPassword(loginUser.getId(), newPassword);
                if (isSuccess) {
                    return "用户[" + loginUser.getUsername() + "]密码更新成功";
                } else {
                    throw new RuntimeException("USER_OWN_PASSWORD_FAILED|用户[" + loginUser.getUsername() + "]密码更新失败");
                }
            }

            case "system:user:own:login":{
                Map<String, String> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, String>>() {});
                String username = paramMap.get("username");
                String password = paramMap.get("password");

                // 前置校验：用户名或密码为空
                if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                    throw new RuntimeException("LOGIN_PARAM_ERROR|用户名或密码不能为空");
                }

                Boolean isSuccess = userService.login(username, password);
                if (isSuccess) {
                    return "登录成功";
                } else {
                    throw new RuntimeException("LOGIN_FAILED|用户名或密码错误");
                }
            }

            case "system:user:delete":{
                Map<String, Object> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, Object>>() {});
                Integer userid = (Integer) paramMap.get("userid");

                // 前置校验：用户不存在
                if (!userService.checkUserExistsByUserId(userid)) {
                    throw new RuntimeException("USER_NOT_EXIST|用户ID【" + userid + "】不存在");
                }

                Boolean isSuccess = userService.deleteUser(userid);
                if (isSuccess) {
                    return "用户[" + userid + "]删除成功";
                } else {
                    throw new RuntimeException("USER_DELETE_FAILED|用户[" + userid + "]删除失败");
                }
            }

            // ========== 2. 角色管理（原有逻辑保留，新增错误码） ==========
            case "system:role:list": {
                return roleService.getAllRoles();
            }

            case "system:role:create":{
                Map<String, String> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, String>>() {});
                String roleName = paramMap.get("roleName");
                String description = paramMap.get("description");

                // 前置校验：角色名重复 → 返回错误码 ROLE_NAME_DUPLICATE
                if (roleService.checkRoleExistsByRoleName(roleName)) {
                    throw new RuntimeException("ROLE_NAME_DUPLICATE|角色名称【" + roleName + "】已存在");
                }

                Role role = new Role();
                role.setName(roleName);
                role.setDescription(description);
                boolean isSuccess = roleService.addRole(role);
                if (isSuccess) {
                    return "新增角色[" + roleName + "]成功";
                } else {
                    throw new RuntimeException("ROLE_CREATE_FAILED|新增角色[" + roleName + "]失败");
                }
            }

            case "system:role:permission": {
                Map<String, Object> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, Object>>() {});
                Integer roleId = (Integer) paramMap.get("roleId");
                List<Integer> permIdList = null;

                // 前置校验：角色不存在
                if (!roleService.checkRoleExistsByRoleId(roleId)) {
                    throw new RuntimeException("ROLE_NOT_EXIST|角色ID【" + roleId + "】不存在");
                }

                Object permIdObj = paramMap.get("permIdList");
                if (permIdObj instanceof List) {
                    permIdList = ((List<?>) permIdObj).stream()
                            .map(obj -> obj != null ? Integer.parseInt(obj.toString()) : null)
                            .collect(Collectors.toList());

                    // 校验权限ID是否有效
                    List<Integer> invalidPermIds = permIdList.stream()
                            .filter(permId -> permId != null && !permissionService.checkPermissionExistsByPermissionId(permId))
                            .collect(Collectors.toList());
                    if (!invalidPermIds.isEmpty()) {
                        throw new RuntimeException("PERMISSION_NOT_EXIST|权限ID【" + invalidPermIds + "】不存在");
                    }
                }

                boolean isSuccess = roleService.assignPermissionsToRole(roleId, permIdList);
                if (isSuccess) {
                    return "角色[" + roleId + "]权限更新成功";
                } else {
                    throw new RuntimeException("ROLE_PERM_UPDATE_FAILED|角色[" + roleId + "]权限更新失败");
                }
            }

            // ========== 3. 权限管理（原有逻辑保留，新增错误码） ==========
            case "system:permission:list":{
                return permissionService.getAllPermissions();
            }

            case "system:permission:create": {
                Map<String, Object> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, Object>>() {});
                String permission_name= (String) paramMap.get("permission_name");
                String code = (String) paramMap.get("code");
                Integer parentId = (Integer) paramMap.get("parent_id");

                // 前置校验：权限编码重复
                if (permissionService.checkPermissionCodeExists(code)) {
                    throw new RuntimeException("PERM_CODE_DUPLICATE|权限编码【" + code + "】已存在");
                }
                // 前置校验：父权限不存在
                if (parentId != null && !permissionService.checkPermissionExistsByPermissionId(parentId)) {
                    throw new RuntimeException("PERM_PARENT_NOT_EXIST|父权限ID【" + parentId + "】不存在");
                }

                Permission permission = new Permission();
                permission.setName(permission_name);
                permission.setCode(code);
                permission.setParentId(parentId);
                boolean isSuccess = permissionService.addPermission(permission);
                if (isSuccess) {
                    return "新增权限[" + permission_name + "]成功";
                } else {
                    throw new RuntimeException("PERM_CREATE_FAILED|新增权限[" + permission_name + "]失败");
                }
            }

            case "syetem:permission:edit": {
                Map<String, Object> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, Object>>() {});
                Integer permission_id = (Integer) paramMap.get("permission_id");
                String permission_name= (String) paramMap.get("permission_name");
                String code = (String) paramMap.get("code");
                Integer parentId = (Integer) paramMap.get("parent_id");

                // 前置校验：权限不存在
                if (!permissionService.checkPermissionExistsByPermissionId(permission_id)) {
                    throw new RuntimeException("PERMISSION_NOT_EXIST|权限ID【" + permission_id + "】不存在");
                }
                // 前置校验：权限编码重复
                if (permissionService.checkPermissionCodeExists(code) && !permissionService.getPermissionById(permission_id).getCode().equals(code)) {
                    throw new RuntimeException("PERM_CODE_DUPLICATE|权限编码【" + code + "】已存在");
                }

                Permission permission = new Permission();
                permission.setId(permission_id);
                permission.setName(permission_name);
                permission.setCode(code);
                permission.setParentId(parentId);
                boolean isSuccess = permissionService.updatePermission(permission);
                if (isSuccess) {
                    return "权限[" + permission_id + "]更新成功";
                } else {
                    throw new RuntimeException("PERM_UPDATE_FAILED|权限[" + permission_id + "]更新失败");
                }
            }

            default:
                throw new IllegalArgumentException("UNSUPPORTED_PERM_CODE|不支持的权限编码：" + permCode);
        }
    }

    // ========== 兼容你原有Service的校验方法（需确保Service层实现以下方法） ==========
    // 注：以下是占位提示，实际需在你的UserService/RoleService/PermissionService中实现这些校验方法
    // 1. UserService需实现：checkUsernameExist(String username)、checkUserExist(Integer userId)
    // 2. RoleService需实现：checkRoleExist(Integer roleId)、checkRoleNameExist(String roleName)
    // 3. PermissionService需实现：checkPermissionExist(Integer permId)、checkPermCodeExist(String code)、getPermissionById(Integer permId)
}