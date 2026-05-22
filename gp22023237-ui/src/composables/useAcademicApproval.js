import { ref, reactive, toRefs } from 'vue'
import useUserStore from '@/store/modules/user'

export function useAcademicApproval(proxy) {
  const approvalDialogVisible = ref(false)
  const approvalType = ref('approve')
  const approvalFormRef = ref(null)

  const approvalData = reactive({
    approvalForm: {
      id: undefined,
      comment: ''
    },
    approvalRules: {
      comment: [{ required: false, message: '请输入审批意见', trigger: 'blur' }]
    }
  })

  const { approvalForm, approvalRules } = toRefs(approvalData)

  function getStatusText(status) {
    // 使用后端新状态体系 approvalStatus: 0-5
    const textMap = {
      0: '待提交',
      1: '待导师审批',
      2: '待秘书审批',
      3: '待院长审批',
      4: '已通过',
      5: '已驳回',
      null: '-',
      undefined: '-'
    }
    return textMap[status] !== undefined ? textMap[status] : '-'
  }

  function getStatusType(status) {
    const typeMap = {
      0: 'info',
      1: 'warning',
      2: 'warning',
      3: 'warning',
      4: 'success',
      5: 'danger'
    }
    return typeMap[status] || 'info'
  }

  function parseDate(dateStr) {
    if (!dateStr) return '-'
    const date = new Date(dateStr)
    if (isNaN(date.getTime())) return '-'
    return date.toLocaleString('zh-CN')
  }

  function getCurrentUserRoleId() {
    const userStore = useUserStore()
    if (userStore.roles) {
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

    // 使用后端新状态体系：approvalStatus (0=待提交, 1=待导师审批, 2=待秘书审批, 3=待院长审批, 4=已通过, 5=已驳回)
    const approvalStatus = row.approvalStatus

    // 老师/导师(7)或任课教师(8)可以审批导师阶段
    if (roleId === 7 || roleId === 8) {
      return approvalStatus === 1
    }
    // 研究生秘书(5)或综合管理员(4)可以审批秘书阶段
    if (roleId === 5 || roleId === 4) {
      return approvalStatus === 2
    }
    // 分管院长(2)或超级管理员(1)可以审批院长阶段
    if (roleId === 2 || roleId === 1) {
      return approvalStatus === 3
    }
    return false
  }

  function openRejectDialog(currentRow) {
    approvalType.value = 'reject'
    approvalForm.value.id = currentRow.id
    approvalForm.value.comment = ''
    approvalDialogVisible.value = true
  }

  function submitApproval(getApproveApiFn, onSuccess) {
    if (approvalType.value === 'reject' && !approvalForm.value.comment.trim()) {
      proxy.$modal.msgWarning('请输入拒绝原因')
      return
    }
    const roleId = getCurrentUserRoleId()
    const approveApi = getApproveApiFn(roleId)
    // 新统一API：action=2通过, action=3驳回（兼容旧status: 1=通过, 2=驳回）
    const action = approvalType.value === 'approve' ? 2 : 3
    approveApi(approvalForm.value.id, action, approvalForm.value.comment).then(() => {
      proxy.$modal.msgSuccess('审批成功')
      approvalDialogVisible.value = false
      if (onSuccess) onSuccess()
    }).catch(() => {
      proxy.$modal.msgError('审批失败')
    })
  }

  return {
    approvalDialogVisible,
    approvalType,
    approvalFormRef,
    approvalForm,
    approvalRules,
    getStatusText,
    getStatusType,
    parseDate,
    getCurrentUserRoleId,
    canApprove,
    openRejectDialog,
    submitApproval
  }
}
