<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="mb16">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-value">{{ stats.total || 0 }}</div>
            <div class="stat-label">审核总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-passed">
          <div class="stat-item">
            <div class="stat-value">{{ stats.passed || 0 }}</div>
            <div class="stat-label">审核通过</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-auditing">
          <div class="stat-item">
            <div class="stat-value">{{ stats.auditing || 0 }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-failed">
          <div class="stat-item">
            <div class="stat-value">{{ stats.failed || 0 }}</div>
            <div class="stat-label">审核不通过</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="学号" prop="studentNo">
        <el-input v-model="queryParams.studentNo" placeholder="请输入学号" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="姓名" prop="studentName">
        <el-input v-model="queryParams.studentName" placeholder="请输入姓名" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="审核状态" prop="auditStatus">
        <el-select v-model="queryParams.auditStatus" placeholder="全部" clearable style="width: 160px">
          <el-option label="待审核" :value="0" />
          <el-option label="审核通过" :value="1" />
          <el-option label="审核不通过" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="入学年份" prop="cohortYear">
        <el-input v-model="queryParams.cohortYear" placeholder="如2023" clearable style="width: 120px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="Check" @click="handleBatchAudit" :loading="batchLoading">批量自动审核</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="auditList" @selection-change="handleSelectionChange" stripe border>
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="学号" align="center" prop="studentNo" width="120" />
      <el-table-column label="姓名" align="center" prop="studentName" width="90" />
      <el-table-column label="院系" align="center" prop="department" width="140" :show-overflow-tooltip="true" />
      <el-table-column label="专业" align="center" prop="major" width="140" :show-overflow-tooltip="true" />
      <el-table-column label="已修学分" align="center" width="90">
        <template #default="scope">
          <span>{{ scope.row.totalCredits || 0 }} / {{ scope.row.requiredCredits || 30 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="学分审核" align="center" width="90" v-if="columns[0].visible">
        <template #default="scope">
          <el-tag :type="getCheckType(scope.row.creditCheck)" size="small">
            {{ getCheckText(scope.row.creditCheck) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="论文审核" align="center" width="90" v-if="columns[1].visible">
        <template #default="scope">
          <el-tag :type="getCheckType(scope.row.thesisCheck)" size="small">
            {{ getCheckText(scope.row.thesisCheck) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="实践审核" align="center" width="90" v-if="columns[2].visible">
        <template #default="scope">
          <el-tag :type="getCheckType(scope.row.practiceCheck)" size="small">
            {{ getCheckText(scope.row.practiceCheck) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" align="center" width="110" v-if="columns[3].visible">
        <template #default="scope">
          <el-tag :type="getAuditStatusType(scope.row.auditStatus)">
            {{ getAuditStatusText(scope.row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核人" align="center" prop="auditorName" width="90" />
      <el-table-column label="审核时间" align="center" prop="auditTime" width="160" />
      <el-table-column label="操作" align="center" width="220" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="success" icon="Check" @click="handleAutoAudit(scope.row)">自动审核</el-button>
          <el-button link type="warning" icon="Edit" @click="handleManualAudit(scope.row)">人工审核</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 人工审核对话框 -->
    <el-dialog title="人工审核" v-model="auditOpen" width="600px" append-to-body>
      <el-form :model="auditForm" :rules="auditRules" ref="auditRef" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="学生姓名">
              <el-input v-model="auditForm.studentName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学号">
              <el-input v-model="auditForm.studentNo" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="已修学分">
              <el-input :model-value="auditForm.totalCredits + ' / ' + (auditForm.requiredCredits || 30)" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">各审核项状态</el-divider>
        <el-row>
          <el-col :span="8">
            <el-form-item label="学分审核">
              <el-tag :type="getCheckType(auditForm.creditCheck)">{{ getCheckText(auditForm.creditCheck) }}</el-tag>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="论文审核">
              <el-tag :type="getCheckType(auditForm.thesisCheck)">{{ getCheckText(auditForm.thesisCheck) }}</el-tag>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="实践审核">
              <el-tag :type="getCheckType(auditForm.practiceCheck)">{{ getCheckText(auditForm.practiceCheck) }}</el-tag>
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">审核判定</el-divider>
        <el-row>
          <el-col :span="24">
            <el-form-item label="审核结果" prop="status">
              <el-radio-group v-model="auditForm.status">
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
              <el-input v-model="auditForm.comment" type="textarea" :rows="4" placeholder="请输入审核意见" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAudit">确 定</el-button>
          <el-button @click="auditOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 审核详情抽屉 -->
    <el-drawer title="毕业审核详情" v-model="detailOpen" size="500px">
      <template v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学号">{{ detailData.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ detailData.studentName }}</el-descriptions-item>
          <el-descriptions-item label="院系">{{ detailData.department }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ detailData.major }}</el-descriptions-item>
          <el-descriptions-item label="已修学分">{{ detailData.totalCredits || 0 }}</el-descriptions-item>
          <el-descriptions-item label="要求学分">{{ detailData.requiredCredits || 30 }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">审核项详情</el-divider>
        <el-row :gutter="16" class="check-row">
          <el-col :span="8">
            <el-card shadow="never" class="check-card" :class="{ 'check-pass': detailData.creditCheck === 1, 'check-fail': detailData.creditCheck === 2 }">
              <div class="check-title">学分审核</div>
              <div class="check-result">
                <el-tag :type="getCheckType(detailData.creditCheck)" size="large">
                  {{ getCheckText(detailData.creditCheck) }}
                </el-tag>
              </div>
              <div class="check-detail">
                已修 {{ detailData.totalCredits || 0 }} 学分 / 要求 {{ detailData.requiredCredits || 30 }} 学分
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never" class="check-card" :class="{ 'check-pass': detailData.thesisCheck === 1, 'check-fail': detailData.thesisCheck === 2 }">
              <div class="check-title">论文审核</div>
              <div class="check-result">
                <el-tag :type="getCheckType(detailData.thesisCheck)" size="large">
                  {{ getCheckText(detailData.thesisCheck) }}
                </el-tag>
              </div>
              <div class="check-detail">
                毕业论文环节是否通过
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never" class="check-card" :class="{ 'check-pass': detailData.practiceCheck === 1, 'check-fail': detailData.practiceCheck === 2 }">
              <div class="check-title">实践审核</div>
              <div class="check-result">
                <el-tag :type="getCheckType(detailData.practiceCheck)" size="large">
                  {{ getCheckText(detailData.practiceCheck) }}
                </el-tag>
              </div>
              <div class="check-detail">
                实践环节是否达标
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-divider content-position="left">综合结论</el-divider>
        <el-result
          :icon="detailData.auditStatus === 1 ? 'success' : detailData.auditStatus === 2 ? 'error' : 'warning'"
          :title="getAuditStatusText(detailData.auditStatus)"
          :sub-title="detailData.comment || '暂无审核意见'"
        />

        <el-descriptions :column="1" border style="margin-top: 16px;">
          <el-descriptions-item label="审核人">{{ detailData.auditorName || '系统自动' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ detailData.auditTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- 批量审核对话框 -->
    <el-dialog title="批量自动审核" v-model="batchOpen" width="450px" append-to-body>
      <el-form :model="batchForm" label-width="80px">
        <el-form-item label="入学年份">
          <el-input v-model="batchForm.cohortYear" placeholder="如2023，留空表示全部" clearable />
        </el-form-item>
        <el-form-item label="院系">
          <el-input v-model="batchForm.department" placeholder="留空表示全部" clearable />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="batchForm.major" placeholder="留空表示全部" clearable />
        </el-form-item>
      </el-form>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px;">
        将对筛选条件内的在读学生执行自动审核，检查学分、论文、实践三项条件。
      </el-alert>
      <template #footer>
        <el-button type="primary" @click="submitBatchAudit" :loading="batchLoading">开始审核</el-button>
        <el-button @click="batchOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="GraduationAudit">
import { listGraduationAudit, autoAuditGraduation, batchAutoAuditGraduation, manualAuditGraduation, getGraduationStats, getGraduationAuditDetail } from '@/api/degree/graduationAudit'
import { getCurrentInstance, ref, reactive, toRefs, onMounted } from 'vue'

const { proxy } = getCurrentInstance()

// 状态变量
const auditList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const batchLoading = ref(false)
const auditOpen = ref(false)
const detailOpen = ref(false)
const batchOpen = ref(false)
const detailData = ref(null)

const stats = ref({ total: 0, passed: 0, auditing: 0, failed: 0 })

const columns = ref([
  { key: 0, label: `学分审核`, visible: true },
  { key: 1, label: `论文审核`, visible: true },
  { key: 2, label: `实践审核`, visible: true },
  { key: 3, label: `审核状态`, visible: true }
])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentNo: undefined,
    studentName: undefined,
    auditStatus: undefined,
    cohortYear: undefined
  },
  auditForm: {
    id: undefined,
    studentId: undefined,
    studentNo: '',
    studentName: '',
    totalCredits: 0,
    requiredCredits: 30,
    creditCheck: 0,
    thesisCheck: 0,
    practiceCheck: 0,
    status: 0,
    comment: ''
  },
  batchForm: {
    cohortYear: '',
    department: '',
    major: ''
  },
  auditRules: {
    status: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
    comment: [{ required: true, message: '请输入审核意见', trigger: 'blur' }]
  }
})

const { queryParams, auditForm, batchForm, auditRules } = toRefs(data)

// === 工具函数 ===
function getCheckText(val) {
  const map = { 0: '未审核', 1: '通过', 2: '未通过' }
  return map[val] ?? '未审核'
}
function getCheckType(val) {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[val] ?? 'info'
}
function getAuditStatusText(val) {
  const map = { 0: '待审核', 1: '审核通过', 2: '审核不通过' }
  return map[val] ?? '未知'
}
function getAuditStatusType(val) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[val] ?? 'info'
}

// === 列表加载 ===
function getList() {
    loading.value = true
  listGraduationAudit(queryParams.value).then(res => {
    loading.value = false
    auditList.value = res.data || []
    total.value = res.pagination?.total || auditList.value.length
  }).catch(() => {
    loading.value = false
    proxy.$modal.msgError('获取数据失败')
  })
}

function loadStats() {
  getGraduationStats().then(res => {
    stats.value = res.data || { total: 0, passed: 0, auditing: 0, failed: 0 }
  }).catch(() => {})
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange() {}

// === 自动审核（单条） ===
function handleAutoAudit(row) {
  proxy.$modal.confirm(`确认对学生"${row.studentName}"执行自动审核？`).then(() => {
    autoAuditGraduation(row.studentId).then(res => {
      if (res.code === 200) {
        proxy.$modal.msgSuccess('自动审核完成')
        getList()
        loadStats()
      } else {
        proxy.$modal.msgError(res.msg || '审核失败')
      }
    }).catch(() => {
      proxy.$modal.msgError('自动审核失败')
    })
  }).catch(() => {})
}

// === 批量自动审核 ===
function handleBatchAudit() {
  batchForm.value = { cohortYear: '', department: '', major: '' }
  batchOpen.value = true
}

function submitBatchAudit() {
  batchLoading.value = true
  const params = {}
  if (batchForm.value.cohortYear) params.cohortYear = parseInt(batchForm.value.cohortYear)
  if (batchForm.value.department) params.department = batchForm.value.department
  if (batchForm.value.major) params.major = batchForm.value.major

  batchAutoAuditGraduation(params).then(res => {
    batchLoading.value = false
    batchOpen.value = false
    if (res.code === 200) {
      const result = res.data
      proxy.$modal.msgSuccess(`批量审核完成：共${result.total}人，成功${result.successCount}人，失败${result.failCount}人`)
      if (result.failMessages && result.failMessages.length > 0) {
        proxy.$modal.alert(result.failMessages.join('<br>'), '失败详情', { dangerouslyUseHTMLString: true })
      }
      getList()
      loadStats()
    } else {
      proxy.$modal.msgError(res.msg || '批量审核失败')
    }
  }).catch(() => {
    batchLoading.value = false
    proxy.$modal.msgError('批量审核失败')
  })
}

// === 人工审核 ===
function handleManualAudit(row) {
  auditForm.value = {
    id: row.id,
    studentId: row.studentId,
    studentNo: row.studentNo,
    studentName: row.studentName,
    totalCredits: row.totalCredits || 0,
    requiredCredits: row.requiredCredits || 30,
    creditCheck: row.creditCheck,
    thesisCheck: row.thesisCheck,
    practiceCheck: row.practiceCheck,
    status: row.auditStatus || 0,
    comment: row.comment || ''
  }
  auditOpen.value = true
}

function submitAudit() {
  proxy.$refs['auditRef'].validate(valid => {
    if (valid) {
      const { id, status, comment } = auditForm.value
      manualAuditGraduation({ id, status, comment }).then(res => {
        if (res.code === 200) {
          proxy.$modal.msgSuccess('审核成功')
          auditOpen.value = false
          getList()
          loadStats()
        } else {
          proxy.$modal.msgError(res.msg || '审核失败')
        }
      }).catch(() => {
        proxy.$modal.msgError('审核失败')
      })
    }
  })
}

// === 详情查看 ===
function handleView(row) {
  getGraduationAuditDetail(row.id).then(res => {
    detailData.value = res.data
    detailOpen.value = true
  }).catch(() => {
    // 回退使用列表数据
    detailData.value = row
    detailOpen.value = true
  })
}

// === 初始化 ===
onMounted(() => {
  getList()
  loadStats()
})
</script>

<style scoped>
.mb16 { margin-bottom: 16px; }
.mb8 { margin-bottom: 8px; }

.stat-card {
  text-align: center;
  cursor: default;
  transition: all 0.3s;
}
.stat-card:hover { transform: translateY(-2px); }
.stat-item { padding: 8px 0; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }

.stat-passed .stat-value { color: #67C23A; }
.stat-auditing .stat-value { color: #E6A23C; }
.stat-failed .stat-value { color: #F56C6C; }

.check-row { margin-top: 8px; }
.check-card { text-align: center; margin-bottom: 12px; }
.check-title { font-size: 14px; font-weight: bold; color: #303133; margin-bottom: 8px; }
.check-result { margin-bottom: 8px; }
.check-detail { font-size: 12px; color: #909399; }
.check-pass { border-left: 3px solid #67C23A; }
.check-fail { border-left: 3px solid #F56C6C; }
</style>
