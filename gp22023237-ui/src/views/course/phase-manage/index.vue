<template>
  <div class="app-container">
    <!-- 顶部：当前阶段信息 -->
    <el-card class="phase-info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Timer /></el-icon>
            当前课程阶段
          </span>
          <el-button type="primary" @click="loadAllData" icon="Refresh" size="small" circle />
        </div>
      </template>
      <el-row :gutter="24" align="middle">
        <el-col :span="5">
          <div class="stat-box">
            <div class="stat-label">当前阶段</div>
            <div class="stat-value">
              <el-tag :type="phaseTagType" size="large" effect="dark" round>
                {{ phaseText }}
              </el-tag>
            </div>
          </div>
        </el-col>
        <el-col :span="19">
          <div class="progress-wrapper">
            <el-steps :active="currentPhase" finish-status="success" simple align-center>
              <el-step title="未开始" icon="CircleClose" />
              <el-step title="选课阶段" icon="UserFilled" />
              <el-step title="已开课" icon="Reading" />
              <el-step title="成绩录入" icon="EditPen" />
              <el-step title="已结课" icon="CircleCheck" />
            </el-steps>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20" class="mt-20">
      <!-- 左侧：阶段管理操作 -->
      <el-col :span="8">
        <el-card class="operation-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Setting /></el-icon>
                阶段管理
              </span>
            </div>
          </template>

          <!-- 推进操作 -->
          <div class="section">
            <div class="section-title">
              <el-icon><Promotion /></el-icon>
              阶段推进
            </div>
            <el-button
              type="primary"
              @click="showAdvanceDialog"
              :loading="advanceLoading"
              :disabled="currentPhase >= 4"
              style="width: 100%;"
              size="large"
              round
            >
              <el-icon v-if="!advanceLoading"><Right /></el-icon>
              {{ advanceButtonText }}
            </el-button>
            <div class="action-desc">{{ advanceButtonDesc }}</div>
          </div>

          <el-divider />

          <!-- 重置按钮 -->
          <div class="section danger-section">
            <div class="section-title danger-title">
              <el-icon><WarningFilled /></el-icon>
              危险操作
            </div>
            <el-button
              type="danger"
              @click="showResetConfirm"
              style="width: 100%;"
              plain
              round
            >
              <el-icon><RefreshLeft /></el-icon>
              重置阶段
            </el-button>
            <div class="action-desc danger">清空所有阶段时间，将阶段重置为未开始状态</div>
          </div>
        </el-card>

        <!-- 统计卡片 -->
        <el-card class="stats-card mt-20" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><DataAnalysis /></el-icon>
                统计信息
              </span>
            </div>
          </template>
          <div class="stats-grid">
            <div class="stats-cell">
              <div class="cell-value">{{ statistics.selectionCount || 0 }}</div>
              <div class="cell-label">选课记录数</div>
            </div>
            <div class="stats-cell">
              <div class="cell-value">{{ statistics.scoreCount || 0 }}</div>
              <div class="cell-label">成绩记录数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：时间信息 -->
      <el-col :span="16">
        <el-card class="config-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Clock /></el-icon>
                时间信息
              </span>
            </div>
          </template>

          <el-tabs v-model="activeTab" type="border-card" stretch>
            <!-- 选课阶段 -->
            <el-tab-pane name="selection">
              <template #label>
                <span class="tab-label" :class="{ 'is-active': activeTab === 'selection' }">
                  <el-icon><UserFilled /></el-icon>
                  选课阶段
                </span>
              </template>
              <div class="tab-content">
                <div class="time-list">
                  <div class="time-item">
                    <div class="time-label"><el-icon><VideoPlay /></el-icon> 开始时间</div>
                    <div class="time-value" :class="{ empty: !phaseConfig.course_selection_start }">
                      {{ phaseConfig.course_selection_start || '未设置' }}
                    </div>
                  </div>
                  <div class="time-item">
                    <div class="time-label"><el-icon><VideoPause /></el-icon> 截止时间</div>
                    <el-button
                      v-if="currentPhase === 1"
                      type="primary"
                      link
                      @click="showEditDeadlineDialog('course_selection_end')"
                      size="small"
                    >
                      <el-icon><Edit /></el-icon>
                      重设
                    </el-button>
                    <div class="time-value" :class="{ empty: !phaseConfig.course_selection_end }">
                      {{ phaseConfig.course_selection_end || '未设置' }}
                    </div>
                  </div>
                </div>
                <div class="time-status">
                  <el-tag :type="statistics.isSelectionOpen ? 'success' : 'info'" effect="plain">
                    {{ statistics.isSelectionOpen ? '选课窗口开放中' : '选课窗口已关闭' }}
                  </el-tag>
                </div>
              </div>
            </el-tab-pane>

            <!-- 成绩录入阶段 -->
            <el-tab-pane name="score">
              <template #label>
                <span class="tab-label" :class="{ 'is-active': activeTab === 'score' }">
                  <el-icon><EditPen /></el-icon>
                  成绩录入
                </span>
              </template>
              <div class="tab-content">
                <div class="time-list">
                  <div class="time-item">
                    <div class="time-label"><el-icon><VideoPlay /></el-icon> 开始时间</div>
                    <div class="time-value" :class="{ empty: !phaseConfig.score_entry_start }">
                      {{ phaseConfig.score_entry_start || '未设置' }}
                    </div>
                  </div>
                  <div class="time-item">
                    <div class="time-label"><el-icon><VideoPause /></el-icon> 截止时间</div>
                    <el-button
                      v-if="currentPhase === 3"
                      type="primary"
                      link
                      @click="showEditDeadlineDialog('score_entry_end')"
                      size="small"
                    >
                      <el-icon><Edit /></el-icon>
                      重设
                    </el-button>
                    <div class="time-value" :class="{ empty: !phaseConfig.score_entry_end }">
                      {{ phaseConfig.score_entry_end || '未设置' }}
                    </div>
                  </div>
                </div>
                <div class="time-status">
                  <el-tag :type="statistics.isScoreEntryOpen ? 'success' : 'info'" effect="plain">
                    {{ statistics.isScoreEntryOpen ? '成绩录入窗口开放中' : '成绩录入窗口已关闭' }}
                  </el-tag>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <!-- 推进对话框 -->
    <el-dialog
      v-model="advanceDialogVisible"
      :title="advanceDialogTitle"
      width="500px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="dialog-advance-info">
        <div class="info-row">
          <span class="info-label">当前阶段</span>
          <el-tag :type="phaseTagType" size="large">{{ phaseText }}</el-tag>
          <el-icon class="arrow"><Right /></el-icon>
          <el-tag :type="nextPhaseTagType" size="large">{{ nextPhaseText }}</el-tag>
        </div>
      </div>
      <el-form :model="advanceForm" ref="advanceFormRef" label-width="120px">
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="advanceForm.startTime"
            type="datetime"
            disabled
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <!-- 阶段0→1和阶段2→3需要设置截止时间 -->
        <el-form-item
          v-if="currentPhase === 0 || currentPhase === 2"
          :label="currentPhase === 0 ? '选课截止时间' : '成绩录入截止时间'"
          prop="endTime"
          :rules="[{ required: true, message: '请选择截止时间', trigger: 'change' }]"
        >
          <el-date-picker
            v-model="advanceForm.endTime"
            type="datetime"
            :placeholder="currentPhase === 0 ? '请选择选课截止时间' : '请选择成绩录入截止时间'"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="advanceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdvanceConfirm" :loading="advanceLoading" round>
          <el-icon><Check /></el-icon>
          确认推进
        </el-button>
      </template>
    </el-dialog>

    <!-- 重置对话框 -->
    <el-dialog
      v-model="resetDialogVisible"
      title="重置课程阶段"
      width="450px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-alert type="warning" :closable="false" show-icon class="reset-warning">
        <template #title>
          <span>此操作将清空所有阶段时间配置，并将阶段重置为未开始状态！</span>
        </template>
      </el-alert>
      <template #footer>
        <el-button @click="resetDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleResetConfirm" :loading="resetLoading" round>
          <el-icon><RefreshLeft /></el-icon>
          确认重置
        </el-button>
      </template>
    </el-dialog>

    <!-- 重设截止时间对话框 -->
    <el-dialog
      v-model="editDeadlineDialogVisible"
      title="重设截止时间"
      width="450px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="editDeadlineForm" ref="editDeadlineFormRef" label-width="100px">
        <el-form-item label="截止时间" prop="deadline" :rules="[{ required: true, message: '请选择截止时间', trigger: 'change' }]">
          <el-date-picker
            v-model="editDeadlineForm.deadline"
            type="datetime"
            placeholder="请选择截止时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDeadlineDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditDeadlineConfirm" :loading="editDeadlineLoading" round>
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { getCurrentPhase, advancePhase, resetPhase, updateDeadline, getPhaseConfig, getPhaseStatistics } from "@/api/course/phase";
import { Timer, Setting, Promotion, WarningFilled, RefreshLeft, Right, UserFilled, EditPen, Reading, Clock, VideoPlay, VideoPause, DataAnalysis, Check, Edit, CircleClose, CircleCheck } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();

