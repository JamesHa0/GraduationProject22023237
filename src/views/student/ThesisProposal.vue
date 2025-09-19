<template>
    <div class="thesis-proposal-container">
        <div class="card">
            <h3 class="card-title">毕业论文开题</h3>

            <div class="proposal-status mb-4">
                <div class="status-label">当前状态：</div>
                <div class="status-indicator" :class="proposalStatusClass">
                    <font-awesome-icon :icon="statusIcon" class="status-icon" />
                    <span class="status-text">{{ proposalStatusText }}</span>
                </div>
            </div>

            <form @submit.prevent="submitProposal">
                <div class="form-grid">
                    <div class="form-row">
                        <div class="form-item">
                            <label>论文题目 <span class="required">*</span></label>
                            <input type="text" v-model="proposal.title" class="form-control" placeholder="请输入论文题目"
                                :disabled="isSubmitted" required />
                        </div>

                        <div class="form-item">
                            <label>研究方向 <span class="required">*</span></label>
                            <input type="text" v-model="proposal.researchDirection" class="form-control"
                                placeholder="请输入研究方向" :disabled="isSubmitted" required />
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-item">
                            <label>指导教师 <span class="required">*</span></label>
                            <input type="text" v-model="proposal.advisor" class="form-control" readonly />
                        </div>

                        <div class="form-item">
                            <label>开题时间</label>
                            <input type="date" v-model="proposal.proposalDate" class="form-control"
                                :disabled="isSubmitted" />
                        </div>
                    </div>

                    <div class="form-item">
                        <label>论文摘要 <span class="required">*</span></label>
                        <textarea v-model="proposal.abstract" class="form-control textarea" rows="5"
                            placeholder="请输入论文摘要（简要介绍研究背景、目的、方法和预期成果）" :disabled="isSubmitted" required></textarea>
                    </div>

                    <div class="form-item">
                        <label>研究内容与计划 <span class="required">*</span></label>
                        <textarea v-model="proposal.researchContent" class="form-control textarea" rows="8"
                            placeholder="请详细描述研究内容、研究方法、技术路线和进度安排" :disabled="isSubmitted" required></textarea>
                    </div>

                    <div class="form-item">
                        <label>参考文献</label>
                        <textarea v-model="proposal.references" class="form-control textarea" rows="5"
                            placeholder="请列出主要参考文献（格式：作者. 标题. 出版信息）" :disabled="isSubmitted"></textarea>
                        <div class="form-hint">示例：张三, 李四. 计算机科学研究. 北京: 高等教育出版社, 2023.</div>
                    </div>

                    <div class="form-item">
                        <label>开题报告上传 <span class="required">*</span></label>
                        <div class="file-upload-area" :class="{ 'disabled': isSubmitted }">
                            <input type="file" id="proposalFile" class="file-input" accept=".pdf,.doc,.docx"
                                :disabled="isSubmitted" @change="handleFileUpload" />
                            <label for="proposalFile" class="file-label" :disabled="isSubmitted">
                                <font-awesome-icon icon="upload" class="upload-icon" />
                                <span v-if="!proposal.fileName">点击上传开题报告文件（PDF、Word格式）</span>
                                <span v-if="proposal.fileName" class="file-name">{{ proposal.fileName }}</span>
                            </label>
                        </div>
                        <div class="form-hint">文件大小不超过10MB，支持PDF、Word格式</div>
                    </div>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn submit-btn" :disabled="isSubmitted || !proposal.fileName">
                        <font-awesome-icon icon="paper-plane" /> 提交开题报告
                    </button>

                    <button type="button" class="btn draft-btn" :disabled="isSubmitted" @click="saveAsDraft">
                        <font-awesome-icon icon="save" /> 保存草稿
                    </button>
                </div>
            </form>
        </div>

        <!-- 审核意见卡片 -->
        <div class="card mt-4" v-if="hasReview">
            <h3 class="card-title">审核意见</h3>

            <div class="review-info">
                <div class="review-item">
                    <span class="review-label">审核结果：</span>
                    <span class="review-value" :class="reviewResultClass">{{ reviewResultText }}</span>
                </div>

                <div class="review-item">
                    <span class="review-label">审核人：</span>
                    <span class="review-value">{{ proposal.reviewer }}</span>
                </div>

                <div class="review-item">
                    <span class="review-label">审核时间：</span>
                    <span class="review-value">{{ proposal.reviewDate }}</span>
                </div>

                <div class="review-item full-width">
                    <span class="review-label">审核意见：</span>
                    <div class="review-comment">{{ proposal.reviewComment }}</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useUserStore } from '../../stores/userStore';
