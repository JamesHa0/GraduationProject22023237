<template>
  <div class="app-container">
    <el-row class="mb-20">
      <el-col :span="24">
        <el-tag v-if="isSubmitted" type="success" effect="dark" size="large">
          选课已提交
        </el-tag>
        <el-tag v-else type="warning" effect="dark" size="large">
          选课进行中（草稿状态）
        </el-tag>
      </el-col>
    </el-row>

    <div class="card-container">
      <el-card v-for="index in maxCourseCount" :key="index" class="card-item" shadow="hover"
        v-if="maxCourseCount > 0">
        <template #header>
          <div class="card-header">
            <span>{{ getChoiceName(index - 1) }}志愿</span>
          </div>
        </template>
        <el-text v-if="isCourseSelected(index)" size="large">
          已选择课程：<el-tag type="success" size="large">{{ getSelectedCourseName(index) || '未选择' }}</el-tag>
        </el-text>
        <p v-else>未选择</p>
        <template #footer>
          <div class="card-footer">
            <el-button v-if="!isSubmitted && !isCourseSelected(index)" type="primary" @click="saveChoice(index - 1)">
              提交
            </el-button>
            <el-button v-if="!isSubmitted && isCourseSelected(index)" type="danger" @click="removeChoice(index - 1)">
              移除
            </el-button>
          </div>
        </template>
      </el-card>
    </div>

    <div class="footer" v-if="!isSubmitted">
      <el-button type="primary" size="large" :disabled="selectedCount === 0" @click="handleBatchSubmit">
        提交所有选课
      </el-button>
    </div>

    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px" @submit.native.prevent>
      <el-form-item label="课程名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入课程名称" clearable style="width: 200px"
          @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="学期" prop="semester">
        <el-input v-model="queryParams.semester" placeholder="请输入学期" clearable style="width: 150px"
          @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
    </el-row>

    <el-card v-if="!isSubmitted" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">可选课程</span>
        </div>
      </template>
      <el-table v-loading="loading" :data="availableCourses" border>
        <el-table-column label="序号" width="50" type="index" align="center">
          <template #default="scope">
            <span>{{ scope.$index + 1 }}</span>
          </template>
        </el-table-column>
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
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="isCourseAlreadySelected(scope.row.id)" type="success">已选择</el-tag>
            <el-tag v-else-if="scope.row.selectedCount >= scope.row.maxStudents" type="danger">已满</el-tag>
            <el-tag v-else-if="selectedCourse === scope.row.id" type="warning">已选中</el-tag>
            <el-tag v-else type="primary">可选</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" align="center" class-name="small-padding fixed-width" width="120">
          <template #default="scope">
            <el-button v-if="isCourseAlreadySelected(scope.row.id)" text type="success" icon="Check" disabled>
              已选择
            </el-button>
            <el-button v-else-if="selectedCourse !== scope.row.id" text type="primary" icon="Plus"
              @click="selectCourse(scope.row)" :disabled="scope.row.selectedCount >= scope.row.maxStudents">
              选中
            </el-button>
            <el-button v-else text type="danger" icon="Close" @click="cancelSelection()">
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card v-if="isSubmitted" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">已提交选课</span>
        </div>
      </template>
      <el-table :data="submittedCourses" border>
        <el-table-column label="志愿顺序" width="100" align="center">
          <template #default="scope">
            {{ getChoiceName(scope.row.choiceOrder - 1) }}
          </template>
        </el-table-column>
        <el-table-column label="课程编号" prop="courseNo" width="150" />
        <el-table-column label="课程名称" prop="courseName" />
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
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="StudentCourseSelect">
import { listCourse } from "@/api/course/course";
import { listCourseSelection, getStudentCourseChoices, saveCourseChoice, removeCourseChoice, batchSubmitCourses } from "@/api/course/selection";
import useUserStore from '@/store/modules/user';
import { getConfigKey } from '@/api/system/config';

const { proxy } = getCurrentInstance();

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

const queryParams = ref({
  name: undefined,
  semester: undefined
});

const loading = ref(true);
const showSearch = ref(true);
const selectedCourse = ref(null);
const isSubmitted = ref(false);

