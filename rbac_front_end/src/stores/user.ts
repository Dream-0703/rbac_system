import { defineStore } from 'pinia';
import { login, getUserRole, getUserPermissions } from '@/api/user';
import type { User } from '@/types/api';

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null') as User | null,
    userRole: localStorage.getItem('userRole') || '',
    userPermissions: JSON.parse(localStorage.getItem('userPermissions') || '[]') as string[],
    isLoggedIn: !!localStorage.getItem('token')
  }),

  actions: {
    // 保存用户信息到localStorage
    saveToLocalStorage() {
      localStorage.setItem('token', this.token);
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo));
      localStorage.setItem('userRole', this.userRole);
      localStorage.setItem('userPermissions', JSON.stringify(this.userPermissions));
      localStorage.setItem('isLoggedIn', JSON.stringify(this.isLoggedIn));
    },
    // 登录
    async login(username: string, password: string) {
      try {
        const response = await login(username, password);
        // 登录成功后，使用用户名作为token（后端当前不返回token）
        if (response.success) {
          const token = username;
          this.token = token;
          this.isLoggedIn = true;
          // 模拟用户信息（后端当前不返回用户详情）
          this.userInfo = { id: 1, username, password: '' } as User;
          
          // 登录成功后从后端获取实际权限和角色
      await this.getUserPermissionList();
      await this.getUserRoleInfo();
      
      // 如果后端没有返回权限，则使用默认权限列表（临时处理，后续移除）
      if (this.userPermissions.length === 0) {
        this.userPermissions = ['dashboard:view', 'system:user:list', 'system:user:create', 'system:user:delete', 'system:user:role', 'system:user:password', 'system:user:own:role', 'system:user:own:permission', 'system:user:own:password', 'system:role:list', 'system:role:create', 'system:role:permission', 'system:permission:list', 'system:permission:tree', 'system:permission:create'];
      }
      
      // 保存到localStorage
      this.saveToLocalStorage();
      // 额外存储username以便拦截器使用
      localStorage.setItem('username', username);
      
      // 重置动态路由加载标记，确保下次导航时重新加载动态路由
      import('@/router').then((routerModule) => {
        routerModule.routeState.isAsyncRoutesLoaded = false;
      });
          return true;
        }
        return false;
      } catch (error) {
        console.error('登录失败:', error);
        return false;
      }
    },



    // 获取用户角色
    async getUserRoleInfo() {
      try {
        if (this.userInfo?.id) {
          const response = await getUserRole(this.token, this.userInfo.id);
          this.userRole = response.data;
        }
      } catch (error) {
        console.error('获取用户角色失败:', error);
      }
    },

    // 获取用户权限列表
    async getUserPermissionList() {
      try {
        if (this.userInfo?.id) {
          const response = await getUserPermissions(this.token, this.userInfo.id);
          if (response.data) {
            this.userPermissions = response.data as string[];
          }
        }
      } catch (error) {
        console.error('获取用户权限失败:', error);
      }
    },

    // 登出
    logout() {
      this.token = '';
      this.userInfo = null;
      this.userRole = '';
      this.userPermissions = [];
      this.isLoggedIn = false;
      // 清除localStorage中的所有用户信息
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      localStorage.removeItem('userRole');
      localStorage.removeItem('userPermissions');
      localStorage.removeItem('isLoggedIn');
      localStorage.removeItem('username');
    },

    // 清除token
    clearToken() {
      this.token = '';
      this.isLoggedIn = false;
      localStorage.removeItem('token');
      localStorage.removeItem('isLoggedIn');
    }
  }
});
