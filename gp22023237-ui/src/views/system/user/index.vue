<template>
  <div class="app-container">
    <el-col>
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
        <el-form-item label="用户名称" prop="userName">
          <el-input v-model="queryParams.userName" placeholder="请输入用户名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="手机号码" prop="phonenumber">
          <el-input v-model="queryParams.phonenumber" placeholder="请输入手机号码" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="用户状态" clearable style="width: 200px">
            <el-option v-for="dict in statusOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate">修改</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="用户编号" align="center" prop="id" width="100" />
        <el-table-column label="用户名" align="center" prop="username" :show-overflow-tooltip="true" />
        <el-table-column label="姓名" align="center" prop="name" :show-overflow-tooltip="true" />
        <el-table-column label="性别" align="center" prop="gender" width="80">
          <template #default="scope">
            {{ genderLabel(scope.row.gender) }}
          </template>
        </el-table-column>
        <el-table-column label="手机" align="center" prop="phone" width="120" />
        <el-table-column label="邮箱" align="center" prop="email" :show-overflow-tooltip="true" />
        <el-table-column label="角色ID" align="center" prop="roleId" width="80" />
        <el-table-column label="状态" align="center" prop="status" width="80">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            ></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="160">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
            <el-button link type="primary" icon="Key" @click="handleResetPwd(scope.row)">重置密码</el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-col>

    <!-- 添加或修改用户对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="userRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="form.id != null" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="form.id == null">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择">
            <el-option v-for="dict in genderOptions" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="手机" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择">
            <el-option v-for="role in roleList" :key="role.id" :label="role.name" :value="role.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in statusOptions" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog title="重置密码" v-model="resetPwdOpen" width="400px" append-to-body>
      <el-form ref="resetPwdRef" :model="resetPwdForm" :rules="resetPwdRules" label-width="80px">
        <el-form-item label="新密码" prop="password">
          <el-input v-model="resetPwdForm.password" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitResetPwd">确 定</el-button>
          <el-button @click="resetPwdOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="User">
import { listRole } from "@/api/system/role"
import request from '@/utils/request'

const { proxy } = getCurrentInstance()

// 硬编码选项
const genderOptions = [
  { label: '男', value: 1 },
  { label: '女', value: 2 }
]
const statusOptions = [
  { label: '正常', value: 1 },
  { label: '禁用', value: 0 }
]

// 工具函数
const genderLabel = (value) => {
  return value === 1 ? '男' : value === 2 ? '女' : '未知'
}

const statusLabel = (value) => {
  return value === 1 ? '正常' : '禁用'
}

const userList = ref([])
const roleList = ref([])
const open = ref(false)
const resetPwdOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    phonenumber: undefined,
    status: undefined
  },
  rules: {
    username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
    name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
    password: [{ required: true, message: '密码不能为空', trigger: 'blur' }]
  },
  resetPwdForm: {},
  resetPwdRules: {
    password: [{ required: true, message: '新密码不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules, resetPwdForm, resetPwdRules } = toRefs(data)

// API调用
function listUser(params) {
  return request({
    url: '/system/user/list',
    method: 'get',
    params
  })
}

function getUser(id) {
  return request({
    url: '/system/user/' + id,
    method: 'get'
  })
}

function addUser(data) {
  return request({
    url: '/system/user',
    method: 'post',
    data
  })
}

function updateUser(data) {
  return request({
    url: '/system/user',
    method: 'put',
    data
  })
}

function delUser(ids) {
  return request({
    url: '/system/user/' + ids,
    method: 'delete'
  })
}

function changeUserStatus(data) {
  return request({
    url: '/system/user/changeStatus',
    method: 'put',
    data
  })
}

function resetUserPwd(data) {
  return request({
    url: '/system/user/resetPwd',
    method: 'put',
    data
  })
}

// 查询用户列表
function getList() {
  loading.value = true
  listUser(queryParams.value).then(res => {
    loading.value = false
    userList.value = res.data.rows || res.data || []
    total.value = res.data.total || 0
  })
}

// 查询角色列表
function getRoleList() {
  listRole().then(res => {
    roleList.value = res.data.rows || res.data || []
  })
}

// 搜索按钮操作
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

// 重置按钮操作
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

// 选择条数
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

// 新增按钮操作
function handleAdd() {
  reset()
  open.value = true
  title.value = '添加用户'
}

// 修改按钮操作
function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getUser(id).then(res => {
    form.value = res.data
    open.value = true
    title.value = '修改用户'
  })
}

// 重置密码
function handleResetPwd(row) {
  resetPwdForm.value = { id: row.id }
  resetPwdOpen.value = true
}

// 提交重置密码
function submitResetPwd() {
  proxy.$refs['resetPwdRef'].validate(valid => {
    if (valid) {
      resetUserPwd(resetPwdForm.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        resetPwdOpen.value = false
      })
    }
  })
}

// 删除按钮操作
function handleDelete(row) {
  const userIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除用户编号为"' + userIds + '"的数据项？').then(function() {
    return delUser(userIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

// 用户状态修改
function handleStatusChange(row) {
  let text = row.status === 1 ? '停用' : '启用'
  proxy.$modal.confirm('确认要"' + text + '""' + row.name + '"用户吗?').then(function() {
    return changeUserStatus(row)
  }).then(() => {
    proxy.$modal.msgSuccess(text + '成功')
  }).catch(function() {
    row.status = row.status === 1 ? 0 : 1
  })
}

// 提交按钮
function submitForm() {
  proxy.$refs['userRef'].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateUser(form.value).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addUser(form.value).then(response => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
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
    username: '',
    name: '',
    password: '',
    gender: 1,
    phone: '',
    email: '',
    roleId: null,
    status: 1
  }
  proxy.resetForm('userRef')
}

getList()
getRoleList()
</script>
