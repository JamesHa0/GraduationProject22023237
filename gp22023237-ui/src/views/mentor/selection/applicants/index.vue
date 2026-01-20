<template>
    <div class="app-container">
        <el-row>
            <el-col :span="12">
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
                        <el-checkbox v-model="queryParams.onlyUnselected" @change="handleQuery">只显示未选</el-checkbox>
                    </el-form-item>
                </el-form>
            </el-col>
            <el-col :span="6">
                <el-tag type="danger" effect="plain" size="large">
                    第一轮导师确认截止时间：{{ deadlineTime }}
                </el-tag>
            </el-col>
            <el-col :span="2">
                <el-tag type="warning" effect="plain" size="large">
                    剩余名额：{{ quota - confirmedQuota }}/{{ quota }}
                </el-tag>
            </el-col>
            <el-col :span="4">
                <el-form-item>
                    <el-button type="primary" icon="switch">切换学生卡片模式</el-button>
                </el-form-item>
            </el-col>
        </el-row>


        <el-table v-loading="loading" :data="mentorList.slice((pageNum - 1) * pageSize, pageNum * pageSize)"
            style="width: 100%;">
            <el-table-column label="序号" width="50" type="index" align="center">
                <template #default="scope">
                    <span>{{ (pageNum - 1) * pageSize + scope.$index + 1 }}</span>
                </template>
            </el-table-column>
            <el-table-column label="姓名" align="center" prop="studentName" :show-overflow-tooltip="true" />
            <el-table-column label="学号" align="center" prop="studentNo" :show-overflow-tooltip="true" />
            <el-table-column label="院系" align="center" prop="department" :show-overflow-tooltip="true" />
            <el-table-column label="专业" align="center" prop="major" :show-overflow-tooltip="true" />
            <el-table-column fixed="right" label="操作" align="center" class-name="small-padding fixed-width">
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
import { getConfigKey } from "@/api/system/config";
import { getUserInfo, updateUserData } from '@/utils/userInfo';
import { onMounted } from "vue";


const { proxy } = getCurrentInstance();

const mentorList = ref([]);// 可选学生列表
const allStudents = ref([]); // 所有学生原始数据
const deadlineTime = ref(''); // 存储截止时间
const quota = ref(0); // 导师名额
const confirmedQuota = ref(0); // 已确认名额

const loading = ref(true);

const detailVisible = ref(false); // 学生详细信息弹窗显示控制
const currentStudent = ref({}); // 当前查看的学生信息


// 分页组件相关数据
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

// 获取导师id
const getMentorId = () => getUserInfo('id');
// 获取导师名额
const getQuota = () => getUserInfo('quota');
// 获取导师已确认名额
const getConfirmedQuota = () => getUserInfo('confirmedQuota');


// 获取截止时间
getConfigKey('first_round_end_tutor')
    .then(response => {
        deadlineTime.value = response?.data || '未设置';
    })
    .catch(error => {
        proxy.$modal.msgError('获取截止时间失败');
        console.error('获取截止时间失败:', error);
        deadlineTime.value = '未设置';
    });

let queryParams = ref({
    name: undefined,
    onlyUnselected: false
});

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
        mentorId: getMentorId(),
        teacherStatus: status
    };

    if (status === 2) {
        proxy.$modal.confirm(`确定要退选学生：${row.studentName} 吗？`, '退选确认')
            .then(() => {
                submitSelection(data)
                    .then(() => {
                        updateUserData('confirmedQuota', parseInt(confirmedQuota.value - 1));
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
                updateUserData('confirmedQuota', parseInt(confirmedQuota.value + 1));
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
    applyFilters();
}

/** 重置按钮操作 */
function resetQuery() {
    queryParams.value.name = undefined;
    queryParams.value.onlyUnselected = false;
    handleQuery();
}

/** 查询可选学生列表 */
function getList() {
    loading.value = true;

    const id = getMentorId();
    if (!id) {
        proxy.$modal.msgError('导师信息尚未加载');
        loading.value = false;
        return;
    }

    confirmedQuota.value = getConfirmedQuota();

    const data = {
        mentorId: id
    };
    initData(data).then(response => {
        allStudents.value = response.data; // 保存原始数据
        applyFilters(); // 应用过滤条件
    }).finally(() => {
        loading.value = false;
    });
}

// 应用过滤条件
function applyFilters() {
    let result = allStudents.value;

    // 按姓名过滤（不区分大小写）
    if (queryParams.value.name) {
        const searchName = queryParams.value.name.toLowerCase();
        result = result.filter(item => item.studentName.toLowerCase().includes(searchName));
    }

    // 按是否已选过滤
    if (queryParams.value.onlyUnselected) {
        result = result.filter(item => item.teacherStatus !== 1);
    }

    mentorList.value = result;
    total.value = result.length;
}


getList(); // 获取导师列表

onMounted(() => {
    quota.value = getQuota();
});

</script>

<style scoped>
.footer {
    margin-top: 20px;
    margin-right: 50px;
    text-align: right
}
</style>