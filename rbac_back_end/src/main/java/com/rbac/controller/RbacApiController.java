package com.rbac.controller;

import com.rbac.security.model.ApiRequest;
import com.rbac.security.proxy.SecurityChain;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RBAC权限系统核心接口（与真实业务逻辑绑定，适配标准化错误码返回）
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "RBAC权限系统接口", description = "用户/角色/权限管理接口（支持精细化错误码、链路追踪ID）")
public class RbacApiController {

    @Autowired
    private ApplicationContext applicationContext;

    private SecurityChain getNewSecurityChain() {
        return applicationContext.getBean(SecurityChain.class);
    }

    // ===================== 用户管理接口（对应system:user:*权限） =====================
    @PostMapping("/user/list")
    @Operation(
            summary = "查询所有用户",
            description = "获取系统中所有用户信息列表，需system:user:list权限，无业务参数，直接返回用户列表",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ApiRequest.class),
                            examples = @ExampleObject(
                                    name = "查询所有用户示例",
                                    value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9.admin.123456789\",\"permCode\":\"system:user:list\",\"bizParams\":\"{}\",\"timestamp\":1735689600000}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "查询成功，返回用户列表（User实体列表）",
                            content = @Content(
                                    schema = @Schema(implementation = com.rbac.security.model.ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "成功示例",
                                                    value = "{\"success\":true,\"errorCode\":null,\"msg\":\"业务执行成功\",\"data\":[{\"id\":1,\"username\":\"admin\",\"password\":\"加密串\"}],\"costTime\":50,\"traceId\":\"REQ_1735689600000_8a7b6c5d\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            ),
                                            @ExampleObject(
                                                    name = "失败示例（无权限）",
                                                    value = "{\"success\":false,\"errorCode\":\"PERMISSION_DENIED\",\"msg\":\"授权失败：无权访问system:user:list\",\"data\":null,\"costTime\":30,\"traceId\":\"REQ_1735689600001_9d8c7b6a\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            )
                                    }
                            )
                    )
            }
    )
    public com.rbac.security.model.ApiResponse getUserList(@RequestBody ApiRequest request) {
        request.setPermCode("system:user:list");
        request.setBizParams("{}"); // 无业务参数，默认空JSON
        fillDefaultParams(request);
        SecurityChain securityChain = getNewSecurityChain();
        return securityChain.proceed(request);
    }

    @PostMapping("/user/create")
    @Operation(
            summary = "新增用户",
            description = "创建新用户，需system:user:create权限，bizParams需传入username和password",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ApiRequest.class),
                            examples = @ExampleObject(
                                    name = "新增用户示例",
                                    value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9.admin.123456789\",\"permCode\":\"system:user:create\",\"bizParams\":\"{\\\"username\\\":\\\"test_user\\\",\\\"password\\\":\\\"123456\\\"}\",\"timestamp\":1735689600000}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "新增结果，返回成功/失败提示",
                            content = @Content(
                                    schema = @Schema(implementation = com.rbac.security.model.ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "成功示例",
                                                    value = "{\"success\":true,\"errorCode\":null,\"msg\":\"业务执行成功\",\"data\":\"新增用户[test_user]成功\",\"costTime\":30,\"traceId\":\"REQ_1735689600002_7c6b5a4d\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            ),
                                            @ExampleObject(
                                                    name = "失败示例（用户名重复）",
                                                    value = "{\"success\":false,\"errorCode\":\"USER_NAME_DUPLICATE\",\"msg\":\"用户名【test_user】已存在\",\"data\":null,\"costTime\":20,\"traceId\":\"REQ_1735689600003_8d7c6b5a\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            )
                                    }
                            )
                    )
            }
    )
    public com.rbac.security.model.ApiResponse createUser(@RequestBody ApiRequest request) {
        request.setPermCode("system:user:create");
        fillDefaultParams(request);
        SecurityChain securityChain = getNewSecurityChain();
        return securityChain.proceed(request);
    }

    @PostMapping("/user/password/reset")
    @Operation(
            summary = "重置用户密码",
            description = "将指定用户密码重置为默认值123456，需system:user:password权限，bizParams需传入userid",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ApiRequest.class),
                            examples = @ExampleObject(
                                    name = "重置密码示例",
                                    value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9.admin.123456789\",\"permCode\":\"system:user:password\",\"bizParams\":\"{\\\"userid\\\":1}\",\"timestamp\":1735689600000}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "重置结果，返回成功/失败提示",
                            content = @Content(
                                    schema = @Schema(implementation = com.rbac.security.model.ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "成功示例",
                                                    value = "{\"success\":true,\"errorCode\":null,\"msg\":\"业务执行成功\",\"data\":\"用户[1]密码重置为[123456]成功\",\"costTime\":25,\"traceId\":\"REQ_1735689600004_9a8b7c6d\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            ),
                                            @ExampleObject(
                                                    name = "失败示例（用户不存在）",
                                                    value = "{\"success\":false,\"errorCode\":\"USER_NOT_EXIST\",\"msg\":\"用户ID【1】不存在\",\"data\":null,\"costTime\":20,\"traceId\":\"REQ_1735689600005_6b5a4d3c\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            )
                                    }
                            )
                    )
            }
    )
    public com.rbac.security.model.ApiResponse resetUserPassword(@RequestBody ApiRequest request) {
        request.setPermCode("system:user:password");
        fillDefaultParams(request);
        SecurityChain securityChain = getNewSecurityChain();
        return securityChain.proceed(request);
    }

    @PostMapping("/user/role/update")
    @Operation(
            summary = "修改用户角色",
            description = "为指定用户分配新角色，需system:user:role权限，bizParams需传入userid和roleid",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ApiRequest.class),
                            examples = @ExampleObject(
                                    name = "修改用户角色示例",
                                    value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9.admin.123456789\",\"permCode\":\"system:user:role\",\"bizParams\":\"{\\\"userid\\\":1,\\\"roleid\\\":2}\",\"timestamp\":1735689600000}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "修改结果，返回成功/失败提示",
                            content = @Content(
                                    schema = @Schema(implementation = com.rbac.security.model.ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "成功示例",
                                                    value = "{\"success\":true,\"errorCode\":null,\"msg\":\"业务执行成功\",\"data\":\"用户[1]角色更新为[2]成功\",\"costTime\":30,\"traceId\":\"REQ_1735689600006_7c6d5e4f\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            ),
                                            @ExampleObject(
                                                    name = "失败示例（角色不存在）",
                                                    value = "{\"success\":false,\"errorCode\":\"ROLE_NOT_EXIST\",\"msg\":\"角色ID【2】不存在\",\"data\":null,\"costTime\":20,\"traceId\":\"REQ_1735689600007_8d7e6f5g\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            )
                                    }
                            )
                    )
            }
    )
    public com.rbac.security.model.ApiResponse updateUserRole(@RequestBody ApiRequest request) {
        request.setPermCode("system:user:role");
        fillDefaultParams(request);
        SecurityChain securityChain = getNewSecurityChain();
        return securityChain.proceed(request);
    }

    @PostMapping("/user/role/get")
    @Operation(
            summary = "查询用户角色",
            description = "获取指定用户的当前角色名称，需system:user:getrole权限，bizParams需传入userid",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ApiRequest.class),
                            examples = @ExampleObject(
                                    name = "查询用户角色示例",
                                    value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9.admin.123456789\",\"permCode\":\"system:user:getrole\",\"bizParams\":\"{\\\"userid\\\":1}\",\"timestamp\":1735689600000}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "查询结果，返回角色名称或提示",
                            content = @Content(
                                    schema = @Schema(implementation = com.rbac.security.model.ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "成功示例",
                                                    value = "{\"success\":true,\"errorCode\":null,\"msg\":\"业务执行成功\",\"data\":\"超级管理员\",\"costTime\":20,\"traceId\":\"REQ_1735689600008_9e8f7g6h\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            ),
                                            @ExampleObject(
                                                    name = "失败示例（用户无角色）",
                                                    value = "{\"success\":false,\"errorCode\":\"USER_ROLE_EMPTY\",\"msg\":\"用户[1]暂无分配角色\",\"data\":null,\"costTime\":15,\"traceId\":\"REQ_1735689600009_0f9e8g7h\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            )
                                    }
                            )
                    )
            }
    )
    public com.rbac.security.model.ApiResponse getUserRole(@RequestBody ApiRequest request) {
        request.setPermCode("system:user:getrole");
        fillDefaultParams(request);
        SecurityChain securityChain = getNewSecurityChain();
        return securityChain.proceed(request);
    }

    @PostMapping("/user/permission/get")
    @Operation(
            summary = "查询用户权限",
            description = "获取指定用户的所有权限列表，需system:user:own:permission权限，bizParams需传入userid",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ApiRequest.class),
                            examples = @ExampleObject(
                                    name = "查询用户权限示例",
                                    value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9.admin.123456789\",\"permCode\":\"system:user:own:permission\",\"bizParams\":\"{\\\"userid\\\":1}\",\"timestamp\":1735689600000}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "查询结果，返回权限列表或提示",
                            content = @Content(
                                    schema = @Schema(implementation = com.rbac.security.model.ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "成功示例",
                                                    value = "{\"success\":true,\"errorCode\":null,\"msg\":\"业务执行成功\",\"data\":[\"system:user:list\",\"system:role:create\"],\"costTime\":25,\"traceId\":\"REQ_1735689600010_1g0f9e8h\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            ),
                                            @ExampleObject(
                                                    name = "失败示例（用户无权限）",
                                                    value = "{\"success\":false,\"errorCode\":\"USER_PERMISSION_EMPTY\",\"msg\":\"用户[1]暂无分配权限\",\"data\":null,\"costTime\":20,\"traceId\":\"REQ_1735689600011_2h1g0f9e\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            )
                                    }
                            )
                    )
            }
    )
    public com.rbac.security.model.ApiResponse getUserPermissions(@RequestBody ApiRequest request) {
        request.setPermCode("system:user:own:permission");
        fillDefaultParams(request);
        SecurityChain securityChain = getNewSecurityChain();
        return securityChain.proceed(request);
    }

    @PostMapping("/user/password/update")
    @Operation(
            summary = "修改自身密码",
            description = "登录用户修改自己的密码，需system:user:own:password权限，bizParams需传入newPassword",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ApiRequest.class),
                            examples = @ExampleObject(
                                    name = "修改自身密码示例",
                                    value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9.admin.123456789\",\"permCode\":\"system:user:own:password\",\"bizParams\":\"{\\\"newPassword\\\":\\\"654321\\\"}\",\"timestamp\":1735689600000}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "修改结果，返回成功/失败提示",
                            content = @Content(
                                    schema = @Schema(implementation = com.rbac.security.model.ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "成功示例",
                                                    value = "{\"success\":true,\"errorCode\":null,\"msg\":\"业务执行成功\",\"data\":\"用户[admin]密码更新成功\",\"costTime\":30,\"traceId\":\"REQ_1735689600012_3i2h1g0f\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            ),
                                            @ExampleObject(
                                                    name = "失败示例（密码规则不符）",
                                                    value = "{\"success\":false,\"errorCode\":\"PASSWORD_RULE_ERROR\",\"msg\":\"新密码不符合规则（长度需≥6位）\",\"data\":null,\"costTime\":20,\"traceId\":\"REQ_1735689600013_4j3i2h1g\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            )
                                    }
                            )
                    )
            }
    )
    public com.rbac.security.model.ApiResponse updateOwnPassword(@RequestBody ApiRequest request) {
        request.setPermCode("system:user:own:password");
        fillDefaultParams(request);
        SecurityChain securityChain = getNewSecurityChain();
        return securityChain.proceed(request);
    }

    @PostMapping("/user/login")
    @Operation(
            summary = "用户登录",
            description = "用户登录接口，无需token，bizParams需传入username和password",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ApiRequest.class),
                            examples = @ExampleObject(
                                    name = "用户登录示例",
                                    value = "{\"permCode\":\"system:user:own:login\",\"bizParams\":\"{\\\"username\\\":\\\"admin\\\",\\\"password\\\":\\\"123456\\\"}\",\"timestamp\":1735689600000}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "登录结果，返回成功/失败提示",
                            content = @Content(
                                    schema = @Schema(implementation = com.rbac.security.model.ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "成功示例",
                                                    value = "{\"success\":true,\"errorCode\":null,\"msg\":\"业务执行成功\",\"data\":\"登录成功\",\"costTime\":20,\"traceId\":\"REQ_1735689600014_5k4j3i2h\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            ),
                                            @ExampleObject(
                                                    name = "失败示例（用户名或密码错误）",
                                                    value = "{\"success\":false,\"errorCode\":\"LOGIN_FAILED\",\"msg\":\"用户名或密码错误\",\"data\":null,\"costTime\":15,\"traceId\":\"REQ_1735689600015_6l5k4j3i\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            )
                                    }
                            )
                    )
            }
    )
    public com.rbac.security.model.ApiResponse login(@RequestBody ApiRequest request) {
        request.setPermCode("system:user:own:login");
        fillDefaultParams(request);
        SecurityChain securityChain = getNewSecurityChain();
        return securityChain.proceed(request);
    }

    @PostMapping("/user/delete")
    @Operation(
            summary = "删除用户",
            description = "删除指定用户，需system:user:delete权限，bizParams需传入userid",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ApiRequest.class),
                            examples = @ExampleObject(
                                    name = "删除用户示例",
                                    value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9.admin.123456789\",\"permCode\":\"system:user:delete\",\"bizParams\":\"{\\\"userid\\\":1}\",\"timestamp\":1735689600000}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "删除结果，返回成功/失败提示",
                            content = @Content(
                                    schema = @Schema(implementation = com.rbac.security.model.ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "成功示例",
                                                    value = "{\"success\":true,\"errorCode\":null,\"msg\":\"业务执行成功\",\"data\":\"用户[1]删除成功\",\"costTime\":25,\"traceId\":\"REQ_1735689600016_7m6l5k4j\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            ),
                                            @ExampleObject(
                                                    name = "失败示例（用户不存在）",
                                                    value = "{\"success\":false,\"errorCode\":\"USER_NOT_EXIST\",\"msg\":\"用户ID【1】不存在\",\"data\":null,\"costTime\":20,\"traceId\":\"REQ_1735689600017_8n7m6l5k\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            )
                                    }
                            )
                    )
            }
    )
    public com.rbac.security.model.ApiResponse deleteUser(@RequestBody ApiRequest request) {
        request.setPermCode("system:user:delete");
        fillDefaultParams(request);
        SecurityChain securityChain = getNewSecurityChain();
        return securityChain.proceed(request);
    }

    // ===================== 角色管理接口（对应system:role:*权限） =====================
    @PostMapping("/role/list")
    @Operation(
            summary = "查询所有角色",
            description = "获取系统中所有角色信息列表，需system:role:list权限，无业务参数，直接返回角色列表",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ApiRequest.class),
                            examples = @ExampleObject(
                                    name = "查询所有角色示例",
                                    value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9.admin.123456789\",\"permCode\":\"system:role:list\",\"bizParams\":\"{}\",\"timestamp\":1735689600000}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "查询成功，返回角色列表（Role实体列表）",
                            content = @Content(
                                    schema = @Schema(implementation = com.rbac.security.model.ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "成功示例",
                                                    value = "{\"success\":true,\"errorCode\":null,\"msg\":\"业务执行成功\",\"data\":[{\"id\":1,\"name\":\"超级管理员\",\"description\":\"系统最高权限\"}],\"costTime\":25,\"traceId\":\"REQ_1735689600014_5k4j3i2h\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            ),
                                            @ExampleObject(
                                                    name = "失败示例（无权限）",
                                                    value = "{\"success\":false,\"errorCode\":\"PERMISSION_DENIED\",\"msg\":\"授权失败：无权访问system:role:list\",\"data\":null,\"costTime\":20,\"traceId\":\"REQ_1735689600015_6l5k4j3i\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            )
                                    }
                            )
                    )
            }
    )
    public com.rbac.security.model.ApiResponse getRoleList(@RequestBody ApiRequest request) {
        request.setPermCode("system:role:list");
        request.setBizParams("{}"); // 无业务参数，默认空JSON
        fillDefaultParams(request);
        SecurityChain securityChain = getNewSecurityChain();
        return securityChain.proceed(request);
    }

    @PostMapping("/role/create")
    @Operation(
            summary = "新增角色",
            description = "创建新角色，需system:role:create权限，bizParams需传入roleName和description",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ApiRequest.class),
                            examples = @ExampleObject(
                                    name = "新增角色示例",
                                    value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9.admin.123456789\",\"permCode\":\"system:role:create\",\"bizParams\":\"{\\\"roleName\\\":\\\"普通用户\\\",\\\"description\\\":\\\"仅基础操作权限\\\"}\",\"timestamp\":1735689600000}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "新增结果，返回成功/失败提示",
                            content = @Content(
                                    schema = @Schema(implementation = com.rbac.security.model.ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "成功示例",
                                                    value = "{\"success\":true,\"errorCode\":null,\"msg\":\"业务执行成功\",\"data\":\"新增角色[普通用户]成功\",\"costTime\":30,\"traceId\":\"REQ_1735689600016_7m6l5k4j\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            ),
                                            @ExampleObject(
                                                    name = "失败示例（角色名重复）",
                                                    value = "{\"success\":false,\"errorCode\":\"ROLE_NAME_DUPLICATE\",\"msg\":\"角色名称【普通用户】已存在\",\"data\":null,\"costTime\":20,\"traceId\":\"REQ_1735689600017_8n7m6l5k\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            )
                                    }
                            )
                    )
            }
    )
    public com.rbac.security.model.ApiResponse createRole(@RequestBody ApiRequest request) {
        request.setPermCode("system:role:create");
        fillDefaultParams(request);
        SecurityChain securityChain = getNewSecurityChain();
        return securityChain.proceed(request);
    }

    @PostMapping("/role/permission/assign")
    @Operation(
            summary = "角色分配权限",
            description = "为指定角色分配多个权限，需system:role:permission权限，bizParams需传入roleId和permIdList（权限ID列表）",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ApiRequest.class),
                            examples = @ExampleObject(
                                    name = "角色分配权限示例",
                                    value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9.admin.123456789\",\"permCode\":\"system:role:permission\",\"bizParams\":\"{\\\"roleId\\\":2,\\\"permIdList\\\":[1,3,5]}\",\"timestamp\":1735689600000}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "分配结果，返回成功/失败提示",
                            content = @Content(
                                    schema = @Schema(implementation = com.rbac.security.model.ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "成功示例",
                                                    value = "{\"success\":true,\"errorCode\":null,\"msg\":\"业务执行成功\",\"data\":\"角色[2]权限更新成功\",\"costTime\":35,\"traceId\":\"REQ_1735689600018_9o8n7m6l\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            ),
                                            @ExampleObject(
                                                    name = "失败示例（权限不存在）",
                                                    value = "{\"success\":false,\"errorCode\":\"PERMISSION_NOT_EXIST\",\"msg\":\"权限ID【5】不存在\",\"data\":null,\"costTime\":25,\"traceId\":\"REQ_1735689600019_0p9o8n7m\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{\"invalidPermIds\":[5]}}"
                                            )
                                    }
                            )
                    )
            }
    )
    public com.rbac.security.model.ApiResponse assignRolePermissions(@RequestBody ApiRequest request) {
        request.setPermCode("system:role:permission");
        fillDefaultParams(request);
        SecurityChain securityChain = getNewSecurityChain();
        return securityChain.proceed(request);
    }

    // ===================== 权限管理接口（对应system:permission:*权限） =====================
    @PostMapping("/permission/list")
    @Operation(
            summary = "查询所有权限",
            description = "获取系统中所有权限信息列表，需system:permission:list权限，无业务参数，直接返回权限列表",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ApiRequest.class),
                            examples = @ExampleObject(
                                    name = "查询所有权限示例",
                                    value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9.admin.123456789\",\"permCode\":\"system:permission:list\",\"bizParams\":\"{}\",\"timestamp\":1735689600000}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "查询成功，返回权限列表（Permission实体列表）",
                            content = @Content(
                                    schema = @Schema(implementation = com.rbac.security.model.ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "成功示例",
                                                    value = "{\"success\":true,\"errorCode\":null,\"msg\":\"业务执行成功\",\"data\":[{\"id\":1,\"name\":\"用户列表查看\",\"code\":\"system:user:list\",\"parentId\":0}],\"costTime\":25,\"traceId\":\"REQ_1735689600020_1q0p9o8n\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            ),
                                            @ExampleObject(
                                                    name = "失败示例（无权限）",
                                                    value = "{\"success\":false,\"errorCode\":\"PERMISSION_DENIED\",\"msg\":\"授权失败：无权访问system:permission:list\",\"data\":null,\"costTime\":20,\"traceId\":\"REQ_1735689600021_2r1q0p9o\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            )
                                    }
                            )
                    )
            }
    )
    public com.rbac.security.model.ApiResponse getPermissionList(@RequestBody ApiRequest request) {
        request.setPermCode("system:permission:list");
        request.setBizParams("{}"); // 无业务参数，默认空JSON
        fillDefaultParams(request);
        SecurityChain securityChain = getNewSecurityChain();
        return securityChain.proceed(request);
    }

    @PostMapping("/permission/create")
    @Operation(
            summary = "新增权限",
            description = "创建新权限，需system:permission:create权限，bizParams需传入permission_name、code、parent_id",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ApiRequest.class),
                            examples = @ExampleObject(
                                    name = "新增权限示例",
                                    value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9.admin.123456789\",\"permCode\":\"system:permission:create\",\"bizParams\":\"{\\\"permission_name\\\":\\\"角色列表查看\\\",\\\"code\\\":\\\"system:role:list\\\",\\\"parent_id\\\":0}\",\"timestamp\":1735689600000}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "新增结果，返回成功/失败提示",
                            content = @Content(
                                    schema = @Schema(implementation = com.rbac.security.model.ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "成功示例",
                                                    value = "{\"success\":true,\"errorCode\":null,\"msg\":\"业务执行成功\",\"data\":\"新增权限[角色列表查看]成功\",\"costTime\":30,\"traceId\":\"REQ_1735689600022_3s2r1q0p\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            ),
                                            @ExampleObject(
                                                    name = "失败示例（权限编码重复）",
                                                    value = "{\"success\":false,\"errorCode\":\"PERM_CODE_DUPLICATE\",\"msg\":\"权限编码【system:role:list】已存在\",\"data\":null,\"costTime\":20,\"traceId\":\"REQ_1735689600023_4t3s2r1q\",\"timestamp\":\"2025-12-01T10:00:00\",\"ext\":{}}"
                                            )
                                    }
                            )
                    )
            }
    )
    public com.rbac.security.model.ApiResponse createPermission(@RequestBody ApiRequest request) {
        request.setPermCode("system:permission:create");
        fillDefaultParams(request);
        SecurityChain securityChain = getNewSecurityChain();
        return securityChain.proceed(request);
    }

    /**
     * 填充默认参数（timestamp/bizParams）
     */
    private void fillDefaultParams(ApiRequest request) {
        if (request.getTimestamp() == null) {
            request.setTimestamp(System.currentTimeMillis());
        }
        if (request.getBizParams() == null) {
            request.setBizParams("{}");
        }
    }
}