<template>
  <div class="app-container">
    <!-- 流程环节配置 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">流程环节配置</span>
              <el-button type="primary" size="small" @click="handleSaveAll">保存全部</el-button>
            </div>
          </template>

          <div class="config-grid">
            <el-card
              v-for="step in processSteps"
              :key="step.type"
              shadow="hover"
              class="config-card"
            >
              <template #header>
                <div class="config-card__header">
                  <el-tag :type="step.type <= activeStep ? 'success' : 'info'" effect="dark" size="small">
                    环节 {{ step.type }}
                  </el-tag>
                  <span class="config-card__title">{{ step.label }}</span>
                </div>
              </template>

              <el-form :model="configMap[step.type]" label-width="100px" size="small">
                <el-form-item label="截止时间">
                  <el-date-picker
                    v-model="configMap[step.type].deadline"
                    type="datetime"
                    placeholder="设置截止时间"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="是否开启">
                  <el-switch v-model="configMap[step.type].enabled" active-text="开启" inactive-text="关闭" />
                </el-form-item>
                <el-form-item label="导师审批">
                  <el-switch v-model="configMap[step.type].supervisorApproval" active-text="需要" inactive-text="不需要" />
                </el-form-item>
                <el-form-item label="秘书审批">
                  <el-switch v-model="configMap[step.type].secretaryApproval" active-text="需要" inactive-text="不需要" />
                </el-form-item>
                <el-form-item label="院长审批">
                  <el-switch v-model="configMap[step.type].deanApproval" active-text="需要" inactive-text="不需要" />
                </el-form-item>
                <el-form-item label="需要评审" v-if="step.type >= 4">
                  <el-switch v-model="configMap[step.type].needReview" active-text="需要" inactive-text="不需要" />
                </el-form-item>
                <el-form-item label="允许驳回重提">
                  <el-switch v-model="configMap[step.type].allowResubmit" active-text="允许" inactive-text="不允许" />
                </el-form-item>
                <el-form-item label="最大重提次数" v-if="configMap[step.type]?.allowResubmit">
                  <el-input-number v-model="configMap[step.type].maxResubmitCount" :min="1" :max="5" />
                </el-form-item>
                <el-form-item label="备注">
                  <el-input v-model="configMap[step.type].remark" type="textarea" :rows="2" placeholder="备注信息" />
                </el-form-item>
              </el-form>
            </el-card>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 全局设置 -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span class="card-title">全局设置</span>
        </div>
      </template>

      <el-form :model="globalConfig" label-width="140px" style="max-width: 600px">
        <el-form-item label="逾期自动提醒">
          <el-switch v-model="globalConfig.overdueReminder" active-text="开启" inactive-text="关闭" />
        </el-form-item>
        <el-form-item label="逾期天数阈值">
          <el-input-number v-model="globalConfig.overdueDays" :min="1" :max="30" />
          <span style="margin-left: 8px; color: #909399">天</span>
        </el-form-item>
        <el-form-item label="流程串行模式">
          <el-switch v-model="globalConfig.serialMode" active-text="严格顺序" inactive-text="允许跳过" />
          <div class="form-tip">
            <el-text type="info" size="small">严格顺序模式下，前序环节未通过不可提交后续环节</el-text>
          </div>
        </el-form-item>
        <el-form-item label="答辩资格检查">
          <el-switch v-model="globalConfig.defenseEligibilityCheck" active-text="开启" inactive-text="关闭" />
          <div class="form-tip">
            <el-text type="info" size="small">开启后学生必须通过外审才能申请答辩</el-text>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup name="ProcessConfig">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listProcessConfig, saveProcessConfig, getGlobalConfig, saveGlobalConfig } from '@/api/degree'

const activeStep = ref(5)

const processSteps = [
  { type: 1, label: '选题' },
  { type: 2, label: '任务书' },
  { type: 3, label: '开题报告' },
  { type: 4, label: '中期检查' },
  { type: 5, label: '过程稿' },
  { type: 6, label: '论文答辩稿' },
  { type: 7, label: '毕业论文' }
]

// 各环节配置
const configMap = reactive({})

// 初始化默认配置
function initConfig() {
  processSteps.forEach(step => {
    configMap[step.type] = {
      deadline: undefined,
      enabled: true,
      supervisorApproval: true,
      secretaryApproval: step.type >= 3,
      deanApproval: step.type >= 4,
      needReview: step.type >= 4,
      allowResubmit: true,
      maxResubmitCount: 2,
      remark: ''
    }
  })
}

// 模块加载时立即初始化，确保模板渲染时 configMap 已填充
initConfig()

