<template>
  <div class="app-container" style="display: flex; gap: 12px; height: calc(100vh - 84px);">
    <!-- 左侧班级面板 -->
    <div class="class-panel" :class="{ collapsed: panelCollapsed }">
      <div class="panel-header">
        <span v-if="!panelCollapsed" class="panel-title">班级列表</span>
        <el-button v-if="!panelCollapsed" link type="primary" icon="Fold" @click="panelCollapsed = true" />
        <el-button v-else link type="primary" icon="Expand" @click="panelCollapsed = false" />
      </div>

      <template v-if="!panelCollapsed">
        <div style="padding: 0 12px 8px; display: flex; flex-direction: column; gap: 6px;">
          <el-select v-model="filterDepartment" placeholder="筛选学院" clearable size="small" style="width: 100%;">
            <el-option v-for="d in departmentOptions" :key="d" :label="d" :value="d" />
          </el-select>
          <el-select v-model="filterMajor" placeholder="筛选专业" clearable size="small" style="width: 100%;">
            <el-option v-for="m in majorOptions" :key="m" :label="m" :value="m" />
          </el-select>
          <el-input v-model="classKeyword" placeholder="搜索班级名称" clearable prefix-icon="Search" size="small" />
        </div>

        <div class="class-list">
          <div class="class-item" :class="{ active: selectedClassId === null }" @click="selectClass(null)">
            <el-icon><User /></el-icon>
            <span class="class-name">全部学生</span>
            <el-badge :value="totalStudentCount" :max="999" type="info" />
          </div>

          <div
            v-for="cls in filteredClassList"
            :key="cls.id"
            class="class-item"
            :class="{ active: selectedClassId === cls.id }"
            @click="selectClass(cls.id)"
          >
            <el-icon><School /></el-icon>
            <span class="class-name" :title="cls.className">{{ cls.className }}</span>
            <el-badge :value="cls.studentCount || 0" :max="999" type="primary" />
            <div class="class-actions" v-show="selectedClassId === cls.id || classHoverId === cls.id"
              @mouseenter="classHoverId = cls.id" @mouseleave="classHoverId = null">
              <el-button link type="primary" icon="Edit" size="small" @click.stop="handleEditClass(cls)" />
              <el-button link type="danger" icon="Delete" size="small" @click.stop="handleDeleteClass(cls)" />
            </div>
          </div>
        </div>

        <div style="padding: 8px 12px; border-top: 1px solid #ebeef5;">
          <el-button type="primary" plain icon="Plus" size="small" style="width: 100%;" @click="handleAddClass">新增班级</el-button>
        </div>
      </template>
    </div>

    <!-- 右侧学生数据区 -->
    <div style="flex: 1; min-width: 0; display: flex; flex-direction: column;">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="queryParams.studentNo" placeholder="请输入学号" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="姓名" prop="studentName">
          <el-input v-model="queryParams.studentName" placeholder="请输入姓名" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <!-- <el-form-item label="学院" prop="department">
          <el-input v-model="queryParams.department" placeholder="请输入学院" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="queryParams.major" placeholder="请输入专业" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item> -->
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="Plus" @click="handleAdd">新增学生</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate">修改</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="warning" plain icon="Upload" @click="handleImport">
            {{ selectedClassId ? '班级批量导入' : '批量导入' }}
          </el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="studentList" @selection-change="handleSelectionChange" style="flex: 1;">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="学号" align="center" prop="studentNo" width="120" />
        <el-table-column label="姓名" align="center" prop="studentName" :show-overflow-tooltip="true" />
        <el-table-column label="学院" align="center" prop="department" width="120" />
        <el-table-column label="专业" align="center" prop="major" width="120" />
        <el-table-column label="入学年份" align="center" prop="admissionYear" width="100" />
        <el-table-column label="归属年级" align="center" prop="cohortYear" width="100" />
        <el-table-column label="毕业年份" align="center" prop="graduationYear" width="100" />
        <el-table-column label="研究方向" align="center" prop="researchDirection" :show-overflow-tooltip="true" />
        <el-table-column label="学籍状态" align="center" width="90">
          <template #default="scope">
            <el-tag :type="getStudentStatusType(scope.row.status)" size="small">
              {{ getStudentStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="双选状态" align="center" width="90">
          <template #default="scope">
            <el-tag :type="getSelectionStatusType(scope.row.selectionStatus)" size="small">
              {{ getSelectionStatusText(scope.row.selectionStatus) }}
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
    </div>

    <!-- 学生新增/编辑/查看弹窗 -->
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
            <el-form-item label="学籍状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">在读</el-radio>
                <el-radio :value="2">休学</el-radio>
                <el-radio :value="3">毕业</el-radio>
                <el-radio :value="4">退学</el-radio>
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

      <el-descriptions :column="2" border v-if="isView && currentRow">
        <el-descriptions-item label="学号">{{ currentRow.studentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentRow.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ currentRow.department || '-' }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ currentRow.major || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入学年份">{{ currentRow.admissionYear || '-' }}</el-descriptions-item>
        <el-descriptions-item label="归属年级">{{ currentRow.cohortYear || '-' }}</el-descriptions-item>
        <el-descriptions-item label="毕业年份">{{ currentRow.graduationYear || '-' }}</el-descriptions-item>
        <el-descriptions-item label="研究方向" :span="2">{{ currentRow.researchDirection || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学籍状态">
          <el-tag :type="getStudentStatusType(currentRow.status)">{{ getStudentStatusText(currentRow.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="双选状态">
          <el-tag :type="getSelectionStatusType(currentRow.selectionStatus)">{{ getSelectionStatusText(currentRow.selectionStatus) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">{{ isView ? '关闭' : '取 消' }}</el-button>
          <el-button type="primary" @click="submitForm" v-if="!isView">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 班级新增/编辑弹窗 -->
    <el-dialog :title="classDialogTitle" v-model="classDialogOpen" width="500px" append-to-body>
      <el-form :model="classForm" :rules="classRules" ref="classFormRef" label-width="80px">
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="classForm.className" placeholder="请输入班级名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="院系" prop="department">
          <el-input v-model="classForm.department" placeholder="请输入院系" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="classForm.major" placeholder="请输入专业" />
        </el-form-item>
        <el-form-item label="入学年份" prop="admissionYear">
          <el-date-picker v-model="classForm.admissionYear" type="year" placeholder="选择年份" value-format="YYYY" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitClassForm">确 定</el-button>
        <el-button @click="classDialogOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入弹窗 -->
    <el-dialog :title="importDialogTitle" v-model="importOpen" width="700px" append-to-body>
      <el-alert title="导入提示" type="info" :closable="false" show-icon style="margin-bottom: 12px;">
        <template #default>
          <div v-if="selectedClassId">
            当前为<strong>{{ selectedClassName }}</strong>的班级导入模式，模板已预填该班级的学院、专业、入学年份信息，导入的学生将自动归入该班级。
          </div>
          <div v-else>
            当前为年级批量导入模式，请先下载模板按格式填写学生信息。
          </div>
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
                第{{ item.row }}行（学号：{{ item.studentNo || '-' }}）
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
  listStudentByClass,
  getStudentDetail,
  addStudent,
  updateStudent,
  deleteStudent,
  deleteStudentBatch,
  createStudentImportTask,
  queryStudentImportTask,
  importStudent,
  downloadStudentImportTemplate
} from "@/api/student/info";
import { listAllClass, addClass, updateClass, delClass } from "@/api/course/class";
import { UploadFilled } from '@element-plus/icons-vue';
import { School } from '@element-plus/icons-vue';
import { getCurrentInstance, ref, reactive, toRefs, computed, watch, onBeforeUnmount } from "vue";

const { proxy } = getCurrentInstance();

// ============ 班级面板相关 ============
const panelCollapsed = ref(false);
const classKeyword = ref('');
const classList = ref([]);
const selectedClassId = ref(null);
const classHoverId = ref(null);
const totalStudentCount = ref(0);

// 学院+专业联动筛选
const filterDepartment = ref('');
const filterMajor = ref('');

const departmentOptions = computed(() => {
  const set = new Set(classList.value.map(c => c.department).filter(Boolean));
  return [...set].sort();
});

const majorOptions = computed(() => {
  let list = classList.value;
  if (filterDepartment.value) {
    list = list.filter(c => c.department === filterDepartment.value);
  }
  const set = new Set(list.map(c => c.major).filter(Boolean));
  return [...set].sort();
});

// 选了学院后，如果当前专业不在新学院下，清空专业选择
watch(filterDepartment, () => {
  if (filterMajor.value && !majorOptions.value.includes(filterMajor.value)) {
    filterMajor.value = '';
  }
});

const filteredClassList = computed(() => {
  let list = classList.value;
  if (filterDepartment.value) {
    list = list.filter(c => c.department === filterDepartment.value);
  }
  if (filterMajor.value) {
    list = list.filter(c => c.major === filterMajor.value);
  }
  if (classKeyword.value) {
    const kw = classKeyword.value.toLowerCase();
    list = list.filter(c => c.className?.toLowerCase().includes(kw));
  }
  return list;
});

// 当前选中的班级名称（用于导入弹窗标题）
const selectedClassName = computed(() => {
  if (!selectedClassId.value) return '';
  const cls = classList.value.find(c => c.id === selectedClassId.value);
  return cls?.className || '';
});

const importDialogTitle = computed(() => {
  return selectedClassId.value ? `${selectedClassName.value} - 班级批量导入` : '年级批量导入';
});

// 班级弹窗
const classDialogOpen = ref(false);
const classDialogTitle = ref('');
const classFormRef = ref();
const classForm = ref({});
const classRules = ref({
  className: [{ required: true, message: '班级名称不能为空', trigger: 'blur' }]
});

function loadClassList() {
  listAllClass().then(res => {
    classList.value = (res.data || []).map(c => ({ ...c, studentCount: c.studentCount || 0 }));
  });
}

function selectClass(classId) {
  selectedClassId.value = classId;
  queryParams.value.pageNum = 1;
  getList();
}

function handleAddClass() {
  classForm.value = { className: undefined, department: undefined, major: undefined, admissionYear: undefined };
  classDialogTitle.value = '新增班级';
  classDialogOpen.value = true;
}

function handleEditClass(row) {
  classForm.value = { ...row };
  classDialogTitle.value = '编辑班级';
  classDialogOpen.value = true;
}

function submitClassForm() {
  proxy.$refs['classFormRef'].validate(valid => {
    if (valid) {
      if (classForm.value.id) {
        updateClass(classForm.value).then(() => {
          proxy.$modal.msgSuccess('修改成功');
          classDialogOpen.value = false;
          loadClassList();
        });
      } else {
        addClass(classForm.value).then(() => {
          proxy.$modal.msgSuccess('新增成功');
          classDialogOpen.value = false;
          loadClassList();
        });
      }
    }
  });
}

function handleDeleteClass(row) {
  proxy.$modal.confirm('是否确认删除该班级？').then(() => {
    return delClass(row.id);
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功');
    if (selectedClassId.value === row.id) {
      selectedClassId.value = null;
      getList();
    }
    loadClassList();
  }).catch(() => {});
}

// ============ 学生管理相关 ============
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
  { key: 8, label: `学籍状态`, visible: true }
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

// 状态标签
function getStudentStatusType(status) {
  const map = { 1: 'success', 2: 'warning', 3: '', 4: 'danger' };
  return map[status] || 'info';
}

function getStudentStatusText(status) {
  const map = { 1: '在读', 2: '休学', 3: '毕业', 4: '退学' };
  return map[status] || '未知';
}

function getSelectionStatusType(status) {
  const map = { 0: 'info', 1: 'warning', 2: 'warning', 3: 'success' };
  return map[status] || 'info';
}

function getSelectionStatusText(status) {
  const map = { 0: '未开始', 1: '双选中', 2: '补选中', 3: '已确定' };
  return map[status] || '未知';
}

// 获取学生列表
function getList() {
  loading.value = true;
  if (selectedClassId.value) {
    listStudentByClass(selectedClassId.value, queryParams.value).then(res => {
      loading.value = false;
      studentList.value = res.data.rows || res.data || [];
      total.value = res.data.total || 0;
      totalStudentCount.value = total.value;
    }).catch((err) => {
      console.error('按班级查询学生失败:', err);
      loading.value = false;
    });
  } else {
    listStudent(queryParams.value).then(res => {
      loading.value = false;
      studentList.value = res.data.rows || res.data || [];
      total.value = res.data.total || 0;
      totalStudentCount.value = total.value;
    }).catch((err) => {
      console.error('查询学生列表失败:', err);
      loading.value = false;
    });
  }
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
          loadClassList();
        });
      }
    }
  });
}

function handleDelete(row) {
  const studentIds = row.id || ids.value;
  const isBatch = Array.isArray(studentIds);
  proxy.$modal.confirm('是否确认删除？').then(() => {
    return isBatch ? deleteStudentBatch(studentIds) : deleteStudent(studentIds);
  }).then(() => {
    getList();
    loadClassList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

// ============ 批量导入 ============

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
  const classId = selectedClassId.value || undefined;
  downloadStudentImportTemplate(classId).then(res => {
    const blob = new Blob([res], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = selectedClassId.value ? `${selectedClassName.value} 班级导入模板.xlsx` : '学生导入模板.xlsx';
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
    if (selectedClassId.value) {
      formData.append('classId', selectedClassId.value);
    }
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
          loadClassList();
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

// 初始化
loadClassList();
getList();
</script>

<style scoped>
.class-panel {
  width: 280px;
  min-width: 280px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  overflow: hidden;
}

.class-panel.collapsed {
  width: 40px;
  min-width: 40px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-bottom: 1px solid #ebeef5;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.class-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.class-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  cursor: pointer;
  transition: background-color 0.2s;
  position: relative;
}

.class-item:hover {
  background-color: #f5f7fa;
}

.class-item.active {
  background-color: #ecf5ff;
  color: #409eff;
}

.class-item.active .class-name {
  font-weight: 600;
  color: #409eff;
}

.class-name {
  flex: 1;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.class-actions {
  display: flex;
  gap: 2px;
  flex-shrink: 0;
}
</style>
