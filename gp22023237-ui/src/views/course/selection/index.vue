<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="学生姓名" prop="studentName">
        <el-input v-model="queryParams.studentName" placeholder="请输入学生姓名" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="课程名称" prop="courseName">
        <el-input v-model="queryParams.courseName" placeholder="请输入课程名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="选课状态" clearable style="width: 120px">
          <el-option label="正常" value="1" />
          <el-option label="已退课" value="0" />
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

    <el-table v-loading="loading" :data="selectionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="学生姓名" align="center" prop="studentName" width="100" />
      <el-table-column label="课程名称" align="center" prop="courseName" :show-overflow-tooltip="true" />
      <el-table-column label="选课时间" align="center" prop="selectionTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.selectionTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
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

    <!-- 添加或修改选课记录对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form :model="form" :rules="rules" ref="selectionRef" label-width="80px">
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
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">正常</el-radio>
                <el-radio :label="0">已退课</el-radio>
              </el-radio-group>
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

<script setup name="CourseSelection">
import { listCourseSelection, getCourseSelection, addCourseSelection, updateCourseSelection, delCourseSelection, delCourseSelectionBatch } from "@/api/course/selection";

const router = useRouter();
const { proxy } = getCurrentInstance();

const selectionList = ref([]);
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

// 列显隐信息
const columns = ref([
  { key: 0, label: `学生姓名`, visible: true },
  { key: 1, label: `课程名称`, visible: true },
  { key: 2, label: `选课时间`, visible: true },
  { key: 3, label: `状态`, visible: true }
]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentName: undefined,
    courseName: undefined,
    status: undefined
  },
  rules: {
    studentId: [{ required: true, message: "学生不能为空", trigger: "change" }],
    courseId: [{ required: true, message: "课程不能为空", trigger: "change" }],
    status: [{ required: true, message: "状态不能为空", trigger: "change" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 获取状态标签类型 */
function getStatusTagType(status) {
  const typeMap = {
    1: "success",
    0: "warning"
  };
  return typeMap[status] || "info";
}

/** 获取状态文本 */
function getStatusText(status) {
  const textMap = {
    1: "正常",
    0: "已退课"
  };
  return textMap[status] || "未知";
}

/** 查询选课记录列表 */
function getList() {
  loading.value = true;
  listCourseSelection(queryParams.value).then(res => {
    loading.value = false;
    selectionList.value = res.data || [];
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

/** 删除按钮操作 */
function handleDelete(row) {
  const selectionIds = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除选课记录ID为"' + (row.id || selectionIds) + '"的数据项？').then(function () {
    return delCourseSelection(selectionIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
};

/** 导出按钮操作 */
function handleExport() {
  proxy.download("course/selection/export", {
    ...queryParams.value,
  },`course_selection_${new Date().getTime()}.xlsx`);
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
    status: 1
  };
  proxy.resetForm("selectionRef");
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
  open.value = true;
  title.value = "添加选课记录";
};

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const selectionId = row.id || ids.value;
  getCourseSelection(selectionId).then(response => {
    form.value = response.data;
    getStudentList();
    getCourseList();
    open.value = true;
    title.value = "修改选课记录";
  });
};

/** 提交按钮 */
function submitForm() {
  proxy.$refs["selectionRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        updateCourseSelection(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addCourseSelection(form.value).then(response => {
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
