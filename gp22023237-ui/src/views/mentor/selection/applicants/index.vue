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
            <el-table-column label="userId" align="center" prop="userId" :show-overflow-tooltip="true" v-if=false />
            <el-table-column label="姓名" align="center" prop="studentName" :show-overflow-tooltip="true" />
            <el-table-column label="学号" align="center" prop="studentNo" :show-overflow-tooltip="true" />
            <el-table-column label="院系" align="center" prop="department" :show-overflow-tooltip="true" />
            <el-table-column label="专业" align="center" prop="major" :show-overflow-tooltip="true" />
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="success" icon="Tickets" @click="showDetail(scope.row)">
                        详细信息
                    </el-button>
                    <el-button v-if="scope.row.teacherStatus == 1" link type="danger" icon="Close"
                        @click="selectStudent(scope.row, 2)">
                        退选
                    </el-button>
                    <el-button v-else link type="primary" icon="Plus" @click="selectStudent(scope.row, 1)">
                        选中
                    </el-button>

                </template>
            </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" v-model:page="pageNum" v-model:limit="pageSize" />




        <!--学生详细信息  BEGIN-->
        <el-dialog v-model="detailVisible" title="学生详细信息" width="500" destroy-on-close>
            <div class="student-detail-container">
                <el-descriptions :column="1" border size="large">
                    <el-descriptions-item label="姓名">{{ currentStudent.studentName }}</el-descriptions-item>
                    <el-descriptions-item label="性别">{{ genderFormat(currentStudent.gender) || '未填写'
                    }}</el-descriptions-item>
                    <el-descriptions-item label="学号">{{ currentStudent.studentNo }}</el-descriptions-item>
                    <el-descriptions-item label="院系">{{ currentStudent.department }}</el-descriptions-item>
                    <el-descriptions-item label="专业">{{ currentStudent.major }}</el-descriptions-item>
                    <el-descriptions-item label="入学年份">{{ parseTime(currentStudent.admissionYear, '{y}')
                        }}</el-descriptions-item>
                    <el-descriptions-item label="联系电话">{{ currentStudent.phone || '未填写' }}</el-descriptions-item>
                    <el-descriptions-item label="邮箱">{{ currentStudent.email || '未填写' }}</el-descriptions-item>
                </el-descriptions>
            </div>

            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="detailVisible = false">关闭</el-button>
                </div>
            </template>
        </el-dialog>
        <!--学生详细信息  END-->

    </div>
</template>

<script setup>
import { listStudents as initData, submitSelection } from "@/api/mentor/selection";
import { listById as getUserById } from '@/api/user/user';
import useUserStore from '@/store/modules/user';


const { proxy } = getCurrentInstance();

const mentorList = ref([]);// 可选学生列表

const loading = ref(true);

const detailVisible = ref(false); // 学生详细信息弹窗显示控制
const currentStudent = ref({}); // 当前查看的学生信息


// 分页组件相关数据
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);


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

// 显示学生详情的方法
const showDetail = (row) => {
    currentStudent.value = { ...row };
    loading.value = true;
    getUserById({ id: row.userId })
        .then(response => {
            // 成功时合并数据
            currentStudent.value = { ...currentStudent.value, ...response.data[0] };
            // 显示对话框
            detailVisible.value = true;
        })
        .catch(error => {
            // 处理错误
            console.error('获取用户信息失败:', error);
            proxy.$modal.msgError('获取用户信息失败');
        })
        .finally(() => {
            // 确保无论成功还是失败都结束加载状态
            loading.value = false;
        });
};


/** 选中学生 */
const selectStudent = (row, status) => {
    const data = {
        id: row.id,
        teacherStatus: status
    };

    if (status === 2) {
        proxy.$modal.confirm(`确定要退选学生：${row.studentName} 吗？`, '退选确认')
            .then(() => {
                submitSelection(data)
                    .then(() => {
                        proxy.$modal.msgSuccess(`已退选学生：${row.studentName}`);
                        getList();
                    })
                    .catch(error => {
                        console.error('退选失败:', error);
                        proxy.$modal.msgError('操作失败，请稍后重试');
                    });
            })
            .catch(() => {
                proxy.$modal.msgError('已取消退选操作');
            });
        return;
    } else {
        submitSelection(data)
            .then(() => {
                proxy.$modal.msgSuccess(`已选中学生：${row.studentName}`);
                getList();
            })
            .catch(error => {
                console.error('选中失败:', error);
                proxy.$modal.msgError('操作失败，请稍后重试');
            });
    }


};

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