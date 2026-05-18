<template>
  <div class="app-container">
    <!-- 搜索区 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="答辩时间" prop="defenseTimeStart">
        <el-date-picker v-model="queryParams.defenseTimeStart" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 240px" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="答辩主席" prop="committeeChair">
        <el-input v-model="queryParams.committeeChair" placeholder="请输入" clearable style="width: 120px" />
      </el-form-item>
      <el-form-item label="答辩结果" prop="defenseResult">
        <el-select v-model="queryParams.defenseResult" placeholder="全部" clearable style="width: 120px">
          <el-option label="未进行" :value="0" />
          <el-option label="通过" :value="1" />
          <el-option label="未通过" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="dataList" border stripe>
      <el-table-column type="index" label="序号" width="55" align="center" />
      <el-table-column label="学号" prop="studentNo" align="center" width="120" />
      <el-table-column label="姓名" prop="studentName" align="center" width="100" />
      <el-table-column label="论文题目" prop="thesisTitle" align="center" show-overflow-tooltip min-width="180" />
      <el-table-column label="答辩时间" align="center" width="160">
        <template #default="{ row }">{{ parseDate(row.defenseTime) }}</template>
      </el-table-column>
      <el-table-column label="答辩地点" prop="defenseLocation" align="center" width="120" show-overflow-tooltip />
      <el-table-column label="答辩主席" prop="committeeChair" align="center" width="100" />
      <el-table-column label="答辩结果" prop="defenseResult" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="getDefenseResultType(row.defenseResult)" size="small">{{ getDefenseResultText(row.defenseResult) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="答辩评分" prop="defenseScore" align="center" width="100">
        <template #default="{ row }">{{ row.defenseScore || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-button link size="small" type="warning" @click="handleRecordResult(row)" v-if="canRecordResult(row)">录入结果</el-button>
          <el-button link size="small" type="primary" @click="handleViewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 录入结果对话框 -->
    <el-dialog v-model="recordDialogVisible" title="录入答辩结果" width="600px" append-to-body>
      <el-form :model="recordForm" :rules="recordRules" ref="recordFormRef" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="学生姓名">
              <el-input :model-value="recordForm.studentName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="论文题目">
              <el-input :model-value="recordForm.thesisTitle" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="答辩结果" prop="defenseResult">
          <el-select v-model="recordForm.defenseResult" placeholder="请选择" style="width: 100%">
            <el-option label="通过" :value="1" />
            <el-option label="未通过" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="答辩评分" prop="defenseScore">
          <el-input-number v-model="recordForm.defenseScore" :min="0" :max="100" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="答辩评语" prop="defenseCommitteeComment">
          <el-input v-model="recordForm.defenseCommitteeComment" type="textarea" :rows="3" placeholder="请输入答辩评语" />
        </el-form-item>
        <el-form-item label="问答记录" prop="qaRecord">
          <el-input v-model="recordForm.qaRecord" type="textarea" :rows="3" placeholder="请输入问答记录" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRecordResult" :loading="recordLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="答辩详情" size="50%" append-to-body>
      <template v-if="currentRow">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="学号">{{ currentRow.studentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentRow.studentName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学位类型">
            <el-tag size="small">{{ currentRow.degreeType === 1 ? '硕士学位' : '博士学位' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="导师">{{ currentRow.mentorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="论文题目" :span="2">{{ currentRow.thesisTitle || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">答辩信息</el-divider>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="答辩时间">{{ parseDate(currentRow.defenseTime) }}</el-descriptions-item>
          <el-descriptions-item label="答辩地点">{{ currentRow.defenseLocation || '-' }}</el-descriptions-item>
          <el-descriptions-item label="答辩主席">{{ currentRow.committeeChair || '-' }}</el-descriptions-item>
          <el-descriptions-item label="答辩委员">{{ currentRow.committeeMembers || '-' }}</el-descriptions-item>
          <el-descriptions-item label="答辩结果">
            <el-tag :type="getDefenseResultType(currentRow.defenseResult)" size="small">{{ getDefenseResultText(currentRow.defenseResult) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="答辩评分">{{ currentRow.defenseScore || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">答辩评语与记录</el-divider>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="答辩评语">
            <div style="white-space: pre-wrap">{{ currentRow.defenseCommitteeComment || '暂无' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="问答记录">
            <div style="white-space: pre-wrap">{{ currentRow.qaRecord || '暂无' }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>
  </div>
</template>

<script setup name="DefenseManage">
import { ref, reactive, toRefs, onMounted } from 'vue'
import { listDefenseRecord, updateDefenseResult, getDefenseDetail } from '@/api/degree/defenseManage'
import { parseDate } from '@/composables/useDegreeStatus'
import { getCurrentUserRoleId, ROLE } from '@/composables/useDegreeApproval'

const { proxy } = getCurrentInstance()

// ==================== 状态 ====================
const loading = ref(false)
const showSearch = ref(true)
const dataList = ref([])
const total = ref(0)
const detailVisible = ref(false)
const recordDialogVisible = ref(false)
const recordLoading = ref(false)
const currentRow = ref(null)
const recordFormRef = ref(null)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    defenseTimeStart: undefined,
    committeeChair: undefined,
    defenseResult: undefined
  },
  recordForm: {
    id: undefined,
    studentName: '',
    thesisTitle: '',
    defenseResult: undefined,
    defenseScore: 0,
    defenseCommitteeComment: '',
    qaRecord: ''
  },
  recordRules: {
    defenseResult: [{ required: true, message: '请选择答辩结果', trigger: 'change' }]
  }
})

const { queryParams, recordForm, recordRules } = toRefs(data)

// ==================== 权限 ====================
function canRecordResult(row) {
  const roleId = getCurrentUserRoleId()
  const canRecord = [ROLE.SUPER_ADMIN, ROLE.DEAN, ROLE.SECRETARY, ROLE.DEPT_SECRETARY].includes(roleId)
  return canRecord && row.defenseResult === 0
}

// ==================== 列表 ====================
function getList() {
  loading.value = true
  const params = { ...queryParams.value }
  // 处理日期范围
  if (params.defenseTimeStart && params.defenseTimeStart.length === 2) {
    params.defenseTimeStart = params.defenseTimeStart[0]
    params.defenseTimeEnd = params.defenseTimeStart[1]
  } else {
    delete params.defenseTimeStart
  }
  listDefenseRecord(params).then(res => {
    loading.value = false
    dataList.value = res.data || []
    total.value = res.pagination?.total || dataList.value.length
  }).catch(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: 10, defenseTimeStart: undefined, committeeChair: undefined, defenseResult: undefined }
  handleQuery()
}

// ==================== 录入结果 ====================
function handleRecordResult(row) {
  recordForm.value = {
    id: row.id,
    studentName: row.studentName,
    thesisTitle: row.thesisTitle,
    defenseResult: undefined,
    defenseScore: 0,
    defenseCommitteeComment: '',
    qaRecord: ''
  }
  recordDialogVisible.value = true
}

function submitRecordResult() {
  recordFormRef.value.validate(valid => {
    if (valid) {
      recordLoading.value = true
      updateDefenseResult({
        id: recordForm.value.id,
        defenseResult: recordForm.value.defenseResult,
        defenseScore: recordForm.value.defenseScore,
        defenseCommitteeComment: recordForm.value.defenseCommitteeComment,
        qaRecord: recordForm.value.qaRecord
      }).then(() => {
        recordLoading.value = false
        proxy.$modal.msgSuccess('录入成功')
        recordDialogVisible.value = false
        getList()
      }).catch(() => {
        recordLoading.value = false
        proxy.$modal.msgError('录入失败')
      })
    }
  })
}

// ==================== 详情 ====================
function handleViewDetail(row) {
  currentRow.value = row
  detailVisible.value = true
}

// ==================== 工具函数 ====================
function getDefenseResultText(val) {
  const map = { 0: '未进行', 1: '通过', 2: '未通过' }
  return map[val] ?? '未进行'
}
function getDefenseResultType(val) {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[val] ?? 'info'
}

// ==================== 初始化 ====================
onMounted(() => {
  getList()
})
</script>

<style scoped>
</style>