const currentPhase = ref(0);
const advanceLoading = ref(false);
const resetLoading = ref(false);
const resetDialogVisible = ref(false);
const advanceDialogVisible = ref(false);
const advanceFormRef = ref(null);
const activeTab = ref('selection');

const editDeadlineDialogVisible = ref(false);
const editDeadlineFormRef = ref(null);
const editDeadlineLoading = ref(false);
const editDeadlineForm = ref({
  configKey: '',
  deadline: ''
});

const phaseConfig = ref({
  course_phase_status: '0',
  course_selection_start: '',
  course_selection_end: '',
  score_entry_start: '',
  score_entry_end: ''
});

const statistics = ref({});

const advanceForm = ref({
  startTime: '',
  endTime: ''
});

// 阶段名称映射
const PHASE_NAMES = ['未开始', '选课阶段', '已开课', '成绩录入阶段', '已结课'];
const PHASE_TAG_TYPES = ['info', 'primary', 'success', 'warning', 'danger'];

const phaseText = computed(() => PHASE_NAMES[currentPhase.value] || '未知');
const phaseTagType = computed(() => PHASE_TAG_TYPES[currentPhase.value] || 'info');

const nextPhaseText = computed(() => {
  const next = currentPhase.value + 1;
  return PHASE_NAMES[next] || '已结课';
});

