import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useUserStore } from '@/stores/user'; // Pinia状态管理（存储用户权限）
import Layout from '@/layout/index.vue'; // 主布局组件

// 静态路由（无需权限即可访问）
const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { hidden: true } // 不在侧边栏显示
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { hidden: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { 
          title: '仪表盘', 
          icon: 'home', 
          permCode: 'dashboard:view' // 仪表盘查看权限（后端需定义）
        }
      }
    ]
  }
];

// 动态路由（需权限校验）
const asyncRoutes: RouteRecordRaw[] = [
  // ========== 用户管理模块（对应system:user:*权限） ==========
  {    path: '/user/list',
    component: Layout,
    meta: {
      title: '用户管理',
      icon: 'user',
      permCode: 'system:user' // 模块总权限（用于侧边栏显示控制）
    },
    children: [
      {
        path: '', // 默认子路由
        name: 'UserList',
        component: () => import('@/views/user/list.vue'),
        meta: {
          title: '用户列表',
          permCode: 'system:user:list' // 对应后端查询权限
        }
      }
    ]
  },

  // ========== 角色管理模块（对应system:role:*权限） ==========
  {
    path: '/role',
    component: Layout,
    redirect: '/role/list',
    meta: {
      title: '角色管理',
      icon: 'role',
      permCode: 'system:role' // 模块总权限
    },
    children: [
      {
        path: 'list',
        name: 'RoleList',
        component: () => import('@/views/role/list.vue'),
        meta: {
          title: '角色列表',
          permCode: 'system:role:list' // 对应后端查询权限
        }
      },
      {
        path: 'create',
        name: 'RoleCreate',
        component: () => import('@/views/role/create.vue'),
        meta: {
          title: '新增角色',
          permCode: 'system:role:create' // 对应后端新增权限
        }
      },
      {
        path: 'assign-permission',
        name: 'RoleAssignPermission',
        component: () => import('@/views/role/assign-permission.vue'),
        meta: {
          title: '分配权限',
          permCode: 'system:role:permission' // 对应后端分配权限
        }
      }
    ]
  },

  // ========== 权限管理模块（对应system:permission:*权限） ==========
  {
    path: '/permission',
    component: Layout,
    redirect: '/permission/list',
    meta: {
      title: '权限管理',
      icon: 'permission',
      permCode: 'system:permission' // 模块总权限
    },
    children: [
      {
        path: 'list',
        name: 'PermissionList',
        component: () => import('@/views/permission/list.vue'),
        meta: {
          title: '权限列表',
          permCode: 'system:permission:list' // 对应后端查询权限
        }
      },
      {
        path: 'create',
        name: 'PermissionCreate',
        component: () => import('@/views/permission/create.vue'),
        meta: {
          title: '新增权限',
          permCode: 'system:permission:create' // 对应后端新增权限
        }
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: constantRoutes
});

// 用于标记动态路由是否已经加载
export const routeState = {
  isAsyncRoutesLoaded: false
};

// 路由守卫：仅保留登录状态检查（权限校验由后端处理）
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();
  const token = userStore.token;

  // 未登录：跳转登录页
  if (!token && to.name !== 'Login') {
    next({ name: 'Login' });
    return;
  }

  // 已登录访问登录页：跳转首页
  if (token && to.name === 'Login') {
    next({ name: 'Dashboard' });
    return;
  }

  // 已登录：确保动态路由已加载
  if (token) {
    // 检查是否需要重新加载动态路由
    const wasLoaded = routeState.isAsyncRoutesLoaded;
    // 加载动态路由（无需权限过滤）
    loadAsyncRoutes();
    
    // 如果动态路由刚刚加载完成，需要重新导航到当前路径才能让路由生效
    if (!wasLoaded && routeState.isAsyncRoutesLoaded) {
      // 使用replace: true避免导航历史记录重复
      next({ ...to, replace: true });
      return;
    }
  }

  // 所有路由直接放行（权限校验由后端处理）
  next();
});

// 动态添加路由（直接加载所有路由，权限由后端处理）
export const loadAsyncRoutes = () => {
  // 检查动态路由是否已经加载，如果已加载则直接返回
  if (routeState.isAsyncRoutesLoaded) {
    return;
  }

  console.log('Loading all dynamic routes (permission checked by backend)');

  // 直接使用所有动态路由，不进行权限过滤
  const allRoutes = asyncRoutes;

  console.log('All routes to load:', allRoutes.length);
  
  // 添加到路由表
  allRoutes.forEach(route => {
    console.log('Adding route:', route.path);
    router.addRoute(route);
  });
  
  // 标记动态路由已加载
  routeState.isAsyncRoutesLoaded = true;
  console.log('Dynamic routes loaded successfully');
};

export default router;