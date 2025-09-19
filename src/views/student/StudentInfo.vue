<template>
    <div class="student-info-container">
        <!-- 个人基本信息卡片 -->
        <div class="card">
            <h3 class="card-title">个人基本信息</h3>
            <div class="form-grid">
                <div class="form-row">
                    <div class="form-item">
                        <label>学号</label>
                        <input type="text" v-model="studentInfo.studentId" class="form-control" readonly />
                    </div>
                    <div class="form-item">
                        <label>姓名</label>
                        <input type="text" v-model="studentInfo.name" class="form-control" />
                    </div>
                    <div class="form-item">
                        <label>性别</label>
                        <select v-model="studentInfo.gender" class="form-control">
                            <option value="男">男</option>
                            <option value="女">女</option>
                        </select>
                    </div>
                    <div class="form-item">
                        <label>出生日期</label>
                        <input type="date" v-model="studentInfo.birthDate" class="form-control" />
                    </div>
                </div>

                <!-- 更多表单行省略 -->
            </div>

            <div class="form-actions">
                <button class="btn save-btn" @click="saveStudentInfo">
                    <i class="fas fa-save"></i> 保存修改
                </button>
            </div>
        </div>

        <!-- 学习经历卡片 -->
        <div class="card mt-4">
            <h3 class="card-title">学习经历</h3>
            <table class="data-table">
                <thead>
                    <tr>
                        <th>起止时间</th>
                        <th>学校/机构</th>
                        <th>专业</th>
                        <th>学位/学历</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(exp, index) in educationExperiences" :key="index">
                        <td>{{ exp.startDate }} - {{ exp.endDate }}</td>
                        <td>{{ exp.school }}</td>
                        <td>{{ exp.major }}</td>
                        <td>{{ exp.degree }}</td>
                        <td>
                            <button class="btn edit-btn" @click="editEducationExp(index)">
                                <i class="fas fa-edit"></i> 编辑
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>

            <button class="btn add-btn mt-2" @click="addEducationExp">
                <i class="fas fa-plus"></i> 添加学习经历
            </button>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useUserStore } from '../../stores/userStore';
import { useAppStore } from '../../stores/appStore';

// 状态管理
const userStore = useUserStore();
const appStore = useAppStore();

// 学生信息
const studentInfo = ref({
    studentId: '',
    name: '',
    gender: '男',
    birthDate: '',
    major: '',
    researchDirection: '',
    enrollmentDate: '',
    expectedGraduationDate: '',
    advisor: '',
    phone: '',
    email: '',
    status: ''
});

// 学习经历
const educationExperiences = ref([
    {
        startDate: '2019-09-01',
        endDate: '2023-06-30',
        school: 'XX大学',
        major: '计算机科学与技术',
        degree: '本科'
    }
]);

// 页面加载时初始化数据
onMounted(() => {
    // 从用户存储中获取基本信息
    const baseInfo = userStore.studentInfo;
    if (baseInfo) {
        studentInfo.value.studentId = baseInfo.studentId;
        studentInfo.value.name = baseInfo.name;
        studentInfo.value.major = baseInfo.major;
        studentInfo.value.advisor = baseInfo.advisor;
        studentInfo.value.status = baseInfo.status;
    }

    // 补充其他信息 - 实际应用中应从API获取
    studentInfo.value.birthDate = '2000-01-15';
    studentInfo.value.enrollmentDate = '2023-09-01';
    studentInfo.value.expectedGraduationDate = '2026-06-30';
    studentInfo.value.researchDirection = '机器学习';
    studentInfo.value.phone = '13800138000';
    studentInfo.value.email = 'student@example.com';
});

// 保存学生信息
const saveStudentInfo = () => {
    // 实际应用中应调用API保存
    appStore.addNotification('个人信息保存成功', 'success');
};

// 添加学习经历
const addEducationExp = () => {
    educationExperiences.value.push({
        startDate: '',
        endDate: '',
        school: '',
        major: '',
        degree: ''
    });
};

// 编辑学习经历
const editEducationExp = (index) => {
    // 实际应用中可以打开编辑弹窗
    appStore.addNotification('编辑学习经历功能', 'info');
};
</script>

<style scoped>
/* 学生信息页面样式 */
.student-info-container {
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

.card {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    padding: 20px;
    margin-bottom: 20px;
}

.main-container.dark-mode .card {
    background-color: #2a2a40;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}

.card-title {
    margin-top: 0;
    margin-bottom: 16px;
    font-size: 18px;
    color: #1f2329;
    font-weight: 600;
}

.main-container.dark-mode .card-title {
    color: #e0e0e0;
}

.form-grid {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.form-row {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
}

.form-item {
    flex: 1;
    min-width: 200px;
}

.form-item label {
    display: block;
    margin-bottom: 6px;
    font-weight: 500;
    font-size: 14px;
}

.main-container.dark-mode .form-item label {
    color: #e0e0e0;
}

.form-control {
    width: 100%;
    padding: 8px 12px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 14px;
}

.main-container.dark-mode .form-control {
    background-color: #333;
    border-color: #555;
    color: #fff;
}

.form-control:read-only {
    background-color: #f5f5f5;
    cursor: not-allowed;
}

.main-container.dark-mode .form-control:read-only {
    background-color: #444;
}

.form-actions {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}

.btn {
    padding: 8px 16px;
    border-radius: 4px;
    border: none;
    cursor: pointer;
    font-size: 14px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
}

.save-btn {
    background-color: #409eff;
    color: #fff;
}

.save-btn:hover {
    background-color: #66b1ff;
}

.data-table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 16px;
}

.data-table th,
.data-table td {
    padding: 12px 16px;
    text-align: left;
    border-bottom: 1px solid #eee;
}

.main-container.dark-mode .data-table th,
.main-container.dark-mode .data-table td {
    border-bottom-color: #444;
}

.data-table th {
    background-color: #f5f7fa;
    font-weight: 500;
    color: #666;
}

.main-container.dark-mode .data-table th {
    background-color: #333355;
    color: #bbb;
}

.data-table tr:hover {
    background-color: #f5f7fa;
}

.main-container.dark-mode .data-table tr:hover {
    background-color: #333355;
}

.edit-btn {
    background-color: #faad14;
    color: #fff;
    padding: 4px 8px;
    font-size: 12px;
}

.add-btn {
    background-color: #52c41a;
    color: #fff;
}

.mt-2 {
    margin-top: 8px;
}

.mt-4 {
    margin-top: 16px;
}

/* 响应式样式 */
@media (max-width: 768px) {
    .form-item {
        min-width: 100%;
    }
}
</style>