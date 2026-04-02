<template>
  <div class="app-container">
    <!-- 学期选择 -->
    <el-card class="mb20" shadow="never">
      <el-form :inline="true">
        <el-form-item label="学期">
          <el-select v-model="currentSemester" placeholder="请选择学期" style="width: 220px">
            <el-option
              v-for="item in semesterOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
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
          <el-radio-group v-model="displayMode" size="small">
            <el-radio-button value="table">表格模式</el-radio-button>
            <el-radio-button value="list">列表模式</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <el-empty v-if="!loading && selectedCourses.length === 0" description="暂无课程，请先进行选课" class="py20" />
      <div v-else>
        <!-- 表格模式 -->
        <div v-if="displayMode === 'table'" class="schedule-container">
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
        <!-- 列表模式 -->
        <div v-else class="list-container">
          <el-table :data="sortedCourses" border style="width: 100%">
            <el-table-column label="序号" width="60" type="index" align="center" />
            <el-table-column label="课程编号" prop="courseNo" width="120" />
            <el-table-column label="课程名称" prop="name" min-width="150" />
            <el-table-column label="总学时" prop="hours" width="80" align="center" />
            <el-table-column label="学分" prop="credit" width="80" align="center" />
            <el-table-column label="修读性质" prop="studyNature" width="100" />
            <el-table-column label="任课教师" prop="teacherName" width="120" />
            <el-table-column label="选课状态" width="100" align="center">
              <template #default="scope">
                <el-tag type="success" size="small">已选</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="教材" width="80" align="center">
              <template #default="scope">
                {{ scope.row.textbook === 1 ? '是' : '否' }}
              </template>
            </el-table-column>
            <el-table-column label="外年级/专业选课" width="120" align="center">
              <template #default="scope">
                {{ scope.row.externalSelection === 1 ? '是' : '否' }}
              </template>
            </el-table-column>
            <el-table-column label="上课时间地点" width="200">
              <template #default="scope">
                <div v-if="scope.row.dayOfWeek">
                  周{{ scope.row.dayOfWeek }} {{ scope.row.startTime || '-' }}-{{ scope.row.endTime || '-' }}
                </div>
                <div v-if="scope.row.classroom">{{ scope.row.classroom }}</div>
              </template>
            </el-table-column>
            <el-table-column label="备注" prop="remark" min-width="150" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup name="CourseSchedule">
import { listCourseSelection } from "@/api/course/selection";
import useUserStore from '@/store/modules/user';

const { proxy } = getCurrentInstance();

// 当前学期：空字符串表示"入学以来"
const currentSemester = ref('');

// 显示模式：table-表格模式，list-列表模式
const displayMode = ref('table');

// 加载状态
const loading = ref(true);

// 学期选项列表
const semesterOptions = ref([]);

// 已选课程
const selectedCourses = ref([]);

// 课程颜色
const courseColors = [
  '#E6F7FF', '#F6FFED', '#FFF7E6', '#FFF1F0', '#F9F0FF',
  '#E0F7FA', '#E8F5E9', '#FFF3E0', '#FCE4EC', '#F3E5F5'
];

// 排序后的课程列表（按星期和时间排序）
const sortedCourses = computed(() => {
  return [...selectedCourses.value].sort((a, b) => {
    // 先按星期几排序
    const dayA = a.dayOfWeek || 0;
    const dayB = b.dayOfWeek || 0;
    if (dayA !== dayB) {
      return dayA - dayB;
    }
    // 再按开始时间排序
    const timeA = a.startTime || '';
    const timeB = b.startTime || '';
    return timeA.localeCompare(timeB);
  });
});

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
  const currentYear = new Date().getFullYear();

  // 生成从入学年份到当前年份的所有学期
  for (let year = admissionYear; year <= currentYear; year++) {
    options.push({
      label: `${year}-${year + 1}学年第一学期`,
      value: `${year}-${year + 1}-1`
    });
    options.push({
      label: `${year}-${year + 1}学年第二学期`,
      value: `${year}-${year + 1}-2`
    });
  }

  // 前置"入学以来"选项
  options.unshift({ label: '入学以来', value: '' });

  semesterOptions.value = options;
}

// 获取已选课程
function getSelectedCourses() {
  const roleInfo = getUserRoleInfo();
  if (!roleInfo || !roleInfo.id) {
    loading.value = false;
    return;
  }

  loading.value = true;
  console.log('查询学生选课，studentId:', roleInfo.id, 'semester:', currentSemester.value);

  const params = { studentId: roleInfo.id, status: 1 };
  if (currentSemester.value) {
    params.semester = currentSemester.value;
  }

  listCourseSelection(params).then(res => {
    console.log('选课返回数据:', res);
    selectedCourses.value = res.data || [];
    console.log('已选课程列表:', selectedCourses.value);
    generateSchedule();
    loading.value = false;
  }).catch((err) => {
    console.error('查询选课失败:', err);
    loading.value = false;
  });
}

// 生成课程表
function generateSchedule() {
  console.log('开始生成课程表，课程数量:', selectedCourses.value.length);
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
    console.log('处理课程:', course.name, 'dayOfWeek:', course.dayOfWeek, 'startTime:', course.startTime);
    if (course.dayOfWeek && dayMap[course.dayOfWeek]) {
      const dayKey = dayMap[course.dayOfWeek];
      const timeSlot = getTimeSlot(course.startTime);
      console.log('dayKey:', dayKey, 'timeSlot:', timeSlot);
      if (timeSlot >= 0 && timeSlot < scheduleData.value.length) {
        scheduleData.value[timeSlot][dayKey] = {
          name: course.name,
          teacher: course.teacherName || '-',
          classroom: course.classroom || '-',
          color: courseColors[index % courseColors.length]
        };
        console.log('课程已添加到课程表');
      } else {
        console.log('时间段无效，跳过');
      }
    } else {
      console.log('dayOfWeek无效，跳过');
    }
  });
  console.log('课程表生成完成:', scheduleData.value);
}

// 根据开始时间获取时间段索引
function getTimeSlot(startTime) {
  if (!startTime) {
    console.log('startTime为空');
    return -1;
  }
  // 提取小时部分进行匹配，更灵活
  const hour = parseInt(startTime.split(':')[0]);
  console.log('startTime:', startTime, 'hour:', hour);

  if (hour >= 8 && hour < 10) return 0;    // 08:00-09:59
  if (hour >= 10 && hour < 12) return 1;   // 10:00-11:59
  if (hour >= 14 && hour < 16) return 2;   // 14:00-15:59
  if (hour >= 16 && hour < 18) return 3;   // 16:00-17:59
  if (hour >= 19 && hour < 21) return 4;   // 19:00-20:59

  console.log('未匹配到时间段');
  return -1;
}

// 查询
function handleQuery() {
  getSelectedCourses();
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
