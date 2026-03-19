<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学术活动管理</span>
          <el-button type="primary" size="small" @click="handleAdd">新增活动</el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
        <el-form-item label="活动类型" prop="type">
          <el-select v-model="queryParams.type" placeholder="请选择" clearable style="width: 120px">
            <el-option label="学术讲座" :value="1" />
            <el-option label="研讨会" :value="2" />
            <el-option label="论坛" :value="3" />
            <el-option label="其他" :value="4" />
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

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="dataList" border>
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="学生ID" prop="studentId" align="center" width="100" />
        <el-table-column label="活动类型" prop="type" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="活动标题" prop="title" align="center" show-overflow-tooltip />
        <el-table-column label="主办方" prop="organizer" align="center" width="120" show-overflow-tooltip />
        <el-table-column label="活动地点" prop="location" align="center" width="120" show-overflow-tooltip />
        <el-table-column label="活动时间" prop="time" align="center" width="180" />
        <el-table-column label="导师审批" prop="tutorApproval" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.tutorApproval)">{{ getStatusText(row.tutorApproval) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="handleView(row)">详情</el-button>
            <el-button link size="small" type="primary" @click="handleApprove(row)" v-if="isMentor">审批</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <!-- 新增/详情对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px" append-to-body>
      <el-form :ref="formRef" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="活动类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择" :disabled="isView">
                <el-option label="学术讲座" :value="1" />
                <el-option label="研讨会" :value="2" />
                <el-option label="论坛" :value="3" />
                <el-option label="其他" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="活动标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入活动标题" :disabled="isView" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="主办方" prop="organizer">
              <el-input v-model="form.organizer" placeholder="请输入主办方" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动时间" prop="time">
              <el-date-picker v-model="form.time" type="datetime" placeholder="选择时间" :disabled="isView" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="活动地点" prop="location">
              <el-input v-model="form.location" placeholder="请输入活动地点" :disabled="isView" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="活动描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入活动描述" :disabled="isView" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ isView ? '关闭' : '取消' }}</el-button>
          <el-button type="primary" @click="handleSubmit" v-if="!isView">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AcademicActivity">
import { ref, reactive, getCurrentInstance, toRefs } from 'vue'
import { ElMessage } from 'element-plus'
import { listActivity, submitActivity } from '@/api/academic'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const showSearch = ref(true)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增活动')
const isView = ref(false)
const isMentor = ref(true)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  type: undefined,
  status: undefined
})

const form = reactive({
  id: undefined,
  studentId: undefined,
  type: undefined,
  title: undefined,
  organizer: undefined,
  time: undefined,
  location: undefined,
  description: undefined
})

function getTypeTagType(type) {
  const typeMap = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'info' }
  return typeMap[type] || 'info'
}

function getTypeName(type) {
  const textMap = { 1: '学术讲座', 2: '研讨会', 3: '论坛', 4: '其他' }
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
  listActivity(queryParams).then(res => {
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

function handleAdd() {
  dialogTitle.value = '新增活动'
  isView.value = false
  reset()
  dialogVisible.value = true
}

function handleView(row) {
  dialogTitle.value = '活动详情'
  isView.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

function reset() {
  form.value = {
    id: undefined,
    studentId: undefined,
    type: undefined,
    title: undefined,
    organizer: undefined,
    time: undefined,
    location: undefined,
    description: undefined
  }
}

function cancel() {
  dialogVisible.value = false
  reset()
}

function handleSubmit() {
  proxy.$refs['formRef'].validate(valid => {
    if (valid) {
      submitActivity(form.value).then(res => {
        if (res.code === 200) {
          ElMessage.success('提交成功')
          dialogVisible.value = false
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
  ElMessage.success('审批通过')
  // TODO: 实现审批逻辑
  getList()
}

getList()
</script>
