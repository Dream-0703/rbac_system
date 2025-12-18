<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
      <!-- 侧边栏头部 -->
      <div class="sidebar-header">
        <div class="logo">RBAC</div>
        <el-button
          type="text"
          :icon="sidebarCollapsed ? Expand : Fold"
          @click="toggleSidebar"
          size="large"
        ></el-button>
      </div>
      
      <!-- 侧边栏菜单 -->
      <el-scrollbar class="sidebar-scrollbar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          :collapse="sidebarCollapsed"
          router
          text-color="#bfcbd9"
          active-text-color="#409eff"
          background-color="#001529"
        >
          <template v-for="menu in sidebarMenus" :key="menu.path">
            <!-- 有子路由的菜单 -->
            <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.path">
              <template #title>
                <el-icon><component :is="menu.icon || Menu" /></el-icon>
                <span>{{ menu.title }}</span>
              </template>
              <el-menu-item
                v-for="child in menu.children"
                :key="child.path"
                :index="child.path"
              >
                <el-icon><component :is="child.icon || Document" /></el-icon>
                <span>{{ child.title }}</span>
              </el-menu-item>
            </el-sub-menu>
            
            <!-- 没有子路由的菜单 -->
            <el-menu-item
              v-else
              :index="menu.path"
            >
              <el-icon><component :is="menu.icon || Document" /></el-icon>
              <span>{{ menu.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </aside>
    
    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 头部导航 -->
      <header class="header">
        <div class="header-left">
          <el-button
            type="text"
            :icon="Menu"
            @click="toggleSidebar"
            size="large"
            class="header-menu-btn"
          ></el-button>
        </div>
        
        <div class="header-right">
          <!-- 面包屑导航 -->
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-for="(item, index) in breadcrumbItems" :key="index">{{ item }}</el-breadcrumb-item>
          </el-breadcrumb>
          
          <!-- 用户信息下拉菜单 -->
          <el-dropdown @command="handleUserCommand" trigger="click">
            <el-button type="text" class="user-button">
              <el-avatar :size="36" :src="userAvatar"></el-avatar>
              <span class="user-name">{{ userName }}</span>
              <el-icon class="user-icon"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <!-- 内容区域 -->
      <div class="content-wrapper">
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { Menu, Document, User, Lock, SwitchButton, Expand, Fold, ArrowDown, HomeFilled, Plus, Setting, Key } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 侧边栏折叠状态
const sidebarCollapsed = ref(false);

// 计算当前激活的菜单
const activeMenu = computed(() => {
  return route.path;
});

// 获取用户名称
const userName = computed(() => {
  return userStore.userInfo?.username || '未知用户';
});

// 用户头像
const userAvatar = computed(() => {
  return userStore.userInfo?.avatar || '';
});

// 静态菜单项定义
const sidebarMenus = [
  {
    path: '/dashboard',
    title: '仪表盘',
    icon: HomeFilled,
    children: []
  },
  {
    path: '/user/list',
    title: '用户管理',
    icon: User,
    children: []
  },
  {
    path: '/role',
    title: '角色管理',
    icon: Setting,
    children: [
      { path: '/role/list', title: '角色列表', icon: Setting },
      { path: '/role/create', title: '新增角色', icon: Plus },
      { path: '/role/assign-permission', title: '分配权限', icon: Key }
    ]
  },
  {
    path: '/permission',
    title: '权限管理',
    icon: Key,
    children: [
      { path: '/permission/list', title: '权限列表', icon: Key },
      { path: '/permission/create', title: '新增权限', icon: Plus }
    ]
  }
];

// 面包屑导航项
const breadcrumbItems = ref<string[]>([]);

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value;
};



// 处理用户下拉菜单命令
const handleUserCommand = (command: string) => {
  switch (command) {
    case 'profile':
      console.log('个人中心');
      break;
    case 'password':
      console.log('修改密码');
      break;
    case 'logout':
      userStore.logout();
      router.push('/login');
      break;
    default:
      break;
  }
};

// 更新面包屑导航
const updateBreadcrumb = () => {
  const path = route.path;
  const pathArr = path.split('/').filter(Boolean);
  
  // 根据路径获取对应的路由标题
  const getRouteTitle = (path: string) => {
    const matchedRoute = router.getRoutes().find(route => route.path === path);
    return matchedRoute?.meta?.title as string || path;
  };
  
  breadcrumbItems.value = pathArr.map((_, index) => {
    const subPath = '/' + pathArr.slice(0, index + 1).join('/');
    return getRouteTitle(subPath);
  });
};

// 监听路由变化，更新面包屑
watch(
  () => route.path,
  () => {
    updateBreadcrumb();
  },
  { immediate: true }
);

// 页面加载时初始化
onMounted(() => {
  // 动态路由已在路由守卫中加载，无需在此重复加载
});
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* 侧边栏样式 */
.sidebar {
  width: 220px;
  background-color: #001529;
  color: #fff;
  transition: width 0.3s ease;
  overflow: hidden;
}

.sidebar.sidebar-collapsed {
  width: 64px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 20px;
  border-bottom: 1px solid #1890ff;
}

.logo {
  font-size: 20px;
  font-weight: 700;
  color: #1890ff;
}

.sidebar-scrollbar {
  height: calc(100vh - 60px);
}

.sidebar-menu {
  border-right: none;
}

/* 主内容区域样式 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部导航样式 */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 20px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-menu-btn {
  color: #606266;
  margin-right: 16px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.breadcrumb {
  font-size: 14px;
}

.user-button {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
}

.user-icon {
  font-size: 12px;
}

/* 内容区域样式 */
.content-wrapper {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f5f7fa;
}

/* 适配小屏幕 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    z-index: 1000;
  }
  
  .sidebar.sidebar-collapsed {
    left: -220px;
  }
  
  .header-right {
    gap: 12px;
  }
  
  .user-name {
    display: none;
  }
  
  .breadcrumb {
    display: none;
  }
}
</style>