<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="菜单名称" prop="menuName">
        <el-input
          v-model="queryParams.menuName"
          placeholder="请输入菜单名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="Sort"
          @click="toggleExpandAll"
        >展开/折叠</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 表头 -->
    <div v-if="menuList.length" class="menu-tree-header">
      <span class="header-drag"></span>
      <span class="header-expand"></span>
      <span class="col col-sort">排序</span>
      <span class="col col-title" style="flex: 1; min-width: 160px;">菜单标题</span>
      <span class="col col-icon">图标</span>
      <span class="col col-index">菜单索引</span>
      <span class="col col-path">路径</span>
      <span class="col col-parent">父级索引</span>
      <span class="col col-actions">操作</span>
    </div>

    <!-- 拖拽排序树 -->
    <div v-loading="loading" class="menu-tree-body">
      <MenuTreeItem
        :items="menuList"
        :level="0"
        @sort-change="handleSortChange"
        @edit="handleUpdate"
      />
      <el-empty v-if="!loading && !menuList.length" description="暂无菜单数据" />
    </div>

    <!-- 添加或修改菜单对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="menuRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="上级菜单">
              <el-tree-select
                v-model="form.parentId"
                :data="menuOptions"
                :props="{ value: 'menusIndex', label: 'title', children: 'children' }"
                value-key="id"
                placeholder="选择上级菜单（0为顶级）"
                check-strictly
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单索引" prop="menusIndex">
              <el-input-number v-model="form.menusIndex" controls-position="right" :min="1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="form.sort" controls-position="right" :min="0" :max="9999" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入菜单标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="图标" prop="icon">
              <el-select v-model="form.icon" placeholder="请选择图标" clearable style="width: 100%">
                <el-option v-for="icon in iconOptions" :key="icon" :value="icon">
                  <div style="display: flex; align-items: center; gap: 8px;">
                    <svg-icon :icon-class="icon" />
                    <span>{{ icon }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="路径" prop="path">
              <el-input v-model="form.path" placeholder="请输入路径" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Menu">
import { provide, reactive, ref, toRefs, getCurrentInstance } from 'vue'
import request from '@/utils/request'
import iconOptions from '@/components/IconSelect/requireIcons'
import MenuTreeItem from './MenuTreeItem.vue'

const { proxy } = getCurrentInstance()

const menuList = ref([])
const menuOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const title = ref('')

// 拖拽排序相关：展开状态管理
const expandedIds = reactive(new Set())
const expandAllFlag = ref(true)

// 提供展开状态给子组件
provide('expandedIds', expandedIds)
provide('expandAllFlag', expandAllFlag)

const data = reactive({
  form: {},
  queryParams: {
    menuName: undefined
  },
  rules: {
    title: [{ required: true, message: '菜单标题不能为空', trigger: 'blur' }],
    menusIndex: [{ required: true, message: '菜单索引不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

// API调用
function listMenu(params) {
  return request({
    url: '/system/menu/list',
    method: 'get',
    params
  })
}

function getMenu(id) {
  return request({
    url: '/system/menu/' + id,
    method: 'get'
  })
}

function addMenu(data) {
  return request({
    url: '/system/menu',
    method: 'post',
    data
  })
}

function updateMenu(data) {
  return request({
    url: '/system/menu',
    method: 'put',
    data
  })
}

function delMenu(id) {
  return request({
    url: '/system/menu/' + id,
    method: 'delete'
  })
}

function treeselect() {
  return request({
    url: '/system/menu/treeselect',
    method: 'get'
  })
}

function updateSort(data) {
  return request({
    url: '/system/menu/sort',
    method: 'put',
    data
  })
}

// 查询菜单列表 - 使用 treeselect 接口直接获取树形结构
function getList() {
  loading.value = true
  treeselect().then(res => {
    loading.value = false
    menuList.value = res.data || []
    // 默认全部展开
    expandAllNodes(menuList.value)
  })
}

// 收集所有节点 ID 以展开全部
function expandAllNodes(menus) {
  for (const menu of menus) {
    expandedIds.add(menu.id)
    if (menu.children && menu.children.length) {
      expandAllNodes(menu.children)
    }
  }
}

// 收集所有节点 ID
function collectAllIds(menus) {
  const ids = []
  for (const menu of menus) {
    ids.push(menu.id)
    if (menu.children && menu.children.length) {
      ids.push(...collectAllIds(menu.children))
    }
  }
  return ids
}

// 获取菜单下拉树结构
function getTreeselect() {
  treeselect().then(res => {
    menuOptions.value = [{ id: 0, menusIndex: 0, title: '主目录', children: res.data }]
  })
}

// 搜索按钮操作
function handleQuery() {
  getList()
}

// 重置按钮操作
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

// 展开/折叠全部
function toggleExpandAll() {
  expandAllFlag.value = !expandAllFlag.value
  if (expandAllFlag.value) {
    // 展开全部
    const allIds = collectAllIds(menuList.value)
    allIds.forEach(id => expandedIds.add(id))
  } else {
    // 折叠全部
    expandedIds.clear()
  }
}

// 新增按钮操作
function handleAdd(row) {
  reset()
  if (row && row.id) {
    form.value.parentId = row.menusIndex
  } else {
    form.value.parentId = 0
  }
  open.value = true
  title.value = '添加菜单'
}

// 修改按钮操作
function handleUpdate(row) {
  reset()
  const id = row.id
  getMenu(id).then(res => {
    form.value = res.data
    open.value = true
    title.value = '修改菜单'
  })
}

// 删除按钮操作
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除菜单编号为"' + row.id + '"的数据项？').then(function() {
    return delMenu(row.id)
  }).then(() => {
    getList()
    getTreeselect()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

// 拖拽排序变更后自动保存
function handleSortChange(sortData) {
  updateSort(sortData).then(() => {
    proxy.$modal.msgSuccess('排序已保存')
    // 刷新下拉树，确保新增/编辑时的菜单树顺序一致
    getTreeselect()
  }).catch(() => {
    proxy.$modal.msgError('排序保存失败')
    // 排序保存失败时重新加载列表恢复原顺序
    getList()
  })
}

// 提交按钮
function submitForm() {
  proxy.$refs['menuRef'].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateMenu(form.value).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
          getTreeselect()
        })
      } else {
        addMenu(form.value).then(response => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
          getTreeselect()
        })
      }
    }
  })
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 重置
function reset() {
  form.value = {
    id: null,
    menusIndex: null,
    sort: 0,
    title: '',
    icon: '',
    path: '',
    parentId: 0
  }
  proxy.resetForm('menuRef')
}

