<template>
    <div class="main-container" :class="{ 'dark-mode': appStore.darkMode }">
        <!-- 顶部导航栏 -->
        <header class="main-header">
            <div class="header-left">
                <button class="sidebar-toggle" @click="toggleSidebar">
                    <font-awesome-icon icon="bars" />
                </button>
                <div class="logo">
                    <font-awesome-icon icon="graduation-cap" class="logo-icon" />
                    <span class="logo-text">研究生管理系统</span>
                </div>
            </div>

            <div class="header-right">
                <button class="theme-toggle" @click="toggleDarkMode" title="切换主题模式">
                    <font-awesome-icon :icon="appStore.darkMode ? 'sun' : 'moon'" />
                </button>

                <div class="notification-btn" @click="showNotifications = !showNotifications">
                    <font-awesome-icon icon="bell" />
                    <span class="notification-badge" v-if="unreadNotifications > 0">{{ unreadNotifications }}</span>
                </div>

                <div class="user-menu">
                    <div class="user-info" @click="showUserMenu = !showUserMenu">
                        <div class="user-avatar">
                            <font-awesome-icon icon="user-circle" size="lg" />
                        </div>
                        <span class="user-name">{{ userStore.username }}</span>
                        <font-awesome-icon icon="chevron-down" size="sm" class="menu-arrow"
                            :class="{ 'rotate': showUserMenu }" />
                    </div>

                    <div class="dropdown-menu" v-if="showUserMenu">
                        <div class="menu-item">
                            <font-awesome-icon icon="user" class="menu-icon" />
                            <span>个人信息</span>
                        </div>
                        <div class="menu-item">
                            <font-awesome-icon icon="cog" class="menu-icon" />
                            <span>系统设置</span>
                        </div>
                        <div class="menu-divider"></div>
                        <div class="menu-item logout-item" @click="logout">
                            <font-awesome-icon icon="sign-out-alt" class="menu-icon" />
                            <span>退出登录</span>
                        </div>
                    </div>
                </div>
            </div>
        </header>

        <!-- 主体内容区 -->
        <div class="main-content">
            <!-- 侧边栏 -->
            <aside class="sidebar" :class="{ 'collapsed': collapsed, 'hidden': hideSidebar }">
                <nav class="sidebar-nav">
                    <div class="nav-group">
                        <div class="nav-group-title">教师功能</div>
                        <router-link to="/teacher/dashboard" class="nav-item"
                            :class="{ 'active': $route.path === '/teacher/dashboard' }">
                            <font-awesome-icon icon="home" class="nav-icon" />
                            <span class="nav-text">工作台</span>
                        </router-link>

                        <router-link to="/teacher/course-management" class="nav-item"
                            :class="{ 'active': $route.path === '/teacher/course-management' }">
                            <font-awesome-icon icon="book" class="nav-icon" />
                            <span class="nav-text">课程管理</span>
                        </router-link>

                        <router-link to="/teacher/advisee-info" class="nav-item"
                            :class="{ 'active': $route.path === '/teacher/advisee-info' }">
                            <font-awesome-icon icon="users" class="nav-icon" />
                            <span class="nav-text">学生管理</span>
                        </router-link>

                        <router-link to="/teacher/academic-review" class="nav-item"
                            :class="{ 'active': $route.path === '/teacher/academic-review' }">
                            <font-awesome-icon icon="file-alt" class="nav-icon" />
                            <span class="nav-text">学术审核</span>
                        </router-link>
                    </div>

                    <div class="nav-group">
                        <div class="nav-group-title">系统</div>
                        <router-link to="/teacher/settings" class="nav-item"
                            :class="{ 'active': $route.path === '/teacher/settings' }">
                            <font-awesome-icon icon="cog" class="nav-icon" />
                            <span class="nav-text">系统设置</span>
                        </router-link>
                    </div>
                </nav>
            </aside>

            <!-- 通知面板 -->
            <div class="notification-panel" v-if="showNotifications">
                <div class="panel-header">
                    <h3 class="panel-title">通知消息</h3>
                    <button class="mark-all-read" @click="markAllAsRead">全部标为已读</button>
                </div>

                <div class="notification-list">
                    <div class="notification-item unread" v-for="(notif, index) in notifications" :key="index">
                        <div class="notif-icon">
                            <font-awesome-icon :icon="notif.icon" :class="notif.type" />
                        </div>
                        <div class="notif-content">
                            <div class="notif-title">{{ notif.title }}</div>
                            <div class="notif-time">{{ notif.time }}</div>
                        </div>
                    </div>

                    <div class="no-notifications" v-if="notifications.length === 0">
                        暂无通知消息
                    </div>
                </div>
            </div>

            <!-- 页面内容 -->
            <div class="page-content">
                <router-view />
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/userStore';
import { useAppStore } from '../../stores/appStore';

