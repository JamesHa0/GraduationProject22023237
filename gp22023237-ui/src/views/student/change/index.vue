<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="学号" prop="studentNo">
        <el-input v-model="queryParams.studentNo" placeholder="请输入学号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="姓名" prop="studentName">
        <el-input v-model="queryParams.studentName" placeholder="请输入姓名" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="变更类型" prop="changeType">
        <el-select v-model="queryParams.changeType" placeholder="请选择" clearable style="width: 200px">
          <el-option label="休学" :value="1" />
          <el-option label="复学" :value="2" />
          <el-option label="退学" :value="3" />
          <el-option label="延期毕业" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增申请</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="changeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="学号" align="center" prop="studentNo" width="120" />
      <el-table-column label="姓名" align="center" prop="studentName" :show-overflow-tooltip="true" />
      <el-table-column label="变更类型" align="center" prop="changeType" width="100">
        <template #default="scope">
          <el-tag>{{ getTypeName(scope.row.changeType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="生效日期" align="center" prop="effectiveDate" width="120">
        <template #default="scope">
          {{ parseDate(scope.row.effectiveDate) }}
        </template>
      </el-table-column>
      <el-table-column label="导师审批" align="center" prop="mentorStatus" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.mentorStatus)">
            {{ getStatusText(scope.row.mentorStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="教学秘书审批" align="center" prop="secretaryStatus" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.secretaryStatus)">
            {{ getStatusText(scope.row.secretaryStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分管院长审批" align="center" prop="deanStatus" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.deanStatus)">
            {{ getStatusText(scope.row.deanStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="申请时间" align="center" prop="applyTime" width="170">
        <template #default="scope">
          {{ parseDate(scope.row.applyTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)"></el-button>
          <el-button link type="primary" icon="Edit" :disabled="scope.row.mentorStatus !== 0" @click="handleUpdate(scope.row)"></el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form :model="form" :rules="rules" ref="changeRef" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="变更类型" prop="changeType">
              <el-radio-group v-model="form.changeType">
                <el-radio :value="1">休学</el-radio>
                <el-radio :value="2">复学</el-radio>
                <el-radio :value="3">退学</el-radio>
                <el-radio :value="4">延期毕业</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="form.studentNo" placeholder="请输入学号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="studentName">
              <el-input v-model="form.studentName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="生效日期" prop="effectiveDate">
              <el-date-picker v-model="form.effectiveDate" type="date" placeholder="选择生效日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="变更原因" prop="reason">
              <el-input v-model="form.reason" type="textarea" :rows="4" placeholder="请输入变更原因" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="附件" prop="attachmentPath">
              <el-input v-model="form.attachmentPath" placeholder="请上传附件" />
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

<script setup name="StudentStatusChange">
import { listStatusChange, getStatusChangeDetail, submitStatusChange } from "@/api/student/status";
import { getCurrentInstance, ref, reactive, toRefs } from "vue";

const { proxy } = getCurrentInstance();

const changeList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const columns = ref([
  { key: 0, label: `学号`, visible: true },
  { key: 1, label: `姓名`, visible: true },
  { key: 2, label: `变更类型`, visible: true },
  { key: 3, label: `生效日期`, visible: true },
  { key: 4, label: `导师审批`, visible: true },
  { key: 5, label: `教学秘书审批`, visible: true },
  { key: 6, label: `分管院长审批`, visible: true },
  { key: 7, label: `申请时间`, visible: true }
]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentNo: undefined,
    studentName: undefined,
    changeType: undefined
  },
  rules: {
    changeType: [{ required: true, message: "请选择变更类型", trigger: "change" }],
    studentNo: [{ required: true, message: "请输入学号", trigger: "blur" }],
    studentName: [{ required: true, message: "请输入姓名", trigger: "blur" }],
    reason: [{ required: true, message: "请输入变更原因", trigger: "blur" }],
    effectiveDate: [{ required: true, message: "请选择生效日期", trigger: "change" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getTypeName(type) {
  const map = { 1: '休学', 2: '复学', 3: '退学', 4: '延期毕业' };
  return map[type] || '-';
}

function getStatusText(status) {
  const map = { 0: '待审批', 1: '已通过', 2: '已拒绝' };
  return map[status] || '-';
}

function getStatusType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' };
  return map[status] || 'info';
}

function parseDate(dateStr) {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN');
}

function getList() {
  loading.value = true;
  listStatusChange(queryParams.value).then(res => {
    loading.value = false;
    changeList.value = res.data.records || res.data || [];
    total.value = res.data.total || changeList.value.length;
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
    studentId: undefined,
    studentNo: undefined,
    studentName: undefined,
    changeType: undefined,
    reason: undefined,
    attachmentPath: undefined,
    effectiveDate: new Date(),
    mentorStatus: 0,
    secretaryStatus: 0,
    deanStatus: 0
  };
  proxy.resetForm("changeRef");
}

function cancel() {
  open.value = false;
  reset();
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "新增申请";
}

function handleView(row) {
  getStatusChangeDetail(row.id).then(res => {
    proxy.$modal.detail(res.data);
  });
}

function handleUpdate(row) {
  reset();
  const id = row.id || ids.value;
  getStatusChangeDetail(id).then(res => {
    form.value = res.data;
    open.value = true;
    title.value = "修改申请";
  });
}

function submitForm() {
  proxy.$refs["changeRef"].validate(valid => {
    if (valid) {
      submitStatusChange(form.value).then(() => {
        proxy.$modal.msgSuccess("提交成功");
        open.value = false;
        getList();
      });
    }
  });
}

getList();
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
