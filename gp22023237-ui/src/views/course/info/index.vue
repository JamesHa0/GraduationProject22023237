<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="课程名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入课程名称" clearable style="width: 200px"
          @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="课程编号" prop="courseNo">
        <el-input v-model="queryParams.courseNo" placeholder="请输入课程编号" clearable style="width: 200px"
          @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="课程状态" clearable style="width: 120px">
          <el-option label="未开课" :value="0" />
          <el-option label="已开课" :value="1" />
          <el-option label="已结课" :value="2" />
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

    <el-table v-loading="loading" :data="courseList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="课程编号" align="center" prop="courseNo" width="120" />
      <el-table-column label="课程名称" align="center" prop="name" :show-overflow-tooltip="true" />
      <el-table-column label="授课教师" align="center" prop="teacherName" width="120">
        <template #default="scope">
          {{ scope.row.teacherName === '待定教师' ? '待定' : (scope.row.teacherName || '待定') }}
        </template>
      </el-table-column>
      <el-table-column label="学分" align="center" prop="credit" width="80" />
      <el-table-column label="学时" align="center" prop="hours" width="80" />
      <el-table-column label="学期" align="center" prop="semester" width="100" />
      <el-table-column label="学年" align="center" prop="year" width="80" />
      <el-table-column label="最大选课人数" align="center" prop="maxStudents" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form :model="form" :rules="rules" ref="courseRef" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="课程编号" prop="courseNo">
              <el-input v-model="form.courseNo" placeholder="请输入课程编号" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入课程名称" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="学分" prop="credit">
              <el-input-number v-model="form.credit" :min="0.5" :max="10" :step="0.5" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学时" prop="hours">
              <el-input-number v-model="form.hours" :min="1" :max="200" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="学期" prop="semester">
              <el-input v-model="form.semester" placeholder="请输入学期" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学年" prop="year">
              <el-input-number v-model="form.year" :min="2000" :max="2100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="授课教师" prop="teacherId">
              <el-select v-model="form.teacherId" placeholder="请选择授课教师" style="width: 100%">
                <el-option label="授课教师待定" :value="0" />
                <el-option v-for="teacher in teacherList" :key="teacher.id" :label="teacher.teacherName" :value="teacher.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大人数" prop="maxStudents">
              <el-input-number v-model="form.maxStudents" :min="1" :max="200" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="0">未开课</el-radio>
                <el-radio :value="1">已开课</el-radio>
                <el-radio :value="2">已结课</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="描述" prop="description">
              <el-input v-model="form.description" type="textarea" placeholder="请输入课程描述" :rows="3" />
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

<script setup name="Course">
import { listCourse, listTeachers, getCourse, addCourse, updateCourse, delCourse } from "@/api/course/course";

const { proxy } = getCurrentInstance();

const courseList = ref([]);
const teacherList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const columns = ref([
  { key: 0, label: `课程编号`, visible: true },
  { key: 1, label: `课程名称`, visible: true },
  { key: 2, label: `授课教师`, visible: true },
  { key: 3, label: `学分`, visible: true },
  { key: 4, label: `学时`, visible: true },
  { key: 5, label: `学期`, visible: true },
  { key: 6, label: `学年`, visible: true },
  { key: 7, label: `最大选课人数`, visible: true },
  { key: 8, label: `状态`, visible: true }
]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: undefined,
    courseNo: undefined,
    status: undefined
  },
  rules: {
    courseNo: [{ required: true, message: "课程编号不能为空", trigger: "blur" }],
    name: [{ required: true, message: "课程名称不能为空", trigger: "blur" }],
    credit: [{ required: true, message: "学分不能为空", trigger: "blur" }],
    hours: [{ required: true, message: "学时不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getStatusTagType(status) {
  const typeMap = { 0: "info", 1: "success", 2: "warning" };
  return typeMap[status] || "info";
}

function getStatusText(status) {
  const textMap = { 0: "未开课", 1: "已开课", 2: "已结课" };
  return textMap[status] || "未知";
}

function getTeacherList() {
  listTeachers().then(res => {
    teacherList.value = res.data || [];
  });
}

function getList() {
  loading.value = true;
  listCourse(queryParams.value).then(res => {
    loading.value = false;
    courseList.value = res.data || [];
    total.value = courseList.value.length;
  });
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

function reset() {
  form.value = {
    id: undefined,
    courseNo: undefined,
    name: undefined,
    credit: 1.0,
    hours: 32,
    teacherId: 0,
    semester: undefined,
    year: new Date().getFullYear(),
    maxStudents: 50,
    status: 0,
    description: undefined
  };
  proxy.resetForm("courseRef");
}

function cancel() {
  open.value = false;
  reset();
}

function handleAdd() {
  reset();
  getTeacherList();
  open.value = true;
  title.value = "添加课程";
}

function handleUpdate(row) {
  reset();
  getTeacherList();
  const id = row.id || ids.value;
  getCourse(id).then(res => {
    form.value = res.data;
    // 如果teacherId对应的是"待定教师"，设置为0表示待定
    if (form.value.teacherName === '待定教师') {
      form.value.teacherId = 0;
    }
    open.value = true;
    title.value = "修改课程";
  });
}

function submitForm() {
  proxy.$refs["courseRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        // 更新时删除日期字段，让后端处理
        const updateData = { ...form.value };
        delete updateData.createTime;
        delete updateData.updateTime;
        delete updateData.teacherName; // 这个字段不需要传到后端
        updateCourse(updateData).then(() => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        // 新增时也删除不需要的字段
        const addData = { ...form.value };
        delete addData.teacherName;
        addCourse(addData).then(() => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  const courseIds = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除？').then(() => {
    return delCourse(courseIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => { });
}

getList();
</script>
