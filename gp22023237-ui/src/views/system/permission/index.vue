<template>
  <div class="app-container permission-container">
    <el-card class="main-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <el-icon class="title-icon"><Lock /></el-icon>
            <span>权限管理</span>
          </div>
          <div class="header-tip">
            <el-tag type="info" size="small">
              <el-icon style="margin-right: 4px"><InfoFilled /></el-icon>
              选择角色后配置对应的菜单权限
            </el-tag>
          </div>
        </div>
      </template>

      <el-row :gutter="20">
        <!-- 左侧角色列表 -->
        <el-col :span="6">
          <el-card class="role-card" shadow="hover">
            <template #header>
              <div class="role-card-header">
                <el-icon><User /></el-icon>
                <span>角色列表</span>
              </div>
            </template>

            <div class="search-wrapper">
              <el-input
                v-model="roleSearch"
                placeholder="搜索角色"
                clearable
                size="default"
                @keyup.enter="handleRoleSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>

            <div class="role-list-wrapper">
              <el-menu
                :default-active="currentRoleId?.toString()"
                class="role-menu"
                @select="handleRoleSelect"
              >
                <el-menu-item
                  v-for="role in filteredRoles"
                  :key="role.id"
                  :index="role.id.toString()"
                  class="role-menu-item"
                >
                  <template #title>
                    <div class="role-item-content">
                      <el-avatar :size="28" :src="getRoleAvatar(role.name)">
                        {{ getRoleInitial(role.name) }}
                      </el-avatar>
                      <span class="role-name">{{ role.name }}</span>
                    </div>
                  </template>
                </el-menu-item>
              </el-menu>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧菜单树 -->
        <el-col :span="18">
          <el-card class="menu-card" shadow="hover">
            <template #header>
              <div class="menu-card-header">
                <div class="menu-title">
                  <el-icon><Menu /></el-icon>
                  <span>菜单权限</span>
                  <el-tag v-if="currentRole" type="success" size="small" style="margin-left: 10px">
                    {{ currentRole.name }}
                  </el-tag>
                </div>
                <div class="menu-actions">
                  <el-button type="primary" plain size="small" @click="expandAll" :disabled="!currentRoleId">
                    <el-icon><Expand /></el-icon>
                    展开全部
                  </el-button>
                  <el-button type="info" plain size="small" @click="collapseAll" :disabled="!currentRoleId">
                    <el-icon><Fold /></el-icon>
                    折叠全部
                  </el-button>
                </div>
              </div>
            </template>

            <div v-if="!currentRoleId" class="empty-state">
              <el-empty description="请先选择一个角色" image-size="120">
                <template #image>
                  <el-icon class="empty-icon"><Select /></el-icon>
                </template>
              </el-empty>
            </div>

            <div v-else class="menu-tree-wrapper">
              <el-tree
                ref="menuTreeRef"
                v-loading="menuLoading"
                :data="menuTree"
                :props="treeProps"
                show-checkbox
                node-key="menusIndex"
                :default-checked-keys="checkedKeys"
                :default-expand-all="false"
                :indent="30"
                @check="handleMenuCheck"
                class="menu-tree"
              >
                <template #default="{ node, data }">
                  <span class="tree-node-content">
                    <el-icon v-if="data.icon" class="menu-icon">
                      <component :is="data.icon" />
                    </el-icon>
                    <el-icon v-else class="menu-icon default-icon">
                      <Document />
                    </el-icon>
                    <span class="node-label">{{ node.label }}</span>
                  </span>
                </template>
              </el-tree>
            </div>

            <template #footer v-if="currentRoleId">
              <div class="card-footer">
                <div class="footer-info">
                  <el-text type="info" size="small">
                    <el-icon style="margin-right: 4px"><CircleCheck /></el-icon>
                    已选择 {{ checkedKeys.length }} 个菜单权限
                  </el-text>
                </div>
                <div class="footer-actions">
                  <el-button @click="handleReset">
                    <el-icon><RefreshLeft /></el-icon>
                    重置
                  </el-button>
                  <el-button type="primary" @click="handleSave" :loading="saveLoading">
                    <el-icon><Check /></el-icon>
                    保存权限
                  </el-button>
                </div>
              </div>
            </template>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup name="Permission">
