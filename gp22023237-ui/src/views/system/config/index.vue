<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" v-show="showSearch" :inline="true" label-width="68px">
      <el-form-item label="配置名称" prop="configName">
        <el-input
          v-model="queryParams.configName"
          placeholder="请输入配置名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="配置键名" prop="configKey">
        <el-input
          v-model="queryParams.configKey"
          placeholder="请输入配置键名"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="配置类型" prop="configType">
        <el-select
          v-model="queryParams.configType"
          placeholder="配置类型"
          clearable
          style="width: 200px"
        >
          <el-option
            v-for="dict in configTypeOptions"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
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
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Refresh"
          @click="handleRefreshCache"
        >刷新缓存</el-button>
      </el-col>
    </el-row>

    <!-- 表格数据 -->
    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" prop="id" width="80" />
      <el-table-column label="配置名称" prop="configName" :show-overflow-tooltip="true" />
      <el-table-column label="配置键名" prop="configKey" :show-overflow-tooltip="true" />
      <el-table-column label="配置键值" prop="configValue" :show-overflow-tooltip="true" />
      <el-table-column label="配置类型" prop="configType" width="100">
        <template #default="scope">
          <span>{{ getConfigTypeLabel(scope.row.configType) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="更新时间" align="center" prop="updateTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加或修改配置对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="configRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="form.configName" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="配置键名" prop="configKey">
          <el-input v-model="form.configKey" placeholder="请输入配置键名" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="配置键值" prop="configValue">
          <el-input v-model="form.configValue" type="textarea" placeholder="请输入配置键值" :rows="3" />
        </el-form-item>
        <el-form-item label="配置类型" prop="configType">
          <el-select v-model="form.configType" placeholder="请选择配置类型" style="width: 100%">
            <el-option
              v-for="dict in configTypeOptions"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" :rows="2" />
        </el-form-item>
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

<script setup name="Config">
import { listConfig, getConfig, delConfig, addConfig, updateConfig, refreshCache } from '@/api/system/config'

const { proxy } = getCurrentInstance()

// 硬编码选项
const configTypeOptions = [
  { label: '系统', value: 'system' },
  { label: '业务', value: 'business' }
]

const configList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    configName: undefined,
    configKey: undefined,
    configType: undefined
  },
  rules: {
    configName: [{ required: true, message: '配置名称不能为空', trigger: 'blur' }],
    configKey: [{ required: true, message: '配置键名不能为空', trigger: 'blur' }],
    configValue: [{ required: true, message: '配置键值不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

// 获取配置类型标签
function getConfigTypeLabel(configType) {
  const option = configTypeOptions.find(o => o.value === configType)
  return option ? option.label : configType
}

// 查询配置列表
function getList() {
  loading.value = true
  listConfig(queryParams.value).then(res => {
    loading.value = false
    configList.value = res.data?.rows || res.data || []
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
  title.value = '添加参数'
}

// 修改按钮操作
function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getConfig(id).then(res => {
    form.value = res.data
    open.value = true
    title.value = '修改参数'
  })
}

// 删除按钮操作
function handleDelete(row) {
  const configIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除参数编号为"' + configIds + '"的数据项？').then(function() {
    return delConfig(configIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

// 刷新缓存按钮操作
function handleRefreshCache() {
  refreshCache().then(() => {
    proxy.$modal.msgSuccess('刷新缓存成功')
  })
}

// 提交按钮
function submitForm() {
  proxy.$refs['configRef'].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateConfig(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addConfig(form.value).then(() => {
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
    configName: '',
    configKey: '',
    configValue: '',
    configType: 'system',
    remark: ''
  }
  proxy.resetForm('configRef')
}

getList()
</script>
