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
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Check"
          :disabled="!sortChanged"
          @click="handleSaveSort"
        >保存排序</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table
      v-if="refreshTable"
      v-loading="loading"
      :data="menuList"
      row-key="id"
      :default-expand-all="isExpandAll"
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="title" label="菜单标题" :show-overflow-tooltip="true" width="200"></el-table-column>
      <el-table-column prop="icon" label="图标" align="center" width="100">
        <template #default="scope">
          <svg-icon v-if="scope.row.icon" :icon-class="scope.row.icon" />
          <span v-else>{{ scope.row.icon }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" align="center" width="120">
        <template #default="scope">
          <span v-if="!scope.row.parentId || scope.row.parentId === 0" style="color: #909399; font-weight: 500;">{{ scope.row.sort }}</span>
          <el-input-number
            v-else
            v-model="scope.row.sort"
            :min="0"
            :max="9999"
            controls-position="right"
            size="small"
            style="width: 90px"
            @change="onSortChange"
          />
        </template>
      </el-table-column>
      <el-table-column prop="menusIndex" label="菜单索引" width="100"></el-table-column>
      <el-table-column prop="path" label="路径" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="parentId" label="父级索引" width="100"></el-table-column>
      <el-table-column label="操作" align="center" width="210" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <!-- <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)">新增</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button> -->
        </template>
      </el-table-column>
    </el-table>

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
import request from '@/utils/request'
import iconOptions from '@/components/IconSelect/requireIcons'

const { proxy } = getCurrentInstance()

const menuList = ref([])
const menuOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const isExpandAll = ref(true)
const refreshTable = ref(true)
const title = ref('')
const sortChanged = ref(false)
const originalSortMap = ref({})

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

// 收集所有菜单项（扁平化树结构）
function flattenMenus(menus) {
  const result = []
  for (const menu of menus) {
    result.push(menu)
    if (menu.children && menu.children.length) {
      result.push(...flattenMenus(menu.children))
    }
  }
  return result
}

// 保存原始排序值
function saveOriginalSort(menus) {
  const flat = flattenMenus(menus)
  originalSortMap.value = {}
  for (const menu of flat) {
    originalSortMap.value[menu.id] = menu.sort
  }
  sortChanged.value = false
}

// 排序号变更检测
function onSortChange() {
  const flat = flattenMenus(menuList.value)
  for (const menu of flat) {
    if (menu.sort !== originalSortMap.value[menu.id]) {
      sortChanged.value = true
      return
    }
  }
  sortChanged.value = false
}

// 查询菜单列表 - 使用 treeselect 接口直接获取树形结构
function getList() {
  loading.value = true
  treeselect().then(res => {
    loading.value = false
    menuList.value = res.data || []
    saveOriginalSort(menuList.value)
  })
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

// 展开/折叠
function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
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

// 批量保存排序
function handleSaveSort() {
  const flat = flattenMenus(menuList.value)
  const changedItems = flat.filter(menu => menu.sort !== originalSortMap.value[menu.id])
  if (changedItems.length === 0) {
    proxy.$modal.msgSuccess('排序未变更')
    return
  }
  const sortData = changedItems.map(item => ({ id: item.id, sort: item.sort }))
  updateSort(sortData).then(() => {
    proxy.$modal.msgSuccess('排序保存成功')
    sortChanged.value = false
    saveOriginalSort(menuList.value)
    getTreeselect()
  }).catch(() => {
    proxy.$modal.msgError('排序保存失败')
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
