<template>
  <div class="app-container">
    <!-- 课程选择区 -->
    <el-card class="mb20" shadow="never">
      <el-form :inline="true" label-width="80px">
        <el-form-item label="选择课程">
          <el-select
            v-model="selectedCourseId"
            placeholder="请选择您教授的课程"
            clearable
            filterable
            style="width: 320px"
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
          <el-tag type="success" size="small" class="ml5">{{ selectedCourse.courseNo }}</el-tag>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 成绩统计 -->
    <el-card class="mb20" shadow="never" v-if="selectedCourseId && scoreList.length > 0">
      <div class="stats-container">
        <div class="stat-item stat-blue">
          <div class="stat-icon"><el-icon><User /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.enteredCount }}<span class="stat-unit"> / {{ stats.totalCount }}人</span></div>
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
          <span class="card-title">成绩列表</span>
          <div class="card-header-right">
            <el-select v-model="queryParams.grade" placeholder="成绩等级" clearable style="width: 120px" @change="getList" size="default">
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
          <el-button type="primary" plain icon="Upload" @click="handleImport" :disabled="!scoreEntryOpen">导入成绩</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="info" plain icon="Download" @click="handleDownloadTemplate" :disabled="!scoreEntryOpen">下载模板</el-button>
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

      <!-- 成绩表格 -->
      <el-table
        v-loading="loading"
        :data="filteredScoreList"
        border
        style="width: 100%"
        :row-class-name="tableRowClassName"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
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
        <el-table-column label="评语" prop="comment" min-width="160" :show-overflow-tooltip="true">
          <template #default="scope">
            <span>{{ scope.row.comment || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" prop="updateTime" width="160" align="center" />
        <el-table-column label="操作" align="center" width="80" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" :disabled="!scoreEntryOpen"></el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
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
          <el-button @click="cancelEdit">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog title="导入成绩" v-model="importDialogVisible" width="680px" append-to-body>
      <el-alert title="导入提示" type="info" :closable="false" show-icon style="margin-bottom: 12px;">
        <template #default>
          <div>请先下载模板，按模板格式填写成绩数据后上传。仅支持 .xlsx / .xls 格式。</div>
          <el-button link type="primary" @click="handleDownloadTemplate">点击下载导入模板</el-button>
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
          <div class="el-upload__tip">
            仅支持 .xlsx / .xls 格式，单文件不超过10MB
          </div>
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

<script setup name="Score">
import { listScoreWithDetails, updateScore, importScore, downloadScoreTemplate, exportScore } from "@/api/course/score";
import { listCourse } from "@/api/course/course";
import { isScoreEntryOpen } from "@/api/course/phase";
import useUserStore from '@/store/modules/user';
import { Search, Document, UploadFilled, User, TrendCharts, CircleCheck, Trophy } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();

// 状态
const loading = ref(false);
const scoreList = ref([]);
const total = ref(0);
const teacherCourses = ref([]);
const selectedCourseId = ref(null);
const selectedCourse = ref(null);
const searchKeyword = ref('');
const importing = ref(false);
const importDialogVisible = ref(false);
const uploadFile = ref(null);
const uploadRef = ref();
const importResult = ref(null);

// 成绩录入时间窗口状态
const scoreEntryOpen = ref(false);

// 编辑对话框
const editOpen = ref(false);
const editLoading = ref(false);
const editFormRef = ref();
const editForm = ref({});

const editRules = {
  usualScore: [{ required: true, message: '平时成绩不能为空', trigger: 'blur' }],
  examScore: [{ required: true, message: '期末成绩不能为空', trigger: 'blur' }]
};

// 查询参数
const queryParams = ref({
  pageNum: 1,
  pageSize: 20,
  courseId: null,
  grade: undefined,
  teacherId: null
});

// 统计数据
const stats = computed(() => {
  const list = scoreList.value;
  if (!list || list.length === 0) {
    return { enteredCount: 0, totalCount: 0, avgScore: '-', passRate: '0.0', excellentRate: '0.0' };
  }
  const entered = list.filter(s => s.totalScore != null);
  const avg = entered.length > 0 ? (entered.reduce((sum, s) => sum + s.totalScore, 0) / entered.length).toFixed(1) : '-';
  const passCount = entered.filter(s => s.totalScore >= 60).length;
  const excellentCount = entered.filter(s => s.totalScore >= 90).length;
  const passRate = entered.length > 0 ? ((passCount / entered.length) * 100).toFixed(1) : '0.0';
  const excellentRate = entered.length > 0 ? ((excellentCount / entered.length) * 100).toFixed(1) : '0.0';
  return {
    enteredCount: entered.length,
    totalCount: list.length,
    avgScore: avg,
    passRate,
    excellentRate
  };
});

// 搜索过滤
const filteredScoreList = computed(() => {
  if (!searchKeyword.value) return scoreList.value;
  const kw = searchKeyword.value.toLowerCase();
  return scoreList.value.filter(row =>
    (row.studentNo && row.studentNo.toLowerCase().includes(kw)) ||
    (row.studentName && row.studentName.toLowerCase().includes(kw))
  );
});

// 获取教师课程列表
function getTeacherCourses() {
  const userStore = useUserStore();
  const roleInfo = userStore.roleInfo;
  if (!roleInfo) return;

  const teacherId = roleInfo.id;
  queryParams.value.teacherId = teacherId;

  listCourse({ teacherId: teacherId }).then(res => {
    teacherCourses.value = res.data || [];
  });
}

// 课程切换
function handleCourseChange(courseId) {
  if (courseId) {
    selectedCourse.value = teacherCourses.value.find(c => c.id === courseId) || null;
    queryParams.value.courseId = courseId;
    queryParams.value.pageNum = 1;
    importResult.value = null;
    getList();
  } else {
    selectedCourse.value = null;
    scoreList.value = [];
    total.value = 0;
  }
}

// 获取成绩列表
function getList() {
  if (!selectedCourseId.value) return;
  loading.value = true;
  listScoreWithDetails(queryParams.value).then(res => {
    if (res.data && res.data.rows) {
      scoreList.value = res.data.rows || [];
      total.value = res.data.total || 0;
    } else {
      scoreList.value = res.data || [];
      total.value = scoreList.value.length;
    }
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
}

// 编辑成绩
function handleUpdate(row) {
  editForm.value = {
    id: row.id,
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
    comment: row.comment || '',
    teacherId: row.teacherId
  };
  editOpen.value = true;
}

// 计算成绩
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

// 监听平时/期末成绩变化自动计算
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

function cancelEdit() {
  editOpen.value = false;
  editForm.value = {};
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

// 搜索
function handleSearch() {
  // 前端过滤，无需重新请求
}

// 导入
function handleImport() {
  importResult.value = null;
  uploadFile.value = null;
  if (uploadRef.value) {
    uploadRef.value.clearFiles();
  }
  importDialogVisible.value = true;
}

function handleFileChange(file) {
  const fileName = file?.name || '';
  const lowerName = fileName.toLowerCase();
  const isExcel = lowerName.endsWith('.xls') || lowerName.endsWith('.xlsx');
  if (!isExcel) {
    proxy.$modal.msgWarning('只能上传 .xls 或 .xlsx 文件');
    uploadRef.value?.clearFiles();
    uploadFile.value = null;
    return;
  }
  const maxSizeMb = 10;
  if ((file.size || 0) / 1024 / 1024 > maxSizeMb) {
    proxy.$modal.msgWarning(`文件大小不能超过 ${maxSizeMb}MB`);
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
  const teacherId = userStore.roleInfo?.id;

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
    if (importResult.value.successCount > 0) {
      getList();
    }
  }).catch(() => {
    importing.value = false;
    proxy.$modal.msgError('导入失败，请检查文件格式');
  });
}

// 下载模板
function handleDownloadTemplate() {
  downloadScoreTemplate().then(res => {
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = '成绩导入模板.xlsx';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    proxy.$modal.msgSuccess('模板下载成功');
  }).catch(() => {
    proxy.$modal.msgError('模板下载失败');
  });
}

// 导出成绩
function handleExport() {
  const userStore = useUserStore();
  const teacherId = userStore.roleInfo?.id;
  exportScore({ courseId: selectedCourseId.value, teacherId }).then(res => {
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${selectedCourse.value?.name || '成绩'}_导出.xlsx`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }).catch(() => {
    proxy.$modal.msgError('导出失败');
  });
}

// 表格行样式
function tableRowClassName({ row }) {
  if (row.totalScore == null) return 'unentered-row';
  return '';
}

// 成绩等级标签
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

function handleSelectionChange(selection) {
  // 预留批量操作
}

// 检查成绩录入时间窗口
function checkScoreEntryOpen() {
  isScoreEntryOpen().then(res => {
    scoreEntryOpen.value = res.data === true;
  }).catch(() => {
    scoreEntryOpen.value = false;
  });
}

// 初始化
getTeacherCourses();
checkScoreEntryOpen();
</script>

<style scoped>
/* 间距 */
.mb8 {
  margin-bottom: 8px;
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
::deep(.unentered-row) {
  background-color: #f5f7fa !important;
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-container {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
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
