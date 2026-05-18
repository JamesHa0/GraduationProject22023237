<template>
  <div class="app-container">
    <!-- 学生端：条件检查 + 申请表单 + 我的申请 -->
    <template v-if="isStudent">
      <!-- 申请条件检查卡片 -->
      <el-row :gutter="16" class="mb16">
        <el-col :span="8">
          <el-card shadow="hover" class="check-card" :class="{ 'check-pass': eligibility.creditsQualified, 'check-fail': !eligibility.creditsQualified && eligibility.checked }">
            <div class="check-title">学分达标</div>
            <div class="check-result">
              <el-icon v-if="eligibility.creditsQualified" :size="32" color="#67C23A"><Check /></el-icon>
              <el-icon v-else-if="eligibility.checked" :size="32" color="#F56C6C"><Close /></el-icon>
              <el-icon v-else :size="32" color="#909399"><Loading /></el-icon>
            </div>
            <div class="check-detail">{{ eligibility.checked ? (eligibility.creditsQualified ? '已达标' : '未达标') : '检查中...' }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="check-card" :class="{ 'check-pass': eligibility.thesisPassed, 'check-fail': !eligibility.thesisPassed && eligibility.checked }">
            <div class="check-title">论文通过</div>
            <div class="check-result">
              <el-icon v-if="eligibility.thesisPassed" :size="32" color="#67C23A"><Check /></el-icon>
              <el-icon v-else-if="eligibility.checked" :size="32" color="#F56C6C"><Close /></el-icon>
              <el-icon v-else :size="32" color="#909399"><Loading /></el-icon>
            </div>
            <div class="check-detail">{{ eligibility.checked ? (eligibility.thesisPassed ? '已通过' : '未通过') : '检查中...' }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="check-card" :class="{ 'check-pass': eligibility.practicePassed, 'check-fail': !eligibility.practicePassed && eligibility.checked }">
            <div class="check-title">实践满足</div>
            <div class="check-result">
              <el-icon v-if="eligibility.practicePassed" :size="32" color="#67C23A"><Check /></el-icon>
              <el-icon v-else-if="eligibility.checked" :size="32" color="#F56C6C"><Close /></el-icon>
              <el-icon v-else :size="32" color="#909399"><Loading /></el-icon>
            </div>
            <div class="check-detail">{{ eligibility.checked ? (eligibility.practicePassed ? '已满足' : '未满足') : '检查中...' }}</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 申请表单 -->
      <el-card shadow="never" class="mb16">
        <template #header>
          <div class="card-header">
            <span class="card-title">提交学位申请</span>
          </div>
        </template>
        <el-alert v-if="!allConditionsMet && eligibility.checked" title="申请条件未全部满足，提交后可能被驳回" type="warning" :closable="false" show-icon style="margin-bottom: 16px" />
        <el-form :model="appForm" :rules="appRules" ref="appFormRef" label-width="120px" :disabled="!eligibility.checked">
          <el-row>
            <el-col :span="12">
              <el-form-item label="申请学位类型" prop="degreeType">
                <el-radio-group v-model="appForm.degreeType">
                  <el-radio :label="1">硕士学位</el-radio>
                  <el-radio :label="2">博士学位</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="答辩时间" prop="defenseTime">
                <el-date-picker v-model="appForm.defenseTime" type="datetime" placeholder="选择答辩时间" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="论文题目" prop="thesisTitle">
                <el-input v-model="appForm.thesisTitle" placeholder="请输入论文题目" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="10">
            <el-col :span="12">
              <el-form-item label="答辩地点" prop="defenseLocation">
                <el-input v-model="appForm.defenseLocation" placeholder="请输入答辩地点" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="答辩主席" prop="committeeChair">
                <el-input v-model="appForm.committeeChair" placeholder="请输入答辩委员会主席" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="答辩委员" prop="committeeMembers">
                <el-input v-model="appForm.committeeMembers" placeholder="请输入答辩委员会成员，用逗号分隔" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="申请材料" prop="attachmentPath">
                <el-input v-model="appForm.attachmentPath" placeholder="请输入材料路径" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item>
            <el-button type="primary" @click="submitApplication" :loading="submitLoading">提交申请</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 我的申请记录 -->
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">我的申请记录</span>
          </div>
        </template>
        <el-table v-loading="myLoading" :data="myApplications" border stripe>
          <el-table-column type="index" label="序号" width="55" align="center" />
          <el-table-column label="学位类型" prop="degreeType" align="center" width="100">
            <template #default="{ row }">
              <el-tag>{{ row.degreeType === 1 ? '硕士学位' : '博士学位' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="论文题目" prop="thesisTitle" align="center" show-overflow-tooltip min-width="180" />
          <el-table-column label="答辩时间" align="center" width="160">
            <template #default="{ row }">{{ parseDate(row.defenseTime) }}</template>
          </el-table-column>
          <el-table-column label="答辩结果" prop="defenseResult" align="center" width="100">
            <template #default="{ row }">
              <el-tag :type="getDefenseResultType(row.defenseResult)" size="small">{{ getDefenseResultText(row.defenseResult) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="分委审批" prop="committeeStatus" align="center" width="100">
            <template #default="{ row }">
              <el-tag :type="getApprovalStatusType(row.committeeStatus)" size="small">{{ getApprovalStatusText(row.committeeStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="学位授予" prop="degreeGranted" align="center" width="100">
            <template #default="{ row }">
              <el-tag :type="row.degreeGranted === 1 ? 'success' : 'info'" size="small">{{ row.degreeGranted === 1 ? '已授予' : '未授予' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" align="center" width="160">
            <template #default="{ row }">{{ parseDate(row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>

    <!-- 管理端：申请列表 -->
    <template v-else>
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">学位申请列表</span>
          </div>
        </template>

        <!-- 查询表单 -->
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
          <el-form-item label="学号" prop="studentNo">
            <el-input v-model="queryParams.studentNo" placeholder="请输入学号" clearable style="width: 120px" />
          </el-form-item>
          <el-form-item label="姓名" prop="studentName">
            <el-input v-model="queryParams.studentName" placeholder="请输入姓名" clearable style="width: 120px" />
          </el-form-item>
          <el-form-item label="分委审批" prop="committeeStatus">
            <el-select v-model="queryParams.committeeStatus" placeholder="请选择" clearable style="width: 120px">
              <el-option label="待审批" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已拒绝" :value="2" />
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
          <el-table-column label="学位类型" prop="degreeType" align="center" width="100">
            <template #default="{ row }">
              <el-tag>{{ row.degreeType === 1 ? '硕士学位' : '博士学位' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="论文题目" prop="thesisTitle" align="center" show-overflow-tooltip min-width="180" />
          <el-table-column label="答辩时间" align="center" width="160">
            <template #default="{ row }">{{ parseDate(row.defenseTime) }}</template>
          </el-table-column>
          <el-table-column label="分委审批" prop="committeeStatus" align="center" width="100">
            <template #default="{ row }">
              <el-tag :type="getApprovalStatusType(row.committeeStatus)" size="small">{{ getApprovalStatusText(row.committeeStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="学位授予" prop="degreeGranted" align="center" width="100">
            <template #default="{ row }">
              <el-tag :type="row.degreeGranted === 1 ? 'success' : 'info'" size="small">{{ row.degreeGranted === 1 ? '已授予' : '未授予' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="100" class-name="small-padding fixed-width">
            <template #default="{ row }">
              <el-button link size="small" type="primary" @click="handleView(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </el-card>
    </template>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="学位申请详情" size="50%" append-to-body>
      <template v-if="currentRow">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="学号">{{ currentRow.studentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentRow.studentName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学位类型">
            <el-tag size="small">{{ currentRow.degreeType === 1 ? '硕士学位' : '博士学位' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="导师">{{ currentRow.mentorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="论文题目" :span="2">{{ currentRow.thesisTitle || '-' }}</el-descriptions-item>
          <el-descriptions-item label="答辩时间">{{ parseDate(currentRow.defenseTime) }}</el-descriptions-item>
          <el-descriptions-item label="答辩地点">{{ currentRow.defenseLocation || '-' }}</el-descriptions-item>
          <el-descriptions-item label="答辩主席">{{ currentRow.committeeChair || '-' }}</el-descriptions-item>
          <el-descriptions-item label="答辩委员">{{ currentRow.committeeMembers || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">审批流程</el-divider>
        <el-timeline>
          <el-timeline-item :type="getDefenseResultType(currentRow.defenseResult) === 'success' ? 'success' : getDefenseResultType(currentRow.defenseResult) === 'danger' ? 'danger' : 'primary'" :hollow="!currentRow.defenseResult">
            <div class="timeline-node">
              <div class="timeline-node__title">答辩评审</div>
              <div class="timeline-node__status">
                <el-tag :type="getDefenseResultType(currentRow.defenseResult)" size="small">{{ getDefenseResultText(currentRow.defenseResult) }}</el-tag>
              </div>
              <div class="timeline-node__detail" v-if="currentRow.defenseScore">评分: {{ currentRow.defenseScore }}</div>
            </div>
          </el-timeline-item>
          <el-timeline-item :type="currentRow.committeeStatus === 1 ? 'success' : currentRow.committeeStatus === 2 ? 'danger' : 'warning'" :hollow="currentRow.committeeStatus === 0">
            <div class="timeline-node">
              <div class="timeline-node__title">学位分委审批</div>
              <div class="timeline-node__status">
                <el-tag :type="getApprovalStatusType(currentRow.committeeStatus)" size="small">{{ getApprovalStatusText(currentRow.committeeStatus) }}</el-tag>
              </div>
              <div class="timeline-node__detail" v-if="currentRow.committeeComment">意见: {{ currentRow.committeeComment }}</div>
            </div>
          </el-timeline-item>
          <el-timeline-item :type="currentRow.degreeGranted === 1 ? 'success' : 'info'" :hollow="currentRow.degreeGranted !== 1">
            <div class="timeline-node">
              <div class="timeline-node__title">学位授予</div>
              <div class="timeline-node__status">
                <el-tag :type="currentRow.degreeGranted === 1 ? 'success' : 'info'" size="small">{{ currentRow.degreeGranted === 1 ? '已授予' : '未授予' }}</el-tag>
              </div>
              <div class="timeline-node__detail" v-if="currentRow.degreeGranted === 1">证书编号: {{ currentRow.certificateNo || '-' }}</div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </template>
    </el-drawer>
  </div>
</template>

<script setup name="DegreeApplication">
import { ref, reactive, toRefs, computed, onMounted } from 'vue'
import { Check, Close, Loading } from '@element-plus/icons-vue'
import { listDegreeApplication, submitDegreeApplication, checkDefenseEligibility } from '@/api/degree'
import { getApprovalStatusText, getApprovalStatusType, parseDate } from '@/composables/useDegreeStatus'
import { getCurrentUserRoleId } from '@/composables/useDegreeApproval'
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()

// ==================== 公共状态 ====================
const loading = ref(false)
const showSearch = ref(true)
const dataList = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentRow = ref(null)
const submitLoading = ref(false)

const isStudent = computed(() => getCurrentUserRoleId() === 6)

// ==================== 答辩资格检查 ====================
const eligibility = ref({
  checked: false,
  creditsQualified: false,
  thesisPassed: false,
  practicePassed: false
})

const allConditionsMet = computed(() => {
  const e = eligibility.value
  return e.checked && e.creditsQualified && e.thesisPassed && e.practicePassed
})

// ==================== 申请表单 ====================
const appFormRef = ref(null)
const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentNo: undefined,
    studentName: undefined,
    committeeStatus: undefined
  },
  appForm: {
    degreeType: 1,
    thesisTitle: undefined,
    defenseTime: undefined,
    defenseLocation: undefined,
    committeeChair: undefined,
    committeeMembers: undefined,
    attachmentPath: undefined
  },
  appRules: {
    thesisTitle: [{ required: true, message: '请输入论文题目', trigger: 'blur' }],
    defenseTime: [{ required: true, message: '请选择答辩时间', trigger: 'change' }]
  }
})

const { queryParams, appForm, appRules } = toRefs(data)

// ==================== 学生端：我的申请 ====================
const myLoading = ref(false)
const myApplications = ref([])

function loadMyApplications() {
  myLoading.value = true
  // 使用roleInfo获取真实的studentId（userStore.userId是user表主键，非student表主键）
  const studentId = userStore.roleInfo?.[0]?.id || userStore.userId
  listDegreeApplication({ studentId, pageSize: 100 }).then(res => {
    myLoading.value = false
    myApplications.value = res.data || []
  }).catch(() => {
    myLoading.value = false
  })
}

// ==================== 管理端：列表 ====================
function getList() {
  loading.value = true
  listDegreeApplication(queryParams.value).then(res => {
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
  queryParams.value = { pageNum: 1, pageSize: 10, studentNo: undefined, studentName: undefined, committeeStatus: undefined }
  handleQuery()
}

// ==================== 提交申请 ====================
function submitApplication() {
  appFormRef.value.validate(valid => {
    if (valid) {
      submitLoading.value = true
      submitDegreeApplication(appForm.value).then(() => {
        submitLoading.value = false
        proxy.$modal.msgSuccess('申请提交成功')
        resetForm()
        loadMyApplications()
      }).catch(() => {
        submitLoading.value = false
        proxy.$modal.msgError('提交失败')
      })
    }
  })
}

function resetForm() {
  appFormRef.value?.resetFields()
  appForm.value = {
    degreeType: 1,
    thesisTitle: undefined,
    defenseTime: undefined,
    defenseLocation: undefined,
    committeeChair: undefined,
    committeeMembers: undefined,
    attachmentPath: undefined
  }
}

// ==================== 详情 ====================
function handleView(row) {
  currentRow.value = row
  detailVisible.value = true
}

// ==================== 答辩资格检查 ====================
function checkEligibility() {
  // 使用roleInfo获取真实的studentId（userStore.userId是user表主键，非student表主键）
  const studentId = userStore.roleInfo?.[0]?.id || userStore.userId
  if (!studentId) return
  checkDefenseEligibility(studentId).then(res => {
    const data = res.data || {}
    eligibility.value = {
      checked: true,
      creditsQualified: data.creditsQualified || false,
      thesisPassed: data.thesisPassed || false,
      practicePassed: data.practicePassed || false
    }
  }).catch(() => {
    eligibility.value = { checked: true, creditsQualified: false, thesisPassed: false, practicePassed: false }
  })
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

const { proxy } = getCurrentInstance()

// ==================== 初始化 ====================
onMounted(() => {
  if (isStudent.value) {
    checkEligibility()
    loadMyApplications()
  } else {
    getList()
  }
})
</script>

<style scoped>
.mb16 { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-size: 16px; font-weight: bold; }

.check-card { text-align: center; transition: all 0.3s; }
.check-card:hover { transform: translateY(-2px); }
.check-title { font-size: 14px; font-weight: bold; color: #303133; margin-bottom: 8px; }
.check-result { margin-bottom: 8px; }
.check-detail { font-size: 12px; color: #909399; }
.check-pass { border-left: 3px solid #67C23A; }
.check-fail { border-left: 3px solid #F56C6C; }

.timeline-node__title { font-weight: bold; font-size: 14px; margin-bottom: 4px; }
.timeline-node__status { margin-bottom: 4px; }
.timeline-node__detail { font-size: 12px; color: #909399; line-height: 1.6; }
</style>
