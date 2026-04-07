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
            <el-col :span="4">
                <el-tag type="primary" effect="dark" size="large">
                    当前：{{ roundName }}
                </el-tag>
            </el-col>
            <el-col :span="4">
                <el-tag :type="phaseTagType" effect="dark" size="large">
                    {{ phaseText }}
                </el-tag>
            </el-col>
            <el-col :span="3">
                <el-tag type="danger" effect="plain" size="large">
                    导师确认截止：{{ deadlineTime }}
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

        <el-alert
            v-if="!canSubmit"
            :title="phaseAlertText"
            type="warning"
            :closable="false"
            show-icon
            class="mb-20"
        />

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
            <el-table-column fixed="right" label="操作" align="center" class-name="small-padding fixed-width"
                min-width="90">
                <template #default="scope">
                    <el-button text type="success" icon="Tickets" @click="showDetail(scope.row)">
                        详细信息
                    </el-button>
                    <template v-if="scope.row.teacherStatus === 0">
                        <el-button text bg type="primary" icon="Plus" @click="selectStudent(scope.row, 1)" :disabled="!canSubmit">
                            同意
                        </el-button>
                        <el-button text bg type="danger" icon="Close" @click="selectStudent(scope.row, 2)" :disabled="!canSubmit">
                            拒绝
                        </el-button>
                    </template>
                    <el-tag v-else-if="scope.row.teacherStatus === 1" type="success">
                        已同意
                    </el-tag>
                    <el-tag v-else-if="scope.row.teacherStatus === 2" type="danger">
                        已拒绝
                    </el-tag>

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
import { getCurrentRound, getCurrentPhase, canMentorSelect } from "@/api/selection/round";
import { getUserInfo, updateUserData } from '@/utils/userInfo';
import { onMounted } from "vue";


const { proxy } = getCurrentInstance();

const mentorList = ref([]);// 可选学生列表
const allStudents = ref([]); // 所有学生原始数据
const deadlineTime = ref(''); // 存储截止时间
const quota = ref(0); // 导师名额
const confirmedQuota = ref(0); // 已确认名额
const currentRound = ref(1); // 当前轮次
const roundName = ref('第一轮'); // 当前轮次名称
const currentPhase = ref(2); // 当前阶段
const canSubmit = ref(true); // 是否可以提交

const getRoundName = (round) => {
    const names = ['', '第一轮', '第二轮', '第三轮', '补选阶段'];
    return names[round] || '第一轮';
};

// 阶段文本
const phaseText = computed(() => {
    const texts = ['未开始', '学生选择中', '导师确认中', '等待推进', '已结束'];
    return texts[currentPhase.value] || '';
});

// 阶段标签颜色
const phaseTagType = computed(() => {
    const types = ['info', 'success', 'primary', 'warning', 'danger'];
    return types[currentPhase.value] || 'info';
});

// 阶段警告文本
const phaseAlertText = computed(() => {
    if (currentPhase.value === 1) {
        return '当前处于学生选择阶段，导师无法操作，请等待';
    } else if (currentPhase.value === 3) {
        return '当前轮次已截止，等待教学秘书推进到下一轮';
    } else if (currentPhase.value === 4) {
        return '双选已结束';
    } else if (currentPhase.value === 0) {
        return '双选尚未开始';
    }
    return '';
});

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


/** 选择学生 */
const selectStudent = (row, status) => {
    if (!canSubmit.value) {
        proxy.$modal.msgWarning('当前阶段无法操作');
        return;
    }

    console.log('选择学生，row数据:', row);
    const data = {
        id: row.id,
        mentorId: getMentorId(),
        studentId: row.studentId,
        teacherStatus: status
    };
    console.log('提交的数据:', data);

    if (status === 2) {
        proxy.$modal.confirm(`确定要拒绝学生：${row.studentName} 吗？（拒绝后不可撤销）`, '拒绝确认')
            .then(() => {
                submitSelection(data)
                    .then(() => {
                        proxy.$modal.msgSuccess(`已拒绝学生：${row.studentName}`);
                        getList();
                    })
                    .catch(error => {
                        console.error('拒绝失败:', error);
                        proxy.$modal.msgError('操作失败，请稍后重试');
                    });
            })
            .catch(() => {
                proxy.$modal.msgError('已取消拒绝操作');
            });
        return;
    } else {
        proxy.$modal.confirm(`确定要同意学生：${row.studentName} 吗？（同意后不可撤销）`, '同意确认')
            .then(() => {
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
            })
            .catch(() => {
                proxy.$modal.msgError('已取消同意操作');
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


// 获取当前轮次
function loadCurrentRound() {
    getCurrentRound().then(response => {
        currentRound.value = response.data;
        roundName.value = getRoundName(response.data);
    }).catch(() => {
        console.error('获取当前轮次失败');
    });
}

// 获取当前阶段状态
function loadCurrentPhase() {
    getCurrentPhase().then(response => {
        currentPhase.value = Number(response.data);
    }).catch(() => {
        console.error('获取当前阶段失败');
    });
}

// 检查是否可以提交
function checkCanSubmit() {
    canMentorSelect().then(response => {
        canSubmit.value = response.data === true;
    }).catch(() => {
        canSubmit.value = false;
    });
}

getList(); // 获取导师列表
loadCurrentRound(); // 获取当前轮次
loadCurrentPhase(); // 获取当前阶段
checkCanSubmit(); // 检查是否可以提交

onMounted(() => {
    quota.value = getQuota();
});

</script>

<style scoped>
.mb-20 {
    margin-bottom: 20px;
}

.footer {
    margin-top: 20px;
    margin-right: 50px;
    text-align: right
}
</style>
