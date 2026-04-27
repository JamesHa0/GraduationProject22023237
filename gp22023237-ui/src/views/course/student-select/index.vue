<template>
  <div class="app-container">
    <!-- 已提交提示 -->
    <el-alert
      v-if="isSubmitted"
      title="您已完成选课提交"
      type="success"
      :closable="false"
      show-icon
      class="mb20"
    >
      <template #default>
        您的选课已提交，不可再修改。如有特殊情况，请联系教务管理员。
      </template>
    </el-alert>

    <!-- 选课时间窗口提示 -->
    <el-alert
      v-if="!selectionOpen && !isSubmitted"
      title="当前不在选课时间窗口内"
      type="warning"
      :closable="false"
      show-icon
      class="mb20"
    >
      <template #default>
        选课功能暂未开放，请等待教学秘书开启选课阶段。
      </template>
    </el-alert>

    <!-- 选课资格提示 -->
    <el-alert
      v-if="hasReachedMaxCourses && !isSubmitted"
      title="您已完成选课"
      type="success"
      :closable="false"
      show-icon
      class="mb20"
    >
      <template #default>
        您已选择 {{ selectedCourses.length }} 门课程，达到最大选课数量限制。如需修改，请先取消已选课程。
      </template>
    </el-alert>

    <el-card class="selection-summary" shadow="never">
      <div class="summary-content">
        <span class="summary-text">
          已选课程：<strong>{{ selectedCourses.length }}</strong> / {{ maxCourseCount }}
        </span>
        <div class="selected-tags" v-if="selectedCourses.length > 0">
          <el-tag
            v-for="course in selectedCourses"
            :key="course.id"
            :closable="!isSubmitted"
            @close="toggleCourseSelection(course, false)"
            class="course-tag"
          >
            {{ course.name }}
          </el-tag>
        </div>
        <el-button
          type="primary"
          size="large"
          :disabled="selectedCourses.length === 0 || isSubmitted || !selectionOpen"
          @click="handleSave"
        >
          {{ isSubmitted ? '已提交' : '保存选课' }}
        </el-button>
      </div>
    </el-card>

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

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">可选课程</span>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="availableCourses"
        border
        ref="courseTableRef"
        @selection-change="handleSelectionChange"
        row-key="id"
      >
        <el-table-column type="selection" width="50" align="center" :selectable="checkSelectable" />
        <el-table-column label="序号" width="50" type="index" align="center">
          <template #default="scope">
            <span>{{ scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="课程编号" prop="courseNo" width="150" />
        <el-table-column label="课程名称" prop="name" />
        <el-table-column label="学分" prop="credit" width="80" align="center" />
        <el-table-column label="学时" prop="hours" width="80" align="center" />
        <el-table-column label="已选人数" width="100" align="center">
          <template #default="scope">
            {{ scope.row.selectedCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="isCourseSelected(scope.row.id)" type="success">已选择</el-tag>
            <el-tag v-else type="primary">可选</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="StudentCourseSelect">
import { listCourse } from "@/api/course/course";
import { listCourseSelection, getStudentCourseChoices, saveCourseSelections, getSubmitStatus } from "@/api/course/selection";
import { isSelectionOpen } from "@/api/course/phase";
import useUserStore from '@/store/modules/user';
import { getConfigKey } from '@/api/system/config';

const { proxy } = getCurrentInstance();

const studentId = ref(null);
const courseTableRef = ref(null);

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

const maxCourseCount = ref(3);
const availableCourses = ref([]);
const selectedCourses = ref([]);
const hasReachedMaxCourses = ref(false);
const isSubmitted = ref(false);
const selectionOpen = ref(false);

const columns = ref([
  { key: 0, label: `课程编号`, visible: true },
  { key: 1, label: `课程名称`, visible: true },
  { key: 2, label: `学分`, visible: true },
  { key: 3, label: `学时`, visible: true },
  { key: 4, label: `已选人数`, visible: true }
]);

const isCourseSelected = (courseId) => {
  return selectedCourses.value.some(c => c.id === courseId);
};

// 检查是否达到最大选课数
const checkMaxCoursesReached = () => {
  hasReachedMaxCourses.value = selectedCourses.value.length >= maxCourseCount.value;
};

const checkSelectable = (row) => {
  if (!selectionOpen.value) {
    return false; // 不在选课时间窗口内
  }
  if (isSubmitted.value) {
    return false; // 已提交后不可选择
  }
  if (isCourseSelected(row.id)) {
    return true; // 已选择的课程总是可以取消选择
  }
  if (selectedCourses.value.length >= maxCourseCount.value) {
    return false;
  }
  return true;
};

const handleSelectionChange = (selection) => {
  if (selection.length > maxCourseCount.value) {
    proxy.$modal.msgWarning(`最多只能选择 ${maxCourseCount.value} 门课程`);
    const lastSelected = selection[selection.length - 1];
    toggleCourseSelection(lastSelected, false);
    return;
  }

  selectedCourses.value = selection;
  checkMaxCoursesReached();
};

const toggleCourseSelection = (course, selected) => {
  if (isSubmitted.value) {
    return;
  }
  if (courseTableRef.value) {
    courseTableRef.value.toggleRowSelection(course, selected);
  }
};

const handleSave = () => {
  if (selectedCourses.value.length === 0) {
    proxy.$modal.msgWarning('请至少选择一门课程');
    return;
  }

  proxy.$modal.confirm('确认保存选课？保存后将不可修改。').then(() => {
    const data = {
      studentId: studentId.value,
      choices: selectedCourses.value.map((course) => ({
        courseId: course.id
      }))
    };

    saveCourseSelections(data).then(() => {
      proxy.$modal.msgSuccess('保存成功，选课已提交');
      isSubmitted.value = true;
      getList();
    }).catch(error => {
      proxy.$modal.msgError(error.msg || '保存失败');
    });
  }).catch(() => {});
};

const checkSubmitStatus = () => {
  const id = getStudentId();
  if (!id) {
    return Promise.resolve();
  }
  return getSubmitStatus({ studentId: id }).then(res => {
    isSubmitted.value = res.data === true;
  }).catch(() => {
    isSubmitted.value = false;
    return Promise.resolve();
  });
};

const getMaxCourseCount = () => {
  getConfigKey('student_max_courses').then(response => {
    maxCourseCount.value = Math.max(1, parseInt(response.data) || 3);
  }).catch(() => {
    maxCourseCount.value = 3;
  });
};

const checkSelectionOpen = () => {
  isSelectionOpen().then(res => {
    selectionOpen.value = res.data === true;
  }).catch(() => {
    selectionOpen.value = false;
  });
};

const getStudentChoices = () => {
  const id = getStudentId();
  if (!id) {
    return Promise.resolve();
  }

  return getStudentCourseChoices({ studentId: id }).then(res => {
    const choices = res.data || [];
    const coursesToSelect = [];
    for (const choice of choices) {
      const course = availableCourses.value.find(c => c.id === choice.courseId);
      if (course) {
        coursesToSelect.push(course);
      }
    }
    selectedCourses.value = coursesToSelect;
    checkMaxCoursesReached();
  }).catch(() => {
    selectedCourses.value = [];
    checkMaxCoursesReached();
    return Promise.resolve();
  });
};

const getAvailableCourses = () => {
  loading.value = true;
  listCourse(queryParams.value).then(res => {
    const allCourses = res.data || [];

    Promise.all(allCourses.map(course => {
      return listCourseSelection({ courseId: course.id, status: 1 }).then(selectionRes => {
        course.selectedCount = selectionRes.data ? selectionRes.data.length : 0;
      });
    })).then(() => {
      availableCourses.value = allCourses.filter(course => {
        return course.status === 1;
      });

      nextTick(() => {
        getStudentChoices().then(() => {
          restoreSelections();
        });
      });

      loading.value = false;
    });
  });
};

const restoreSelections = () => {
  if (!courseTableRef.value) return;

  for (const course of selectedCourses.value) {
    courseTableRef.value.toggleRowSelection(course, true);
  }
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
  checkSubmitStatus().then(() => {
    getAvailableCourses();
  });
};

getMaxCourseCount();
checkSelectionOpen();
getList();
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}

.mb20 {
  margin-bottom: 20px;
}

.selection-summary {
  margin-bottom: 20px;
}

.summary-content {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.summary-text {
  font-size: 16px;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex: 1;
}

.course-tag {
  margin: 0;
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
