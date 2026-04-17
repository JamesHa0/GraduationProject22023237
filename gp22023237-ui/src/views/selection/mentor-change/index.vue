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
      <el-form-item label="学号" prop="studentNo">
        <el-input v-model="queryParams.studentNo" placeholder="请输入学号" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item label="学生姓名" prop="studentName">
        <el-input v-model="queryParams.studentName" placeholder="请输入学生姓名" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
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
          <el-button link type="primary" icon="Download" @click="handleExport(scope.row)">导出</el-button>
          <el-button link type="primary" icon="View" @click="handleView(scope.row)"></el-button>
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
            <el-form-item label="新导师">
              <el-input v-model="form.newMentorName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="更换原因">
              <el-input v-model="form.changeReason" type="textarea" :rows="4" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
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
        <div class="dialog-footer">
          <el-button @click="cancel">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MentorChangeManage">
import { listMentorChange, getMentorChangeDetail } from "@/api/student/mentorChange";
import { saveAs } from 'file-saver';
import axios from 'axios';
import { getToken } from '@/utils/auth';
import { getCurrentInstance, ref, reactive, toRefs } from "vue";

const { proxy } = getCurrentInstance();

const changeList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const title = ref("");
const originalMentorName = ref('');

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
    overallStatus: undefined,
    studentNo: undefined,
    studentName: undefined
  },
  rules: {}
});

const { queryParams, form, rules } = toRefs(data);

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
  loading.value = true;
  listMentorChange(queryParams.value).then(res => {
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

function reset() {
  form.value = {};
  originalMentorName.value = '';
  proxy.resetForm("changeRef");
}

function cancel() {
  open.value = false;
  reset();
}

function handleView(row) {
  reset();
  getMentorChangeDetail(row.id).then(res => {
    form.value = res.data;
    originalMentorName.value = res.data.originalMentorName || '';
    open.value = true;
    title.value = "查看详情";
  });
}

const handleExport = (row) => {
  proxy.$modal.confirm(`确定要导出"${row.studentName}"的导师更换申请表吗？`).then(() => {
    axios({
      method: 'get',
      url: import.meta.env.VITE_APP_BASE_API + '/student/mentor-change/export/' + row.id,
      responseType: 'blob',
      headers: { 'Token': getToken() }
    }).then(response => {
      const blob = new Blob([response.data], {
        type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
      });
      saveAs(blob, `导师更换申请表_${row.studentName}.docx`);
      proxy.$modal.msgSuccess('导出成功');
    }).catch(error => {
      console.error('导出失败', error);
      proxy.$modal.msgError('导出失败');
    });
  }).catch(() => {});
};

getList();
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