import { useAppStore } from '../../stores/appStore';

// 状态管理
const userStore = useUserStore();
const appStore = useAppStore();

// 开题报告数据
const proposal = ref({
    title: '',
    researchDirection: '',
    advisor: '',
    proposalDate: '',
    abstract: '',
    researchContent: '',
    references: '',
    fileName: '',
    file: null,
    status: 'draft', // draft: 草稿, submitted: 已提交, approved: 已通过, rejected: 已驳回
    reviewer: '',
    reviewDate: '',
    reviewComment: ''
});

// 页面加载时初始化数据
onMounted(() => {
    // 从用户信息中获取指导教师
    proposal.value.advisor = userStore.studentInfo?.advisor || '张教授';

    // 模拟从服务器加载数据
    loadProposalData();
});

// 模拟加载开题报告数据
const loadProposalData = () => {
    // 实际应用中这里会从API获取数据
    // 这里模拟一些已保存的数据
    proposal.value.title = '基于深度学习的图像识别算法研究';
    proposal.value.researchDirection = '计算机视觉';
    proposal.value.proposalDate = '2024-10-15';
    proposal.value.abstract = '本研究旨在探索深度学习在图像识别领域的应用，重点研究卷积神经网络的改进算法，以提高复杂场景下的图像识别准确率。预期成果将为相关领域的研究提供新的思路和方法。';
    proposal.value.researchContent = '1. 研究背景与意义：随着人工智能技术的发展，图像识别在多个领域得到广泛应用...\n2. 研究内容：\n   (1) 分析现有卷积神经网络模型的优缺点\n   (2) 提出一种改进的网络结构...\n3. 研究计划：\n   第1-2个月：文献调研和理论学习\n   第3-4个月：模型设计与实现...';
    proposal.value.references = '1. Krizhevsky, A., Sutskever, I., & Hinton, G. E. (2012). ImageNet classification with deep convolutional neural networks.\n2. 王某某, 李某某. 深度学习在计算机视觉中的应用. 计算机学报, 2022.';

    // 模拟已提交状态
    // proposal.value.status = 'submitted';
    // proposal.value.fileName = '开题报告_张三.pdf';

    // 模拟已审核状态
    // proposal.value.status = 'approved';
    // proposal.value.fileName = '开题报告_张三.pdf';
    // proposal.value.reviewer = '李教授';
    // proposal.value.reviewDate = '2024-10-20';
    // proposal.value.reviewComment = '选题具有一定的研究价值，研究计划合理，同意开题。建议在研究内容中补充与现有算法的对比分析。';
};

// 计算属性 - 是否已提交
const isSubmitted = computed(() => {
    return ['submitted', 'approved', 'rejected'].includes(proposal.value.status);
});

// 计算属性 - 是否有审核意见
const hasReview = computed(() => {
    return ['approved', 'rejected'].includes(proposal.value.status);
});

// 计算属性 - 开题状态文本
const proposalStatusText = computed(() => {
    const statusMap = {
        draft: '草稿',
        submitted: '已提交，等待审核',
        approved: '已通过',
        rejected: '已驳回'
    };
    return statusMap[proposal.value.status] || '草稿';
});

// 计算属性 - 开题状态样式
const proposalStatusClass = computed(() => {
    const classMap = {
        draft: 'status-draft',
        submitted: 'status-pending',
        approved: 'status-active',
        rejected: 'status-rejected'
    };
    return `status-tag ${classMap[proposal.value.status] || 'status-draft'}`;
});

// 计算属性 - 状态图标
const statusIcon = computed(() => {
    const iconMap = {
        draft: 'pen-to-square',
        submitted: 'clock',
        approved: 'check',
        rejected: 'xmark'
    };
    return iconMap[proposal.value.status] || 'pen-to-square';
});

