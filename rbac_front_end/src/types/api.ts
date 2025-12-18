// src/types/api.ts

/**
 * 对应后端 User.java（参考toString方法）
 */
export interface User {
  id: number;
  username: string;
  password: string;
  avatar?: string;
  createdAt?: string;
}

/**
 * 对应后端 Role.java（参考toString方法）
 */
export interface Role {
  id: number;
  name: string;
  description?: string;
}

/**
 * 对应后端 Permission.java（参考toString方法）
 */
export interface Permission {
  id: number;
  name: string;
  code: string;
  parentId: number;
}

/**
 * 对应后端 ApiRequest.java（请求参数格式）
 */
export interface ApiRequest {
  token: string;
  permCode: string;
  bizParams?: string;
  timestamp?: number;
  user?: User;
}

/**
 * 对应后端 ApiResponse.java（响应格式）
 * 移除any类型，使用泛型严格约束data字段
 */
export interface ApiResponse<T = unknown> {  // 用unknown替代any，强制类型检查
  success: boolean;
  msg: string;
  data: T;  // 具体类型由调用处指定
  costTime?: number;
  errorCode?: string;
  traceId?: string;
  timestamp?: string;
  ext?: Record<string, unknown>;  // 替代any类型的Map
}