// 状态管理
const userStore = useUserStore();
const appStore = useAppStore();
const router = useRouter();

// 侧边栏状态
const collapsed = ref(false);
const hideSidebar = ref(false);
const showUserMenu = ref(false);
const showNotifications = ref(false);

// 通知数据
const notifications = ref([
    {
        title: "张三提交了开题报告，请进行审核",
        time: "10分钟前",
        icon: "file-alt",
        type: "info"
    },
    {
        title: "李四修改了个人信息",
        time: "1小时前",
        icon: "user",
        type: "info"
    },
    {
        title: "《高级算法》课程学生名单已更新",
        time: "昨天",
        icon: "book",
        type: "info"
    }
]);

// 未读通知数量
const unreadNotifications = computed(() => {
    // 实际应用中应该根据通知的已读状态计算
    return notifications.value.length;
});

// 页面加载时检查窗口大小
onMounted(() => {
    checkWindowSize();
    window.addEventListener('resize', checkWindowSize);
});

// 检查窗口大小，在小屏幕上自动隐藏侧边栏
const checkWindowSize = () => {
    if (window.innerWidth < 768) {
        hideSidebar.value = true;
    } else {
        hideSidebar.value = false;
    }
};

// 切换侧边栏显示/隐藏
const toggleSidebar = () => {
    hideSidebar.value = !hideSidebar.value;
};

// 切换侧边栏折叠状态
const toggleCollapse = () => {
    collapsed.value = !collapsed.value;
};

// 切换暗黑模式
const toggleDarkMode = () => {
    appStore.setDarkMode(!appStore.darkMode);
};

// 标记所有通知为已读
const markAllAsRead = () => {
    // 实际应用中应该更新通知的已读状态
    appStore.addNotification('所有通知已标记为已读', 'success');
};

// 退出登录
const logout = () => {
    userStore.clearUserInfo();
    router.push('/login');
    appStore.addNotification('已成功退出登录', 'success');
    showUserMenu.value = false;
};
</script>

<style scoped>
.main-container {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    transition: background-color 0.3s ease;
}

/* 顶部导航栏 */
.main-header {
    height: 60px;
    background-color: #fff;
    border-bottom: 1px solid #eee;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    z-index: 10;
}

.dark-mode .main-header {
    background-color: #1a1a2e;
    border-bottom-color: #333;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.header-left {
    display: flex;
    align-items: center;
    gap: 15px;
}