// 计算属性 - 审核结果文本
const reviewResultText = computed(() => {
    return proposal.value.status === 'approved' ? '通过' : '未通过';
});

// 计算属性 - 审核结果样式
const reviewResultClass = computed(() => {
    return proposal.value.status === 'approved' ? 'status-active' : 'status-rejected';
});

// 处理文件上传
const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
        // 简单验证文件大小
        if (file.size > 10 * 1024 * 1024) {
            appStore.addNotification('文件大小不能超过10MB', 'warning');
            return;
        }

        proposal.value.file = file;
        proposal.value.fileName = file.name;
    }
};

// 提交开题报告
const submitProposal = () => {
    // 模拟API提交
    appStore.addNotification('正在提交开题报告...', 'info');

    setTimeout(() => {
        proposal.value.status = 'submitted';
        appStore.addNotification('开题报告提交成功，等待审核', 'success');
    }, 1000);
};

// 保存为草稿
const saveAsDraft = () => {
    // 模拟保存到服务器
    appStore.addNotification('正在保存草稿...', 'info');

    setTimeout(() => {
        appStore.addNotification('草稿保存成功', 'success');
    }, 800);
};
</script>

<style scoped>
.thesis-proposal-container {
    animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(10px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.proposal-status {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 15px;
    background-color: #f5f7fa;
    border-radius: 4px;
}

.dark-mode .proposal-status {
    background-color: #333355;
}

.status-label {
    font-weight: 500;
}

.status-indicator {
    display: flex;
    align-items: center;
    gap: 6px;
}

.status-icon {
    font-size: 16px;
}

.status-draft {
    background-color: #f0f2f5;
    color: #86909c;
}

.status-rejected {
    background-color: #fff1f0;
    color: #f5222d;
}

.dark-mode .status-rejected {
    background-color: #5c1e23;
    color: #ff7875;
}

.textarea {
    resize: vertical;
}

.form-hint {
    margin-top: 6px;
    font-size: 12px;
    color: #86909c;
    line-height: 1.5;
}

.file-upload-area {
    position: relative;
}

.file-input {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    opacity: 0;
    cursor: pointer;
    z-index: 1;
}

.file-label {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    width: 100%;
    padding: 12px;
    border: 2px dashed #ddd;
    border-radius: 4px;
    background-color: #fafafa;
    color: #666;
    cursor: pointer;
    transition: all 0.2s ease;
}

.dark-mode .file-label {
    border-color: #444;
    background-color: #2a2a40;
    color: #bbb;
}

.file-upload-area.disabled .file-label {
    opacity: 0.7;
    cursor: not-allowed;
    background-color: #f5f5f5;
}

.dark-mode .file-upload-area.disabled .file-label {
    background-color: #333;
}

.upload-icon {
    font-size: 18px;
}

.file-name {
    color: #1890ff;
    word-break: break-all;
}

.form-actions {
    margin-top: 30px;
    display: flex;
    gap: 15px;
    justify-content: flex-end;
}

.submit-btn {
    background-color: #52c41a;
    color: white;
}

.draft-btn {
    background-color: #faad14;
    color: white;
}

.review-info {
    margin-top: 10px;
}

.review-item {
    display: flex;
    margin-bottom: 16px;
    align-items: flex-start;
}

.review-item.full-width {
    flex-direction: column;
}

.review-label {
    min-width: 100px;
    font-weight: 500;
    flex-shrink: 0;
}

.review-value {
    flex: 1;
}

.review-comment {
    margin-top: 6px;
    padding: 12px;
    background-color: #f5f7fa;
    border-radius: 4px;
    line-height: 1.6;
}

.dark-mode .review-comment {
    background-color: #333355;
}

.required {
    color: #f5222d;
}

.mt-4 {
    margin-top: 16px;
}

.mb-4 {
    margin-bottom: 16px;
}

@media (max-width: 768px) {
    .proposal-status {
        flex-direction: column;
        align-items: flex-start;
    }

    .form-actions {
        flex-direction: column;
    }

    .form-actions .btn {
        width: 100%;
    }
}
</style>