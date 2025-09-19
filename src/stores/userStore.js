import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
    state: () => ({
        // 用户基本信息
        username: '',
        userType: '', // student 或 teacher
        teacherRole: '', // teaching, advisor, counselor
        isLoggedIn: false,

        // 学生额外信息
        studentInfo: {
            studentId: '',
            name: '',
            major: '',
            advisor: '',
            status: ''
        }
    }),

    actions: {
        // 登录操作
        login(username, userType, teacherRole = '') {
            console.log('登录操作');

            this.username = username;
            this.userType = userType;
            this.teacherRole = teacherRole;
            this.isLoggedIn = true;

            // 如果是学生，加载学生基本信息
            if (userType === 'student') {
                console.log('加载学生信息');

                this.loadStudentInfo(username);
            }
        },

        // 登出操作
        logout() {
            this.username = '';
            this.userType = '';
            this.teacherRole = '';
            this.isLoggedIn = false;
            this.studentInfo = {};
        },

        // 加载学生信息
        loadStudentInfo(username) {
            // 实际应用中这里应该从API获取
            this.studentInfo = {
                studentId: '2023001001',
                name: 'zyh',
                major: '人工智能',
                advisor: '李四教授',
                status: '在读'
            };
            console.log('加载学生信息成功： ' + this.studentInfo.name);

        }
    },

    getters: {
        // 获取用户角色文本
        roleText: (state) => {
            if (state.userType === 'student') return '学生';
            if (state.userType === 'teacher') {
                const roles = {
                    teaching: '教学老师',
                    advisor: '指导教师',
                    counselor: '辅导员'
                };
                return roles[state.teacherRole] || '教师';
            }
            return '';
        }
    }
});
