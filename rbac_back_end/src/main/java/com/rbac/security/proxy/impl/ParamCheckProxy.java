package com.rbac.security.proxy.impl;

import com.rbac.security.model.ApiRequest;
import com.rbac.security.model.ApiResponse;
import com.rbac.security.proxy.SecurityChain;
import com.rbac.security.proxy.SecurityProxy;
import com.rbac.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 安全代理4：参数校验代理（适配RealBusinessService全量接口）
 * 特点：覆盖所有业务接口的参数校验，包含必填项、类型、格式校验
 */
@Slf4j
@Component
public class ParamCheckProxy implements SecurityProxy {
    @Override
    public ApiResponse execute(ApiRequest request, SecurityChain chain) {
        log.info("执行参数校验...");
        log.info("请求参数：{}", request);
        String permCode = request.getPermCode();
        String bizParams = request.getBizParams();

        // 空权限编码直接校验失败
        if (permCode == null || permCode.isEmpty()) {
            return new ApiResponse(false, "参数校验失败：权限编码不能为空", null);
        }
        log.info("接口{}参数校验开始，参数：{}", permCode, bizParams);

        // 按不同接口针对性校验参数
        switch (permCode) {
            // ========== 1. 用户管理接口 ==========
            case "system:user:list":
                // 查看用户列表：无参数，直接通过
                log.info("接口{}无需参数校验，跳过", permCode);
                break;

            case "system:user:create":
                // 新增用户：必填username、password（字符串，非空）
                if (bizParams == null || bizParams.isEmpty()) {
                    return new ApiResponse(false, "参数校验失败：新增用户参数不能为空", null);
                }
                try {
                    Map<String, String> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, String>>() {});
                    if (paramMap.get("username") == null || paramMap.get("username").trim().isEmpty()) {
                        return new ApiResponse(false, "参数校验失败：用户名不能为空", null);
                    }
                    if (paramMap.get("password") == null || paramMap.get("password").trim().isEmpty()) {
                        return new ApiResponse(false, "参数校验失败：密码不能为空", null);
                    }
                } catch (Exception e) {
                    return new ApiResponse(false, "参数校验失败：新增用户参数格式错误（需JSON对象，包含username/password字符串）", null);
                }
                break;

            case "system:user:password":
                // 重置用户密码：必填userid（整数，大于0）
                if (bizParams == null || bizParams.isEmpty()) {
                    return new ApiResponse(false, "参数校验失败：重置密码参数不能为空", null);
                }
                try {
                    Map<String, Object> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, Object>>() {});
                    Integer userid = (Integer) paramMap.get("userid");
                    if (userid == null || userid <= 0) {
                        return new ApiResponse(false, "参数校验失败：用户ID必须是大于0的整数", null);
                    }
                } catch (Exception e) {
                    return new ApiResponse(false, "参数校验失败：重置密码参数格式错误（需JSON对象，包含userid整数）", null);
                }
                break;

