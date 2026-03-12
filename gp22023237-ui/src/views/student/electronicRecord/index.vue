<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="学生ID" prop="studentId">
        <el-input v-model="queryParams.studentId" placeholder="请输入学生ID" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="档案类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="档案类型" clearable style="width: 150px">
          <el-option label="基本信息" value="1" />
          <el-option label="学习经历" value="2" />
          <el-option label="奖惩情况" value="3" />
          <el-option label="科研成果" value="4" />
          <el-option label="其他" value="5" />
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
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="recordList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="学生ID" align="center" prop="studentId" width="100" />
      <el-table-column label="档案类型" align="center" prop="type" width="100">
        <template #default="scope">
          <el-tag :type="getTypeTagType(scope.row.type)">{{ getTypeText(scope.row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="档案内容" align="center" prop="content" :show-overflow-tooltip="true" />
      <el-table-column label="附件URL" align="center" prop="fileUrl" width="200" :show-overflow-tooltip="true" />
      <el-table-column label="创建人ID" align="center" prop="createBy" width="100" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="success" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="学生ID" prop="studentId">
          <el-input v-model="form.studentId" placeholder="请输入学生ID" />
        </el-form-item>
        <el-form-item label="档案类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择档案类型" style="width: 100%">
            <el-option label="基本信息" :value="1" />
            <el-option label="学习经历" :value="2" />
            <el-option label="奖惩情况" :value="3" />
            <el-option label="科研成果" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="档案内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入档案内容" />
        </el-form-item>
        <el-form-item label="附件URL" prop="fileUrl">
          <el-input v-model="form.fileUrl" placeholder="请输入附件URL" />
        </el-form-item>
        <el-form-item label="创建人ID" prop="createBy" v-if="form.id">
          <el-input v-model="form.createBy" placeholder="请输入创建人ID" />
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

<script setup name="ElectronicRecord">
import { listElectronicRecord, addElectronicRecord, updateElectronicRecord, deleteElectronicRecord } from "@/api/student/electronicRecord"

const { proxy } = getCurrentInstance()

const recordList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentId: null,
    type: null
  },
  rules: {
    studentId: [{ required: true, message: "学生ID不能为空", trigger: "blur" }],
    type: [{ required: true, message: "档案类型不能为空", trigger: "change" }],
    content: [{ required: true, message: "档案内容不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listElectronicRecord(queryParams.value).then(response => {
    recordList.value = response.data.records
    total.value = response.data.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    studentId: null,
    type: null,
    content: null,
    fileUrl: null,
    createBy: null
  }
  proxy.resetForm("formRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增电子档案"
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  // 在实际项目中应该先调用 getElectronicRecord(id) 获取详情
  form.value = { ...row }
  open.value = true
  title.value = "修改电子档案"
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.id) {
        updateElectronicRecord(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addElectronicRecord(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleView(row) {
  proxy.$modal.msgInfo("详情: " + JSON.stringify(row))
}

function handleDelete(row) {
  const deleteIds = row.id ? [row.id] : ids.value
  proxy.$modal.confirm('是否确认删除ID为"' + deleteIds.join(', ') + '"的档案记录？').then(function() {
    return Promise.all(deleteIds.map(id => deleteElectronicRecord(id)))
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function getTypeText(type) {
  const typeMap = { 1: '基本信息', 2: '学习经历', 3: '奖惩情况', 4: '科研成果', 5: '其他' }
  return typeMap[type] || '未知'
}

function getTypeTagType(type) {
  const typeMap = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'info', 5: '' }
  return typeMap[type] || ''
}

getList()
</script>
