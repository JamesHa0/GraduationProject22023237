import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '../stores/userStore';

// 公共页面
const Login = () => import('../views/Login.vue');
const NotFound = () => import('../views/NotFound.vue');

// 学生页面
const StudentLayout = () => import('../views/student/Layout.vue');
const StudentInfo = () => import('../views/student/StudentInfo.vue');
const CourseQuery = () => import('../views/student/CourseQuery.vue');
const ThesisProposal = () => import('../views/student/ThesisProposal.vue');
const AcademicAchieve = () => import('../views/student/AcademicAchieve.vue');

// 教师页面
const TeacherLayout = () => import('../views/teacher/Layout.vue');
const CourseManagement = () => import('../views/teacher/CourseManagement.vue');
const AdviseeInfo = () => import('../views/teacher/AdviseeInfo.vue');
const AcademicReview = () => import('../views/teacher/AcademicReview.vue');

// 路由规则
const routes = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        name: 'Login',
        component: Login,
        meta: {
            public: true // 不需要登录
        }
    },
    {
        path: '/student',
        children: [
            {
                path: '',
                name: 'Student',
                component: StudentLayout,
                // meta: { requiresAuth: true, role: 'student' }
            },
            {
                path: 'info',
                name: 'StudentInfo',
                component: StudentInfo,
                meta: { title: '个人信息' }
            },
            {
                path: 'courses',
                name: 'CourseQuery',
                component: CourseQuery,
                meta: { title: '课程查询与选课' }
            },
            {
                path: 'thesis-proposal',
                name: 'ThesisProposal',
                component: ThesisProposal,
                meta: { title: '开题申请' }
            },
            {
                path: 'academic-achieve',
                name: 'AcademicAchieve',
                component: AcademicAchieve,
                meta: { title: '学术成果管理' }
            }
        ]
    },
    {
        path: '/teacher',
        children: [
            {
                path: '',
                name: 'Teacher',
                component: TeacherLayout,
                meta: { requiresAuth: true, role: 'teacher' }
            },
            {
                path: 'courses',
                name: 'CourseManagement',
                component: CourseManagement,
                meta: { title: '课程管理' }
            },
            {
                path: 'advisees',
                name: 'AdviseeInfo',
                component: AdviseeInfo,
                meta: { title: '指导学生信息' }
            },
            {
                path: 'academic-review',
                name: 'AcademicReview',
                component: AcademicReview,
                meta: { title: '学术成果审核' }
            }
        ]
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: NotFound
    }
];

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
});

// 路由守卫
router.beforeEach((to, from, next) => {
    const userStore = useUserStore();

    // 公共页面直接访问
    if (to.meta.public) {
        return next();
    }

    // 需要登录的页面
    if (to.meta.requiresAuth) {
        // 未登录，跳转到登录页
        if (!userStore.isLoggedIn) {
            return next('/login');
        }

        // 检查角色权限
        if (to.meta.role && userStore.userType !== to.meta.role) {
            // 角色不匹配，跳转到对应首页
            if (userStore.userType === 'student') {
                return next('/student');
            } else if (userStore.userType === 'teacher') {
                return next('/teacher');
            } else {
                return next('/login');
            }
        }

        return next();
    }

    next();
});

export default router;