const maxCourseCount = ref(3);
const choiceNames = ['第一', '第二', '第三', '第四', '第五'];
const getChoiceName = (index) => {
  return choiceNames[index] || `第${index + 1}`;
};

const studentChoicesData = ref([]);
const submittedCourses = ref([]);
const availableCourses = ref([]);
const selectedCount = ref(0);

const columns = ref([
  { key: 0, label: `课程编号`, visible: true },
  { key: 1, label: `课程名称`, visible: true },
  { key: 2, label: `学分`, visible: true },
  { key: 3, label: `学时`, visible: true },
  { key: 4, label: `授课教师`, visible: true },
  { key: 5, label: `星期`, visible: true },
  { key: 6, label: `时间`, visible: true },
  { key: 7, label: `教室`, visible: true },
  { key: 8, label: `已选/限选`, visible: true }
]);

const isCourseSelected = (choiceOrder) => {
  return studentChoicesData.value.some(item => item.choiceOrder === choiceOrder);
};

const getSelectedCourseName = (choiceOrder) => {
  const choice = studentChoicesData.value.find(item => item.choiceOrder === choiceOrder);
  return choice ? choice.courseName : null;
};

const isCourseAlreadySelected = (courseId) => {
  return studentChoicesData.value.some(choice => choice.courseId === courseId);
};

const isMaxChoicesReached = () => {
  return studentChoicesData.value.length >= maxCourseCount.value;
};

const selectCourse = (row) => {
  if (isMaxChoicesReached()) {
    proxy.$modal.msgWarning(`已达到最大选课数 (${maxCourseCount.value})，无法继续选择`);
    return;
  }
  if (isCourseAlreadySelected(row.id)) {
    proxy.$modal.msgWarning('该课程已选择');
    return;
  }

  if (hasTimeConflict(row)) {
    proxy.$modal.msgWarning('该课程与已选课程时间冲突');
    return;
  }

  if (selectedCourse.value) {
    const prevRow = availableCourses.value.find(item => item.id === selectedCourse.value);
    if (prevRow) {
      prevRow.isSelected = false;
    }
  }

  selectedCourse.value = row.id;
  row.isSelected = true;
  proxy.$modal.msgSuccess('已选中该课程');
};

const cancelSelection = () => {
  if (selectedCourse.value) {
    const row = availableCourses.value.find(item => item.id === selectedCourse.value);
    if (row) {
      row.isSelected = false;
    }
    selectedCourse.value = null;
    proxy.$modal.msgSuccess('已取消选中');
  }
};

const saveChoice = (choiceIndex) => {
  if (!selectedCourse.value) {
    proxy.$modal.msgWarning(`请为第${choiceIndex + 1}志愿选择一门课程`);
    return;
  }

  const data = {
    studentId: studentId.value,
    courseId: selectedCourse.value,
    choiceOrder: choiceIndex + 1
  };

  proxy.$modal.confirm(`提交第${choiceIndex + 1}志愿选择`).then(() => {
    saveCourseChoice(data).then(() => {
      proxy.$modal.msgSuccess(`第${choiceIndex + 1}志愿提交成功`);
      selectedCourse.value = null;
      getList();
    }).catch(error => {
      proxy.$modal.msgError(error.msg || '提交失败');
    });
  }).catch(() => {});
};

const removeChoice = (choiceIndex) => {
  const selectedChoice = studentChoicesData.value.find(
    choice => choice.choiceOrder === choiceIndex + 1
  );

  proxy.$modal.confirm(`是否移除已选择的第${choiceIndex + 1}志愿？`).then(() => {
    const data = {
      studentId: studentId.value,
      choiceOrder: choiceIndex + 1
    };

    removeCourseChoice(data).then(() => {
      proxy.$modal.msgSuccess(`第${choiceIndex + 1}志愿移除成功`);
      selectedCourse.value = null;
      getList();
    }).catch(error => {
      proxy.$modal.msgError(error.msg || '移除失败');
    });
  }).catch(() => {});
};

