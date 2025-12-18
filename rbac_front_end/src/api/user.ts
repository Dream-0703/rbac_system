import request from '@/utils/request';
import type { ApiRequest, ApiResponse, User } from '@/types/api';
import { getCurrentUser, getCurrentToken } from '@/utils/auth';

/**
 * 用户登录
 * @param username 用户名
 * @param password 密码
 */
export const login = async (
  username: string,
  password: string
): Promise<ApiResponse<string>> => {
  const reqData: ApiRequest = {
    token: '',
    permCode: 'system:user:own:login',
    bizParams: JSON.stringify({ username, password })
  };
  return request.post('/user/login', reqData);
};



/**
 * 查询所有用户（对应后端 system:user:list 权限）
 */
export const getUserList = async (token: string): Promise<ApiResponse<User[]>> => {
  const reqData: ApiRequest = {
    token: token,
    permCode: 'system:user:list',  // 与后端switch case匹配
    bizParams: '{}',  // 无业务参数，传空JSON（对应后端bizParams解析）
    user: getCurrentUser() || undefined
  };
  return request.post('/user/list', reqData);  // 对应后端 @PostMapping("/user/list")
};

/**
 * 新增用户（对应后端 system:user:create 权限）
 * @param token 登录令牌
 * @param username 用户名（后端校验重复）
 * @param password 密码
 */
export const createUser = async (
  token: string,
  username: string,
  password: string
): Promise<ApiResponse<string>> => {
  const reqData: ApiRequest = {
    token: token,
    permCode: 'system:user:create',
    bizParams: JSON.stringify({ username, password }),  // 后端解析为Map<String, String>
    user: getCurrentUser() || undefined
  };
  return request.post('/user/create', reqData);  // 对应后端 @PostMapping("/user/create")
};

/**
 * 重置用户密码（对应后端 system:user:password 权限）
 * @param token 登录令牌
 * @param userId 用户ID（后端参数名userid，注意小写）
 */
export const resetUserPassword = async (
  token: string,
  userId: number
): Promise<ApiResponse<string>> => {
  const reqData: ApiRequest = {
    token,
    permCode: 'system:user:password',
    bizParams: JSON.stringify({ userid: userId })  // 与后端paramMap.get("userid")匹配
  };
  return request.post('/user/password/reset', reqData);  // 对应后端重置密码接口
};

/**
 * 修改用户角色（对应后端 system:user:role 权限）
 * @param token 登录令牌
 * @param userId 用户ID（userid）
 * @param roleId 角色ID（roleid）
 */
export const updateUserRole = async (
  token: string,
  userId: number,
  roleId: number
): Promise<ApiResponse<string>> => {
  const reqData: ApiRequest = {
    token,
    permCode: 'system:user:role',
    bizParams: JSON.stringify({ userid: userId, roleid: roleId })  // 与后端参数名匹配
  };
  return request.post('/user/role/update', reqData);  // 对应后端修改角色接口
};

/**
 * 查询用户角色（对应后端 system:user:own:role 权限）
 * @param token 登录令牌
 * @param userId 用户ID（后端参数名userid，注意小写）
 */
export const getUserRole = async (
  token: string,
  userId: number
): Promise<ApiResponse<string>> => {
  const reqData: ApiRequest = {
    token,
    permCode: 'system:user:own:role',
    bizParams: JSON.stringify({ userid: userId }),  // 与后端paramMap.get("userid")匹配
    user: getCurrentUser() || undefined
  };
  return request.post('/user/role/get', reqData);  // 对应后端查询用户角色接口
};

/**
 * 查询用户权限（对应后端 system:user:own:permission 权限）
 * @param token 登录令牌
 * @param userId 用户ID（后端参数名userid，注意小写）
 */
export const getUserPermissions = async (
  token: string,
  userId: number
): Promise<ApiResponse<string[]>> => {
  const reqData: ApiRequest = {
    token,
    permCode: 'system:user:own:permission',
    bizParams: JSON.stringify({ userid: userId })  // 与后端paramMap.get("userid")匹配
  };
  return request.post('/user/permission/get', reqData);  // 对应后端查询用户权限接口
};

/**
 * 修改自身密码（对应后端 system:user:own:password 权限）
 * @param token 登录令牌
 * @param newPassword 新密码
 */
export const updateOwnPassword = async (
  token: string,
  newPassword: string
): Promise<ApiResponse<string>> => {
  const reqData: ApiRequest = {
    token,
    permCode: 'system:user:own:password',
    bizParams: JSON.stringify({ newPassword })  // 与后端paramMap.get("newPassword")匹配
  };
  return request.post('/user/password/update', reqData);  // 对应后端修改自身密码接口
};

/**
 * 删除用户（对应后端 system:user:delete 权限）
 * @param token 登录令牌
 * @param userId 用户ID（后端参数名userid，注意小写）
 */
export const deleteUser = async (
  token: string,
  userId: number
): Promise<ApiResponse<string>> => {
  const reqData: ApiRequest = {
    token,
    permCode: 'system:user:delete',
    bizParams: JSON.stringify({ userid: userId })  // 与后端paramMap.get("userid")匹配
  };
  return request.post('/user/delete', reqData);  // 对应后端删除用户接口
};