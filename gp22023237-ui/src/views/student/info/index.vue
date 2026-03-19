<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生信息管理</span>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" :inline="true" ref="queryForm">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="queryParams.studentNo" placeholder="请输入学号" clearable />
        </el-form-item>
        <el-form-item label="姓名" prop="studentName">
          <el-input v-model="queryParams.studentName" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="dataList" border>
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="学号" prop="studentNo" align="center" />
        <el-table-column label="姓名" prop="studentName" align="center" />
        <el-table-column label="学院" prop="department" align="center" />
        <el-table-column label="专业" prop="major" align="center" />
        <el-table-column label="入学年份" prop="admissionYear" align="center" />
        <el-table-column label="毕业年份" prop="graduationYear" align="center" />
        <el-table-column label="研究方向" prop="researchDirection" align="center" />
        <el-table-column label="状态" prop="status" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="handleView(row)">详情</el-button>
            <el-button link size="small" type="primary" @click="handleEdit(row)">修改</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const loading = ref(false)
const dataList = ref([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  studentNo: '',
  studentName: ''
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
  queryParams.studentName = ''
  handleQuery()
}

function handleView(row) {
  console.log('查看', row)
}

function handleEdit(row) {
  console.log('修改', row)
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
