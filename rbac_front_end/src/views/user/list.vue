<template>
  <div class="user-list-container">
    <div class="user-list-header">
      <h2>用户列表</h2>
      <el-button type="primary" @click="handleAddUser">新增用户</el-button>
    </div>
    
    <el-card class="user-list-card">
      <el-table
        v-loading="loading"
        :data="userList"
        style="width: 100%"
        border
      >
        <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
        <el-table-column prop="username" label="用户名" min-width="150"></el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="180"></el-table-column>
        <el-table-column label="操作" width="240" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleAssignRole(scope.row.id)">
              分配角色
            </el-button>
            <el-button size="small" type="warning" @click="handleResetPassword(scope.row.id)">
              重置密码
            </el-button>
            <el-button size="small" type="danger" @click="handleDeleteUser(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="user-list-pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 新增用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="新增用户"
      width="500px"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreateUser">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 分配角色对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="分配角色"
      width="600px"
    >
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="roleRules"
        label-width="120px"
        label-position="right"
      >
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="roleForm.userId" readonly></el-input>
        </el-form-item>
        
        <el-form-item label="当前角色">
          <el-input v-model="currentRoleName" readonly placeholder="未分配角色"></el-input>
        </el-form-item>
        
        <el-form-item label="选择角色" prop="roleId">
          <el-select
            v-model="roleForm.roleId"
            placeholder="请选择角色"
            style="width: 300px"
            filterable
          >
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveRole" :loading="roleLoading">
            保存角色
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetDialogVisible"
      title="重置密码"
      width="500px"
    >
      <div class="reset-password-info">
        <el-alert
          title="确认重置密码"
          type="warning"
          :description="`确定要将用户ID为 ${resetUserId} 的密码重置为默认密码 '123456' 吗？`"
          show-icon
        ></el-alert>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirmReset" :loading="resetLoading">
            确认重置
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useUserStore } from '@/stores/user';
import { getUserList, createUser, deleteUser, getUserRole, updateUserRole, resetUserPassword } from '@/api/user';
import { getRoleList } from '@/api/role';
import type { User, Role } from '@/types/api';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';

const userStore = useUserStore();

const userList = ref<User[]>([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 角色相关
const roleList = ref<Role[]>([]);
const currentRoleId = ref<number | null>(null);

// 分配角色对话框
const roleDialogVisible = ref(false);
const roleFormRef = ref<FormInstance | null>(null);
const roleForm = ref({
  userId: '',
  roleId: 0
});
const roleLoading = ref(false);

const currentRoleName = computed(() => {
  if (!currentRoleId.value) return '';
  const role = roleList.value.find(r => r.id === currentRoleId.value);
  return role ? role.name : '';
});

const roleRules = ref<FormRules>({
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
});

// 新增用户对话框
const dialogVisible = ref(false);
const userFormRef = ref<FormInstance | null>(null);
const userForm = ref({
  username: '',
  password: ''
});

// 重置密码对话框
const resetDialogVisible = ref(false);
const resetUserId = ref<number | null>(null);
const resetLoading = ref(false);

const userRules = ref<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度在 6 到 32 个字符', trigger: 'blur' }
  ]
});

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true;
  try {
    const response = await getUserList(userStore.token);
    if (response.success) {
      userList.value = response.data;
      total.value = response.data.length;
    } else {
      ElMessage.error('获取用户列表失败: ' + response.msg);
    }
  } catch (error: unknown) {
    console.error('获取用户列表错误:', error);
    if (error instanceof Error && error.message) {
      ElMessage.error('获取用户列表失败: ' + error.message);
    } else {
      ElMessage.error('获取用户列表失败');
    }
  } finally {
    loading.value = false;
  }
};

// 页面加载时获取用户列表
onMounted(() => {
  fetchUserList();
});

// 分页处理
const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize;
  fetchUserList();
};

const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage;
  fetchUserList();
};

// 新增用户
const handleAddUser = () => {
  dialogVisible.value = true;
  userForm.value = {
    username: '',
    password: ''
  };
};

