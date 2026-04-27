<template>
    <div class="app-container">
        <!-- 顶部：当前轮次信息 -->
        <el-card class="round-info-card" shadow="hover">
            <template #header>
                <div class="card-header">
                    <span class="card-title">
                        <el-icon><Timer /></el-icon>
                        当前轮次信息
                    </span>
                    <el-button type="primary" @click="loadAllData" icon="Refresh" size="small" circle />
                </div>
            </template>
            <el-row :gutter="24" align="middle">
                <el-col :span="5">
                    <div class="stat-box">
                        <div class="stat-label">当前轮次</div>
                        <div class="stat-value">
                            <el-tag :type="getRoundType(currentRound)" size="large" effect="dark" round>
                                {{ getRoundName(currentRound) }}
                            </el-tag>
                        </div>
                    </div>
                </el-col>
                <el-col :span="5">
                    <div class="stat-box">
                        <div class="stat-label">当前阶段</div>
                        <div class="stat-value">
                            <el-tag :type="phaseTagType" size="large" effect="dark" round>
                                {{ phaseText }}
                            </el-tag>
                        </div>
                    </div>
                </el-col>
                <el-col :span="14">
                    <div class="progress-wrapper">
                        <el-steps :active="displayStep" finish-status="success" simple align-center>
                            <el-step title="学生选择" icon="UserFilled" />
                            <el-step :title="getStepLabel(1)" icon="DocumentChecked" />
                            <el-step :title="getStepLabel(2)" icon="DocumentChecked" />
                            <el-step :title="getStepLabel(3)" icon="DocumentChecked" />
                            <el-step title="补选学生选择" icon="UserFilled" />
                            <el-step title="补选导师选择" icon="DocumentChecked" />
                        </el-steps>
                    </div>
                </el-col>
            </el-row>
        </el-card>

        <el-row :gutter="20" class="mt-20">
            <!-- 左侧：轮次管理操作 -->
            <el-col :span="8">
                <el-card class="operation-card" shadow="hover">
                    <template #header>
                        <div class="card-header">
                            <span class="card-title">
                                <el-icon><Setting /></el-icon>
                                轮次管理
                            </span>
                        </div>
                    </template>

                    <!-- 推进操作 -->
                    <div class="section">
                        <div class="section-title">
                            <el-icon><Promotion /></el-icon>
                            轮次推进
                        </div>
                        <el-button
                            type="primary"
                            @click="showAdvanceDialog"
                            :loading="advanceLoading"
                            :disabled="!canAdvance"
                            style="width: 100%;"
                            size="large"
                            round
                        >
                            <el-icon v-if="!advanceLoading"><Right /></el-icon>
                            {{ advanceButtonText }}
                        </el-button>
                        <div class="action-desc">{{ advanceButtonDesc }}</div>
                    </div>

                    <el-divider />

                    <!-- 快捷操作 -->
                    <div class="section">
                        <div class="section-title">
                            <el-icon><Operation /></el-icon>
                            快捷操作
                        </div>
                        <el-button
                            type="warning"
                            @click="handleAdvanceRejected"
                            :loading="advanceRejectedLoading"
                            :disabled="currentRound === 0"
                            style="width: 100%;"
                            round
                        >
                            <el-icon><Sort /></el-icon>
                            推进被拒绝学生
                        </el-button>
                        <div class="action-desc">将所有被拒绝学生的志愿推进到下一轮</div>
                    </div>

                    <el-divider />

                    <!-- 重置按钮 -->
                    <div class="section danger-section">
                        <div class="section-title danger-title">
                            <el-icon><WarningFilled /></el-icon>
                            危险操作
                        </div>
                        <el-button
                            type="danger"
                            @click="showResetConfirm"
                            style="width: 100%;"
                            plain
                            round
                        >
                            <el-icon><RefreshLeft /></el-icon>
                            重置双选
                        </el-button>
                        <div class="action-desc danger">清空所有轮次时间，将轮次重置为未开始状态</div>
                    </div>
                </el-card>

                <!-- 未处理志愿统计 -->
                <el-card class="stats-card mt-20" shadow="hover">
                    <div class="stats-content">
                        <div class="stats-icon">
                            <el-icon :size="32"><Warning /></el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-number">{{ statistics.unmatchedStudents || 0 }}</div>
                            <div class="stats-label">未处理的学生志愿数</div>
                        </div>
                        <div class="stats-action">
                            <el-button type="primary" link @click="goToRelationship" size="small">
                                前往手动分配 →
                            </el-button>
                        </div>
                    </div>
                </el-card>
            </el-col>

            <!-- 右侧：时间信息 -->
            <el-col :span="16">
                <el-card class="config-card" shadow="hover">
                    <template #header>
                        <div class="card-header">
                            <span class="card-title">
                                <el-icon><Clock /></el-icon>
                                时间信息
                            </span>
                            <el-tag size="small" effect="plain">{{ getRoundName(getActualRound(currentRound)) }}</el-tag>
                        </div>
                    </template>

                    <el-tabs v-model="activeTab" type="border-card" stretch>
                        <el-tab-pane name="9">
                            <template #label>
                                <span class="tab-label" :class="{ 'is-active': activeTab === '9' }">
                                    <el-icon><UserFilled /></el-icon>
                                    学生选择
                                </span>
                            </template>
                            <div class="tab-content">
                                <div class="time-list">
                                    <div class="time-item">
                                        <div class="time-label"><el-icon><VideoPlay /></el-icon> 开始时间</div>
                                        <div class="time-value" :class="{ empty: !roundConfig.student_select_start }">
                                            {{ roundConfig.student_select_start || '未设置' }}
                                        </div>
                                    </div>
                                    <div class="time-item">
                                        <div class="time-label"><el-icon><VideoPause /></el-icon> 截止时间</div>
                                        <el-button 
                                            v-if="getActualRound(currentRound) === 9"
                                            type="primary" 
                                            link 
                                            @click="showEditDeadlineDialog(9)" 
                                            size="small"
                                        >
                                            <el-icon><Edit /></el-icon>
                                            重设
                                        </el-button>
                                        <div class="time-value" :class="{ empty: !roundConfig.student_select_end }">
                                            {{ roundConfig.student_select_end || '未设置' }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </el-tab-pane>

                        <el-tab-pane name="1">
                            <template #label>
                                <span class="tab-label" :class="{ 'is-active': activeTab === '1' }">
                                    <el-icon><DocumentChecked /></el-icon>
                                    {{ getTabLabel(1) }}
                                </span>
                            </template>
                            <div class="tab-content">
                                <div class="time-list">
                                    <div class="time-item">
                                        <div class="time-label"><el-icon><VideoPlay /></el-icon> 开始时间</div>
                                        <div class="time-value" :class="{ empty: !roundConfig.first_round_start }">
                                            {{ roundConfig.first_round_start || '未设置' }}
                                        </div>
                                    </div>
                                    <div class="time-item">
                                        <div class="time-label"><el-icon><VideoPause /></el-icon> 导师截止时间</div>
                                        <el-button 
                                            v-if="getActualRound(currentRound) === 1"
                                            type="primary" 
                                            link 
                                            @click="showEditDeadlineDialog(1)" 
                                            size="small"
                                        >
                                            <el-icon><Edit /></el-icon>
                                            重设
                                        </el-button>
                                        <div class="time-value" :class="{ empty: !roundConfig.first_round_end_tutor }">
                                            {{ roundConfig.first_round_end_tutor || '未设置' }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </el-tab-pane>

                        <el-tab-pane name="2">
                            <template #label>
                                <span class="tab-label" :class="{ 'is-active': activeTab === '2' }">
                                    <el-icon><DocumentChecked /></el-icon>
                                    {{ getTabLabel(2) }}
                                </span>
                            </template>
                            <div class="tab-content">
                                <div class="time-list">
                                    <div class="time-item">
                                        <div class="time-label"><el-icon><VideoPlay /></el-icon> 开始时间</div>
                                        <div class="time-value" :class="{ empty: !roundConfig.second_round_start }">
                                            {{ roundConfig.second_round_start || '未设置' }}
                                        </div>
                                    </div>
                                    <div class="time-item">
                                        <div class="time-label"><el-icon><VideoPause /></el-icon> 导师截止时间</div>
                                        <el-button 
                                            v-if="getActualRound(currentRound) === 2"
                                            type="primary" 
                                            link 
                                            @click="showEditDeadlineDialog(2)" 
                                            size="small"
                                        >
                                            <el-icon><Edit /></el-icon>
                                            重设
                                        </el-button>
                                        <div class="time-value" :class="{ empty: !roundConfig.second_round_end_tutor }">
                                            {{ roundConfig.second_round_end_tutor || '未设置' }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </el-tab-pane>

                        <el-tab-pane name="3">
                            <template #label>
                                <span class="tab-label" :class="{ 'is-active': activeTab === '3' }">
                                    <el-icon><DocumentChecked /></el-icon>
                                    {{ getTabLabel(3) }}
                                </span>
                            </template>
                            <div class="tab-content">
                                <div class="time-list">
                                    <div class="time-item">
                                        <div class="time-label"><el-icon><VideoPlay /></el-icon> 开始时间</div>
                                        <div class="time-value" :class="{ empty: !roundConfig.third_round_start }">
                                            {{ roundConfig.third_round_start || '未设置' }}
                                        </div>
                                    </div>
                                    <div class="time-item">
                                        <div class="time-label"><el-icon><VideoPause /></el-icon> 导师截止时间</div>
                                        <el-button 
                                            v-if="getActualRound(currentRound) === 3"
                                            type="primary" 
                                            link 
                                            @click="showEditDeadlineDialog(3)" 
                                            size="small"
                                        >
                                            <el-icon><Edit /></el-icon>
                                            重设
                                        </el-button>
                                        <div class="time-value" :class="{ empty: !roundConfig.third_round_end_tutor }">
                                            {{ roundConfig.third_round_end_tutor || '未设置' }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </el-tab-pane>


                        <el-tab-pane name="7">
                            <template #label>
                                <span class="tab-label" :class="{ 'is-active': activeTab === '7' }">
                                    <el-icon><UserFilled /></el-icon>
                                    补选学生选择
                                </span>
                            </template>
                            <div class="tab-content">
                                <div class="time-list">
                                    <div class="time-item">
                                        <div class="time-label"><el-icon><VideoPlay /></el-icon> 开始时间</div>
                                        <div class="time-value" :class="{ empty: !roundConfig.supplementary_student_start }">
                                            {{ roundConfig.supplementary_student_start || '未设置' }}
                                        </div>
                                    </div>
                                    <div class="time-item">
                                        <div class="time-label"><el-icon><VideoPause /></el-icon> 学生截止时间</div>
                                        <el-button
                                            v-if="getActualRound(currentRound) === 7"
                                            type="primary"
                                            link
                                            @click="showEditDeadlineDialog(7)"
                                            size="small"
                                        >
                                            <el-icon><Edit /></el-icon>
                                            重设
                                        </el-button>
                                        <div class="time-value" :class="{ empty: !roundConfig.supplementary_student_end }">
                                            {{ roundConfig.supplementary_student_end || '未设置' }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </el-tab-pane>

                        <el-tab-pane name="8">
                            <template #label>
                                <span class="tab-label" :class="{ 'is-active': activeTab === '8' }">
                                    <el-icon><DocumentChecked /></el-icon>
                                    补选导师选择
                                </span>
                            </template>
                            <div class="tab-content">
                                <div class="time-list">
                                    <div class="time-item">
                                        <div class="time-label"><el-icon><VideoPlay /></el-icon> 开始时间</div>
                                        <div class="time-value" :class="{ empty: !roundConfig.supplementary_tutor_start }">
                                            {{ roundConfig.supplementary_tutor_start || '未设置' }}
                                        </div>
                                    </div>
                                    <div class="time-item">
                                        <div class="time-label"><el-icon><VideoPause /></el-icon> 导师截止时间</div>
                                        <el-button
                                            v-if="getActualRound(currentRound) === 8"
                                            type="primary"
                                            link
                                            @click="showEditDeadlineDialog(8)"
                                            size="small"
                                        >
                                            <el-icon><Edit /></el-icon>
                                            重设
                                        </el-button>
                                        <div class="time-value" :class="{ empty: !roundConfig.supplementary_tutor_end }">
                                            {{ roundConfig.supplementary_tutor_end || '未设置' }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </el-tab-pane>
                    </el-tabs>

                    <div class="max-choices-info">
                        <el-icon><Setting /></el-icon>
                        <span class="info-label">学生可选最大志愿数：</span>
                        <el-tag type="primary" size="large" effect="dark">{{ roundConfig.student_max_choices || 3 }}</el-tag>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <!-- 底部：各轮次统计 -->
        <el-card class="mt-20 stats-overview" shadow="hover">
            <template #header>
                <div class="card-header">
                    <span class="card-title">
                        <el-icon><DataAnalysis /></el-icon>
                        各轮次统计
                    </span>
                </div>
            </template>
            <el-row :gutter="16">
                <el-col :span="6" v-for="round in [1, 2, 3, 8]" :key="round">
                    <div class="round-stats-card" :class="{ 'current-round': getActualRound(currentRound) === round }">
                        <div class="round-stats-header">
                            <span class="round-name">{{ getRoundName(round) }}</span>
                            <el-tag v-if="getActualRound(currentRound) === round" size="small" type="primary" effect="dark" round>当前</el-tag>
                        </div>
                        <div class="round-stats-body">
                            <div class="stats-grid">
                                <div class="stats-cell total">
                                    <div class="cell-value">{{ statistics[`round${round}`]?.total || 0 }}</div>
                                    <div class="cell-label">总志愿</div>
                                </div>
                                <div class="stats-cell pending">
                                    <div class="cell-value">{{ statistics[`round${round}`]?.pending || 0 }}</div>
                                    <div class="cell-label">待处理</div>
                                </div>
                                <div class="stats-cell accepted">
                                    <div class="cell-value">{{ statistics[`round${round}`]?.accepted || 0 }}</div>
                                    <div class="cell-label">已同意</div>
                                </div>
                                <div class="stats-cell rejected">
                                    <div class="cell-value">{{ statistics[`round${round}`]?.rejected || 0 }}</div>
                                    <div class="cell-label">已拒绝</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </el-col>
            </el-row>
        </el-card>

        <!-- 推进对话框 -->
        <el-dialog
            v-model="advanceDialogVisible"
            :title="advanceDialogTitle"
            width="500px"
            :close-on-click-modal="false"
            destroy-on-close
        >
            <div class="dialog-advance-info">
                <div class="info-row">
                    <span class="info-label">当前轮次</span>
                    <el-tag :type="getRoundType(currentRound)" size="large">{{ getRoundName(currentRound) }}</el-tag>
                    <el-icon class="arrow"><Right /></el-icon>
                    <el-tag :type="getRoundType(targetRoundForAdvance)" size="large">{{ getRoundName(targetRoundForAdvance) }}</el-tag>
                </div>
            </div>
            <el-form :model="advanceForm" :rules="advanceRules" ref="advanceFormRef" label-width="120px">
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
                <!-- 学生选择轮：只有学生截止时间 -->
                <el-form-item v-if="targetRoundForAdvance === 9" label="学生截止时间" prop="endTimeStudent">
                    <el-date-picker
                        v-model="advanceForm.endTimeStudent"
                        type="datetime"
                        placeholder="请选择学生截止时间"
                        format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%;"
                    />
                </el-form-item>
                <!-- 从学生选择推进到第一轮：需要设置第一轮的导师截止时间 -->
                <el-form-item v-if="currentRound === 9 && targetRoundForAdvance === 1" label="导师截止时间" prop="endTimeTutor">
                    <el-date-picker
                        v-model="advanceForm.endTimeTutor"
                        type="datetime"
                        placeholder="请选择第一轮导师截止时间"
                        format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%;"
                    />
                </el-form-item>
                <!-- 导师轮(1/2/3)：只有导师截止时间（排除从学生选择轮推进到第一轮的情况，因为上面已经有了） -->
                <el-form-item v-if="targetRoundForAdvance >= 1 && targetRoundForAdvance <= 3 && !(currentRound === 9 && targetRoundForAdvance === 1)" label="导师截止时间" prop="endTimeTutor">
                    <el-date-picker
                        v-model="advanceForm.endTimeTutor"
                        type="datetime"
                        placeholder="请选择导师截止时间"
                        format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%;"
                    />
                </el-form-item>
                <!-- 补选学生选择轮：只有学生截止时间 -->
                <el-form-item v-if="isLastRegularRound" label="补选学生截止时间" prop="endTimeStudent">
                    <el-date-picker
                        v-model="advanceForm.endTimeStudent"
                        type="datetime"
                        placeholder="请选择补选学生截止时间"
                        format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%;"
                    />
                </el-form-item>
                <!-- 补选学生选择轮（7）推进到补选导师选择轮（8）：只有导师截止时间 -->
                <el-form-item v-if="targetRoundForAdvance === 78 || targetRoundForAdvance === 8" label="导师截止时间" prop="endTimeTutor">
                    <el-date-picker
                        v-model="advanceForm.endTimeTutor"
                        type="datetime"
                        placeholder="请选择补选导师截止时间"
                        format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%;"
                    />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="advanceDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleAdvanceConfirm" :loading="advanceLoading" round>
                    <el-icon><Check /></el-icon>
                    {{ isLastRegularRound ? '确认开启补选' : '确认推进' }}
                </el-button>
            </template>
        </el-dialog>

        <!-- 重置对话框 -->
        <el-dialog
            v-model="resetDialogVisible"
            title="重置双选"
            width="450px"
            :close-on-click-modal="false"
            destroy-on-close
        >
            <el-alert type="warning" :closable="false" show-icon class="reset-warning">
                <template #title>
                    <span>此操作将清空所有轮次时间配置，并将轮次重置为未开始状态！</span>
                </template>
            </el-alert>
            <el-form :model="resetForm" :rules="resetRules" ref="resetFormRef" label-width="140px" class="reset-form">
                <el-form-item label="学生最大志愿数" prop="maxChoices">
                    <el-input-number
                        v-model="resetForm.maxChoices"
                        :min="1"
                        :max="3"
                        :step="1"
                        style="width: 150px;"
                    />
                    <span class="form-tip">（范围：1-3）</span>
                </el-form-item>
                <el-form-item label="归属年级" prop="cohortYear">
                    <el-select v-model="resetForm.cohortYear" placeholder="请选择归属年级" style="width: 200px;" clearable>
                        <el-option label="全部年级" value="" />
                        <el-option
                            v-for="year in cohortYears"
                            :key="year"
                            :label="year + '级'"
                            :value="String(year)"
                        />
                    </el-select>
                    <span class="form-tip">（选择后，本次双选仅该年级学生可参与）</span>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="resetDialogVisible = false">取消</el-button>
                <el-button type="danger" @click="handleResetConfirm" :loading="resetLoading" round>
                    <el-icon><RefreshLeft /></el-icon>
                    确认重置
                </el-button>
            </template>
        </el-dialog>

        <!-- 重设截止时间对话框 -->
        <el-dialog
            v-model="editDeadlineDialogVisible"
            title="重设截止时间"
            width="450px"
            :close-on-click-modal="false"
            destroy-on-close
        >
            <el-form :model="editDeadlineForm" :rules="editDeadlineRules" ref="editDeadlineFormRef" label-width="100px">
                <el-form-item label="当前轮次">
                    <el-tag :type="getRoundType(editDeadlineForm.round)" size="large">
                        {{ getRoundName(editDeadlineForm.round) }}
                    </el-tag>
                </el-form-item>
                <el-form-item label="截止时间" prop="deadline">
                    <el-date-picker
                        v-model="editDeadlineForm.deadline"
                        type="datetime"
                        placeholder="请选择截止时间"
                        format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%;"
                    />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="editDeadlineDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleEditDeadlineConfirm" :loading="editDeadlineLoading" round>
                    确认修改
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { getCurrentRound, getRoundConfig, updateRoundConfig, advanceRejected, getRoundStatistics, advanceRound, resetRounds, getCurrentPhase, startSupplementaryRound } from "@/api/selection/round";
import { listCohortYears } from "@/api/student/info";
import { useRouter } from 'vue-router';
import { Timer, Setting, Promotion, Operation, WarningFilled, RefreshLeft, Right, UserFilled, DocumentChecked, Clock, VideoPlay, VideoPause, Warning, DataAnalysis, Check, Sort, Edit } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();
const router = useRouter();

const currentRound = ref(1);
const currentPhase = ref(0);
const advanceLoading = ref(false);
const advanceRejectedLoading = ref(false);
const activeTab = ref('1');
const advanceDialogVisible = ref(false);
const advanceFormRef = ref(null);
const resetDialogVisible = ref(false);
const resetFormRef = ref(null);
const resetLoading = ref(false);

const editDeadlineDialogVisible = ref(false);
const editDeadlineFormRef = ref(null);
const editDeadlineLoading = ref(false);
const editDeadlineForm = ref({
    round: 0,
    deadline: ''
});

const editDeadlineRules = {
    deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }]
};

const cohortYears = ref([]);

const resetForm = ref({
    maxChoices: 3,
    cohortYear: ''
});

const resetRules = {
    maxChoices: [
        { required: true, message: '请设置学生最大志愿数', trigger: 'change' },
        { type: 'number', min: 1, max: 3, message: '范围：1-3', trigger: 'change' }
    ]
};

const roundConfig = ref({
    current_round: '0',
    enable_extra_round: 'false',
    student_max_choices: '3',
    student_select_start: '',
    student_select_end: '',
    first_round_start: '',
    first_round_end_tutor: '',
    second_round_start: '',
    second_round_end_tutor: '',
    third_round_start: '',
    third_round_end_tutor: ''
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
    if (currentRound.value === 0) return 9;
    if (isIntermediatePhase(currentRound.value)) {
        return getTargetRoundFromIntermediate(currentRound.value);
    }
    if (currentRound.value === 9) return 1;
    // 如果是最大常规轮次，返回7（补选学生选择轮）
    if (isLastRegularRound.value) {
        return 7;
    }
    // 如果是补选学生选择轮（7），直接推进到补选导师选择轮（8）
    if (currentRound.value === 7) return 8;
    return currentRound.value + 1;
});

const canAdvance = computed(() => {
    // 如果已经是补选导师选择轮（8），不能再推进
    if (currentRound.value === 8) {
        return false;
    }
    // 如果是最后一轮常规轮次，允许开启补选
    if (isLastRegularRound.value) {
        return true;
    }
    // 如果当前已经是中间阶段，允许推进
    if (isIntermediatePhase(currentRound.value)) {
        return true;
    }
    const target = targetRoundForAdvance.value;
    const maxRound = parseInt(roundConfig.value.student_max_choices) || 3;
    return target === 9 || target === 1 || (target >= 1 && target <= maxRound) || target === 7 || target === 8;
});

const isLastRegularRound = computed(() => {
    const maxRound = parseInt(roundConfig.value.student_max_choices) || 3;
    return currentRound.value === maxRound;
});

const advanceButtonText = computed(() => {
    if (currentRound.value === 0) {
        return '开始学生选择';
    } else if (currentRound.value === 9) {
        return '开始第一轮';
    } else if (isLastRegularRound.value) {
        return '开启补选';
    } else if (canAdvance.value) {
        return '推进到下一轮';
    } else {
        return '已是最后一轮';
    }
});

const advanceButtonDesc = computed(() => {
    if (currentRound.value === 0) {
        return '开始学生选择阶段，设置学生截止时间';
    } else if (currentRound.value === 9) {
        return '结束学生选择，开始第一轮导师选择';
    } else if (isLastRegularRound.value) {
        return '开启补选阶段，将标记未匹配学生进入补选';
    } else if (canAdvance.value) {
        return `从${getRoundName(currentRound.value)}推进到${getRoundName(targetRoundForAdvance.value)}`;
    } else {
        return '已达到最大轮次，无法继续推进';
    }
});

const advanceDialogTitle = computed(() => {
    if (currentRound.value === 0) {
        return '开始学生选择';
    }
    if (currentRound.value === 9 && targetRoundForAdvance.value === 1) {
        return '从学生选择轮推进到第一轮';
    }
    return `推进到${getRoundName(targetRoundForAdvance.value)}`;
});

const displayStep = computed(() => {
    const round = currentRound.value;
    if (round === 0) return -1;
    if (round === 9) return 0;
    if (round === 91) return 0;
    if (round >= 1 && round <= 3) return round;
    if (round === 7 || round === 78) return 4;
    if (round === 8) return 5;
    return -1;
});

const getRoundName = (round) => {
    const maxRound = parseInt(roundConfig.value.student_max_choices) || 3;
    if (round === 91) return '学生选择结束';
    if (round === 12) return '第一轮结束';
    if (round === 23) return maxRound < 2 ? '第二轮结束(未启用)' : '第二轮结束';
    if (round === 34) return maxRound < 3 ? '第三轮结束(未启用)' : '第三轮结束';
    if (round === 78) return '补选学生选择结束';
    if (round === 7) return '补选学生选择轮';
    if (round === 8) return '补选导师选择轮';
    const names = [
        '双选未开始',
        '第一轮',
        maxRound < 2 ? '第二轮(未启用)' : '第二轮',
        maxRound < 3 ? '第三轮(未启用)' : '第三轮'
    ];
    if (round === 9) return '学生选择轮';
    return names[round] || '';
};

const getTabLabel = (round) => {
    const maxRound = parseInt(roundConfig.value.student_max_choices) || 3;
    if (round === 1) return '第一轮';
    if (round === 2) return maxRound < 2 ? '第二轮(未启用)' : '第二轮';
    if (round === 3) return maxRound < 3 ? '第三轮(未启用)' : '第三轮';
    if (round === 7) return '补选学生选择';
    if (round === 8) return '补选导师选择';
    return '';
};

const getStepLabel = (round) => {
    const maxRound = parseInt(roundConfig.value.student_max_choices) || 3;
    if (round === 1) return '第一轮';
    if (round === 2) return maxRound < 2 ? '第二轮(未启用)' : '第二轮';
    if (round === 3) return maxRound < 3 ? '第三轮(未启用)' : '第三轮';
    return '';
};

const getRoundType = (round) => {
    if (round === 91 || round === 12 || round === 23 || round === 34 || round === 78) return 'warning';
    if (round === 9 || round === 7) return 'primary';
    const types = ['info', 'primary', 'success', 'warning', 'danger', 'danger', 'danger', 'primary', 'success'];
    return types[round] || 'info';
};

const getActualRound = (round) => {
    if (round === 91) return 9;
    if (round === 12) return 1;
    if (round === 23) return 2;
    if (round === 34) return 3;
    if (round === 78) return 7;
    return round;
};

const isIntermediatePhase = (round) => {
    return round === 91 || round === 12 || round === 23 || round === 34 || round === 78;
};

const getTargetRoundFromIntermediate = (round) => {
    if (round === 91) return 1;
    if (round === 12) return 2;
    if (round === 23) return 3;
    if (round === 34) return 7;
    if (round === 78) return 8;
    return round + 1;
};

const phaseText = computed(() => {
    const texts = ['未开始', '学生选择中', '导师确认中', '请等待推进', '已结束'];
    return texts[currentPhase.value] || '';
});

const phaseTagType = computed(() => {
    const types = ['info', 'success', 'primary', 'warning', 'danger'];
    return types[currentPhase.value] || 'info';
});

const loadCurrentRound = () => {
    getCurrentRound().then(response => {
        let round = Number(response.data);
        if (isNaN(round)) {
            round = 0;
        }
        currentRound.value = round;
        if (round === 9 || round === 91) {
            activeTab.value = '9';
        } else if (round >= 1 && round <= 3) {
            activeTab.value = String(round);
        } else if (round === 7 || round === 78) {
            activeTab.value = '7';
        } else if (round === 8) {
            activeTab.value = '8';
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
    loadRoundConfig();
    loadStatistics();
    loadCohortYears();
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
            const startTime = advanceForm.value.startTime;
            const endTime = advanceForm.value.endTimeStudent || advanceForm.value.endTimeTutor;

            if (endTime && startTime) {
                const start = new Date(startTime.replace(/-/g, '/'));
                const end = new Date(endTime.replace(/-/g, '/'));

                if (end <= start) {
                    proxy.$modal.msgError('截止时间必须晚于开始时间');
                    return;
                }
            }

            // 如果是最后一轮常规轮次，调用开启补选接口
            if (isLastRegularRound.value) {
                handleStartSupplementary();
            } else {
                // 否则调用普通推进接口
                handleNormalAdvance();
            }
        }
    });
};

const handleNormalAdvance = () => {
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
};

const handleStartSupplementary = () => {
    proxy.$modal.confirm('确定要开启补选吗？开启后，所有未确认导师关系的学生将被标记为需要补选，进入补选阶段。').then(() => {
        advanceLoading.value = true;
        startSupplementaryRound({
            endTimeStudent: advanceForm.value.endTimeStudent,
            endTimeTutor: advanceForm.value.endTimeTutor
        }).then(() => {
            proxy.$modal.msgSuccess('开启补选成功');
            advanceDialogVisible.value = false;
            loadAllData();
        }).catch(() => {
            proxy.$modal.msgError('开启补选失败');
        }).finally(() => {
            advanceLoading.value = false;
        });
    }).catch(() => {});
};

const loadCohortYears = () => {
    listCohortYears().then(res => {
        cohortYears.value = res.data || [];
    }).catch(() => {
        console.error('获取归属年级列表失败');
    });
};

const showResetConfirm = () => {
    resetForm.value.maxChoices = parseInt(roundConfig.value.student_max_choices) || 3;
    resetForm.value.cohortYear = roundConfig.value.selection_cohort_year || '';
    resetDialogVisible.value = true;
};

const handleResetConfirm = () => {
    resetFormRef.value.validate(valid => {
        if (valid) {
            resetLoading.value = true;
            resetRounds({
                maxChoices: resetForm.value.maxChoices,
                cohortYear: resetForm.value.cohortYear
            }).then(() => {
                proxy.$modal.msgSuccess('重置成功');
                resetDialogVisible.value = false;
                loadAllData();
            }).catch(() => {
                proxy.$modal.msgError('重置失败');
            }).finally(() => {
                resetLoading.value = false;
            });
        }
    });
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

const goToRelationship = () => {
    router.push({ path: '/selection/relationship', query: { onlyUndetermined: 'true' } });
};

const getDeadlineConfigKey = (round) => {
    const keyMap = {
        9: 'student_select_end',
        1: 'first_round_end_tutor',
        2: 'second_round_end_tutor',
        3: 'third_round_end_tutor',
        7: 'supplementary_student_end',
        8: 'supplementary_tutor_end'
    };
    return keyMap[round] || '';
};

const showEditDeadlineDialog = (round) => {
    editDeadlineForm.value.round = round;
    const configKey = getDeadlineConfigKey(round);
    editDeadlineForm.value.deadline = roundConfig.value[configKey] || '';
    editDeadlineDialogVisible.value = true;
};

const handleEditDeadlineConfirm = () => {
    editDeadlineFormRef.value.validate(valid => {
        if (valid) {
            editDeadlineLoading.value = true;
            const configKey = getDeadlineConfigKey(editDeadlineForm.value.round);
            updateRoundConfig({
                configKey: configKey,
                configValue: editDeadlineForm.value.deadline
            }).then(() => {
                proxy.$modal.msgSuccess('截止时间修改成功');
                editDeadlineDialogVisible.value = false;
                loadAllData();
            }).catch(() => {
                proxy.$modal.msgError('修改失败');
            }).finally(() => {
                editDeadlineLoading.value = false;
            });
        }
    });
};

onMounted(() => {
    loadAllData();
});
</script>

<style scoped>
/* 卡片标题 */
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.card-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    display: flex;
    align-items: center;
    gap: 6px;
}

/* 顶部轮次信息 */
.round-info-card {
    margin-bottom: 20px;
}

.stat-box {
    text-align: center;
    padding: 10px;
}

.stat-label {
    font-size: 13px;
    color: #909399;
    margin-bottom: 10px;
}

.stat-value {
    min-height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.progress-wrapper {
    padding: 10px 0;
}

.progress-wrapper :deep(.el-steps--simple) {
    background: transparent;
}

/* 间距 */
.mt-20 {
    margin-top: 20px;
}

/* 操作卡片 */
.operation-card .section {
    padding: 12px 0;
}

.section-title {
    font-weight: 600;
    color: #303133;
    margin-bottom: 14px;
    font-size: 14px;
    display: flex;
    align-items: center;
    gap: 6px;
}

.danger-section {
    background: linear-gradient(135deg, #fef0f0 0%, #fff5f5 100%);
    border-radius: 8px;
    padding: 12px !important;
    margin: 0 -20px;
    width: calc(100% + 40px);
}

.danger-title {
    color: #F56C6C !important;
}

.switch-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    border-bottom: 1px solid #f2f6fc;
}

.switch-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.switch-label {
    font-size: 14px;
    color: #303133;
    font-weight: 500;
}

.switch-desc {
    color: #909399;
    font-size: 12px;
}

.action-desc {
    color: #909399;
    font-size: 12px;
    margin-top: 10px;
    line-height: 1.5;
    text-align: center;
}

.action-desc.danger {
    color: #F56C6C;
}

/* 未匹配统计 */
.stats-card {
    border-left: 4px solid var(--el-color-warning);
}

.stats-content {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 8px 0;
}

.stats-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--el-color-warning);
}

.stats-info {
    flex: 1;
}

.stats-number {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
    line-height: 1.2;
}

.stats-label {
    font-size: 13px;
    color: #909399;
}

/* 时间配置 */
.config-card :deep(.el-tabs--border-card) {
    border: none;
    box-shadow: none;
}

.config-card :deep(.el-tabs__header) {
    margin-bottom: 8px;
}

.config-card :deep(.el-tabs--border-card > .el-tabs__content) {
    padding: 0;
}

.config-card :deep(.el-card__body) {
    padding: 12px 16px 16px;
}

.max-choices-info {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    margin-top: 12px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
    border-radius: 8px;
    border: 1px solid #e4e7ed;
}

.max-choices-info .el-icon {
    color: #409eff;
    font-size: 18px;
}

.max-choices-info .info-label {
    font-size: 14px;
    color: #606266;
    font-weight: 500;
}

.tab-label {
    display: flex;
    align-items: center;
    gap: 4px;
    opacity: 0.7;
    transition: all 0.3s;
}

.tab-label.is-active,
.tab-label:hover {
    opacity: 1;
}

.tab-content {
    padding: 16px;
}

.time-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.time-item {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    background: #f8f9fa;
    border-radius: 6px;
    transition: all 0.3s;
}

.time-item:hover {
    background: #f0f2f5;
}

.time-item .el-button {
    flex-shrink: 0;
    margin-left: 4px;
}

.time-label {
    width: 100px;
    font-size: 13px;
    color: #606266;
    display: flex;
    align-items: center;
    gap: 4px;
    font-weight: 500;
}

.time-value {
    flex: 1;
    font-family: 'Courier New', monospace;
    font-size: 14px;
    color: #303133;
    font-weight: 500;
    text-align: right;
}

.time-value.empty {
    color: #c0c4cc;
    font-style: italic;
}

/* 统计概览 */
.stats-overview :deep(.el-card__body) {
    padding: 20px;
}

.round-stats-card {
    border: 1px solid #e4e7ed;
    border-radius: 10px;
    overflow: hidden;
    transition: all 0.3s;
}

.round-stats-card.current-round {
    border-color: var(--el-color-primary);
    box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.2), 0 4px 12px rgba(64, 158, 255, 0.1);
}

.round-stats-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: linear-gradient(135deg, #f5f7fa 0%, #eef1f5 100%);
    border-bottom: 1px solid #e4e7ed;
}

.round-stats-card.current-round .round-stats-header {
    background: linear-gradient(135deg, #e6f1fc 0%, #d9ebff 100%);
    border-bottom-color: var(--el-color-primary-light-5);
}

.round-name {
    font-weight: 600;
    font-size: 14px;
    color: #303133;
}

.round-stats-body {
    padding: 16px;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
}

.stats-cell {
    text-align: center;
    padding: 10px 8px;
    border-radius: 8px;
    background: #fafbfc;
}

.cell-value {
    font-size: 22px;
    font-weight: 700;
    line-height: 1.2;
}

.cell-label {
    font-size: 11px;
    color: #909399;
    margin-top: 4px;
}

.stats-cell.total .cell-value {
    color: #409eff;
}

.stats-cell.pending .cell-value {
    color: #e6a23c;
}

.stats-cell.accepted .cell-value {
    color: #67c23a;
}

.stats-cell.rejected .cell-value {
    color: #f56c6c;
}

/* 对话框 */
.dialog-advance-info {
    background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
    border-radius: 10px;
    padding: 16px 20px;
    margin-bottom: 20px;
}

.info-row {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 15px;
}

.info-label {
    color: #606266;
    font-weight: 500;
}

.arrow {
    color: #909399;
}

/* 重置对话框 */
.reset-warning {
    margin-bottom: 20px;
}

.reset-form {
    padding: 10px 0;
}

.reset-form .form-tip {
    margin-left: 10px;
    color: #909399;
    font-size: 13px;
}
</style>
