<template>
    <div class="app-container">
        <el-card>
            <template #header>
                <div class="card-header">
                    <span>未匹配学生列表</span>
                    <el-button type="primary" @click="loadUnmatchedStudents" icon="Refresh">刷新</el-button>
                </div>
            </template>

            <el-table v-loading="loading" :data="unmatchedStudents" style="width: 100%;">
                <el-table-column label="序号" width="60" type="index" align="center" />
                <el-table-column label="姓名" align="center" prop="studentName" :show-overflow-tooltip="true" />
                <el-table-column label="学号" align="center" prop="studentNo" :show-overflow-tooltip="true" />
                <el-table-column label="院系" align="center" prop="department" :show-overflow-tooltip="true" />
                <el-table-column label="专业" align="center" prop="major" :show-overflow-tooltip="true" />
                <el-table-column fixed="right" label="操作" align="center" width="200">
                    <template #default="scope">
                        <el-button type="primary" link icon="User" @click="showAssignDialog(scope.row)">
                            分配导师
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <!-- 分配导师对话框 -->
        <el-dialog v-model="assignDialogVisible" title="分配导师" width="600px">
            <el-form :model="assignForm" label-width="100px">
                <el-form-item label="学生姓名">
                    <span>{{ currentStudent.studentName }}</span>
                </el-form-item>
                <el-form-item label="学号">
                    <span>{{ currentStudent.studentNo }}</span>
                </el-form-item>
                <el-form-item label="选择导师" required>
                    <el-select v-model="assignForm.mentorId" placeholder="请选择导师" filterable style="width: 100%;">
                        <el-option
                            v-for="mentor in availableMentors"
                            :key="mentor.id"
                            :label="`${mentor.teacherName} - ${mentor.department} (剩余: ${mentor.remainingQuota})`"
                            :value="mentor.id"
                            :disabled="mentor.remainingQuota <= 0"
                        />
                    </el-select>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="assignDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleAssign" :loading="assignLoading">确认分配</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { getUnmatchedStudents, manualAssign } from "@/api/selection/round";
import { listMentor } from "@/api/student/selection";

const { proxy } = getCurrentInstance();

const loading = ref(false);
const unmatchedStudents = ref([]);
const assignDialogVisible = ref(false);
const assignLoading = ref(false);
const currentStudent = ref({});
const availableMentors = ref([]);

const assignForm = ref({
    mentorId: null
});

const loadUnmatchedStudents = () => {
    loading.value = true;
    getUnmatchedStudents().then(response => {
        unmatchedStudents.value = response.data || [];
    }).catch(() => {
        proxy.$modal.msgError('获取未匹配学生列表失败');
    }).finally(() => {
        loading.value = false;
    });
};

const loadAvailableMentors = () => {
    listMentor('').then(response => {
        availableMentors.value = response.data || [];
    }).catch(() => {
        proxy.$modal.msgError('获取导师列表失败');
    });
};

const showAssignDialog = (student) => {
    currentStudent.value = student;
    assignForm.value.mentorId = null;
    loadAvailableMentors();
    assignDialogVisible.value = true;
};

const handleAssign = () => {
    if (!assignForm.value.mentorId) {
        proxy.$modal.msgWarning('请选择导师');
        return;
    }

    proxy.$modal.confirm(`确定要将学生 ${currentStudent.value.studentName} 分配给该导师吗？`).then(() => {
        assignLoading.value = true;
        manualAssign({
            studentId: currentStudent.value.studentId,
            mentorId: assignForm.value.mentorId
        }).then(() => {
            proxy.$modal.msgSuccess('分配成功');
            assignDialogVisible.value = false;
            loadUnmatchedStudents();
        }).catch(() => {
            proxy.$modal.msgError('分配失败');
        }).finally(() => {
            assignLoading.value = false;
        });
    }).catch(() => {});
};

onMounted(() => {
    loadUnmatchedStudents();
});
</script>

<style scoped>
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
</style>
