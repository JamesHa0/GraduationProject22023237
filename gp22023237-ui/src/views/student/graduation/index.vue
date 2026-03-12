<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="学生ID" prop="studentId">
        <el-input v-model="queryParams.studentId" placeholder="请输入学生ID" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="审核结果" prop="overallResult">
        <el-select v-model="queryParams.overallResult" placeholder="审核结果" clearable style="width: 150px">
          <el-option label="不通过" value="0" />
          <el-option label="通过" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Promotion" @click="handleAutoReview">自动审核</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleManualReview">手动审核</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="graduationList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="学生ID" align="center" prop="studentId" width="100" />
      <el-table-column label="学分审核" align="center" prop="creditCheck" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.creditCheck === 1 ? 'success' : 'danger'">
            {{ scope.row.creditCheck === 1 ? '通过' : '不通过' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="论文审核" align="center" prop="thesisCheck" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.thesisCheck === 1 ? 'success' : 'danger'">
            {{ scope.row.thesisCheck === 1 ? '通过' : '不通过' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="实践审核" align="center" prop="practiceCheck" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.practiceCheck === 1 ? 'success' : 'danger'">
            {{ scope.row.practiceCheck === 1 ? '通过' : '不通过' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="综合结果" align="center" prop="overallResult" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.overallResult === 1 ? 'success' : 'danger'">
            {{ scope.row.overallResult === 1 ? '通过' : '不通过' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核人ID" align="center" prop="reviewerId" width="120" />
      <el-table-column label="审核时间" align="center" prop="reviewTime" width="160" />
      <el-table-column label="备注" align="center" prop="comment" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="120">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
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

    <!-- 自动审核对话框 -->
    <el-dialog title="自动审核" v-model="autoReviewOpen" width="500px" append-to-body>
      <el-form ref="autoReviewFormRef" :model="autoReviewForm" :rules="autoReviewRules" label-width="100px">
        <el-form-item label="学生ID" prop="studentId">
          <el-input v-model="autoReviewForm.studentId" placeholder="请输入学生ID" />
        </el-form-item>
        <el-form-item label="审核人ID" prop="reviewerId">
          <el-input v-model="autoReviewForm.reviewerId" placeholder="请输入审核人ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAutoReview">确 定</el-button>
          <el-button @click="autoReviewOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 手动审核对话框 -->
    <el-dialog title="手动审核" v-model="manualReviewOpen" width="600px" append-to-body>
      <el-form ref="manualReviewFormRef" :model="manualReviewForm" :rules="manualReviewRules" label-width="100px">
        <el-form-item label="学分审核">
          <el-radio-group v-model="manualReviewForm.creditCheck">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="0">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="论文审核">
          <el-radio-group v-model="manualReviewForm.thesisCheck">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="0">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="实践审核">
          <el-radio-group v-model="manualReviewForm.practiceCheck">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="0">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="comment">
          <el-input v-model="manualReviewForm.comment" type="textarea" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="审核人ID" prop="reviewerId">
          <el-input v-model="manualReviewForm.reviewerId" placeholder="请输入审核人ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitManualReview">确 定</el-button>
          <el-button @click="manualReviewOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Graduation">
import { listGraduation, autoReview, manualReview } from "@/api/student/graduation"

const { proxy } = getCurrentInstance()

const graduationList = ref([])
const autoReviewOpen = ref(false)
const manualReviewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  autoReviewForm: {
    studentId: null,
    reviewerId: null
  },
  autoReviewRules: {
    studentId: [{ required: true, message: "学生ID不能为空", trigger: "blur" }],
    reviewerId: [{ required: true, message: "审核人ID不能为空", trigger: "blur" }]
  },
  manualReviewForm: {
    id: null,
    creditCheck: 1,
    thesisCheck: 1,
    practiceCheck: 1,
    comment: null,
    reviewerId: null
  },
  manualReviewRules: {
    reviewerId: [{ required: true, message: "审核人ID不能为空", trigger: "blur" }]
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentId: null,
    overallResult: null
  }
})

const { autoReviewForm, autoReviewRules, manualReviewForm, manualReviewRules, queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listGraduation(queryParams.value).then(response => {
    graduationList.value = response.data.records
    total.value = response.data.total
    loading.value = false
  })
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

function handleAutoReview() {
  proxy.resetForm("autoReviewFormRef")
  autoReviewOpen.value = true
}

function submitAutoReview() {
  proxy.$refs["autoReviewFormRef"].validate(valid => {
    if (valid) {
      autoReview(autoReviewForm.value.studentId, autoReviewForm.value.reviewerId).then(response => {
        proxy.$modal.msgSuccess("自动审核成功")
        autoReviewOpen.value = false
        getList()
      })
    }
  })
}

function handleManualReview() {
  if (single.value) {
    proxy.$modal.msgWarning("请选择一条记录")
    return
  }
  const row = graduationList.value.find(item => item.id === ids.value[0])
  manualReviewForm.value.id = row.id
  manualReviewForm.value.creditCheck = row.creditCheck
  manualReviewForm.value.thesisCheck = row.thesisCheck
  manualReviewForm.value.practiceCheck = row.practiceCheck
  manualReviewForm.value.comment = row.comment
  manualReviewForm.value.reviewerId = null
  manualReviewOpen.value = true
}

function submitManualReview() {
  proxy.$refs["manualReviewFormRef"].validate(valid => {
    if (valid) {
      manualReview(manualReviewForm.value).then(response => {
        proxy.$modal.msgSuccess("手动审核成功")
        manualReviewOpen.value = false
        getList()
      })
    }
  })
}

function handleView(row) {
  proxy.$modal.msgInfo("详情: " + JSON.stringify(row))
}

getList()
</script>