const nextPhaseTagType = computed(() => {
  const next = currentPhase.value + 1;
  return PHASE_TAG_TYPES[next] || 'info';
});

const advanceButtonText = computed(() => {
  const btnTexts = ['开启选课', '推进到已开课', '推进到成绩录入', '结课', '已结课'];
  return btnTexts[currentPhase.value] || '已结课';
});

const advanceButtonDesc = computed(() => {
  const descs = [
    '开启选课阶段，设置选课截止时间',
    '结束选课，进入已开课阶段',
    '进入成绩录入阶段，设置录入截止时间',
    '结束成绩录入，课程结课',
    '课程已结课，无法继续推进'
  ];
  return descs[currentPhase.value] || '';
});

const advanceDialogTitle = computed(() => {
  return `从"${phaseText.value}"推进到"${nextPhaseText.value}"`;
});

// 加载数据
const loadCurrentPhase = () => {
  getCurrentPhase().then(response => {
    currentPhase.value = Number(response.data) || 0;
    // 切换Tab到当前活跃阶段
    if (currentPhase.value === 1) {
      activeTab.value = 'selection';
    } else if (currentPhase.value === 3) {
      activeTab.value = 'score';
    }
  }).catch(() => {
    proxy.$modal.msgError('获取当前阶段失败');
  });
};

const loadPhaseConfig = () => {
  getPhaseConfig().then(response => {
    phaseConfig.value = { ...phaseConfig.value, ...response.data };
  }).catch(() => {
    proxy.$modal.msgError('获取阶段配置失败');
  });
};

const loadStatistics = () => {
  getPhaseStatistics().then(response => {
    statistics.value = response.data;
  }).catch(() => {
    console.error('获取统计信息失败');
  });
};

const loadAllData = () => {
  loadCurrentPhase();
  loadPhaseConfig();
  loadStatistics();
};

// 推进对话框
const showAdvanceDialog = () => {
  const now = new Date();
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
  advanceForm.value.startTime = now.toISOString().slice(0, 16).replace('T', ' ') + ':00';
  advanceForm.value.endTime = '';
  advanceDialogVisible.value = true;
};

const handleAdvanceConfirm = () => {
  advanceFormRef.value.validate(valid => {
    if (valid) {
      // 需要截止时间的阶段校验时间
      if (currentPhase.value === 0 || currentPhase.value === 2) {
        const start = new Date(advanceForm.value.startTime.replace(/-/g, '/'));
        const end = new Date(advanceForm.value.endTime.replace(/-/g, '/'));
        if (end <= start) {
          proxy.$modal.msgError('截止时间必须晚于开始时间');
          return;
        }
      }

      advanceLoading.value = true;
      advancePhase({ endTime: advanceForm.value.endTime }).then(() => {
        proxy.$modal.msgSuccess('推进成功');
        advanceDialogVisible.value = false;
        loadAllData();
      }).catch(() => {
        proxy.$modal.msgError('推进失败');
      }).finally(() => {
        advanceLoading.value = false;
      });
    }
  });
};

