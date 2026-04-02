<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="学号" prop="studentNo">
        <el-input v-model="queryParams.studentNo" placeholder="请输入学号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="姓名" prop="studentName">
        <el-input v-model="queryParams.studentName" placeholder="请输入姓名" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="审核状态" prop="auditStatus">
        <el-select v-model="queryParams.auditStatus" placeholder="请选择" clearable style="width: 200px">
          <el-option label="待审核" :value="0" />
          <el-option label="审核通过" :value="1" />
          <el-option label="审核不通过" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="auditList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="学号" align="center" prop="studentNo" width="120" />
      <el-table-column label="姓名" align="center" prop="studentName" :show-overflow-tooltip="true" />
      <el-table-column label="学分审核" align="center" prop="creditCheck" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.creditCheck === 1 ? 'success' : 'danger'">
            {{ scope.row.creditCheck === 1 ? '通过' : '未通过' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="论文审核" align="center" prop="thesisCheck" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.thesisCheck === 1 ? 'success' : 'danger'">
            {{ scope.row.thesisCheck === 1 ? '通过' : '未通过' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="成果审核" align="center" prop="achievementCheck" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.achievementCheck === 1 ? 'success' : 'danger'">
            {{ scope.row.achievementCheck === 1 ? '通过' : '未通过' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="实践审核" align="center" prop="practiceCheck" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.practiceCheck === 1 ? 'success' : 'warning'">
            {{ scope.row.practiceCheck === 1 ? '通过' : '未完成' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" align="center" prop="auditStatus" width="100">
        <template #default="scope">
          <el-tag :type="getAuditStatusType(scope.row.auditStatus)">
            {{ getAuditStatusText(scope.row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="申请时间" align="center" prop="applyTime" width="170" />
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)"></el-button>
          <el-button link type="primary" icon="Edit" @click="handleAudit(scope.row)"></el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 审核对话框 -->
    <el-dialog :title="dialogTitle" v-model="open" width="700px" append-to-body>
      <el-form :model="form" :rules="rules" ref="auditRef" label-width="120px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="学生姓名">
              <el-input v-model="form.studentName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="学分审核">
              <el-radio-group v-model="form.creditCheck">
                <el-radio :label="1">通过</el-radio>
                <el-radio :label="0">不通过</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="论文审核">
              <el-radio-group v-model="form.thesisCheck">
                <el-radio :label="1">通过</el-radio>
                <el-radio :label="0">不通过</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="成果审核">
              <el-radio-group v-model="form.achievementCheck">
                <el-radio :label="1">通过</el-radio>
                <el-radio :label="0">不通过</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实践审核">
              <el-radio-group v-model="form.practiceCheck">
                <el-radio :label="1">通过</el-radio>
                <el-radio :label="0">不通过</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="审核状态">
              <el-radio-group v-model="form.auditStatus">
                <el-radio :label="0">待审核</el-radio>
                <el-radio :label="1">审核通过</el-radio>
                <el-radio :label="2">审核不通过</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="审核意见" prop="comment">
              <el-input v-model="form.comment" type="textarea" :rows="4" placeholder="请输入审核意见" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="GraduationAudit">
import { listGraduationAudit, manualAuditGraduation, getGraduationStats } from '@/api/student/status'
import { getCurrentInstance, ref, reactive, toRefs } from "vue"
import useUserStore from "@/store/modules/user"

const { proxy } = getCurrentInstance()
const userStore = useUserStore()

const auditList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const dialogTitle = ref("")

const columns = ref([
  { key: 0, label: `学号`, visible: true },
  { key: 1, label: `姓名`, visible: true },
  { key: 2, label: `学分审核`, visible: true },
  { key: 3, label: `论文审核`, visible: true },
  { key: 4, label: `成果审核`, visible: true },
  { key: 5, label: `实践审核`, visible: true },
  { key: 6, label: `审核状态`, visible: true },
  { key: 7, label: `申请时间`, visible: true }
])

const data = reactive({
  form: {
    id: '',
    studentId: undefined,
    studentNo: '',
    studentName: '',
    creditCheck: 0,
    thesisCheck: 0,
    achievementCheck: 0,
    practiceCheck: 0,
    auditStatus: 0,
    comment: ''
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentId: undefined,
    auditStatus: undefined
  },
  rules: {
    creditCheck: [{ required: true, message: '请选择学分审核结果', trigger: 'change' }],
    thesisCheck: [{ required: true, message: '请选择论文审核结果', trigger: 'change' }],
    achievementCheck: [{ required: true, message: '请选择成果审核结果', trigger: 'change' }],
    practiceCheck: [{ required: true, message: '请选择实践审核结果', trigger: 'change' }],
    auditStatus: [{ required: true, message: '请选择审核状态', trigger: 'change' }],
    comment: [{ required: true, message: '请输入审核意见', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getAuditStatusText(status) {
  const map = { 0: '待审核', 1: '审核通过', 2: '审核不通过' }
  return map[status] || '未知'
}

function getAuditStatusType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

function getList() {
  loading.value = true
  listGraduationAudit(queryParams.value).then(res => {
    loading.value = false
    auditList.value = res.data.records || res.data || []
    total.value = res.data.total || auditList.value.length
  }).catch(err => {
    loading.value = false
    console.error(err)
    proxy.$modal.msgError('获取数据失败')
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    id: '',
    studentId: undefined,
    studentNo: '',
    studentName: '',
    creditCheck: 0,
    thesisCheck: 0,
    achievementCheck: 0,
    practiceCheck: 0,
    auditStatus: 0,
    comment: ''
  }
  proxy.resetForm('auditRef')
}

function cancel() {
  open.value = false
  reset()
}

function handleView(row) {
  proxy.$modal.detail(row)
}

function handleAudit(row) {
  reset()
  form.value = {
    id: row.id,
    studentId: row.studentId,
    studentNo: row.studentNo,
    studentName: row.studentName,
    creditCheck: row.creditCheck || 0,
    thesisCheck: row.thesisCheck || 0,
    achievementCheck: row.achievementCheck || 0,
    practiceCheck: row.practiceCheck || 0,
    auditStatus: row.auditStatus || 0,
    comment: row.comment || ''
  }
  dialogTitle.value = '毕业资格审核'
  open.value = true
}

function submitForm() {
  proxy.$refs['auditRef'].validate(valid => {
    if (valid) {
      const { id, auditStatus, comment } = form.value
      const auditorId = userStore.userId || 1
      const auditorName = userStore.name || '当前用户'

      manualAuditGraduation(id, auditStatus, comment, auditorId, auditorName).then(res => {
        if (res.code === 200) {
          proxy.$modal.msgSuccess('审核成功')
          open.value = false
          getList()
        } else {
          proxy.$modal.msgError(res.msg || '审核失败')
        }
      }).catch(err => {
        console.error(err)
        proxy.$modal.msgError('审核失败')
      })
    }
  })
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