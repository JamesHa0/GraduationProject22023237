<template>
  <div class="app-container">
    <!-- 统计信息 -->
    <el-row :gutter="20" class="mb20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#409EFF"><Document /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ selectedCount }}</div>
              <div class="stat-label">已选课程</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#67C23A"><Grid /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ totalCredits }}</div>
              <div class="stat-label">总学分</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 已选课程列表 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">选课结果</span>
        </div>
      </template>
      <el-table v-loading="loading" :data="selectedCourses" border>
        <el-table-column label="课程编号" prop="courseNo" width="150" />
        <el-table-column label="课程名称" prop="name" />
        <el-table-column label="学分" prop="credit" width="80" align="center" />
        <el-table-column label="学时" prop="hours" width="80" align="center" />
        <el-table-column label="授课教师" prop="teacherName" width="120" />
        <el-table-column label="星期" width="80" align="center">
          <template #default="scope">
            {{ scope.row.dayOfWeek ? '周' + scope.row.dayOfWeek : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="时间" width="150" align="center">
          <template #default="scope">
            {{ scope.row.startTime || '-' }} - {{ scope.row.endTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="教室" prop="classroom" width="120" />
        <el-table-column label="选课状态" width="100" align="center">
          <template #default="scope">
            <el-tag type="success">已确认</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && selectedCourses.length === 0" description="暂无选课记录" />
    </el-card>
  </div>
</template>

<script setup name="SelectResult">
import { listCourseSelection } from "@/api/course/selection";
import useUserStore from '@/store/modules/user';

const { proxy } = getCurrentInstance();

// 学生ID
const studentId = ref(null);
const getStudentId = () => {
  const userStore = useUserStore();
  if (userStore.roleInfo && userStore.roleInfo[0]) {
    return userStore.roleInfo[0].id;
  } else {
    proxy.$modal.msgError('学生信息尚未加载');
    return null;
  }
};

// 加载状态
const loading = ref(true);

// 已选课程
const selectedCourses = ref([]);

// 统计信息
const selectedCount = ref(0);
const totalCredits = ref(0);

// 获取已选课程
function getSelectedCourses() {
  const id = getStudentId();
  if (!id) {
    loading.value = false;
    return;
  }

  loading.value = true;
  listCourseSelection({ studentId: id, status: 1 }).then(res => {
    selectedCourses.value = res.data || [];
    updateStatistics();
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
}

// 更新统计信息
function updateStatistics() {
  selectedCount.value = selectedCourses.value.length;
  totalCredits.value = selectedCourses.value.reduce((sum, course) => {
    return sum + (course.credit || 0);
  }, 0);
}

// 初始化
function init() {
  const id = getStudentId();
  if (!id) {
    return;
  }
  studentId.value = id;
  getSelectedCourses();
}

init();
</script>

<style scoped>
.mb20 {
  margin-bottom: 20px;
}

.stat-card {
  cursor: default;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  font-size: 40px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
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
</style>
