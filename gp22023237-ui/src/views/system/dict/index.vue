<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
         <el-form-item label="字典名称" prop="dictName">
            <el-input
               v-model="queryParams.dictName"
               placeholder="请输入字典名称"
               clearable
               style="width: 240px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="字典类型" prop="dictType">
            <el-input
               v-model="queryParams.dictType"
               placeholder="请输入字典类型"
               clearable
               style="width: 240px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select
               v-model="queryParams.status"
               placeholder="字典状态"
               clearable
               style="width: 240px"
            >
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
               v-if="canManage"
            >新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-if="canManage"
            >修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-if="canManage"
            >删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Refresh"
               @click="handleRefreshCache"
               v-if="canManage"
            >刷新缓存</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="typeList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="字典编号" align="center" prop="dictId" />
         <el-table-column label="字典名称" align="center" prop="dictName" :show-overflow-tooltip="true"/>
         <el-table-column label="字典类型" align="center" prop="dictType" :show-overflow-tooltip="true" />
         <el-table-column label="状态" align="center" prop="status">
            <template #default="scope">
               <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
            </template>
         </el-table-column>
         <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
         <el-table-column label="创建时间" align="center" prop="createTime" width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="View" @click="handleDataDialog(scope.row)" v-if="canManage">查看</el-button>
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="canManage">编辑</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-if="canManage">删除</el-button>
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

      <!-- 添加或修改字典类型对话框 -->
      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="dictRef" :model="form" :rules="rules" label-width="80px">
            <el-row>
               <el-col :span="12">
                  <el-form-item label="字典名称" prop="dictName">
                     <el-input v-model="form.dictName" placeholder="请输入字典名称" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="字典类型" prop="dictType">
                     <el-input v-model="form.dictType" placeholder="请输入字典类型" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="状态" prop="status">
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
            <el-row>
               <el-col :span="24">
                  <el-form-item label="备注" prop="remark">
                     <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" :rows="3"></el-input>
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

      <!-- 字典数据对话框 -->
      <el-dialog :title="'字典数据 - ' + currentDictName + '（' + currentDictType + '）'" v-model="dataDialogVisible" width="80%" top="5vh" append-to-body destroy-on-close>
         <el-form :model="dataQueryParams" ref="dataQueryRef" :inline="true" v-show="dataShowSearch">
            <el-form-item label="字典标签" prop="dictLabel">
               <el-input
                  v-model="dataQueryParams.dictLabel"
                  placeholder="请输入字典标签"
                  clearable
                  style="width: 200px"
                  @keyup.enter="handleDataQuery"
               />
            </el-form-item>
            <el-form-item label="状态" prop="status">
               <el-select v-model="dataQueryParams.status" placeholder="数据状态" clearable style="width: 200px">
                  <el-option
                     v-for="dict in sys_normal_disable"
                     :key="dict.value"
                     :label="dict.label"
                     :value="dict.value"
                  />
               </el-select>
            </el-form-item>
            <el-form-item>
               <el-button type="primary" icon="Search" @click="handleDataQuery">搜索</el-button>
               <el-button icon="Refresh" @click="resetDataQuery">重置</el-button>
            </el-form-item>
         </el-form>

         <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
               <el-button
                  type="primary"
                  plain
                  icon="Plus"
                  @click="handleDataAdd"
                  v-if="canManage"
               >新增</el-button>
            </el-col>
            <el-col :span="1.5">
               <el-button
                  type="success"
                  plain
                  icon="Edit"
                  :disabled="dataSingle"
                  @click="handleDataUpdate"
                  v-if="canManage"
               >修改</el-button>
            </el-col>
            <el-col :span="1.5">
               <el-button
                  type="danger"
                  plain
                  icon="Delete"
                  :disabled="dataMultiple"
                  @click="handleDataDelete"
                  v-if="canManage"
               >删除</el-button>
            </el-col>
            <right-toolbar v-model:showSearch="dataShowSearch" @queryTable="getDataList"></right-toolbar>
         </el-row>

         <el-table v-loading="dataLoading" :data="dataList" @selection-change="handleDataSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="字典编码" align="center" prop="dictCode" />
            <el-table-column label="字典标签" align="center" prop="dictLabel">
               <template #default="scope">
                  <span v-if="(scope.row.listClass == '' || scope.row.listClass == 'default') && (scope.row.cssClass == '' || scope.row.cssClass == null)">{{ scope.row.dictLabel }}</span>
                  <el-tag v-else :type="scope.row.listClass == 'primary' ? '' : scope.row.listClass" :class="scope.row.cssClass">{{ scope.row.dictLabel }}</el-tag>
               </template>
            </el-table-column>
            <el-table-column label="字典键值" align="center" prop="dictValue" />
            <el-table-column label="字典排序" align="center" prop="dictSort" />
            <el-table-column label="状态" align="center" prop="status">
               <template #default="scope">
                  <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
               </template>
            </el-table-column>
            <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
            <el-table-column label="创建时间" align="center" prop="createTime" width="180">
               <template #default="scope">
                  <span>{{ parseTime(scope.row.createTime) }}</span>
               </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
               <template #default="scope">
                  <el-button link type="primary" icon="Edit" @click="handleDataUpdate(scope.row)" v-if="canManage">编辑</el-button>
                  <el-button link type="primary" icon="Delete" @click="handleDataDelete(scope.row)" v-if="canManage">删除</el-button>
               </template>
            </el-table-column>
         </el-table>

         <pagination
            v-show="dataTotal > 0"
            :total="dataTotal"
            v-model:page="dataQueryParams.pageNum"
            v-model:limit="dataQueryParams.pageSize"
            @pagination="getDataList"
         />
      </el-dialog>

      <!-- 添加或修改字典数据对话框 -->
      <el-dialog :title="dataTitle" v-model="dataOpen" width="600px" append-to-body>
         <el-form ref="dataRef" :model="dataForm" :rules="dataRules" label-width="80px">
            <el-row>
               <el-col :span="12">
                  <el-form-item label="字典类型">
                     <el-input v-model="dataForm.dictType" :disabled="true" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="数据标签" prop="dictLabel">
                     <el-input v-model="dataForm.dictLabel" placeholder="请输入数据标签" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="数据键值" prop="dictValue">
                     <el-input v-model="dataForm.dictValue" placeholder="请输入数据键值" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="显示排序" prop="dictSort">
                     <el-input-number v-model="dataForm.dictSort" controls-position="right" :min="0" style="width: 100%" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="样式属性" prop="cssClass">
                     <el-input v-model="dataForm.cssClass" placeholder="请输入样式属性" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="回显样式" prop="listClass">
                     <el-select v-model="dataForm.listClass" style="width: 100%">
                        <el-option
                           v-for="item in listClassOptions"
                           :key="item.value"
                           :label="item.label + '(' + item.value + ')'"
                           :value="item.value"
                        ></el-option>
                     </el-select>
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="状态" prop="status">
                     <el-radio-group v-model="dataForm.status">
                        <el-radio
                           v-for="dict in sys_normal_disable"
                           :key="dict.value"
                           :value="dict.value"
                        >{{ dict.label }}</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="24">
                  <el-form-item label="备注" prop="remark">
                     <el-input v-model="dataForm.remark" type="textarea" placeholder="请输入内容" :rows="3"></el-input>
                  </el-form-item>
               </el-col>
            </el-row>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="dataSubmitForm">确 定</el-button>
               <el-button @click="dataCancel">取 消</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="Dict">
