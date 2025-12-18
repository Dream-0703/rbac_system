<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <h2 class="dashboard-title">仪表盘</h2>
      <div class="dashboard-subtitle">欢迎回来，{{ userName }}</div>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ userCount }}</div>
          <div class="stat-label">用户总数</div>
        </div>
        <div class="stat-icon user-icon">
          <el-icon><User /></el-icon>
        </div>
      </el-card>
      
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ roleCount }}</div>
          <div class="stat-label">角色总数</div>
        </div>
        <div class="stat-icon role-icon">
          <el-icon><UserFilled /></el-icon>
        </div>
      </el-card>
      
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ permissionCount }}</div>
          <div class="stat-label">权限总数</div>
        </div>
        <div class="stat-icon permission-icon">
          <el-icon><Key /></el-icon>
        </div>
      </el-card>
      
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ todayLoginCount }}</div>
          <div class="stat-label">今日登录</div>
        </div>
        <div class="stat-icon login-icon">
          <el-icon><Monitor /></el-icon>
        </div>
      </el-card>
    </div>
    
    <!-- 最近活动 -->
    <div class="activity-section">
      <el-card shadow="hover" class="activity-card">
        <template #header>
          <div class="card-header">
            <span>最近活动</span>
            <el-button type="text" size="small">查看全部</el-button>
          </div>
        </template>
        
        <div class="activity-list">
          <div v-for="(item, index) in recentActivities" :key="index" class="activity-item">
            <div class="activity-icon">
              <el-icon><Operation /></el-icon>
            </div>
            <div class="activity-content">
              <div class="activity-title">{{ item.title }}</div>
              <div class="activity-time">{{ item.time }}</div>
            </div>
            <el-tag :type="item.type" size="small">{{ item.status }}</el-tag>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useUserStore } from '@/stores/user';
import { User, UserFilled, Key, Monitor, Operation } from '@element-plus/icons-vue';

const userStore = useUserStore();

// 获取用户名称
const userName = computed(() => {
  return userStore.userInfo?.username || '未知用户';
});

// 统计数据
const userCount = ref(128);
const roleCount = ref(16);
const permissionCount = ref(256);
const todayLoginCount = ref(24);

// 最近活动列表
const recentActivities = ref([
  {
    title: 'admin 用户创建了新角色 "编辑者"',
    time: '2025-12-18 14:30',
    type: 'success',
    status: '成功'
  },
  {
    title: 'user123 修改了个人密码',
    time: '2025-12-18 13:15',
    type: 'info',
    status: '完成'
  },
  {
    title: 'system 用户更新了权限配置',
    time: '2025-12-18 11:45',
    type: 'warning',
    status: '更新'
  },
  {
    title: 'test 用户尝试登录失败',
    time: '2025-12-18 10:20',
    type: 'danger',
    status: '失败'
  }
]);
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 100px);
}

.dashboard-header {
  margin-bottom: 24px;
}

.dashboard-title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0;
}

.dashboard-subtitle {
  font-size: 14px;
  color: #606266;
  margin-top: 8px;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  position: relative;
  overflow: hidden;
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-content {
  padding: 16px;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.stat-icon {
  position: absolute;
  top: 16px;
  right: 16px;
  font-size: 48px;
  opacity: 0.1;
}

.user-icon {
  color: #1890ff;
}

.role-icon {
  color: #67c23a;
}

.permission-icon {
  color: #e6a23c;
}

.login-icon {
  color: #f56c6c;
}

/* 最近活动 */
.activity-section {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-list {
  max-height: 400px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  margin-right: 16px;
  color: #1890ff;
  font-size: 24px;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #909399;
}

/* 适配小屏幕 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .stat-number {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