const handleBatchSubmit = () => {
  if (studentChoicesData.value.length === 0) {
    proxy.$modal.msgWarning('请至少选择一门课程');
    return;
  }

  proxy.$modal.confirm('确认提交所有选课？提交后不可修改').then(() => {
    const data = {
      studentId: studentId.value,
      choices: studentChoicesData.value.map(choice => ({
        courseId: choice.courseId,
        choiceOrder: choice.choiceOrder
      }))
    };

    batchSubmitCourses(data).then(() => {
      proxy.$modal.msgSuccess('提交成功');
      isSubmitted.value = true;
      getList();
    }).catch(error => {
      proxy.$modal.msgError(error.msg || '提交失败');
    });
  }).catch(() => {});
};

const hasTimeConflict = (newCourse) => {
  if (!newCourse.dayOfWeek || !newCourse.startTime) {
    return false;
  }

  for (const existingChoice of studentChoicesData.value) {
    if (!existingChoice.dayOfWeek || !existingChoice.startTime) {
      continue;
    }

    if (newCourse.dayOfWeek !== existingChoice.dayOfWeek) {
      continue;
    }

    if (isTimeOverlap(newCourse.startTime, newCourse.endTime,
                     existingChoice.startTime, existingChoice.endTime)) {
      return true;
    }
  }
  return false;
};

const isTimeOverlap = (start1, end1, start2, end2) => {
  if (!start1 || !end1 || !start2 || !end2) {
    return false;
  }

  try {
    const s1 = timeToMinutes(start1);
    const e1 = timeToMinutes(end1);
    const s2 = timeToMinutes(start2);
    const e2 = timeToMinutes(end2);

    return s1 < e2 && s2 < e1;
  } catch (e) {
    return false;
  }
};

const timeToMinutes = (time) => {
  const parts = time.split(':');
  return parseInt(parts[0]) * 60 + parseInt(parts[1]);
};

const getMaxCourseCount = () => {
  getConfigKey('student_max_courses').then(response => {
    maxCourseCount.value = Math.max(1, parseInt(response.data) || 3);
  }).catch(() => {
    maxCourseCount.value = 3;
  });
};

const getStudentChoices = () => {
  const id = getStudentId();
  if (!id) {
    return Promise.resolve();
  }

  return getStudentCourseChoices({ studentId: id }).then(res => {
    studentChoicesData.value = res.data || [];
    selectedCount.value = studentChoicesData.value.length;
  }).catch(() => {
    studentChoicesData.value = [];
    selectedCount.value = 0;
    return Promise.resolve();
  });
};

const checkIfSubmitted = () => {
  const id = getStudentId();
  if (!id) {
    return Promise.resolve();
  }

  return listCourseSelection({ studentId: id, submitStatus: 1 }).then(res => {
    if (res.data && res.data.length > 0) {
      isSubmitted.value = true;
      submittedCourses.value = res.data;
    } else {
      isSubmitted.value = false;
      submittedCourses.value = [];
    }
  }).catch(() => {
    isSubmitted.value = false;
    submittedCourses.value = [];
    return Promise.resolve();
  });
};

const getAvailableCourses = () => {
  loading.value = true;
  listCourse(queryParams.value).then(res => {
    const allCourses = res.data || [];

    Promise.all(allCourses.map(course => {
      return listCourseSelection({ courseId: course.id, submitStatus: 1 }).then(selectionRes => {
        course.selectedCount = selectionRes.data ? selectionRes.data.length : 0;
      });
    })).then(() => {
      availableCourses.value = allCourses.filter(course => {
        return course.status === 1;
      });
      loading.value = false;
    });
  });
};

const handleQuery = () => {
  getAvailableCourses();
};

const resetQuery = () => {
  queryParams.value.name = undefined;
  queryParams.value.semester = undefined;
  handleQuery();
};

const getList = () => {
  const id = getStudentId();
  if (!id) {
    loading.value = false;
    return;
  }
  studentId.value = id;

  checkIfSubmitted().then(() => {
    if (isSubmitted.value) {
      loading.value = false;
    } else {
      getStudentChoices().then(() => {
        getAvailableCourses();
      });
    }
  });
};

getMaxCourseCount();
getList();
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}

.mb20 {
  margin-bottom: 20px;
}

.mb-20 {
  margin-bottom: 20px;
}

.card-container {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.card-item {
  flex: 1;
  min-width: 200px;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
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

.footer {
  margin-top: 20px;
  margin-right: 50px;
  margin-bottom: 20px;
  text-align: right;
}
</style>
