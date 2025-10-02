<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" @submit.native.prevent>
            <el-form-item label="查询导师" prop="nursingName">
                <el-input v-model="queryParams.name" placeholder="请输入要查询的导师姓名" clearable style="width: 200px"
                    @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
        </el-form>

        <el-table v-loading="loading" :data="mentorList.slice((pageNum - 1) * pageSize, pageNum * pageSize)"
            style="width: 100%;" @selection-change="handleSelectionChange">
            <el-table-column label="序号" width="50" type="index" align="center">
                <template #default="scope">
                    <span>{{ (pageNum - 1) * pageSize + scope.$index + 1 }}</span>
                </template>
            </el-table-column>
            <el-table-column label="ID" align="center" prop="id" :show-overflow-tooltip="true" v-if=false />
            <el-table-column label="姓名" align="center" prop="name" :show-overflow-tooltip="true" />
            <el-table-column label="未定" align="center" prop="" :show-overflow-tooltip="true" />
            <el-table-column label="未定" align="center" prop="" :show-overflow-tooltip="true" />
            <el-table-column label="未定" align="center" prop="" :show-overflow-tooltip="true" />
            <el-table-column label="状态" align="center" prop="" :show-overflow-tooltip="true">
                <template #default="scope">
                    <el-tag v-if="scope.row.status == 1" type="primary">可选</el-tag>
                    <el-tag v-else-if="scope.row.status == 2" type="danger">已满</el-tag>
                    <el-tag v-else-if="scope.row.status == 3" type="success">已选中</el-tag>
                    <el-tag v-else type="info">不可选</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="未定" align="center" prop="" :show-overflow-tooltip="true" />
            <el-table-column label="未定" align="center" prop="" :show-overflow-tooltip="true" />
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button v-if="!scope.row.isSelected && !isRowSelected(scope.row)" link type="primary" icon="Plus"
                        @click="toggleRowButton(scope.row)" v-loading="scope.row.isLoading">
                        选中
                    </el-button>

                    <el-button v-else-if="scope.row.isSelected && isRowSelected(scope.row)" link type="danger"
                        icon="Close" @click="toggleRowButton(scope.row)" v-loading="scope.row.isLoading">
                        取消
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" v-model:page="pageNum" v-model:limit="pageSize" />

        <div class="footer">
            <el-button type="primary" @click="submit()">
                提交
            </el-button>
        </div>

    </div>
</template>

<script setup>
import { listByRoleId as initData } from "@/api/user/user";

const { proxy } = getCurrentInstance();

const mentorList = ref([]);
const loading = ref(true);

const selectedMentors = ref([]); // 选中的导师

const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

let queryParams = ref({
    name: undefined
});

// 切换行按钮状态
const toggleRowButton = (row) => {
    row.isLoading = true;
    row.isSelected = !row.isSelected;

    if (row.isSelected) {
        selectedMentors.value.push(row);
    } else {
        const index = selectedMentors.value.findIndex(menter => menter.id === row.id);
        if (index > -1) {
            selectedMentors.value.splice(index, 1);
        }
    }

    handleSelectionChange(selectedMentors.value);
    proxy.$modal.msgWarning("请尽快提交");

    row.isLoading = false;
    row.status = row.isSelected ? 3 : 1;
};

// 处理选择变化
const handleSelectionChange = (rows) => {
    selectedMentors.value = rows;
};

// 检查导师是否被选中
const isRowSelected = (row) => {
    return selectedMentors.value.some(selectedMentors => selectedMentors.id === row.id);
};


/** 查询可选导师列表 */
function getList() {
    loading.value = true;
    queryParams.value.roleId = 7; // 7-导师
    initData(queryParams.value).then(response => {
        mentorList.value = response.data;
        console.log(response);

        total.value = response.data.length;
        loading.value = false;
    });
}

/** 搜索按钮操作 */
function handleQuery() {
    pageNum.value = 1;
    getList();
}

/** 重置按钮操作 */
function resetQuery() {
    proxy.resetForm("queryRef");
    handleQuery();
}



/** 提交添加项目 */
function submit() {
    addFormRef.value.validate((valid) => {
        if (valid) {
            addFormVisible.value = false;
            add(addForm.value).then(response => {
                getList();
                addForm = ref({
                    serialNumber: '',
                    nursingName: '',
                    servicePrice: '',
                    message: '',
                    status: '',
                    executionCycle: '',
                    executionTimes: 0
                });
                proxy.$modal.msgSuccess("添加成功");
            })
                .catch(() => {
                    getList();
                    proxy.$modal.msgError("添加失败");
                });
        } else {
            proxy.$modal.msgError('必填项不能为空');
            return false;
        }
    });
}

getList();
</script>

<style scoped>
.footer {
    margin-top: 20px;
    margin-right: 50px;
    text-align: right
}
</style>