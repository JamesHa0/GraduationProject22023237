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
                    <el-form label-width="150px">
                        <el-form-item label="第一轮截止时间">
                            <el-date-picker
                                v-model="roundConfig.round_1_end_tutor"
                                type="datetime"
                                placeholder="选择截止时间"
                                format="YYYY-MM-DD HH:mm:ss"
                                value-format="YYYY-MM-DD HH:mm:ss"
                                @change="updateConfig('round_1_end_tutor', roundConfig.round_1_end_tutor)"
                            />
                        </el-form-item>
                        <el-form-item label="第二轮截止时间">
                            <el-date-picker
                                v-model="roundConfig.round_2_end_tutor"
                                type="datetime"
                                placeholder="选择截止时间"
                                format="YYYY-MM-DD HH:mm:ss"
                                value-format="YYYY-MM-DD HH:mm:ss"
                                @change="updateConfig('round_2_end_tutor', roundConfig.round_2_end_tutor)"
                            />
                        </el-form-item>
                        <el-form-item label="第三轮截止时间">
                            <el-date-picker
                                v-model="roundConfig.round_3_end_tutor"
                                type="datetime"
                                placeholder="选择截止时间"
                                format="YYYY-MM-DD HH:mm:ss"
                                value-format="YYYY-MM-DD HH:mm:ss"
                                @change="updateConfig('round_3_end_tutor', roundConfig.round_3_end_tutor)"
                            />
                        </el-form-item>
                        <el-form-item label="补选开始时间">
                            <el-date-picker
                                v-model="roundConfig.supplementary_start"
                                type="datetime"
                                placeholder="选择开始时间"
                                format="YYYY-MM-DD HH:mm:ss"
                                value-format="YYYY-MM-DD HH:mm:ss"
                                @change="updateConfig('supplementary_start', roundConfig.supplementary_start)"
                            />
                        </el-form-item>
                        <el-form-item label="补选结束时间">
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
                    <el-button type="primary" link @click="goToManualAssign">
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
    round_1_end_tutor: '',
    round_2_end_tutor: '',
    round_3_end_tutor: '',
    supplementary_start: '',
    supplementary_end: ''
});

const statistics = ref({});

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

const goToManualAssign = () => {
    router.push('/selection/manual-assign');
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
</style>
