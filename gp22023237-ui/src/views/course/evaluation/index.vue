<template>
  <div class="app-container">
    <!-- 课程阶段状态横幅 -->
    <el-card class="phase-banner" :class="scoreEntryOpen ? 'banner-open' : 'banner-closed'" shadow="hover">
      <div class="banner-content">
        <div class="banner-left">
          <el-icon :size="22"><Timer /></el-icon>
          <span class="banner-label">当前阶段：</span>
          <el-tag :type="scoreEntryOpen ? 'warning' : 'info'" size="large" effect="dark" round>
            {{ scoreEntryOpen ? '成绩录入阶段' : '非评价阶段' }}
          </el-tag>
        </div>
        <div class="banner-right">
          <template v-if="scoreEntryOpen">
            <el-icon class="blink"><CircleCheck /></el-icon>
            <span class="banner-status open">可提交教学评价</span>
          </template>
          <template v-else>
            <el-icon><CircleClose /></el-icon>
            <span class="banner-status closed">当前不可提交评价</span>
          </template>
        </div>
      </div>
    </el-card>

    <!-- 统计信息 -->
    <el-card class="mb20" shadow="never">
      <div class="stats-container">
        <div class="stat-item stat-orange">
          <div class="stat-icon"><el-icon><Bell /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ pendingCount }}<span class="stat-unit">门</span></div>
            <div class="stat-label">待评价</div>
          </div>
        </div>
        <div class="stat-item stat-green">
          <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ completedCount }}<span class="stat-unit">门</span></div>
            <div class="stat-label">已评价</div>
          </div>
        </div>
        <div class="stat-item stat-blue">
          <div class="stat-icon"><el-icon><Document /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ pendingCount + completedCount }}<span class="stat-unit">门</span></div>
            <div class="stat-label">总课程</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 课程评价列表 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">课程教学评价</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 待评价 -->
        <el-tab-pane label="待评价" name="pending">
          <el-alert
            v-if="scoreEntryOpen && pendingCourses.length > 0"
            title="请在成绩录入阶段完成教学评价，未完成评价的课程，教师将无法为您录入成绩。"
            type="warning"
            :closable="false"
            show-icon
            class="mb8"
          />
          <el-table :data="pendingCourses" border style="width: 100%" v-loading="loading">
            <el-table-column label="序号" width="60" type="index" align="center" />
            <el-table-column label="课程名称" prop="courseName" min-width="150" />
            <el-table-column label="授课教师" prop="teacherName" width="120" align="center" />
            <el-table-column label="学分" prop="credit" width="80" align="center" />
            <el-table-column label="学期" prop="semester" width="160" />
            <el-table-column label="状态" width="100" align="center">
              <template #default>
                <el-tag type="danger" size="small" effect="plain">待评价</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="scope">
                <el-button
                  type="primary"
                  size="small"
                  @click="handleEvaluate(scope.row)"
                  :disabled="!scoreEntryOpen"
                >
                  评价
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && pendingCourses.length === 0" description="暂无待评价课程" />
        </el-tab-pane>

        <!-- 已评价 -->
        <el-tab-pane label="已评价" name="completed">
          <el-table :data="completedCourses" border style="width: 100%" v-loading="loading">
            <el-table-column label="序号" width="60" type="index" align="center" />
            <el-table-column label="课程名称" prop="courseName" min-width="150" />
            <el-table-column label="授课教师" prop="teacherName" width="120" align="center" />
            <el-table-column label="综合评分" width="100" align="center">
              <template #default="scope">
                <span class="score-highlight">{{ scope.row.overallScore }}</span>
              </template>
            </el-table-column>
            <el-table-column label="评价时间" prop="evaluateTime" width="170" align="center" />
            <el-table-column label="状态" width="100" align="center">
              <template #default>
                <el-tag type="success" size="small" effect="plain">已评价</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="scope">
                <el-button type="info" size="small" link @click="handleViewDetail(scope.row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && completedCourses.length === 0" description="暂无已评价课程" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 评价弹窗 -->
    <el-dialog title="教学评价" v-model="evalDialogVisible" width="560px" append-to-body :close-on-click-modal="false">
      <div v-if="evalCourse" class="eval-dialog-content">
        <div class="eval-course-info">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="课程名称">{{ evalCourse.courseName }}</el-descriptions-item>
            <el-descriptions-item label="授课教师">{{ evalCourse.teacherName }}</el-descriptions-item>
            <el-descriptions-item label="学分">{{ evalCourse.credit }}</el-descriptions-item>
            <el-descriptions-item label="学期">{{ evalCourse.semester }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <el-divider content-position="left">评分（1-5星）</el-divider>

        <el-form :model="evalForm" ref="evalFormRef" label-width="90px" :rules="evalRules">
          <el-form-item label="教学态度" prop="attitudeScore">
            <div class="rate-wrapper">
              <el-rate v-model="evalForm.attitudeScore" :texts="rateTexts" show-text />
            </div>
          </el-form-item>
          <el-form-item label="教学内容" prop="contentScore">
            <div class="rate-wrapper">
              <el-rate v-model="evalForm.contentScore" :texts="rateTexts" show-text />
            </div>
          </el-form-item>
          <el-form-item label="教学方法" prop="methodScore">
            <div class="rate-wrapper">
              <el-rate v-model="evalForm.methodScore" :texts="rateTexts" show-text />
            </div>
          </el-form-item>
          <el-form-item label="教学效果" prop="effectScore">
            <div class="rate-wrapper">
              <el-rate v-model="evalForm.effectScore" :texts="rateTexts" show-text />
            </div>
          </el-form-item>
          <el-form-item label="文字评语">
            <el-input
              v-model="evalForm.comment"
              type="textarea"
              :rows="3"
              placeholder="请输入对课程教学的意见或建议（选填）"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitEval" :loading="submitLoading">提交评价</el-button>
          <el-button @click="evalDialogVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog title="评价详情" v-model="detailDialogVisible" width="560px" append-to-body>
      <div v-if="detailCourse" class="detail-dialog-content">
        <el-descriptions :column="2" border size="small" class="mb16">
          <el-descriptions-item label="课程名称">{{ detailCourse.courseName }}</el-descriptions-item>
          <el-descriptions-item label="授课教师">{{ detailCourse.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="综合评分">
            <span class="score-highlight">{{ detailCourse.overallScore }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="评价时间">{{ detailCourse.evaluateTime }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">评分详情</el-divider>

        <div class="detail-scores">
          <div class="detail-score-item">
            <span class="detail-score-label">教学态度</span>
            <el-rate v-model="detailCourse.attitudeScore" disabled />
            <span class="detail-score-value">{{ detailCourse.attitudeScore }}分</span>
          </div>
          <div class="detail-score-item">
            <span class="detail-score-label">教学内容</span>
            <el-rate v-model="detailCourse.contentScore" disabled />
            <span class="detail-score-value">{{ detailCourse.contentScore }}分</span>
          </div>
          <div class="detail-score-item">
            <span class="detail-score-label">教学方法</span>
            <el-rate v-model="detailCourse.methodScore" disabled />
            <span class="detail-score-value">{{ detailCourse.methodScore }}分</span>
          </div>
          <div class="detail-score-item">
            <span class="detail-score-label">教学效果</span>
            <el-rate v-model="detailCourse.effectScore" disabled />
            <span class="detail-score-value">{{ detailCourse.effectScore }}分</span>
          </div>
        </div>

        <div v-if="detailCourse.comment" class="detail-comment">
          <el-divider content-position="left">文字评语</el-divider>
          <p>{{ detailCourse.comment }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="TeachingEvaluation">
import { listPendingCourses, listCompletedCourses, submitEvaluation } from "@/api/course/evaluation";
import { isScoreEntryOpen, getCurrentPhase } from "@/api/course/phase";
import useUserStore from '@/store/modules/user';
import { Timer, CircleCheck, CircleClose, Bell, Document } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();

// ===== 状态 =====
const loading = ref(false);
const activeTab = ref('pending');
const pendingCourses = ref([]);
const completedCourses = ref([]);
const scoreEntryOpen = ref(false);

// 评价弹窗
const evalDialogVisible = ref(false);
const submitLoading = ref(false);
const evalCourse = ref(null);
const evalFormRef = ref();
const evalForm = ref({
  attitudeScore: 0,
  contentScore: 0,
  methodScore: 0,
  effectScore: 0,
  comment: ''
});

const rateTexts = ['很差', '较差', '一般', '较好', '很好'];

const evalRules = {
  attitudeScore: [{ required: true, message: '请对教学态度评分', trigger: 'change', validator: (rule, value, callback) => { value > 0 ? callback() : callback(new Error('请对教学态度评分')); } }],
  contentScore: [{ required: true, message: '请对教学内容评分', trigger: 'change', validator: (rule, value, callback) => { value > 0 ? callback() : callback(new Error('请对教学内容评分')); } }],
  methodScore: [{ required: true, message: '请对教学方法评分', trigger: 'change', validator: (rule, value, callback) => { value > 0 ? callback() : callback(new Error('请对教学方法评分')); } }],
  effectScore: [{ required: true, message: '请对教学效果评分', trigger: 'change', validator: (rule, value, callback) => { value > 0 ? callback() : callback(new Error('请对教学效果评分')); } }]
};

// 查看详情弹窗
const detailDialogVisible = ref(false);
const detailCourse = ref(null);

// ===== 计算属性 =====
const pendingCount = computed(() => pendingCourses.value.length);
const completedCount = computed(() => completedCourses.value.length);

// ===== 方法 =====
function getStudentId() {
  const userStore = useUserStore();
  return userStore.roleInfo?.[0]?.id;
}

function loadPhaseStatus() {
  isScoreEntryOpen().then(res => {
    scoreEntryOpen.value = res.data === true;
  }).catch(() => {
    scoreEntryOpen.value = false;
  });
}

function loadPendingCourses() {
  const studentId = getStudentId();
  if (!studentId) return;
  loading.value = true;
  listPendingCourses(studentId).then(res => {
    pendingCourses.value = res.data || [];
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
}

function loadCompletedCourses() {
  const studentId = getStudentId();
  if (!studentId) return;
  loading.value = true;
  listCompletedCourses(studentId).then(res => {
    completedCourses.value = res.data || [];
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
}

function handleTabChange(tab) {
  if (tab === 'pending') {
    loadPendingCourses();
  } else {
    loadCompletedCourses();
  }
}

function handleEvaluate(course) {
  evalCourse.value = course;
  evalForm.value = {
    attitudeScore: 0,
    contentScore: 0,
    methodScore: 0,
    effectScore: 0,
    comment: ''
  };
  evalDialogVisible.value = true;
}

function submitEval() {
  proxy.$refs["evalFormRef"].validate(valid => {
    if (valid) {
      submitLoading.value = true;
      const studentId = getStudentId();
      submitEvaluation({
        studentId,
        courseId: evalCourse.value.courseId,
        attitudeScore: evalForm.value.attitudeScore,
        contentScore: evalForm.value.contentScore,
        methodScore: evalForm.value.methodScore,
        effectScore: evalForm.value.effectScore,
        comment: evalForm.value.comment
      }).then(() => {
        submitLoading.value = false;
        proxy.$modal.msgSuccess('评价提交成功');
        evalDialogVisible.value = false;
        loadPendingCourses();
        if (activeTab.value === 'completed') {
          loadCompletedCourses();
        }
      }).catch(() => {
        submitLoading.value = false;
        proxy.$modal.msgError('评价提交失败，请重试');
      });
    }
  });
}

function handleViewDetail(course) {
  detailCourse.value = { ...course };
  detailDialogVisible.value = true;
}

// ===== 初始化 =====
loadPhaseStatus();
loadPendingCourses();
loadCompletedCourses();
</script>

<style scoped>
.phase-banner {
  margin-bottom: 20px;
  border: none;
  transition: all 0.3s;
}

.phase-banner.banner-open {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
}

.phase-banner.banner-closed {
  background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
}

.phase-banner :deep(.el-card__body) {
  padding: 12px 20px;
}

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.banner-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.banner-label {
  font-weight: 500;
  color: #606266;
}

.banner-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.banner-status {
  font-weight: 600;
  font-size: 14px;
}

.banner-status.open { color: #67C23A; }
.banner-status.closed { color: #E6A23C; }

.blink {
  animation: blink 2s infinite;
  color: #67C23A;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.mb8 { margin-bottom: 8px; }
.mb16 { margin-bottom: 16px; }
.mb20 { margin-bottom: 20px; }

.stats-container {
  display: flex;
  justify-content: space-around;
  padding: 10px 0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.stat-orange .stat-icon { background: rgba(230, 162, 60, 0.12); color: #E6A23C; }
.stat-green .stat-icon { background: rgba(103, 194, 58, 0.12); color: #67C23A; }
.stat-blue .stat-icon { background: rgba(64, 158, 255, 0.12); color: #409EFF; }

.stat-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-unit {
  font-size: 13px;
  font-weight: 400;
}

.stat-orange .stat-value { color: #E6A23C; }
.stat-green .stat-value { color: #67C23A; }
.stat-blue .stat-value { color: #409EFF; }

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
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

.score-highlight {
  color: #409EFF;
  font-weight: bold;
  font-size: 16px;
}

.rate-wrapper {
  padding-top: 4px;
}

.detail-scores {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-score-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-score-label {
  width: 80px;
  font-size: 14px;
  color: #606266;
  text-align: right;
}

.detail-score-value {
  font-size: 14px;
  color: #409EFF;
  font-weight: 500;
}

.detail-comment p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
}

@media (max-width: 768px) {
  .stats-container {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .banner-content {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
