<template>
    <div class="app-container">
        <div class="card-container">
            <el-card v-for="index in maxChoiceCount" :key="index" class="card-item" shadow="hover"
                v-if="maxChoiceCount > 0">
                <template #header>
                    <div class="card-header">
                        <span>{{ getChoiceName(index - 1) }}志愿</span>
                    </div>
                </template>
                <el-text v-if="isChoiceSelected(index)" size="large">
                    已选择导师：<el-tag type="success" size="large">{{ getChoiceMentor(index) || '未选择' }}</el-tag>
                </el-text>
                <p v-else>未选择</p>
                <template #footer>
                    <div class="card-footer">
                        <el-button v-if="!isChoiceSelected(index)" type="primary" @click="submit(index - 1, 1)">
                            提交
                        </el-button>
                        <el-button v-else type="danger" @click="submit(index - 1, 0)">
                            放弃
                        </el-button>
                    </div>
                </template>
            </el-card>
        </div>
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
            <el-table-column label="ID" align="center" prop="id" :show-overflow-tooltip="true" v-if=false />
            <el-table-column label="姓名" align="center" prop="teacherName" :show-overflow-tooltip="true" />
            <el-table-column label="职称" align="center" prop="title" :show-overflow-tooltip="true" />
            <el-table-column label="院系" align="center" prop="department" :show-overflow-tooltip="true" />
            <el-table-column label="研究领域" align="center" prop="researchField" :show-overflow-tooltip="true" />
            <el-table-column label="状态" align="center" prop="" :show-overflow-tooltip="true">
                <template #default="scope">
                    <el-tag v-if="scope.row.status == 1" type="primary">可选</el-tag>
                    <el-tag v-else-if="scope.row.status == 2" type="danger">无剩余名额</el-tag>
                    <el-tag v-else-if="scope.row.status == 3" type="success">已选择</el-tag>
                    <el-tag v-else type="info">不可选</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="剩余名额" align="center" prop="remainingQuota" :show-overflow-tooltip="true" />
            <el-table-column fixed="right" label="操作" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button v-if="isMentorSelected(scope.row.id)" text type="success" icon="Check" disabled>
                        已提交
                    </el-button>
                    <el-button v-else-if="selectedMentor !== scope.row.id" text type="primary" icon="Plus"
                        @click="selectMentor(scope.row)" :disabled="scope.row.status === 2">
                        选中
                    </el-button>
                    <el-button v-else text type="danger" icon="Close" @click="cancelSelection()">
                        取消
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" v-model:page="pageNum" v-model:limit="pageSize" />

    </div>
</template>

<script setup>
import { listMentor as initData, submitSelection, studentChoices } from "@/api/student/selection";
import useUserStore from '@/store/modules/user';
import { getConfigKey } from '@/api/system/config';

const { proxy } = getCurrentInstance();

const mentorList = ref([]);

const maxChoiceCount = ref(0); // 最大志愿数
const choiceNames = ['第一', '第二', '第三', '第四', '第五'];
// 获取第几志愿名称
const getChoiceName = (index) => {
    return choiceNames[index] || `第${index + 1}`;
}

const selectedMentor = ref(null); // 选中的导师

const loading = ref(true);

const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

// 访问学生信息中的ID
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

const studentChoicesData = ref([]); // 学生志愿情况
// 获取第几志愿的导师名称
const getChoiceMentor = (choiceOrder) => {
    const choice = studentChoicesData.value.find(item => item.studentChoiceOrder === choiceOrder);
    return choice ? choice.mentorName : null;
}
// 判断第几志愿是否已选择
const isChoiceSelected = (choiceOrder) => {
    return studentChoicesData.value.some(item => item.studentChoiceOrder === choiceOrder);
}

let queryParams = ref({
    name: undefined
});

// 判断导师是否已被选择的函数
const isMentorSelected = (mentorId) => {
    return studentChoicesData.value.some(choice => choice.mentorId === mentorId);
};
// 检查已选择的志愿数量是否达到上限
const isMaxChoicesReached = () => {
    return studentChoicesData.value.length >= maxChoiceCount.value;
};
// 选中导师函数
const selectMentor = (row) => {
    // 检查是否已达到最大选择数
    if (isMaxChoicesReached()) {
        proxy.$modal.msgWarning(`已达到最大志愿数 (${maxChoiceCount.value})，无法继续选择`);
        return;
    }
    row.isLoading = true;
    // 如果已有选中的导师，先取消之前的选择
    if (selectedMentor.value) {
        const prevRow = mentorList.value.find(item => item.id === selectedMentor.value);
        if (prevRow) {
            prevRow.status = prevRow.remainingQuota > 0 ? 1 : 2;
        }
    }

    // 选中新的导师
    selectedMentor.value = row.id;
    row.isLoading = false;
    row.status = 3; // 已选中状态
    proxy.$modal.msgSuccess("已选中该导师");
};

