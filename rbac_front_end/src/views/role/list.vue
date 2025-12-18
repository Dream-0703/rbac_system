<template>
  <div class="role-list-container">
    <!-- 页面标题和新增按钮 -->
    <div class="page-header">
      <h2>角色列表</h2>
      <el-button type="primary" @click="$router.push('/role/create')">新增角色</el-button>
    </div>

    <!-- 角色数据表格 -->
    <el-card shadow="hover">
      <el-table
        v-loading="loading"
        :data="roleList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="角色ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="description" label="角色描述" />
        <el-table-column prop="createdAt" label="创建时间" width="200" />
        <el-table-column prop="updatedAt" label="更新时间" width="200" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="handleAssignPermission(scope.row.id)"
            >
              分配权限
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination" v-if="roleList.length > 0">
        <el-pagination
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { getRoleList } from '@/api/role';
import type { Role } from '@/types/api';

// 路由实例
const router = useRouter();
// 用户状态
const userStore = useUserStore();
// 加载状态
const loading = ref(false);
// 角色列表数据
const roleList = ref<Role[]>([]);
// 分页参数
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

/**
 * 获取角色列表数据
 */
const fetchRoleList = async () => {
  loading.value = true;
  try {
    const response = await getRoleList(userStore.token);
    if (response.success) {
      // 假设后端返回的数据结构为{data: Role[]}
      roleList.value = response.data || [];
      total.value = roleList.value.length;
    } else {
      ElMessage.error(response.msg || '获取角色列表失败');
    }
  } catch (error) {
    console.error('获取角色列表失败:', error);
    ElMessage.error('网络请求失败，请重试');
  } finally {
    loading.value = false;
  }
};

/**
 * 分配权限
 * @param roleId 角色ID
 */
const handleAssignPermission = (roleId: number) => {
  router.push({ path: '/role/assign-permission', query: { roleId } });
};

/**
 * 分页大小变化
 * @param size 每页条数
 */
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchRoleList();
};

/**
 * 当前页码变化
 * @param page 当前页码
 */
const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchRoleList();
};

// 组件挂载时获取角色列表
onMounted(() => {
  fetchRoleList();
});
</script>

<style scoped>
.role-list-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>