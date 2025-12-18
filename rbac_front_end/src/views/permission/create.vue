<template>
  <div class="permission-create-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>新增权限</h2>
    </div>

    <!-- 新增权限表单 -->
    <el-card shadow="hover">
      <el-form
        ref="permissionFormRef"
        :model="permissionForm"
        :rules="rules"
        label-width="120px"
        style="max-width: 600px; margin: 0 auto"
      >
        <!-- 权限名称 -->
        <el-form-item label="权限名称" prop="permission_name">
          <el-input
            v-model="permissionForm.permission_name"
            placeholder="请输入权限名称"
            clearable
            :disabled="loading"
          />
        </el-form-item>

        <!-- 权限代码 -->
        <el-form-item label="权限代码" prop="code">
          <el-input
            v-model="permissionForm.code"
            placeholder="请输入权限代码（如：system:user:list）"
            clearable
            :disabled="loading"
          />
        </el-form-item>

        <!-- 父权限 -->
        <el-form-item label="父权限" prop="parent_id">
          <el-select
            v-model="permissionForm.parent_id"
            placeholder="请选择父权限（根权限请留空）"
            clearable
            :disabled="loading"
          >
            <el-option label="根权限" value="0" />
            <el-option
              v-for="perm in permissionOptions"
              :key="perm.id"
              :label="`${perm.name} (${perm.code})`"
              :value="perm.id"
            />
          </el-select>
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
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { getPermissionList, createPermission } from '@/api/permission';
import type { Permission } from '@/types/api';

// 路由实例
const router = useRouter();
// 用户状态
const userStore = useUserStore();
// 加载状态
const loading = ref(false);
// 表单引用
const permissionFormRef = ref<FormInstance | null>(null);
// 权限选项列表
const permissionOptions = ref<Permission[]>([]);

// 表单数据
const permissionForm = reactive({
  permission_name: '',
  code: '',
  parent_id: '0' // 默认根权限
});

// 表单验证规则
const rules = reactive<FormRules>({
  permission_name: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { min: 2, max: 50, message: '权限名称长度应在 2-50 个字符之间', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入权限代码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9:]+$/, message: '权限代码只能包含字母、数字和冒号', trigger: 'blur' },
    { min: 3, max: 100, message: '权限代码长度应在 3-100 个字符之间', trigger: 'blur' }
  ]
});

/**
 * 获取权限列表用于父权限选择
 */
const fetchPermissionOptions = async () => {
  loading.value = true;
  try {
    const response = await getPermissionList(userStore.token);
    if (response.success) {
      permissionOptions.value = response.data || [];
    } else {
      ElMessage.error(response.msg || '获取权限列表失败');
    }
  } catch (error: unknown) {
    console.error('获取权限列表失败:', error);
    ElMessage.error('网络请求失败，请重试');
  } finally {
    loading.value = false;
  }
};

/**
 * 表单提交
 */
const handleSubmit = async () => {
  if (!permissionFormRef.value) return;
  
  try {
    await permissionFormRef.value.validate();
    loading.value = true;
    
    const response = await createPermission(
      userStore.token,
      {
        permission_name: permissionForm.permission_name,
        code: permissionForm.code,
        parent_id: parseInt(permissionForm.parent_id)
      }
    );
    
    if (response.success) {
      ElMessage.success('权限创建成功');
      router.push('/permission/list');
    } else {
      ElMessage.error(response.msg || '权限创建失败');
    }
  } catch (error: unknown) {
    if (error instanceof Error && error.message) {
      // 表单验证失败信息
      ElMessage.error(error.message);
    } else {
      console.error('权限创建失败:', error);
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
  router.push('/permission/list');
};

// 组件挂载时获取权限列表
onMounted(() => {
  fetchPermissionOptions();
});
</script>

<style scoped>
.permission-create-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}
</style>