// 取消选中函数
const cancelSelection = () => {
    if (selectedMentor.value) {
        const row = mentorList.value.find(item => item.id === selectedMentor.value);
        if (row) {
            row.status = row.remainingQuota > 0 ? 1 : 2;
        }
        selectedMentor.value = null;
        proxy.$modal.msgSuccess("已取消选中");
    }
};


// 查询志愿轮数
function getMaxChoiceCount() {
    getConfigKey("student_max_choices").then(response => {
        // 确保获取到有效值，至少为1
        maxChoiceCount.value = Math.max(1, parseInt(response.data) || 1);
        console.log(`获取最大志愿数:`, maxChoiceCount.value);
    }).catch(() => {
        // 出错时设置默认值
        proxy.$modal.msgError('获取最大志愿数失败');
        maxChoiceCount.value = 0;
    });

}

/** 查询学生志愿情况 */
function getStudentChoices() {

    let param = ref({
        studentId: studentId.value
    });

    // 返回 Promise 以支持链式调用
    return studentChoices(param.value).then(response => {
        console.log(`学生志愿情况：`, response);
        studentChoicesData.value = response.data || [];
    }).catch(error => {
        proxy.$modal.msgError("获取学生志愿情况失败");
        console.error("获取志愿情况错误:", error);
        // 即使出错也要返回一个 resolved promise 以保证链式调用继续
        return Promise.resolve();
    });
}


/** 查询可选导师列表 */
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
            // 检查该导师是否已被选为任何志愿
            const isAlreadySelected = studentChoicesData.value.some(choice =>
                choice.mentorId === mentor.id
            );
            if (isAlreadySelected) {
                mentor.status = 3; // 已选择
            } else if (mentor.remainingQuota > 0) {
                mentor.status = 1; // 可选
            } else {
                mentor.status = 2; // 无剩余名额
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

    getMaxChoiceCount(); // 获取最大志愿数
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



/** 提交按钮操作 */
function submit(choiceIndex, studentStatus) {

    if (!studentId.value) {
        proxy.$modal.msgError('学生信息尚未加载')
        return;
    }

    if (studentStatus == 1) {
        if (!selectedMentor.value) {
            proxy.$modal.msgWarning(`请为第${choiceIndex + 1}志愿选择一位导师`);
            return;
        }

        const data = {
            round: 1,  // 暂定1轮
            studentChoiceOrder: choiceIndex + 1,
            studentId: studentId.value,
            mentorId: selectedMentor.value,
            studentStatus: studentStatus
        }

        console.log(`提交的信息:`, data);

        proxy.$modal.confirm(`提交第${choiceIndex + 1}志愿选择`).then(() => {
            // 提交选中导师 ID
            submitSelection(data).then(response => {
                proxy.$modal.msgSuccess(`第${choiceIndex + 1}志愿提交成功`);

                selectedMentor.value = null; // 清除选中状态

                getList();
            }).catch(error => {
                proxy.$modal.msgError("提交失败");
                console.error("提交错误:", error);
            });
        }).catch(() => { });
    }
    else if (studentStatus == 0) {
        // 获取该志愿对应的已选导师信息
        const selectedChoice = studentChoicesData.value.find(
            choice => choice.studentChoiceOrder === choiceIndex + 1
        );

        proxy.$modal.confirm(`是否放弃已提交的第${choiceIndex + 1}志愿？`).then(() => {

            const data = {
                round: 1,  // 暂定1轮
                studentChoiceOrder: choiceIndex + 1,
                studentId: studentId.value,
                mentorId: selectedChoice.mentorId,
                studentStatus: studentStatus
            }
            console.log(`放弃信息:`, data);


            submitSelection(data).then(response => {
                proxy.$modal.msgSuccess(`第${choiceIndex + 1}志愿放弃成功`);
                selectedMentor.value = null; // 清除选中状态
                getList();
            }).catch(error => {
                proxy.$modal.msgError("放弃失败");
                console.error("放弃错误:", error);
            });
        }).catch(() => { });
    }

}

getList(); // 获取导师列表
</script>

<style scoped>
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

.footer {
    margin-top: 20px;
    margin-right: 50px;
    text-align: right
}
</style>