getList()
getTreeselect()
</script>

<style scoped>
/* 表头 */
.menu-tree-header {
  display: flex;
  align-items: center;
  padding: 0 8px;
  min-height: 40px;
  background: var(--el-fill-color);
  border-bottom: 2px solid var(--el-border-color);
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}

.header-drag {
  flex-shrink: 0;
  width: 24px;
}

.header-expand {
  flex-shrink: 0;
  width: 20px;
}

.menu-tree-header .col {
  flex-shrink: 0;
  padding: 0 8px;
  font-size: 14px;
}

.menu-tree-header .col-title {
  flex: 1;
  min-width: 160px;
}

.menu-tree-header .col-icon {
  width: 80px;
  text-align: center;
}

.menu-tree-header .col-sort {
  width: 80px;
  text-align: center;
}

.menu-tree-header .col-index {
  width: 80px;
  text-align: center;
}

.menu-tree-header .col-path {
  width: 150px;
}

.menu-tree-header .col-parent {
  width: 80px;
  text-align: center;
}

.menu-tree-header .col-actions {
  width: 80px;
  text-align: center;
}

/* 树体容器 */
.menu-tree-body {
  border: 1px solid var(--el-border-color);
  border-top: none;
  min-height: 100px;
  background: #fff;
}
</style>