import useDictStore from '@/store/modules/dict'
import useUserStore from '@/store/modules/user'
import { listType, getType, delType, addType, updateType, refreshCache } from "@/api/system/dict/type";
import { listData, getData, delData, addData, updateData } from "@/api/system/dict/data";

const { proxy } = getCurrentInstance();
const userStore = useUserStore()
const { sys_normal_disable } = proxy.useDict("sys_normal_disable");

// 获取当前用户角色ID
function getCurrentUserRoleId() {
  if (userStore.roles !== undefined && userStore.roles !== null) {
    if (Array.isArray(userStore.roles)) {
      return userStore.roles.length > 0 ? userStore.roles[0] : null
    }
    return Number(userStore.roles)
  }
  return null
}

// 判断当前用户是否可以管理字典（新增/编辑/删除）
// 1-超级管理员, 4-综合管理员, 5-研究生秘书
const canManage = computed(() => {
  const roleId = getCurrentUserRoleId()
  return roleId === 1 || roleId === 4 || roleId === 5
})

// ========== 字典类型相关状态 ==========
const typeList = ref([]);
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
    dictName: undefined,
    dictType: undefined,
    status: undefined
  },
  rules: {
    dictName: [{ required: true, message: "字典名称不能为空", trigger: "blur" }],
    dictType: [{ required: true, message: "字典类型不能为空", trigger: "blur" }]
  },
});

const { queryParams, form, rules } = toRefs(data);

// ========== 字典数据相关状态 ==========
const dataDialogVisible = ref(false);
const dataList = ref([]);
const dataLoading = ref(false);
const dataShowSearch = ref(true);
const dataIds = ref([]);
const dataSingle = ref(true);
const dataMultiple = ref(true);
const dataTotal = ref(0);
const currentDictType = ref("");
const currentDictName = ref("");
const dataTitle = ref("");
const dataOpen = ref(false);

const listClassOptions = ref([
  { value: "default", label: "默认" },
  { value: "primary", label: "主要" },
  { value: "success", label: "成功" },
  { value: "info", label: "信息" },
  { value: "warning", label: "警告" },
  { value: "danger", label: "危险" }
]);

