<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学位申请</span>
          <el-button type="primary" size="small" @click="handleAdd">新增申请</el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="queryParams.studentNo" placeholder="请输入学号" clearable />
        </el-form-item>
        <el-form-item label="申请状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="待答辩" :value="1" />
            <el-option label="已答辩" :value="2" />
            <el-option label="已授予学位" :value="3" />
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
        <el-table-column label="学位类型" prop="degreeType" align="center">
          <template #default="{ row }">
            <el-tag>{{ row.degreeType === 1 ? '硕士学位' : '博士学位' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="论文题目" prop="thesisTitle" align="center" show-overflow-tooltip />
        <el-table-column label="答辩时间" prop="defenseTime" align="center" />
        <el-table-column label="答辩结果" prop="defenseResult" align="center">
          <template #default="{ row }">
            <el-tag :type="getResultType(row.defenseResult)">
              {{ getResultText(row.defenseResult) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="学位授予状态" prop="degreeGranted" align="center">
          <template #default="{ row }">
            <el-tag :type="row.degreeGranted === 1 ? 'success' : 'info'">
              {{ row.degreeGranted === 1 ? '已授予' : '未授予' }}
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
    <el-dialog v-model="dialogVisible" title="学位申请学位申请信息" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="申请学位类型" prop="degreeType">
          <el-radio-group v-model="form.degreeType">
            <el-radio :label="1">硕士学位</el-radio>
            <el-radio :label="2">博士学位</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="论文题目" prop="thesisTitle">
          <el-input v-model="form.thesisTitle" placeholder="请输入论文题目" />
        </el-form-item>
        <el-form-item label="答辩时间" prop="defenseTime">
          <el-date-picker v-model="form.defenseTime" type="datetime" placeholder="选择时间" />
        </el-form-item>
        <el-form-item label="答辩地点" prop="defenseLocation">
          <el-input v-model="form.defenseLocation" placeholder="请输入地点" />
        </el-form-item>
        <el-form-item label="答辩委员会主席" prop="committeeChair">
          <el-input v-model="form.committeeChair" placeholder="请输入主席姓名" />
        </el-form-item>
        <el-form-item label="答辩委员会成员" prop="committeeMembers">
          <el-input v-model="form.committeeMembers" placeholder="请输入成员姓名，用逗号分隔" />
        </el-form-item>
        <el-form-item label="申请材料" prop="attachmentPath">
          <el-input v-model="form.attachmentPath" placeholder="请输入材料路径" />
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
import { listDegreeApplication, submitDegreeApplication, recordDefenseResult, committeeApprove, grantDegree } from '@/api/degree'

const loading = ref(false)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isView = ref(false)

const queryParams = reactive({
  pageNum:1,
  pageSize: 10,
  studentId: undefined,
  degreeGranted: undefined
})

const form = reactive({
  id: '',
  studentId: undefined,
  degreeType: 1,
  thesisTitle: '',
  defenseTime: '',
  defenseLocation: '',
  committeeChair: '',
  committeeMembers: '',
  defenseResult: 0,
  defenseScore: 0,
  degreeGranted: 0,
  certificateNo: '',
  attachmentPath: ''
})

const rules = {
  thesisTitle: [{ required: true, message: '请输入论文题目', trigger: 'blur' }],
  defenseTime: [{ required: true, message: '请选择答辩时间', trigger: 'change' }]
}

function getResultText(result) {
  const map = { 0: '未答辩', 1: '通过', 2: '不通过' }
  return map[result] || '-'
}

function getResultType(result) {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[result] || ''
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

function handleAdd() {
  isView.value = false
  Object.assign(form, {
    id: '',
    studentId: undefined,
    degreeType: 1,
    thesisTitle: '',
    defenseTime: '',
    defenseLocation: '',
    committeeChair: '',
    committeeMembers: '',
    defenseResult: 0,
    defenseScore: 0,
    degreeGranted: 0,
    certificateNo: '',
    attachmentPath: ''
  })
  dialogVisible.value = true
}

function handleView(row) {
  isView.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

function handleSubmit() {
  if (isView.value) {
    dialogVisible.value = false
    return
  }

  submitDegreeApplication(form).then(res => {
    if (res.code === 200) {
      ElMessage.success('申请提交成功')
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

function getList() {
  loading.value = true
  listDegreeApplication(queryParams).then(res => {
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
