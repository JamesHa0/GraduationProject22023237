<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>毕业资格审核</span>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="queryParams.studentNo" placeholder="请输入学号" clearable />
        </el-form-item>
        <el-form-item label="审核状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="待审核" :value="0" />
            <el-option label="审核通过" :value="1" />
            <el-option label="审核不通过" :value="2" />
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
        <el-table-column label="学分审核" prop="creditCheck" align="center">
          <template #default="{ row }">
            <el-tag :type="row.creditCheck === 1 ? 'success' : 'danger'">
              {{ row.creditCheck === 1 ? '通过' : '未通过' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="论文审核" prop="thesisCheck" align="center">
          <template #default="{ row }">
            <el-tag :type="row.thesisCheck === 1 ? 'success' : 'danger'">
              {{ row.thesisCheck === 1 ? '通过' : '未通过' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="实践审核" prop="practiceCheck" align="center">
          <template #default="{ row }">
            <el-tag :type="row.practiceCheck === 1 ? 'success' : 'warning'">
              {{ row.practiceCheck === 1 ? '通过' : '未完成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核意见" prop="comment" align="center" show-overflow-tooltip />
        <el-table-column label="操作" align="center">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="handleAudit(row)">审核</el-button>
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

    <!-- 审核对话框 -->
    <el-dialog v-model="dialogVisible" title="毕业资格审核" width="600px">
      <el-form :model="form" ref="formRef" label-width="120px">
        <el-form-item label="学生姓名">
          <el-input v-model="form.studentName" disabled />
        </el-form-item>
        <el-form-item label="学分审核">
          <el-radio-group v-model="form.creditCheck">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="0">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="论文审核">
          <el-radio-group v-model="form.thesisCheck">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="0">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="实践审核">
          <el-radio-group v-model="form.practiceCheck">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="0">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="form.comment" type="textarea" :rows="4" placeholder="请输入审核意见" />
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
  studentNo: '',
  status: ''
})

const form = reactive({
  id: '',
  studentName: '',
  creditCheck: 0,
  thesisCheck: 0,
  practiceCheck: 0,
  comment: ''
})

function handleQuery() {
  loading.value = true
  // TODO: 调用API获取数据
  setTimeout(() => {
    loading.value = false
  }, 500)
}

function resetQuery() {
  queryParams.studentNo = ''
  queryParams.status = ''
  handleQuery()
}

function handleAudit(row) {
  form.id = row.id
  form.studentName = row.studentName
  form.creditCheck = row.creditCheck
  form.thesisCheck = row.thesisCheck
  form.practiceCheck = row.practiceCheck
  form.comment = row.comment || ''
  dialogVisible.value = true
}

function handleSubmit() {
  ElMessage.success('审核成功')
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
