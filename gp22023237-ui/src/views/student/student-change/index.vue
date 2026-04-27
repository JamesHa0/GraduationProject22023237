<template>
  <div class="app-container">
    <!-- 申请表单区域 -->
    <el-card shadow="never" class="mb20">
      <template #header>
        <div class="card-header">
          <span class="card-title">学籍异动申请</span>
        </div>
      </template>

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
              <el-input v-model="form.studentNo" placeholder="自动获取" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="studentName">
              <el-input v-model="form.studentName" placeholder="自动获取" disabled />
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
            <el-form-item>
              <el-button type="primary" @click="submitForm" :loading="submitLoading">提交申请</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 申请历史记录 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">我的申请记录</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="changeList" border>
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="变更类型" align="center" prop="changeType" :show-overflow-tooltip="true">
          <template #default="scope">
            <el-tag>{{ getTypeName(scope.row.changeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="生效日期" align="center" prop="effectiveDate" :show-overflow-tooltip="true">
          <template #default="scope">
            {{ parseDate(scope.row.effectiveDate) }}
          </template>
        </el-table-column>
        <el-table-column label="导师审批" align="center" prop="mentorStatus" :show-overflow-tooltip="true">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.mentorStatus)">
              {{ getStatusText(scope.row.mentorStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="教学秘书审批" align="center" prop="secretaryStatus" :show-overflow-tooltip="true">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.secretaryStatus)">
              {{ getStatusText(scope.row.secretaryStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" align="center" prop="applyTime" :show-overflow-tooltip="true">
          <template #default="scope">
            {{ parseDate(scope.row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="100" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog title="申请详情" v-model="detailDialogVisible" width="700px" append-to-body>
      <el-descriptions :column="1" border v-if="currentRow">
        <el-descriptions-item label="学号">{{ currentRow.studentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentRow.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="变更类型">
          <el-tag>{{ getTypeName(currentRow.changeType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ parseDate(currentRow.effectiveDate) }}</el-descriptions-item>
        <el-descriptions-item label="变更原因">
          <div style="white-space: pre-wrap">{{ currentRow.reason || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="导师审批">
          <el-tag :type="getStatusType(currentRow.mentorStatus)">
            {{ getStatusText(currentRow.mentorStatus) }}
          </el-tag>
          <span v-if="currentRow.mentorComment" style="margin-left: 10px;">{{ currentRow.mentorComment }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="教学秘书审批">
          <el-tag :type="getStatusType(currentRow.secretaryStatus)">
            {{ getStatusText(currentRow.secretaryStatus) }}
          </el-tag>
          <span v-if="currentRow.secretaryComment" style="margin-left: 10px;">{{ currentRow.secretaryComment }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="整体状态">
          <el-tag :type="getOverallStatusType(currentRow.status)">
            {{ getOverallStatusText(currentRow.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ parseDate(currentRow.applyTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="StudentChangeApply">
import { listStatusChange, getStatusChangeDetail, submitStatusChange } from "@/api/student/status";
import { getCurrentInstance, ref, reactive, toRefs, onMounted, watch } from "vue";
import useUserStore from '@/store/modules/user';

const { proxy } = getCurrentInstance();
const userStore = useUserStore();

const changeList = ref([]);
const loading = ref(true);
const submitLoading = ref(false);
const total = ref(0);
const detailDialogVisible = ref(false);
const currentRow = ref(null);
const changeRef = ref(null);

const data = reactive({
  form: {
    studentNo: undefined,
    studentName: undefined,
    changeType: undefined,
    reason: undefined
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10
  },
  rules: {
    changeType: [{ required: true, message: "请选择变更类型", trigger: "change" }],
    reason: [{ required: true, message: "请输入变更原因", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getTypeName(type) {
  const map = { 1: '休学', 2: '复学', 3: '退学', 4: '延期毕业' };
  return map[type] || '-';
}

// 单级审批状态
function getStatusText(status) {
  const map = { 0: '待审批', 1: '已通过', 2: '已拒绝' };
  return map[status] || '-';
}

function getStatusType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' };
  return map[status] || 'info';
}

// 整体状态
function getOverallStatusText(status) {
  const map = { 0: '待导师审批', 1: '待秘书审批', 2: '已通过', 3: '已拒绝' };
  return map[status] || '-';
}

function getOverallStatusType(status) {
  const map = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger' };
  return map[status] || 'info';
}

function parseDate(dateStr) {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return '-';
  return date.toLocaleString('zh-CN');
}

// 获取当前学生信息
function getCurrentStudentInfo() {
  if (userStore.roleInfo && userStore.roleInfo[0]) {
    const roleInfo = userStore.roleInfo[0];
    form.value.studentNo = roleInfo.studentNo || roleInfo.userName || '';
    form.value.studentName = roleInfo.studentName || roleInfo.nickname || userStore.name || '';
  } else if (userStore.name) {
    form.value.studentName = userStore.name;
  }

  if (!form.value.studentNo) {
    proxy.$modal.msgWarning("未能获取到学号信息，请确认当前账号已关联学生信息");
  }
}

function getList() {
  loading.value = true;
  // 过滤当前学生的申请记录
  const params = {
    ...queryParams.value,
    studentNo: form.value.studentNo
  };
  listStatusChange(params).then(res => {
    loading.value = false;
    changeList.value = res.data.records || res.data || [];
    total.value = res.data.total || changeList.value.length;
  }).catch(() => {
    loading.value = false;
    proxy.$modal.msgError("获取申请记录失败，请稍后重试");
  });
}

function resetForm() {
  form.value = {
    studentNo: form.value.studentNo,
    studentName: form.value.studentName,
    changeType: undefined,
    reason: undefined
  };
  if (changeRef.value) {
    proxy.resetForm("changeRef");
  }
}

function submitForm() {
  if (!form.value.studentNo) {
    proxy.$modal.msgWarning("学号信息缺失，无法提交申请");
    return;
  }
  proxy.$refs["changeRef"].validate(valid => {
    if (valid) {
      submitLoading.value = true;
      submitStatusChange(form.value).then(() => {
        proxy.$modal.msgSuccess("提交成功");
        resetForm();
        getList();
      }).catch(() => {
        proxy.$modal.msgError("提交申请失败，请稍后重试");
      }).finally(() => {
        submitLoading.value = false;
      });
    }
  });
}

function handleView(row) {
  getStatusChangeDetail(row.id).then(res => {
    currentRow.value = res.data;
    detailDialogVisible.value = true;
  }).catch(() => {
    proxy.$modal.msgError("获取申请详情失败");
  });
}

onMounted(() => {
  getCurrentStudentInfo();
  getList();
});

// 监听 roleInfo 变化，确保学生信息能正确加载
watch(
  () => userStore.roleInfo,
  (newVal) => {
    if (newVal && newVal[0]) {
      getCurrentStudentInfo();
      if (form.value.studentNo) {
        getList();
      }
    }
  },
  { deep: true, immediate: true }
);
</script>

<style scoped>
.mb20 {
  margin-bottom: 20px;
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
