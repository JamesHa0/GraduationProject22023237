<template>
    <div class="app-container">
        <!-- 顶部：当前轮次信息 -->
        <el-card class="round-info-card">
            <template #header>
                <div class="card-header">
                    <span>当前轮次信息</span>
                    <el-button type="primary" @click="loadAllData" icon="Refresh" size="small">刷新</el-button>
                </div>
            </template>
            <el-row :gutter="20">
                <el-col :span="4">
                    <el-statistic title="当前轮次">
                        <template #default>
                            <div>
                                <el-tag :type="getRoundType(currentRound)" size="large">
                                    {{ getRoundName(currentRound) }}
                                </el-tag>
                            </div>
                        </template>
                    </el-statistic>
                </el-col>
                <el-col :span="4">
                    <el-statistic title="当前阶段">
                        <template #default>
                            <div>
                                <el-tag :type="phaseTagType" size="large">
                                    {{ phaseText }}
                                </el-tag>
                            </div>
                        </template>
                    </el-statistic>
                </el-col>
                <el-col :span="16">
                    <div class="round-progress">
                        <el-steps :active="displayStep" finish-status="success" simple>
                            <el-step title="第一轮" />
                            <el-step title="第二轮" />
                            <el-step title="第三轮" />
                            <el-step title="补选" />
                        </el-steps>
                    </div>
                </el-col>
            </el-row>
        </el-card>

        <el-row :gutter="20" class="mt-20">
            <!-- 左侧：轮次管理操作 -->
            <el-col :span="8">
                <el-card class="operation-card">
                    <template #header>
                        <div class="card-header">
                            <span>轮次管理</span>
                        </div>
                    </template>

                    <!-- 推进操作 -->
                    <div class="section">
                        <div class="section-title">轮次推进</div>
                        <el-form label-width="0">
                            <el-form-item>
                                <el-button
                                    type="primary"
                                    @click="showAdvanceDialog"
                                    :loading="advanceLoading"
                                    :disabled="!canAdvance"
                                    style="width: 100%;"
                                    size="large"
                                >
                                    {{ advanceButtonText }}
                                </el-button>
                            </el-form-item>
                            <div class="action-desc">{{ advanceButtonDesc }}</div>
                        </el-form>
                    </div>

                    <el-divider />

                    <!-- 全局开关 -->
                    <div class="section">
                        <div class="section-title">全局设置</div>
                        <el-form label-width="0">
                            <el-form-item class="switch-item">
                                <div class="switch-label">开启额外轮次</div>
                                <el-switch
                                    v-model="extraRoundEnabled"
                                    active-text="是"
                                    inactive-text="否"
                                    @change="updateExtraRoundSwitch"
                                />
                            </el-form-item>
                            <div class="switch-desc">开启后，落选学生可参与额外轮次</div>
                        </el-form>
                    </div>

                    <el-divider />

                    <!-- 快捷操作 -->
                    <div class="section">
                        <div class="section-title">快捷操作</div>
                        <el-button
                            type="warning"
                            @click="handleAdvanceRejected"
                            :loading="advanceRejectedLoading"
                            :disabled="currentRound === 0"
                            style="width: 100%;"
                        >
                            推进被拒绝学生
                        </el-button>
                        <div class="action-desc">将所有被拒绝学生的志愿推进到下一轮</div>
                    </div>

                    <el-divider />

                    <!-- 重置按钮 -->
                    <div class="section">
                        <div class="section-title">危险操作</div>
                        <el-button
                            type="danger"
                            @click="showResetConfirm"
                            style="width: 100%;"
                        >
                            重置双选
                        </el-button>
                        <div class="action-desc danger">清空所有轮次时间，将轮次重置为未开始状态</div>
                    </div>
                </el-card>

                <!-- 未匹配学生统计 -->
                <el-card class="stats-card mt-20">
                    <el-statistic title="未匹配学生数" :value="statistics.unmatchedStudents || 0">
                        <template #suffix>
                            <el-button type="primary" link @click="goToRelationship" size="small">
                                前往手动分配
                            </el-button>
                        </template>
                    </el-statistic>
                </el-card>
            </el-col>

            <!-- 右侧：时间配置 -->
            <el-col :span="16">
                <el-card class="config-card">
                    <template #header>
                        <div class="card-header">
                            <span>时间配置</span>
                        </div>
                    </template>

                    <el-tabs v-model="activeTab" type="border-card">
                        <el-tab-pane label="第一轮" name="1">
                            <div class="tab-content">
                                <el-form label-width="130px">
                                    <el-form-item label="开始时间">
                                        <el-date-picker
                                            v-model="roundConfig.first_round_start"
                                            type="datetime"
                                            placeholder="选择开始时间"
                                            format="YYYY-MM-DD HH:mm:ss"
                                            value-format="YYYY-MM-DD HH:mm:ss"
                                            @change="updateConfig('first_round_start', roundConfig.first_round_start)"
                                        />
                                    </el-form-item>
                                    <el-form-item label="学生截止时间">
                                        <el-date-picker
                                            v-model="roundConfig.first_round_end_student"
                                            type="datetime"
                                            placeholder="选择截止时间"
                                            format="YYYY-MM-DD HH:mm:ss"
                                            value-format="YYYY-MM-DD HH:mm:ss"
                                            @change="updateConfig('first_round_end_student', roundConfig.first_round_end_student)"
                                        />
                                    </el-form-item>
                                    <el-form-item label="导师截止时间">
                                        <el-date-picker
                                            v-model="roundConfig.first_round_end_tutor"
                                            type="datetime"
                                            placeholder="选择截止时间"
                                            format="YYYY-MM-DD HH:mm:ss"
                                            value-format="YYYY-MM-DD HH:mm:ss"
                                            @change="updateConfig('first_round_end_tutor', roundConfig.first_round_end_tutor)"
                                        />
                                    </el-form-item>
                                </el-form>
                            </div>
                        </el-tab-pane>

                        <el-tab-pane label="第二轮" name="2">
                            <div class="tab-content">
                                <el-form label-width="130px">
                                    <el-form-item label="开始时间">
                                        <el-date-picker
                                            v-model="roundConfig.second_round_start"
                                            type="datetime"
                                            placeholder="选择开始时间"
                                            format="YYYY-MM-DD HH:mm:ss"
                                            value-format="YYYY-MM-DD HH:mm:ss"
                                            @change="updateConfig('second_round_start', roundConfig.second_round_start)"
                                        />
                                    </el-form-item>
                                    <el-form-item label="学生截止时间">
                                        <el-date-picker
                                            v-model="roundConfig.second_round_end_student"
                                            type="datetime"
                                            placeholder="选择截止时间"
                                            format="YYYY-MM-DD HH:mm:ss"
                                            value-format="YYYY-MM-DD HH:mm:ss"
                                            @change="updateConfig('second_round_end_student', roundConfig.second_round_end_student)"
                                        />
                                    </el-form-item>
                                    <el-form-item label="导师截止时间">
                                        <el-date-picker
                                            v-model="roundConfig.second_round_end_tutor"
                                            type="datetime"
                                            placeholder="选择截止时间"
                                            format="YYYY-MM-DD HH:mm:ss"
                                            value-format="YYYY-MM-DD HH:mm:ss"
                                            @change="updateConfig('second_round_end_tutor', roundConfig.second_round_end_tutor)"
                                        />
                                    </el-form-item>
                                </el-form>
                            </div>
                        </el-tab-pane>

                        <el-tab-pane label="第三轮" name="3">
                            <div class="tab-content">
                                <el-form label-width="130px">
                                    <el-form-item label="开始时间">
                                        <el-date-picker
                                            v-model="roundConfig.third_round_start"
                                            type="datetime"
                                            placeholder="选择开始时间"
                                            format="YYYY-MM-DD HH:mm:ss"
                                            value-format="YYYY-MM-DD HH:mm:ss"
                                            @change="updateConfig('third_round_start', roundConfig.third_round_start)"
                                        />
                                    </el-form-item>
                                    <el-form-item label="学生截止时间">
                                        <el-date-picker
                                            v-model="roundConfig.third_round_end_student"
                                            type="datetime"
                                            placeholder="选择截止时间"
                                            format="YYYY-MM-DD HH:mm:ss"
                                            value-format="YYYY-MM-DD HH:mm:ss"
                                            @change="updateConfig('third_round_end_student', roundConfig.third_round_end_student)"
                                        />
                                    </el-form-item>
                                    <el-form-item label="导师截止时间">
                                        <el-date-picker
                                            v-model="roundConfig.round_3_end_tutor"
                                            type="datetime"
                                            placeholder="选择截止时间"
                                            format="YYYY-MM-DD HH:mm:ss"
                                            value-format="YYYY-MM-DD HH:mm:ss"
                                            @change="updateConfig('round_3_end_tutor', roundConfig.round_3_end_tutor)"
                                        />
                                    </el-form-item>
                                </el-form>
                            </div>
                        </el-tab-pane>

                        <el-tab-pane label="补选阶段" name="4">
                            <div class="tab-content">
                                <el-form label-width="130px">
                                    <el-form-item label="开始时间">
                                        <el-date-picker
                                            v-model="roundConfig.supplementary_start"
                                            type="datetime"
                                            placeholder="选择开始时间"
                                            format="YYYY-MM-DD HH:mm:ss"
                                            value-format="YYYY-MM-DD HH:mm:ss"
                                            @change="updateConfig('supplementary_start', roundConfig.supplementary_start)"
                                        />
                                    </el-form-item>
                                    <el-form-item label="结束时间">
                                        <el-date-picker
                                            v-model="roundConfig.supplementary_end"
                                            type="datetime"
                                            placeholder="选择结束时间"
                                            format="YYYY-MM-DD HH:mm:ss"
                                            value-format="YYYY-MM-DD HH:mm:ss"
                                            @change="updateConfig('supplementary_end', roundConfig.supplementary_end)"
                                        />
                                    </el-form-item>
                                </el-form>
                            </div>
                        </el-tab-pane>
                    </el-tabs>
                </el-card>
            </el-col>
        </el-row>

        <!-- 底部：各轮次统计 -->
        <el-card class="mt-20">
            <template #header>
                <div class="card-header">
                    <span>各轮次统计</span>
                </div>
            </template>
            <el-row :gutter="20">
                <el-col :span="6" v-for="round in 4" :key="round">
                    <el-card :shadow="getActualRound(currentRound) === round ? 'hover' : 'never'" :class="{ 'current-round': getActualRound(currentRound) === round }">
                        <template #header>
                            <span>{{ getRoundName(round) }}</span>
                        </template>
                        <el-descriptions :column="1" border size="small">
                            <el-descriptions-item label="总志愿数">
                                {{ statistics[`round${round}`]?.total || 0 }}
                            </el-descriptions-item>
                            <el-descriptions-item label="待处理">
                                <el-tag type="warning">{{ statistics[`round${round}`]?.pending || 0 }}</el-tag>
                            </el-descriptions-item>
                            <el-descriptions-item label="已同意">
                                <el-tag type="success">{{ statistics[`round${round}`]?.accepted || 0 }}</el-tag>
                            </el-descriptions-item>
                            <el-descriptions-item label="已拒绝">
                                <el-tag type="danger">{{ statistics[`round${round}`]?.rejected || 0 }}</el-tag>
                            </el-descriptions-item>
                        </el-descriptions>
                    </el-card>
                </el-col>
            </el-row>
        </el-card>

        <!-- 推进对话框 -->
        <el-dialog
            v-model="advanceDialogVisible"
            :title="advanceDialogTitle"
            width="500px"
            :close-on-click-modal="false"
        >
            <el-form :model="advanceForm" :rules="advanceRules" ref="advanceFormRef" label-width="120px">
                <el-form-item label="当前轮次">
                    <el-tag :type="getRoundType(currentRound)">{{ getRoundName(currentRound) }}</el-tag>
                </el-form-item>
                <el-form-item label="目标轮次">
                    <el-tag :type="getRoundType(targetRoundForAdvance)" style="margin-left: 10px;">{{ getRoundName(targetRoundForAdvance) }}</el-tag>
                </el-form-item>
                <el-form-item label="开始时间">
                    <el-date-picker
                        v-model="advanceForm.startTime"
                        type="datetime"
                        disabled
                        format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%;"
                    />
                </el-form-item>
                <el-form-item v-if="targetRoundForAdvance !== 4" label="学生截止时间" prop="endTimeStudent">
                    <el-date-picker
                        v-model="advanceForm.endTimeStudent"
                        type="datetime"
                        placeholder="请选择学生截止时间"
                        format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%;"
                    />
                </el-form-item>
                <el-form-item v-if="targetRoundForAdvance !== 4" label="导师截止时间" prop="endTimeTutor">
                    <el-date-picker
                        v-model="advanceForm.endTimeTutor"
                        type="datetime"
                        placeholder="请选择导师截止时间"
                        format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%;"
                    />
                </el-form-item>
                <el-form-item v-if="targetRoundForAdvance === 4" label="结束时间" prop="endTimeStudent">
                    <el-date-picker
                        v-model="advanceForm.endTimeStudent"
                        type="datetime"
                        placeholder="请选择结束时间"
                        format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%;"
                    />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="advanceDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleAdvanceConfirm" :loading="advanceLoading">确认推进</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { getCurrentRound, getMaxRound, getRoundConfig, updateRoundConfig, advanceRejected, getRoundStatistics, advanceRound, resetRounds, getCurrentPhase } from "@/api/selection/round";
