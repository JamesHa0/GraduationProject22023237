<template>
    <div class="app-container">
        <el-card>
            <template #header>
                <div class="card-header">
                    <span>导师名额管理</span>
                    <div class="header-actions">
                        <el-button type="primary" @click="loadData" icon="Refresh">刷新</el-button>
                        <el-button type="success" @click="showBatchDialog" :disabled="selectedRows.length === 0" icon="Edit">
                            批量设置 ({{ selectedRows.length }})
                        </el-button>
                        <el-button type="danger" @click="handleResetAll" icon="RefreshLeft">重置全部名额</el-button>
                    </div>
                </div>
            </template>

            <el-form :inline="true" :model="queryForm" class="search-form">
                <el-form-item label="导师姓名">
                    <el-input v-model="queryForm.teacherName" placeholder="请输入导师姓名" clearable style="width: 200px" />
                </el-form-item>
                <el-form-item label="院系">
                    <el-input v-model="queryForm.department" placeholder="请输入院系" clearable style="width: 200px" />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="handleSearch" icon="Search">搜索</el-button>
                    <el-button @click="handleResetQuery" icon="RefreshRight">重置</el-button>
                </el-form-item>
            </el-form>

            <el-table v-loading="loading" :data="tableData" border style="width: 100%;" @selection-change="handleSelectionChange">
                <el-table-column type="selection" width="50" align="center" />
                <el-table-column label="序号" width="60" type="index" align="center" />
                <el-table-column label="导师信息" min-width="180">
                    <template #default="scope">
                        <div>{{ scope.row.teacherName }}</div>
                        <div class="sub-text">{{ scope.row.teacherNo }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="职称" prop="title" width="100" align="center">
                    <template #default="scope">
                        {{ scope.row.title || '-' }}
                    </template>
                </el-table-column>
                <el-table-column label="院系" prop="department" min-width="120" :show-overflow-tooltip="true" />
                <el-table-column label="研究领域" prop="researchField" min-width="140" :show-overflow-tooltip="true">
                    <template #default="scope">
                        {{ scope.row.researchField || '-' }}
                    </template>
                </el-table-column>
                <el-table-column label="招生名额" prop="quota" width="100" align="center" />
                <el-table-column label="剩余名额" width="100" align="center">
                    <template #default="scope">
                        <span :class="{ 'text-danger': scope.row.remainingQuota === 0 }">
                            {{ scope.row.remainingQuota }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column label="已确认名额" prop="confirmedQuota" width="100" align="center" />
                <el-table-column label="名额状态" width="100" align="center">
                    <template #default="scope">
                        <el-tag :type="getQuotaStatusTag(scope.row)" size="small">
                            {{ getQuotaStatusText(scope.row) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column fixed="right" label="操作" align="center" width="100">
                    <template #default="scope">
                        <el-button type="primary" link size="small" @click="showEditDialog(scope.row)">
                            编辑名额
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

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

        <!-- 单个编辑弹窗 -->
        <el-dialog v-model="editDialogVisible" title="编辑导师名额" width="500px" :close-on-click-modal="false">
            <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="120px">
                <el-form-item label="导师姓名">
                    <el-input :model-value="editForm.teacherName" disabled />
                </el-form-item>
                <el-form-item label="当前名额">
                    <el-input :model-value="editForm.currentQuota" disabled />
                </el-form-item>
                <el-form-item label="已确认名额">
                    <el-input :model-value="editForm.confirmedQuota" disabled />
                </el-form-item>
                <el-form-item label="新招生名额" prop="quota">
                    <el-input-number
                        v-model="editForm.quota"
                        :min="editForm.confirmedQuota"
                        :step="1"
                        :precision="0"
                        controls-position="right"
                        style="width: 100%;"
                    />
                </el-form-item>
                <el-form-item label="预计算剩余">
                    <el-tag :type="editForm.quota - editForm.confirmedQuota > 0 ? 'success' : 'warning'" size="large">
                        {{ editForm.quota - editForm.confirmedQuota }}
                    </el-tag>
                    <span class="form-tip">（新名额 - 已确认名额）</span>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="editDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleEditSubmit" :loading="submitLoading">确认</el-button>
            </template>
        </el-dialog>

        <!-- 批量设置弹窗 -->
        <el-dialog v-model="batchDialogVisible" title="批量设置导师名额" width="500px" :close-on-click-modal="false">
            <el-alert
                :title="`已选中 ${selectedRows.length} 位导师，将统一设置招生名额`"
                type="info"
                :closable="false"
                show-icon
                style="margin-bottom: 20px;"
            />
            <el-form :model="batchForm" :rules="batchRules" ref="batchFormRef" label-width="120px">
                <el-form-item label="招生名额" prop="quota">
                    <el-input-number
                        v-model="batchForm.quota"
                        :min="0"
                        :step="1"
                        :precision="0"
                        controls-position="right"
                        style="width: 100%;"
                    />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="batchDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleBatchSubmit" :loading="submitLoading">确认</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { listQuota, updateQuota, batchUpdateQuota, resetAllQuota } from "@/api/selection/quota";

const { proxy } = getCurrentInstance();

const loading = ref(false);
const submitLoading = ref(false);
const editDialogVisible = ref(false);
const batchDialogVisible = ref(false);
const editFormRef = ref(null);
const batchFormRef = ref(null);

const tableData = ref([]);
const total = ref(0);
const selectedRows = ref([]);

const queryForm = ref({
    pageNum: 1,
    pageSize: 10,
    teacherName: '',
    department: ''
});

const editForm = ref({
    teacherId: null,
    teacherName: '',
    currentQuota: 0,
    confirmedQuota: 0,
    quota: 0
});

const batchForm = ref({
    quota: 0
});

const editRules = {
    quota: [
        { required: true, message: '请输入名额', trigger: 'blur' },
        { type: 'number', message: '名额必须为数字', trigger: 'blur' }
    ]
};

const batchRules = {
    quota: [
        { required: true, message: '请输入名额', trigger: 'blur' },
        { type: 'number', message: '名额必须为数字', trigger: 'blur' }
    ]
};

const getQuotaStatusText = (row) => {
    const remaining = row.remainingQuota || 0;
    const total = row.quota || 0;
    if (total === 0) return '未设置';
    if (remaining === 0) return '已满';
    if (remaining <= Math.ceil(total * 0.3)) return '紧张';
    return '充足';
};

const getQuotaStatusTag = (row) => {
    const remaining = row.remainingQuota || 0;
    const total = row.quota || 0;
    if (total === 0) return 'info';
    if (remaining === 0) return 'danger';
    if (remaining <= Math.ceil(total * 0.3)) return 'warning';
    return 'success';
};

const loadData = () => {
    loading.value = true;
    listQuota(queryForm.value).then(response => {
        tableData.value = response.data || [];
        total.value = response.pagination?.total || 0;
    }).catch(() => {
        proxy.$modal.msgError('获取数据失败');
    }).finally(() => {
        loading.value = false;
    });
};

const handleSearch = () => {
    queryForm.value.pageNum = 1;
    loadData();
};

const handleResetQuery = () => {
    queryForm.value = {
        pageNum: 1,
        pageSize: 10,
        teacherName: '',
        department: ''
    };
    loadData();
};

const handleSelectionChange = (selection) => {
    selectedRows.value = selection;
};

const showEditDialog = (row) => {
    editForm.value = {
        teacherId: row.id,
        teacherName: row.teacherName,
        currentQuota: row.quota || 0,
        confirmedQuota: row.confirmedQuota || 0,
        quota: row.quota || 0
    };
    editDialogVisible.value = true;
};

const showBatchDialog = () => {
    if (selectedRows.value.length === 0) {
        proxy.$modal.msgWarning('请至少选择一位导师');
        return;
    }
    batchForm.value = { quota: 0 };
    batchDialogVisible.value = true;
};

const handleEditSubmit = () => {
    proxy.$refs.editFormRef.validate(valid => {
        if (valid) {
            if (editForm.value.quota < 0) {
                proxy.$modal.msgError('名额不能为负数');
                return;
            }
            if (editForm.value.quota < editForm.value.confirmedQuota) {
                proxy.$modal.msgError(`新名额不能小于已确认名额（${editForm.value.confirmedQuota}）`);
                return;
            }
            submitLoading.value = true;
            updateQuota({
                teacherId: editForm.value.teacherId,
                quota: editForm.value.quota
            }).then(() => {
                proxy.$modal.msgSuccess('更新成功');
                editDialogVisible.value = false;
                loadData();
            }).catch((error) => {
                proxy.$modal.msgError(error.msg || '更新失败');
            }).finally(() => {
                submitLoading.value = false;
            });
        }
    });
};

const handleBatchSubmit = () => {
    proxy.$refs.batchFormRef.validate(valid => {
        if (valid) {
            if (batchForm.value.quota < 0) {
                proxy.$modal.msgError('名额不能为负数');
                return;
            }
            proxy.$modal.confirm(`确定将选中的 ${selectedRows.value.length} 位导师的招生名额统一设置为 ${batchForm.value.quota} 吗？`).then(() => {
                submitLoading.value = true;
                const teacherIds = selectedRows.value.map(row => row.id);
                batchUpdateQuota({
                    teacherIds: teacherIds,
                    quota: batchForm.value.quota
                }).then((response) => {
                    const result = response.data;
                    if (result.failCount === 0) {
                        proxy.$modal.msgSuccess(`批量设置成功，共 ${result.successCount} 位导师`);
                    } else {
                        let msg = `成功 ${result.successCount} 位，失败 ${result.failCount} 位`;
                        if (result.failedDetails && result.failedDetails.length > 0) {
                            msg += '：' + result.failedDetails.join('；');
                        }
                        proxy.$modal.msgWarning(msg);
                    }
                    batchDialogVisible.value = false;
                    loadData();
                }).catch((error) => {
                    proxy.$modal.msgError(error.msg || '批量更新失败');
                }).finally(() => {
                    submitLoading.value = false;
                });
            }).catch(() => {});
        }
    });
};

const handleResetAll = () => {
    proxy.$modal.confirm('确定要重置所有导师的名额为0吗？此操作不可撤销！').then(() => {
        loading.value = true;
        resetAllQuota().then(() => {
            proxy.$modal.msgSuccess('重置成功');
            loadData();
        }).catch((error) => {
            proxy.$modal.msgError(error.msg || '重置失败');
        }).finally(() => {
            loading.value = false;
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

.text-danger {
    color: #F56C6C;
    font-weight: bold;
}

.form-tip {
    margin-left: 10px;
    font-size: 12px;
    color: #909399;
}
</style>
