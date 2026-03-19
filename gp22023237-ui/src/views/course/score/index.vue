<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="学生ID" prop="studentId">
        <el-input v-model="queryParams.studentId" placeholder="请输入学生ID" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="课程ID" prop="courseId">
        <el-input v-model="queryParams.courseId" placeholder="请输入课程ID" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="成绩等级" prop="grade">
        <el-select v-model="queryParams.grade" placeholder="成绩等级" clearable style="width: 120px">
          <el-option label="优秀" value="A" />
          <el-option label="良好" value="B" />
          <el-option label="中等" value="C" />
          <el-option label="及格" value="D" />
          <el-option label="不及格" value="F" />
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

    <el-table v-loading="loading" :data="scoreList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="学生ID" align="center" prop="studentId" width="100" />
      <el-table-column label="课程ID" align="center" prop="courseId" width="100" />
      <el-table-column label="平时成绩" align="center" prop="usualScore" width="100" />
      <el-table-column label="期末成绩" align="center" prop="examScore" width="100" />
      <el-table-column label="总成绩" align="center" prop="totalScore" width="100" />
      <el-table-column label="成绩等级" align="center" prop="grade" width="100">
        <template #default="scope">
          <el-tag :type="getGradeTagType(scope.row.grade)">
            {{ scope.row.grade }}
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
      <el-form :model="form" :rules="rules" ref="scoreRef" label-width="80px">
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
          <el-col :span="8">
            <el-form-item label="平时成绩" prop="usualScore">
              <el-input-number v-model="form.usualScore" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="期末成绩" prop="examScore">
              <el-input-number v-model="form.examScore" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总成绩" prop="totalScore">
              <el-input-number v-model="form.totalScore" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="成绩等级" prop="grade">
              <el-select v-model="form.grade" placeholder="请选择成绩等级" style="width: 100%">
                <el-option label="优秀" value="A" />
                <el-option label="良好" value="B" />
                <el-option label="中等" value="C" />
                <el-option label="及格" value="D" />
                <el-option label="不及格" value="F" />
              </el-select>
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

<script setup name="Score">
import { listScore, getScore, addScore, updateScore, delScore } from "@/api/course/score";

const { proxy } = getCurrentInstance();

const scoreList = ref([]);
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
    grade: undefined
  },
  rules: {
    studentId: [{ required: true, message: "学生ID不能为空", trigger: "blur" }],
    courseId: [{ required: true, message: "课程ID不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getGradeTagType(grade) {
  const typeMap = { A: "success", B: "primary", C: "warning", D: "info", F: "danger" };
  return typeMap[grade] || "info";
}

function getList() {
  loading.value = true;
  listScore(queryParams.value).then(res => {
    loading.value = false;
    scoreList.value = res.data || [];
    total.value = scoreList.value.length;
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
    usualScore: 0,
    examScore: 0,
    totalScore: 0,
    grade: undefined
  };
  proxy.resetForm("scoreRef");
}

function cancel() {
  open.value = false;
  reset();
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加成绩记录";
}

function handleUpdate(row) {
  reset();
  getScore(row.id).then(res => {
    form.value = res.data;
    open.value = true;
    title.value = "修改成绩记录";
  });
}

function submitForm() {
  proxy.$refs["scoreRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        updateScore(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addScore(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  const scoreIds = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除？').then(() => {
    return delScore(scoreIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>