import { useRouter } from 'vue-router';

const { proxy } = getCurrentInstance();
const router = useRouter();

const currentRound = ref(1);
const currentPhase = ref(0);
const maxRound = ref(4);
const advanceLoading = ref(false);
const advanceRejectedLoading = ref(false);
const activeTab = ref('1');
const advanceDialogVisible = ref(false);
const advanceFormRef = ref(null);

const roundConfig = ref({
    current_round: '1',
    enable_extra_round: 'false',
    first_round_start: '',
    first_round_end_student: '',
    first_round_end_tutor: '',
    second_round_start: '',
    second_round_end_student: '',
    second_round_end_tutor: '',
    third_round_start: '',
    third_round_end_student: '',
    round_3_end_tutor: '',
    supplementary_start: '',
    supplementary_end: ''
});

const statistics = ref({});

const advanceForm = ref({
    startTime: '',
    endTimeStudent: '',
    endTimeTutor: ''
});

const advanceRules = {
    endTimeStudent: [{ required: true, message: '请选择截止时间', trigger: 'change' }],
    endTimeTutor: [{ required: true, message: '请选择导师截止时间', trigger: 'change' }]
};

const targetRoundForAdvance = computed(() => {
    if (currentRound.value === 0) return 1;
    if (isIntermediatePhase(currentRound.value)) {
        return getTargetRoundFromIntermediate(currentRound.value);
    }
    return currentRound.value + 1;
});

