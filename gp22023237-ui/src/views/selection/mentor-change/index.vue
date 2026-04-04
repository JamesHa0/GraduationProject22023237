<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="申请状态" prop="overallStatus">
        <el-select v-model="queryParams.overallStatus" placeholder="请选择" clearable style="width: 200px">
          <el-option label="待原导师审批" :value="0" />
          <el-option label="待新导师审批" :value="1" />
          <el-option label="已通过" :value="2" />
          <el-option label="已拒绝" :value="3" />
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

    <el-table v-loading="loading" :data="changeList">
      <el-table-column label="学号" align="center" prop="studentNo" width="120" />
      <el-table-column label="姓名" align="center" prop="studentName" :show-overflow-tooltip="true" />
      <el-table-column label="原导师" align="center" prop="originalMentorName" :show-overflow-tooltip="true" />
      <el-table-column label="新导师" align="center" prop="newMentorName" :show-overflow-tooltip="true" />
      <el-table-column label="原导师审批" align="center" prop="originalMentorStatus" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.originalMentorStatus)">
            {{ getStatusText(scope.row.originalMentorStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="新导师审批" align="center" prop="newMentorStatus" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.newMentorStatus)">
            {{ getStatusText(scope.row.newMentorStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="整体状态" align="center" prop="overallStatus" width="120">
        <template #default="scope">
          <el-tag :type="getOverallStatusType(scope.row.overallStatus)">
            {{ getOverallStatusText(scope.row.overallStatus) }}
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
          <el-button link type="primary" icon="Edit" :disabled="scope.row.overallStatus !== 0" @click="handleUpdate(scope.row)"></el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form :model="form" :rules="rules" ref="changeRef" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="学号">
              <el-input v-model="form.studentNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input v-model="form.studentName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="原导师">
              <el-input v-model="originalMentorName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新导师" prop="newMentorId">
              <el-select v-model="form.newMentorId" placeholder="请选择新导师" style="width: 100%" :disabled="isViewMode">
                <el-option v-for="mentor in mentorList" :key="mentor.id" :label="mentor.teacherName" :value="mentor.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="更换原因" prop="changeReason">
              <el-input v-model="form.changeReason" type="textarea" :rows="4" placeholder="请输入更换原因" :disabled="isViewMode" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="isViewMode">
          <el-col :span="12">
            <el-form-item label="原导师审批">
              <el-tag :type="getStatusType(form.originalMentorStatus)">
                {{ getStatusText(form.originalMentorStatus) }}
              </el-tag>
              <span v-if="form.originalMentorComment" style="margin-left: 10px;">{{ form.originalMentorComment }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新导师审批">
              <el-tag :type="getStatusType(form.newMentorStatus)">
                {{ getStatusText(form.newMentorStatus) }}
              </el-tag>
              <span v-if="form.newMentorComment" style="margin-left: 10px;">{{ form.newMentorComment }}</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer" v-if="!isViewMode">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
        <div class="dialog-footer" v-else>
          <el-button @click="cancel">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MentorChange">
import { listMentorChange, getMentorChangeDetail, submitMentorChange, getStudentCurrentMentor } from "@/api/student/mentorChange";
import { listMentor } from "@/api/student/selection";
import { getCurrentInstance, ref, reactive, toRefs } from "vue";
import useUserStore from '@/store/modules/user';

const { proxy } = getCurrentInstance();

const changeList = ref([]);
const mentorList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const title = ref("");
const isViewMode = ref(false);
const originalMentorName = ref('');

// 当前学生信息
const currentStudent = reactive({
  id: null,
  studentNo: '',
  studentName: ''
});

// 当前学生的原导师
const currentMentor = reactive({
  id: null,
  teacherName: ''
});

const columns = ref([
  { key: 0, label: `学号`, visible: true },
  { key: 1, label: `姓名`, visible: true },
  { key: 2, label: `原导师`, visible: true },
  { key: 3, label: `新导师`, visible: true },
  { key: 4, label: `原导师审批`, visible: true },
  { key: 5, label: `新导师审批`, visible: true },
  { key: 6, label: `整体状态`, visible: true },
  { key: 7, label: `申请时间`, visible: true }
]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    overallStatus: undefined
  },
  rules: {
    newMentorId: [{ required: true, message: "请选择新导师", trigger: "change" }],
    changeReason: [{ required: true, message: "请输入更换原因", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

// 获取当前学生信息
function getCurrentStudentInfo() {
  const userStore = useUserStore();
  if (userStore.roleInfo && userStore.roleInfo[0]) {
    currentStudent.id = userStore.roleInfo[0].id;
    currentStudent.studentNo = userStore.roleInfo[0].userName || userStore.roleInfo[0].studentNo || '';
    currentStudent.studentName = userStore.roleInfo[0].nickName || userStore.roleInfo[0].studentName || '';
    return true;
  } else {
    proxy.$modal.msgError('学生信息尚未加载');
    return false;
  }
}

function getStatusText(status) {
  const map = { 0: '待审批', 1: '已通过', 2: '已拒绝' };
  return map[status] || '-';
}

function getStatusType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' };
  return map[status] || 'info';
}

function getOverallStatusText(status) {
  const map = { 0: '待原导师审批', 1: '待新导师审批', 2: '已通过', 3: '已拒绝' };
  return map[status] || '-';
}

function getOverallStatusType(status) {
  const map = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger' };
  return map[status] || 'info';
}

function parseDate(dateStr) {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN');
}

function getList() {
  if (!getCurrentStudentInfo()) {
    loading.value = false;
    return;
  }

  loading.value = true;
  const params = {
    ...queryParams.value,
    studentId: currentStudent.id
  };
  listMentorChange(params).then(res => {
    loading.value = false;
    changeList.value = res.data.records || res.data || [];
    total.value = res.data.total || changeList.value.length;
  });
}

function getMentorList() {
  listMentor({}).then(res => {
    mentorList.value = res.data || [];
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

function reset() {
  form.value = {
    id: undefined,
    studentId: currentStudent.id,
    studentNo: currentStudent.studentNo,
    studentName: currentStudent.studentName,
    originalMentorId: currentMentor.id,
    newMentorId: undefined,
    changeReason: undefined,
    originalMentorStatus: 0,
    newMentorStatus: 0,
    overallStatus: 0
  };
  originalMentorName.value = currentMentor.teacherName || '';
  isViewMode.value = false;
  proxy.resetForm("changeRef");
}

// 获取学生当前导师信息
async function fetchCurrentMentor() {
  if (!currentStudent.id) {
    return;
  }
  try {
    const res = await getStudentCurrentMentor(currentStudent.id);
    if (res.data) {
      currentMentor.id = res.data.id;
      currentMentor.teacherName = res.data.teacherName || '';
    } else {
      currentMentor.id = null;
      currentMentor.teacherName = '';
      proxy.$modal.msgWarning('您当前没有导师，无法申请更换导师');
    }
  } catch (e) {
    console.error('获取当前导师信息失败', e);
    currentMentor.id = null;
    currentMentor.teacherName = '';
  }
}

function cancel() {
  open.value = false;
  reset();
}

async function handleAdd() {
  if (!getCurrentStudentInfo()) {
    return;
  }
  await fetchCurrentMentor();
  if (!currentMentor.id) {
    return;
  }
  reset();
  getMentorList();
  open.value = true;
  title.value = "新增申请";
}

function handleView(row) {
  reset();
  getMentorList();
  getMentorChangeDetail(row.id).then(res => {
    form.value = res.data;
    originalMentorName.value = res.data.originalMentorName || '';
    isViewMode.value = true;
    open.value = true;
    title.value = "查看详情";
  });
}

async function handleUpdate(row) {
  if (!getCurrentStudentInfo()) {
    return;
  }
  reset();
  getMentorList();
  getMentorChangeDetail(row.id).then(res => {
    form.value = res.data;
    originalMentorName.value = res.data.originalMentorName || '';
    isViewMode.value = false;
    open.value = true;
    title.value = "修改申请";
  });
}

function submitForm() {
  proxy.$refs["changeRef"].validate(valid => {
    if (valid) {
      submitMentorChange(form.value).then(() => {
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
