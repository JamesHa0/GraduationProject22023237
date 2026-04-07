<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
         <el-form-item label="字段名称" prop="fieldName">
            <el-input
               v-model="queryParams.fieldName"
               placeholder="请输入字段名称"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="字段编码" prop="fieldCode">
            <el-input
               v-model="queryParams.fieldCode"
               placeholder="请输入字段编码"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="所属模块" prop="module">
            <el-select v-model="queryParams.module" placeholder="所属模块" clearable style="width: 200px">
               <el-option label="学生信息" value="student" />
               <el-option label="教师信息" value="teacher" />
               <el-option label="课程信息" value="course" />
               <el-option label="学术信息" value="academic" />
               <el-option label="学位信息" value="degree" />
            </el-select>
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="字段状态" clearable style="width: 200px">
               <el-option
                  v-for="dict in sys_normal_disable"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
               />
            </el-select>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button
               type="primary"
               plain
               icon="Plus"
               @click="handleAdd"
               v-hasPermi="['system:field:add']"
            >新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['system:field:edit']"
            >修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['system:field:remove']"
            >删除</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="fieldList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="字段编号" align="center" prop="fieldId" />
         <el-table-column label="字段名称" align="center" prop="fieldName" :show-overflow-tooltip="true" />
         <el-table-column label="字段编码" align="center" prop="fieldCode" :show-overflow-tooltip="true" />
         <el-table-column label="字段类型" align="center" prop="fieldType">
            <template #default="scope">
               <dict-tag :options="fieldTypeOptions" :value="scope.row.fieldType" />
            </template>
         </el-table-column>
         <el-table-column label="所属模块" align="center" prop="module" />
         <el-table-column label="是否必填" align="center" prop="isRequired">
            <template #default="scope">
               <dict-tag :options="sys_yes_no" :value="scope.row.isRequired" />
            </template>
         </el-table-column>
         <el-table-column label="排序" align="center" prop="orderNum" />
         <el-table-column label="状态" align="center" prop="status">
            <template #default="scope">
               <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
            </template>
         </el-table-column>
         <el-table-column label="创建时间" align="center" prop="createTime" width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:field:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:field:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination
         v-show="total > 0"
         :total="total"
         v-model:page="queryParams.pageNum"
         v-model:limit="queryParams.pageSize"
         @pagination="getList"
      />

      <!-- 添加或修改系统字段对话框 -->
      <el-dialog :title="title" v-model="open" width="700px" append-to-body>
         <el-form ref="fieldRef" :model="form" :rules="rules" label-width="100px">
            <el-row>
               <el-col :span="12">
                  <el-form-item label="字段名称" prop="fieldName">
                     <el-input v-model="form.fieldName" placeholder="请输入字段名称" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="字段编码" prop="fieldCode">
                     <el-input v-model="form.fieldCode" placeholder="请输入字段编码" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="字段类型" prop="fieldType">
                     <el-select v-model="form.fieldType" placeholder="请选择字段类型">
                        <el-option label="输入框" value="input" />
                        <el-option label="选择框" value="select" />
                        <el-option label="文本域" value="textarea" />
                        <el-option label="日期" value="date" />
                        <el-option label="时间" value="datetime" />
                        <el-option label="数字" value="number" />
                     </el-select>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="字段长度" prop="fieldLength">
                     <el-input-number v-model="form.fieldLength" controls-position="right" :min="0" placeholder="请输入字段长度" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="所属模块" prop="module">
                     <el-select v-model="form.module" placeholder="请选择所属模块">
                        <el-option label="学生信息" value="student" />
                        <el-option label="教师信息" value="teacher" />
                        <el-option label="课程信息" value="course" />
                        <el-option label="学术信息" value="academic" />
                        <el-option label="学位信息" value="degree" />
                     </el-select>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="排序" prop="orderNum">
                     <el-input-number v-model="form.orderNum" controls-position="right" :min="0" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="是否必填">
                     <el-radio-group v-model="form.isRequired">
                        <el-radio
                           v-for="dict in sys_yes_no"
                           :key="dict.value"
                           :value="dict.value"
                        >{{ dict.label }}</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="状态">
                     <el-radio-group v-model="form.status">
                     <el-radio
                        v-for="dict in sys_normal_disable"
                        :key="dict.value"
                        :value="dict.value"
                     >{{ dict.label }}</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
            </el-row>
            <el-form-item label="默认值" prop="defaultValue">
               <el-input v-model="form.defaultValue" placeholder="请输入默认值" />
            </el-form-item>
            <el-form-item label="选项值" prop="options" v-if="form.fieldType === 'select'">
               <el-input v-model="form.options" type="textarea" placeholder="请输入选项值，JSON格式，如：[{\"label\":\"选项1\",\"value\":\"1\"}]" />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
               <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
            </el-form-item>
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

<script setup name="Field">
import { listField, addField, delField, getField, updateField } from "@/api/system/field";

const { proxy } = getCurrentInstance();
const { sys_normal_disable, sys_yes_no } = proxy.useDict("sys_normal_disable", "sys_yes_no");

const fieldTypeOptions = [
  { label: '输入框', value: 'input' },
  { label: '选择框', value: 'select' },
  { label: '文本域', value: 'textarea' },
  { label: '日期', value: 'date' },
  { label: '时间', value: 'datetime' },
  { label: '数字', value: 'number' }
];

const fieldList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    fieldName: undefined,
    fieldCode: undefined,
    module: undefined,
    status: undefined
  },
  rules: {
    fieldName: [{ required: true, message: "字段名称不能为空", trigger: "blur" }],
    fieldCode: [{ required: true, message: "字段编码不能为空", trigger: "blur" }],
    fieldType: [{ required: true, message: "字段类型不能为空", trigger: "change" }],
    module: [{ required: true, message: "所属模块不能为空", trigger: "change" }],
    orderNum: [{ required: true, message: "排序不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询系统字段列表 */
function getList() {
  loading.value = true;
  listField(queryParams.value).then(response => {
    fieldList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  form.value = {
    fieldId: undefined,
    fieldName: undefined,
    fieldCode: undefined,
    fieldType: undefined,
    fieldLength: undefined,
    isRequired: "N",
    defaultValue: undefined,
    options: undefined,
    module: undefined,
    orderNum: 0,
    status: "0",
    remark: undefined
  };
  proxy.resetForm("fieldRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.fieldId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加系统字段";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const fieldId = row.fieldId || ids.value;
  getField(fieldId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改系统字段";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["fieldRef"].validate(valid => {
    if (valid) {
      if (form.value.fieldId != undefined) {
        updateField(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addField(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const fieldIds = row.fieldId || ids.value;
  proxy.$modal.confirm('是否确认删除系统字段编号为"' + fieldIds + '"的数据项？').then(function() {
    return delField(fieldIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>
