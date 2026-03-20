<template>
    <div class="app-container">
        <el-alert title="请为三个志愿依次选择导师，确认无误后点击下方的“提交所有志愿”按钮" type="info" :closable="false" class="alert-info" show-icon />

        <div class="card-container">
            <el-card v-for="index in maxChoiceCount" :key="index" class="card-item" shadow="hover"
                v-if="maxChoiceCount > 0">
                <template #header>
                    <div class="card-header">
                        <span>{{ getChoiceName(index - 1) }}志愿</span>
                    </div>
                </template>
                <el-text v-if="currentChoices[index - 1]" size="large">
                    已选择导师：<el-tag type="success" size="large">{{ getMentorName(currentChoices[index - 1]) || '未选择' }}</el-tag>
                </el-text>
                <p v-else>未选择</p>
                <template #footer>
                    <div class="card-footer">
                        <el-button v-if="currentChoices[index - 1]" text type="danger" icon="Delete" @click="removeChoice(index - 1)">
                            移除
                        </el-button>
                        <el-button v-else type="primary" @click="openMentorSelector(index - 1)">
                            选择导师
                        </el-button>
                    </div>
                </template>
            </el-card>
        </div>

        <div class="submit-section">
            <el-button type="primary" size="large" :disabled="!canSubmitAll" @click="submitAllChoices">
                提交所有志愿
            </el-button>
            <el-button size="large" @click="resetAllChoices">
                重置
            </el-button>
        </div>

        <!-- 选择导师对话框 -->
        <el-dialog v-model="selectorVisible" :title="`为${getChoiceName(selectingChoiceIndex)}志愿选择导师`" width="80%" destroy-on-close>
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
                style="width: 100%;">
                <el-table-column label="序号" width="50" type="index" align="center">
                    <template #default="scope">
                        <span>{{ (pageNum - 1) * pageSize + scope.$index + 1 }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="姓名" align="center" prop="teacherName" :show-overflow-tooltip="true" />
                <el-table-column label="职称" align="center" prop="title" :show-overflow-tooltip="true" />
                <el-table-column label="院系" align="center" prop="department" :show-overflow-tooltip="true" />
                <el-table-column label="研究领域" align="center" prop="researchField" :show-overflow-tooltip="true" />
                <el-table-column label="状态" align="center" prop="" :show-overflow-tooltip="true">
                    <template #default="scope">
                        <el-tag v-if="isMentorSelectedInAnyChoice(scope.row.id)" type="warning">已选</el-tag>
                        <el-tag v-else-if="scope.row.status == 1" type="primary">可选</el-tag>
                        <el-tag v-else-if="scope.row.status == 2" type="danger">无剩余名额</el-tag>
                        <el-tag v-else type="info">不可选</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="剩余名额" align="center" prop="remainingQuota" :show-overflow-tooltip="true" />
                <el-table-column fixed="right" label="操作" align="center" class-name="small-padding fixed-width">
                    <template #default="scope">
                        <el-button v-if="isMentorSelectedInAnyChoice(scope.row.id)" text type="warning" icon="Check" disabled>
                            已选择
                        </el-button>
                        <el-button v-else text type="primary" icon="Plus"
                            @click="confirmSelectMentor(scope.row)" :disabled="scope.row.status === 2">
                            选择
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <pagination v-show="total > 0" :total="total" v-model:page="pageNum" v-model:limit="pageSize" />
        </el-dialog>
    </div>
</template>

<script setup>
import { listMentor as initData, submitBatchSelection, studentChoices } from "@/api/student/selection";
import useUserStore from '@/store/modules/user';
import { getConfigKey } from '@/api/system/config';

const { proxy } = getCurrentInstance();

const mentorList = ref([]);

const maxChoiceCount = ref(0);
const choiceNames = ['第一', '第二', '第三', '第四', '第五'];
const getChoiceName = (index) => {
    return choiceNames[index] || `第${index + 1}`;
}

// 当前选择的志愿（临时状态，未提交）
const currentChoices = ref([]);

// 已提交的志愿
const submittedChoices = ref([]);

// 选择器相关
const selectorVisible = ref(false);
const selectingChoiceIndex = ref(0);

const loading = ref(true);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

const studentId = ref(null);
const getStudentId = () => {
    const userStore = useUserStore();
    if (userStore.roleInfo && userStore.roleInfo[0]) {
        return userStore.roleInfo[0].id;
    } else {
        proxy.$modal.msgError('学生信息尚未加载');
        return null;
    }
};

// 根据导师ID获取导师名称
const getMentorName = (mentorId) => {
    const mentor = mentorList.value.find(m => m.id === mentorId);
    return mentor ? mentor.teacherName : '';
};

// 检查导师是否已在任何志愿中被选择
const isMentorSelectedInAnyChoice = (mentorId) => {
    return currentChoices.value.includes(mentorId);
};

// 检查是否可以提交所有志愿
const canSubmitAll = computed(() => {
    // 必须填满所有志愿才能提交
    return currentChoices.value.filter(id => id !== null && id !== undefined).length === maxChoiceCount.value &&
           submittedChoices.value.length === 0;
});

let queryParams = ref({
    name: undefined
});

// 打开导师选择对话框
const openMentorSelector = (choiceIndex) => {
    selectingChoiceIndex.value = choiceIndex;
    selectorVisible.value = true;
    handleQuery();
};

// 确认选择导师
const confirmSelectMentor = (row) => {
    currentChoices.value[selectingChoiceIndex.value] = row.id;
    selectorVisible.value = false;
    proxy.$modal.msgSuccess(`已为${getChoiceName(selectingChoiceIndex.value)}志愿选择导师：${row.teacherName}`);
};

// 移除某个志愿的选择
const removeChoice = (choiceIndex) => {
    currentChoices.value[choiceIndex] = null;
};

// 重置所有选择
const resetAllChoices = () => {
    proxy.$modal.confirm('确定要重置所有选择吗？').then(() => {
        currentChoices.value = new Array(maxChoiceCount.value).fill(null);
        proxy.$modal.msgSuccess('已重置');
    }).catch(() => {});
};

// 提交所有志愿
const submitAllChoices = () => {
    if (!studentId.value) {
        proxy.$modal.msgError('学生信息尚未加载');
        return;
    }

    // 验证所有志愿都已选择
    const filledCount = currentChoices.value.filter(id => id !== null && id !== undefined).length;
    if (filledCount < maxChoiceCount.value) {
        proxy.$modal.msgWarning(`请先填满所有${maxChoiceCount.value}个志愿`);
        return;
    }

    // 验证没有重复选择的导师
    const uniqueMentors = new Set(currentChoices.value.filter(id => id));
    if (uniqueMentors.size !== currentChoices.value.length) {
        proxy.$modal.msgError('不能重复选择同一位导师');
        return;
    }

    proxy.$modal.confirm('确定提交所有志愿吗？提交后将不可修改。').then(() => {
        const data = {
            studentId: studentId.value,
            choices: currentChoices.value.map((mentorId, index) => ({
                mentorId: mentorId,
                studentChoiceOrder: index + 1
            }))
        };

        submitBatchSelection(data).then(response => {
            proxy.$modal.msgSuccess('所有志愿提交成功');
            getStudentChoices();
        }).catch(error => {
            proxy.$modal.msgError('提交失败');
            console.error('提交错误:', error);
        });
    }).catch(() => {});
};

// 查询志愿轮数
function getMaxChoiceCount() {
    getConfigKey("student_max_choices").then(response => {
        maxChoiceCount.value = Math.max(1, parseInt(response.data) || 1);
        // 初始化choices数组
        currentChoices.value = new Array(maxChoiceCount.value).fill(null);
        console.log(`获取最大志愿数:`, maxChoiceCount.value);
    }).catch(() => {
        proxy.$modal.msgError('获取最大志愿数失败');
        maxChoiceCount.value = 0;
    });
}

// 查询学生已选志愿
function getStudentChoices() {
    if (!studentId.value) {
        return Promise.resolve();
    }

    let param = {
        studentId: studentId.value
    };

    return studentChoices(param).then(response => {
        console.log(`学生志愿情况：`, response);
        submittedChoices.value = response.data || [];

        // 如果已经有提交的志愿，填充到currentChoices中（但不允许修改）
        if (submittedChoices.value.length > 0) {
            submittedChoices.value.forEach(choice => {
                if (choice.studentChoiceOrder >= 1 && choice.studentChoiceOrder <= maxChoiceCount.value) {
                    currentChoices.value[choice.studentChoiceOrder - 1] = choice.mentorId;
                }
            });
        }
    }).catch(error => {
        proxy.$modal.msgError("获取学生志愿情况失败");
        console.error("获取志愿情况错误:", error);
        return Promise.resolve();
    });
}

// 查询可选导师列表
function getList() {
    loading.value = true;

    const id = getStudentId();
    if (!id) {
        proxy.$modal.msgError('学生信息尚未加载');
        loading.value = false;
        return;
    }
    studentId.value = id;

    // 确保先获取学生志愿情况，再获取导师列表
    getStudentChoices().then(() => {
        return initData(queryParams.value);
    }).then(response => {
        mentorList.value = response.data.map(mentor => {
            const isSubmitted = submittedChoices.value.some(choice => choice.mentorId === mentor.id);
            if (isSubmitted) {
                mentor.status = 3;
            } else if (mentor.remainingQuota > 0) {
                mentor.status = 1;
            } else {
                mentor.status = 2;
            }
            return mentor;
        });
        console.log(response);

        total.value = response.data.length;
        loading.value = false;
    }).catch(error => {
        console.error("获取数据失败:", error);
        proxy.$modal.msgError("获取数据失败");
        loading.value = false;
    });

    getMaxChoiceCount();
}

// 搜索按钮操作
function handleQuery() {
    pageNum.value = 1;
    loading.value = true;
    initData(queryParams.value).then(response => {
        mentorList.value = response.data.map(mentor => {
            const isSubmitted = submittedChoices.value.some(choice => choice.mentorId === mentor.id);
            if (isSubmitted) {
                mentor.status = 3;
            } else if (mentor.remainingQuota > 0) {
                mentor.status = 1;
            } else {
                mentor.status = 2;
            }
            return mentor;
        });
        total.value = response.data.length;
        loading.value = false;
    });
}

// 重置按钮操作
function resetQuery() {
    proxy.resetForm("queryRef");
    handleQuery();
}

getList();
</script>

<style scoped>
.alert-info {
    margin-bottom: 20px;
}

.card-container {
    display: flex;
    justify-content: space-between;
    gap: 20px;
    margin-bottom: 20px;
    flex-wrap: wrap;
}

.card-item {
    flex: 1;
    min-width: 200px;
}

.card-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
}

.submit-section {
    margin-top: 30px;
    text-align: center;
    padding: 20px;
    border-top: 1px solid #ebeef5;
}
</style>
