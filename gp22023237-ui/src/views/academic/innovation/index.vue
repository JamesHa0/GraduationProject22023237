<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>创新实践项目</span>
          <el-button type="primary" size="small" @click="handleAdd">新增项目</el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
        <el-form-item label="项目类型" prop="type">
          <el-select v-model="queryParams.type" placeholder="请选择" clearable style="width: 120px">
            <el-option label="省级项目" :value="1" />
            <el-option label="国家级项目" :value="2" />
            <el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待审批" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
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
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="ID" align="center" prop="id" width="80" />
        <el-table-column label="学生ID" align="center" prop="studentId" width="100" />
        <el-table-column label="项目类型" align="center" prop="type" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="项目标题" align="center" prop="title" show-overflow-tooltip />
        <el-table-column label="主办方" align="center" prop="organizer" width="120" show-overflow-tooltip />
        <el-table-column label="项目时间" align="center" prop="time" width="180" />
        <el-table-column label="获奖情况" align="center" prop="award" width="150" show-overflow-tooltip />
        <el-table-column label="导师审批" align="center" prop="tutorApproval" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.tutorApproval)">{{ getStatusText(row.tutorApproval) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" @click="handleView(row)"></el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(row)"></el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form :model="form" :rules="rules" ref="innovationRef" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="项目类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择" :disabled="isView">
                <el-option label="省级项目" :value="1" />
                <el-option label="国家级项目" :value="2" />
                <el-option label="其他" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="项目标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入项目标题" :disabled="isView" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="主办方" prop="organizer">
              <el-input v-model="form.organizer" placeholder="请输入主办方" :disabled="isView" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="项目时间" prop="time">
              <el-date-picker v-model="form.time" type="datetime" placeholder="选择时间" :disabled="isView" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="获奖情况" prop="award">
              <el-input v-model="form.award" placeholder="请输入获奖情况" :disabled="isView" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="项目描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入项目描述" :disabled="isView" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">取消</el-button>
          <el-button type="primary" @click="submitForm" v-if="!isView">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="InnovationProject">
import { ref, reactive, getCurrentInstance, toRefs } from 'vue'
import { ElMessage } from 'element-plus'
import { listInnovation, submitInnovation, delInnovation, approveInnovation } from '@/api/academic'

const { proxy } = getCurrentInstance()

const dataList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const isView = ref(false)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    type: undefined,
    status: undefined
  },
  rules: {
    type: [{ required: true, message: "项目类型不能为空", trigger: "change" }],
    title: [{ required: true, message: "项目标题不能为空", trigger: "blur" }],
    organizer: [{ required: true, message: "主办方不能为空", trigger: "blur" }],
    time: [{ required: true, message: "项目时间不能为空", trigger: "change" }],
    award: [{ required: false, message: "", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getTypeTagType(type) {
  const typeMap = { 1: "success", 2: "primary", 3: "info" }
  return typeMap[type] || "info"
}

function getTypeName(type) {
  const textMap = { 1: "省级项目", 2: "国家级项目", 3: "其他" }
  return textMap[type] || "-"
}

function getStatusText(status) {
  const textMap = { 0: "待审批", 1: "已通过", 2: "已拒绝" }
  return textMap[status] || "-"
}

function getStatusType(status) {
  const typeMap = { 0: "warning", 1: "success", 2: "danger" }
  return typeMap[status] || "info"
}

function getList() {
  loading.value = true
  listInnovation(queryParams.value).then(res => {
    loading.value = false
    if (res.code === 200) {
      dataList.value = res.data || []
      total.value = dataList.value.length
    } else {
      ElMessage.error(res.msg || '获取数据失败')
    }
  }).catch(err => {
    loading.value = false
    ElMessage.error('获取数据失败')
    console.error(err)
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  queryParams.value.type = undefined
  queryParams.value.status = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    id: undefined,
    studentId: undefined,
    type: undefined,
    title: undefined,
    organizer: undefined,
    time: undefined,
    award: undefined,
    description: undefined
  }
}

function cancel() {
  open.value = false
  reset()
}

function handleAdd() {
  reset()
  isView.value = false
  open.value = true
  title.value = "添加创新项目"
}

function handleView(row) {
  reset()
  isView.value = true
  Object.assign(form, row)
  open.value = true
  title.value = "项目详情"
}

function submitForm() {
  proxy.$refs["innovationRef"].validate(valid => {
    if (valid) {
      submitInnovation(form.value).then(res => {
        if (res.code === 200) {
          ElMessage.success('提交成功')
          open.value = false
          getList()
        } else {
          ElMessage.error(res.msg || '提交失败')
        }
      }).catch(err => {
        ElMessage.error('提交失败')
        console.error(err)
      })
    }
  })
}

function handleDelete(row) {
  const innovationIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除？').then(() => {
    delInnovation(innovationIds).then(res => {
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getList()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    }).catch(err => {
      ElMessage.error('删除失败')
      console.error(err)
    })
  }).catch(() => {})
}

function handleApprove(row) {
  const params = {
    id: row.id,
    status: 1,
    comment: '审批通过'
  }

  approveInnovation(params).then(res => {
    if (res.code === 200) {
      ElMessage.success('审批成功')
      getList()
    } else {
      ElMessage.error(res.msg || '审批失败')
    }
  }).catch(err => {
    ElMessage.error('审批失败')
    console.error(err)
  })
}

getList()
</script>
