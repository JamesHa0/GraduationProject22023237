import { defineStore } from 'pinia';

export const useAppStore = defineStore('app', {
    state: () => ({
        darkMode: false,
        sidebarCollapsed: false,
        notifications: []
    }),

    actions: {
        // 切换暗黑模式
        toggleDarkMode() {
            this.darkMode = !this.darkMode;
            // 可以在这里添加保存到本地存储的逻辑
            localStorage.setItem('darkMode', this.darkMode);
        },

        // 切换侧边栏状态
        toggleSidebar() {
            this.sidebarCollapsed = !this.sidebarCollapsed;
        },

        // 添加通知
        addNotification(message, type = 'info', duration = 3000) {
            const id = Date.now();
            this.notifications.push({ id, message, type });

            // 自动移除通知
            setTimeout(() => {
                this.removeNotification(id);
            }, duration);
        },

        // 移除通知
        removeNotification(id) {
            this.notifications = this.notifications.filter(notification => notification.id !== id);
        }
    }
});
