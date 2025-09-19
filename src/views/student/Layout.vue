<template>
    <div class="main-container" :class="{ 'dark-mode': appStore.darkMode }">
        <!-- 顶部导航 -->
        <header class="main-header">
            <div class="header-left">
                <h1 class="system-title">人工智能学院研究生管理信息系统</h1>
                <p class="user-info">欢迎您，{{ userStore.username }}（{{ userStore.roleText }}）</p>
            </div>
            <div class="header-right">
                <button @click="appStore.toggleDarkMode" class="theme-btn" title="切换主题模式">
                    <i class="fas" :class="appStore.darkMode ? 'fa-sun' : 'fa-moon'"></i>
                </button>
                <button @click="handleLogout" class="logout-btn" title="退出登录">
                    <i class="fas fa-sign-out-alt"></i>
                </button>
            </div>
        </header>

        <!-- 内容区域 -->
        <div class="content-container">
            <!-- 侧边栏 -->
            <aside class="sidebar" :class="{ 'collapsed': appStore.sidebarCollapsed }">
                <div class="sidebar-header">
                    <h2>学生中心</h2>
                    <button @click="appStore.toggleSidebar" class="toggle-btn" title="折叠/展开侧边栏">
                        <i class="fas" :class="appStore.sidebarCollapsed ? 'fa-angle-right' : 'fa-angle-left'"></i>
                    </button>
                </div>

                <nav class="sidebar-nav">
                    <div class="nav-group">
                        <h3 class="nav-group-title">
                            <i class="fas fa-graduation-cap"></i>
                            <span :class="{ 'hidden': appStore.sidebarCollapsed }">学籍管理</span>
                        </h3>
                        <ul class="nav-items">
                            <li :class="{ 'active': $route.name === 'StudentInfo' }"
                                @click="appStore.sidebarCollapsed = false">
                                <i class="fas fa-user"></i>
                                <span :class="{ 'hidden': appStore.sidebarCollapsed }">个人信息</span>
                            </li>
                            <li :class="{ 'active': $route.name === 'LeaveSchool' }"
                                @click="appStore.sidebarCollapsed = false">
                                <i class="fas fa-school"></i>
                                <span :class="{ 'hidden': appStore.sidebarCollapsed }">休学/复学申请</span>
                            </li>
                            <li :class="{ 'active': $route.name === 'GraduationCheck' }"
                                @click="appStore.sidebarCollapsed = false">
                                <i class="fas fa-check-circle"></i>
                                <span :class="{ 'hidden': appStore.sidebarCollapsed }">毕业资格审核</span>
                            </li>
                        </ul>
                    </div>

                    <div class="nav-group">
                        <h3 class="nav-group-title">
                            <i class="fas fa-book"></i>
                            <span :class="{ 'hidden': appStore.sidebarCollapsed }">课程管理</span>
                        </h3>
                        <ul class="nav-items">
                            <li :class="{ 'active': $route.name === 'CourseQuery' }"
                                @click="appStore.sidebarCollapsed = false">
                                <i class="fas fa-search"></i>
                                <span :class="{ 'hidden': appStore.sidebarCollapsed }">课程查询与选课</span>
                            </li>
                            <li :class="{ 'active': $route.name === 'GradeQuery' }"
                                @click="appStore.sidebarCollapsed = false">
                                <i class="fas fa-clipboard-check"></i>
                                <span :class="{ 'hidden': appStore.sidebarCollapsed }">成绩查询</span>
                            </li>
                        </ul>
                    </div>

                    <!-- 其他导航组省略 -->
                </nav>
            </aside>

            <!-- 主内容区 -->
            <main class="main-content">
                <div class="content-header">
                    <h2 class="content-title">{{ $route.meta.title || '研究生管理系统' }}</h2>
                    <div class="content-actions">
                        <button class="btn refresh-btn" @click="handleRefresh" title="刷新页面">
                            <i class="fas fa-sync-alt"></i> 刷新
                        </button>
                    </div>
                </div>

                <!-- 路由视图 -->
                <router-view class="view-content"></router-view>

                <!-- 通知组件 -->
                <div class="notifications-container">
                    <div v-for="notification in appStore.notifications" :key="notification.id" class="notification"
                        :class="`notification-${notification.type}`">
                        <i class="fas" :class="getIcon(notification.type)"></i>
                        <span>{{ notification.message }}</span>
                        <button class="close-notification" @click="appStore.removeNotification(notification.id)">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>
                </div>
            </main>
        </div>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/userStore';
import { useAppStore } from '../../stores/appStore';

// 状态和路由
const userStore = useUserStore();
const appStore = useAppStore();
const router = useRouter();

// 登出处理
const handleLogout = () => {
    userStore.logout();
    router.push('/login');
    appStore.addNotification('已成功退出登录', 'info');
};

// 刷新页面
const handleRefresh = () => {
    // 可以在这里添加刷新逻辑
    appStore.addNotification('页面已刷新', 'info');
};

// 获取通知图标
const getIcon = (type) => {
    const icons = {
        success: 'fa-check-circle',
        warning: 'fa-exclamation-triangle',
        info: 'fa-info-circle',
        error: 'fa-exclamation-circle'
    };
    return icons[type] || 'fa-info-circle';
};
</script>

<style scoped>
/* 布局样式 */
.main-container {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    transition: background-color 0.3s ease;
    background-color: #f5f7fa;
}

.main-container.dark-mode {
    background-color: #1a1a2e;
    color: #e0e0e0;
}

.main-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
    height: 60px;
    background-color: #fff;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
    z-index: 10;
}

