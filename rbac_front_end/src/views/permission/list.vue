<template>
  <div class="permission-list-container">
    <!-- 页面标题和新增按钮 -->
    <div class="page-header">
      <h2>权限列表</h2>
      <el-button type="primary" @click="$router.push('/permission/create')">新增权限</el-button>
    </div>

    <!-- 权限数据表格 -->
    <el-card shadow="hover">
      <el-table
        v-loading="loading"
        :data="permissionList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="权限ID" width="80" />
        <el-table-column prop="name" label="权限名称" width="150" />
        <el-table-column prop="code" label="权限代码" width="200" />
        <el-table-column prop="parentId" label="父权限ID" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="200" />
        <el-table-column prop="updatedAt" label="更新时间" width="200" />
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination" v-if="permissionList.length > 0">
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
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { getPermissionList } from '@/api/permission';
import type { Permission } from '@/types/api';


// 用户状态
const userStore = useUserStore();
// 加载状态
const loading = ref(false);
// 权限列表数据
const permissionList = ref<Permission[]>([]);
// 分页参数
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

/**
 * 获取权限列表数据
 */
const fetchPermissionList = async () => {
  loading.value = true;
  try {
    const response = await getPermissionList(userStore.token);
    if (response.success) {
      // 假设后端返回的数据结构为{data: Permission[]}
      permissionList.value = response.data || [];
      total.value = permissionList.value.length;
    } else {
      ElMessage.error(response.msg || '获取权限列表失败');
    }
  } catch (error) {
    console.error('获取权限列表失败:', error);
    ElMessage.error('网络请求失败，请重试');
  } finally {
    loading.value = false;
  }
};

/**
 * 分页大小变化
 * @param size 每页条数
 */
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchPermissionList();
};

/**
 * 当前页码变化
 * @param page 当前页码
 */
const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchPermissionList();
};

// 组件挂载时获取权限列表
onMounted(() => {
  fetchPermissionList();
});
</script>

<style scoped>
.permission-list-container {
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