<template>
  <div class="app-container">
    <!-- 学期选择 -->
    <el-card class="mb20" shadow="never">
      <el-form :inline="true">
        <el-form-item label="学期">
          <el-select v-model="currentSemester" placeholder="请选择学期" style="width: 200px">
            <el-option label="2024-2025学年第一学期" value="2024-1" />
            <el-option label="2024-2025学年第二学期" value="2024-2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 课程表 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">课程表</span>
        </div>
      </template>
      <div class="schedule-container">
        <el-table :data="scheduleData" border style="width: 100%">
          <el-table-column label="时间" width="100" align="center">
            <template #default="scope">
              <div class="time-cell">
                <div class="time-slot">{{ scope.row.slot }}</div>
                <div class="time-range">{{ scope.row.time }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="周一" align="center">
            <template #default="scope">
              <div v-if="scope.row.monday" class="course-cell" :style="{ background: scope.row.monday.color }">
                <div class="course-name">{{ scope.row.monday.name }}</div>
                <div class="course-info">{{ scope.row.monday.teacher }}</div>
                <div class="course-info">{{ scope.row.monday.classroom }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="周二" align="center">
            <template #default="scope">
              <div v-if="scope.row.tuesday" class="course-cell" :style="{ background: scope.row.tuesday.color }">
                <div class="course-name">{{ scope.row.tuesday.name }}</div>
                <div class="course-info">{{ scope.row.tuesday.teacher }}</div>
                <div class="course-info">{{ scope.row.tuesday.classroom }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="周三" align="center">
            <template #default="scope">
              <div v-if="scope.row.wednesday" class="course-cell" :style="{ background: scope.row.wednesday.color }">
                <div class="course-name">{{ scope.row.wednesday.name }}</div>
                <div class="course-info">{{ scope.row.wednesday.teacher }}</div>
                <div class="course-info">{{ scope.row.wednesday.classroom }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="周四" align="center">
            <template #default="scope">
              <div v-if="scope.row.thursday" class="course-cell" :style="{ background: scope.row.thursday.color }">
                <div class="course-name">{{ scope.row.thursday.name }}</div>
                <div class="course-info">{{ scope.row.thursday.teacher }}</div>
                <div class="course-info">{{ scope.row.thursday.classroom }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="周五" align="center">
            <template #default="scope">
              <div v-if="scope.row.friday" class="course-cell" :style="{ background: scope.row.friday.color }">
                <div class="course-name">{{ scope.row.friday.name }}</div>
                <div class="course-info">{{ scope.row.friday.teacher }}</div>
                <div class="course-info">{{ scope.row.friday.classroom }}</div>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 我的课程列表 -->
    <el-card class="mt20" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">我的课程</span>
        </div>
      </template>
      <el-table v-loading="loading" :data="selectedCourses" border>
        <el-table-column label="课程编号" prop="courseNo" width="150" />
        <el-table-column label="课程名称" prop="name" />
        <el-table-column label="学分" prop="credit" width="80" align="center" />
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
      </el-table>
      <el-empty v-if="!loading && selectedCourses.length === 0" description="暂无课程" />
    </el-card>
  </div>
</template>

<script setup name="CourseSchedule">
import { listCourseSelection } from "@/api/course/selection";
import useUserStore from '@/store/modules/user';

const { proxy } = getCurrentInstance();

// 当前学期
const currentSemester = ref('2024-2');

// 加载状态
const loading = ref(true);

// 已选课程
const selectedCourses = ref([]);

// 课程颜色
const courseColors = [
  '#E6F7FF', '#F6FFED', '#FFF7E6', '#FFF1F0', '#F9F0FF',
  '#E0F7FA', '#E8F5E9', '#FFF3E0', '#FCE4EC', '#F3E5F5'
];

// 课程表数据
const scheduleData = ref([
  { slot: '第1-2节', time: '08:00-09:40', monday: null, tuesday: null, wednesday: null, thursday: null, friday: null },
  { slot: '第3-4节', time: '10:00-11:40', monday: null, tuesday: null, wednesday: null, thursday: null, friday: null },
  { slot: '第5-6节', time: '14:00-15:40', monday: null, tuesday: null, wednesday: null, thursday: null, friday: null },
  { slot: '第7-8节', time: '16:00-17:40', monday: null, tuesday: null, wednesday: null, thursday: null, friday: null },
  { slot: '第9-10节', time: '19:00-20:40', monday: null, tuesday: null, wednesday: null, thursday: null, friday: null }
]);

// 星期映射
const dayMap = { 1: 'monday', 2: 'tuesday', 3: 'wednesday', 4: 'thursday', 5: 'friday' };

// 学生ID
const studentId = ref(null);
const getStudentId = () => {
  const userStore = useUserStore();
  if (userStore.roleInfo && userStore.roleInfo[0]) {
    return userStore.roleInfo[0].id;
  } else {
    return null;
  }
};

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
    generateSchedule();
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
}

// 生成课程表
function generateSchedule() {
  // 重置课程表
  scheduleData.value.forEach(row => {
    row.monday = null;
    row.tuesday = null;
    row.wednesday = null;
    row.thursday = null;
    row.friday = null;
  });

  // 填充课程
  selectedCourses.value.forEach((course, index) => {
    if (course.dayOfWeek && dayMap[course.dayOfWeek]) {
      const dayKey = dayMap[course.dayOfWeek];
      const timeSlot = getTimeSlot(course.startTime);
      if (timeSlot >= 0 && timeSlot < scheduleData.value.length) {
        scheduleData.value[timeSlot][dayKey] = {
          name: course.name,
          teacher: course.teacherName || '-',
          classroom: course.classroom || '-',
          color: courseColors[index % courseColors.length]
        };
      }
    }
  });
}

// 根据开始时间获取时间段索引
function getTimeSlot(startTime) {
  const timeMap = {
    '08:00': 0, '08:30': 0,
    '10:00': 1, '10:30': 1,
    '14:00': 2, '14:30': 2,
    '16:00': 3, '16:30': 3,
    '19:00': 4, '19:30': 4
  };
  return timeMap[startTime] !== undefined ? timeMap[startTime] : -1;
}

// 查询
function handleQuery() {
  getSelectedCourses();
}

// 初始化
function init() {
  const id = getStudentId();
  if (!id) {
    loading.value = false;
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

.time-cell {
  padding: 8px 0;
}

.time-slot {
  font-weight: bold;
  color: #303133;
}

.time-range {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.course-cell {
  padding: 8px;
  border-radius: 4px;
  min-height: 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.course-name {
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.course-info {
  font-size: 12px;
  color: #606266;
  margin-top: 2px;
}
</style>
