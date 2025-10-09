<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" @submit.native.prevent>
            <el-form-item label="查询学生" prop="nursingName">
                <el-input v-model="queryParams.name" placeholder="请输入要查询的学生姓名" clearable style="width: 200px"
                    @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="switch">切换学生卡片模式</el-button>
            </el-form-item>
        </el-form>

        <el-table v-loading="loading" :data="mentorList.slice((pageNum - 1) * pageSize, pageNum * pageSize)"
            style="width: 100%;">
            <el-table-column label="序号" width="50" type="index" align="center">
                <template #default="scope">
                    <span>{{ (pageNum - 1) * pageSize + scope.$index + 1 }}</span>
                </template>
            </el-table-column>
            <el-table-column label="ID" align="center" prop="id" :show-overflow-tooltip="true" v-if=false />
            <el-table-column label="姓名" align="center" prop="studentName" :show-overflow-tooltip="true" />
            <el-table-column label="学号" align="center" prop="studentNo" :show-overflow-tooltip="true" />
            <el-table-column label="院系" align="center" prop="department" :show-overflow-tooltip="true" />
            <el-table-column label="专业" align="center" prop="major" :show-overflow-tooltip="true" />
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="success" icon="Check" disabled>
                        已选择
                    </el-button>
                    <el-button link type="primary" icon="Plus" @click="selectMentor(scope.row)">
                        选中
                    </el-button>
                    <el-button link type="danger" icon="Close" @click="cancelSelection()">
                        拒绝
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" v-model:page="pageNum" v-model:limit="pageSize" />

    </div>
</template>

<script setup>
import { listStudents as initData } from "@/api/mentor/selection";
import useUserStore from '@/store/modules/user';

const { proxy } = getCurrentInstance();

const mentorList = ref([]);

const loading = ref(true);

const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

// 访问导师信息中的ID
const mentorId = ref(null);
const getMentorId = () => {
    const userStore = useUserStore();
    if (userStore.roleInfo && userStore.roleInfo[0]) {
        return userStore.roleInfo[0].id;
    } else {
        proxy.$modal.msgError('导师信息尚未加载');
        return null;
    }
};

let queryParams = ref({
    name: undefined
});


/** 查询可选学生列表 */
function getList() {
    loading.value = true;

    const id = getMentorId();
    if (!id) {
        proxy.$modal.msgError('导师信息尚未加载');
        loading.value = false;
        return;
    }
    const data = {
        mentorId: id
    };

    initData(data).then(response => {
        mentorList.value = response.data;
        total.value = response.data.length;
        console.log(`初始化数据：`, mentorList.value);
    });



    loading.value = false;

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

getList(); // 获取导师列表
</script>

<style scoped>
.footer {
    margin-top: 20px;
    margin-right: 50px;
    text-align: right
}
</style>