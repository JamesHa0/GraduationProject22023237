<template>
    <div class="app-container">
        <el-card shadow="never">
            <template #header>
                <div class="card-header">
                    <span class="card-title">双选结果</span>
                </div>
            </template>

            <el-table v-loading="loading" :data="resultList" border>
                <el-table-column label="志愿序号" prop="studentChoiceOrder" width="100" align="center">
                    <template #default="scope">
                        <el-tag type="primary">{{ getChoiceName(scope.row.studentChoiceOrder) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="导师姓名" prop="mentorName" align="center" />
                <el-table-column label="职称" prop="mentorTitle" align="center" />
                <el-table-column label="院系" prop="mentorDepartment" align="center" />
                <el-table-column label="研究领域" prop="mentorResearchField" align="center" show-overflow-tooltip />
                <el-table-column label="学生状态" prop="studentStatus" width="100" align="center">
                    <template #default="scope">
                        <el-tag :type="getStudentStatusType(scope.row.studentStatus)">
                            {{ getStudentStatusText(scope.row.studentStatus) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="导师状态" prop="teacherStatus" width="100" align="center">
                    <template #default="scope">
                        <el-tag :type="getTeacherStatusType(scope.row.teacherStatus)">
                            {{ getTeacherStatusText(scope.row.teacherStatus) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="最终状态" width="100" align="center">
                    <template #default="scope">
                        <el-tag v-if="scope.row.studentStatus === 1 && scope.row.teacherStatus === 1" type="success">
                            已确认
                        </el-tag>
                        <el-tag v-else type="info">待确认</el-tag>
                    </template>
                </el-table-column>
            </el-table>

            <el-empty v-if="!loading && resultList.length === 0" description="暂无双选记录" />
        </el-card>

        <!-- 最终结果展示 -->
        <el-card class="mt20" shadow="never" v-if="finalResult">
            <template #header>
                <div class="card-header">
                    <span class="card-title">最终确认导师</span>
                </div>
            </template>
            <el-result icon="success" title="双选成功">
                <template #sub-title>
                    您已成功选择导师：{{ finalResult.mentorName }}
                </template>
                <template #extra>
                    <el-descriptions :column="2" border style="width: 600px; margin: 0 auto;">
                        <el-descriptions-item label="导师姓名">{{ finalResult.mentorName }}</el-descriptions-item>
                        <el-descriptions-item label="职称">{{ finalResult.mentorTitle || '-' }}</el-descriptions-item>
                        <el-descriptions-item label="院系">{{ finalResult.mentorDepartment || '-' }}</el-descriptions-item>
                        <el-descriptions-item label="研究领域">{{ finalResult.mentorResearchField || '-' }}</el-descriptions-item>
                    </el-descriptions>
                </template>
            </el-result>
        </el-card>
    </div>
</template>

<script setup>
import { studentChoices } from "@/api/student/selection";
import useUserStore from '@/store/modules/user';

const { proxy } = getCurrentInstance();

const loading = ref(true);
const resultList = ref([]);
const finalResult = ref(null);

const choiceNames = ['第一', '第二', '第三', '第四', '第五'];

const getChoiceName = (order) => {
    return choiceNames[order - 1] || `第${order}`;
};

const getStudentStatusText = (status) => {
    const map = { 0: '已放弃', 1: '已提交' };
    return map[status] || '-';
};

const getStudentStatusType = (status) => {
    const map = { 0: 'info', 1: 'success' };
    return map[status] || 'info';
};

const getTeacherStatusText = (status) => {
    const map = { 0: '待处理', 1: '已同意', 2: '已拒绝' };
    return map[status] || '待处理';
};

const getTeacherStatusType = (status) => {
    const map = { 0: 'warning', 1: 'success', 2: 'danger' };
    return map[status] || 'warning';
};

const getStudentId = () => {
    const userStore = useUserStore();
    if (userStore.roleInfo && userStore.roleInfo[0]) {
        return userStore.roleInfo[0].id;
    } else {
        proxy.$modal.msgError('学生信息尚未加载');
        return null;
    }
};

function getList() {
    loading.value = true;
    const id = getStudentId();
    if (!id) {
        loading.value = false;
        return;
    }

    studentChoices({ studentId: id }).then(response => {
        resultList.value = response.data || [];
        // 查找最终确认的导师
        finalResult.value = resultList.value.find(
            item => item.studentStatus === 1 && item.teacherStatus === 1
        );
        loading.value = false;
    }).catch(() => {
        loading.value = false;
    });
}

getList();
</script>

<style scoped>
.mt20 {
    margin-top: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.card-title {
    font-size: 16px;
    font-weight: bold;
}
</style>
