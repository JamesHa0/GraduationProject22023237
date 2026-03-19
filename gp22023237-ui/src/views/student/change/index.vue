<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学籍变更申请</span>
          <el-button type="primary" size="small" @click="handleAdd">新增申请</el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="变更类型" prop="type">
          <el-select v-model="queryParams.type" placeholder="请选择" clearable>
            <el-option label="请假" :value="1" />
            <el-option label="休学" :value="2" />
            <el-option label="复学" :value="3" />
            <el-option label="退学" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="dataList" border>
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="学号" prop="studentNo" align="center" />
        <el-table-column label="姓名" prop="studentName" align="center" />
        <el-table-column label="变更类型" prop="type" align="center">
          <template #default="{ row }">
            <el-tag>{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" prop="startDate" align="center" />
        <el-table-column label="结束时间" prop="endDate" align="center" />
        <el-table-column label="导师审批" prop="mentorStatus" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.mentorStatus)">
              {{ getStatusText(row.mentorStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="辅导员审批" prop="counselorStatus" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.counselorStatus)">
              {{ getStatusText(row.counselorStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="handleView(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 申请对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="变更类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择">
            <el-option label="请假" :value="1" />
            <el-option label="休学" :value="2" />
            <el-option label="复学" :value="3" />
            <el-option label="退学" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="4" placeholder="请输入原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增申请')

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  type: ''
})

const form = reactive({
  type: '',
  startDate: '',
  endDate: '',
  reason: ''
})

const rules = {
  type: [{ required: true, message: '请选择变更类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  reason: [{ required: true, message: '请输入原因', trigger: 'blur' }]
}

function getTypeName(type) {
  const map = { 1: '请假', 2: '休学', 3: '复学', 4: '退学' }
  return map[type] || '-'
}

function getStatusText(status) {
  const map = { 0: '待审批', 1: '已通过', 2: '已拒绝' }
  return map[status] || '-'
}

function getStatusType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || ''
}

function handleQuery() {
  loading.value = true
  // TODO: 调用API获取数据
  setTimeout(() => {
    loading.value = false
  }, 500)
}

function resetQuery() {
  queryParams.type = ''
  handleQuery()
}

function handleAdd() {
  dialogTitle.value = '新增申请'
  dialogVisible.value = true
}

function handleSubmit() {
  ElMessage.success('申请提交成功')
  dialogVisible.value = false
}

function handleView(row) {
  console.log('查看', row)
}

function handleSizeChange(val) {
  queryParams.pageSize = val
  handleQuery()
}

function handleCurrentChange(val) {
  queryParams.pageNum = val
  handleQuery()
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
