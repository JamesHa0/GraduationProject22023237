<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header><span>任务书管理</span></template>

      <!-- 导师视角：学生列表 + 任务书状态 -->
      <template v-if="isSupervisor">
        <el-table :data="students" v-loading="loading" stripe>
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column prop="studentName" label="姓名" width="100" />
          <el-table-column prop="major" label="专业" min-width="140" />
          <el-table-column prop="thesisTitle" label="论文题目" min-width="180">
            <template #default="{ row }">{{ row.thesisTitle || '未定题' }}</template>
          </el-table-column>
          <el-table-column label="任务书状态" width="120">
            <template #default="{ row }">
              <template v-if="taskMap[row.id]">
                <el-tag :type="getProcessStatusType(taskMap[row.id].processStatus)">{{ getProcessStatusText(taskMap[row.id].processStatus) }}</el-tag>
              </template>
              <template v-else>
                <el-tag type="info">未添加</el-tag>
              </template>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <template v-if="taskMap[row.id]">
                <el-button size="small" @click="handleEdit(row)">编辑</el-button>
              </template>
              <template v-else>
                <el-button type="primary" size="small" @click="handleAdd(row)">添加任务书</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <!-- 秘书/院长视角：所有任务书记录列表 -->
      <template v-else>
        <el-table :data="allTaskRecords" v-loading="loading" stripe>
          <el-table-column label="学生姓名" min-width="100">
            <template #default="{ row }">{{ thesisMap[row.thesisId]?.studentName || '-' }}</template>
          </el-table-column>
          <el-table-column label="论文题目" min-width="200">
            <template #default="{ row }">{{ thesisMap[row.thesisId]?.thesisTitle || '-' }}</template>
          </el-table-column>
          <el-table-column label="课题背景" min-width="180">
            <template #default="{ row }">
              {{ truncateText(parseContentExtend(row).background) }}
            </template>
          </el-table-column>
          <el-table-column label="审核状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getProcessStatusType(row.processStatus)">{{ getProcessStatusText(row.processStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" width="160">
            <template #default="{ row }">{{ parseDate(row.submitTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="handleViewDetail(row)">详情</el-button>
              <template v-if="canApprove(row)">
                <el-button type="success" size="small" @click="openApproveDialog(row)">通过</el-button>
                <el-button type="danger" size="small" @click="openRejectDialog(row)">退回</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination style="margin-top: 16px;"
          v-model:current-page="pageNum" v-model:page-size="pageSize"
          :total="total" @current-change="loadAllTaskRecords" />
      </template>

      <!-- 导师端：添加/编辑任务书对话框 -->
      <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑任务书' : '添加任务书'" width="700px">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="学生">
            <el-input :value="currentStudent?.studentName + ' - ' + (currentStudent?.thesisTitle || '未定题')" disabled />
          </el-form-item>
          <el-form-item label="课题背景" prop="background">
            <el-input v-model="form.background" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item label="主要任务" prop="mainTask">
            <el-input v-model="form.mainTask" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item label="进度安排" prop="schedule">
            <el-input v-model="form.schedule" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item label="参考文献要求">
            <el-input v-model="form.references" type="textarea" :rows="2" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="doSubmit">提交</el-button>
        </template>
      </el-dialog>

      <!-- 秘书/院长端：任务书详情对话框（含审批操作） -->
      <el-dialog v-model="detailVisible" title="任务书详情" width="700px">
        <el-descriptions :column="1" border v-if="detailRecord">
          <el-descriptions-item label="学生姓名">{{ thesisMap[detailRecord.thesisId]?.studentName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="论文题目">{{ thesisMap[detailRecord.thesisId]?.thesisTitle || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="getProcessStatusType(detailRecord.processStatus)">{{ getProcessStatusText(detailRecord.processStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="课题背景">{{ parseContentExtend(detailRecord).background || '-' }}</el-descriptions-item>
          <el-descriptions-item label="主要任务">{{ parseContentExtend(detailRecord).mainTask || '-' }}</el-descriptions-item>
          <el-descriptions-item label="进度安排">{{ parseContentExtend(detailRecord).schedule || '-' }}</el-descriptions-item>
          <el-descriptions-item label="参考文献要求">{{ parseContentExtend(detailRecord).references || '-' }}</el-descriptions-item>
          <!-- 审批状态展示 -->
          <el-descriptions-item label="导师审批">
            <el-tag v-if="detailRecord.supervisorStatus === 1" type="success">已通过</el-tag>
            <el-tag v-else-if="detailRecord.supervisorStatus === 2" type="danger">已拒绝</el-tag>
            <el-tag v-else type="info">待审批</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="秘书审批">
            <el-tag v-if="detailRecord.secretaryStatus === 1" type="success">已通过</el-tag>
            <el-tag v-else-if="detailRecord.secretaryStatus === 2" type="danger">已拒绝</el-tag>
            <el-tag v-else type="info">待审批</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <template #footer v-if="canApprove(detailRecord)">
          <el-button type="success" @click="openApproveDialog(detailRecord); detailVisible = false">通 过</el-button>
          <el-button type="danger" @click="openRejectDialog(detailRecord); detailVisible = false">退 回</el-button>
          <el-button @click="detailVisible = false">关 闭</el-button>
        </template>
        <template #footer v-else>
          <el-button @click="detailVisible = false">关 闭</el-button>
        </template>
      </el-dialog>

      <!-- 审批对话框 -->
      <el-dialog v-model="approvalDialogVisible" :title="approvalType === 'approve' ? '审批通过' : '退回任务书'" width="500px">
        <el-form ref="approvalFormRef" :model="approvalForm" label-width="80px">
          <el-form-item label="审批意见" :required="approvalType === 'reject'">
            <el-input v-model="approvalForm.comment" type="textarea" :rows="3" :placeholder="approvalType === 'reject' ? '请输入退回原因（必填）' : '请输入审批意见（选填）'" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="approvalDialogVisible = false">取消</el-button>
          <el-button :type="approvalType === 'approve' ? 'success' : 'danger'" @click="submitApproval(loadData)">{{ approvalType === 'approve' ? '确认通过' : '确认退回' }}</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getCurrentInstance } from 'vue'
import { getSupervisorTask, createTask, updateTask, getSupervisorStudents, listProcess, listThesisMain } from '@/api/degree'
import { parseContentExtend } from '@/views/degree/processConfig'
import { getProcessStatusText, getProcessStatusType, parseDate } from '@/composables/useDegreeStatus'
import { getCurrentUserRoleId, getCurrentUserId, ROLE, useProcessApproval } from '@/composables/useDegreeApproval'

const { proxy } = getCurrentInstance()
const userStore = proxy.$pinia._s.get('user')

const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const currentStudent = ref(null)
const form = ref({ thesisId: null, background: '', mainTask: '', schedule: '', references: '' })
const rules = {
  background: [{ required: true, message: '请输入课题背景', trigger: 'blur' }],
  mainTask: [{ required: true, message: '请输入主要任务', trigger: 'blur' }],
  schedule: [{ required: true, message: '请输入进度安排', trigger: 'blur' }]
}

// 导师视角数据
const students = ref([])
const taskMap = reactive({})

// 秘书/院长视角数据
const allTaskRecords = ref([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const detailVisible = ref(false)
const detailRecord = ref(null)
const thesisMap = reactive({})

// 审批功能
const {
  approvalDialogVisible,
  approvalType,
  approvalFormRef,
  approvalForm,
  canApprove,
  openApproveDialog,
  openRejectDialog,
  submitApproval,
  loadProcessConfig
} = useProcessApproval(proxy)

const isSupervisor = computed(() => {
  const roleId = getCurrentUserRoleId()
  return roleId === ROLE.MENTOR || roleId === ROLE.CO_MENTOR
})

function truncateText(text, maxLen = 40) {
  if (!text) return '-'
  return text.length > maxLen ? text.substring(0, maxLen) + '...' : text
}

function handleAdd(row) {
  isEdit.value = false
  currentStudent.value = row
  form.value = { thesisId: row.id, background: '', mainTask: '', schedule: '', references: '' }
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  currentStudent.value = row
  const record = taskMap[row.id]
  const ext = parseContentExtend(record)
  form.value = { id: record.id, thesisId: row.id, background: ext.background || '', mainTask: ext.mainTask || '', schedule: ext.schedule || '', references: ext.references || '' }
  dialogVisible.value = true
}

function doSubmit() {
  formRef.value?.validate(valid => {
    if (!valid) return
    if (isEdit.value) {
      updateTask(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); dialogVisible.value = false; loadData() })
    } else {
      createTask(form.value).then(() => { proxy.$modal.msgSuccess('创建成功'); dialogVisible.value = false; loadData() })
    }
  })
}

function handleViewDetail(row) {
  detailRecord.value = row
  detailVisible.value = true
}

async function loadAllTaskRecords() {
  loading.value = true
  try {
    const res = await listProcess({ processType: 2, pageNum: pageNum.value, pageSize: pageSize.value })
    allTaskRecords.value = res.data || res.rows || []
    total.value = res.pagination?.total || res.total || 0

    // 获取相关论文主表信息用于显示学生名和论文题
    const thesisIds = [...new Set(allTaskRecords.value.map(r => r.thesisId).filter(Boolean))]
    if (thesisIds.length > 0) {
      const needFetch = thesisIds.filter(id => !thesisMap[id])
      if (needFetch.length > 0) {
        const thesisRes = await listThesisMain({ pageNum: 1, pageSize: 999 })
        const thesisList = thesisRes.data || thesisRes.rows || []
        thesisList.forEach(t => { thesisMap[t.id] = t })
      }
    }
  } catch (e) { console.error(e) }
  loading.value = false
}

async function loadData() {
  loading.value = true
  try {
    if (isSupervisor.value) {
      const supervisorId = userStore?.roleInfo?.[0]?.id || userStore?.userId
      const [taskRes, studentRes] = await Promise.all([
        getSupervisorTask({ supervisorId, pageNum: 1, pageSize: 999 }),
        getSupervisorStudents(supervisorId)
      ])
      students.value = studentRes.data || studentRes || []
      const tasks = taskRes.data || taskRes.rows || []
      Object.keys(taskMap).forEach(k => delete taskMap[k])
      tasks.forEach(t => { if (t.thesisId) taskMap[t.thesisId] = t })
    } else {
      await loadAllTaskRecords()
    }
  } catch (e) { console.error(e) }
  loading.value = false
}

onMounted(() => {
  loadProcessConfig().then(() => loadData())
})
</script>

<style scoped>
</style>
