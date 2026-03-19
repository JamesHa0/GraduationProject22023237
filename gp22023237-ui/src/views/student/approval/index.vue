<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学籍变更审批</span>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="待审批" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
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
        <el-table-column label="变更类型" prop="type" align="center" />
        <el-table-column label="原因" prop="reason" align="center" show-overflow-tooltip />
        <el-table-column label="提交时间" prop="submitTime" align="center" />
        <el-table-column label="操作" align="center">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="handleApprove(row)">审批</el-button>
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

    <!-- 审批对话框 -->
    <el-dialog v-model="dialogVisible" title="审批" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="学生姓名">
          <el-input v-model="form.studentName" disabled />
        </el-form-item>
        <el-form-item label="变更类型">
          <el-input v-model="form.typeName" disabled />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="form.reason" type="textarea" :rows="3" disabled />
        </el-form-item>
        <el-form-item label="审批结果" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">同意</el-radio>
            <el-radio :label="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见" prop="comment">
          <el-input v-model="form.comment" type="textarea" :rows="3" placeholder="请输入审批意见" />
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

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: ''
})

const form = reactive({
  id: '',
  studentName: '',
  typeName: '',
  reason: '',
  status: 1,
  comment: ''
})

const rules = {
  status: [{ required: true, message: '请选择审批结果', trigger: 'change' }],
  comment: [{ required: true, message: '请输入审批意见', trigger: 'blur' }]
}

function handleQuery() {
  loading.value = true
  // TODO: 调用API获取数据
  setTimeout(() => {
    loading.value = false
  }, 500)
}

function resetQuery() {
  queryParams.status = ''
  handleQuery()
}

function handleApprove(row) {
  form.id = row.id
  form.studentName = row.studentName
  form.typeName = row.typeName
  form.reason = row.reason
  form.status = 1
  form.comment = ''
  dialogVisible.value = true
}

function handleSubmit() {
  ElMessage.success('审批成功')
  dialogVisible.value = false
  handleQuery()
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
