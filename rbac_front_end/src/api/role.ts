import request from '@/utils/request';
import type { ApiRequest, ApiResponse, Role } from '@/types/api';

/**
 * 查询所有角色（需要system:role:list权限）
 * @param token 登录令牌
 */
export const getRoleList = async (token: string): Promise<ApiResponse<Role[]>> => {
  const reqData: ApiRequest = {
    token,
    permCode: 'system:role:list',
    bizParams: '{}' // 无业务参数
  };
  return request.post('/role/list', reqData);
};

/**
 * 新增角色（需要system:role:create权限）
 * @param token 登录令牌
 * @param roleName 角色名称
 * @param description 角色描述（可选）
 */
export const createRole = async (
  token: string,
  roleName: string,
  description?: string
): Promise<ApiResponse<string>> => {
  const reqData: ApiRequest = {
    token,
    permCode: 'system:role:create',
    bizParams: JSON.stringify({ roleName, description: description || '' })
  };
  return request.post('/role/create', reqData);
};

/**
 * 为角色分配权限（需要system:role:permission权限）
 * @param token 登录令牌
 * @param roleId 角色ID
 * @param permIdList 权限ID列表
 */
export const assignPermissionsToRole = async (
  token: string,
  roleId: number,
  permIdList: number[]
): Promise<ApiResponse<string>> => {
  const reqData: ApiRequest = {
    token,
    permCode: 'system:role:permission',
    bizParams: JSON.stringify({ roleId, permIdList })
  };
  return request.post('/role/permission/assign', reqData);
};