.sidebar-toggle {
    background: none;
    border: none;
    font-size: 18px;
    cursor: pointer;
    color: #666;
    width: 36px;
    height: 36px;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.sidebar-toggle:hover {
    background-color: #f5f5f5;
}

.dark-mode .sidebar-toggle {
    color: #bbb;
}

.dark-mode .sidebar-toggle:hover {
    background-color: #333;
}

.logo {
    display: flex;
    align-items: center;
    gap: 8px;
}

.logo-icon {
    color: #409eff;
    font-size: 20px;
}

.logo-text {
    font-size: 18px;
    font-weight: 600;
    color: #1f2329;
}

.dark-mode .logo-text {
    color: #e0e0e0;
}

.header-right {
    display: flex;
    align-items: center;
    gap: 15px;
}

.theme-toggle {
    background: none;
    border: none;
    font-size: 18px;
    cursor: pointer;
    color: #666;
    width: 36px;
    height: 36px;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.theme-toggle:hover {
    background-color: #f5f5f5;
}

.dark-mode .theme-toggle {
    color: #bbb;
}

.dark-mode .theme-toggle:hover {
    background-color: #333;
}

.notification-btn {
    position: relative;
    background: none;
    border: none;
    font-size: 18px;
    cursor: pointer;
    color: #666;
    width: 36px;
    height: 36px;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.notification-btn:hover {
    background-color: #f5f5f5;
}

.dark-mode .notification-btn {
    color: #bbb;
}

.dark-mode .notification-btn:hover {
    background-color: #333;
}

.notification-badge {
    position: absolute;
    top: 2px;
    right: 2px;
    width: 16px;
    height: 16px;
    background-color: #f5222d;
    color: white;
    border-radius: 50%;
    font-size: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.user-menu {
    position: relative;
}

.user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 5px 10px;
    border-radius: 4px;
}

.user-info:hover {
    background-color: #f5f5f5;
}

.dark-mode .user-info:hover {
    background-color: #333;
}

.user-avatar {
    color: #409eff;
}

.user-name {
    font-weight: 500;
}

.menu-arrow {
    transition: transform 0.2s ease;
}

.rotate {
    transform: rotate(180deg);
}

.dropdown-menu {
    position: absolute;
    top: 100%;
    right: 0;
    min-width: 180px;
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    padding: 5px 0;
    margin-top: 5px;
    z-index: 100;
}

.dark-mode .dropdown-menu {
    background-color: #2a2a40;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
}

.menu-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 16px;
    cursor: pointer;
    transition: background-color 0.2s ease;
}

.menu-item:hover {
    background-color: #f5f7fa;
}

.dark-mode .menu-item:hover {
    background-color: #333355;
}

.menu-icon {
    width: 16px;
    color: #666;
}

.dark-mode .menu-icon {
    color: #bbb;
}

.menu-divider {
    height: 1px;
    background-color: #eee;
    margin: 5px 0;
}

.dark-mode .menu-divider {
    background-color: #444;
}

.logout-item {
    color: #f5222d;
}

.logout-item .menu-icon {
    color: #f5222d;
}

/* 主体内容区 */
.main-content {
    display: flex;
    flex: 1;
}

/* 侧边栏 */
.sidebar {
    width: 220px;
    background-color: #fff;
    border-right: 1px solid #eee;
    transition: all 0.3s ease;
    overflow: hidden;
    z-index: 5;
}

.dark-mode .sidebar {
    background-color: #1a1a2e;
    border-right-color: #333;
}

.sidebar.collapsed {
    width: 60px;
}

.sidebar.hidden {
    display: none;
}

.sidebar-nav {
    padding-top: 20px;
}

.nav-group {
    margin-bottom: 20px;
}

.nav-group-title {
    padding: 0 20px 10px;
    font-size: 12px;
    color: #86909c;
    text-transform: uppercase;
    letter-spacing: 1px;
}

.nav-item {
    display: flex;
    align-items: center;
    padding: 12px 20px;
    color: #4e5969;
    text-decoration: none;
    transition: all 0.2s ease;
}

.nav-item:hover {
    background-color: #f5f7fa;
    color: #1890ff;
}

.dark-mode .nav-item {
    color: #bbb;
}

.dark-mode .nav-item:hover {
    background-color: #333355;
    color: #66b1ff;
}

.nav-item.active {
    background-color: #e6f7ff;
    color: #1890ff;
    border-left: 3px solid #1890ff;
}

.dark-mode .nav-item.active {
    background-color: #1a365d;
    color: #66b1ff;
}

.nav-icon {
    width: 18px;
    margin-right: 10px;
}

.sidebar.collapsed .nav-text,
.sidebar.collapsed .nav-group-title {
    display: none;
}

.sidebar.collapsed .nav-item {
    justify-content: center;
    padding: 12px;
}

.sidebar.collapsed .nav-item.active {
    border-left: none;
}

.sidebar.collapsed .nav-icon {
    margin-right: 0;
    font-size: 18px;
}

/* 通知面板 */
.notification-panel {
    position: absolute;
    top: 60px;
    right: 20px;
    width: 360px;
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    z-index: 100;
    max-height: 400px;
    overflow: hidden;
}

.dark-mode .notification-panel {
    background-color: #2a2a40;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
}

.panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border-bottom: 1px solid #eee;
}

.dark-mode .panel-header {
    border-bottom-color: #444;
}

.panel-title {
    font-size: 16px;
    font-weight: 600;
    margin: 0;
}

.mark-all-read {
    background: none;
    border: none;
    color: #1890ff;
    font-size: 14px;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: 4px;
}

.mark-all-read:hover {
    background-color: #f5f7fa;
}

.notification-list {
    max-height: calc(400px - 50px);
    overflow-y: auto;
}

.notification-item {
    padding: 12px 16px;
    border-bottom: 1px solid #eee;
    display: flex;
    align-items: flex-start;
    gap: 12px;
    cursor: pointer;
    transition: background-color 0.2s ease;
}

.dark-mode .notification-item {
    border-bottom-color: #444;
}

.notification-item:hover {
    background-color: #f5f7fa;
}

.dark-mode .notification-item:hover {
    background-color: #333355;
}

.notification-item.unread {
    background-color: #f0f7ff;
}

.dark-mode .notification-item.unread {
    background-color: #1a2940;
}

.notif-icon {
    font-size: 18px;
    margin-top: 2px;
}

.notif-icon.info {
    color: #1890ff;
}

.notif-icon.success {
    color: #52c41a;
}

.notif-icon.warning {
    color: #faad14;
}

.notif-icon.error {
    color: #f5222d;
}

.notif-content {
    flex: 1;
}

.notif-title {
    font-size: 14px;
    margin-bottom: 4px;
}

.notif-time {
    font-size: 12px;
    color: #86909c;
}

.no-notifications {
    padding: 20px;
    text-align: center;
    color: #86909c;
}

/* 页面内容区 */
.page-content {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
}

/* 响应式样式 */
@media (max-width: 768px) {
    .sidebar {
        position: fixed;
        height: calc(100vh - 60px);
        top: 60px;
        left: 0;
    }

    .notification-panel {
        width: calc(100% - 40px);
    }

    .logo-text {
        display: none;
    }

    .user-name {
        display: none;
    }
}
</style>