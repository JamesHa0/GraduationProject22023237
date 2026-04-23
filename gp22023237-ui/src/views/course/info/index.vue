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
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Upload" @click="handleImport">批量导入</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="courseList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="课程编号" align="center" prop="courseNo" width="120" />
      <el-table-column label="课程名称" align="center" prop="name" :show-overflow-tooltip="true" />
      <el-table-column label="学分" align="center" prop="credit" width="80" />
      <el-table-column label="学时" align="center" prop="hours" width="80" />
      <el-table-column label="学期" align="center" prop="semester" width="100" />
      <el-table-column label="学年" align="center" prop="year" width="80" />
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

    <!-- 批量导入对话框 -->
    <el-dialog title="批量导入课程" v-model="importOpen" width="680px" append-to-body>
      <el-alert
        title="导入提示"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 12px;">
        <template #default>
          <div>请先下载模板（当前版本：{{ templateVersion }}），按“课程模板”Sheet填写，参考“填写说明”Sheet中的字段规则与错误码。</div>
          <el-button link type="primary" @click="downloadTemplate">点击下载导入模板</el-button>
        </template>
      </el-alert>

      <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 20px;">
        <template #default>
          <div>关键规则：课程编号唯一；学分范围0.5-10；学时范围1-200；时间格式必须是HH:mm:ss；状态仅支持0/1/2。</div>
        </template>
      </el-alert>

      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
        accept=".xlsx,.xls"
        drag>
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            仅支持 .xls/.xlsx，单文件不超过10MB
          </div>
        </template>
      </el-upload>

      <!-- 导入结果展示 -->
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
                第{{ item.row }}行（课程编号：{{ item.courseNo || '-' }}）
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
          <el-button type="primary" @click="submitImport" :loading="importLoading" :disabled="!uploadFile">确 定</el-button>
          <el-button @click="cancelImport">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Course">
import { listCourse, getCourse, addCourse, updateCourse, delCourse, delCourseBatch, importCourse, downloadImportTemplate } from "@/api/course/course";
import { UploadFilled } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();

const courseList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

// 导入相关
const importOpen = ref(false);
const importLoading = ref(false);
const uploadFile = ref(null);
const uploadRef = ref();
const importResult = ref(null);
const templateVersion = ref('v2.0');

const columns = ref([
  { key: 0, label: `课程编号`, visible: true },
  { key: 1, label: `课程名称`, visible: true },
  { key: 2, label: `学分`, visible: true },
  { key: 3, label: `学时`, visible: true },
  { key: 4, label: `学期`, visible: true },
  { key: 5, label: `学年`, visible: true },
  { key: 6, label: `状态`, visible: true }
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

function getList() {
  loading.value = true;
  listCourse(queryParams.value).then(res => {
    courseList.value = res.data.rows || res.data || [];
    total.value = res.data.total || 0;
  }).catch(() => {
    courseList.value = [];
    total.value = 0;
  }).finally(() => {
    loading.value = false;
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
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function reset() {
  form.value = {
    id: undefined,
    courseNo: undefined,
    name: undefined,
    credit: 1.0,
    hours: 32,
    semester: undefined,
    year: new Date().getFullYear(),
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
  open.value = true;
  title.value = "添加课程";
}

function handleUpdate(row) {
  reset();
  const id = row.id || ids.value;
  getCourse(id).then(res => {
    form.value = res.data;
    open.value = true;
    title.value = "修改课程";
  });
}

function submitForm() {
  proxy.$refs["courseRef"].validate(valid => {
    if (valid) {
      if (form.value.id !== undefined) {
        const updateData = { ...form.value };
        delete updateData.createTime;
        delete updateData.updateTime;
        updateCourse(updateData).then(() => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        const addData = { ...form.value };
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
  const isSingle = !!row?.id;
  const request = isSingle ? delCourse(row.id) : delCourseBatch(ids.value);

  proxy.$modal.confirm('是否确认删除？').then(() => {
    return request;
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => { });
}

// 批量导入相关
function handleImport() {
  resetImport();
  importOpen.value = true;
}

function resetImport() {
  uploadFile.value = null;
  importResult.value = null;
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
  const isExcel = lowerName.endsWith('.xls') || lowerName.endsWith('.xlsx');
  if (!isExcel) {
    proxy.$modal.msgWarning("只能上传 .xls 或 .xlsx 文件");
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
  downloadImportTemplate().then(res => {
    const blob = new Blob([res], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = '课程导入模板.xlsx';
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
  const formData = new FormData();
  formData.append('file', uploadFile.value);

  importCourse(formData).then(res => {
    importResult.value = res.data;
    if (importResult.value.failCount > 0) {
      proxy.$modal.msgWarning(`导入完成：成功 ${importResult.value.successCount} 条，失败 ${importResult.value.failCount} 条`);
    } else {
      proxy.$modal.msgSuccess("导入完成，全部成功");
    }
    if (importResult.value.successCount > 0) {
      getList();
    }
  }).catch(() => {
    proxy.$modal.msgError("导入失败，请检查模板和数据后重试");
  }).finally(() => {
    importLoading.value = false;
  });
}

getList();
</script>
