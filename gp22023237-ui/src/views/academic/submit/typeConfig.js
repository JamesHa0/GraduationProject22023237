import {
  listActivity, submitActivity, getActivityDetail,
  approveActivityMentor, approveActivitySecretary, approveActivityDean,
  listInnovation, submitInnovation, getInnovationDetail,
  approveInnovationMentor, approveInnovationSecretary, approveInnovationDean,
  listAchievement, submitAchievement, getAchievementDetail,
  approveAchievementMentor, approveAchievementSecretary, approveAchievementDean
} from '@/api/academic'

// --- Activity helpers ---
function getActivityTypeName(type) {
  const m = { 1: '学术讲座', 2: '研讨会', 3: '论坛', 4: '其他' }
  return m[type] || '-'
}
function getActivityTypeTagType(type) {
  const m = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'info' }
  return m[type] || 'info'
}

// --- Innovation helpers ---
function getInnovationTypeName(type) {
  const m = { 1: '创新项目', 2: '创业项目', 3: '竞赛' }
  return m[type] || '-'
}
function getInnovationTypeTagType(type) {
  const m = { 1: 'primary', 2: 'success', 3: 'warning' }
  return m[type] || 'info'
}
function getLevelName(level) {
  const m = { 1: '国家级', 2: '省级', 3: '市级', 4: '校级' }
  return m[level] || '-'
}
function getAwardLevelName(level) {
  const m = { 1: '特等奖', 2: '一等奖', 3: '二等奖', 4: '三等奖', 5: '优秀奖' }
  return m[level] || '-'
}

// --- Achievement helpers ---
function getAchievementTypeName(type) {
  const m = { 1: '论文', 2: '专利', 3: '科研奖励', 4: '项目参与' }
  return m[type] || '-'
}
function getAchievementTypeTagType(type) {
  const m = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'info' }
  return m[type] || 'info'
}
function getJournalLevelName(level) {
  const m = { 1: 'SCI/EI', 2: '核心期刊', 3: '普通期刊' }
  return m[level] || '-'
}
function getPatentTypeName(type) {
  const m = { 1: '发明专利', 2: '实用新型', 3: '外观设计' }
  return m[type] || '-'
}
function getPatentStatusName(status) {
  const m = { 0: '申请中', 1: '已授权' }
  return m[status] || '-'
}
function getAchievementAwardLevelName(level) {
  const m = { 1: '国家级', 2: '省级', 3: '市级', 4: '校级' }
  return m[level] || '-'
}
function getProjectRoleName(role) {
  const m = { 1: '负责人', 2: '核心成员', 3: '参与者' }
  return m[role] || '-'
}

function getApproveApi(roleId, apiMap) {
  if (roleId === 7 || roleId === 8) return apiMap.mentor
  if (roleId === 5 || roleId === 4) return apiMap.secretary
  return apiMap.dean
}

// 内容类型下拉选项（供表单和查询使用）
// typeValue对齐后端ContentType编码：1=ACTIVITY, 2=ACHIEVEMENT, 3=INNOVATION
export const TYPE_OPTIONS = [
  { label: '学术活动', value: 'activity', typeValue: 1 },
  { label: '学术成果', value: 'achievement', typeValue: 2 },
  { label: '创新创业', value: 'innovation', typeValue: 3 }
]

// 根据 typeValue 数字值获取 key
// 对齐后端ContentType: 1=activity, 2=achievement, 3=innovation
export function getTypeKeyByValue(typeValue) {
  const map = { 1: 'activity', 2: 'achievement', 3: 'innovation' }
  return map[typeValue] || 'activity'
}

// 获取通用标题字段的值（用于列表统一展示）
export function getTitleValue(row) {
  if (row.activityName) return row.activityName
  if (row.projectName) return row.projectName
  if (row.title) return row.title
  return '-'
}

// 根据类型数字值获取标签类型和名称（用于列表内容类型tag）
// 对齐后端ContentType: 1=ACTIVITY, 2=ACHIEVEMENT, 3=INNOVATION
export function getContentTypeTagType(typeValue) {
  const map = { 1: 'primary', 2: 'warning', 3: 'success' }
  return map[typeValue] || 'info'
}

export function getContentTypeName(typeValue) {
  const map = { 1: '学术活动', 2: '学术成果', 3: '创新创业' }
  return map[typeValue] || '-'
}