import { getRoleMenu, saveRoleMenu, listRoles } from '@/api/system/permission'
import {
  Lock,
  InfoFilled,
  User,
  Search,
  Menu,
  Expand,
  Fold,
  Select,
  Document,
  CircleCheck,
  RefreshLeft,
  Check
} from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

// 数据
const roleList = ref([])
const roleSearch = ref('')
const currentRoleId = ref(null)
const menuTree = ref([])
const checkedKeys = ref([])
const originalCheckedKeys = ref([])

// 状态
const menuLoading = ref(false)
const saveLoading = ref(false)

// 引用
const menuTreeRef = ref(null)

// 树形配置
const treeProps = {
  children: 'children',
  label: 'title'
}

// 计算属性
const filteredRoles = computed(() => {
  if (!roleSearch.value) {
    return roleList.value
  }
  return roleList.value.filter(role =>
    role.name.toLowerCase().includes(roleSearch.value.toLowerCase())
  )
})

const currentRole = computed(() => {
  return roleList.value.find(r => r.id === currentRoleId.value)
})

// 获取角色头像（占位图）
function getRoleAvatar(name) {
  return ''
}

// 获取角色名称首字
function getRoleInitial(name) {
  return name ? name.charAt(0) : '角'
}

// 查询角色列表
function getRoleList() {
  listRoles().then(res => {
    roleList.value = res.data || []
  }).catch(() => {
    proxy.$modal.msgError('获取角色列表失败')
  })
}

// 角色搜索
function handleRoleSearch() {
  // 搜索已通过计算属性实现
}

// 选择角色
function handleRoleSelect(roleId) {
  currentRoleId.value = parseInt(roleId)
  loadRoleMenu()
}

// 加载角色菜单
function loadRoleMenu() {
  if (!currentRoleId.value) return

  menuLoading.value = true
  getRoleMenu(currentRoleId.value).then(res => {
    menuTree.value = res.data?.menus || []
    checkedKeys.value = res.data?.checkedKeys || []
    originalCheckedKeys.value = [...checkedKeys.value]

    // 等待 DOM 更新后设置选中状态
    nextTick(() => {
      if (menuTreeRef.value) {
        menuTreeRef.value.setCheckedKeys(checkedKeys.value)
      }
    })
  }).catch(() => {
    proxy.$modal.msgError('获取菜单权限失败')
  }).finally(() => {
    menuLoading.value = false
  })
}

// 菜单选中变化
function handleMenuCheck() {
  if (menuTreeRef.value) {
    const checked = menuTreeRef.value.getCheckedKeys(true)
    const halfChecked = menuTreeRef.value.getHalfCheckedKeys()
    checkedKeys.value = [...checked, ...halfChecked]
  }
}

// 展开全部
function expandAll() {
  if (menuTreeRef.value) {
    const nodes = menuTreeRef.value.store.nodesMap
    Object.keys(nodes).forEach(key => {
      menuTreeRef.value.store.nodesMap[key].expanded = true
    })
  }
}

// 折叠全部
function collapseAll() {
  if (menuTreeRef.value) {
    const nodes = menuTreeRef.value.store.nodesMap
    Object.keys(nodes).forEach(key => {
      menuTreeRef.value.store.nodesMap[key].expanded = false
    })
  }
}

// 保存权限
function handleSave() {
  if (!currentRoleId.value) {
    proxy.$modal.msgWarning('请先选择一个角色')
    return
  }

  if (!menuTreeRef.value) {
    return
  }

  // 获取所有选中的菜单（包括一级和二级）
  const allCheckedKeys = getAllCheckedKeys()

  proxy.$modal.confirm(`确认保存角色「${currentRole.value?.name}」的菜单权限吗？`).then(() => {
    saveLoading.value = true
    saveRoleMenu(currentRoleId.value, allCheckedKeys).then(() => {
      proxy.$modal.msgSuccess('保存成功')
      originalCheckedKeys.value = [...checkedKeys.value]
      loadRoleMenu()
    }).catch(() => {
      proxy.$modal.msgError('保存失败')
    }).finally(() => {
      saveLoading.value = false
    })
  }).catch(() => {})
}

