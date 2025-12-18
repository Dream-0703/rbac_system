<template>
  <div class="role-assign-permission-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>分配权限</h2>
      <div class="role-info" v-if="roleId">
        角色ID: {{ roleId }}
      </div>
    </div>

    <!-- 权限分配区域 -->
    <el-card shadow="hover">
      <div class="permission-tree-container">
        <el-tree
          v-loading="loading"
          ref="permissionTreeRef"
          :data="permissionTree"
          show-checkbox
          node-key="id"
          :default-expanded-keys="expandedKeys"
          :default-checked-keys="checkedKeys"
          :props="treeProps"
          :check-on-click-node="true"
          @check-change="handleCheckChange"
        />
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button
          type="primary"
          @click="handleSubmit"
          :loading="loading"
          style="margin-right: 10px"
        >
          保存
        </el-button>
        <el-button
          @click="handleCancel"
          :disabled="loading"
        >
          取消
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElTree } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { getPermissionList } from '@/api/permission';
import { assignPermissionsToRole } from '@/api/role';
import type { Permission } from '@/types/api';

// 路由实例
const router = useRouter();
// 路由参数
const route = useRoute();
// 用户状态
const userStore = useUserStore();
// 加载状态
const loading = ref(false);
// 权限树引用
const permissionTreeRef = ref<InstanceType<typeof ElTree> | null>(null);
// 角色ID
const roleId = ref<number | null>(null);
// 权限树数据
const permissionTree = ref<Permission[]>([]);
// 展开的节点
const expandedKeys = ref<number[]>([]);
// 选中的节点
const checkedKeys = ref<number[]>([]);
// 选择的权限ID列表
const selectedPermissionIds = ref<number[]>([]);

// 树组件配置
const treeProps = {
  label: 'name',
  children: 'children'
};

/**
 * 从URL参数获取角色ID
 */
const getRoleIdFromParams = () => {
  const id = route.query.roleId;
  if (id && typeof id === 'string') {
    const roleIdNum = parseInt(id, 10);
    if (!isNaN(roleIdNum)) {
      roleId.value = roleIdNum;
      return true;
    }
  }
  ElMessage.error('角色ID参数错误');
  router.push('/role/list');
  return false;
};

/**
 * 构建权限树结构
 * @param permissions 权限列表
 * @returns 树形结构权限数据
 */
const buildPermissionTree = (permissions: Permission[]): (Permission & { children?: Permission[] })[] => {
  const tree: (Permission & { children?: Permission[] })[] = [];
  const map = new Map<number, Permission & { children?: Permission[] }>();

  // 首先将所有权限添加到map中
  permissions.forEach(perm => {
    map.set(perm.id, {
      ...perm,
      children: []
    });
  });

  // 然后构建树形结构
  permissions.forEach(perm => {
    const node = map.get(perm.id);
    if (node) {
      if (perm.parentId === 0 || perm.parentId === null) {
        // 根节点
        tree.push(node);
        expandedKeys.value.push(perm.id); // 默认展开根节点
      } else {
        // 子节点
        const parent = map.get(perm.parentId);
        if (parent) {
          if (!parent.children) {
            parent.children = [];
          }
          parent.children.push(node);
        }
      }
    }
  });

  return tree;
};

/**
 * 获取权限列表
 */
const fetchPermissions = async () => {
  loading.value = true;
  try {
    const response = await getPermissionList(userStore.token);
    if (response.success) {
      const permissions = response.data || [];
      permissionTree.value = buildPermissionTree(permissions);
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
 * 处理节点选中状态变化
 * @param data 节点数据
 * @param checked 是否选中
 * @param indeterminate 是否半选
 */
const handleCheckChange = () => {
  // 获取所有选中的节点
  if (permissionTreeRef.value) {
    const checkedNodes = permissionTreeRef.value.getCheckedNodes();
    selectedPermissionIds.value = checkedNodes.map(node => node.id);
  }
};

/**
 * 提交分配的权限
 */
const handleSubmit = async () => {
  if (!roleId.value) return;
  
  loading.value = true;
  try {
    const response = await assignPermissionsToRole(
      userStore.token,
      roleId.value,
      selectedPermissionIds.value
    );
    
    if (response.success) {
      ElMessage.success('权限分配成功');
      router.push('/role/list');
    } else {
      ElMessage.error(response.msg || '权限分配失败');
    }
  } catch (error) {
    console.error('权限分配失败:', error);
    ElMessage.error('网络请求失败，请重试');
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

// 组件挂载时初始化
onMounted(() => {
  if (getRoleIdFromParams()) {
    fetchPermissions();
  }
});
</script>

<style scoped>
.role-assign-permission-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.role-info {
  font-size: 16px;
  color: #606266;
}

.permission-tree-container {
  max-height: 500px;
  overflow-y: auto;
  margin-bottom: 20px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>