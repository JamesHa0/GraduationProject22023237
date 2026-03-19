<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="学生ID" prop="studentId">
        <el-input v-model="queryParams.studentId" placeholder="请输入学生ID" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="课程ID" prop="courseId">
        <el-input v-model="queryParams.courseId" placeholder="请输入课程ID" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="选课状态" clearable style="width: 120px">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
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
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="selectionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="学生ID" align="center" prop="studentId" width="100" />
      <el-table-column label="课程ID" align="center" prop="courseId" width="100" />
      <el-table-column label="选课时间" align="center" prop="selectionTime" width="180" />
      <el-table-column label="状态" align="center" prop="status" width="100">
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

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form :model="form" :rules="rules" ref="selectionRef" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="学生ID" prop="studentId">
              <el-input v-model="form.studentId" placeholder="请输入学生ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程ID" prop="courseId">
              <el-input v-model="form.courseId" placeholder="请输入课程ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="选课状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="0">待审核</el-radio>
                <el-radio :value="1">已通过</el-radio>
                <el-radio :value="2">已拒绝</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确定</el-button>
          <el-button @click="cancel">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CourseSelection">
import { listCourseSelection, getCourseSelection, addCourseSelection, updateCourseSelection, delCourseSelection } from "@/api/course/selection";

const { proxy } = getCurrentInstance();

const selectionList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentId: undefined,
    courseId: undefined,
    status: undefined
  },
  rules: {
    studentId: [{ required: true, message: "学生ID不能为空", trigger: "blur" }],
    courseId: [{ required: true, message: "课程ID不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getStatusTagType(status) {
  const typeMap = { 0: "warning", 1: "success", 2: "danger" };
  return typeMap[status] || "info";
}

function getStatusText(status) {
  const textMap = { 0: "待审核", 1: "已通过", 2: "已拒绝" };
  return textMap[status] || "未知";
}

function getList() {
  loading.value = true;
  listCourseSelection(queryParams.value).then(res => {
    loading.value = false;
    selectionList.value = res.data || [];
    total.value = selectionList.value.length;
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
  multiple.value = !selection.length;
}

function reset() {
  form.value = {
    id: undefined,
    studentId: undefined,
    courseId: undefined,
    status: 0
  };
  proxy.resetForm("selectionRef");
}

function cancel() {
  open.value = false;
  reset();
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加选课记录";
}

function handleUpdate(row) {
  reset();
  getCourseSelection(row.id).then(res => {
    form.value = res.data;
    open.value = true;
    title.value = "修改选课记录";
  });
}

function submitForm() {
  proxy.$refs["selectionRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        updateCourseSelection(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addCourseSelection(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  const selectionIds = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除？').then(() => {
    return delCourseSelection(selectionIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>