// 获取所有选中的菜单的 menusIndex（包括一级和二级菜单）
function getAllCheckedKeys() {
  const result = []

  function traverse(nodes) {
    for (const node of nodes) {
      if (checkedKeys.value.includes(node.menusIndex)) {
        result.push(node.menusIndex)
      }
      if (node.children && node.children.length > 0) {
        traverse(node.children)
      }
    }
  }

  traverse(menuTree.value)
  return result
}

// 重置
function handleReset() {
  checkedKeys.value = [...originalCheckedKeys.value]
  nextTick(() => {
    if (menuTreeRef.value) {
      menuTreeRef.value.setCheckedKeys(checkedKeys.value)
    }
  })
}

// 初始化
getRoleList()
</script>

<style scoped>
.permission-container {
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
  padding: 20px;
}

.main-card {
  border: none;
  border-radius: 8px;
  min-width: 900px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.header-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.title-icon {
  margin-right: 8px;
  color: #409eff;
  font-size: 20px;
}

.role-card,
.menu-card {
  border-radius: 8px;
  height: calc(100vh - 220px);
  min-height: 500px;
  display: flex;
  flex-direction: column;
}

.role-card :deep(.el-card__body),
.menu-card :deep(.el-card__body) {
  flex: 1;
  overflow: hidden;
  padding: 16px;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.role-card :deep(.el-card__footer),
.menu-card :deep(.el-card__footer) {
  padding: 16px;
  border-top: 1px solid #ebeef5;
  background-color: #fafafa;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
}

.role-card-header,
.menu-card-header {
  display: flex;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.role-card-header .el-icon,
.menu-card-header .el-icon {
  margin-right: 6px;
  color: #409eff;
}

.menu-card-header {
  justify-content: space-between;
}

.menu-title {
  display: flex;
  align-items: center;
}

.menu-actions {
  display: flex;
  gap: 8px;
}

.search-wrapper {
  margin-bottom: 16px;
}

.role-list-wrapper {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  margin: 0 -16px;
  padding: 0 16px;
  min-height: 0;
}

.role-menu {
  border: none;
  background: transparent;
  height: 100%;
  overflow-y: auto;
}

.role-menu-item {
  border-radius: 6px;
  margin-bottom: 4px;
}

.role-menu-item:hover {
  background-color: #f5f7fa;
}

.role-menu-item:not(:last-child) {
  margin-bottom: 4px;
}

.role-item-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.role-name {
  font-weight: 500;
  color: #606266;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.empty-icon {
  font-size: 80px;
  color: #c0c4cc;
}

.menu-tree-wrapper {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 0;
  min-height: 0;
}

.menu-tree {
  padding: 8px;
}

.tree-node-content {
  display: flex;
  align-items: center;
  padding: 4px 0;
}

.menu-icon {
  margin-right: 8px;
  color: #409eff;
  font-size: 16px;
}

.menu-icon.default-icon {
  color: #909399;
}

.node-label {
  font-size: 14px;
  color: #303133;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-info {
  display: flex;
  align-items: center;
}

.footer-actions {
  display: flex;
  gap: 12px;
}

/* 滚动条美化 */
.role-list-wrapper::-webkit-scrollbar,
.menu-tree-wrapper::-webkit-scrollbar {
  width: 6px;
}

.role-list-wrapper::-webkit-scrollbar-thumb,
.menu-tree-wrapper::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.role-list-wrapper::-webkit-scrollbar-track,
.menu-tree-wrapper::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}

/* Element Plus 树组件样式优化 */
.menu-tree :deep(.el-tree-node__content) {
  height: 36px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.menu-tree :deep(.el-tree-node__content:hover) {
  background-color: #f5f7fa;
}

.menu-tree :deep(.el-checkbox__label) {
  padding-left: 6px;
}
</style>
