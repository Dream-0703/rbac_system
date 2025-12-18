<template>
  <div class="role-create-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>新增角色</h2>
    </div>

    <!-- 新增角色表单 -->
    <el-card shadow="hover">
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="rules"
        label-width="100px"
        style="max-width: 600px; margin: 0 auto"
      >
        <!-- 角色名称 -->
        <el-form-item label="角色名称" prop="roleName">
          <el-input
            v-model="roleForm.roleName"
            placeholder="请输入角色名称"
            clearable
            :disabled="loading"
          />
        </el-form-item>

        <!-- 角色描述 -->
        <el-form-item label="角色描述" prop="description">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            rows="3"
            placeholder="请输入角色描述（可选）"
            clearable
            :disabled="loading"
          />
        </el-form-item>

        <!-- 表单操作按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            @click="handleSubmit"
            :loading="loading"
            style="margin-right: 10px"
          >
            提交
          </el-button>
          <el-button
            @click="handleCancel"
            :disabled="loading"
          >
            取消
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { createRole } from '@/api/role';

// 路由实例
const router = useRouter();
// 用户状态
const userStore = useUserStore();
// 加载状态
const loading = ref(false);
// 表单引用
const roleFormRef = ref<FormInstance | null>(null);

// 表单数据
const roleForm = reactive({
  roleName: '',
  description: ''
});

// 表单验证规则
const rules = reactive<FormRules>({
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度应在 2-20 个字符之间', trigger: 'blur' }
  ],
  description: [
    { max: 100, message: '角色描述长度不能超过 100 个字符', trigger: 'blur' }
  ]
});

/**
 * 表单提交
 */
const handleSubmit = async () => {
  if (!roleFormRef.value) return;
  
  try {
    await roleFormRef.value.validate();
    loading.value = true;
    
    const response = await createRole(
      userStore.token,
      roleForm.roleName,
      roleForm.description
    );
    
    if (response.success) {
      ElMessage.success('角色创建成功');
      router.push('/role/list');
    } else {
      ElMessage.error(response.msg || '角色创建失败');
    }
  } catch (error: unknown) {
    if (error instanceof Error && error.message) {
      // 表单验证失败信息
      ElMessage.error(error.message);
    } else {
      console.error('角色创建失败:', error);
      ElMessage.error('网络请求失败，请重试');
    }
  } finally {
    loading.value = false;
  }
};

/**
 * 取消操作
 */
const handleCancel = () => {
  router.push('/role/list');
};
</script>

<style scoped>
.role-create-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}
</style>