// 重置
const showResetConfirm = () => {
  resetDialogVisible.value = true;
};

const handleResetConfirm = () => {
  proxy.$modal.confirm('确定要重置课程阶段吗？此操作不可恢复！').then(() => {
    resetLoading.value = true;
    resetPhase().then(() => {
      proxy.$modal.msgSuccess('重置成功');
      resetDialogVisible.value = false;
      loadAllData();
    }).catch(() => {
      proxy.$modal.msgError('重置失败');
    }).finally(() => {
      resetLoading.value = false;
    });
  }).catch(() => {});
};

// 重设截止时间
const showEditDeadlineDialog = (configKey) => {
  editDeadlineForm.value.configKey = configKey;
  editDeadlineForm.value.deadline = phaseConfig.value[configKey] || '';
  editDeadlineDialogVisible.value = true;
};

const handleEditDeadlineConfirm = () => {
  editDeadlineFormRef.value.validate(valid => {
    if (valid) {
      editDeadlineLoading.value = true;
      updateDeadline({
        configKey: editDeadlineForm.value.configKey,
        configValue: editDeadlineForm.value.deadline
      }).then(() => {
        proxy.$modal.msgSuccess('截止时间修改成功');
        editDeadlineDialogVisible.value = false;
        loadAllData();
      }).catch(() => {
        proxy.$modal.msgError('修改失败');
      }).finally(() => {
        editDeadlineLoading.value = false;
      });
    }
  });
};

onMounted(() => {
  loadAllData();
});
</script>

<style scoped>
/* 卡片标题 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 顶部阶段信息 */
.phase-info-card {
  margin-bottom: 20px;
}

.stat-box {
  text-align: center;
  padding: 10px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  min-height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-wrapper {
  padding: 10px 0;
}

.progress-wrapper :deep(.el-steps--simple) {
  background: transparent;
}

/* 间距 */
.mt-20 {
  margin-top: 20px;
}

/* 操作卡片 */
.operation-card .section {
  padding: 12px 0;
}

.section-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.danger-section {
  background: linear-gradient(135deg, #fef0f0 0%, #fff5f5 100%);
  border-radius: 8px;
  padding: 12px !important;
  margin: 0 -20px;
  width: calc(100% + 40px);
}

.danger-title {
  color: #F56C6C !important;
}

.action-desc {
  color: #909399;
  font-size: 12px;
  margin-top: 10px;
  line-height: 1.5;
  text-align: center;
}

.action-desc.danger {
  color: #F56C6C;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.stats-cell {
  text-align: center;
  padding: 16px 8px;
  border-radius: 8px;
  background: #fafbfc;
}

.stats-cell .cell-value {
  font-size: 28px;
  font-weight: 700;
  color: #409eff;
  line-height: 1.2;
}

.stats-cell .cell-label {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}

/* 时间配置 */
.config-card :deep(.el-tabs--border-card) {
  border: none;
  box-shadow: none;
}

.config-card :deep(.el-tabs__header) {
  margin-bottom: 8px;
}

.config-card :deep(.el-tabs--border-card > .el-tabs__content) {
  padding: 0;
}

.config-card :deep(.el-card__body) {
  padding: 12px 16px 16px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0.7;
  transition: all 0.3s;
}

.tab-label.is-active,
.tab-label:hover {
  opacity: 1;
}

.tab-content {
  padding: 16px;
}

.time-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.time-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  transition: all 0.3s;
}

.time-item:hover {
  background: #f0f2f5;
}

.time-item .el-button {
  flex-shrink: 0;
  margin-left: 4px;
}

.time-label {
  width: 100px;
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.time-value {
  flex: 1;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  text-align: right;
}

.time-value.empty {
  color: #c0c4cc;
  font-style: italic;
}

.time-status {
  margin-top: 12px;
  text-align: center;
}

/* 对话框 */
.dialog-advance-info {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 10px;
  padding: 16px 20px;
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
}

.info-label {
  color: #606266;
  font-weight: 500;
}

.arrow {
  color: #909399;
}

/* 重置对话框 */
.reset-warning {
  margin-bottom: 20px;
}
</style>
