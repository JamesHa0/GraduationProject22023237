/**
 * 学位模块审批逻辑 composable
 * 封装三级流程审批（导师/秘书/院长）+ 学位分委审批 + 学位授予
 * 参照 useAcademicApproval 的模式
 */
import { ref, reactive, toRefs } from 'vue'
import useUserStore from '@/store/modules/user'
import {
  approveProcessSupervisor,
  approveProcessSecretary,
  approveProcessDean,
  committeeApprove,
  grantDegree
} from '@/api/degree'

/**
 * 获取当前用户角色ID
 * @returns {number|null}
 */
export function getCurrentUserRoleId() {
  const userStore = useUserStore()
  if (userStore.roles && userStore.roles.length > 0) {
    return userStore.roles[0]
  }
  return null
}

/**
 * 判断当前用户是否为指定角色
 * @param  {...number} roleIds
 * @returns {boolean}
 */
export function hasRole(...roleIds) {
  const roleId = getCurrentUserRoleId()
  return roleIds.includes(roleId)
}

/**
 * 流程审批 composable（导师/秘书/院长三级审批）
 * 用于 progress/index.vue
 */
export function useProcessApproval(proxy) {
  const approvalDialogVisible = ref(false)
  const approvalType = ref('approve')
  const approvalFormRef = ref(null)

  const approvalData = reactive({
    approvalForm: {
      id: undefined,
      comment: ''
    }
  })

  const { approvalForm } = toRefs(approvalData)

  /**
   * 判断当前用户是否可审批该流程记录
   * 规则：导师→supervisorStatus===0, 秘书→supervisor通过且secretary===0, 院长→全部通过且dean===0
   */
  function canApprove(row) {
    if (!row) return false
    const roleId = getCurrentUserRoleId()
    if (!roleId) return false

    if (roleId === 7 || roleId === 8) {
      return row.supervisorStatus === 0
    }
    if (roleId === 5 || roleId === 4) {
      return row.supervisorStatus === 1 && row.secretaryStatus === 0
    }
    if (roleId === 2 || roleId === 1) {
      return row.supervisorStatus === 1 && row.secretaryStatus === 1 &&
        (row.deanStatus === 0 || row.deanStatus === null || row.deanStatus === undefined)
    }
    return false
  }

  /**
   * 判断当前用户是否可录入评审/答辩结果
   * 仅外审(4)和答辩(5,6)环节可录入，需评审中状态
   */
  function canRecordResult(row, processType) {
    if (!row) return false
    const roleId = getCurrentUserRoleId()
    if (!roleId) return false
    if (processType < 4) return false
    if (row.processStatus !== 2) return false
    return roleId === 5 || roleId === 2 || roleId === 1
  }

  /** 打开通过对话框 */
  function openApproveDialog(row) {
    approvalType.value = 'approve'
    approvalForm.value.id = row.id
    approvalForm.value.comment = ''
    approvalDialogVisible.value = true
  }

  /** 打开拒绝对话框 */
  function openRejectDialog() {
    approvalType.value = 'reject'
    approvalForm.value.comment = ''
    approvalDialogVisible.value = true
  }

  /** 提交审批 */
  function submitApproval(onSuccess) {
    if (approvalType.value === 'reject' && !approvalForm.value.comment.trim()) {
      proxy.$modal.msgWarning('请输入拒绝原因')
      return
    }

    const roleId = getCurrentUserRoleId()
    let approveApi

    if (roleId === 7 || roleId === 8) {
      approveApi = approveProcessSupervisor
    } else if (roleId === 5 || roleId === 4) {
      approveApi = approveProcessSecretary
    } else {
      approveApi = approveProcessDean
    }

    const status = approvalType.value === 'approve' ? 1 : 2

    approveApi(approvalForm.value.id, status, approvalForm.value.comment).then(() => {
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
    canApprove,
    canRecordResult,
    openApproveDialog,
    openRejectDialog,
    submitApproval
  }
}

/**
 * 学位审批 composable（分委审批 + 学位授予）
 * 用于 approval/index.vue
 */
export function useDegreeCommitteeApproval(proxy) {
  const approvalDialogVisible = ref(false)
  const approvalType = ref('approve')
  const approvalFormRef = ref(null)
  const grantDialogVisible = ref(false)
  const grantFormRef = ref(null)

  const approvalData = reactive({
    approvalForm: {
      id: undefined,
      comment: ''
    },
    grantForm: {
      id: undefined,
      degreeGranted: 1,
      certificateNo: '',
      degreeGrantDate: ''
    }
  })

  const { approvalForm, grantForm } = toRefs(approvalData)

  /**
   * 判断当前用户是否可审批学位申请（分委审批）
   * 学位分委成员(3)或超管(1)，且 committeeStatus === 0
   */
  function canApprove(row) {
    if (!row) return false
    const roleId = getCurrentUserRoleId()
    return (roleId === 3 || roleId === 1) && row.committeeStatus === 0
  }

  /**
   * 判断当前用户是否可授予学位
   * 分管院长(2)或超管(1)，且分委已通过、学位未授予
   */
  function canGrant(row) {
    if (!row) return false
    const roleId = getCurrentUserRoleId()
    return (roleId === 2 || roleId === 1) && row.committeeStatus === 1 && row.degreeGranted === 0
  }

  /** 打开通过对话框 */
  function openApproveDialog(row) {
    approvalType.value = 'approve'
    approvalForm.value.id = row.id
    approvalForm.value.comment = ''
    approvalDialogVisible.value = true
  }

  /** 打开拒绝对话框 */
  function openRejectDialog() {
    approvalType.value = 'reject'
    approvalForm.value.id = approvalForm.value.id || (proxy.currentRow && proxy.currentRow.id)
    approvalForm.value.comment = ''
    approvalDialogVisible.value = true
  }

  /** 提交分委审批 */
  function submitApproval(onSuccess) {
    if (approvalType.value === 'reject' && !approvalForm.value.comment.trim()) {
      proxy.$modal.msgWarning('请输入拒绝原因')
      return
    }
    const status = approvalType.value === 'approve' ? 1 : 2
    committeeApprove(approvalForm.value.id, status, approvalForm.value.comment).then(() => {
      proxy.$modal.msgSuccess('审批成功')
      approvalDialogVisible.value = false
      if (onSuccess) onSuccess()
    }).catch(() => {
      proxy.$modal.msgError('审批失败')
    })
  }

  /** 打开授予学位对话框 */
  function openGrantDialog(row) {
    grantForm.value.id = row.id
    grantForm.value.certificateNo = ''
    grantForm.value.degreeGrantDate = ''
    grantDialogVisible.value = true
  }

  /** 提交学位授予 */
  function submitGrant(onSuccess) {
    grantDegree(grantForm.value).then(() => {
      proxy.$modal.msgSuccess('学位授予成功')
      grantDialogVisible.value = false
      if (onSuccess) onSuccess()
    }).catch(() => {
      proxy.$modal.msgError('学位授予失败')
    })
  }

  return {
    approvalDialogVisible,
    approvalType,
    approvalFormRef,
    approvalForm,
    grantDialogVisible,
    grantFormRef,
    grantForm,
    canApprove,
    canGrant,
    openApproveDialog,
    openRejectDialog,
    submitApproval,
    openGrantDialog,
    submitGrant
  }
}
