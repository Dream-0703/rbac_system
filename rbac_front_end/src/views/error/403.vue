<template>
  <div class="error-container">
    <div class="error-content">
      <div class="error-code">403</div>
      <div class="error-title">禁止访问</div>
      <div class="error-message">抱歉，您没有权限访问此页面</div>
      <div class="error-actions">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goLogin">重新登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

// 返回首页
const goHome = () => {
  router.push('/');
};

// 重新登录
const goLogin = () => {
  router.push('/login');
};
</script>

<style scoped>
.error-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.error-content {
  text-align: center;
  padding: 40px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  color: #f56c6c;
  margin-bottom: 16px;
}

.error-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.error-message {
  font-size: 14px;
  color: #606266;
  margin-bottom: 32px;
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

@media (max-width: 768px) {
  .error-code {
    font-size: 80px;
  }
  
  .error-title {
    font-size: 20px;
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
  }
}
</style>