const handleCreateUser = async () => {
  if (!userFormRef.value) return;
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const response = await createUser(
          userStore.token,
          userForm.value.username,
          userForm.value.password
        );
        
        if (response.success) {
          ElMessage.success('新增用户成功');
          dialogVisible.value = false;
          fetchUserList();
        } else {
          ElMessage.error('新增用户失败: ' + response.msg);
        }
      } catch (error: unknown) {
        console.error('新增用户错误:', error);
        if (error instanceof Error && error.message) {
          ElMessage.error('新增用户失败: ' + error.message);
        } else {
          ElMessage.error('新增用户失败');
        }
      } finally {
        loading.value = false;
      }
    }
  });
};

// 分配角色
const handleAssignRole = async (userId: number) => {
  roleForm.value.userId = userId.toString();
  resetUserId.value = userId;
  roleDialogVisible.value = true;
  
  // 获取角色列表和用户当前角色
  try {
    // 并行获取角色列表和用户当前角色
    const [roleListResponse, userRoleResponse] = await Promise.all([
      getRoleList(userStore.token),
      getUserRole(userStore.token, userId)
    ]);
    
    if (roleListResponse.success) {
      roleList.value = roleListResponse.data;
    } else {
      ElMessage.error('获取角色列表失败: ' + roleListResponse.msg);
    }
    
    if (userRoleResponse.success && userRoleResponse.data) {
      // 根据角色名称查找对应的角色ID
      const role = roleListResponse.data.find(r => r.name === userRoleResponse.data);
      if (role) {
        currentRoleId.value = role.id;
        roleForm.value.roleId = role.id;
      } else {
        ElMessage.warning('用户当前角色不存在于系统中');
        currentRoleId.value = null;
        roleForm.value.roleId = 0;
      }
    } else {
      ElMessage.warning('用户未分配角色');
      currentRoleId.value = null;
      roleForm.value.roleId = 0;
    }
  } catch (error: unknown) {
    console.error('获取角色信息错误:', error);
    ElMessage.error('获取角色信息失败');
  }
};

const handleSaveRole = async () => {
  if (!roleFormRef.value) return;
  
  await roleFormRef.value.validate(async (valid) => {
    if (valid) {
      roleLoading.value = true;
      try {
        const response = await updateUserRole(
          userStore.token,
          parseInt(roleForm.value.userId),
          roleForm.value.roleId
        );
        
        if (response.success) {
          ElMessage.success('角色分配成功');
          currentRoleId.value = roleForm.value.roleId;
          roleDialogVisible.value = false;
        } else {
          ElMessage.error('角色分配失败: ' + response.msg);
        }
      } catch (error: unknown) {
        console.error('分配角色错误:', error);
        if (error instanceof Error && error.message) {
          ElMessage.error('角色分配失败: ' + error.message);
        } else {
          ElMessage.error('角色分配失败');
        }
      } finally {
        roleLoading.value = false;
      }
    }
  });
};

// 重置密码
const handleResetPassword = (userId: number) => {
  resetUserId.value = userId;
  resetDialogVisible.value = true;
};

const handleConfirmReset = async () => {
  if (!resetUserId.value) return;
  
  resetLoading.value = true;
  try {
    const response = await resetUserPassword(userStore.token, resetUserId.value);
    
    if (response.success) {
      ElMessage.success('密码重置成功，新密码为: 123456');
      resetDialogVisible.value = false;
    } else {
      ElMessage.error('密码重置失败: ' + response.msg);
    }
  } catch (error: unknown) {
    console.error('重置密码错误:', error);
    if (error instanceof Error && error.message) {
      ElMessage.error('密码重置失败: ' + error.message);
    } else {
      ElMessage.error('密码重置失败');
    }
  } finally {
    resetLoading.value = false;
  }
};

// 删除用户
const handleDeleteUser = async (userId: number) => {
  try {
    // 弹出确认对话框
    await ElMessageBox.confirm(
      '确定要删除该用户吗？删除后不可恢复！',
      '删除用户',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    loading.value = true;
    const response = await deleteUser(userStore.token, userId);
    
    if (response.success) {
      ElMessage.success('删除用户成功');
      fetchUserList(); // 重新加载用户列表
    } else {
      ElMessage.error('删除用户失败: ' + response.msg);
    }
  } catch (error: unknown) {
    // 如果是用户取消了确认对话框，不显示错误消息
    if (error instanceof Error && error.name !== 'ElMessageBoxCancel') {
      console.error('删除用户错误:', error);
      ElMessage.error('删除用户失败');
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.user-list-container {
  padding: 20px;
}

.user-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.user-list-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.user-list-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
