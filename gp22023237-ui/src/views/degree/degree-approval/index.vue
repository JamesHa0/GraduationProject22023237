<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 学位审批标签页 -->
      <el-tab-pane label="学位审批" name="approval">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">学位申请与审批</span>
              <el-button type="primary" size="small" @click="handleAddApplication" v-if="isStudent">新增申请</el-button>
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
          <el-table v-loading="loading" :data="dataList" border>
            <el-table-column type="index" label="序号" width="55" align="center" />
            <el-table-column label="学号" prop="studentNo" align="center" width="120" />
            <el-table-column label="姓名" prop="studentName" align="center" width="100" />
            <el-table-column label="学位类型" prop="degreeType" align="center" width="100">
              <template #default="{ row }">
                <el-tag>{{ row.degreeType === 1 ? '硕士学位' : '博士学位' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="论文题目" prop="thesisTitle" align="center" show-overflow-tooltip min-width="180" />
            <el-table-column label="答辩结果" prop="defenseResult" align="center" width="100">
              <template #default="{ row }">
                <el-tag :type="getReviewResultType(row.defenseResult)" size="small">
                  {{ getReviewResultText(row.defenseResult) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="答辩评分" prop="defenseScore" align="center" width="100" />
            <el-table-column label="分委审批" prop="committeeStatus" align="center" width="100">
              <template #default="{ row }">
                <el-tag :type="getApprovalStatusType(row.committeeStatus)" size="small">
                  {{ getApprovalStatusText(row.committeeStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="学位授予" prop="degreeGranted" align="center" width="100">
              <template #default="{ row }">
                <el-tag :type="row.degreeGranted === 1 ? 'success' : 'info'" size="small">
                  {{ row.degreeGranted === 1 ? '已授予' : '未授予' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
              <template #default="{ row }">
                <el-button link size="small" type="primary" @click="handleView(row)">详情</el-button>
                <el-button link size="small" type="success" @click="handleApprove(row)" v-if="canApprove(row)">审批</el-button>
                <el-button link size="small" type="warning" @click="handleGrant(row)" v-if="canGrant(row)">授予</el-button>
              </template>
            </el-table-column>
          </el-table>

          <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
        </el-card>
      </el-tab-pane>

      <!-- 我的学生标签页（导师专用） -->
      <el-tab-pane label="我的学生" name="myStudents" v-if="isMentor">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">我指导的学生</span>
              <el-button type="primary" size="small" icon="Refresh" @click="loadMyStudents">刷新</el-button>
            </div>
          </template>

          <el-table v-loading="studentsLoading" :data="studentsList" border>
            <el-table-column type="index" label="序号" width="55" align="center" />
            <el-table-column label="学号" prop="studentNo" align="center" width="120" />
            <el-table-column label="姓名" prop="studentName" align="center" width="100" />
            <el-table-column label="专业" prop="major" align="center" width="120" />
            <el-table-column label="论文题目" align="center" show-overflow-tooltip min-width="200">
              <template #default="{ row }">
                {{ row.thesisTitle || '未填写' }}
              </template>
            </el-table-column>
            <el-table-column label="论文状态" align="center" width="100">
              <template #default="{ row }">
                <el-tag :type="getFinalResultType(row.finalResult)" size="small">
                  {{ getFinalResultText(row.finalResult) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="归档状态" align="center" width="100">
              <template #default="{ row }">
                <el-tag :type="row.archiveStatus === 1 ? 'success' : 'info'" size="small">
                  {{ row.archiveStatus === 1 ? '已归档' : '未归档' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
              <template #default="{ row }">
                <el-button link size="small" type="primary" @click="viewStudentProgress(row)">流程详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <pagination v-show="studentsTotal > 0" :total="studentsTotal" v-model:page="studentsQuery.pageNum" v-model:limit="studentsQuery.pageSize" @pagination="loadMyStudents" />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 详情抽屉 - 左侧时间轴 + 右侧操作区 -->
    <el-drawer v-model="detailDrawerVisible" title="学位申请详情" size="65%" append-to-body>
      <template v-if="currentRow">
        <div class="detail-layout">
          <!-- 左侧：审批时间轴 -->
          <div class="detail-left">
            <h4 class="section-title">审批流程</h4>
            <el-timeline class="approval-timeline">
              <el-timeline-item
                :type="getReviewResultType(currentRow.defenseResult) === 'success' ? 'success' : getReviewResultType(currentRow.defenseResult) === 'danger' ? 'danger' : 'primary'"
                :hollow="!currentRow.defenseResult"
              >
                <div class="timeline-node">
                  <div class="timeline-node__title">答辩评审</div>
                  <div class="timeline-node__status">
                    <el-tag :type="getReviewResultType(currentRow.defenseResult)" size="small">
                      {{ getReviewResultText(currentRow.defenseResult) }}
                    </el-tag>
                  </div>
                  <div class="timeline-node__detail" v-if="currentRow.defenseScore">评分: {{ currentRow.defenseScore }}</div>
                </div>
              </el-timeline-item>

              <el-timeline-item
                :type="currentRow.committeeStatus === 1 ? 'success' : currentRow.committeeStatus === 2 ? 'danger' : currentRow.committeeStatus === 0 ? 'warning' : 'info'"
                :hollow="currentRow.committeeStatus === 0"
              >
                <div class="timeline-node">
                  <div class="timeline-node__title">学位分委审批</div>
                  <div class="timeline-node__status">
                    <el-tag :type="getApprovalStatusType(currentRow.committeeStatus)" size="small">
                      {{ getApprovalStatusText(currentRow.committeeStatus) }}
                    </el-tag>
                  </div>
                  <div class="timeline-node__detail" v-if="currentRow.committeeComment">意见: {{ currentRow.committeeComment }}</div>
                </div>
              </el-timeline-item>

              <el-timeline-item
                :type="currentRow.degreeGranted === 1 ? 'success' : 'info'"
                :hollow="currentRow.degreeGranted !== 1"
              >
                <div class="timeline-node">
                  <div class="timeline-node__title">学位授予</div>
                  <div class="timeline-node__status">
                    <el-tag :type="currentRow.degreeGranted === 1 ? 'success' : 'info'" size="small">
                      {{ currentRow.degreeGranted === 1 ? '已授予' : '未授予' }}
                    </el-tag>
                  </div>
                  <div class="timeline-node__detail" v-if="currentRow.degreeGranted === 1">
                    证书编号: {{ currentRow.certificateNo || '-' }}<br/>
                    授予日期: {{ parseDate(currentRow.degreeGrantDate) }}
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>

            <!-- 答辩信息 -->
            <h4 class="section-title" style="margin-top: 24px">答辩信息</h4>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="答辩时间">{{ parseDate(currentRow.defenseTime) }}</el-descriptions-item>
              <el-descriptions-item label="答辩地点">{{ currentRow.defenseLocation || '-' }}</el-descriptions-item>
              <el-descriptions-item label="答辩主席">{{ currentRow.committeeChair || '-' }}</el-descriptions-item>
              <el-descriptions-item label="答辩委员">{{ currentRow.committeeMembers || '-' }}</el-descriptions-item>
              <el-descriptions-item label="答辩评语">
                <div style="white-space: pre-wrap">{{ currentRow.defenseCommitteeComment || '-' }}</div>
              </el-descriptions-item>
              <el-descriptions-item label="问答记录">
                <div style="white-space: pre-wrap">{{ currentRow.qaRecord || '-' }}</div>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 右侧：学生信息 + 操作区 -->
          <div class="detail-right">
            <h4 class="section-title">学生信息</h4>
            <el-descriptions :column="1" border size="small" style="margin-bottom: 20px">
              <el-descriptions-item label="学号">{{ currentRow.studentNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="姓名">{{ currentRow.studentName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="学位类型">
                <el-tag size="small">{{ currentRow.degreeType === 1 ? '硕士学位' : '博士学位' }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="导师">{{ currentRow.mentorName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="论文题目">{{ currentRow.thesisTitle || '-' }}</el-descriptions-item>
            </el-descriptions>

            <!-- 操作区 -->
            <h4 class="section-title">操作</h4>
            <div class="action-area">
              <div class="action-block" v-if="canApprove(currentRow)">
                <el-alert title="该申请待您审批" type="warning" :closable="false" show-icon style="margin-bottom: 16px" />
                <el-input v-model="detailApprovalComment" type="textarea" :rows="3" placeholder="请输入审批意见（拒绝时必填）" style="margin-bottom: 12px" />
                <div style="display: flex; gap: 8px">
                  <el-button type="danger" @click="handleDetailReject">拒绝</el-button>
                  <el-button type="primary" @click="handleDetailApprove">通过</el-button>
                </div>
              </div>

              <div class="action-block" v-if="canGrant(currentRow)">
                <el-alert title="该申请已通过分委审批，可授予学位" type="success" :closable="false" show-icon style="margin-bottom: 16px" />
                <el-form label-width="100px" size="small">
                  <el-form-item label="证书编号">
                    <el-input v-model="detailCertificateNo" placeholder="请输入学位证书编号" />
                  </el-form-item>
                </el-form>
                <el-button type="warning" @click="handleDetailGrant">授予学位</el-button>
              </div>

              <div class="action-block" v-if="!canApprove(currentRow) && !canGrant(currentRow)">
                <el-alert
                  :title="currentRow.degreeGranted === 1 ? '学位已授予，流程完结' : currentRow.committeeStatus === 0 ? '等待分委审批中' : '当前无可用操作'"
                  :type="currentRow.degreeGranted === 1 ? 'success' : 'info'"
                  :closable="false"
                  show-icon
                />
              </div>
            </div>
          </div>
        </div>
      </template>
    </el-drawer>

    <!-- 学生流程详情抽屉 - 左侧时间轴 + 右侧详情 -->
    <el-drawer v-model="studentDrawerVisible" :title="`${currentStudent.studentName} - 论文流程进度`" size="65%" append-to-body>
      <div class="detail-layout">
        <!-- 左侧：流程时间轴 -->
        <div class="detail-left">
          <h4 class="section-title">流程进度</h4>
          <el-steps :active="studentActiveStep" align-center direction="vertical" :space="60" style="margin-bottom: 20px">
            <el-step v-for="step in processSteps" :key="step.type" :title="step.label" :status="getStudentStepStatus(step.type)" />
          </el-steps>

          <el-timeline>
            <el-timeline-item
              v-for="step in processSteps"
              :key="step.type"
              :type="getStudentTimelineType(step.type)"
              placement="top"
            >
              <div class="timeline-node">
                <div class="timeline-node__title">{{ step.label }}</div>
                <div class="timeline-node__status">
                  <el-tag size="small" :type="getStudentStepTagType(step.type)">
                    {{ getStudentStepDesc(step.type) }}
                  </el-tag>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>

        <!-- 右侧：详细信息 -->
        <div class="detail-right">
          <h4 class="section-title">学生信息</h4>
          <el-descriptions :column="1" border size="small" style="margin-bottom: 20px">
            <el-descriptions-item label="学号">{{ currentStudent.studentNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ currentStudent.studentName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="论文题目">{{ currentStudent.thesisTitle || '-' }}</el-descriptions-item>
          </el-descriptions>

          <h4 class="section-title">各环节详情</h4>
          <el-collapse v-model="studentProcessActiveNames">
            <el-collapse-item
              v-for="step in processSteps"
              :key="step.type"
              :name="step.type"
            >
              <template #title>
                <div class="collapse-title">
                  <span>{{ step.label }}</span>
                  <el-tag size="small" :type="getStudentStepTagType(step.type)" style="margin-left: 8px">
                    {{ getStudentStepDesc(step.type) }}
                  </el-tag>
                </div>
              </template>

              <div v-if="studentProcessMap[step.type] && studentProcessMap[step.type].length > 0">
                <el-table :data="studentProcessMap[step.type]" border size="small">
                  <el-table-column label="版本" prop="version" width="60" align="center" />
                  <el-table-column label="导师审批" width="90" align="center">
                    <template #default="{ row }">
                      <el-tag :type="getApprovalStatusType(row.supervisorStatus)" size="small">
                        {{ getApprovalStatusText(row.supervisorStatus) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="秘书审批" width="90" align="center">
                    <template #default="{ row }">
                      <el-tag :type="getApprovalStatusType(row.secretaryStatus)" size="small">
                        {{ getApprovalStatusText(row.secretaryStatus) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="院长审批" width="90" align="center">
                    <template #default="{ row }">
                      <el-tag :type="getApprovalStatusType(row.deanStatus)" size="small">
                        {{ getApprovalStatusText(row.deanStatus) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="状态" width="90" align="center">
                    <template #default="{ row }">
                      <el-tag :type="getProcessStatusType(row.processStatus)" size="small">
                        {{ getProcessStatusText(row.processStatus) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="提交时间" width="150" align="center">
                    <template #default="{ row }">
                      {{ parseDate(row.submitTime) }}
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              <el-empty v-else :description="`暂无${step.label}记录`" :image-size="40" />
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>
    </el-drawer>

    <!-- 审批对话框 -->
    <el-dialog v-model="approvalDialogVisible" :title="approvalType === 'approve' ? '通过申请' : '拒绝申请'" width="500px" append-to-body>
      <el-form :model="approvalForm" :rules="approvalRules" ref="approvalFormRef" label-width="80px">
        <el-form-item label="审批意见" prop="comment">
          <el-input v-model="approvalForm.comment" type="textarea" :rows="4" :placeholder="approvalType === 'approve' ? '请输入审批意见（可选）' : '请输入拒绝原因'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApproval">确定</el-button>
      </template>
    </el-dialog>

    <!-- 学位授予对话框 -->
    <el-dialog v-model="grantDialogVisible" title="授予学位" width="500px" append-to-body>
      <el-form :model="grantForm" :rules="grantRules" ref="grantFormRef" label-width="100px">
        <el-form-item label="学位证书编号" prop="certificateNo">
          <el-input v-model="grantForm.certificateNo" placeholder="请输入学位证书编号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="grantDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGrant">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新增学位申请对话框 -->
    <el-dialog v-model="appDialogVisible" title="新增学位申请" width="800px" append-to-body>
      <el-form :model="appForm" :rules="appRules" ref="appFormRef" label-width="120px">
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
              <el-date-picker v-model="appForm.defenseTime" type="datetime" placeholder="选择时间" style="width: 100%" />
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
              <el-input v-model="appForm.defenseLocation" placeholder="请输入地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="答辩委员会主席" prop="committeeChair">
              <el-input v-model="appForm.committeeChair" placeholder="请输入主席姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="答辩委员会成员" prop="committeeMembers">
              <el-input v-model="appForm.committeeMembers" placeholder="请输入成员姓名，用逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="申请材料路径" prop="attachmentPath">
              <el-input v-model="appForm.attachmentPath" placeholder="请输入材料路径" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="appDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApplication">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DegreeApprovalNew">
import { ref, reactive, getCurrentInstance, toRefs, computed, onMounted } from 'vue'
import { listDegreeApplication, submitDegreeApplication, committeeApprove, grantDegree, listThesisMain, listProcess } from '@/api/degree'
import {
  getApprovalStatusText, getApprovalStatusType,
  getReviewResultText, getReviewResultType,
  getProcessStatusText, getProcessStatusType,
  getFinalResultText, getFinalResultType,
  parseDate, getLatestProcessStatusText, getTimelineType
} from '@/composables/useDegreeStatus'
import { getCurrentUserRoleId, ROLE } from '@/composables/useDegreeApproval'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()

// ==================== 公共状态 ====================
const loading = ref(false)
const showSearch = ref(true)
const dataList = ref([])
const total = ref(0)
const detailDrawerVisible = ref(false)
const approvalDialogVisible = ref(false)
const grantDialogVisible = ref(false)
const currentRow = ref(null)
const approvalType = ref('approve')
const approvalFormRef = ref(null)
const grantFormRef = ref(null)
const activeTab = ref('approval')

// 详情抽屉内审批
const detailApprovalComment = ref('')
const detailCertificateNo = ref('')

// ==================== 新增申请状态 ====================
const appDialogVisible = ref(false)
const appFormRef = ref(null)

// ==================== 我的学生状态 ====================
const studentsLoading = ref(false)
const studentsList = ref([])
const studentsTotal = ref(0)
const studentDrawerVisible = ref(false)
const currentStudent = ref({})
const studentProcessMap = ref({})
const studentProcessActiveNames = ref([1, 2, 3])

const processSteps = [
  { type: 1, label: '选题' },
  { type: 2, label: '任务书' },
  { type: 3, label: '开题报告' },
  { type: 4, label: '中期检查' },
  { type: 5, label: '过程稿' },
  { type: 6, label: '论文答辩稿' },
  { type: 7, label: '毕业论文' }
]

const isMentor = computed(() => {
  const roleId = getCurrentUserRoleId()
  return roleId === 7 || roleId === 8
})

const isStudent = computed(() => {
  const roleId = getCurrentUserRoleId()
  return roleId === 6
})

const studentActiveStep = computed(() => {
  let step = 0
  for (const s of processSteps) {
    const records = studentProcessMap.value[s.type] || []
    const hasPassed = records.some(r => r.processStatus === 3 || r.processStatus === 5)
    if (hasPassed) step = s.type
    else break
  }
  return step
})

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentNo: undefined,
    studentName: undefined,
    committeeStatus: undefined
  },
  studentsQuery: {
    pageNum: 1,
    pageSize: 10
  },
  approvalForm: { id: undefined, comment: '' },
  approvalRules: { comment: [{ required: false, message: '请输入审批意见', trigger: 'blur' }] },
  grantForm: { id: undefined, certificateNo: '' },
  grantRules: { certificateNo: [{ required: true, message: '请输入学位证书编号', trigger: 'blur' }] },
  appForm: {
    id: undefined,
    studentId: undefined,
    degreeType: 1,
    thesisTitle: undefined,
    defenseTime: undefined,
    defenseLocation: undefined,
    committeeChair: undefined,
    committeeMembers: undefined,
    defenseResult: 0,
    defenseScore: 0,
    degreeGranted: 0,
    certificateNo: undefined,
    attachmentPath: undefined
  },
  appRules: {
    thesisTitle: [{ required: true, message: '请输入论文题目', trigger: 'blur' }],
    defenseTime: [{ required: true, message: '请选择答辩时间', trigger: 'change' }]
  }
})

const { queryParams, studentsQuery, approvalForm, approvalRules, grantForm, grantRules, appForm, appRules } = toRefs(data)

// ==================== 学生步骤状态辅助 ====================

function getStudentStepStatus(type) {
  const records = studentProcessMap.value[type] || []
  if (records.length === 0) return 'wait'
  const latest = records.reduce((a, b) => (a.version > b.version ? a : b))
  if (latest.processStatus === 3 || latest.processStatus === 5) return 'finish'
  if (latest.processStatus === 4) return 'error'
  return 'process'
}

function getStudentStepTagType(type) {
  const status = getStudentStepStatus(type)
  if (status === 'finish') return 'success'
  if (status === 'error') return 'danger'
  if (status === 'process') return 'warning'
  return 'info'
}

function getStudentStepDesc(type) {
  const records = studentProcessMap.value[type] || []
  return getLatestProcessStatusText(records)
}

function getStudentTimelineType(type) {
  const records = studentProcessMap.value[type] || []
  return getTimelineType(records)
}

// ==================== 权限判断 ====================

function canApprove(row) {
  if (!row) return false
  const roleId = getCurrentUserRoleId()
  return (roleId === ROLE.COMMITTEE_MEMBER || roleId === ROLE.SUPER_ADMIN) && row.committeeStatus === 0
}

function canGrant(row) {
  if (!row) return false
  const roleId = getCurrentUserRoleId()
  return (roleId === ROLE.DEAN || roleId === ROLE.SUPER_ADMIN) && row.committeeStatus === 1 && row.degreeGranted === 0
}

// ==================== 学位审批操作 ====================

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
  proxy.resetForm('queryRef')
  queryParams.value.studentNo = undefined
  queryParams.value.studentName = undefined
  queryParams.value.committeeStatus = undefined
  handleQuery()
}

function handleView(row) {
  currentRow.value = row
  detailApprovalComment.value = ''
  detailCertificateNo.value = ''
  detailDrawerVisible.value = true
}

function handleApprove(row) {
  currentRow.value = row
  approvalType.value = 'approve'
  approvalForm.value.id = row.id
  approvalForm.value.comment = ''
  approvalDialogVisible.value = true
}

function handleGrant(row) {
  currentRow.value = row
  grantForm.value.id = row.id
  grantForm.value.certificateNo = ''
  grantDialogVisible.value = true
}

// ==================== 详情抽屉内审批 ====================

function handleDetailApprove() {
  doCommitteeApprove(1, detailApprovalComment.value)
}

function handleDetailReject() {
  if (!detailApprovalComment.value.trim()) {
    proxy.$modal.msgWarning('请输入拒绝原因')
    return
  }
  doCommitteeApprove(2, detailApprovalComment.value)
}

function doCommitteeApprove(status, comment) {
  committeeApprove(currentRow.value.id, status, comment).then(() => {
    proxy.$modal.msgSuccess('审批成功')
    detailDrawerVisible.value = false
    getList()
  }).catch(() => {
    proxy.$modal.msgError('审批失败')
  })
}

function handleDetailGrant() {
  if (!detailCertificateNo.value.trim()) {
    proxy.$modal.msgWarning('请输入学位证书编号')
    return
  }
  grantDegree(currentRow.value.id, detailCertificateNo.value).then(() => {
    proxy.$modal.msgSuccess('学位授予成功')
    detailDrawerVisible.value = false
    getList()
  }).catch(() => {
    proxy.$modal.msgError('学位授予失败')
  })
}

// ==================== 表格行内审批/授予 ====================

function submitApproval() {
  if (approvalType.value === 'reject' && !approvalForm.value.comment.trim()) {
    proxy.$modal.msgWarning('请输入拒绝原因')
    return
  }
  const status = approvalType.value === 'approve' ? 1 : 2
  committeeApprove(approvalForm.value.id, status, approvalForm.value.comment).then(() => {
    proxy.$modal.msgSuccess('审批成功')
    approvalDialogVisible.value = false
    getList()
  }).catch(() => {
    proxy.$modal.msgError('审批失败')
  })
}

function submitGrant() {
  proxy.$refs['grantFormRef'].validate(valid => {
    if (valid) {
      grantDegree(grantForm.value.id, grantForm.value.certificateNo).then(() => {
        proxy.$modal.msgSuccess('学位授予成功')
        grantDialogVisible.value = false
        getList()
      }).catch(() => {
        proxy.$modal.msgError('学位授予失败')
      })
    }
  })
}

// ==================== 我的学生操作 ====================

function loadMyStudents() {
  const roleId = getCurrentUserRoleId()
  if (roleId !== 7 && roleId !== 8) return

  studentsLoading.value = true
  listThesisMain({
    pageNum: studentsQuery.value.pageNum,
    pageSize: studentsQuery.value.pageSize,
    supervisorId: userStore.user?.id
  }).then(res => {
    studentsLoading.value = false
    studentsList.value = res.data || []
    studentsTotal.value = res.pagination?.total || studentsList.value.length
  }).catch(() => {
    studentsLoading.value = false
  })
}

function viewStudentProgress(row) {
  currentStudent.value = row
  studentProcessMap.value = {}
  studentDrawerVisible.value = true

  // 并行加载各环节记录
  const promises = processSteps.map(step =>
    listProcess({ thesisId: row.id, processType: step.type, pageSize: 100 }).then(res => {
      const list = res.data || []
      studentProcessMap.value[step.type] = list.map(item => {
        if (item.contentExtend) {
          try {
            item.contentExtend = typeof item.contentExtend === 'string' ? JSON.parse(item.contentExtend) : item.contentExtend
          } catch (e) { /* ignore */ }
        }
        return item
      })
    })
  )
  Promise.all(promises)
}

// ==================== 新增学位申请 ====================

function handleAddApplication() {
  // 从roleInfo获取真实的studentId（userStore.userId是user表主键，非student表主键）
  const studentId = userStore.roleInfo?.[0]?.id || userStore.userId
  appForm.value = {
    id: undefined,
    studentId: studentId,
    degreeType: 1,
    thesisTitle: undefined,
    defenseTime: undefined,
    defenseLocation: undefined,
    committeeChair: undefined,
    committeeMembers: undefined,
    defenseResult: 0,
    defenseScore: 0,
    degreeGranted: 0,
    certificateNo: undefined,
    attachmentPath: undefined
  }
  appDialogVisible.value = true
}

function submitApplication() {
  appFormRef.value.validate(valid => {
    if (valid) {
      submitDegreeApplication(appForm.value).then(() => {
        proxy.$modal.msgSuccess('提交成功')
        appDialogVisible.value = false
        getList()
      }).catch(() => {
        proxy.$modal.msgError('提交失败')
      })
    }
  })
}

// ==================== 初始化 ====================

onMounted(() => {
  getList()
  if (isMentor.value) {
    loadMyStudents()
  }
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-size: 16px; font-weight: bold; }

/* 左右分栏布局 */
.detail-layout { display: flex; gap: 24px; min-height: 400px; }
.detail-left { flex: 1; min-width: 0; border-right: 1px solid #ebeef5; padding-right: 20px; }
.detail-right { width: 340px; flex-shrink: 0; }

.section-title { font-size: 15px; font-weight: bold; margin-bottom: 16px; padding-bottom: 8px; border-bottom: 2px solid #409eff; color: #303133; }

/* 审批时间轴 */
.approval-timeline { padding-left: 4px; }
.timeline-node__title { font-weight: bold; font-size: 14px; margin-bottom: 4px; }
.timeline-node__status { margin-bottom: 4px; }
.timeline-node__detail { font-size: 12px; color: #909399; line-height: 1.6; }

/* 操作区 */
.action-area { padding: 12px; background: #fafafa; border-radius: 8px; }
.action-block { margin-bottom: 16px; }
.action-block:last-child { margin-bottom: 0; }

/* 折叠面板标题 */
.collapse-title { display: flex; align-items: center; }
</style>
