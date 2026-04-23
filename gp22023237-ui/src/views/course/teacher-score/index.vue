<template>
  <div class="app-container">
    <!-- 课程阶段状态横幅 -->
    <el-card class="phase-banner" :class="phaseBannerClass" shadow="hover">
      <div class="banner-content">
        <div class="banner-left">
          <el-icon :size="22"><Timer /></el-icon>
          <span class="banner-label">当前阶段：</span>
          <el-tag :type="phaseTagType" size="large" effect="dark" round>{{ phaseText }}</el-tag>
        </div>
        <div class="banner-right">
          <template v-if="scoreEntryOpen">
            <el-icon class="blink"><CircleCheck /></el-icon>
            <span class="banner-status open">成绩录入窗口开放中</span>
            <span v-if="phaseConfig.score_entry_end" class="banner-deadline">
              截止：{{ phaseConfig.score_entry_end }}
            </span>
          </template>
          <template v-else>
            <el-icon><CircleClose /></el-icon>
            <span class="banner-status closed">成绩录入窗口已关闭</span>
          </template>
        </div>
      </div>
    </el-card>

    <!-- 课程选择区 -->
    <el-card class="mb20" shadow="never">
      <el-form :inline="true" label-width="80px">
        <el-form-item label="选择课程">
          <el-select
            v-model="selectedCourseId"
            placeholder="请选择您教授的课程"
            clearable
            filterable
            style="width: 340px"
            @change="handleCourseChange"
          >
            <el-option
              v-for="course in teacherCourses"
              :key="course.id"
              :label="`${course.name}（${course.semester || ''}）`"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="selectedCourse">
          <el-tag type="primary" size="large">{{ selectedCourse.name }}</el-tag>
          <el-tag type="info" size="small" class="ml5">{{ selectedCourse.semester }}</el-tag>
          <el-tag type="warning" size="small" class="ml5">{{ selectedCourse.credit }}学分</el-tag>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 成绩统计 -->
    <el-card class="mb20" shadow="never" v-if="selectedCourseId">
      <div class="stats-container">
        <div class="stat-item stat-blue">
          <div class="stat-icon"><el-icon><User /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.enteredCount }}<span class="stat-unit"> / {{ stats.total }}人</span></div>
            <div class="stat-label">已录入</div>
          </div>
        </div>
        <div class="stat-item stat-green">
          <div class="stat-icon"><el-icon><TrendCharts /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.avgScore }}</div>
            <div class="stat-label">平均分</div>
          </div>
        </div>
        <div class="stat-item stat-orange">
          <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.passRate }}<span class="stat-unit">%</span></div>
            <div class="stat-label">及格率</div>
          </div>
        </div>
        <div class="stat-item stat-purple">
          <div class="stat-icon"><el-icon><Trophy /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.excellentRate }}<span class="stat-unit">%</span></div>
            <div class="stat-label">优秀率</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 操作工具栏 + 成绩表格 -->
    <el-card shadow="never" v-if="selectedCourseId">
      <template #header>
        <div class="card-header">
          <span class="card-title">学生成绩列表</span>
          <div class="card-header-right">
            <el-select v-model="queryGrade" placeholder="成绩等级" clearable style="width: 120px" @change="getList" size="default">
              <el-option label="优秀" value="A" />
              <el-option label="良好" value="B" />
              <el-option label="中等" value="C" />
              <el-option label="及格" value="D" />
              <el-option label="不及格" value="E" />
            </el-select>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索姓名/学号"
              clearable
              style="width: 180px"
              :prefix-icon="Search"
              @input="handleSearch"
              size="default"
            />
          </div>
        </div>
      </template>

      <!-- 工具栏 -->
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="Download" @click="handleDownloadStudentTemplate" :disabled="!scoreEntryOpen">下载学生模板</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="info" plain icon="Download" @click="handleDownloadBlankTemplate" :disabled="!scoreEntryOpen">下载空白模板</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="Upload" @click="handleImport" :disabled="!scoreEntryOpen">导入成绩</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="warning" plain icon="Download" @click="handleExport">导出成绩</el-button>
        </el-col>
      </el-row>

      <!-- 时间窗口提示 -->
      <el-alert
        v-if="!scoreEntryOpen"
        title="当前不在成绩录入时间窗口内"
        type="warning"
        :closable="false"
        show-icon
        class="mb8"
      >
        <template #default>
          成绩录入功能已禁用，请联系教学秘书开启成绩录入阶段。
        </template>
      </el-alert>

      <!-- 评价提示 -->
      <el-alert
        v-if="scoreEntryOpen && hasUnevaluatedStudents"
        title="部分学生尚未完成教学评价"
        type="info"
        :closable="false"
        show-icon
        class="mb8"
      >
        <template #default>
          标记为"未评价"的学生尚未提交教学评价，暂无法为其录入成绩。请提醒相关学生先完成教学评价。
        </template>
      </el-alert>

      <!-- 成绩表格 -->
      <el-table
        v-loading="loading"
        :data="filteredList"
        border
        style="width: 100%"
        :row-class-name="tableRowClassName"
      >
        <el-table-column label="序号" width="60" type="index" align="center" />
        <el-table-column label="学号" prop="studentNo" width="120" />
        <el-table-column label="姓名" prop="studentName" width="90" />
        <el-table-column label="平时成绩" width="100" align="center">
          <template #default="scope">
            <span>{{ scope.row.usualScore != null ? scope.row.usualScore : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="期末成绩" width="100" align="center">
          <template #default="scope">
            <span>{{ scope.row.examScore != null ? scope.row.examScore : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="总成绩" width="90" align="center">
          <template #default="scope">
            <span :class="getScoreClass(scope.row.totalScore)">
              {{ scope.row.totalScore != null ? scope.row.totalScore : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="等级" width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.grade" :type="getGradeTagType(scope.row.grade)" size="small">
              {{ getGradeLabel(scope.row.grade) }}
            </el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="录入状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.totalScore != null" type="success" size="small" effect="plain">已录入</el-tag>
            <el-tag v-else type="info" size="small" effect="plain">未录入</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评价状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.evaluated" type="success" size="small" effect="plain">已评价</el-tag>
            <el-tag v-else type="danger" size="small" effect="plain">未评价</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评语" prop="comment" min-width="120" :show-overflow-tooltip="true">
          <template #default="scope">
            <span>{{ scope.row.comment || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" prop="updateTime" width="160" align="center" />
        <el-table-column label="操作" align="center" width="80" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip v-if="!scope.row.evaluated && scoreEntryOpen" content="该学生尚未完成教学评价，无法录入成绩" placement="top">
              <el-button link type="info" icon="Edit" disabled></el-button>
            </el-tooltip>
            <el-button v-else link type="primary" icon="Edit" @click="handleUpdate(scope.row)" :disabled="!scoreEntryOpen"></el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 空状态 -->
    <el-card shadow="never" v-if="!selectedCourseId">
      <el-empty description="请先选择一门课程" :image-size="120">
        <template #image>
          <el-icon :size="80" color="#c0c4cc"><Document /></el-icon>
        </template>
      </el-empty>
    </el-card>

    <!-- 编辑成绩对话框 -->
    <el-dialog title="编辑成绩" v-model="editOpen" width="500px" append-to-body>
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="学号">
              <el-input v-model="editForm.studentNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input v-model="editForm.studentName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="平时成绩" prop="usualScore">
              <el-input-number v-model="editForm.usualScore" :min="0" :max="100" :precision="1" :step="1" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="期末成绩" prop="examScore">
              <el-input-number v-model="editForm.examScore" :min="0" :max="100" :precision="1" :step="1" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="平时权重">
              <el-input-number v-model="editForm.usualWeight" :min="0" :max="1" :precision="1" :step="0.1" style="width: 100%" controls-position="right" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="期末权重">
              <el-input-number v-model="editForm.examWeight" :min="0" :max="1" :precision="1" :step="0.1" style="width: 100%" controls-position="right" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="总成绩">
              <el-input :model-value="editForm.totalScore != null ? editForm.totalScore : '-'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="等级">
              <el-tag v-if="editForm.grade" :type="getGradeTagType(editForm.grade)">{{ getGradeLabel(editForm.grade) }}</el-tag>
              <span v-else class="text-muted">-</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="评语" prop="comment">
              <el-input v-model="editForm.comment" type="textarea" placeholder="请输入评语" :rows="3" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitEdit" :loading="editLoading">确 定</el-button>
          <el-button @click="editOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog title="导入成绩" v-model="importDialogVisible" width="680px" append-to-body>
      <el-alert title="导入提示" type="info" :closable="false" show-icon style="margin-bottom: 12px;">
        <template #default>
          <div>建议先下载"学生模板"（已预填学号姓名），按模板格式填写成绩后上传。也支持使用"空白模板"手动填写学号。仅支持 .xlsx / .xls 格式。</div>
        </template>
      </el-alert>
      <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 20px;">
        <template #default>
          <div>关键规则：学号必须存在；平时成绩和期末成绩范围0-100；同一学生同一课程不可重复导入。</div>
        </template>
      </el-alert>
      <el-upload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx,.xls"
        :auto-upload="false"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
        drag
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将Excel文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">仅支持 .xlsx / .xls 格式，单文件不超过10MB</div>
        </template>
      </el-upload>

      <!-- 导入结果展示 -->
      <div v-if="importResult" style="margin-top: 20px;">
        <el-divider content-position="left">导入结果</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="总记录数">{{ importResult.total }}</el-descriptions-item>
          <el-descriptions-item label="成功数量">
            <span style="color: #67C23A;">{{ importResult.successCount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="失败数量">
            <span style="color: #F56C6C;">{{ importResult.failCount }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <div v-if="importResult.failDetails && importResult.failDetails.length > 0" style="margin-top: 15px;">
          <el-alert title="失败详情（请修复后重试）" type="warning" :closable="false">
            <ul style="margin: 0; padding-left: 20px; max-height: 220px; overflow-y: auto;">
              <li v-for="(item, index) in importResult.failDetails" :key="index" style="margin-bottom: 8px;">
                第{{ item.row }}行（学号：{{ item.studentNo || '-' }}）
                <span> - {{ item.reason }}</span>
              </li>
            </ul>
          </el-alert>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitImport" :loading="importing" :disabled="!uploadFile">确认导入</el-button>
          <el-button @click="importDialogVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TeacherScore">
import { listCourseStudents, downloadTemplateWithStudents } from "@/api/course/teacherScore";
import { updateScore, importScore, downloadScoreTemplate, exportScore } from "@/api/course/score";
import { listCourse } from "@/api/course/course";
import { getCurrentPhase, isScoreEntryOpen, getPhaseConfig } from "@/api/course/phase";
import useUserStore from '@/store/modules/user';
import {
  Search, Document, UploadFilled, User, TrendCharts, CircleCheck, Trophy,
  Timer, CircleClose
} from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();

// ===== 状态 =====
const loading = ref(false);
const scoreList = ref([]);
const teacherCourses = ref([]);
const selectedCourseId = ref(null);
const selectedCourse = ref(null);
const searchKeyword = ref('');
const queryGrade = ref('');
const importing = ref(false);
const importDialogVisible = ref(false);
const uploadFile = ref(null);
const uploadRef = ref();
const importResult = ref(null);

// 课程阶段状态
const currentPhase = ref(0);
const scoreEntryOpen = ref(false);
const phaseConfig = ref({});

// 统计
const stats = ref({ enteredCount: 0, total: 0, avgScore: '-', passRate: '0.0', excellentRate: '0.0' });

// 编辑对话框
const editOpen = ref(false);
const editLoading = ref(false);
const editFormRef = ref();
const editForm = ref({});
const editRules = {
  usualScore: [{ required: true, message: '平时成绩不能为空', trigger: 'blur' }],
  examScore: [{ required: true, message: '期末成绩不能为空', trigger: 'blur' }]
};

// 定时器
let phaseTimer = null;

// ===== 计算属性 =====
const PHASE_NAMES = ['未开始', '选课阶段', '已开课', '成绩录入阶段', '已结课'];
const PHASE_TAG_TYPES = ['info', 'primary', 'success', 'warning', 'danger'];

const phaseText = computed(() => PHASE_NAMES[currentPhase.value] || '未知');
const phaseTagType = computed(() => PHASE_TAG_TYPES[currentPhase.value] || 'info');
const phaseBannerClass = computed(() => scoreEntryOpen.value ? 'banner-open' : 'banner-closed');

const filteredList = computed(() => {
  if (!searchKeyword.value) return scoreList.value;
  const kw = searchKeyword.value.toLowerCase();
  return scoreList.value.filter(row =>
    (row.studentNo && row.studentNo.toLowerCase().includes(kw)) ||
    (row.studentName && row.studentName.toLowerCase().includes(kw))
  );
});

const hasUnevaluatedStudents = computed(() => {
  return scoreList.value.some(row => !row.evaluated);
});

// ===== 数据加载 =====
function getTeacherCourses() {
  const userStore = useUserStore();
  const teacherId = userStore.roleInfo?.[0]?.id;
  if (!teacherId) return;
  listCourse({ teacherId }).then(res => {
    teacherCourses.value = res.data?.rows || res.data || [];
  });
}

function loadPhaseStatus() {
  getCurrentPhase().then(res => {
    currentPhase.value = Number(res.data) || 0;
  });
  isScoreEntryOpen().then(res => {
    scoreEntryOpen.value = res.data === true;
  }).catch(() => {
    scoreEntryOpen.value = false;
  });
  getPhaseConfig().then(res => {
    phaseConfig.value = res.data || {};
  });
}

function handleCourseChange(courseId) {
  if (courseId) {
    selectedCourse.value = teacherCourses.value.find(c => c.id === courseId) || null;
    importResult.value = null;
    getList();
  } else {
    selectedCourse.value = null;
    scoreList.value = [];
    stats.value = { enteredCount: 0, total: 0, avgScore: '-', passRate: '0.0', excellentRate: '0.0' };
  }
}

function getList() {
  if (!selectedCourseId.value) return;
  loading.value = true;
  listCourseStudents({
    courseId: selectedCourseId.value,
    keyword: searchKeyword.value || undefined,
    grade: queryGrade.value || undefined
  }).then(res => {
    const data = res.data;
    scoreList.value = data.rows || [];
    stats.value = {
      enteredCount: data.enteredCount || 0,
      total: data.total || 0,
      avgScore: data.avgScore || '-',
      passRate: data.passRate || '0.0',
      excellentRate: data.excellentRate || '0.0'
    };
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
}

function handleSearch() {
  getList();
}

// ===== 编辑 =====
function handleUpdate(row) {
  editForm.value = {
    id: row.scoreId,
    studentId: row.studentId,
    courseId: row.courseId,
    studentNo: row.studentNo,
    studentName: row.studentName,
    usualScore: row.usualScore,
    examScore: row.examScore,
    totalScore: row.totalScore,
    usualWeight: row.usualWeight || 0.3,
    examWeight: row.examWeight || 0.7,
    grade: row.grade,
    comment: row.comment || ''
  };
  editOpen.value = true;
}

function recalcEditForm() {
  const form = editForm.value;
  if (form.usualScore != null && form.examScore != null) {
    form.totalScore = Math.round((form.usualScore * form.usualWeight + form.examScore * form.examWeight) * 100) / 100;
    form.grade = calcGrade(form.totalScore);
  } else if (form.examScore != null) {
    form.totalScore = form.examScore;
    form.grade = calcGrade(form.totalScore);
  } else if (form.usualScore != null) {
    form.totalScore = form.usualScore;
    form.grade = calcGrade(form.totalScore);
  } else {
    form.totalScore = null;
    form.grade = null;
  }
}

watch(() => [editForm.value.usualScore, editForm.value.examScore], () => {
  if (editOpen.value) recalcEditForm();
});

function calcGrade(score) {
  if (score >= 90) return 'A';
  if (score >= 80) return 'B';
  if (score >= 70) return 'C';
  if (score >= 60) return 'D';
  return 'E';
}

function submitEdit() {
  proxy.$refs["editFormRef"].validate(valid => {
    if (valid) {
      editLoading.value = true;
      recalcEditForm();
      const data = { ...editForm.value };
      delete data.studentNo;
      delete data.studentName;
      updateScore(data).then(() => {
        editLoading.value = false;
        proxy.$modal.msgSuccess('修改成功');
        editOpen.value = false;
        getList();
      }).catch(() => {
        editLoading.value = false;
        proxy.$modal.msgError('修改失败，请重试');
      });
    }
  });
}

// ===== 下载模板 =====
function handleDownloadStudentTemplate() {
  const userStore = useUserStore();
  const teacherId = userStore.roleInfo?.[0]?.id;
  downloadTemplateWithStudents({ courseId: selectedCourseId.value, teacherId }).then(res => {
    downloadBlob(res, `${selectedCourse.value?.name || '成绩'}_学生模板.xlsx`);
    proxy.$modal.msgSuccess('学生模板下载成功');
  }).catch(() => {
    proxy.$modal.msgError('模板下载失败');
  });
}

function handleDownloadBlankTemplate() {
  downloadScoreTemplate().then(res => {
    downloadBlob(res, '成绩导入模板.xlsx');
    proxy.$modal.msgSuccess('空白模板下载成功');
  }).catch(() => {
    proxy.$modal.msgError('模板下载失败');
  });
}

function handleExport() {
  const userStore = useUserStore();
  const teacherId = userStore.roleInfo?.[0]?.id;
  exportScore({ courseId: selectedCourseId.value, teacherId }).then(res => {
    downloadBlob(res, `${selectedCourse.value?.name || '成绩'}_导出.xlsx`);
  }).catch(() => {
    proxy.$modal.msgError('导出失败');
  });
}

function downloadBlob(data, filename) {
  const blob = new Blob([data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}

// ===== 导入 =====
function handleImport() {
  importResult.value = null;
  uploadFile.value = null;
  if (uploadRef.value) uploadRef.value.clearFiles();
  importDialogVisible.value = true;
}

function handleFileChange(file) {
  const fileName = file?.name || '';
  const lowerName = fileName.toLowerCase();
  if (!lowerName.endsWith('.xls') && !lowerName.endsWith('.xlsx')) {
    proxy.$modal.msgWarning('只能上传 .xls 或 .xlsx 文件');
    uploadRef.value?.clearFiles();
    uploadFile.value = null;
    return;
  }
  if ((file.size || 0) / 1024 / 1024 > 10) {
    proxy.$modal.msgWarning('文件大小不能超过10MB');
    uploadRef.value?.clearFiles();
    uploadFile.value = null;
    return;
  }
  uploadFile.value = file.raw;
}

function handleExceed() {
  proxy.$modal.msgWarning('只能上传一个文件');
}

function submitImport() {
  if (!uploadFile.value) {
    proxy.$modal.msgWarning('请选择要上传的文件');
    return;
  }
  const userStore = useUserStore();
  const teacherId = userStore.roleInfo?.[0]?.id;
  importing.value = true;
  const formData = new FormData();
  formData.append('file', uploadFile.value);
  formData.append('courseId', selectedCourseId.value);
  formData.append('teacherId', teacherId);
  importScore(formData).then(res => {
    importing.value = false;
    importResult.value = res.data;
    if (importResult.value.failCount > 0) {
      proxy.$modal.msgWarning(`导入完成：成功 ${importResult.value.successCount} 条，失败 ${importResult.value.failCount} 条`);
    } else {
      proxy.$modal.msgSuccess('导入完成，全部成功');
    }
    if (importResult.value.successCount > 0) getList();
  }).catch(() => {
    importing.value = false;
    proxy.$modal.msgError('导入失败，请检查文件格式');
  });
}

// ===== 辅助方法 =====
function tableRowClassName({ row }) {
  if (!row.evaluated) return 'unevaluated-row';
  return row.totalScore == null ? 'unentered-row' : '';
}

function getGradeTagType(grade) {
  const typeMap = { A: 'success', B: 'primary', C: 'warning', D: 'info', E: 'danger' };
  return typeMap[grade] || 'info';
}

function getGradeLabel(grade) {
  const labelMap = { A: '优秀', B: '良好', C: '中等', D: '及格', E: '不及格' };
  return labelMap[grade] || grade;
}

function getScoreClass(score) {
  if (score == null) return 'text-muted';
  if (score >= 90) return 'score-excellent';
  if (score >= 80) return 'score-good';
  if (score >= 70) return 'score-medium';
  if (score >= 60) return 'score-pass';
  return 'score-fail';
}

// ===== 初始化 =====
getTeacherCourses();
loadPhaseStatus();

// 每60秒刷新阶段状态
phaseTimer = setInterval(loadPhaseStatus, 60000);

onBeforeUnmount(() => {
  if (phaseTimer) clearInterval(phaseTimer);
});
</script>

<style scoped>
/* 阶段横幅 */
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

.banner-status.open {
  color: #67C23A;
}

.banner-status.closed {
  color: #E6A23C;
}

.banner-deadline {
  color: #909399;
  font-size: 13px;
  margin-left: 4px;
}

.blink {
  animation: blink 2s infinite;
  color: #67C23A;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

/* 间距 */
.mb8 {
  margin-bottom: 8px;
}

.mb20 {
  margin-bottom: 20px;
}

.ml5 {
  margin-left: 5px;
}

/* 统计区域 */
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

.stat-blue .stat-icon { background: rgba(64, 158, 255, 0.12); color: #409EFF; }
.stat-green .stat-icon { background: rgba(103, 194, 58, 0.12); color: #67C23A; }
.stat-orange .stat-icon { background: rgba(230, 162, 60, 0.12); color: #E6A23C; }
.stat-purple .stat-icon { background: rgba(144, 116, 255, 0.12); color: #9074FF; }

.stat-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-unit {
  font-size: 13px;
  font-weight: 400;
}

.stat-blue .stat-value { color: #409EFF; }
.stat-green .stat-value { color: #67C23A; }
.stat-orange .stat-value { color: #E6A23C; }
.stat-purple .stat-value { color: #9074FF; }

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
}

.card-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 成绩颜色 */
.score-excellent { color: #67C23A; font-weight: bold; }
.score-good { color: #409EFF; font-weight: bold; }
.score-medium { color: #E6A23C; font-weight: bold; }
.score-pass { color: #909399; font-weight: bold; }
.score-fail { color: #F56C6C; font-weight: bold; }
.text-muted { color: #C0C4CC; }

/* 行样式 */
:::deep(.unentered-row) {
  background-color: #fafbfc !important;
}

:::deep(.unevaluated-row) {
  background-color: #fef0f0 !important;
}

/* 响应式 */
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

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .card-header-right {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
