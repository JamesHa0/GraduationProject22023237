<template>
  <div class="app-container">
    <!-- 学期选择 -->
    <el-card class="mb20" shadow="never">
      <el-form :inline="true">
        <el-form-item label="学期">
          <el-select v-model="currentSemester" placeholder="请选择学期" style="width: 220px">
            <el-option v-for="item in semesterOptions" :key="item.value" :label="item.label" :value="item.value" />
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
      <el-empty v-if="!loading && scheduleList.length === 0" description="暂无排课信息" class="py20" />
      <div v-else>
        <!-- 表格模式 -->
        <div v-if="displayMode === 'table'" class="schedule-container">
          <el-table :data="scheduleData" border style="width: 100%">
            <el-table-column label="时间" width="120" align="center">
              <template #default="scope">
                <div class="time-cell">
                  <div class="time-slot">{{ scope.row.slot }}</div>
                  <div class="time-range">{{ scope.row.time }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column v-for="d in 5" :key="d" :label="dayNames[d]" align="center">
              <template #default="scope">
                <div v-if="scope.row.days[d]" class="course-cell" :style="{ background: scope.row.days[d].color }">
                  <div class="course-name">{{ scope.row.days[d].name }}</div>
                  <div class="course-info">{{ scope.row.days[d].teacher }}</div>
                  <div class="course-info">{{ scope.row.days[d].classroom }}</div>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <!-- 列表模式 -->
        <div v-else class="list-container">
          <el-table :data="scheduleList" border style="width: 100%">
            <el-table-column label="序号" width="60" type="index" align="center" />
            <el-table-column label="课程名称" prop="courseName" min-width="150" />
            <el-table-column label="任课教师" prop="teacherName" width="120" />
            <el-table-column label="班级" prop="className" width="120" />
            <el-table-column label="星期" width="80" align="center">
              <template #default="scope">{{ dayNames[scope.row.dayOfWeek] }}</template>
            </el-table-column>
            <el-table-column label="节次" prop="sectionDisplay" width="110" align="center" />
            <el-table-column label="时间" width="160">
              <template #default="scope">{{ scope.row.startSectionValue }} ~ {{ scope.row.endSectionValue }}</template>
            </el-table-column>
            <el-table-column label="教室" prop="classroom" width="100" />
          </el-table>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup name="CourseSchedule">
import { listSchedule, listTimeSlots } from "@/api/course/schedule";
import useUserStore from '@/store/modules/user';

const { proxy } = getCurrentInstance();

const currentSemester = ref('');
const displayMode = ref('table');
const loading = ref(true);
const semesterOptions = ref([]);
const scheduleList = ref([]);
const timeSlots = ref([]);

const dayNames = { 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六', 7: '周日' };

const courseColors = [
  '#E6F7FF', '#F6FFED', '#FFF7E6', '#FFF1F0', '#F9F0FF',
  '#E0F7FA', '#E8F5E9', '#FFF3E0', '#FCE4EC', '#F3E5F5'
];

// 课程表数据（动态从 timeSlots 生成）
const scheduleData = ref([]);

const getUserRoleInfo = () => {
  const userStore = useUserStore();
  if (userStore.roleInfo && userStore.roleInfo[0]) {
    return userStore.roleInfo[0];
  }
  return null;
};

function generateSemesterOptions(admissionYear) {
  const options = [];
  const now = new Date();
  const currentYear = now.getFullYear();
  const currentMonth = now.getMonth() + 1;

  for (let year = admissionYear; year <= currentYear; year++) {
    if ((year < currentYear) || (year === currentYear && currentMonth >= 9)) {
      options.push({ label: `${year}-${year + 1}学年第一学期`, value: `${year}-${year + 1}-1` });
    }
    if ((year + 1 < currentYear) || (year + 1 === currentYear && currentMonth >= 2)) {
      options.push({ label: `${year}-${year + 1}学年第二学期`, value: `${year}-${year + 1}-2` });
    }
  }

  semesterOptions.value = options;
  if (options.length > 0) {
    const currentOption = options.find(opt => opt.value === currentSemester.value);
    if (!currentOption || !currentSemester.value) {
      currentSemester.value = options[options.length - 1].value;
    }
  }
}

// 初始化课表网格行（从 timeSlots API 获取单节时间片）
function initScheduleData() {
  return listTimeSlots().then(res => {
    timeSlots.value = res.data || [];
    scheduleData.value = timeSlots.value.map(slot => ({
      slot: slot.dictLabel,
      time: slot.dictValue,
      sectionCode: slot.dictCode,
      days: {} // { 1: {name, teacher, classroom, color}, ... }
    }));
  });
}

function getScheduleData() {
  const roleInfo = getUserRoleInfo();
  if (!roleInfo || !roleInfo.id) {
    loading.value = false;
    return;
  }

  loading.value = true;
  const params = {
    classId: roleInfo.classId || undefined,
    semester: currentSemester.value,
    pageSize: 9999
  };

  listSchedule(params).then(res => {
    scheduleList.value = res.data.rows || res.data || [];
    generateSchedule();
    loading.value = false;
  }).catch((err) => {
    console.error('查询排课信息失败:', err);
    scheduleList.value = [];
    loading.value = false;
  });
}

// 生成课程表（使用范围判断）
function generateSchedule() {
  // 重置
  scheduleData.value.forEach(row => { row.days = {}; });

  // 填充课程：每节时间片行检查是否有课程覆盖
  scheduleList.value.forEach((item, index) => {
    if (!item.dayOfWeek) return;
    scheduleData.value.forEach(row => {
      // 范围判断：课程的 startSection <= 当前行 sectionCode <= endSection
      if (item.startSection <= row.sectionCode && item.endSection >= row.sectionCode) {
        if (!row.days[item.dayOfWeek]) {
          row.days[item.dayOfWeek] = {
            name: item.courseName,
            teacher: item.teacherName || '-',
            classroom: item.classroom || '-',
            color: courseColors[index % courseColors.length]
          };
        }
      }
    });
  });
}

function handleQuery() {
  getScheduleData();
}

async function init() {
  const roleInfo = getUserRoleInfo();
  if (!roleInfo) {
    loading.value = false;
    return;
  }

  // 先加载时间片再加载排课数据
  await initScheduleData();

  if (roleInfo.admissionYear) {
    generateSemesterOptions(roleInfo.admissionYear);
  } else {
    generateSemesterOptions(new Date().getFullYear() - 3);
  }

  getScheduleData();
}

init();
</script>

<style scoped>
.mb20 { margin-bottom: 20px; }
.py20 { padding: 20px 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-size: 16px; font-weight: bold; }
.time-cell { padding: 8px 0; }
.time-slot { font-weight: bold; color: #303133; }
.time-range { font-size: 12px; color: #909399; margin-top: 4px; }
.course-cell { padding: 8px; border-radius: 4px; min-height: 80px; display: flex; flex-direction: column; justify-content: center; }
.course-name { font-weight: bold; color: #303133; margin-bottom: 4px; }
.course-info { font-size: 12px; color: #606266; margin-top: 2px; }
</style>
