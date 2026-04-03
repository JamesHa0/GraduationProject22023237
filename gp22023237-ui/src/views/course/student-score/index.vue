<template>
  <div class="app-container">
    <!-- 学期选择 -->
    <el-card class="mb20" shadow="never">
      <el-form :inline="true">
        <el-form-item label="学期">
          <el-select v-model="currentSemester" placeholder="全部学期" clearable style="width: 220px">
            <el-option
              v-for="item in semesterOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="成绩等级">
          <el-select v-model="queryGrade" placeholder="全部等级" clearable style="width: 120px">
            <el-option label="优秀" value="A" />
            <el-option label="良好" value="B" />
            <el-option label="中等" value="C" />
            <el-option label="及格" value="D" />
            <el-option label="不及格" value="E" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 成绩统计 -->
    <el-card class="mb20" shadow="never" v-if="scoreList.length > 0">
      <div class="stats-container">
        <div class="stat-item">
          <div class="stat-value">{{ totalCourses }}</div>
          <div class="stat-label">已修课程</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ avgScore }}</div>
          <div class="stat-label">平均成绩</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ totalCredits }}</div>
          <div class="stat-label">总学分</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ passRate }}%</div>
          <div class="stat-label">及格率</div>
        </div>
      </div>
    </el-card>

    <!-- 成绩列表 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">我的成绩</span>
        </div>
      </template>
      <el-empty v-if="!loading && scoreList.length === 0" description="暂无成绩数据" class="py20" />
      <el-table v-else :data="scoreList" border style="width: 100%">
        <el-table-column label="序号" width="60" type="index" align="center" />
        <el-table-column label="学期" prop="semester" width="160" />
        <el-table-column label="课程编号" prop="courseNo" width="120" />
        <el-table-column label="课程名称" prop="courseName" min-width="150" />
        <el-table-column label="学分" prop="credit" width="80" align="center" />
        <el-table-column label="学时" prop="hours" width="80" align="center" />
        <el-table-column label="修读性质" prop="studyNature" width="100" />
        <el-table-column label="授课教师" prop="teacherName" width="120" />
        <el-table-column label="平时成绩" prop="usualScore" width="90" align="center">
          <template #default="scope">
            {{ scope.row.usualScore != null ? scope.row.usualScore : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="期末成绩" prop="examScore" width="90" align="center">
          <template #default="scope">
            {{ scope.row.examScore != null ? scope.row.examScore : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="总成绩" prop="totalScore" width="90" align="center">
          <template #default="scope">
            <span :class="getScoreClass(scope.row.totalScore)">
              {{ scope.row.totalScore != null ? scope.row.totalScore : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="成绩等级" prop="grade" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.grade" :type="getGradeTagType(scope.row.grade)" size="small">
              {{ getGradeLabel(scope.row.grade) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评语" prop="comment" min-width="150" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="StudentScore">
import { getMyScores } from "@/api/course/score";
import useUserStore from '@/store/modules/user';

const { proxy } = getCurrentInstance();

// 当前学期
const currentSemester = ref('');

// 查询的成绩等级
const queryGrade = ref('');

// 加载状态
const loading = ref(true);

// 学期选项列表
const semesterOptions = ref([]);

// 成绩列表
const scoreList = ref([]);

// 计算统计数据
const totalCourses = computed(() => scoreList.value.length);

const avgScore = computed(() => {
  if (scoreList.value.length === 0) return '-';
  const total = scoreList.value.reduce((sum, item) => sum + (item.totalScore || 0), 0);
  return (total / scoreList.value.length).toFixed(2);
});

const totalCredits = computed(() => {
  return scoreList.value.reduce((sum, item) => sum + (item.credit || 0), 0);
});

const passRate = computed(() => {
  if (scoreList.value.length === 0) return 0;
  const passCount = scoreList.value.filter(item => item.totalScore != null && item.totalScore >= 60).length;
  return ((passCount / scoreList.value.length) * 100).toFixed(1);
});

// 成绩等级标签类型
function getGradeTagType(grade) {
  const typeMap = { A: "success", B: "primary", C: "warning", D: "info", E: "danger" };
  return typeMap[grade] || "info";
}

// 成绩等级标签文字
function getGradeLabel(grade) {
  const labelMap = { A: "优秀", B: "良好", C: "中等", D: "及格", E: "不及格" };
  return labelMap[grade] || grade;
}

// 成绩颜色类
function getScoreClass(score) {
  if (score == null) return '';
  if (score >= 90) return 'score-excellent';
  if (score >= 80) return 'score-good';
  if (score >= 70) return 'score-medium';
  if (score >= 60) return 'score-pass';
  return 'score-fail';
}

// 获取用户store中的学生信息
const getUserRoleInfo = () => {
  const userStore = useUserStore();
  if (userStore.roleInfo && userStore.roleInfo[0]) {
    return userStore.roleInfo[0];
  } else {
    return null;
  }
};

// 生成学期选项
function generateSemesterOptions(admissionYear) {
  const options = [];
  const now = new Date();
  const currentYear = now.getFullYear();
  const currentMonth = now.getMonth() + 1;

  // 生成从入学年份到当前年份的所有学年
  for (let year = admissionYear; year <= currentYear; year++) {
    // 第一学期：year年9月 - year+1年1月
    const firstSemesterAvailable = (year < currentYear) || (year === currentYear && currentMonth >= 9);
    if (firstSemesterAvailable) {
      options.push({
        label: `${year}-${year + 1}学年第一学期`,
        value: `${year}-${year + 1}-1`
      });
    }

    // 第二学期：year+1年2月 - year+1年7月
    const secondSemesterAvailable = (year + 1 < currentYear) || (year + 1 === currentYear && currentMonth >= 2);
    if (secondSemesterAvailable) {
      options.push({
        label: `${year}-${year + 1}学年第二学期`,
        value: `${year}-${year + 1}-2`
      });
    }
  }

  semesterOptions.value = options;
}

// 获取我的成绩
function getMyScoreList() {
  const roleInfo = getUserRoleInfo();
  if (!roleInfo || !roleInfo.id) {
    loading.value = false;
    return;
  }

  loading.value = true;

  const params = {
    studentId: roleInfo.id
  };

  if (currentSemester.value) {
    params.semester = currentSemester.value;
  }

  if (queryGrade.value) {
    params.grade = queryGrade.value;
  }

  getMyScores(params).then(res => {
    scoreList.value = res.data || [];
    loading.value = false;
  }).catch((err) => {
    console.error('查询成绩失败:', err);
    loading.value = false;
  });
}

// 查询
function handleQuery() {
  getMyScoreList();
}

// 初始化
function init() {
  const roleInfo = getUserRoleInfo();
  if (!roleInfo) {
    loading.value = false;
    return;
  }

  // 从roleInfo中获取入学年份并生成学期选项
  if (roleInfo.admissionYear) {
    generateSemesterOptions(roleInfo.admissionYear);
  } else {
    // 如果没有入学年份，使用默认值（当前年份往前推3年）
    const defaultYear = new Date().getFullYear() - 3;
    generateSemesterOptions(defaultYear);
  }

  getMyScoreList();
}

init();
</script>

<style scoped>
.mb20 {
  margin-bottom: 20px;
}

.py20 {
  padding: 20px 0;
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

.stats-container {
  display: flex;
  justify-content: space-around;
  padding: 10px 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.score-excellent {
  color: #67c23a;
  font-weight: bold;
}

.score-good {
  color: #409eff;
  font-weight: bold;
}

.score-medium {
  color: #e6a23c;
  font-weight: bold;
}

.score-pass {
  color: #909399;
  font-weight: bold;
}

.score-fail {
  color: #f56c6c;
  font-weight: bold;
}
</style>
