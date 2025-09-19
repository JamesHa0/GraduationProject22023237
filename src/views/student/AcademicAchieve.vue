<template>
    <div class="academic-achieve-container">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">学术成果管理</h3>
                <button class="btn add-btn" @click="showAddModal = true">
                    <font-awesome-icon icon="plus" /> 添加学术成果
                </button>
            </div>

            <div class="filter-bar">
                <select v-model="filterType" class="form-control filter-select" @change="filterAchievements">
                    <option value="">全部类型</option>
                    <option value="paper">期刊/会议论文</option>
                    <option value="patent">专利</option>
                    <option value="award">获奖成果</option>
                    <option value="project">科研项目</option>
                </select>
            </div>

            <div class="achievements-list">
                <div class="achievement-item" v-for="(item, index) in filteredAchievements" :key="item.id"
                    :class="{ 'even-item': index % 2 === 0 }">
                    <div class="achievement-header">
                        <div class="achievement-title">
                            {{ item.title }}
                        </div>
                        <div class="achievement-type">
                            <span class="status-tag" :class="getTypeClass(item.type)">{{ getTypeName(item.type)
                            }}</span>
                        </div>
                    </div>

                    <div class="achievement-details">
                        <div class="detail-item">
                            <span class="detail-label">发表/获得时间：</span>
                            <span class="detail-value">{{ item.date }}</span>
                        </div>

                        <div class="detail-item">
                            <span class="detail-label">作者/参与者：</span>
                            <span class="detail-value">{{ item.authors }}</span>
                        </div>

                        <div class="detail-item full-width">
                            <span class="detail-label">描述：</span>
                            <span class="detail-value">{{ item.description }}</span>
                        </div>

                        <div class="detail-item" v-if="item.journal || item.conference">
                            <span class="detail-label">发表于：</span>
                            <span class="detail-value">
                                {{ item.journal || item.conference }}
                                <template v-if="item.impactFactor">(影响因子: {{ item.impactFactor }})</template>
                            </span>
                        </div>

                        <div class="detail-item" v-if="item.certNumber">
                            <span class="detail-label">证书编号：</span>
                            <span class="detail-value">{{ item.certNumber }}</span>
                        </div>

                        <div class="detail-item" v-if="item.fileUrl">
                            <span class="detail-label">相关文件：</span>
                            <a href="#" class="file-link" @click.prevent="downloadFile(item)">{{ item.fileName }}</a>
                        </div>
                    </div>

                    <div class="achievement-actions">
                        <button class="btn edit-btn" @click="editAchievement(item)">
                            <font-awesome-icon icon="edit" /> 编辑
                        </button>
                        <button class="btn delete-btn" @click="confirmDelete(item.id)">
                            <font-awesome-icon icon="trash" /> 删除
                        </button>
                    </div>
                </div>

                <div class="no-data" v-if="filteredAchievements.length === 0">
                    <font-awesome-icon icon="file-circle-question" size="3x" class="no-data-icon" />
                    <p>暂无学术成果数据</p>
                    <p class="hint-text">点击"添加学术成果"按钮开始记录您的学术成果</p>
                </div>
            </div>
        </div>

        <!-- 添加/编辑学术成果模态框 -->
        <div class="modal-backdrop" v-if="showAddModal || showEditModal">
            <div class="modal">
                <div class="modal-header">
                    <h3 class="modal-title">{{ showEditModal ? '编辑学术成果' : '添加学术成果' }}</h3>
                    <button class="close-btn" @click="closeModal">
                        <font-awesome-icon icon="xmark" />
                    </button>
                </div>

                <div class="modal-body">
                    <form @submit.prevent="saveAchievement">
                        <div class="form-grid">
                            <div class="form-item">
                                <label>成果标题 <span class="required">*</span></label>
                                <input type="text" v-model="currentAchievement.title" class="form-control" required />
                            </div>

                            <div class="form-item">
                                <label>成果类型 <span class="required">*</span></label>
                                <select v-model="currentAchievement.type" class="form-control"
                                    @change="handleTypeChange" required>
                                    <option value="">请选择成果类型</option>
                                    <option value="paper">期刊/会议论文</option>
                                    <option value="patent">专利</option>
                                    <option value="award">获奖成果</option>
                                    <option value="project">科研项目</option>
                                </select>
                            </div>

                            <div class="form-item">
                                <label>发表/获得时间 <span class="required">*</span></label>
                                <input type="date" v-model="currentAchievement.date" class="form-control" required />
                            </div>

                            <div class="form-item">
                                <label>作者/参与者 <span class="required">*</span></label>
                                <input type="text" v-model="currentAchievement.authors" class="form-control"
                                    placeholder="请输入所有作者/参与者，用逗号分隔" required />
                            </div>

                            <div class="form-item" v-if="currentAchievement.type === 'paper'">
                                <label>期刊/会议名称</label>
                                <input type="text" v-model="publicationName" class="form-control"
                                    placeholder="请输入发表的期刊或会议名称" @input="updatePublicationName" />
                            </div>

                            <div class="form-item" v-if="currentAchievement.type === 'paper'">
                                <label>影响因子 (期刊论文)</label>
                                <input type="number" v-model="currentAchievement.impactFactor" class="form-control"
                                    step="0.01" min="0" />
                            </div>

                            <div class="form-item"
                                v-if="currentAchievement.type === 'patent' || currentAchievement.type === 'award'">
                                <label>证书编号</label>
                                <input type="text" v-model="currentAchievement.certNumber" class="form-control" />
                            </div>

                            <div class="form-item">
                                <label>成果描述 <span class="required">*</span></label>
                                <textarea v-model="currentAchievement.description" class="form-control textarea"
                                    rows="4" required></textarea>
                            </div>

                            <div class="form-item">
                                <label>上传相关文件</label>
                                <div class="file-upload-area">
                                    <input type="file" id="achievementFile" class="file-input"
                                        accept=".pdf,.doc,.docx,.jpg,.png" @change="handleFileUpload" />
                                    <label for="achievementFile" class="file-label">
                                        <font-awesome-icon icon="upload" class="upload-icon" />
                                        <span v-if="!currentAchievement.fileName">点击上传相关文件（论文PDF、证书扫描件等）</span>
                                        <span v-if="currentAchievement.fileName" class="file-name">{{
                                            currentAchievement.fileName }}</span>
                                    </label>
                                </div>
                                <div class="form-hint">支持PDF、Word、图片格式，文件大小不超过10MB</div>
                            </div>
                        </div>

                        <div class="modal-footer">
                            <button type="button" class="btn cancel-btn" @click="closeModal">取消</button>
                            <button type="submit" class="btn confirm-btn">保存</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <!-- 删除确认模态框 -->
        <div class="modal-backdrop" v-if="showDeleteConfirm">
            <div class="modal small-modal">
                <div class="modal-header">
                    <h3 class="modal-title">确认删除</h3>
                    <button class="close-btn" @click="showDeleteConfirm = false">
                        <font-awesome-icon icon="xmark" />
                    </button>
                </div>

                <div class="modal-body">
                    <p class="delete-message">
                        您确定要删除这条学术成果记录吗？此操作不可撤销。
                    </p>
                </div>

                <div class="modal-footer">
                    <button class="btn cancel-btn" @click="showDeleteConfirm = false">取消</button>
                    <button class="btn delete-confirm-btn" @click="deleteAchievement">确认删除</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useAppStore } from '../../stores/appStore';

