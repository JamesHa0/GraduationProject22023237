<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学术成果管理</span>
          <el-button type="primary" size="small" @click="handleAdd">新增成果</el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
        <el-form-item label="成果类型" prop="type">
          <el-select v-model="queryParams.type" placeholder="请选择" clearable style="width: 120px">
            <el-option label="论文" :value="1" />
            <el-option label="专利" :value="2" />
            <el-option label="获奖" :value="3" />
            <el-option label="项目参与" :value="4" />
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
        <el-table-column label="成果类型" align="center" prop="type" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="成果标题" align="center" prop="title" show-overflow-tooltip />
        <el-table-column label="发表时间" align="center" prop="publicationTime" width="180" />
        <el-table-column label="导师审批" align="center" prop="tutorApproval" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.tutorApproval)">{{ getStatusText(row.tutorApproval) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="秘书审批" align="center" prop="secretaryApproval" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.secretaryApproval)">{{ getStatusText(row.secretaryApproval) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="handleView(row)">详情</el-button>
            <el-button link size="small" type="primary" @click="handleApprove(row)" v-if="isMentor">审批</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="open" width="600px" append-to-body>
      <el-form :model="form" :rules="rules" ref="achievementRef" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="成果类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择" :disabled="isView">
                <el-option label="论文" :value="1" />
                <el-option label="专利" :value="2" />
                <el-option label="获奖" :value="3" />
                <el-option label="项目参与" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="成果标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入成果标题" :disabled="isView" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="成果描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入成果描述" :disabled="isView" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="发表时间" prop="publicationTime">
              <el-date-picker v-model="form.publicationTime" type="date" placeholder="选择发表时间" :disabled="isView" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="附件上传" prop="fileUrl">
              <el-input v-model="form.fileUrl" placeholder="请输入附件URL" :disabled="isView" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="open = false">{{ isView ? '关闭' : '取消' }}</el-button>
          <el-button type="primary" @click="handleSubmit" v-if="!isView">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AcademicAchievement">
import { ref, reactive, getCurrentInstance, toRefs } from 'vue'
import { ElMessage } from 'element-plus'
import { listAchievement, submitAchievement, delAchievement, approveAchievement } from '@/api/academic'

const { proxy } = getCurrentInstance()

const dataList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const dialogTitle = ref('')
const isView = ref(false)
const isMentor = ref(true)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    type: undefined,
    status: undefined
  },
  rules: {
    type: [{ required: true, message: '请选择成果类型', trigger: 'change' }],
    title: [{ required: true, message: '成果标题不能为空', trigger: 'blur' }],
    description: [{ required: true, message: '请输入成果描述', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getTypeTagType(type) {
  const typeMap = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'info' }
  return typeMap[type] || 'info'
}

function getTypeName(type) {
  const textMap = { 1: '论文', 2: '专利', 3: '获奖', 4: '项目参与' }
  return textMap[type] || '-'
}

function getStatusText(status) {
  const textMap = { 0: '待审批', 1: '已通过', 2: '已拒绝' }
  return textMap[status] || '-'
}

function getStatusType(status) {
  const typeMap = { 0: 'warning', 1: 'success', 2: 'danger' }
  return typeMap[status] || 'info'
}

function getList() {
  loading.value = true
  listAchievement(queryParams.value).then(res => {
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
  proxy.resetForm('queryRef')
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
    description: undefined,
    publicationTime: undefined,
    fileUrl: undefined
  }
}

function cancel() {
  open.value = false
  reset()
}

function handleAdd() {
  dialogTitle.value = '新增成果'
  isView.value = false
  reset()
  open.value = true
}

function handleView(row) {
  dialogTitle.value = '成果详情'
  isView.value = true
  Object.assign(form, row)
  open.value = true
}

function handleSubmit() {
  proxy.$refs['achievementRef'].validate(valid => {
    if (valid) {
      submitAchievement(form.value).then(res => {
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

function handleApprove(row) {
  const params = {
    id: row.id,
    status: 1,
    comment: '审批通过'
  }

  approveAchievement(params).then(res => {
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

function handleDelete(row) {
  const achievementIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除？').then(() => {
    delAchievement(achievementIds).then(res => {
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

getList()
</script>
