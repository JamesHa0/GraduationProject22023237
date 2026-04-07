<template>
    <div class="app-container">
        <el-card>
            <template #header>
                <div class="card-header">
                    <span>导师学生关系管理</span>
                    <div class="header-actions">
                        <el-button type="primary" @click="loadData" icon="Refresh">刷新</el-button>
                        <el-button v-if="canModify" type="success" @click="showAddDialog" icon="Plus">新增关系</el-button>
                    </div>
                </div>
            </template>

            <!-- 搜索表单 -->
            <el-form :inline="true" :model="queryForm" class="search-form">
                <el-form-item label="学生姓名">
                    <el-input v-model="queryForm.studentName" placeholder="请输入学生姓名" clearable style="width: 200px" />
                </el-form-item>
                <el-form-item label="导师姓名">
                    <el-input v-model="queryForm.teacherName" placeholder="请输入导师姓名" clearable style="width: 200px" />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="handleSearch" icon="Search">搜索</el-button>
                    <el-button @click="handleReset" icon="RefreshRight">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 数据表格 -->
            <el-table v-loading="loading" :data="tableData" border style="width: 100%;">
                <el-table-column label="序号" width="60" type="index" align="center" />
                <el-table-column label="学生信息" width="280">
                    <template #default="scope">
                        <div>{{ scope.row.studentName }}</div>
                        <div class="sub-text">{{ scope.row.studentNo }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="学生院系" prop="studentDepartment" align="center" :show-overflow-tooltip="true" />
                <el-table-column label="专业" prop="major" align="center" :show-overflow-tooltip="true" />
                <el-table-column label="导师信息" width="280">
                    <template #default="scope">
                        <div>{{ scope.row.teacherName }}</div>
                        <div class="sub-text">{{ scope.row.title || '-' }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="导师院系" prop="mentorDepartment" align="center" :show-overflow-tooltip="true" />
                <el-table-column label="研究领域" prop="researchField" align="center" :show-overflow-tooltip="true" />
                <el-table-column label="导师类型" width="100" align="center">
                    <template #default="scope">
                        <el-tag :type="getMentorTypeTag(scope.row.mentorType)">
                            {{ getMentorTypeName(scope.row.mentorType) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="状态" width="120" align="center">
                    <template #default="scope">
                        <el-tag :type="getStatusTag(scope.row.teacherStatus)">
                            {{ getStatusText(scope.row.teacherStatus) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="确认时间" prop="confirmTime" width="170" align="center">
                    <template #default="scope">
                        {{ formatDate(scope.row.confirmTime) }}
                    </template>
                </el-table-column>
                <el-table-column v-if="canModify" fixed="right" label="操作" align="center" width="180">
                    <template #default="scope">
                        <el-button type="primary" link size="small" @click="showEditDialog(scope.row)">
                            编辑
                        </el-button>
                        <el-button type="danger" link size="small" @click="handleDelete(scope.row)">
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <el-pagination
                v-model:current-page="queryForm.pageNum"
                v-model:page-size="queryForm.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="loadData"
                @current-change="loadData"
                class="pagination"
            />
        </el-card>

        <!-- 新增/编辑对话框 -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
                <el-form-item label="学生" prop="studentId" required>
                    <el-select v-model="form.studentId" placeholder="请选择学生" filterable style="width: 100%;" @change="onStudentChange">
                        <el-option
                            v-for="student in availableStudents"
                            :key="student.id"
                            :label="`${student.studentName} - ${student.studentNo}`"
                            :value="student.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="导师" prop="mentorId" required>
                    <el-select v-model="form.mentorId" placeholder="请选择导师" filterable style="width: 100%;" @change="onMentorChange">
                        <el-option
                            v-for="mentor in availableMentors"
                            :key="mentor.id"
                            :label="`${mentor.teacherName} - ${mentor.department} (剩余: ${mentor.remainingQuota})`"
                            :value="mentor.id"
                            :disabled="mentor.remainingQuota <= 0"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="导师类型" prop="mentorType">
                    <el-radio-group v-model="form.mentorType">
                        <el-radio :label="1">第一导师</el-radio>
                        <el-radio :label="2">合作导师</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="状态" prop="teacherStatus">
                    <el-radio-group v-model="form.teacherStatus">
                        <el-radio :label="1">已同意</el-radio>
                        <el-radio :label="0">待处理</el-radio>
                    </el-radio-group>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确认</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { listRelationship, createRelationship, updateRelationship, deleteRelationship, listStudents, listMentors } from "@/api/selection/relationship";
import useUserStore from '@/store/modules/user';

const { proxy } = getCurrentInstance();
const userStore = useUserStore();

const loading = ref(false);
const submitLoading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('新增关系');
const formRef = ref(null);

const tableData = ref([]);
const total = ref(0);
const availableStudents = ref([]);
const availableMentors = ref([]);

const queryForm = ref({
    pageNum: 1,
    pageSize: 10,
    studentName: '',
    teacherName: ''
});

const form = ref({
    id: null,
    studentId: null,
    mentorId: null,
    mentorType: 1,
    teacherStatus: 1,
    round: 4,
    studentChoiceOrder: 1,
    studentStatus: 1
});

const rules = {
    studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
    mentorId: [{ required: true, message: '请选择导师', trigger: 'change' }]
};

// 判断是否有修改权限
const canModify = computed(() => {
    if (userStore.roles && userStore.roles.length > 0) {
        const roleId = userStore.roles[0];
        console.log('Current user roleId:', roleId);
        return roleId === 1 || roleId === 4 || roleId === 5;
    }
    return true; // 默认允许
});

const getMentorTypeName = (type) => {
    const names = { 1: '第一导师', 2: '合作导师' };
    return names[type] || '-';
};

const getMentorTypeTag = (type) => {
    const types = { 1: 'primary', 2: 'success' };
    return types[type] || 'info';
};

const getStatusText = (status) => {
    const texts = { 0: '待处理', 1: '已同意', 2: '已拒绝' };
    return texts[status] || '待处理';
};

const getStatusTag = (status) => {
    const types = { 0: 'warning', 1: 'success', 2: 'danger' };
    return types[status] || 'warning';
};

const formatDate = (date) => {
    if (!date) return '-';
    return new Date(date).toLocaleString();
};

const loadData = () => {
    loading.value = true;
    listRelationship(queryForm.value).then(response => {
        console.log('API Response:', response);
        // request.js已经把records作为data返回了，分页信息在pagination中
        let records = response.data || [];
        // 根据查询条件进行前端过滤
        if (queryForm.value.studentName) {
            records = records.filter(item =>
                item.studentName && item.studentName.includes(queryForm.value.studentName)
            );
        }
        if (queryForm.value.teacherName) {
            records = records.filter(item =>
                item.teacherName && item.teacherName.includes(queryForm.value.teacherName)
            );
        }
        tableData.value = records;
        total.value = response.pagination?.total || 0;
    }).catch((error) => {
        console.error('Load data error:', error);
        proxy.$modal.msgError('获取数据失败');
    }).finally(() => {
        loading.value = false;
    });
};

const handleSearch = () => {
    queryForm.value.pageNum = 1;
    loadData();
};

const handleReset = () => {
    queryForm.value = {
        pageNum: 1,
        pageSize: 10,
        studentName: '',
        teacherName: ''
    };
    loadData();
};

const loadAvailableStudents = () => {
    listStudents().then(response => {
        availableStudents.value = response.data || [];
    }).catch(() => {
        proxy.$modal.msgError('获取学生列表失败');
    });
};

const loadAvailableMentors = () => {
    listMentors().then(response => {
        availableMentors.value = response.data || [];
    }).catch(() => {
        proxy.$modal.msgError('获取导师列表失败');
    });
};

const showAddDialog = () => {
    dialogTitle.value = '新增关系';
    form.value = {
        id: null,
        studentId: null,
        mentorId: null,
        mentorType: 1,
        teacherStatus: 1,
        round: 4,
        studentChoiceOrder: 1,
        studentStatus: 1
    };
    loadAvailableStudents();
    loadAvailableMentors();
    dialogVisible.value = true;
};

const showEditDialog = (row) => {
    dialogTitle.value = '编辑关系';
    form.value = {
        id: row.id,
        studentId: row.studentId,
        mentorId: row.mentorId,
        mentorType: row.mentorType || 1,
        teacherStatus: row.teacherStatus || 1,
        round: row.round || 4,
        studentChoiceOrder: row.studentChoiceOrder || 1,
        studentStatus: row.studentStatus || 1
    };
    // 编辑时加载所有学生和导师
    loadAvailableStudents();
    loadAvailableMentors();
    // 将当前学生和导师添加到可选列表（如果不在列表中）
    if (row.studentId && !availableStudents.value.find(s => s.id === row.studentId)) {
        availableStudents.value.push({
            id: row.studentId,
            studentNo: row.studentNo,
            studentName: row.studentName
        });
    }
    if (row.mentorId && !availableMentors.value.find(m => m.id === row.mentorId)) {
        availableMentors.value.push({
            id: row.mentorId,
            teacherNo: row.teacherNo,
            teacherName: row.teacherName,
            department: row.mentorDepartment,
            remainingQuota: 0
        });
    }
    dialogVisible.value = true;
};

const onStudentChange = () => {
    // 学生选择变化时的处理
};

const onMentorChange = () => {
    // 导师选择变化时的处理
};

const handleSubmit = () => {
    proxy.$refs.formRef.validate(valid => {
        if (valid) {
            submitLoading.value = true;
            const api = form.value.id ? updateRelationship : createRelationship;
            api(form.value).then(() => {
                proxy.$modal.msgSuccess(form.value.id ? '更新成功' : '创建成功');
                dialogVisible.value = false;
                loadData();
            }).catch((error) => {
                proxy.$modal.msgError(error.msg || '操作失败');
            }).finally(() => {
                submitLoading.value = false;
            });
        }
    });
};

const handleDelete = (row) => {
    proxy.$modal.confirm(`确定要删除学生"${row.studentName}"与导师"${row.teacherName}"的关系吗？`).then(() => {
        deleteRelationship(row.id).then(() => {
            proxy.$modal.msgSuccess('删除成功');
            loadData();
        }).catch((error) => {
            proxy.$modal.msgError(error.msg || '删除失败');
        });
    }).catch(() => {});
};

onMounted(() => {
    loadData();
});
</script>

<style scoped>
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.header-actions {
    display: flex;
    gap: 10px;
}

.search-form {
    margin-bottom: 20px;
}

.sub-text {
    font-size: 12px;
    color: #909399;
}

.pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}
</style>