const canAdvance = computed(() => {
    const target = targetRoundForAdvance.value;
    return target <= maxRound.value && target <= 4;
});

const advanceButtonText = computed(() => {
    if (currentRound.value === 0) {
        return '开始第一轮';
    } else if (canAdvance.value) {
        return '推进到下一轮';
    } else {
        return '已是最后一轮';
    }
});

const advanceButtonDesc = computed(() => {
    if (currentRound.value === 0) {
        return '开始第一轮双选，设置第一轮截止时间';
    } else if (canAdvance.value) {
        return `从${getRoundName(currentRound.value)}推进到${getRoundName(targetRoundForAdvance.value)}`;
    } else {
        return '已达到最大轮次，无法继续推进';
    }
});

const advanceDialogTitle = computed(() => {
    if (currentRound.value === 0) {
        return '开始第一轮';
    }
    return `推进到${getRoundName(targetRoundForAdvance.value)}`;
});

// 用于进度条显示的步骤
const displayStep = computed(() => {
    const actual = getActualRound(currentRound.value);
    return actual > 0 ? actual - 1 : -1;
});

const extraRoundEnabled = computed({
    get: () => roundConfig.value.enable_extra_round === 'true',
    set: (val) => {
        roundConfig.value.enable_extra_round = val ? 'true' : 'false';
    }
});

