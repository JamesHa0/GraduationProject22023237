<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="学生姓名" prop="studentName">
        <el-input v-model="queryParams.studentName" placeholder="请输入学生姓名" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="课程名称" prop="courseName">
        <el-input v-model="queryParams.courseName" placeholder="请输入课程名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="成绩" prop="score">
        <el-input-number v-model="queryParams.score" :min="0" :max="100" :step="1" placeholder="请输入成绩" style="width: 120px" />
      </el-form-item>
      <el-form-item label="等级" prop="grade">
        <el-select v-model="queryParams.grade" placeholder="请选择等级" clearable style="width: 120px">
          <el-option label="优秀" value="A" />
          <el-option label="良好" value="B" />
          <el-option label="中等" value="C" />
          <el-option label="及格" value="D" />
          <el-option label="不及格" value="E" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="scoreList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="学生姓名" align="center" prop="studentName" width="100" />
      <el-table-column label="课程名称" align="center" prop="courseName" :show-overflow-tooltip="true" />
      <el-table-column label="成绩" align="center" prop="score" width="80">
        <template #default="scope">
          <el-tag :type="getScoreTagType(scope.row.score)">
            {{ scope.row.score }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="等级" align="center" prop="grade" width="80">
        <template #default="scope">
          <el-tag :type="getGradeTagType(scope.row.grade)">
            {{ getGradeText(scope.row.grade) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="评语" align="center" prop="comment" width="200" :show-overflow-tooltip="true" />
      <el-table-column label="录入教师" align="center" prop="teacherName" width="100" />
      <el-table-column label="更新时间" align="center" prop="updateTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-tooltip content="修改" placement="top">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 添加或修改成绩对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form :model="form" :rules="rules" ref="scoreRef" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="学生" prop="studentId">
              <el-select v-model="form.studentId" placeholder="请选择学生" style="width: 100%">
                <el-option v-for="item in studentOptions" :key="item.id" :label="item.studentName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程" prop="courseId">
              <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
                <el-option v-for="item in courseOptions" :key="item.id" :label="item.courseName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="成绩" prop="score">
              <el-input-number v-model="form.score" :min="0" :max="100" :step="0.5" placeholder="请输入成绩" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="等级" prop="grade">
              <el-select v-model="form.grade" placeholder="请选择等级" style="width: 100%">
                <el-option label="优秀" value="A" />
                <el-option label="良好" value="B" />
                <el-option label="中等" value="C" />
                <el-option label="及格" value="D" />
                <el-option label="不及格" value="E" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="评语" prop="comment">
              <el-input v-model="form.comment" type="textarea" placeholder="请输入评语" :rows="3" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="录入教师" prop="teacherId">
              <el-select v-model="form.teacherId" placeholder="请选择录入教师" style="width: 100%">
                <el-option v-for="item in teacherOptions" :key="item.id" :label="item.teacherName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Score">
import { listScore, getScore, addScore, updateScore, delScore, delScoreBatch } from "@/api/course/score";

const router = useRouter();
const { proxy } = getCurrentInstance();

const scoreList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const studentOptions = ref([]);
const courseOptions = ref([]);
const teacherOptions = ref([]);

// 列显隐信息
const columns = ref([
  { key: 0, label: `学生姓名`, visible: true },
  { key: 1, label: `课程名称`, visible: true },
  { key: 2, label: `成绩`, visible: true },
  { key: 3, label: `等级`, visible: true },
  { key: 4, label: `评语`, visible: true },
  { key: 5, label: `录入教师`, visible: true },
  { key: 6, label: `更新时间`, visible: true }
]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentName: undefined,
    courseName: undefined,
    score: undefined,
    grade: undefined
  },
  rules: {
    studentId: [{ required: true, message: "学生不能为空", trigger: "change" }],
    courseId: [{ required: true, message: "课程不能为空", trigger: "change" }],
    score: [{ required: true, message: "成绩不能为空", trigger: "blur" }],
    grade: [{ required: true, message: "等级不能为空", trigger: "change" }],
    teacherId: [{ required: true, message: "录入教师不能为空", trigger: "change" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 获取成绩标签类型 */
function getScoreTagType(score) {
  if (score >= 90) return "success";
  if (score >= 80) return "primary";
  if (score >= 70) return "info";
  if (score >= 60) return "warning";
  return "danger";
}

/** 获取等级标签类型 */
function getGradeTagType(grade) {
  const typeMap = {
    A: "success",
    B: "primary",
    C: "info",
    D: "warning",
    E: "danger"
  };
  return typeMap[grade] || "info";
}

/** 获取等级文本 */
function getGradeText(grade) {
  const textMap = {
    A: "优秀",
    B: "良好",
    C: "中等",
    D: "及格",
    E: "不及格"
  };
  return textMap[grade] || "未知";
}

/** 查询成绩列表 */
function getList() {
  loading.value = true;
  listScore(queryParams.value).then(res => {
    loading.value = false;
    scoreList.value = res.data || [];
    total.value = res.total || 0;
  });
};

/** 查询学生列表 */
function getStudentList() {
  // 这里需要调用获取学生列表的API，假设已经存在
  // listStudent().then(res => {
  //   studentOptions.value = res.data;
  // });
  // 暂时使用模拟数据
  studentOptions.value = [
    { id: 1, studentName: "张三" },
    { id: 2, studentName: "李四" },
    { id: 3, studentName: "王五" }
  ];
};

/** 查询课程列表 */
function getCourseList() {
  // 这里需要调用获取课程列表的API，假设已经存在
  // listCourse().then(res => {
  //   courseOptions.value = res.data;
  // });
  // 暂时使用模拟数据
  courseOptions.value = [
    { id: 1, courseName: "高等数学" },
    { id: 2, courseName: "大学英语" },
    { id: 3, courseName: "计算机基础" }
  ];
};

/** 查询教师列表 */
function getTeacherList() {
  // 这里需要调用获取教师列表的API，假设已经存在
  // listTeacher().then(res => {
  //   teacherOptions.value = res.data;
  // });
  // 暂时使用模拟数据
  teacherOptions.value = [
    { id: 1, teacherName: "张教授" },
    { id: 2, teacherName: "李副教授" },
    { id: 3, teacherName: "王讲师" }
  ];
};

/** 删除按钮操作 */
function handleDelete(row) {
  const scoreIds = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除成绩ID为"' + (row.id || scoreIds) + '"的数据项？').then(function () {
    return delScore(scoreIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
};

/** 导出按钮操作 */
function handleExport() {
  proxy.download("course/score/export", {
    ...queryParams.value,
  },`score_${new Date().getTime()}.xlsx`);
};

/** 选择条数  */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 重置操作表单 */
function reset() {
  form.value = {
    id: undefined,
    studentId: undefined,
    courseId: undefined,
    score: 0,
    grade: "D",
    comment: undefined,
    teacherId: undefined
  };
  proxy.resetForm("scoreRef");
};

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
};

/** 新增按钮操作 */
function handleAdd() {
  reset();
  getStudentList();
  getCourseList();
  getTeacherList();
  open.value = true;
  title.value = "添加成绩";
};

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const scoreId = row.id || ids.value;
  getScore(scoreId).then(response => {
    form.value = response.data;
    getStudentList();
    getCourseList();
    getTeacherList();
    open.value = true;
    title.value = "修改成绩";
  });
};

/** 提交按钮 */
function submitForm() {
  proxy.$refs["scoreRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        updateScore(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addScore(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
};

getList();
</script>