// 全局配置
const globalConfig = reactive({
  overdueReminder: true,
  overdueDays: 7,
  serialMode: true,
  defenseEligibilityCheck: true
})

// 从后端加载配置，失败时回退到localStorage
function loadConfig() {
  listProcessConfig().then(res => {
    const configs = res.data || []
    configs.forEach(c => {
      if (configMap[c.processType]) {
        Object.assign(configMap[c.processType], {
          deadline: c.deadline,
          enabled: c.enabled === 1,
          supervisorApproval: c.needSupervisorApproval === 1,
          secretaryApproval: c.needSecretaryApproval === 1,
          deanApproval: c.needDeanApproval === 1,
          needReview: c.needReviewResult === 1,
          allowResubmit: true,
          maxResubmitCount: 2,
          remark: c.remark || '',
          _id: c.id
        })
      }
    })
  }).catch(() => {
    // API失败时回退到localStorage
    const stored = localStorage.getItem('degree_process_config')
    if (stored) {
      try {
        const data = JSON.parse(stored)
        if (data.configMap) {
          Object.keys(data.configMap).forEach(key => {
            if (configMap[key]) {
              Object.assign(configMap[key], data.configMap[key])
            }
          })
        }
      } catch (e) {
        console.warn('加载配置失败', e)
      }
    }
  })

  getGlobalConfig().then(res => {
    const configs = res.data || []
    configs.forEach(c => {
      if (c.configKey === 'thesis_overdue_reminder') globalConfig.overdueReminder = c.configValue === 'true'
      else if (c.configKey === 'thesis_overdue_days') globalConfig.overdueDays = parseInt(c.configValue) || 7
      else if (c.configKey === 'thesis_serial_mode') globalConfig.serialMode = c.configValue === 'true'
      else if (c.configKey === 'thesis_defense_eligibility_check') globalConfig.defenseEligibilityCheck = c.configValue === 'true'
    })
  }).catch(() => {
    const stored = localStorage.getItem('degree_process_config')
    if (stored) {
      try {
        const data = JSON.parse(stored)
        Object.assign(globalConfig, data.globalConfig || {})
      } catch (e) {
        console.warn('加载全局配置失败', e)
      }
    }
  })
}

function handleSaveAll() {
  // 将Date对象格式化为后端期望的 yyyy-MM-dd HH:mm:ss 字符串
  const formatDateStr = (date) => {
    if (!date) return null
    const d = new Date(date)
    const pad = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  }

  // 构建后端保存数据
  const configs = processSteps.map(step => ({
    id: configMap[step.type]._id || undefined,
    processType: step.type,
    processName: step.label,
    deadline: formatDateStr(configMap[step.type].deadline),
    enabled: configMap[step.type].enabled ? 1 : 0,
    needSupervisorApproval: configMap[step.type].supervisorApproval ? 1 : 0,
    needSecretaryApproval: configMap[step.type].secretaryApproval ? 1 : 0,
    needDeanApproval: configMap[step.type].deanApproval ? 1 : 0,
    needReviewResult: configMap[step.type].needReview ? 1 : 0,
    sort: step.type,
    remark: configMap[step.type].remark
  }))

  const globalConfigs = [
    { configKey: 'thesis_overdue_reminder', configValue: String(globalConfig.overdueReminder), configName: '逾期自动提醒', configType: 'thesis' },
    { configKey: 'thesis_overdue_days', configValue: String(globalConfig.overdueDays), configName: '逾期天数阈值', configType: 'thesis' },
    { configKey: 'thesis_serial_mode', configValue: String(globalConfig.serialMode), configName: '流程串行模式', configType: 'thesis' },
    { configKey: 'thesis_defense_eligibility_check', configValue: String(globalConfig.defenseEligibilityCheck), configName: '答辩资格检查', configType: 'thesis' }
  ]

  Promise.all([
    saveProcessConfig(configs),
    saveGlobalConfig(globalConfigs)
  ]).then(() => {
    ElMessage.success('配置已保存')
    loadConfig()
  }).catch(() => {
    // API失败时保存到localStorage
    localStorage.setItem('degree_process_config', JSON.stringify({
      configMap,
      globalConfig
    }))
    ElMessage.success('配置已保存（本地）')
  })
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
}

.config-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 16px;
}

.config-card {
  border: 1px solid #ebeef5;
  transition: box-shadow 0.2s;
}

.config-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.config-card__header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.config-card__title {
  font-size: 15px;
  font-weight: bold;
}

.form-tip {
  margin-top: 4px;
}
</style>