const getRoundName = (round) => {
    if (round === 12) return '第一轮结束';
    if (round === 23) return '第二轮结束';
    if (round === 34) return '第三轮结束';
    const names = ['双选未开始', '第一轮', '第二轮', '第三轮', '补选阶段'];
    return names[round] || '';
};

const getRoundType = (round) => {
    if (round === 12 || round === 23 || round === 34) return 'warning';
    const types = ['info', 'primary', 'success', 'warning', 'danger'];
    return types[round] || 'info';
};

// 获取实际轮次（处理中间阶段值）
const getActualRound = (round) => {
    if (round === 12) return 1;
    if (round === 23) return 2;
    if (round === 34) return 3;
    return round;
};

// 判断是否为中间阶段
const isIntermediatePhase = (round) => {
    return round === 12 || round === 23 || round === 34;
};

// 从中间阶段值获取目标轮次
const getTargetRoundFromIntermediate = (round) => {
    if (round === 12) return 2;
    if (round === 23) return 3;
    if (round === 34) return 4;
    return round + 1;
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

const loadCurrentRound = () => {
    getCurrentRound().then(response => {
        let round = Number(response.data);
        if (isNaN(round)) {
            round = 1;
        }
        currentRound.value = round;
        if (round > 0 && round <= 4) {
            activeTab.value = String(round);
        }
    }).catch(() => {
        proxy.$modal.msgError('获取当前轮次失败');
    });
};

const loadCurrentPhase = () => {
    getCurrentPhase().then(response => {
        currentPhase.value = Number(response.data);
    }).catch(() => {
        console.error('获取当前阶段失败');
    });
};

const loadMaxRound = () => {
    getMaxRound().then(response => {
        maxRound.value = Number(response.data) || 4;
    }).catch(() => {
        maxRound.value = 4;
    });
};

const loadRoundConfig = () => {
    getRoundConfig().then(response => {
        roundConfig.value = { ...roundConfig.value, ...response.data };
    }).catch(() => {
        proxy.$modal.msgError('获取轮次配置失败');
    });
};

const loadStatistics = () => {
    getRoundStatistics().then(response => {
        statistics.value = response.data;
    }).catch(() => {
        proxy.$modal.msgError('获取统计数据失败');
    });
};

const loadAllData = () => {
    loadCurrentRound();
    loadCurrentPhase();
    loadMaxRound();
    loadRoundConfig();
    loadStatistics();
};

const showAdvanceDialog = () => {
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    advanceForm.value.startTime = now.toISOString().slice(0, 16).replace('T', ' ') + ':00';
    advanceForm.value.endTimeStudent = '';
    advanceForm.value.endTimeTutor = '';
    advanceDialogVisible.value = true;
};

const handleAdvanceConfirm = () => {
    advanceFormRef.value.validate(valid => {
        if (valid) {
            advanceLoading.value = true;
            advanceRound({
                endTimeStudent: advanceForm.value.endTimeStudent,
                endTimeTutor: advanceForm.value.endTimeTutor
            }).then(() => {
                proxy.$modal.msgSuccess('推进成功');
                advanceDialogVisible.value = false;
                loadAllData();
            }).catch(() => {
                proxy.$modal.msgError('推进失败');
            }).finally(() => {
                advanceLoading.value = false;
            });
        }
    });
};

const showResetConfirm = () => {
    proxy.$modal.confirm('确定要重置双选吗？此操作将清空所有轮次时间配置，并将轮次重置为未开始状态！', '警告', {
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        resetRounds().then(() => {
            proxy.$modal.msgSuccess('重置成功');
            loadAllData();
        }).catch(() => {
            proxy.$modal.msgError('重置失败');
        });
    }).catch(() => {});
};

const handleAdvanceRejected = () => {
    proxy.$modal.confirm('确定要将所有被拒绝学生的志愿推进到下一轮吗？').then(() => {
        advanceRejectedLoading.value = true;
        advanceRejected().then(response => {
            proxy.$modal.msgSuccess(response.msg || '推进成功');
            loadStatistics();
        }).catch(() => {
            proxy.$modal.msgError('推进失败');
        }).finally(() => {
            advanceRejectedLoading.value = false;
        });
    }).catch(() => {});
};

const updateConfig = (key, value) => {
    updateRoundConfig({ configKey: key, configValue: value }).then(() => {
        proxy.$modal.msgSuccess('配置更新成功');
    }).catch(() => {
        proxy.$modal.msgError('配置更新失败');
    });
};

const updateExtraRoundSwitch = () => {
    updateRoundConfig({
        configKey: 'enable_extra_round',
        configValue: roundConfig.value.enable_extra_round
    }).then(() => {
        proxy.$modal.msgSuccess('配置更新成功');
    }).catch(() => {
        proxy.$modal.msgError('配置更新失败');
    });
};

const goToRelationship = () => {
    router.push('/selection/relationship');
};

onMounted(() => {
    loadAllData();
});
</script>

<style scoped>
.round-info-card {
    margin-bottom: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.round-progress {
    padding: 20px 0;
}

.mt-20 {
    margin-top: 20px;
}

.current-round {
    border: 2px solid var(--el-color-primary);
}

.operation-card .section {
    padding: 8px 0;
}

.section-title {
    font-weight: 600;
    color: #303133;
    margin-bottom: 16px;
    font-size: 14px;
}

.switch-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
}

.switch-label {
    font-size: 14px;
    color: #606266;
}

.switch-desc {
    color: #909399;
    font-size: 12px;
    line-height: 1.5;
}

.action-desc {
    color: #909399;
    font-size: 12px;
    margin-top: 8px;
    line-height: 1.5;
}

.action-desc.danger {
    color: #F56C6C;
}

.tab-content {
    padding: 20px;
}

.stats-card :deep(.el-statistic__head) {
    margin-bottom: 8px;
}

.config-card :deep(.el-tabs--border-card) {
    border: none;
    box-shadow: none;
}

.config-card :deep(.el-tabs--border-card > .el-tabs__content) {
    padding: 0;
}
</style>
