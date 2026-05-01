<template>
  <div class="app-container academic-review">
    <!-- 列表区域 -->
    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">学术内容审核</span>
          <el-tag type="info" size="small">待审批 {{ pendingCount }} 项</el-tag>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
        <el-form-item label="内容类型" prop="type">
          <el-select v-model="queryParams.type" placeholder="全部类型" clearable style="width: 140px">
            <el-option label="学术活动" :value="1" />
            <el-option label="学术成果" :value="2" />
            <el-option label="创新创业" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
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
      <el-table v-loading="loading" :data="dataList" border stripe>
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="内容类型" prop="contentType" align="center" width="110">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.contentType)" size="small">{{ getTypeName(row.contentType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标题" prop="title" align="left" show-overflow-tooltip min-width="200">
          <template #default="{ row }">
            <span class="title-link" @click="handleReview(row)">{{ getTitleValue(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="申请人" prop="studentName" align="center" width="90" />
        <el-table-column label="申请时间" prop="submitTime" align="center" width="160">
          <template #default="{ row }">{{ parseDate(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="审批状态" prop="approvalStatus" align="center" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.approvalStatus)" size="small">{{ getStatusText(row.approvalStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
          <template #default="{ row }">
            <el-button link size="small" type="primary" icon="View" @click="handleReview(row)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <!-- 审核详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="drawerTitle"
      size="680px"
      direction="rtl"
      :before-close="handleDrawerClose"
      class="review-drawer"
    >
      <template v-if="currentRow">
        <div class="drawer-body">
          <!-- 基本信息 -->
          <div class="section">
            <div class="section-title">
              <el-icon><Document /></el-icon>
              <span>申请信息</span>
            </div>
            <div class="detail-content">
              <ActivityDetail v-if="currentRow.contentType === 1" :row="currentRow" :helpers="activityHelpers" />
              <InnovationDetail v-else-if="currentRow.contentType === 3" :row="currentRow" :helpers="innovationHelpers" />
              <AchievementDetail v-else-if="currentRow.contentType === 2" :row="currentRow" :helpers="achievementHelpers" />
            </div>
          </div>

          <!-- 审批历史时间线 -->
          <div class="section" v-if="approvalRecords.length > 0">
            <div class="section-title">
              <el-icon><Clock /></el-icon>
              <span>审批记录</span>
            </div>
            <div class="timeline-wrapper">
              <el-timeline>
                <el-timeline-item
                  v-for="record in approvalRecords"
                  :key="record.id"
                  :type="getTimelineType(record.approvalAction)"
                  :timestamp="parseDate(record.approvalTime)"
                  placement="top"
                >
                  <div class="timeline-card">
                    <div class="timeline-header">
                      <span class="approver-name">{{ record.approverName || '未知' }}</span>
                      <el-tag :type="getActionTagType(record.approvalAction)" size="small">{{ record.approvalActionName }}</el-tag>
                      <span class="approver-type">{{ record.approverTypeName }}</span>
                    </div>
                    <div class="timeline-comment" v-if="record.approvalComment">{{ record.approvalComment }}</div>
                    <!-- 审批人附件 -->
                    <div class="timeline-attachments" v-if="getAttachmentList(record.reviewerFileUrls).length">
                      <div class="attachment-label">附件：</div>
                      <div class="attachment-items">
                        <template v-for="(item, idx) in getAttachmentList(record.reviewerFileUrls)" :key="idx">
                          <el-link type="primary" :underline="false" @click="handleDownload(item)" style="margin-right: 10px">
                            <el-icon><Document /></el-icon>
                            {{ getFileNameFromItem(item) }}
                          </el-link>
                        </template>
                      </div>
                    </div>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>
          </div>

          <!-- 审批操作区域 -->
          <div class="section" v-if="canApprove(currentRow)">
            <div class="section-title">
              <el-icon><EditPen /></el-icon>
              <span>审批操作</span>
            </div>
            <div class="review-form">
              <el-form :model="reviewForm" ref="reviewFormRef" label-width="80px">
                <el-form-item label="审批意见" prop="comment">
                  <el-input
                    v-model="reviewForm.comment"
                    type="textarea"
                    :rows="4"
                    :placeholder="isRejectMode ? '请输入驳回原因（必填）' : '请输入审批意见（选填）'"
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
                <el-form-item label="审批附件">
                  <el-upload
                    :action="uploadUrl"
                    :headers="uploadHeaders"
                    :before-upload="handleBeforeUpload"
                    :on-success="handleUploadSuccess"
                    :on-error="handleUploadError"
                    :on-remove="handleRemove"
                    :file-list="reviewFileList"
                    :limit="3"
                    :on-exceed="handleExceed"
                  >
                    <el-button type="primary" plain size="small">选取文件</el-button>
                    <template #tip>
                      <div class="el-upload__tip">支持 doc/docx/xls/xlsx/ppt/pptx/pdf/txt/zip/rar/7z/jpg/jpeg/png/gif，单文件不超过20MB，最多3个</div>
                    </template>
                  </el-upload>
                </el-form-item>
              </el-form>
              <div class="review-actions">
                <el-button @click="drawerVisible = false">取消</el-button>
                <el-button type="danger" @click="submitReview('reject')" :loading="submitLoading">驳回修改</el-button>
                <el-button type="success" @click="submitReview('approve')" :loading="submitLoading">审批通过</el-button>
              </div>
            </div>
          </div>

          <!-- 已审批状态提示 -->
          <div class="section" v-else-if="currentRow.approvalStatus === 4">
            <el-result icon="success" title="该申请已通过审批" sub-title="无需重复操作" />
          </div>
          <div class="section" v-else-if="currentRow.approvalStatus === 5">
            <el-result icon="error" title="该申请已被驳回" sub-title="等待学生修改后重新提交" />
          </div>
          <div class="section" v-else-if="!canApprove(currentRow) && [1,2,3].includes(currentRow.approvalStatus)">
            <el-result icon="warning" title="当前审批节点非您负责" :sub-title="`当前需${getStatusText(currentRow.approvalStatus)}`" />
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup name="AcademicReview">
import { ref, reactive, computed, getCurrentInstance, toRefs, watch } from 'vue'
import { Document, Clock, EditPen } from '@element-plus/icons-vue'
import { listReview, approveReview, getApprovalRecords, getSubmissionDetail } from '@/api/academic'
import { uploadAcademicFile } from '@/api/academic'
import useUserStore from '@/store/modules/user'
import { TYPE_CONFIG } from '@/views/academic/submit/typeConfig'
import { useAttachment } from '@/composables/useAttachment'
import ActivityDetail from '@/views/academic/submit/components/ActivityDetail.vue'
import InnovationDetail from '@/views/academic/submit/components/InnovationDetail.vue'
import AchievementDetail from '@/views/academic/submit/components/AchievementDetail.vue'
import { getToken } from '@/utils/auth'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()

const activityHelpers = TYPE_CONFIG.activity.helpers
const innovationHelpers = TYPE_CONFIG.innovation.helpers
const achievementHelpers = TYPE_CONFIG.achievement.helpers

const {
  getAttachmentList, getFileNameFromItem, handleDownload
} = useAttachment()

const loading = ref(false)
const showSearch = ref(true)
const dataList = ref([])
const total = ref(0)
const drawerVisible = ref(false)
const currentRow = ref(null)
const approvalRecords = ref([])
const submitLoading = ref(false)
const reviewFormRef = ref(null)
const isRejectMode = ref(false)

// 附件上传
const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/file/upload-academic'
const uploadHeaders = ref({ Token: getToken() })
const reviewFileList = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    type: undefined,
    status: undefined
  },
  reviewForm: {
    comment: '',
    reviewerFileUrls: ''
  }
})

