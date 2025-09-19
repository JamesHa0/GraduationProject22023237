<template>
    <div class="login-container" :class="{ 'dark-mode': appStore.darkMode }">
        <div class="login-card">
            <h1 class="system-title">人工智能学院研究生管理信息系统</h1>
            <div class="login-form">
                <div class="form-group">
                    <label for="username">用户名</label>
                    <input type="text" id="username" v-model="username" placeholder="请输入用户名" class="form-control" />
                </div>
                <div class="form-group">
                    <label for="password">密码</label>
                    <input type="password" id="password" v-model="password" placeholder="请输入密码" class="form-control" />
                </div>
                <div class="form-group">
                    <label>用户类型</label>
                    <div class="radio-group">
                        <label class="radio-label">
                            <input type="radio" v-model="userType" value="student" checked /> 学生
                        </label>
                        <label class="radio-label">
                            <input type="radio" v-model="userType" value="teacher" /> 教师
                        </label>
                        <label class="radio-label" v-if="userType === 'teacher'">
                            <select v-model="teacherRole" class="form-control role-select">
                                <option value="teaching">教学老师</option>
                                <option value="advisor">指导教师</option>
                                <option value="counselor">辅导员</option>
                            </select>
                        </label>
                    </div>
                </div>
                <button @click="handleLogin" class="btn login-btn">
                    <i class="fas fa-sign-in-alt"></i> 登录
                </button>
            </div>
            <div class="theme-toggle">
                <button @click="appStore.toggleDarkMode" class="theme-btn">
                    <i class="fas" :class="appStore.darkMode ? 'fa-sun' : 'fa-moon'"></i>
                    {{ appStore.darkMode ? '切换到亮色模式' : '切换到暗色模式' }}
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/userStore';
import { useAppStore } from '../stores/appStore';

// 状态定义
const username = ref('');
const password = ref('');
const userType = ref('student');
const teacherRole = ref('teaching');

// Store和路由
const userStore = useUserStore();
const appStore = useAppStore();
const router = useRouter();

// 登录处理
const handleLogin = () => {
    // if (!username.value || !password.value) {
    //     appStore.addNotification('请输入用户名和密码', 'warning');
    //     return;
    // }

    // 登录逻辑 - 实际应用中应该调用API验证
    userStore.login(username.value, userType.value, teacherRole.value);

    // 根据用户类型跳转到对应首页
    if (userType.value === 'student') {
        router.push('/student');
    } else {
        router.push('/teacher');
    }

    appStore.addNotification(`登录成功，欢迎 ${username.value}`, 'success');
};
</script>

<style scoped>
/* 登录页面样式 */
.login-container {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 100vh;
    padding: 20px;
    transition: background-color 0.3s ease;
    background-color: #f5f7fa;
}

.login-container.dark-mode {
    background-color: #1a1a2e;
}

.login-card {
    width: 100%;
    max-width: 450px;
    background-color: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    padding: 30px;
    transition: all 0.3s ease;
}

.login-container.dark-mode .login-card {
    background-color: #2a2a40;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.system-title {
    text-align: center;
    margin-bottom: 25px;
    color: #1f2329;
}

.login-container.dark-mode .system-title {
    color: #fff;
}

.form-group {
    margin-bottom: 16px;
}

.form-group label {
    display: block;
    margin-bottom: 6px;
    font-weight: 500;
    font-size: 14px;
}

.login-container.dark-mode .form-group label {
    color: #e0e0e0;
}

.radio-group {
    display: flex;
    gap: 16px;
    margin-top: 6px;
    flex-wrap: wrap;
}

.radio-label {
    display: flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
}

.role-select {
    margin-top: 6px;
    width: 120px;
}

.login-btn {
    width: 100%;
    background-color: #409eff;
    color: #fff;
    font-weight: 500;
    padding: 10px;
    font-size: 16px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
}

.login-btn:hover {
    background-color: #66b1ff;
}

.theme-toggle {
    text-align: center;
    margin-top: 20px;
}

.theme-btn {
    background: transparent;
    color: #409eff;
    border: 1px solid #409eff;
    padding: 6px 12px;
    border-radius: 4px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 6px;
    margin: 0 auto;
}

.login-container.dark-mode .theme-btn {
    color: #66b1ff;
    border-color: #66b1ff;
}

.form-control {
    width: 100%;
    padding: 8px 12px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 14px;
    transition: border-color 0.2s ease;
}

.login-container.dark-mode .form-control {
    background-color: #333;
    border-color: #555;
    color: #fff;
}

.form-control:focus {
    outline: none;
    border-color: #409eff;
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}
</style>