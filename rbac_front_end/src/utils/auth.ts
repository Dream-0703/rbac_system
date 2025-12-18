// src/utils/auth.ts
import type { User } from '@/types/api';

/**
 * 获取当前登录用户信息
 * @returns 用户信息或null
 */
export const getCurrentUser = (): User | null => {
  const userInfoStr = localStorage.getItem('userInfo');
  if (userInfoStr) {
    try {
      return JSON.parse(userInfoStr) as User;
    } catch (error) {
      console.error('解析用户信息失败:', error);
      return null;
    }
  }
  return null;
};

/**
 * 获取当前登录token
 * @returns token或空字符串
 */
export const getCurrentToken = (): string => {
  return localStorage.getItem('token') || '';
};