const publicationName = computed({
    get() {
        return currentAchievement.value.journal || currentAchievement.value.conference || '';
    },
    set(value) {
        if (currentAchievement.value.type === 'paper') {
            if (value.includes('会议')) {
                currentAchievement.value.conference = value;
                currentAchievement.value.journal = '';
            } else {
                currentAchievement.value.journal = value;
                currentAchievement.value.conference = '';
            }
        }
    }
});

// 状态管理
const appStore = useAppStore();

// 学术成果数据
const achievements = ref([
    {
        id: 1,
        title: "基于深度学习的图像识别算法研究",
        type: "paper",
        date: "2023-06-15",
        authors: "张三, 李四, 王五",
        journal: "计算机学报",
        impactFactor: 3.5,
        description: "本文提出了一种改进的卷积神经网络模型，在多个公开数据集上的实验结果表明，该模型的识别准确率优于现有主流算法。",
        fileUrl: "/files/paper1.pdf",
        fileName: "基于深度学习的图像识别算法研究.pdf"
    },
    {
        id: 2,
        title: "一种新型智能推荐系统",
        type: "patent",
        date: "2023-11-20",
        authors: "张三, 赵六",
        certNumber: "ZL2023XXXXXXXXX",
        description: "本专利涉及一种基于用户行为分析的智能推荐系统，能够根据用户的历史行为和偏好，实时推荐个性化内容。",
        fileUrl: "/files/patent1.pdf",
        fileName: "专利证书扫描件.pdf"
    },
    {
        id: 3,
        title: "全国大学生人工智能创新大赛一等奖",
        type: "award",
        date: "2024-05-10",
        authors: "张三, 李四, 王五, 赵六",
        certNumber: "2024AIComp-001",
        description: "团队开发的智能辅助诊断系统在大赛中获得评委一致好评，荣获一等奖。该系统能够辅助医生进行疾病诊断，提高诊断效率。",
        fileUrl: "/files/award1.jpg",
        fileName: "获奖证书.jpg"
    }
]);

