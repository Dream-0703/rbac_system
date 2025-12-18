import request from '@/utils/request';
import type { ApiRequest, ApiResponse, Permission } from '@/types/api';

/**
 * 查询所有权限（需要system:permission:list权限）
 * @param token 登录令牌
 */
export const getPermissionList = async (token: string): Promise<ApiResponse<Permission[]>> => {
  const reqData: ApiRequest = {
    token,
    permCode: 'system:permission:list',
    bizParams: '{}' // 无业务参数
  };
  return request.post('/permission/list', reqData);
};

/**
 * 新增权限（需要system:permission:create权限）
 * @param token 登录令牌
 * @param permission 权限对象（permission_name, code, parent_id）
 */
export const createPermission = async (
  token: string,
  permission: { permission_name: string; code: string; parent_id: number }
): Promise<ApiResponse<string>> => {
  const reqData: ApiRequest = {
    token,
    permCode: 'system:permission:create',
    bizParams: JSON.stringify(permission)
  };
  return request.post('/permission/create', reqData);
};