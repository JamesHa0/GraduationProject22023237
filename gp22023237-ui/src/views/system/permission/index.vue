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

        <!-- 右侧菜单列表 -->
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

            <div v-else class="menu-list-wrapper" v-loading="menuLoading">
              <div class="menu-list">
                <template v-for="menu in menuTree" :key="menu.menusIndex">
                  <div class="menu-item">
                    <div class="menu-item-content">
                      <span class="expand-icon" v-if="hasChildren(menu)" @click="toggleExpand(menu.menusIndex)">
                        <el-icon><ArrowRight v-if="!isExpanded(menu.menusIndex)" /><ArrowDown v-else /></el-icon>
                      </span>
                      <span class="expand-placeholder" v-else></span>
                      <span class="menu-icon-wrapper">
                        <svg-icon v-if="menu.icon" :icon-class="menu.icon" class="menu-icon" />
                        <el-icon v-else class="menu-icon default-icon"><Document /></el-icon>
                      </span>
                      <el-checkbox
                        v-if="!hasChildren(menu)"
                        :model-value="isChecked(menu.menusIndex)"
                        @change="(val) => handleCheck(menu.menusIndex, val)"
                        class="menu-checkbox"
                      >
                        <span class="menu-label">{{ menu.title }}</span>
                      </el-checkbox>
                      <span v-else class="menu-label parent-label">{{ menu.title }}</span>
                    </div>
                    <div v-if="hasChildren(menu) && isExpanded(menu.menusIndex)" class="menu-children">
                      <template v-for="child in menu.children" :key="child.menusIndex">
                        <div class="menu-item">
                          <div class="menu-item-content">
                            <span class="expand-icon" v-if="hasChildren(child)" @click="toggleExpand(child.menusIndex)">
                              <el-icon><ArrowRight v-if="!isExpanded(child.menusIndex)" /><ArrowDown v-else /></el-icon>
                            </span>
                            <span class="expand-placeholder" v-else></span>
                            <span class="menu-icon-wrapper">
                              <svg-icon v-if="child.icon" :icon-class="child.icon" class="menu-icon" />
                              <el-icon v-else class="menu-icon default-icon"><Document /></el-icon>
                            </span>
                            <el-checkbox
                              v-if="!hasChildren(child)"
                              :model-value="isChecked(child.menusIndex)"
                              @change="(val) => handleCheck(child.menusIndex, val)"
                              class="menu-checkbox"
                            >
                              <span class="menu-label">{{ child.title }}</span>
                            </el-checkbox>
                            <span v-else class="menu-label parent-label">{{ child.title }}</span>
                          </div>
                          <div v-if="hasChildren(child) && isExpanded(child.menusIndex)" class="menu-children">
                            <template v-for="grandchild in child.children" :key="grandchild.menusIndex">
                              <div class="menu-item">
                                <div class="menu-item-content">
                                  <span class="expand-placeholder"></span>
                                  <span class="menu-icon-wrapper">
                                    <svg-icon v-if="grandchild.icon" :icon-class="grandchild.icon" class="menu-icon" />
                                    <el-icon v-else class="menu-icon default-icon"><Document /></el-icon>
                                  </span>
                                  <el-checkbox
                                    :model-value="isChecked(grandchild.menusIndex)"
                                    @change="(val) => handleCheck(grandchild.menusIndex, val)"
                                    class="menu-checkbox"
                                  >
                                    <span class="menu-label">{{ grandchild.title }}</span>
                                  </el-checkbox>
                                </div>
                              </div>
                            </template>
                          </div>
                        </div>
                      </template>
                    </div>
                  </div>
                </template>
              </div>
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
  Check,
  ArrowRight,
  ArrowDown
} from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

// 数据
const roleList = ref([])
const roleSearch = ref('')
const currentRoleId = ref(null)
const menuTree = ref([])
const checkedKeys = ref([])
const originalCheckedKeys = ref([])
const expandedKeys = ref([])

// 状态
const menuLoading = ref(false)
const saveLoading = ref(false)

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

// 辅助函数
function hasChildren(menu) {
  return menu.children && menu.children.length > 0
}

function isExpanded(menuIndex) {
  return expandedKeys.value.includes(menuIndex)
}

function isChecked(menuIndex) {
  return checkedKeys.value.includes(menuIndex)
}

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
    // 默认展开所有
    expandAll()
  }).catch(() => {
    proxy.$modal.msgError('获取菜单权限失败')
  }).finally(() => {
    menuLoading.value = false
  })
}

// 切换展开状态
function toggleExpand(menuIndex) {
  const index = expandedKeys.value.indexOf(menuIndex)
  if (index > -1) {
    expandedKeys.value.splice(index, 1)
  } else {
    expandedKeys.value.push(menuIndex)
  }
}

// 展开全部
function expandAll() {
  const keys = []
  function traverse(nodes) {
    for (const node of nodes) {
      if (node.children && node.children.length > 0) {
        keys.push(node.menusIndex)
        traverse(node.children)
      }
    }
  }
  traverse(menuTree.value)
  expandedKeys.value = keys
}

// 折叠全部
function collapseAll() {
  expandedKeys.value = []
}

// 处理复选框变化
function handleCheck(menuIndex, checked) {
  const index = checkedKeys.value.indexOf(menuIndex)
  if (checked && index === -1) {
    checkedKeys.value.push(menuIndex)
  } else if (!checked && index > -1) {
    checkedKeys.value.splice(index, 1)
  }
}

// 保存权限
function handleSave() {
  if (!currentRoleId.value) {
    proxy.$modal.msgWarning('请先选择一个角色')
    return
  }

  proxy.$modal.confirm(`确认保存角色「${currentRole.value?.name}」的菜单权限吗？`).then(() => {
    saveLoading.value = true
    saveRoleMenu(currentRoleId.value, checkedKeys.value).then(() => {
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

// 重置
function handleReset() {
  checkedKeys.value = [...originalCheckedKeys.value]
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

.menu-list-wrapper {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 0;
  min-height: 0;
}

.menu-list {
  padding: 8px;
}

.menu-item {
  user-select: none;
}

.menu-item-content {
  display: flex;
  align-items: center;
  height: 36px;
  padding: 0 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.menu-item-content:hover {
  background-color: #f5f7fa;
}

.expand-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  cursor: pointer;
  color: #909399;
}

.expand-icon:hover {
  color: #409eff;
}

.expand-placeholder {
  width: 24px;
}

.menu-icon-wrapper {
  display: flex;
  align-items: center;
  margin-right: 8px;
}

.menu-icon {
  color: #409eff;
  font-size: 16px;
}

.menu-icon.default-icon {
  color: #909399;
}

.menu-checkbox {
  display: flex;
  align-items: center;
}

.menu-label {
  font-size: 14px;
  color: #303133;
}

.menu-label.parent-label {
  font-weight: 500;
}

.menu-children {
  padding-left: 24px;
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
.menu-list-wrapper::-webkit-scrollbar {
  width: 6px;
}

.role-list-wrapper::-webkit-scrollbar-thumb,
.menu-list-wrapper::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.role-list-wrapper::-webkit-scrollbar-track,
.menu-list-wrapper::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}
</style>
