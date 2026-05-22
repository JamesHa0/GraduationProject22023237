<template>
  <div class="app-container">
    <!-- 申请表单区域 -->
    <el-card shadow="never" class="mb20">
      <template #header>
        <div class="card-header">
          <span class="card-title">导师更换申请</span>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="changeRef" label-width="100px">
        <el-alert v-if="hasPendingApplication" title="您有待审批的更换申请，暂不能再次申请" type="warning" :closable="false" show-icon class="mb10" />
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
          <el-col :span="12">
            <el-form-item label="原导师">
              <el-input v-model="originalMentorName" placeholder="自动获取" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新导师" prop="newMentorId">
              <el-select v-model="form.newMentorId" placeholder="请选择新导师" style="width: 100%" :disabled="!hasCurrentMentor || hasPendingApplication">
                <el-option
                  v-for="mentor in mentorList.filter(m => m.id !== form.originalMentorId)"
                  :key="mentor.id"
                  :label="mentor.teacherName + ' (' + mentor.department + ')'"
                  :value="mentor.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="更换原因" prop="changeReason">
              <el-input v-model="form.changeReason" type="textarea" :rows="4" placeholder="请输入更换原因" :disabled="!hasCurrentMentor || hasPendingApplication" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item>
              <el-button type="primary" @click="submitForm" :loading="submitLoading" :disabled="!hasCurrentMentor || hasPendingApplication">提交申请</el-button>
              <el-button @click="resetForm" :disabled="!hasCurrentMentor || hasPendingApplication">重置</el-button>
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
        <el-table-column label="原导师" align="center" prop="originalMentorName" :show-overflow-tooltip="true" />
        <el-table-column label="新导师" align="center" prop="newMentorName" :show-overflow-tooltip="true" />
        <el-table-column label="原导师审批" align="center" prop="originalMentorStatus" :show-overflow-tooltip="true">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.originalMentorStatus)">
              {{ getStatusText(scope.row.originalMentorStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="新导师审批" align="center" prop="newMentorStatus" :show-overflow-tooltip="true">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.newMentorStatus)">
              {{ getStatusText(scope.row.newMentorStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="整体状态" align="center" prop="overallStatus" :show-overflow-tooltip="true">
          <template #default="scope">
            <el-tag :type="getOverallStatusType(scope.row.overallStatus)">
              {{ getOverallStatusText(scope.row.overallStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" align="center" prop="applyTime" :show-overflow-tooltip="true">
          <template #default="scope">
            {{ parseDate(scope.row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
            <el-button link type="primary" icon="Download" @click="handleExport(scope.row)">导出</el-button>
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
        <el-descriptions-item label="原导师">{{ currentRow.originalMentorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="新导师">{{ currentRow.newMentorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更换原因">
          <div style="white-space: pre-wrap">{{ currentRow.changeReason || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="原导师审批">
          <el-tag :type="getStatusType(currentRow.originalMentorStatus)">
            {{ getStatusText(currentRow.originalMentorStatus) }}
          </el-tag>
          <span v-if="currentRow.originalMentorComment" style="margin-left: 10px;">意见: {{ currentRow.originalMentorComment }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="新导师审批">
          <el-tag :type="getStatusType(currentRow.newMentorStatus)">
            {{ getStatusText(currentRow.newMentorStatus) }}
          </el-tag>
          <span v-if="currentRow.newMentorComment" style="margin-left: 10px;">意见: {{ currentRow.newMentorComment }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="整体状态">
          <el-tag :type="getOverallStatusType(currentRow.overallStatus)">
            {{ getOverallStatusText(currentRow.overallStatus) }}
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

<script setup name="MentorChangeApplication">
import { listMentorChange, getMentorChangeDetail, submitMentorChange, getStudentCurrentMentor, listAvailableMentors } from "@/api/student/mentorChange";
import { getCurrentInstance, ref, reactive, toRefs, watch } from "vue";
import useUserStore from '@/store/modules/user';
import { saveAs } from 'file-saver';
import axios from 'axios';
import { getToken } from '@/utils/auth';

const { proxy } = getCurrentInstance();
const userStore = useUserStore();

const changeList = ref([]);
const loading = ref(true);
const submitLoading = ref(false);
const total = ref(0);
const detailDialogVisible = ref(false);
const currentRow = ref(null);
const changeRef = ref(null);
const originalMentorName = ref('');
const mentorList = ref([]);
const hasCurrentMentor = ref(true);
const currentStudentId = ref(null);
const hasPendingApplication = ref(false);

const data = reactive({
  form: {
    studentId: undefined,
    studentNo: undefined,
    studentName: undefined,
    originalMentorId: undefined,
    newMentorId: undefined,
    changeReason: undefined
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10
  },
  rules: {
    newMentorId: [{ required: true, message: "请选择新导师", trigger: "change" }],
    changeReason: [{ required: true, message: "请输入更换原因", trigger: "blur" }]
  }
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
  if (isNaN(date.getTime())) return '-';
  return date.toLocaleString('zh-CN');
}

// 获取当前学生信息
// 后端getRoleInfo对学生角色(6)返回Student对象，字段为: id, studentNo, studentName等
function getCurrentStudentInfo() {
  if (userStore.roleInfo && userStore.roleInfo[0]) {
    const roleInfo = userStore.roleInfo[0];
    // roleInfo.id 是 student 表主键，即学生ID
    form.value.studentNo = roleInfo.studentNo || '';
    form.value.studentName = roleInfo.studentName || userStore.name || '';
    currentStudentId.value = roleInfo.id || userStore.userId;
    form.value.studentId = currentStudentId.value;
  } else if (userStore.name) {
    form.value.studentName = userStore.name;
    currentStudentId.value = userStore.userId;
    form.value.studentId = currentStudentId.value;
  }
}

async function loadMentorList() {
  try {
    const res = await listAvailableMentors();
    mentorList.value = res.data || [];
  } catch (error) {
    console.error('加载导师列表失败', error);
  }
}

async function loadCurrentMentor() {
  if (!currentStudentId.value) return;

  try {
    const res = await getStudentCurrentMentor(currentStudentId.value);
    if (res.data && res.data.id) {
      originalMentorName.value = res.data.teacherName || '';
      form.value.originalMentorId = res.data.id;
      hasCurrentMentor.value = true;
    } else {
      hasCurrentMentor.value = false;
      proxy.$modal.msgWarning('您当前没有导师，无法申请更换导师');
    }
  } catch (error) {
    console.error('获取当前导师失败', error);
    hasCurrentMentor.value = false;
  }
}

function getList() {
  loading.value = true;
  const params = {
    ...queryParams.value,
    studentId: currentStudentId.value
  };
  listMentorChange(params).then(res => {
    loading.value = false;
    changeList.value = res.data.records || res.data || [];
    total.value = res.data.total || changeList.value.length;
    hasPendingApplication.value = changeList.value.some(item => item.overallStatus === 0 || item.overallStatus === 1);
  }).catch(() => {
    loading.value = false;
  });
}

function resetForm() {
  form.value = {
    studentId: currentStudentId.value,
    studentNo: form.value.studentNo,
    studentName: form.value.studentName,
    originalMentorId: form.value.originalMentorId,
    newMentorId: undefined,
    changeReason: undefined
  };
  if (changeRef.value) {
    proxy.resetForm("changeRef");
  }
}

function submitForm() {
  proxy.$refs["changeRef"].validate(valid => {
    if (valid) {
      submitLoading.value = true;
      submitMentorChange(form.value).then(() => {
        proxy.$modal.msgSuccess("提交成功");
        resetForm();
        getList();
      }).finally(() => {
        submitLoading.value = false;
      });
    }
  });
}

function handleView(row) {
  getMentorChangeDetail(row.id).then(res => {
    currentRow.value = res.data;
    detailDialogVisible.value = true;
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

// 监听 roleInfo 变化，统一处理初始化和数据更新
// immediate:true 会在组件创建时立即触发，无需 onMounted 重复加载
watch(
  () => userStore.roleInfo,
  async (newVal) => {
    if (newVal && newVal[0]) {
      getCurrentStudentInfo();
      await loadMentorList();
      await loadCurrentMentor();
      if (currentStudentId.value) {
        getList();
      }
    }
  },
  { deep: true, immediate: true }
);
</script>

<style scoped>
.mb10 {
  margin-bottom: 10px;
}

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
