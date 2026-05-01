<template>
  <div class="app-container">
    <!-- 提交表单区域（仅学生可见） -->
    <el-card v-if="isStudent" shadow="never" class="mb20">
      <template #header>
        <div class="card-header">
          <span class="card-title">学术内容提交</span>
        </div>
      </template>

      <el-form :model="form" :rules="currentRules" ref="formRef" label-width="100px">
        <!-- 身份信息行（角色区分） -->
        <el-row>
          <el-col :span="12">
            <el-form-item :label="isStudent ? '学号' : '教师工号'" prop="submitterNo">
              <el-input v-model="form.submitterNo" :placeholder="isStudent ? '自动获取' : '自动获取'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="submitterName">
              <el-input v-model="form.submitterName" placeholder="自动获取" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 内容类型下拉选择 -->
        <el-row>
          <el-col :span="12">
            <el-form-item label="内容类型" prop="activeType">
              <el-select v-model="form.activeType" placeholder="请选择内容类型" style="width: 100%" @change="handleTypeChange">
                <el-option v-for="opt in TYPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 动态类型专属表单 -->
        <ActivityForm v-if="form.activeType === 'activity'" :form="form" />
        <InnovationForm v-if="form.activeType === 'innovation'" :form="form" />
        <AchievementForm v-if="form.activeType === 'achievement'" :form="form" />

        <!-- 提交/重置按钮 -->
        <el-row>
          <el-col :span="24">
            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="submitLoading">提交申请</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 提交记录列表 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">我的提交记录</span>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
        <el-form-item label="内容类型" prop="type">
          <el-select v-model="queryParams.type" placeholder="请选择" clearable style="width: 150px">
            <el-option v-for="opt in TYPE_OPTIONS" :key="opt.typeValue" :label="opt.label" :value="opt.typeValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待导师审批" :value="1" />
            <el-option label="待秘书审批" :value="2" />
            <el-option label="待院长审批" :value="3" />
            <el-option label="已通过" :value="4" />
            <el-option label="已驳回" :value="5" />
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
        <el-table-column label="内容类型" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getContentTypeTagType(row.type)">{{ getContentTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标题" align="center" show-overflow-tooltip min-width="180">
          <template #default="{ row }">{{ getTitleValue(row) }}</template>
        </el-table-column>
        <el-table-column label="导师审批" prop="mentorStatus" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusType(row.mentorStatus)" size="small">{{ getApprovalStatusText(row.mentorStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="秘书审批" prop="secretaryStatus" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusType(row.secretaryStatus)" size="small">{{ getApprovalStatusText(row.secretaryStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="院长审批" prop="deanStatus" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusType(row.deanStatus)" size="small">{{ getApprovalStatusText(row.deanStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" prop="submitTime" align="center" width="160">
          <template #default="{ row }">{{ parseDate(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="附件" align="center" width="150">
          <template #default="{ row }">
            <div v-if="getAttachmentList(row.attachmentPath).length" class="attachment-cell">
              <template v-for="(item, idx) in getAttachmentList(row.attachmentPath)" :key="idx">
                <el-image
                  v-if="isImageFile(item)"
                  :src="signedUrls[getUrlFromItem(item)] || ''"
                  :preview-src-list="getSignedImageList(row.attachmentPath)"
                  :initial-index="getImageListIndex(row.attachmentPath, item)"
                  fit="cover"
                  style="width: 40px; height: 40px; border-radius: 4px; margin: 2px"
                />
                <el-link v-else type="primary" :underline="false" style="margin: 2px 4px" @click="handleDownload(item)">
                  <el-icon><Document /></el-icon>
                </el-link>
              </template>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
          <template #default="{ row }">
            <el-button link size="small" type="primary" icon="View" @click="handleView(row)">详情</el-button>
            <el-button link size="small" type="success" @click="handleApprove(row)" v-if="canApprove(row)">通过</el-button>
            <el-button link size="small" type="danger" @click="handleReject(row)" v-if="canApprove(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog title="内容详情" v-model="detailDialogVisible" width="700px" append-to-body>
      <ActivityDetail v-if="detailRow && getRowTypeKey(detailRow) === 'activity'" :row="detailRow" :helpers="TYPE_CONFIG.activity.helpers" />
      <InnovationDetail v-if="detailRow && getRowTypeKey(detailRow) === 'innovation'" :row="detailRow" :helpers="TYPE_CONFIG.innovation.helpers" />
      <AchievementDetail v-if="detailRow && getRowTypeKey(detailRow) === 'achievement'" :row="detailRow" :helpers="TYPE_CONFIG.achievement.helpers" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <template v-if="detailRow && canApprove(detailRow)">
            <el-button type="danger" @click="handleOpenRejectDialog">拒绝</el-button>
            <el-button type="primary" @click="handleApprove(detailRow)">通过</el-button>
          </template>
        </div>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog v-model="approvalDialogVisible" :title="approvalType === 'approve' ? '通过申请' : '拒绝申请'" width="500px" append-to-body>
      <el-form :model="approvalForm" :rules="approvalRules" ref="approvalFormRef" label-width="80px">
        <el-form-item label="审批意见" prop="comment">
          <el-input v-model="approvalForm.comment" type="textarea" :rows="4" :placeholder="approvalType === 'approve' ? '请输入审批意见（可选）' : '请输入拒绝原因'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="approvalDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitApproval">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AcademicManagement">
import { ref, reactive, computed, getCurrentInstance, toRefs, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Document } from '@element-plus/icons-vue'
import { TYPE_CONFIG, TYPE_OPTIONS, getTypeKeyByValue, getTitleValue, getContentTypeTagType, getContentTypeName } from './typeConfig'
import { useAcademicApproval } from '@/composables/useAcademicApproval'
import { useAttachment } from '@/composables/useAttachment'
import { listSubmissions } from '@/api/academic'
import useUserStore from '@/store/modules/user'
import ActivityForm from './components/ActivityForm.vue'
import InnovationForm from './components/InnovationForm.vue'
import AchievementForm from './components/AchievementForm.vue'
import ActivityDetail from './components/ActivityDetail.vue'
import InnovationDetail from './components/InnovationDetail.vue'
import AchievementDetail from './components/AchievementDetail.vue'

const { proxy } = getCurrentInstance()
const route = useRoute()
const userStore = useUserStore()

const {
  approvalDialogVisible, approvalType, approvalFormRef,
  approvalForm, approvalRules,
  getStatusText, getStatusType, parseDate, canApprove,
  openRejectDialog, submitApproval
} = useAcademicApproval(proxy)

const {
  signedUrls, getUrlFromItem, getFileNameFromItem,
  getAttachmentPath, getAttachmentList, isImageFile,
  getImageList, getImageListIndex, fetchSignedUrls,
  getSignedImageList, handleDownload
} = useAttachment()

// 角色判断
const isStudent = computed(() => Number(userStore.roles) === 6)
const isTeacher = computed(() => Number(userStore.roles) === 7)

// 三级审批节点状态映射：0=未审批, 1=已通过, 2=已拒绝
function getApprovalStatusText(status) {
  const map = { 0: '待审批', 1: '已通过', 2: '已拒绝', null: '-', undefined: '-' }
  return map[status] !== undefined ? map[status] : '-'
}
function getApprovalStatusType(status) {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

// 表单相关
const formRef = ref(null)
const submitLoading = ref(false)
const activeType = ref(route.query.type || 'activity')

const currentConfig = computed(() => TYPE_CONFIG[activeType.value])
const currentRules = computed(() => currentConfig.value.rules)

const data = reactive({
  form: {
    submitterNo: undefined,
    submitterName: undefined,
    submitterId: undefined,
    activeType: activeType.value,
    ...TYPE_CONFIG[activeType.value].defaultForm
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    type: undefined,
    status: undefined
  }
})

const { queryParams, form } = toRefs(data)

// 列表相关
const loading = ref(false)
const showSearch = ref(true)
const dataList = ref([])
const total = ref(0)

// 详情弹窗
const detailDialogVisible = ref(false)
const detailRow = ref(null)

// 获取当前用户信息
function getCurrentUserInfo() {
  if (userStore.roleInfo && userStore.roleInfo[0]) {
    const info = userStore.roleInfo[0]
    if (isStudent.value) {
      form.value.submitterNo = info.studentNo || info.userName || ''
      form.value.submitterName = info.studentName || info.nickname || userStore.name || ''
      form.value.submitterId = info.studentId || info.id || userStore.userId
      form.value.studentId = form.value.submitterId
    } else if (isTeacher.value) {
      form.value.submitterNo = info.teacherNo || info.userName || info.teacherName || ''
      form.value.submitterName = info.teacherName || info.nickname || userStore.name || ''
      form.value.submitterId = info.teacherId || info.id || userStore.userId
    }
  } else if (userStore.name) {
    form.value.submitterName = userStore.name
    form.value.submitterId = userStore.userId
  }
}

// 获取记录的类型key
function getRowTypeKey(row) {
  if (row.contentType) return getTypeKeyByValue(row.contentType)
  // 兼容旧字段
  if (row.type) return getTypeKeyByValue(row.type)
  // 兼容没有type字段的记录：根据数据特征推断
  if (row.activityName) return 'activity'
  if (row.projectName) return 'innovation'
  if (row.title && row.achievementType !== undefined) return 'achievement'
  return activeType.value
}

// 获取审批API（根据行的类型）
function getRowApproveApi(row) {
  const typeKey = getRowTypeKey(row)
  return TYPE_CONFIG[typeKey].getApproveApi
}

// 查询列表（支持混合类型）
// 后端强制过滤：仅返回当前登录用户自己提交的记录，非学生角色返回空列表
function getList() {
  loading.value = true
  const typeFilter = queryParams.value.type

  if (typeFilter) {
    // 指定类型：使用对应API（v1兼容）
    const typeKey = getTypeKeyByValue(typeFilter)
    const config = TYPE_CONFIG[typeKey]
    const params = {
      pageNum: queryParams.value.pageNum,
      pageSize: queryParams.value.pageSize,
      status: queryParams.value.status
    }
    config.listApi(params).then(res => {
      const records = (res.data.records || res.data || []).map(item => ({ ...item, contentType: typeFilter }))
      dataList.value = records
      total.value = res.data.total || records.length
      fetchSignedUrls(records.map(r => r.attachmentPath || r.fileUrls))
    }).catch(() => {
      dataList.value = []
      total.value = 0
    }).finally(() => {
      loading.value = false
    })
  } else {
    // 未指定类型：使用v2统一列表API，避免并行3个API导致分页错误
    const params = {
      pageNum: queryParams.value.pageNum,
      pageSize: queryParams.value.pageSize,
      approvalStatus: queryParams.value.status
    }
    listSubmissions(params).then(res => {
      const records = (res.data.records || res.data || []).map(item => ({
        ...item,
        contentType: item.contentType
      }))
      dataList.value = records
      total.value = res.data.total || records.length
      fetchSignedUrls(records.map(r => r.attachmentPath || r.fileUrls))
    }).catch(() => {
      dataList.value = []
      total.value = 0
    }).finally(() => {
      loading.value = false
    })
  }
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.value.type = undefined
  queryParams.value.status = undefined
  handleQuery()
}

// 内容类型切换
function handleTypeChange(val) {
  activeType.value = val
  // 保留身份信息，重置类型专属字段
  const savedInfo = {
    submitterNo: form.value.submitterNo,
    submitterName: form.value.submitterName,
    submitterId: form.value.submitterId,
    studentId: form.value.studentId
  }
  form.value = { ...savedInfo, activeType: val, ...TYPE_CONFIG[val].defaultForm }
  if (formRef.value) {
    proxy.resetForm('formRef')
  }
}

// 重置表单
function resetForm() {
  const savedInfo = {
    submitterNo: form.value.submitterNo,
    submitterName: form.value.submitterName,
    submitterId: form.value.submitterId,
    studentId: form.value.studentId,
    activeType: form.value.activeType
  }
  form.value = { ...savedInfo, ...TYPE_CONFIG[activeType.value].defaultForm }
  if (formRef.value) {
    proxy.resetForm('formRef')
  }
}

// 提交表单
function handleSubmit() {
  formRef.value.validate(valid => {
    if (valid) {
      submitLoading.value = true
      // 准备提交数据，移除辅助字段
      const submitData = { ...form.value }
      delete submitData.submitterNo
      delete submitData.submitterName
      delete submitData.activeType

      currentConfig.value.submitApi(submitData).then(() => {
        proxy.$modal.msgSuccess('提交成功')
        resetForm()
        getList()
      }).catch(() => {
        proxy.$modal.msgError('提交失败')
      }).finally(() => {
        submitLoading.value = false
      })
    }
  })
}

// 查看详情
function handleView(row) {
  detailRow.value = row
  detailDialogVisible.value = true
}

// 审批通过
function handleApprove(row) {
  detailRow.value = row
  approvalType.value = 'approve'
  approvalForm.value.id = row.id
  approvalForm.value.comment = ''
  approvalDialogVisible.value = true
}

// 审批拒绝
function handleReject(row) {
  detailRow.value = row
  approvalType.value = 'reject'
  approvalForm.value.id = row.id
  approvalForm.value.comment = ''
  approvalDialogVisible.value = true
}

// 详情中打开拒绝弹窗
function handleOpenRejectDialog() {
  detailDialogVisible.value = false
  openRejectDialog(detailRow.value)
}

// 提交审批
function handleSubmitApproval() {
  submitApproval(getRowApproveApi(detailRow.value), () => {
    detailDialogVisible.value = false
    getList()
  })
}

onMounted(() => {
  getCurrentUserInfo()
  getList()
})

// 监听 roleInfo 变化，确保身份信息正确加载
watch(
  () => userStore.roleInfo,
  (newVal) => {
    if (newVal && newVal[0]) {
      getCurrentUserInfo()
      getList()
    }
  },
  { deep: true, immediate: true }
)
</script>

<style scoped>
.mb20 {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
}

.attachment-cell {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
}
</style>
