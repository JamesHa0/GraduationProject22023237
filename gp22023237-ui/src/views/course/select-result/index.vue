<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">选课结果</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="selectedCourses" border>
        <el-table-column label="序号" width="80" type="index" align="center" />
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

    <!-- 最终结果展示 -->
    <el-card class="mt20" shadow="never" v-if="selectedCourses.length > 0">
      <template #header>
        <div class="card-header">
          <span class="card-title">选课统计</span>
        </div>
      </template>
      <el-result icon="success" title="选课成功">
        <template #sub-title>
          您已选择 {{ selectedCourses.length }} 门课程，共 {{ totalCredits }} 学分
        </template>
        <template #extra>
          <el-descriptions :column="2" border style="width: 600px; margin: 0 auto;">
            <el-descriptions-item label="已选课程">{{ selectedCourses.length }} 门</el-descriptions-item>
            <el-descriptions-item label="总学分">{{ totalCredits }} 学分</el-descriptions-item>
          </el-descriptions>
        </template>
      </el-result>
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
.mt20 {
  margin-top: 20px;
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
