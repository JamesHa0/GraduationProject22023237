<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>论文中期检查</span>
          <el-button type="primary" size="small" @click="handleAdd">新增检查</el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="queryParams.studentNo" placeholder="请输入学号" clearable />
        </el-form-item>
        <el-form-item label="审核状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="待审核" :value="1" />
            <el-option label="导师已通过" :value="2" />
            <el-option label="秘书已通过" :value="3" />
            <el-option label="院长已通过" :value="4" />
            <el-option label="审核不通过" :value="5" />
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
        <el-table-column label="进展情况" prop="progress" align="center" show-overflow-tooltip />
        <el-table-column label="导师审批" prop="mentorStatus" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.mentorStatus)">
              {{ getStatusText() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="秘书审批" prop="secretaryStatus" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.secretaryStatus)">
              {{ getStatusText() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="院长审批" prop="deanStatus" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.deanStatus)">
              {{ getStatusText() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="handleView(row)">详情</el-button>
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
    <el-dialog v-model="dialogVisible" title="中期检查审批" width="700px">
      <el-form :model="form" ref="formRef" label-width="100px">
        <el-form-item label="进展情况">
          <el-input v-model="form.progress" type="textarea" :rows="4" disabled />
        </el-form-item>
        <el-form-item label="下一步计划">
          <el-input v-model="form.nextPlan" type="textarea" :rows="3" disabled />
        </el-form-item>
        <el-form-item label="审批结果" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">同意</el-radio>
            <el-radio :label="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见" prop="comment">
          <el-input v-model="form.comment" type="textarea" :rows="4" placeholder="请输入审批意见" />
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
  pageNum:1,
  pageSize: 10,
  studentNo: '',
  status: ''
})

const form = reactive({
  id: '',
  progress: '',
  nextPlan: '',
  status: 1,
  comment: ''
})

function getStatusText() {
  return '待审批'
}

function getStatusType(status) {
  return 'warning'
}

function handleQuery() {
  loading.value = true
  setTimeout(() => { loading.value = false }, 500)
}

function resetQuery() {
  queryParams.studentNo = ''
  queryParams.status = ''
  handleQuery()
}

function handleView(row) {
  console.log('查看详情', row)
}

function handleApprove(row) {
  form.id = row.id
  form.progress = row.progress || ''
  form.nextPlan = row.nextPlan || ''
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
