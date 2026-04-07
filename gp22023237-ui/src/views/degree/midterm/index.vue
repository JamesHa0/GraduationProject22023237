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
import { listMidterm, getMidtermDetail, submitMidterm, approveMidtermMentor, approveMidtermSecretary, approveMidtermDean } from '@/api/degree'

const loading = ref(false)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isView = ref(false)

const queryParams = reactive({
  pageNum:1,
  pageSize: 10,
  studentId: undefined,
  overallStatus: undefined
})

const form = reactive({
  id: '',
  studentId: undefined,
  studentName: '',
  studentNo: '',
  progress: '',
  completedWork: '',
  uncompletedWork: '',
  problems: '',
  solutions: '',
  nextPlan: '',
  draftStatus: '',
  mentorStatus: 0,
  secretaryStatus: 0,
  deanStatus: 0,
  status: 1,
  comment: ''
})

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
  getList()
}

function resetQuery() {
  queryParams.studentNo = ''
  queryParams.status = ''
  handleQuery()
}

function handleView(row) {
  isView.value = true
  getMidtermDetail(row.id).then(res => {
    if (res.code === 200) {
      Object.assign(form, res.data)
      dialogVisible.value = true
    } else {
      ElMessage.error(res.msg || '获取详情失败')
    }
  })
}

function handleApprove(row) {
  isView.value = false
  getMidtermDetail(row.id).then(res => {
    if (res.code === 200) {
      Object.assign(form, res.data)
      form.status = 1
      form.comment = ''
      dialogVisible.value = true
    } else {
      ElMessage.error(res.msg || '获取详情失败')
    }
  })
}

function handleSubmit() {
  if (isView.value) {
    dialogVisible.value = false
    return
  }

  // 提交审批
  const { id, status, comment } = form
  // TODO: 根据当前用户角色选择不同的审批方法
  approveMidtermMentor(id, status, comment).then(res => {
    if (res.code === 200) {
      ElMessage.success('审批成功')
      dialogVisible.value = false
      getList()
    } else {
      ElMessage.error(res.msg || '审批失败')
    }
  }).catch(err => {
    ElMessage.error('审批失败')
    console.error(err)
  })
}

function getList() {
  loading.value = true
  listMidterm(queryParams).then(res => {
    loading.value = false
    if (res.code === 200) {
      const result = res.data
      dataList.value = result.records || []
      total.value = result.total || 0
    } else {
      ElMessage.error(res.msg || '获取数据失败')
    }
  }).catch(err => {
    loading.value = false
    ElMessage.error('获取数据失败')
    console.error(err)
  })
}

function handleSizeChange(val) {
  queryParams.pageSize = val
  handleQuery()
}

function handleCurrentChange(val) {
  queryParams.pageNum = val
  handleQuery()
}

// 初始化加载数据
getList()
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
