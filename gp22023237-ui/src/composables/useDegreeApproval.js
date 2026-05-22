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
  grantDegree,
  listProcessConfig
} from '@/api/degree'

// ==================== 角色常量定义 ====================
// 与后端角色表保持一致，避免硬编码
export const ROLE = {
  SUPER_ADMIN: 1,      // 超级管理员
  DEAN: 2,             // 分管院长
  COMMITTEE_MEMBER: 3,  // 学位分委成员/综合管理员
  SECRETARY: 4,        // 综合管理员（原标注教学秘书，实际role_id=4为综合管理员）
  DEPT_SECRETARY: 5,   // 教学秘书（实际role_id=5为教学秘书）
  MENTOR: 7,           // 导师
  CO_MENTOR: 8         // 副导师
}

// 可进行导师审批的角色
const SUPERVISOR_ROLES = [ROLE.MENTOR, ROLE.CO_MENTOR]
// 可进行秘书审批的角色
const SECRETARY_ROLES = [ROLE.DEPT_SECRETARY, ROLE.SECRETARY]
// 可进行院长审批的角色
const DEAN_ROLES = [ROLE.DEAN, ROLE.SUPER_ADMIN]
// 可录入评审结果的角色
const RESULT_RECORDER_ROLES = [ROLE.DEPT_SECRETARY, ROLE.DEAN, ROLE.SUPER_ADMIN]

/**
* 获取当前用户角色ID
* @returns {number|null}
*/
export function getCurrentUserRoleId() {
  const userStore = useUserStore()
  if (userStore.roles) {
    // roles 可能是数字（如5）或数组（如[5]），统一处理
    if (Array.isArray(userStore.roles)) {
      return userStore.roles.length > 0 ? userStore.roles[0] : null
    }
    return userStore.roles
  }
  return null
}

/**
* 获取当前用户ID
* @returns {string|number|null}
*/
export function getCurrentUserId() {
  const userStore = useUserStore()
  return userStore.userId || null
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
 * 用于 progress/index.vue 及 supervisor/task/index.vue 等
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

  // 流程配置缓存：processType → { needSupervisorApproval, needSecretaryApproval, needDeanApproval }
  const processConfigMap = reactive({})
  let configLoaded = false

  /** 加载流程配置 */
  function loadProcessConfig() {
    if (configLoaded) return Promise.resolve()
    return listProcessConfig().then(res => {
      const configs = res.data || []
      configs.forEach(c => {
        processConfigMap[c.processType] = {
          needSupervisorApproval: c.needSupervisorApproval === 1,
          needSecretaryApproval: c.needSecretaryApproval === 1,
          needDeanApproval: c.needDeanApproval === 1
        }
      })
      configLoaded = true
    }).catch(() => {
      configLoaded = true // 即使失败也标记，避免重复请求
    })
  }

  /**
   * 判断当前用户是否可审批该流程记录
   * 根据流程配置动态判断前置条件：
   * - 导师：supervisorStatus===0 且流程需要导师审批
   * - 秘书：secretaryStatus===0 且（不需要导师审批 或 导师已通过）
   * - 院长：deanStatus===0 且（不需要导师审批 或 导师已通过）且（不需要秘书审批 或 秘书已通过）
   */
  function canApprove(row) {
    if (!row) return false
    const roleId = getCurrentUserRoleId()
    if (!roleId) return false

    const config = processConfigMap[row.processType]

    if (SUPERVISOR_ROLES.includes(roleId)) {
      // 导师审批：流程需要导师审批 且 导师未审批
      if (config && !config.needSupervisorApproval) return false
      return row.supervisorStatus === 0
    }
    if (SECRETARY_ROLES.includes(roleId)) {
      // 秘书审批：流程需要秘书审批 且 秘书未审批 且（不需要导师审批 或 导师已通过）
      if (config && !config.needSecretaryApproval) return false
      const supervisorOk = !config || !config.needSupervisorApproval || row.supervisorStatus === 1
      return supervisorOk && row.secretaryStatus === 0
    }
    if (DEAN_ROLES.includes(roleId)) {
      // 院长审批：流程需要院长审批 且 院长未审批 且（不需要导师审批 或 导师已通过）且（不需要秘书审批 或 秘书已通过）
      if (config && !config.needDeanApproval) return false
      const supervisorOk = !config || !config.needSupervisorApproval || row.supervisorStatus === 1
      const secretaryOk = !config || !config.needSecretaryApproval || row.secretaryStatus === 1
      return supervisorOk && secretaryOk &&
        (row.deanStatus === 0 || row.deanStatus === null || row.deanStatus === undefined)
    }
    return false
  }

  /**
   * 判断当前用户是否可录入评审/答辩结果
   * 仅论文答辩稿(6)和毕业论文(7)环节可录入，需评审中状态
   */
  function canRecordResult(row, processType) {
    if (!row) return false
    const roleId = getCurrentUserRoleId()
    if (!roleId) return false
    // 只有答辩稿和毕业论文环节可录入评审结果
    if (processType < 6) return false
    if (row.processStatus !== 2) return false
    return RESULT_RECORDER_ROLES.includes(roleId)
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
    const approverId = getCurrentUserId()
    let approveApi

    if (SUPERVISOR_ROLES.includes(roleId)) {
      approveApi = approveProcessSupervisor
    } else if (SECRETARY_ROLES.includes(roleId)) {
      approveApi = approveProcessSecretary
    } else {
      approveApi = approveProcessDean
    }

    const status = approvalType.value === 'approve' ? 1 : 2

    approveApi(approvalForm.value.id, status, approvalForm.value.comment, approverId).then(() => {
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
    submitApproval,
    loadProcessConfig
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
    return (roleId === ROLE.COMMITTEE_MEMBER || roleId === ROLE.SUPER_ADMIN) && row.committeeStatus === 0
  }

  /**
   * 判断当前用户是否可授予学位
   * 分管院长(2)或超管(1)，且分委已通过、学位未授予
   */
  function canGrant(row) {
    if (!row) return false
    const roleId = getCurrentUserRoleId()
    return (roleId === ROLE.DEAN || roleId === ROLE.SUPER_ADMIN) && row.committeeStatus === 1 && row.degreeGranted === 0
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
    const approverId = getCurrentUserId()
    committeeApprove(approvalForm.value.id, status, approvalForm.value.comment, approverId).then(() => {
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
    grantDegree(grantForm.value.id, grantForm.value.certificateNo).then(() => {
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
