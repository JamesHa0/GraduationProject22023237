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
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#E6A23C"><Warning /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ maxCredits - totalCredits }}</div>
              <div class="stat-label">剩余学分</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 已选课程 -->
    <el-card class="mb20" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">已选课程</span>
        </div>
      </template>
      <el-table v-if="selectedCourses.length > 0" :data="selectedCourses" border>
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
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="scope">
            <el-button type="danger" size="small" @click="handleDropCourse(scope.row)">退课</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无已选课程" />
    </el-card>

    <!-- 可选课程 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">可选课程</span>
          <el-row>
            <el-form :model="queryParams" :inline="true" size="small">
              <el-form-item label="课程名称">
                <el-input v-model="queryParams.name" placeholder="请输入课程名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
              </el-form-item>
              <el-form-item label="学期">
                <el-input v-model="queryParams.semester" placeholder="请输入学期" clearable style="width: 150px" @keyup.enter="handleQuery" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </el-row>
        </div>
      </template>
      <el-table v-loading="loading" :data="availableCourses" border>
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
        <el-table-column label="已选/限选" width="100" align="center">
          <template #default="scope">
            {{ scope.row.selectedCount || 0 }} / {{ scope.row.maxStudents || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              :disabled="!canSelect(scope.row)"
              @click="handleSelectCourse(scope.row)"
            >
              {{ canSelect(scope.row) ? '选课' : '已选' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="StudentCourseSelect">
import { listCourse, getCourse } from "@/api/course/course";
import { listCourseSelection, addCourseSelection, delCourseSelection } from "@/api/course/selection";
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

// 查询参数
const queryParams = ref({
  name: undefined,
  semester: undefined
});

// 加载状态
const loading = ref(true);

// 已选课程
const selectedCourses = ref([]);

// 可选课程
const availableCourses = ref([]);

// 统计信息
const selectedCount = ref(0);
const totalCredits = ref(0);
const maxCredits = ref(20); // 最大学分限制

// 获取已选课程
function getSelectedCourses() {
  const id = getStudentId();
  if (!id) {
    return Promise.resolve();
  }

  return listCourseSelection({ studentId: id, status: 1 }).then(res => {
    selectedCourses.value = res.data || [];
    updateStatistics();
  });
}

// 获取可选课程
function getAvailableCourses() {
  loading.value = true;
  listCourse(queryParams.value).then(res => {
    const allCourses = res.data || [];

    // 查询每门课程的选课人数
    Promise.all(allCourses.map(course => {
      return listCourseSelection({ courseId: course.id, status: 1 }).then(selectionRes => {
        course.selectedCount = selectionRes.data ? selectionRes.data.length : 0;
      });
    })).then(() => {
      // 过滤掉已选的课程和未开课的课程
      availableCourses.value = allCourses.filter(course => {
        return course.status === 1 && !isCourseSelected(course.id);
      });
      loading.value = false;
    });
  });
}

// 更新统计信息
function updateStatistics() {
  selectedCount.value = selectedCourses.value.length;
  totalCredits.value = selectedCourses.value.reduce((sum, course) => {
    return sum + (course.credit || 0);
  }, 0);
}

// 判断课程是否已选
function isCourseSelected(courseId) {
  return selectedCourses.value.some(course => course.id === courseId);
}

// 判断是否可以选择课程
function canSelect(course) {
  // 1. 不能选已选的课程
  if (isCourseSelected(course.id)) {
    return false;
  }

  // 2. 检查人数限制
  if (course.maxStudents && course.selectedCount >= course.maxStudents) {
    return false;
  }

  // 3. 检查学分限制
  if (totalCredits.value + (course.credit || 0) > maxCredits.value) {
    return false;
  }

  // 4. 检查时间冲突
  if (hasTimeConflict(course)) {
    return false;
  }

  return true;
}

// 检查时间冲突
function hasTimeConflict(newCourse) {
  if (!newCourse.dayOfWeek || !newCourse.startTime) {
    return false;
  }

  for (const existingCourse of selectedCourses.value) {
    if (!existingCourse.dayOfWeek || !existingCourse.startTime) {
      continue;
    }

    // 检查是否同一天
    if (newCourse.dayOfWeek !== existingCourse.dayOfWeek) {
      continue;
    }

    // 检查时间是否重叠
    if (isTimeOverlap(newCourse.startTime, newCourse.endTime,
                     existingCourse.startTime, existingCourse.endTime)) {
      return true;
    }
  }

  return false;
}

// 判断两个时间段是否重叠
function isTimeOverlap(start1, end1, start2, end2) {
  if (!start1 || !end1 || !start2 || !end2) {
    return false;
  }

  try {
    const s1 = timeToMinutes(start1);
    const e1 = timeToMinutes(end1);
    const s2 = timeToMinutes(start2);
    const e2 = timeToMinutes(end2);

    // 重叠的条件：s1 < e2 && s2 < e1
    return s1 < e2 && s2 < e1;
  } catch (e) {
    return false;
  }
}

// 将时间字符串转换为分钟数
function timeToMinutes(time) {
  const parts = time.split(':');
  return parseInt(parts[0]) * 60 + parseInt(parts[1]);
}

// 选课
function handleSelectCourse(course) {
  const id = getStudentId();
  if (!id) {
    proxy.$modal.msgError('学生信息尚未加载');
    return;
  }

  const data = {
    studentId: id,
    courseId: course.id,
    status: 1
  };

  proxy.$modal.confirm(`确认选择课程《${course.name}》？`).then(() => {
    addCourseSelection(data).then(() => {
      proxy.$modal.msgSuccess('选课成功');
      getSelectedCourses().then(() => {
        getAvailableCourses();
      });
    }).catch(error => {
      proxy.$modal.msgError(error.data || '选课失败');
    });
  }).catch(() => {});
}

// 退课
function handleDropCourse(course) {
  proxy.$modal.confirm(`确认退课《${course.name}》？`).then(() => {
    // 找到选课记录ID
    listCourseSelection({ studentId: getStudentId(), courseId: course.id, status: 1 }).then(res => {
      if (res.data && res.data.length > 0) {
        const selectionId = res.data[0].id;
        delCourseSelection(selectionId).then(() => {
          proxy.$modal.msgSuccess('退课成功');
          getSelectedCourses().then(() => {
            getAvailableCourses();
          });
        });
      }
    });
  }).catch(() => {});
}

// 搜索
function handleQuery() {
  getAvailableCourses();
}

// 重置
function resetQuery() {
  queryParams.value.name = undefined;
  queryParams.value.semester = undefined;
  handleQuery();
}

// 初始化
function init() {
  const id = getStudentId();
  if (!id) {
    return;
  }
  studentId.value = id;
  getSelectedCourses().then(() => {
    getAvailableCourses();
  });
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