const { queryParams, reviewForm } = toRefs(data)

const pendingCount = computed(() => {
  return dataList.value.filter(r => [1, 2, 3].includes(r.approvalStatus) && canApprove(r)).length
})

const drawerTitle = computed(() => {
  if (!currentRow.value) return '审核详情'
  const typeName = getTypeName(currentRow.value.contentType)
  const statusText = getStatusText(currentRow.value.approvalStatus)
  return `${typeName}审核 - ${statusText}`
})

function getTypeTagType(type) {
  const typeMap = { 1: 'primary', 2: 'warning', 3: 'success' }
  return typeMap[type] || 'info'
}

function getTypeName(type) {
  const textMap = { 1: '学术活动', 2: '学术成果', 3: '创新创业' }
  return textMap[type] || '-'
}

function getTitleValue(row) {
  if (row.title) return row.title
  if (row.activityName) return row.activityName
  if (row.projectName) return row.projectName
  return '-'
}

function getStatusText(status) {
  const textMap = {
    0: '待提交', 1: '待导师审批', 2: '待秘书审批',
    3: '待院长审批', 4: '已通过', 5: '已驳回',
    null: '-', undefined: '-'
  }
  return textMap[status] !== undefined ? textMap[status] : '-'
}

function getStatusType(status) {
  const typeMap = { 0: 'info', 1: 'warning', 2: 'warning', 3: 'warning', 4: 'success', 5: 'danger' }
  return typeMap[status] || 'info'
}

function parseDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN')
}

function getCurrentUserRoleId() {
  if (userStore.roles !== undefined && userStore.roles !== null) {
    if (Array.isArray(userStore.roles)) {
      return userStore.roles.length > 0 ? userStore.roles[0] : null
    }
    return userStore.roles
  }
  return null
}

function canApprove(row) {
  if (!row) return false
  const roleId = getCurrentUserRoleId()
  if (!roleId) return false
  const approvalStatus = row.approvalStatus
  if (roleId === 7 || roleId === 8) return approvalStatus === 1
  if (roleId === 5 || roleId === 4) return approvalStatus === 2
  if (roleId === 2 || roleId === 1) return approvalStatus === 3
  return false
}

function getTimelineType(action) {
  const map = { 1: 'primary', 2: 'success', 3: 'danger', 4: 'warning' }
  return map[action] || 'info'
}

function getActionTagType(action) {
  const map = { 1: 'primary', 2: 'success', 3: 'danger', 4: 'warning' }
  return map[action] || 'info'
}

function getList() {
  loading.value = true
  listReview(queryParams.value).then(res => {
    loading.value = false
    dataList.value = res.data.records || res.data || []
    total.value = res.data.total || dataList.value.length
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
  queryParams.value.type = undefined
  queryParams.value.status = undefined
  handleQuery()
}

async function handleReview(row) {
  currentRow.value = row
  isRejectMode.value = false
  drawerVisible.value = true
  approvalRecords.value = []
  reviewForm.value.comment = ''
  reviewForm.value.reviewerFileUrls = ''
  reviewFileList.value = []

  // 加载详细审批记录
  try {
    const res = await getApprovalRecords(row.id)
    if (res.data) {
      approvalRecords.value = Array.isArray(res.data) ? res.data : []
    }
  } catch (e) {
    console.warn('加载审批记录失败', e)
  }
}

function handleDrawerClose(done) {
  if (submitLoading.value) return
  done()
}

function handleBeforeUpload(file) {
  const allowedExt = ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'pdf', 'txt', 'zip', 'rar', '7z', 'jpg', 'jpeg', 'png', 'gif']
  const ext = file.name.split('.').pop().toLowerCase()
  if (!allowedExt.includes(ext)) {
    proxy.$modal.msgError('不支持的文件类型')
    return false
  }
  if (file.size > 20 * 1024 * 1024) {
    proxy.$modal.msgError('文件大小不能超过20MB')
    return false
  }
  return true
}

function handleUploadSuccess(response, file) {
  const isSuccess = response.result === 'success' || response.code === 200
  if (isSuccess) {
    proxy.$modal.msgSuccess('上传成功')
    const url = response.data.url
    const fileName = file.name || ''
    const currentPaths = reviewForm.value.reviewerFileUrls ? reviewForm.value.reviewerFileUrls.split(',').filter(u => u.trim()) : []
    currentPaths.push(fileName ? (url + '|' + fileName) : url)
    reviewForm.value.reviewerFileUrls = currentPaths.join(',')
  } else {
    proxy.$modal.msgError(response.error || response.msg || '上传失败')
    reviewFileList.value = reviewFileList.value.filter(f => f.uid !== file.uid)
  }
}

function handleUploadError() {
  proxy.$modal.msgError('上传失败，请重试')
}

function handleRemove(file) {
  const currentPaths = reviewForm.value.reviewerFileUrls ? reviewForm.value.reviewerFileUrls.split(',').filter(u => u.trim()) : []
  const urlToRemove = file.response?.data?.url || file.url
  const updatedPaths = currentPaths.filter(item => {
    const pipeIdx = item.indexOf('|')
    const itemUrl = pipeIdx > -1 ? item.substring(0, pipeIdx) : item
    return itemUrl !== urlToRemove
  })
  reviewForm.value.reviewerFileUrls = updatedPaths.length > 0 ? updatedPaths.join(',') : ''
}

function handleExceed() {
  proxy.$modal.msgWarning('最多上传3个附件')
}

function submitReview(type) {
  isRejectMode.value = type === 'reject'
  if (type === 'reject' && !reviewForm.value.comment.trim()) {
    proxy.$modal.msgWarning('驳回时请填写驳回原因')
    return
  }

  const actionText = type === 'approve' ? '通过' : '驳回'
  proxy.$modal.confirm(`确认${actionText}该申请？`).then(() => {
    submitLoading.value = true
    const params = {
      id: currentRow.value.id,
      status: type === 'approve' ? 1 : 2,
      comment: reviewForm.value.comment || '',
      reviewerFileUrls: reviewForm.value.reviewerFileUrls || undefined
    }
    approveReview(params).then(() => {
      proxy.$modal.msgSuccess(type === 'approve' ? '审批通过' : '已驳回申请')
      drawerVisible.value = false
      getList()
    }).catch(() => {
      proxy.$modal.msgError('操作失败')
    }).finally(() => {
      submitLoading.value = false
    })
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.academic-review {
  padding: 16px;
}

.list-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 17px;
  font-weight: 700;
  color: #1d2129;
}

.title-link {
  color: #165dff;
  cursor: pointer;
  font-weight: 500;
}
.title-link:hover {
  text-decoration: underline;
}

/* 抽屉样式 */
.review-drawer :deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e6eb;
  background: #f7f8fa;
}
.review-drawer :deep(.el-drawer__body) {
  padding: 0;
}

.drawer-body {
  padding: 20px;
  overflow-y: auto;
  height: calc(100vh - 60px);
}

/* 区块 */
.section {
  margin-bottom: 24px;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 14px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e5e6eb;
}
.section-title .el-icon {
  color: #165dff;
}

.detail-content {
  background: #f9fafb;
  border-radius: 8px;
  padding: 12px;
}

/* 时间线 */
.timeline-wrapper {
  padding: 4px 0 0 4px;
}

.timeline-card {
  background: #f9fafb;
  border-radius: 8px;
  padding: 12px 16px;
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.approver-name {
  font-weight: 600;
  font-size: 14px;
  color: #1d2129;
}
.approver-type {
  font-size: 12px;
  color: #86909c;
  margin-left: auto;
}

.timeline-comment {
  font-size: 13px;
  color: #4e5969;
  line-height: 1.6;
  white-space: pre-wrap;
  margin-top: 4px;
  padding: 8px 12px;
  background: #fff;
  border-radius: 6px;
  border-left: 3px solid #165dff;
}

.timeline-attachments {
  margin-top: 8px;
  display: flex;
  align-items: flex-start;
  gap: 4px;
  font-size: 13px;
}
.attachment-label {
  color: #86909c;
  flex-shrink: 0;
}
.attachment-items {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

/* 审批表单 */
.review-form {
  background: #f9fafb;
  border-radius: 8px;
  padding: 20px;
}

.review-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5e6eb;
}

/* 响应式 */
@media (max-width: 768px) {
  .review-drawer :deep(.el-drawer) {
    width: 100% !important;
  }
  .drawer-body {
    padding: 12px;
  }
  .review-form {
    padding: 12px;
  }
  .review-actions {
    flex-wrap: wrap;
  }
  .review-actions .el-button {
    flex: 1;
    min-width: 0;
  }
}
</style>