const dataFormData = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    dictType: undefined,
    dictLabel: undefined,
    status: undefined
  },
  rules: {
    dictLabel: [{ required: true, message: "数据标签不能为空", trigger: "blur" }],
    dictValue: [{ required: true, message: "数据键值不能为空", trigger: "blur" }],
    dictSort: [{ required: true, message: "数据顺序不能为空", trigger: "blur" }]
  }
});

const dataForm = toRefs(dataFormData).form;
const dataQueryParams = toRefs(dataFormData).queryParams;
const dataRules = toRefs(dataFormData).rules;

// ========== 字典类型方法 ==========

/** 查询字典类型列表 */
function getList() {
  loading.value = true;
  listType(queryParams.value).then(response => {
    typeList.value = response.data.rows;
    total.value = response.data.total;
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
    dictId: undefined,
    dictName: undefined,
    dictType: undefined,
    status: "0",
    remark: undefined
  };
  proxy.resetForm("dictRef");
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

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加字典类型";
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.dictId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const dictId = row.dictId || ids.value;
  getType(dictId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改字典类型";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["dictRef"].validate(valid => {
    if (valid) {
      if (form.value.dictId != undefined) {
        updateType(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addType(form.value).then(response => {
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
  const dictIds = row.dictId || ids.value;
  proxy.$modal.confirm('是否确认删除字典编号为"' + dictIds + '"的数据项？').then(function() {
    return delType(dictIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 刷新缓存按钮操作 */
function handleRefreshCache() {
  refreshCache().then(() => {
    proxy.$modal.msgSuccess("刷新成功");
    useDictStore().cleanDict();
  });
}

// ========== 字典数据方法 ==========

/** 打开字典数据对话框 */
function handleDataDialog(row) {
  currentDictType.value = row.dictType;
  currentDictName.value = row.dictName;
  dataQueryParams.value = {
    pageNum: 1,
    pageSize: 10,
    dictType: row.dictType,
    dictLabel: undefined,
    status: undefined
  };
  dataDialogVisible.value = true;
  getDataList();
}

/** 查询字典数据列表 */
function getDataList() {
  dataLoading.value = true;
  listData(dataQueryParams.value).then(response => {
    dataList.value = response.data.rows;
    dataTotal.value = response.data.total;
    dataLoading.value = false;
  });
}

/** 字典数据搜索 */
function handleDataQuery() {
  dataQueryParams.value.pageNum = 1;
  getDataList();
}

/** 字典数据重置搜索 */
function resetDataQuery() {
  proxy.resetForm("dataQueryRef");
  dataQueryParams.value.dictType = currentDictType.value;
  dataQueryParams.value.pageNum = 1;
  getDataList();
}

/** 字典数据多选框选中 */
function handleDataSelectionChange(selection) {
  dataIds.value = selection.map(item => item.dictCode);
  dataSingle.value = selection.length != 1;
  dataMultiple.value = !selection.length;
}

/** 字典数据表单重置 */
function dataReset() {
  dataForm.value = {
    dictCode: undefined,
    dictLabel: undefined,
    dictValue: undefined,
    cssClass: undefined,
    listClass: "default",
    dictSort: 0,
    status: "0",
    remark: undefined
  };
  proxy.resetForm("dataRef");
}

/** 字典数据新增按钮 */
function handleDataAdd() {
  dataReset();
  dataForm.value.dictType = currentDictType.value;
  dataOpen.value = true;
  dataTitle.value = "添加字典数据";
}

/** 字典数据修改按钮 */
function handleDataUpdate(row) {
  dataReset();
  const dictCode = row.dictCode || dataIds.value;
  getData(dictCode).then(response => {
    dataForm.value = response.data;
    dataOpen.value = true;
    dataTitle.value = "修改字典数据";
  });
}

/** 字典数据提交按钮 */
function dataSubmitForm() {
  proxy.$refs["dataRef"].validate(valid => {
    if (valid) {
      if (dataForm.value.dictCode != undefined) {
        updateData(dataForm.value).then(response => {
          useDictStore().removeDict(currentDictType.value);
          proxy.$modal.msgSuccess("修改成功");
          dataOpen.value = false;
          getDataList();
        });
      } else {
        addData(dataForm.value).then(response => {
          useDictStore().removeDict(currentDictType.value);
          proxy.$modal.msgSuccess("新增成功");
          dataOpen.value = false;
          getDataList();
        });
      }
    }
  });
}

/** 字典数据取消按钮 */
function dataCancel() {
  dataOpen.value = false;
  dataReset();
}

/** 字典数据删除按钮 */
function handleDataDelete(row) {
  const dictCodes = row.dictCode || dataIds.value;
  proxy.$modal.confirm('是否确认删除字典编码为"' + dictCodes + '"的数据项？').then(function() {
    return delData(dictCodes);
  }).then(() => {
    getDataList();
    proxy.$modal.msgSuccess("删除成功");
    useDictStore().removeDict(currentDictType.value);
  }).catch(() => {});
}

getList();
</script>