// 筛选条件
const filterType = ref('');

// 过滤后的学术成果
const filteredAchievements = computed(() => {
    if (!filterType.value) {
        return achievements.value;
    }
    return achievements.value.filter(item => item.type === filterType.value);
});

// 模态框控制
const showAddModal = ref(false);
const showEditModal = ref(false);
const showDeleteConfirm = ref(false);
const deleteId = ref(null);

// 当前编辑的学术成果
const currentAchievement = ref({
    id: null,
    title: '',
    type: '',
    date: '',
    authors: '',
    journal: '',
    conference: '',
    impactFactor: '',
    certNumber: '',
    description: '',
    fileUrl: '',
    fileName: '',
    file: null
});

// 打开添加模态框
const openAddModal = () => {
    resetCurrentAchievement();
    showAddModal.value = true;
    showEditModal.value = false;
};

// 编辑学术成果
const editAchievement = (item) => {
    // 复制对象，避免直接修改原数据
    currentAchievement.value = { ...item };
    showEditModal.value = true;
    showAddModal.value = false;
};

// 关闭模态框
const closeModal = () => {
    showAddModal.value = false;
    showEditModal.value = false;
    resetCurrentAchievement();
};

// 重置当前学术成果对象
const resetCurrentAchievement = () => {
    currentAchievement.value = {
        id: null,
        title: '',
        type: '',
        date: '',
        authors: '',
        journal: '',
        conference: '',
        impactFactor: '',
        certNumber: '',
        description: '',
        fileUrl: '',
        fileName: '',
        file: null
    };
};

// 处理成果类型变化
const handleTypeChange = () => {
    // 清空与类型相关的字段
    currentAchievement.value.journal = '';
    currentAchievement.value.conference = '';
    currentAchievement.value.impactFactor = '';
    currentAchievement.value.certNumber = '';
};

// 更新期刊/会议名称
const updatePublicationName = (e) => {
    if (currentAchievement.value.type === 'paper') {
        // 简单判断是期刊还是会议（实际应用中可能需要更复杂的逻辑）
        if (e.target.value.includes('会议')) {
            currentAchievement.value.conference = e.target.value;
            currentAchievement.value.journal = '';
        } else {
            currentAchievement.value.journal = e.target.value;
            currentAchievement.value.conference = '';
        }
    }
};

// 处理文件上传
const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
        if (file.size > 10 * 1024 * 1024) {
            appStore.addNotification('文件大小不能超过10MB', 'warning');
            return;
        }

        currentAchievement.value.file = file;
        currentAchievement.value.fileName = file.name;
    }
};

// 保存学术成果
const saveAchievement = () => {
    if (showEditModal.value) {
        // 更新现有成果
        const index = achievements.value.findIndex(item => item.id === currentAchievement.value.id);
        if (index !== -1) {
            achievements.value[index] = { ...currentAchievement.value };
            appStore.addNotification('学术成果更新成功', 'success');
        }
    } else {
        // 添加新成果
        const newId = Math.max(...achievements.value.map(item => item.id), 0) + 1;
        currentAchievement.value.id = newId;
        // 模拟文件上传后的URL
        currentAchievement.value.fileUrl = `/files/achievement-${newId}.pdf`;
        achievements.value.push({ ...currentAchievement.value });
        appStore.addNotification('学术成果添加成功', 'success');
    }

    closeModal();
};

// 确认删除
const confirmDelete = (id) => {
    deleteId.value = id;
    showDeleteConfirm.value = true;
};