            case "system:user:role":
                // 更改用户角色：必填userid、roleid（均为整数，大于0）
                if (bizParams == null || bizParams.isEmpty()) {
                    return new ApiResponse(false, "参数校验失败：更改用户角色参数不能为空", null);
                }
                try {
                    Map<String, Integer> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, Integer>>() {});
                    Integer userid = paramMap.get("userid");
                    Integer roleid = paramMap.get("roleid");
                    if (userid == null || userid <= 0) {
                        return new ApiResponse(false, "参数校验失败：用户ID必须是大于0的整数", null);
                    }
                    if (roleid == null || roleid <= 0) {
                        return new ApiResponse(false, "参数校验失败：角色ID必须是大于0的整数", null);
                    }
                } catch (Exception e) {
                    return new ApiResponse(false, "参数校验失败：更改用户角色参数格式错误（需JSON对象，包含userid/roleid整数）", null);
                }
                break;
            case "system:user:getrole":
                if (bizParams == null || bizParams.isEmpty()) {
                    return new ApiResponse(false, "参数校验失败：查询用户角色参数不能为空", null);
                }
                try {
                    Map<String, Integer> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, Integer>>() {});
                    Integer userid = paramMap.get("userid");
                    if (userid == null || userid <= 0) {
                        return new ApiResponse(false, "参数校验失败：用户ID必须是大于0的整数", null);
                    }
                } catch (Exception e) {
                    return new ApiResponse(false, "参数校验失败：查询用户角色参数格式错误（需JSON对象，包含userid整数）", null);
                }
                break;
            case "system:user:own:role":
                // 查看用户角色：必填userid（整数，大于0）
                if (bizParams == null || bizParams.isEmpty()) {
                    return new ApiResponse(false, "参数校验失败：查看用户角色参数不能为空", null);
                }
                try {
                    Map<String, Integer> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, Integer>>() {});
                    Integer userid = paramMap.get("userid");
                    if (userid == null || userid <= 0) {
                        return new ApiResponse(false, "参数校验失败：用户ID必须是大于0的整数", null);
                    }
                } catch (Exception e) {
                    return new ApiResponse(false, "参数校验失败：查看用户角色参数格式错误（需JSON对象，包含userid整数）", null);
                }
                break;

            case "system:user:own:permission":
                // 查看用户权限：必填userid（整数，大于0）
                if (bizParams == null || bizParams.isEmpty()) {
                    return new ApiResponse(false, "参数校验失败：查看用户权限参数不能为空", null);
                }
                try {
                    Map<String, Integer> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, Integer>>() {});
                    Integer userid = paramMap.get("userid");
                    if (userid == null || userid <= 0) {
                        return new ApiResponse(false, "参数校验失败：用户ID必须是大于0的整数", null);
                    }
                } catch (Exception e) {
                    return new ApiResponse(false, "参数校验失败：查看用户权限参数格式错误（需JSON对象，包含userid整数）", null);
                }
                break;

            case "system:user:own:password":
                // 修改自身密码：必填newPassword（字符串，非空）
                if (bizParams == null || bizParams.isEmpty()) {
                    return new ApiResponse(false, "参数校验失败：修改密码参数不能为空", null);
                }
                try {
                    Map<String, Object> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, Object>>() {});
                    String newPassword = (String) paramMap.get("newPassword");
                    if (newPassword == null || newPassword.trim().isEmpty()) {
                        return new ApiResponse(false, "参数校验失败：新密码不能为空", null);
                    }
                } catch (Exception e) {
                    return new ApiResponse(false, "参数校验失败：修改密码参数格式错误（需JSON对象，包含newPassword字符串）", null);
                }
                break;

            // ========== 2. 角色管理接口 ==========
            case "system:role:list":
                // 查看角色列表：无参数，直接通过
                log.info("接口{}无需参数校验，跳过", permCode);
                break;

            case "system:role:create":
                // 新增角色：必填roleName（字符串，非空），description可选
                if (bizParams == null || bizParams.isEmpty()) {
                    return new ApiResponse(false, "参数校验失败：新增角色参数不能为空", null);
                }
                try {
                    Map<String, String> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, String>>() {});
                    if (paramMap.get("roleName") == null || paramMap.get("roleName").trim().isEmpty()) {
                        return new ApiResponse(false, "参数校验失败：角色名称不能为空", null);
                    }
                } catch (Exception e) {
                    return new ApiResponse(false, "参数校验失败：新增角色参数格式错误（需JSON对象，包含roleName字符串）", null);
                }
                break;

            case "system:role:permission":
                // 给角色分配权限：必填roleId（角色ID，整数>0）、permIdList（权限ID列表）
                if (bizParams == null || bizParams.isEmpty()) {
                    return new ApiResponse(false, "参数校验失败：分配角色权限参数不能为空", null);
                }
                try {
                    Map<String, Object> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, Object>>() {});
                    // 修复：参数名从userId改为roleId
                    Integer roleId = (Integer) paramMap.get("roleId");
                    if (roleId == null || roleId <= 0) {
                        return new ApiResponse(false, "参数校验失败：角色ID必须是大于0的整数", null);
                    }
                    // 校验权限ID列表
                    Object permIdObj = paramMap.get("permIdList");
                    if (!(permIdObj instanceof List)) {
                        return new ApiResponse(false, "参数校验失败：permIdList必须是数组格式", null);
                    }
                    List<?> permIdList = (List<?>) permIdObj;
                    if (permIdList.isEmpty()) {
                        return new ApiResponse(false, "参数校验失败：权限ID列表不能为空", null);
                    }
                    // 校验列表中每个权限ID都是整数且大于0
                    for (Object obj : permIdList) {
                        if (!(obj instanceof Integer) || (Integer) obj <= 0) {
                            return new ApiResponse(false, "参数校验失败：权限ID必须是大于0的整数", null);
                        }
                    }
                } catch (Exception e) {
                    return new ApiResponse(false, "参数校验失败：分配角色权限参数格式错误", null);
                }
                break;

            // ========== 3. 权限管理接口 ==========
            case "system:permission:list":
                // 查看权限列表：无参数，直接通过
                log.info("接口{}无需参数校验，跳过", permCode);
                break;

            case "system:permission:create":
                // 新增权限：必填permission_name、code（字符串非空），parentId可选（整数≥0）
                if (bizParams == null || bizParams.isEmpty()) {
                    return new ApiResponse(false, "参数校验失败：新增权限参数不能为空", null);
                }
                try {
                    Map<String, Object> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, Object>>() {});
                    // 校验权限名称
                    String permissionName = (String) paramMap.get("permission_name");
                    if (permissionName == null || permissionName.trim().isEmpty()) {
                        return new ApiResponse(false, "参数校验失败：权限名称不能为空", null);
                    }
                    // 校验权限编码
                    String code = (String) paramMap.get("code");
                    if (code == null || code.trim().isEmpty()) {
                        return new ApiResponse(false, "参数校验失败：权限编码不能为空", null);
                    }
                    // 校验父ID（可选，但如果传了必须是≥0的整数）
                    Integer parentId = (Integer) paramMap.get("parent_id");
                    if (parentId != null && parentId < 0) {
                        return new ApiResponse(false, "参数校验失败：父权限ID必须是大于等于0的整数", null);
                    }
                } catch (Exception e) {
                    return new ApiResponse(false, "参数校验失败：新增权限参数格式错误（需JSON对象，包含permission_name/code字符串、parent_id可选整数）", null);
                }
                break;

            case "syetem:permission:edit":
                // 修改权限：必填permission_id（整数>0）、permission_name、code（字符串非空），parentId可选（整数≥0）
                if (bizParams == null || bizParams.isEmpty()) {
                    return new ApiResponse(false, "参数校验失败：修改权限参数不能为空", null);
                }
                try {
                    Map<String, Object> paramMap = JsonUtil.parseJson(bizParams, new TypeReference<Map<String, Object>>() {});
                    // 校验权限ID
                    Integer permissionId = (Integer) paramMap.get("permission_id");
                    if (permissionId == null || permissionId <= 0) {
                        return new ApiResponse(false, "参数校验失败：权限ID必须是大于0的整数", null);
                    }
                    // 校验权限名称
                    String permissionName = (String) paramMap.get("permission_name");
                    if (permissionName == null || permissionName.trim().isEmpty()) {
                        return new ApiResponse(false, "参数校验失败：权限名称不能为空", null);
                    }
                    // 校验权限编码
                    String code = (String) paramMap.get("code");
                    if (code == null || code.trim().isEmpty()) {
                        return new ApiResponse(false, "参数校验失败：权限编码不能为空", null);
                    }
                    // 校验父ID（可选，但如果传了必须是≥0的整数）
                    Integer parentId = (Integer) paramMap.get("parent_id");
                    if (parentId != null && parentId < 0) {
                        return new ApiResponse(false, "参数校验失败：父权限ID必须是大于等于0的整数", null);
                    }
                } catch (Exception e) {
                    return new ApiResponse(false, "参数校验失败：修改权限参数格式错误（需JSON对象，包含permission_id整数、permission_name/code字符串、parent_id可选整数）", null);
                }
                break;

            // ========== 其他接口 ==========
            default:
                log.info("接口{}无匹配的校验规则，跳过严格校验", permCode);
                break;
        }

        log.info("参数校验成功：接口{}的参数合法", permCode);
        // 执行下一个代理（所有校验通过）
        return chain.proceed(request);
    }
}