.main-container.dark-mode .main-header {
    background-color: #2a2a40;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}

.header-left {
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.system-title {
    margin: 0;
    font-size: 18px;
    color: #1f2329;
}

.main-container.dark-mode .system-title {
    color: #fff;
}

.user-info {
    margin: 0;
    font-size: 12px;
    color: #666;
}

.main-container.dark-mode .user-info {
    color: #bbb;
}

.header-right {
    display: flex;
    gap: 10px;
}

.theme-btn,
.logout-btn {
    background: transparent;
    border: none;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: background-color 0.2s ease;
    color: #666;
}

.main-container.dark-mode .theme-btn,
.main-container.dark-mode .logout-btn {
    color: #bbb;
}

.theme-btn:hover,
.logout-btn:hover {
    background-color: #f5f5f5;
}

.main-container.dark-mode .theme-btn:hover,
.main-container.dark-mode .logout-btn:hover {
    background-color: #333;
}

.content-container {
    display: flex;
    flex: 1;
}

.sidebar {
    width: 240px;
    background-color: #fff;
    border-right: 1px solid #eee;
    transition: all 0.3s ease;
    display: flex;
    flex-direction: column;
}

.main-container.dark-mode .sidebar {
    background-color: #2a2a40;
    border-right-color: #444;
}

.sidebar.collapsed {
    width: 60px;
}

.sidebar-header {
    padding: 16px 20px;
    border-bottom: 1px solid #eee;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.main-container.dark-mode .sidebar-header {
    border-bottom-color: #444;
}

.sidebar-header h2 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
}

.toggle-btn {
    background: transparent;
    border: none;
    color: #666;
    cursor: pointer;
}

.main-container.dark-mode .toggle-btn {
    color: #bbb;
}

.sidebar-nav {
    padding-top: 10px;
    flex: 1;
    overflow-y: auto;
}

.nav-group {
    margin-bottom: 10px;
}

.nav-group-title {
    padding: 12px 20px;
    margin: 0;
    font-size: 14px;
    font-weight: 500;
    color: #666;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 10px;
    transition: all 0.2s ease;
}

.main-container.dark-mode .nav-group-title {
    color: #bbb;
}

.nav-group-title:hover {
    background-color: #f5f7fa;
}

.main-container.dark-mode .nav-group-title:hover {
    background-color: #333355;
}

.nav-items {
    list-style: none;
    margin: 0;
    padding: 0;
}

.nav-items li {
    padding: 10px 20px 10px 45px;
    font-size: 14px;
    color: #333;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 10px;
    transition: all 0.2s ease;
}

.main-container.dark-mode .nav-items li {
    color: #e0e0e0;
}

.nav-items li:hover {
    background-color: #f5f7fa;
}

.main-container.dark-mode .nav-items li:hover {
    background-color: #333355;
}

.nav-items li.active {
    background-color: #e6f7ff;
    color: #1890ff;
    border-left: 3px solid #1890ff;
}

.main-container.dark-mode .nav-items li.active {
    background-color: #333355;
    color: #66b1ff;
    border-left-color: #66b1ff;
}

.main-content {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
}

.content-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.content-title {
    margin: 0;
    font-size: 20px;
    color: #1f2329;
}

.main-container.dark-mode .content-title {
    color: #fff;
}

.refresh-btn {
    background-color: #f5f7fa;
    color: #666;
    border: none;
    border-radius: 4px;
    padding: 6px 12px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 6px;
}

.main-container.dark-mode .refresh-btn {
    background-color: #333355;
    color: #bbb;
}

.view-content {
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

.hidden {
    display: none;
}

/* 通知样式 */
.notifications-container {
    position: fixed;
    top: 70px;
    right: 20px;
    z-index: 1000;
    width: 300px;
}

.notification {
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    padding: 12px 16px;
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    gap: 10px;
    animation: slideIn 0.3s ease;
    border-left: 4px solid transparent;
}

.main-container.dark-mode .notification {
    background-color: #2a2a40;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.notification-success {
    border-left-color: #52c41a;
}

.notification-success i {
    color: #52c41a;
}

.notification-warning {
    border-left-color: #faad14;
}

.notification-warning i {
    color: #faad14;
}

.notification-info {
    border-left-color: #409eff;
}

.notification-info i {
    color: #409eff;
}

.close-notification {
    background: transparent;
    border: none;
    color: #999;
    cursor: pointer;
    margin-left: auto;
}

.main-container.dark-mode .close-notification {
    color: #666;
}

@keyframes slideIn {
    from {
        opacity: 0;
        transform: translateX(100%);
    }

    to {
        opacity: 1;
        transform: translateX(0);
    }
}

/* 响应式样式 */
@media (max-width: 768px) {
    .content-container {
        flex-direction: column;
    }

    .sidebar {
        width: 100%;
        border-right: none;
        border-bottom: 1px solid #eee;
        flex-direction: row;
        overflow-x: auto;
    }

    .main-container.dark-mode .sidebar {
        border-bottom-color: #444;
    }

    .sidebar.collapsed {
        width: 100%;
    }

    .sidebar-header {
        border-bottom: none;
        border-right: 1px solid #eee;
    }

    .main-container.dark-mode .sidebar-header {
        border-right-color: #444;
    }

    .sidebar-nav {
        display: flex;
        padding-top: 0;
        overflow-y: visible;
    }

    .nav-group {
        display: flex;
        flex-direction: column;
        margin-bottom: 0;
        margin-right: 10px;
    }

    .nav-group-title {
        padding: 12px;
        text-align: center;
        justify-content: center;
    }

    .nav-items li {
        padding: 10px 12px;
        justify-content: center;
    }

    .hidden {
        display: none !important;
    }
}
</style>