// 删除学术成果
const deleteAchievement = () => {
    if (deleteId.value) {
        achievements.value = achievements.value.filter(item => item.id !== deleteId.value);
        appStore.addNotification('学术成果已删除', 'success');
    }

    showDeleteConfirm.value = false;
    deleteId.value = null;
};

// 下载文件
const downloadFile = (item) => {
    appStore.addNotification(`正在下载: ${item.fileName}`, 'info');
    // 实际应用中这里会触发文件下载
};

// 根据类型获取名称
const getTypeName = (type) => {
    const typeMap = {
        'paper': '期刊/会议论文',
        'patent': '专利',
        'award': '获奖成果',
        'project': '科研项目'
    };
    return typeMap[type] || type;
};

// 根据类型获取样式类
const getTypeClass = (type) => {
    const classMap = {
        'paper': 'status-active',
        'patent': 'status-pending',
        'award': 'status-completed',
        'project': 'status-info'
    };
    return classMap[type] || '';
};

// 筛选成果
const filterAchievements = () => {
    // 由computed属性自动处理
};
</script>

<style scoped>
.academic-achieve-container {
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

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.add-btn {
    background-color: #52c41a;
    color: white;
}

.filter-bar {
    margin-bottom: 20px;
}

.filter-select {
    width: 200px;
}

.achievements-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.achievement-item {
    border: 1px solid #eee;
    border-radius: 8px;
    padding: 16px;
    transition: all 0.2s ease;
}

.dark-mode .achievement-item {
    border-color: #444;
}

.achievement-item:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
}

.even-item {
    background-color: #fafafa;
}

.dark-mode .even-item {
    background-color: #2a2a40;
}

.achievement-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;
}

.achievement-title {
    font-weight: 600;
    font-size: 16px;
    flex: 1;
}

.achievement-details {
    margin-bottom: 12px;
}

.detail-item {
    display: flex;
    margin-bottom: 8px;
    font-size: 14px;
}

.detail-item.full-width {
    flex-direction: column;
}

.detail-label {
    min-width: 120px;
    color: #666;
    flex-shrink: 0;
}

.dark-mode .detail-label {
    color: #bbb;
}

.detail-value {
    flex: 1;
    line-height: 1.5;
}

.file-link {
    color: #1890ff;
    text-decoration: none;
}

.file-link:hover {
    text-decoration: underline;
}

.achievement-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 10px;
}

.edit-btn {
    background-color: #faad14;
    color: white;
    padding: 4px 8px;
    font-size: 12px;
}

.delete-btn {
    background-color: #f5222d;
    color: white;
    padding: 4px 8px;
    font-size: 12px;
}

.no-data {
    text-align: center;
    padding: 60px 20px;
    color: #86909c;
}

.no-data-icon {
    margin-bottom: 16px;
    opacity: 0.6;
}

.hint-text {
    font-size: 14px;
    margin-top: 8px;
}

.textarea {
    resize: vertical;
}

.form-hint {
    margin-top: 6px;
    font-size: 12px;
    color: #86909c;
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
}

.dark-mode .file-label {
    border-color: #444;
    background-color: #333;
    color: #bbb;
}

.file-name {
    color: #1890ff;
    word-break: break-all;
}

.upload-icon {
    font-size: 18px;
}

.small-modal {
    max-width: 400px;
}

.delete-message {
    text-align: center;
    padding: 20px 0;
    line-height: 1.6;
}

.delete-confirm-btn {
    background-color: #f5222d;
    color: white;
}

.cancel-btn {
    background-color: #f5f7fa;
    color: #4e5969;
}

.dark-mode .cancel-btn {
    background-color: #333355;
    color: #bbb;
}

.required {
    color: #f5222d;
}

.status-info {
    background-color: #e6f7ff;
    color: #1890ff;
}

.dark-mode .status-info {
    background-color: #1a365d;
    color: #66b1ff;
}

@media (max-width: 768px) {
    .card-header {
        flex-direction: column;
        align-items: stretch;
        gap: 10px;
    }

    .add-btn {
        width: 100%;
    }

    .detail-item {
        flex-direction: column;
    }

    .detail-label {
        margin-bottom: 4px;
    }

    .achievement-actions {
        flex-wrap: wrap;
    }

    .achievement-actions .btn {
        flex: 1;
        min-width: 100px;
    }
}
</style>