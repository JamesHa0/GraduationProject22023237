<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="学号" prop="studentNo">
        <el-input v-model="queryParams.studentNo" placeholder="请输入学号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="姓名" prop="studentName">
        <el-input v-model="queryParams.studentName" placeholder="请输入姓名" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="学院" prop="department">
        <el-input v-model="queryParams.department" placeholder="请输入学院" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="专业" prop="major">
        <el-input v-model="queryParams.major" placeholder="请输入专业" clearable style="width: 200px" @keyup.enter="handleQuery" />
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
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Upload" @click="handleImport">批量导入</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="studentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="学号" align="center" prop="studentNo" width="120" />
      <el-table-column label="姓名" align="center" prop="studentName" :show-overflow-tooltip="true" />
      <el-table-column label="学院" align="center" prop="department" width="120" />
      <el-table-column label="专业" align="center" prop="major" width="120" />
      <el-table-column label="入学年份" align="center" prop="admissionYear" width="100" />
      <el-table-column label="归属年级" align="center" prop="cohortYear" width="100" />
      <el-table-column label="毕业年份" align="center" prop="graduationYear" width="100" />
      <el-table-column label="研究方向" align="center" prop="researchDirection" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)"></el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form :model="form" :rules="rules" ref="studentRef" label-width="100px" v-if="!isView">
        <el-row>
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="form.studentNo" placeholder="请输入学号" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="studentName">
              <el-input v-model="form.studentName" placeholder="请输入姓名" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="学院" prop="department">
              <el-input v-model="form.department" placeholder="请输入学院" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业" prop="major">
              <el-input v-model="form.major" placeholder="请输入专业" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="入学年份" prop="admissionYear">
              <el-input-number v-model="form.admissionYear" :min="2000" :max="2100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="归属年级" prop="cohortYear">
              <el-input-number v-model="form.cohortYear" :min="2000" :max="2100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="毕业年份" prop="graduationYear">
              <el-input-number v-model="form.graduationYear" :min="2000" :max="2100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="研究方向" prop="researchDirection">
              <el-input v-model="form.researchDirection" placeholder="请输入研究方向" maxlength="200" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">正常</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="双选状态" prop="selectionStatus">
              <el-radio-group v-model="form.selectionStatus">
                <el-radio :value="0">未开始</el-radio>
                <el-radio :value="1">第一轮</el-radio>
                <el-radio :value="2">第二轮</el-radio>
                <el-radio :value="3">已确定</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <el-descriptions :column="1" border v-if="isView && currentRow">
        <el-descriptions-item label="学号">{{ currentRow.studentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentRow.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ currentRow.department || '-' }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ currentRow.major || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入学年份">{{ currentRow.admissionYear || '-' }}</el-descriptions-item>
        <el-descriptions-item label="归属年级">{{ currentRow.cohortYear || '-' }}</el-descriptions-item>
        <el-descriptions-item label="毕业年份">{{ currentRow.graduationYear || '-' }}</el-descriptions-item>
        <el-descriptions-item label="研究方向">{{ currentRow.researchDirection || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(currentRow.status)">{{ getStatusText(currentRow.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="双选状态">{{ getSelectionStatusText(currentRow.selectionStatus) }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">{{ isView ? '关闭' : '取 消' }}</el-button>
          <el-button type="primary" @click="submitForm" v-if="!isView">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="批量导入学生" v-model="importOpen" width="700px" append-to-body>
      <el-alert title="导入提示" type="info" :closable="false" show-icon style="margin-bottom: 12px;">
        <template #default>
          <div>请先下载模板（当前版本：{{ templateVersion }}），按“学生模板”Sheet填写，CSV请保持UTF-8编码和模板列名一致。</div>
          <el-button link type="primary" @click="downloadTemplate">点击下载导入模板</el-button>
        </template>
      </el-alert>

      <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 20px;">
        <template #default>
          <div>关键规则：学号唯一；年份范围2000-2100；状态仅支持0/1；双选状态仅支持0/1/2/3。</div>
        </template>
      </el-alert>

      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
        accept=".xlsx,.xls,.csv"
        drag>
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 .xls/.xlsx/.csv，单文件不超过10MB
          </div>
        </template>
      </el-upload>

      <div v-if="taskInfo" style="margin-top: 16px;">
        <el-divider content-position="left">导入进度</el-divider>
        <el-progress :percentage="taskInfo.progress || 0" :status="progressStatus" :stroke-width="18" />
        <div style="margin-top: 8px; color: #606266;">{{ taskInfo.message || '正在处理...' }}</div>
      </div>

      <div v-if="importResult" style="margin-top: 20px;">
        <el-divider content-position="left">导入结果</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="总记录数">{{ importResult.total }}</el-descriptions-item>
          <el-descriptions-item label="成功数量">
            <span style="color: #67C23A;">{{ importResult.successCount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="失败数量">
            <span style="color: #F56C6C;">{{ importResult.failCount }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="importResult.failDetails && importResult.failDetails.length > 0" style="margin-top: 15px;">
          <el-alert title="失败详情（请按建议修复后重试）" type="warning" :closable="false">
            <ul style="margin: 0; padding-left: 20px; max-height: 220px; overflow-y: auto;">
              <li v-for="(item, index) in importResult.failDetails" :key="index" style="margin-bottom: 8px;">
                第{{ item.row }}行（学号：{{ item.studentNo || '-' }})
                <span> - {{ item.reason }}</span>
                <span v-if="item.errorCode">（{{ item.errorCode }}）</span>
                <div v-if="item.field || item.suggestion" style="color: #909399; margin-top: 2px;">
                  <span v-if="item.field">字段：{{ item.field }}；</span>
                  <span v-if="item.suggestion">建议：{{ item.suggestion }}</span>
                </div>
              </li>
            </ul>
          </el-alert>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitImport" :loading="importLoading" :disabled="!uploadFile">开始导入</el-button>
          <el-button @click="cancelImport">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Student">
import {
  listStudent,
  getStudentDetail,
  addStudent,
  updateStudent,
  deleteStudent,
  createStudentImportTask,
  queryStudentImportTask,
  importStudent,
  downloadStudentImportTemplate
} from "@/api/student/info";
import { UploadFilled } from '@element-plus/icons-vue';
import { getCurrentInstance, ref, reactive, toRefs, onBeforeUnmount } from "vue";

const { proxy } = getCurrentInstance();

const studentList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const isView = ref(false);
const currentRow = ref(null);

const importOpen = ref(false);
const importLoading = ref(false);
const uploadFile = ref(null);
const uploadRef = ref();
const importResult = ref(null);
const templateVersion = ref('v1.0');
const taskInfo = ref(null);
const currentTaskId = ref('');
const progressStatus = ref('');
let pollTimer = null;

const columns = ref([
  { key: 0, label: `学号`, visible: true },
  { key: 1, label: `姓名`, visible: true },
  { key: 2, label: `学院`, visible: true },
  { key: 3, label: `专业`, visible: true },
  { key: 4, label: `入学年份`, visible: true },
  { key: 5, label: `归属年级`, visible: true },
  { key: 6, label: `毕业年份`, visible: true },
  { key: 7, label: `研究方向`, visible: true },
  { key: 8, label: `状态`, visible: true }
]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentNo: undefined,
    studentName: undefined,
    department: undefined,
    major: undefined
  },
  rules: {
    studentNo: [{ required: true, message: "学号不能为空", trigger: "blur" }],
    studentName: [{ required: true, message: "姓名不能为空", trigger: "blur" }],
    department: [{ required: true, message: "学院不能为空", trigger: "blur" }],
    major: [{ required: true, message: "专业不能为空", trigger: "blur" }],
    admissionYear: [{ required: true, message: "入学年份不能为空", trigger: "blur" }],
    graduationYear: [{ required: true, message: "毕业年份不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getStatusTagType(status) {
  const typeMap = { 1: "success", 0: "danger" };
  return typeMap[status] || "info";
}

function getStatusText(status) {
  const textMap = { 1: "正常", 0: "禁用" };
  return textMap[status] || "未知";
}

function getSelectionStatusText(status) {
  const textMap = { 0: "未开始", 1: "第一轮", 2: "第二轮", 3: "已确定" };
  return textMap[status] || "未知";
}

function getList() {
  loading.value = true;
  listStudent(queryParams.value).then(res => {
    loading.value = false;
    studentList.value = res.data || [];
    total.value = studentList.value.length;
  }).catch(() => {
    loading.value = false;
    studentList.value = [];
    total.value = 0;
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
    studentNo: undefined,
    studentName: undefined,
    department: undefined,
    major: undefined,
    admissionYear: new Date().getFullYear(),
    cohortYear: new Date().getFullYear(),
    graduationYear: new Date().getFullYear() + 4,
    researchDirection: undefined,
    status: 1,
    selectionStatus: 0
  };
  isView.value = false;
  currentRow.value = null;
  proxy.resetForm("studentRef");
}

function cancel() {
  open.value = false;
  reset();
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "新增学生";
}

function handleView(row) {
  currentRow.value = row;
  isView.value = true;
  title.value = "学生详情";
  open.value = true;
}

function handleUpdate(row) {
  reset();
  const id = row.id || ids.value;
  getStudentDetail(id).then(res => {
    form.value = res.data;
    open.value = true;
    title.value = "修改学生";
  });
}

function submitForm() {
  proxy.$refs["studentRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        updateStudent(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addStudent(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  const studentIds = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除？').then(() => {
    return deleteStudent(studentIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

function handleImport() {
  resetImport();
  importOpen.value = true;
}

function resetImport() {
  uploadFile.value = null;
  importResult.value = null;
  taskInfo.value = null;
  currentTaskId.value = '';
  progressStatus.value = '';
  stopPolling();
  if (uploadRef.value) {
    uploadRef.value.clearFiles();
  }
}

function cancelImport() {
  importOpen.value = false;
  resetImport();
}

function handleFileChange(file) {
  const fileName = file?.name || '';
  const lowerName = fileName.toLowerCase();
  const validType = lowerName.endsWith('.xls') || lowerName.endsWith('.xlsx') || lowerName.endsWith('.csv');
  if (!validType) {
    proxy.$modal.msgWarning("只能上传 .xls、.xlsx 或 .csv 文件");
    uploadRef.value?.clearFiles();
    uploadFile.value = null;
    return;
  }
  const maxSizeMb = 10;
  const isLt10Mb = (file.size || 0) / 1024 / 1024 <= maxSizeMb;
  if (!isLt10Mb) {
    proxy.$modal.msgWarning(`文件大小不能超过 ${maxSizeMb}MB`);
    uploadRef.value?.clearFiles();
    uploadFile.value = null;
    return;
  }
  uploadFile.value = file.raw;
}

function handleExceed() {
  proxy.$modal.msgWarning("只能上传一个文件");
}

function downloadTemplate() {
  downloadStudentImportTemplate().then(res => {
    const blob = new Blob([res], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = '学生导入模板.xlsx';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    proxy.$modal.msgSuccess("模板下载成功");
  }).catch(() => {
    proxy.$modal.msgError("模板下载失败");
  });
}

function submitImport() {
  if (!uploadFile.value) {
    proxy.$modal.msgWarning("请选择要上传的文件");
    return;
  }

  importLoading.value = true;
  importResult.value = null;

  createStudentImportTask().then(taskRes => {
    const taskId = taskRes?.data?.taskId;
    if (!taskId) {
      throw new Error('创建任务失败');
    }
    currentTaskId.value = taskId;
    taskInfo.value = { progress: 1, message: '导入任务已创建，准备上传文件...' };
    progressStatus.value = '';

    const formData = new FormData();
    formData.append('file', uploadFile.value);
    formData.append('taskId', taskId);
    return importStudent(formData);
  }).then(() => {
    startPolling(currentTaskId.value);
    proxy.$modal.msgSuccess("文件上传成功，开始导入");
  }).catch(() => {
    proxy.$modal.msgError("导入启动失败，请检查文件后重试");
  }).finally(() => {
    importLoading.value = false;
  });
}

function startPolling(taskId) {
  stopPolling();
  pollTimer = setInterval(() => {
    queryStudentImportTask(taskId).then(res => {
      const task = res?.data;
      if (!task) {
        return;
      }
      taskInfo.value = task;
      if (task.status === 'SUCCESS') {
        progressStatus.value = 'success';
        importResult.value = {
          total: task.total || 0,
          successCount: task.successCount || 0,
          failCount: task.failCount || 0,
          failDetails: task.failDetails || []
        };
        if ((task.successCount || 0) > 0) {
          getList();
        }
        stopPolling();
      } else if (task.status === 'FAILED') {
        progressStatus.value = 'exception';
        proxy.$modal.msgError(task.message || '导入失败');
        stopPolling();
      }
    }).catch(() => {
      stopPolling();
      progressStatus.value = 'exception';
      proxy.$modal.msgError('查询导入进度失败');
    });
  }, 1000);
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
}

onBeforeUnmount(() => {
  stopPolling();
});

getList();
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
