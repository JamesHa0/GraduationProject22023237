<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学位审批</span>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="queryParams.studentNo" placeholder="请输入学号" clearable />
        </el-form-item>
        <el-form-item label="审批状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="待审批" :value="0" />
            <el-option label="已同意" :value="1" />
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
        <el-table-column label="学位类型" prop="degreeType" align="center">
          <template #default="{ row }">
            <el-tag>{{ row.degreeType === 1 ? '硕士学位' : '博士学位' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="论文题目" prop="thesisTitle" align="center" show-overflow-tooltip />
        <el-table-column label="答辩结果" prop="defenseResult" align="center">
          <template #default="{ row }">
            <el-tag :type="getResultType(row.defenseResult)">
              {{ getResultText(row.defenseResult) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="答辩评分" prop="defenseScore" align="center" />
        <el-table-column label="分委会审批状态" prop="committeeStatus" align="center">
          <template #default="{ row }">
            <el-tag :type="getCommitteeStatusType(row.committeeStatus)">
              {{ getCommitteeStatusText(row.committeeStatus) }}
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
            <el-button link size="small" type="primary" @click="handleApprove(row)" v-if="row.committeeStatus === 0">审批</el-button>
            <el-button link size="small" type="success" @click="handleGrant(row)" v-if="row.committeeStatus === 1 && row.degreeGranted === 0">授予学位</el-button>
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
    <el-dialog v-model="dialogVisible" title="学位分委员会审批" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="学生姓名">
          <el-input v-model="form.studentName" disabled />
        </el-form-item>
        <el-form-item label="学位类型">
          <el-input :value="form.degreeType === 1 ? '硕士学位' : '博士学位'" disabled />
        </el-form-item>
        <el-form-item label="论文题目">
          <el-input v-model="form.thesisTitle" disabled />
        </el-form-item>
        <el-form-item label="答辩评分">
          <el-input v-model="form.defenseScore" disabled />
        </el-form-item>
        <el-form-item label="审批结果" prop="committeeStatus">
          <el-radio-group v-model="form.committeeStatus">
            <el-radio :label="1">同意</el-radio>
            <el-radio :label="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见" prop="committeeComment">
          <el-input v-model="form.committeeComment" type="textarea" :rows="4" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 授予学位对话框 -->
    <el-dialog v-model="grantDialogVisible" title="授予学位" width="600px">
      <el-form :model="grantForm" :rules="grantRules" ref="grantFormRef" label-width="120px">
        <el-form-item label="学生姓名">
          <el-input v-model="grantForm.studentName" disabled />
        </el-form-item>
        <el-form-item label="学位类型">
          <el-input :value="grantForm.degreeType === 1 ? '硕士学位' : '博士学位'" disabled />
        </el-form-item>
        <el-form-item label="学位证书编号" prop="certificateNo">
          <el-input v-model="grantForm.certificateNo" placeholder="请输入证书编号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="grantDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGrantSubmit">确定</el-button>
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
const grantDialogVisible = ref(false)

const queryParams = reactive({
  pageNum:1,
  pageSize: 10,
  studentNo: '',
  status: ''
})

const form = reactive({
  id: '',
  studentName: '',
  degreeType: 1,
  thesisTitle: '',
  defenseScore: 0,
  committeeStatus: 1,
  committeeComment: ''
})

const grantForm = reactive({
  id: '',
  studentName: '',
  degreeType: 1,
  certificateNo: ''
})

const rules = {
  committeeStatus: [{ required: true, message: '请选择审批结果', trigger: 'change' }],
  committeeComment: [{ required: true, message: '请输入审批意见', trigger: 'blur' }]
}

const grantRules = {
  certificateNo: [{ required: true, message: '请输入证书编号', trigger: 'blur' }]
}

function getResultText(result) {
  const map = { 0: '未答辩', 1: '通过', 2: '不通过' }
  return map[result] || '-'
}

function getResultType(result) {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[result] || ''
}

function getCommitteeStatusText(status) {
  const map = { 0: '待审批', 1: '已同意', 2: '已拒绝' }
  return map[status] || '-'
}

function getCommitteeStatusType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || ''
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
  form.studentName = row.studentName
  form.degreeType = row.degreeType
  form.thesisTitle = row.thesisTitle
  form.defenseScore = row.defenseScore
  form.committeeStatus = 1
  form.committeeComment = ''
  dialogVisible.value = true
}

function handleSubmit() {
  ElMessage.success('审批成功')
  dialogVisible.value = false
  handleQuery()
}

function handleGrant(row) {
  grantForm.id = row.id
  grantForm.studentName = row.studentName
  grantForm.degreeType = row.degreeType
  grantForm.certificateNo = ''
  grantDialogVisible.value = true
}

function handleGrantSubmit() {
  ElMessage.success('学位授予成功')
  grantDialogVisible.value = false
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