export const TYPE_CONFIG = {
  activity: {
    name: '学术活动',
    typeName: '活动',
    typeValue: 1,
    titleField: 'activityName',
    listApi: listActivity,
    submitApi: submitActivity,
    detailApi: getActivityDetail,
    subTypeLabel: '活动类型',
    subTypeField: 'activityType',
    subTypeOptions: [
      { label: '学术讲座', value: 1 },
      { label: '研讨会', value: 2 },
      { label: '论坛', value: 3 },
      { label: '其他', value: 4 }
    ],
    defaultForm: {
      id: undefined,
      studentId: undefined,
      activityType: undefined,
      activityName: undefined,
      speaker: undefined,
      activityTime: new Date(),
      location: undefined,
      content: undefined,
      attachmentPath: undefined
    },
    rules: {
      activityType: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
      activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
      speaker: [{ required: true, message: '请输入主讲人', trigger: 'blur' }],
      activityTime: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
      location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
      content: [{ required: true, message: '请输入活动内容', trigger: 'blur' }]
    },
    approveApiMap: {
      mentor: approveActivityMentor,
      secretary: approveActivitySecretary,
      dean: approveActivityDean
    },
    getApproveApi: (roleId) => getApproveApi(roleId, {
      mentor: approveActivityMentor,
      secretary: approveActivitySecretary,
      dean: approveActivityDean
    }),
    helpers: {
      getTypeName: getActivityTypeName,
      getTypeTagType: getActivityTypeTagType
    }
  },

  innovation: {
    name: '创新创业',
    typeName: '项目',
    typeValue: 3,
    titleField: 'projectName',
    listApi: listInnovation,
    submitApi: submitInnovation,
    detailApi: getInnovationDetail,
    subTypeLabel: '项目类型',
    subTypeField: 'projectType',
    subTypeOptions: [
      { label: '创新项目', value: 1 },
      { label: '创业项目', value: 2 },
      { label: '竞赛', value: 3 }
    ],
    defaultForm: {
      id: undefined,
      studentId: undefined,
      projectType: undefined,
      projectName: undefined,
      projectLevel: undefined,
      projectNo: undefined,
      leader: undefined,
      members: undefined,
      advisor: undefined,
      startDate: new Date(),
      endDate: undefined,
      description: undefined,
      achievements: undefined,
      awardLevel: undefined,
      fundingAmount: undefined,
      attachmentPath: undefined
    },
    rules: {
      projectType: [{ required: true, message: '请选择项目类型', trigger: 'change' }],
      projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
      projectNo: [{ required: true, message: '请输入项目编号', trigger: 'blur' }],
      leader: [{ required: true, message: '请输入负责人', trigger: 'blur' }],
      members: [{ required: true, message: '请输入参与成员', trigger: 'blur' }],
      advisor: [{ required: true, message: '请输入指导老师', trigger: 'blur' }],
      startDate: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
      description: [{ required: true, message: '请输入项目描述', trigger: 'blur' }],
      awardLevel: [{ required: true, message: '请选择获奖等级', trigger: 'change' }],
      fundingAmount: [{ required: true, message: '请输入资助金额', trigger: 'blur' }]
    },
    approveApiMap: {
      mentor: approveInnovationMentor,
      secretary: approveInnovationSecretary,
      dean: approveInnovationDean
    },
    getApproveApi: (roleId) => getApproveApi(roleId, {
      mentor: approveInnovationMentor,
      secretary: approveInnovationSecretary,
      dean: approveInnovationDean
    }),
    helpers: {
      getTypeName: getInnovationTypeName,
      getTypeTagType: getInnovationTypeTagType,
      getLevelName,
      getAwardLevelName
    }
  },

  achievement: {
    name: '学术成果',
    typeName: '成果',
    typeValue: 2,
    titleField: 'title',
    listApi: listAchievement,
    submitApi: submitAchievement,
    detailApi: getAchievementDetail,
    subTypeLabel: '成果类型',
    subTypeField: 'achievementType',
    subTypeOptions: [
      { label: '论文', value: 1 },
      { label: '专利', value: 2 },
      { label: '科研奖励', value: 3 },
      { label: '项目参与', value: 4 }
    ],
    defaultForm: {
      id: undefined,
      studentId: undefined,
      achievementType: undefined,
      title: undefined,
      authors: undefined,
      publicationDate: new Date(),
      journalName: undefined,
      journalLevel: undefined,
      volume: undefined,
      issue: undefined,
      pages: undefined,
      doi: undefined,
      patentNo: undefined,
      patentType: undefined,
      patentStatus: undefined,
      awardName: undefined,
      awardLevel: undefined,
      awardIssuer: undefined,
      projectName: undefined,
      projectRole: undefined,
      abstractContent: undefined,
      attachmentPath: undefined
    },
    rules: {
      achievementType: [{ required: true, message: '请选择成果类型', trigger: 'change' }],
      title: [{ required: true, message: '请输入成果标题', trigger: 'blur' }],
      authors: [{ required: true, message: '请输入作者', trigger: 'blur' }],
      publicationDate: [{ required: true, message: '请选择发表/授权时间', trigger: 'change' }],
      abstractContent: [{ required: true, message: '请输入摘要或描述', trigger: 'blur' }],
      // 论文类型必填
      journalName: [{ required: true, message: '请输入期刊名称', trigger: 'blur' }],
      journalLevel: [{ required: true, message: '请选择期刊级别', trigger: 'change' }],
      // 专利类型必填
      patentNo: [{ required: true, message: '请输入专利号', trigger: 'blur' }],
      patentType: [{ required: true, message: '请选择专利类型', trigger: 'change' }],
      // 科研奖励类型必填
      awardName: [{ required: true, message: '请输入奖励名称', trigger: 'blur' }],
      awardLevel: [{ required: true, message: '请选择奖励级别', trigger: 'change' }],
      awardIssuer: [{ required: true, message: '请输入发奖单位', trigger: 'blur' }],
      // 项目参与类型必填
      projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
      projectRole: [{ required: true, message: '请选择项目角色', trigger: 'change' }]
    },
    approveApiMap: {
      mentor: approveAchievementMentor,
      secretary: approveAchievementSecretary,
      dean: approveAchievementDean
    },
    getApproveApi: (roleId) => getApproveApi(roleId, {
      mentor: approveAchievementMentor,
      secretary: approveAchievementSecretary,
      dean: approveAchievementDean
    }),
    helpers: {
      getTypeName: getAchievementTypeName,
      getTypeTagType: getAchievementTypeTagType,
      getJournalLevelName,
      getPatentTypeName,
      getPatentStatusName,
      getAwardLevelName: getAchievementAwardLevelName,
      getProjectRoleName
    }
  }
}
