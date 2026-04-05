<template>
    <div class="app-container">
        <el-card class="round-info-card">
            <template #header>
                <div class="card-header">
                    <span>当前轮次信息</span>
                </div>
            </template>
            <el-row :gutter="20">
                <el-col :span="8">
                    <el-statistic title="当前轮次">
                        <template #default>
                            <el-tag :type="getRoundType(currentRound)" size="large">
                                {{ getRoundName(currentRound) }}
                            </el-tag>
                        </template>
                    </el-statistic>
                </el-col>
                <el-col :span="16">
                    <div class="round-progress">
                        <el-steps :active="currentRound - 1" finish-status="success" simple>
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
            <el-col :span="12">
                <el-card>
                    <template #header>
                        <div class="card-header">
                            <span>轮次切换</span>
                        </div>
                    </template>
                    <el-form label-width="120px">
                        <el-form-item label="切换到">
                            <el-radio-group v-model="targetRound">
                                <el-radio :label="1">第一轮</el-radio>
                                <el-radio :label="2">第二轮</el-radio>
                                <el-radio :label="3">第三轮</el-radio>
                                <el-radio :label="4">补选阶段</el-radio>
                            </el-radio-group>
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="handleSwitchRound" :loading="switchLoading">
                                确认切换
                            </el-button>
                            <el-button type="warning" @click="handleAdvanceRejected" :loading="advanceLoading">
                                推进被拒绝学生
                            </el-button>
                        </el-form-item>
                    </el-form>
                </el-card>
            </el-col>
            <el-col :span="12">
                <el-card>
                    <template #header>
                        <div class="card-header">
                            <span>时间配置</span>
                        </div>
                    </template>
                    <el-form label-width="130px">
                        <el-form-item label="开启额外轮次">
                            <el-switch
                                v-model="extraRoundEnabled"
                                active-text="是"
                                inactive-text="否"
                                @change="updateExtraRoundSwitch"
                            />
                            <div style="color: #909399; font-size: 12px; margin-top: 4px;">
                                开启后，落选学生可参与额外轮次
                            </div>
                        </el-form-item>

                        <el-divider />

                        <div class="round-section">
                            <div class="round-title">第一轮</div>
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
                        </div>

                        <el-divider />

                        <div class="round-section">
                            <div class="round-title">第二轮</div>
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
                        </div>

                        <el-divider />

                        <div class="round-section">
                            <div class="round-title">第三轮</div>
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
                        </div>

                        <el-divider />

                        <div class="round-section">
                            <div class="round-title">补选阶段</div>
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
                        </div>
                    </el-form>
                </el-card>
            </el-col>
        </el-row>

        <el-card class="mt-20">
            <template #header>
                <div class="card-header">
                    <span>各轮次统计</span>
                    <el-button type="primary" @click="loadStatistics" icon="Refresh">刷新</el-button>
                </div>
            </template>
            <el-row :gutter="20">
                <el-col :span="6" v-for="round in 4" :key="round">
                    <el-card :shadow="currentRound === round ? 'hover' : 'never'" :class="{ 'current-round': currentRound === round }">
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
            <el-divider />
            <el-statistic title="未匹配学生数" :value="statistics.unmatchedStudents || 0">
                <template #suffix>
                    <el-button type="primary" link @click="goToRelationship">
                        前往手动分配
                    </el-button>
                </template>
            </el-statistic>
        </el-card>
    </div>
</template>

<script setup>
import { getCurrentRound, switchRound, getRoundConfig, updateRoundConfig, advanceRejected, getRoundStatistics } from "@/api/selection/round";
import { useRouter } from 'vue-router';

const { proxy } = getCurrentInstance();
const router = useRouter();

const currentRound = ref(1);
const targetRound = ref(1);
const switchLoading = ref(false);
const advanceLoading = ref(false);

const roundConfig = ref({
    current_round: '1',
    enable_extra_round: 'false',
    // 第一轮
    first_round_start: '',
    first_round_end_student: '',
    first_round_end_tutor: '',
    // 第二轮
    second_round_start: '',
    second_round_end_student: '',
    second_round_end_tutor: '',
    // 第三轮
    third_round_start: '',
    third_round_end_student: '',
    round_3_end_tutor: '',
    // 补选
    supplementary_start: '',
    supplementary_end: ''
});

const statistics = ref({});

// 计算属性：处理布尔值转换
const extraRoundEnabled = computed({
    get: () => roundConfig.value.enable_extra_round === 'true',
    set: (val) => {
        roundConfig.value.enable_extra_round = val ? 'true' : 'false';
    }
});

const getRoundName = (round) => {
    const names = ['', '第一轮', '第二轮', '第三轮', '补选阶段'];
    return names[round] || '';
};

const getRoundType = (round) => {
    const types = ['', 'primary', 'success', 'warning', 'danger'];
    return types[round] || 'info';
};

const loadCurrentRound = () => {
    getCurrentRound().then(response => {
        currentRound.value = response.data;
        targetRound.value = response.data;
    }).catch(() => {
        proxy.$modal.msgError('获取当前轮次失败');
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

const handleSwitchRound = () => {
    if (targetRound.value === currentRound.value) {
        proxy.$modal.msgWarning('已经是当前轮次，无需切换');
        return;
    }
    proxy.$modal.confirm(`确定要切换到${getRoundName(targetRound.value)}吗？`).then(() => {
        switchLoading.value = true;
        switchRound({ targetRound: targetRound.value }).then(() => {
            proxy.$modal.msgSuccess('切换成功');
            loadCurrentRound();
            loadStatistics();
        }).catch(() => {
            proxy.$modal.msgError('切换失败');
        }).finally(() => {
            switchLoading.value = false;
        });
    }).catch(() => {});
};

const handleAdvanceRejected = () => {
    proxy.$modal.confirm('确定要将所有被拒绝学生的志愿推进到下一轮吗？').then(() => {
        advanceLoading.value = true;
        advanceRejected().then(response => {
            proxy.$modal.msgSuccess(response.msg || '推进成功');
            loadStatistics();
        }).catch(() => {
            proxy.$modal.msgError('推进失败');
        }).finally(() => {
            advanceLoading.value = false;
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
    loadCurrentRound();
    loadRoundConfig();
    loadStatistics();
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

.round-section {
    margin-bottom: 16px;
}

.round-title {
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
    font-size: 14px;
}
</style>
