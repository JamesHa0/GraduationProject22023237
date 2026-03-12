<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="学生ID" prop="studentId">
        <el-input v-model="queryParams.studentId" placeholder="请输入学生ID" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="申请状态" clearable style="width: 150px">
          <el-option label="待审核" value="0" />
          <el-option label="审核中" value="1" />
          <el-option label="已通过" value="2" />
          <el-option label="已拒绝" value="3" />
          <el-option label="已取消" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="异动类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="异动类型" clearable style="width: 150px">
          <el-option label="休学" value="1" />
          <el-option label="复学" value="2" />
          <el-option label="退学" value="3" />
          <el-option label="转专业" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增申请</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="single" @click="handleDelete">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
    </el-form>

    <el-table v-loading="loading" :data="statusChangeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="学生ID" align="center" prop="studentId" width="100" />
      <el-table-column label="异动类型" align="center" prop="type" width="100">
        <template #default="scope">
          <el-tag :type="getTypeTagType(scope.row.type)">{{ getTypeText(scope.row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="申请原因" align="center" prop="reason" :show-overflow-tooltip="true" />
      <el-table-column label="开始日期" align="center" prop="startDate" width="120" />
      <el-table-column label="结束日期" align="center" prop="endDate" width="120" />
      <el-table-column label="导师审核" align="center" prop="tutorApproval" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.tutorApproval === 1 ? 'success' : (scope.row.tutorApproval === 2 ? 'danger' : 'info')">
            {{ getApprovalText(scope.row.tutorApproval) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="教学秘书审核" align="center" prop="counselorApproval" width="120">
        <template #default="scope">
          <el-tag :type="scope.row.counselorApproval === 1 ? 'success' : (scope.row.counselorApproval === 2 ? 'danger' : 'info')">
            {{ getApprovalApprovalText(scope.row.counselorApproval) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="success" icon="Select" v-if="scope.row.status === 0" @click="handleTutorApproval(scope.row)">导师审核</el-button>
          <el-button link type="warning" icon="Select" v-if="scope.row.tutorApproval === 1 && scope.row.counselorApproval === 0" @click="handleCounselorApproval(scope.row)">秘书审核</el-button>
          <el-button link type="danger" icon="Close" v-if="scope.row.status === 0" @click="handleCancel(scope.row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="学生ID" prop="studentId">
          <el-input v-model="form.studentId" placeholder="请输入学生ID" />
        </el-form-item>
        <el-form-item label="异动类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择异动类型" style="width: 100%">
            <el-option label="休学" value="1" />
            <el-option label="复学" value="2" />
            <el-option label="退学" value="3" />
            <el-option label="转专业" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" placeholder="请输入申请原因" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" placeholder="选择开始日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate" v-if="form.type === 1">
          <el-date-picker v-model="form.endDate" type="date" placeholder="选择结束日期" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog :title="approvalTitle" v-model="approvalOpen" width="400px" append-to-body>
      <el-form ref="approvalFormRef" :model="approvalForm" label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="approvalForm.approval">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核人ID" prop="approverId">
          <el-input v-model="approvalForm.approverId" placeholder="请输入审核人ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitApproval">确 定</el-button>
          <el-button @click="approvalOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="StatusChange">
import { listStatusChange, submitApplication, tutorApproval, counselorApproval, cancelApplication } from "@/api/student/statusChange"

const { proxy } = getCurrentInstance()

const statusChangeList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const approvalOpen = ref(false)
const approvalTitle = ref("")
const approvalType = ref("") // 'tutor' or 'counselor'

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentId: null,
    status: null,
    type: null
  },
  rules: {
    studentId: [{ required: true, message: "学生ID不能为空", trigger: "blur" }],
    type: [{ required: true, message: "异动类型不能为空", trigger: "change" }],
    reason: [{ required: true, message: "申请原因不能为空", trigger: "blur" }],
    startDate: [{ required: true, message: "开始日期不能为空", trigger: "change" }]
  },
  approvalForm: {
    id: null,
    approval: 1,
    approverId: null
  }
})

const { queryParams, form, rules, approvalForm } = toRefs(data)

function getList() {
  loading.value = true
  listStatusChange(queryParams.value).then(response => {
    statusChangeList.value = response.data.records
    total.value = response.data.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    studentId: null,
    type: null,
    reason: null,
    startDate: null,
    endDate: null
  }
  proxy.resetForm("formRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增学籍异动申请"
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      submitApplication(form.value).then(response => {
        proxy.$modal.msgSuccess("提交成功")
        open.value = false
        getList()
      })
    }
  })
}

function handleView(row) {
  proxy.$modal.msgInfo("详情: " + JSON.stringify(row))
}

function handleTutorApproval(row) {
  approvalType.value = "tutor"
  approvalTitle.value = "导师审核"
  approvalForm.value.id = row.id
  approvalForm.value.approval = 1
  approvalForm.value.approverId = null
  approvalOpen.value = true
}

function handleCounselorApproval(row) {
  approvalType.value = "counselor"
  approvalTitle.value = "教学秘书审核"
  approvalForm.value.id = row.id
  approvalForm.value.approval = 1
  approvalForm.value.approverId = null
  approvalOpen.value = true
}

function submitApproval() {
  if (!approvalForm.value.approverId) {
    proxy.$modal.msgError("请输入审核人ID")
    return
  }
  if (approvalType.value === "tutor") {
    tutorApproval(approvalForm.value.id, approvalForm.value.approval, approvalForm.value.approverId).then(response => {
      proxy.$modal.msgSuccess("审核成功")
      approvalOpen.value = false
      getList()
    })
  } else {
    counselorApproval(approvalForm.value.id, approvalForm.value.approval, approvalForm.value.approverId).then(response => {
      proxy.$modal.msgSuccess("审核成功")
      approvalOpen.value = false
      getList()
    })
  }
}

function handleCancel(row) {
  proxy.$modal.confirm('是否确认取消申请ID为"' + row.id + '"的申请？').then(function() {
    return cancelApplication(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("取消成功")
  }).catch(() => {})
}

function handleDelete(row) {
  proxy.$modal.msgWarning("删除功能暂未实现")
}

function getTypeText(type) {
  const typeMap = { 1: '休学', 2: '复学', 3: '退学', 4: '转专业' }
  return typeMap[type] || '未知'
}

function getTypeTagType(type) {
  const typeMap = { 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' }
  return typeMap[type] || ''
}

function getStatusText(status) {
  const statusMap = { 0: '待审核', 1: '审核中', 2: '已通过', 3: '已拒绝', 4: '已取消' }
  return statusMap[status] || '未知'
}

function getStatusTagType(status) {
  const statusMap = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' }
  return statusMap[status] || ''
}

function getApprovalText(approval) {
  const approvalMap = { 0: '未审核', 1: '已通过', 2: '已拒绝' }
  return approvalMap[approval] || '未知'
}

function getApprovalApprovalText(approval) {
  const approvalMap = { 0: '未审核', 1: '已通过', 2: '已拒绝' }
  return approvalMap[approval] || '未知'
}

getList()
</script>
