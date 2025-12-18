<template>
  <div class="login-container">
    <!-- 背景图片区域 -->
    <div class="login-background"></div>
    
    <!-- 登录表单区域 -->
    <div class="login-form-wrapper">
      <div class="login-form">
        <div class="login-title">RBAC 权限管理系统</div>
        <div class="login-subtitle">统一身份认证平台</div>
        
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-position="top"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              clearable
              size="large"
            ></el-input>
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              size="large"
            ></el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              @click="handleLogin"
              size="large"
              class="login-button"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-footer">
          <span>© 2025 RBAC权限管理系统</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { User, Lock } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();
const loginFormRef = ref<FormInstance | null>(null);
const loading = ref(false);

const loginForm = reactive({
  username: '',
  password: ''
});

const loginRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度在 6 到 32 个字符', trigger: 'blur' }
  ]
});

import { loadAsyncRoutes } from '@/router';

const handleLogin = async () => {
  if (!loginFormRef.value) return;
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const success = await userStore.login(loginForm.username, loginForm.password);
        if (success) {
          // 加载动态路由
          loadAsyncRoutes();
          await router.push('/dashboard');
        } else {
          ElMessage.error('登录失败，请检查用户名和密码');
        }
      } catch (error) {
        ElMessage.error('登录失败，请稍后重试');
        console.error('登录错误:', error);
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.login-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('@/assets/images/back.png'); /* 后续替换为用户提供的图片路径 */
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  filter: blur(8px);
  z-index: 1;
}

.login-form-wrapper {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.login-form {
  width: 420px;
  padding: 40px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.login-title {
  font-size: 24px;
  font-weight: 700;
  color: #1890ff;
  text-align: center;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #606266;
  text-align: center;
  margin-bottom: 32px;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
}

.login-footer {
  margin-top: 24px;
  text-align: center;
  font-size: 12px;
  color: #909399;
}

/* 适配小屏幕 */
@media (max-width: 768px) {
  .login-form {
    width: 90%;
    padding: 30px 20px;
  }
  
  .login-title {
    font-size: 20px;
  }
}
</style>
