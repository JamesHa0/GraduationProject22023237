import { createWebHistory, createRouter } from 'vue-router'
/* Layout */
import Layout from '@/layout'

/**
 * Note: 路由配置项
 *
 * hidden: true                     // 当设置 true 的时候该路由不会再侧边栏出现 如401，login等页面，或者如一些编辑页面/edit/1
 * alwaysShow: true                 // 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
 *                                  // 只有一个时，会将那个子路由当做根路由显示在侧边栏--如引导页面
 *                                  // 若你想不管路由下面的 children 声明的个数都显示你的根路由
 *                                  // 你可以设置 alwaysShow: true，这样它就会忽略之前定义的规则，一直显示根路由
 * redirect: noRedirect             // 当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
 * name:'router-name'               // 设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
 * query: '{"id": 1, "name": "ry"}' // 访问路由的默认传递参数
 * roles: ['admin', 'common']       // 访问路由的角色权限
 * permissions: ['a:a:a', 'b:b:b']  // 访问路由的菜单权限
 * meta : {
    noCache: true                   // 如果设置为true，则不会被 <keep-alive> 缓存(默认 false)
    title: 'title'                  // 设置该路由在侧边栏和面包屑中展示的名字
    icon: 'svg-name'                // 设置该路由的图标，对应路径src/assets/icons/svg
    breadcrumb: false               // 如果设置为false，则不会在breadcrumb面包屑中显示
    activeMenu: '/system/user'      // 当路由设置了该属性，则会高亮相对应的侧边栏。
  }
 */

// 公共路由
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index.vue')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {
    path: "/:pathMatch(.*)*",
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: '/index',
        component: () => import('@/views/index'),
        name: 'Index',
        meta: { title: '首页', icon: 'dashboard', affix: true }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  },
  {
    path: '/course',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'info',
        component: () => import('@/views/course/info/index'),
        name: 'CourseInfo',
        meta: { title: '课程信息管理', icon: 'book' }
      },
      {
        path: 'selection',
        component: () => import('@/views/course/selection/index'),
        name: 'CourseSelection',
        meta: { title: '选课管理', icon: 'list' }
      },
      {
        path: 'score',
        component: () => import('@/views/course/score/index'),
        name: 'Score',
        meta: { title: '成绩管理', icon: 'score' }
      },
      {
        path: 'student-select',
        component: () => import('@/views/course/student-select/index'),
        name: 'StudentCourseSelect',
        meta: { title: '学生选课', icon: 'form' }
      },
      {
        path: 'select-result',
        component: () => import('@/views/course/select-result/index'),
        name: 'SelectResult',
        meta: { title: '选课结果', icon: 'list' }
      },
      {
        path: 'schedule',
        component: () => import('@/views/course/schedule/index'),
        name: 'CourseSchedule',
        meta: { title: '课程表查询', icon: 'date' }
      },
      {
        path: 'student-score',
        component: () => import('@/views/course/student-score/index'),
        name: 'StudentScore',
        meta: { title: '学业成绩', icon: 'score' }
      },
      {
        path: 'phase-manage',
        component: () => import('@/views/course/phase-manage/index'),
        name: 'CoursePhaseManage',
        meta: { title: '课程阶段管理', icon: 'build' }
      },
      {
        path: 'teacher-score',
        component: () => import('@/views/course/teacher-score/index'),
        name: 'TeacherScore',
        meta: { title: '成绩录入', icon: 'edit' }
      },
      {
        path: 'evaluation',
        component: () => import('@/views/course/evaluation/index'),
        name: 'TeachingEvaluation',
        meta: { title: '教学评价', icon: 'education' }
      },
      {
        path: 'schedule-manage',
        component: () => import('@/views/course/schedule-manage/index'),
        name: 'ScheduleManage',
        meta: { title: '排课管理', icon: 'date' }
      }
    ]
  },
  {
    path: '/student',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'info',
        component: () => import('@/views/student/info/index'),
        name: 'StudentInfo',
        meta: { title: '学生信息管理', icon: 'user' }
      },
      {
        path: 'change',
        component: () => import('@/views/student/change/index'),
        name: 'StudentChange',
        meta: { title: '学籍异动申请', icon: 'edit' }
      },
      {
        path: 'approval',
        component: () => import('@/views/student/approval/index'),
        name: 'StudentApproval',
        meta: { title: '学籍异动审核', icon: 'check' }
      },
      {
        path: 'student-change',
        component: () => import('@/views/student/student-change/index'),
        name: 'StudentChangeApply',
        meta: { title: '学籍异动申请', icon: 'edit' }
      },
      {
        path: 'student-profile',
        component: () => import('@/views/student/student-profile/index'),
        name: 'StudentProfile',
        meta: { title: '学籍信息维护', icon: 'user' }
      }
    ]
  },
  {
    path: '/academic',
    component: Layout,
    hidden: true,
    redirect: '/academic/submit',
    children: [
      {
        path: 'submit',
        component: () => import('@/views/academic/submit/index'),
        name: 'AcademicManagement',
        meta: { title: '学术内容管理', icon: 'star' }
      },
      {
        path: 'activity',
        redirect: { path: '/academic/submit', query: { type: 'activity' } }
      },
      {
        path: 'innovation',
        redirect: { path: '/academic/submit', query: { type: 'innovation' } }
      },
      {
        path: 'achievement',
        redirect: { path: '/academic/submit', query: { type: 'achievement' } }
      },
      {
        path: 'review',
        component: () => import('@/views/academic/review/index'),
        name: 'AcademicReview',
        meta: { title: '学术内容审核', icon: 'check' }
      }
    ]
  },
  {
    path: '/thesis',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      // ==================== 学生流程区（501-507）====================
      {
        path: 'topic',
        component: () => import('@/views/degree/topic/index'),
        name: 'ThesisTopic',
        meta: { title: '选题', icon: 'edit' }
      },
      {
        path: 'task',
        component: () => import('@/views/degree/task/index'),
        name: 'ThesisTask',
        meta: { title: '任务书', icon: 'document' }
      },
      {
        path: 'proposal',
        component: () => import('@/views/degree/proposal/index'),
        name: 'ThesisProposal',
        meta: { title: '开题报告', icon: 'date' }
      },
      {
        path: 'midterm',
        component: () => import('@/views/degree/midterm/index'),
        name: 'ThesisMidterm',
        meta: { title: '中期检查', icon: 'checked' }
      },
      {
        path: 'draft',
        component: () => import('@/views/degree/draft/index'),
        name: 'ThesisDraft',
        meta: { title: '过程稿', icon: 'files' }
      },
      {
        path: 'defense-draft',
        component: () => import('@/views/degree/defense-draft/index'),
        name: 'ThesisDefenseDraft',
        meta: { title: '论文答辩稿', icon: 'files' }
      },
      {
        path: 'thesis-final',
        component: () => import('@/views/degree/thesis-final/index'),
        name: 'ThesisFinal',
        meta: { title: '毕业论文', icon: 'document-checked' }
      },
      // ==================== 导师管理区（510-516）====================
      {
        path: 'supervisor/topic',
        component: () => import('@/views/degree/supervisor/topic/index'),
        name: 'SupervisorTopic',
        meta: { title: '学生选题', icon: 'edit' }
      },
      {
        path: 'supervisor/topic-modification',
        component: () => import('@/views/degree/supervisor/topic-modification/index'),
        name: 'SupervisorTopicModification',
        meta: { title: '选题修改申请', icon: 'edit' }
      },
      {
        path: 'supervisor/task',
        component: () => import('@/views/degree/supervisor/task/index'),
        name: 'SupervisorTask',
        meta: { title: '任务书管理', icon: 'document' }
      },
      {
        path: 'supervisor/proposal',
        component: () => import('@/views/degree/supervisor/proposal/index'),
        name: 'SupervisorProposal',
        meta: { title: '学生开题报告', icon: 'date' }
      },
      {
        path: 'supervisor/midterm',
        component: () => import('@/views/degree/supervisor/midterm/index'),
        name: 'SupervisorMidterm',
        meta: { title: '学生中期检查', icon: 'checked' }
      },
      {
        path: 'supervisor/defense-draft',
        component: () => import('@/views/degree/supervisor/defense-draft/index'),
        name: 'SupervisorDefenseDraft',
        meta: { title: '论文答辩稿管理', icon: 'files' }
      },
      {
        path: 'supervisor/thesis-final',
        component: () => import('@/views/degree/supervisor/thesis-final/index'),
        name: 'SupervisorThesisFinal',
        meta: { title: '论文最终稿管理', icon: 'document-checked' }
      },
      // ==================== 行政管理区（520-525）====================
      {
        path: 'progress',
        component: () => import('@/views/degree/progress/index'),
        name: 'ThesisProgress',
        meta: { title: '进度查询', icon: 'search' }
      },
      {
        path: 'process-approval',
        component: () => import('@/views/degree/process-approval/index'),
        name: 'ProcessApproval',
        meta: { title: '流程审批', icon: 'check' }
      },
      {
        path: 'process-config',
        component: () => import('@/views/degree/process-config/index'),
        name: 'ProcessConfig',
        meta: { title: '流程配置', icon: 'setup' }
      },
      {
        path: 'statistics',
        component: () => import('@/views/degree/statistics/index'),
        name: 'ThesisStatistics',
        meta: { title: '统计归档', icon: 'chart' }
      },
      {
        path: 'evaluation',
        component: () => import('@/views/degree/evaluation/index'),
        name: 'ThesisEvaluation',
        meta: { title: '成绩评定', icon: 'score' }
      }
    ]
  },
  {
    path: '/degree',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      // ==================== 毕业审核区（701）====================
      {
        path: 'graduation-audit',
        component: () => import('@/views/degree/graduation-audit/index'),
        name: 'GraduationAuditManage',
        meta: { title: '毕业审核', icon: 'finished' }
      },
      // ==================== 学位管理区（702-704）====================
      {
        path: 'degree-application',
        component: () => import('@/views/degree/degree-application/index'),
        name: 'DegreeApplication',
        meta: { title: '学位申请', icon: 'form' }
      },
      {
        path: 'defense-manage',
        component: () => import('@/views/degree/defense-manage/index'),
        name: 'DefenseManage',
        meta: { title: '答辩管理', icon: 'date' }
      },
      {
        path: 'degree-approval',
        component: () => import('@/views/degree/degree-approval/index'),
        name: 'DegreeApprovalNew',
        meta: { title: '学位审批', icon: 'approval' }
      },
      {
        path: 'degree-grant',
        component: () => import('@/views/degree/degree-grant/index'),
        name: 'DegreeGrant',
        meta: { title: '学位授予', icon: 'education' }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'user',
        component: () => import('@/views/system/user/index'),
        name: 'UserManage',
        meta: { title: '用户管理', icon: 'user' }
      },
      {
        path: 'role',
        component: () => import('@/views/system/role/index'),
        name: 'RoleManage',
        meta: { title: '角色管理', icon: 'peoples' }
      },
      {
        path: 'menu',
        component: () => import('@/views/system/menu/index'),
        name: 'MenuManage',
        meta: { title: '菜单管理', icon: 'tree' }
      },
      {
        path: 'dept',
        component: () => import('@/views/system/dept/index'),
        name: 'DeptManage',
        meta: { title: '部门管理', icon: 'tree' }
      },
      {
        path: 'dict',
        component: () => import('@/views/system/dict/index'),
        name: 'DictManage',
        meta: { title: '字典管理', icon: 'dict' }
      },
      {
        path: 'config',
        component: () => import('@/views/system/config/index'),
        name: 'ConfigManage',
        meta: { title: '参数设置', icon: 'edit' }
      },
      {
        path: 'notice',
        component: () => import('@/views/system/notice/index'),
        name: 'NoticeManage',
        meta: { title: '通知公告', icon: 'message' }
      },
      {
        path: 'post',
        component: () => import('@/views/system/post/index'),
        name: 'PostManage',
        meta: { title: '岗位管理', icon: 'post' }
      },
      {
        path: 'field',
        component: () => import('@/views/system/field/index'),
        name: 'FieldManage',
        meta: { title: '系统字段', icon: 'form' }
      },
      {
        path: 'permission',
        component: () => import('@/views/system/permission/index'),
        name: 'PermissionManage',
        meta: { title: '权限管理', icon: 'lock' }
      },
      {
        path: 'oplog',
        component: () => import('@/views/system/oplog/index'),
        name: 'Oplog',
        meta: { title: '操作日志', icon: 'log' }
      }
    ]
  },
  {
    path: '/selection',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'student-choose',
        component: () => import('@/views/selection/student-choose/index'),
        name: 'StudentChoose',
        meta: { title: '学生选导师', icon: 'online' }
      },
      {
        path: 'results',
        component: () => import('@/views/selection/results/index'),
        name: 'SelectionResults',
        meta: { title: '双选结果查询', icon: 'list' }
      },
      {
        path: 'mentor-choose',
        component: () => import('@/views/selection/mentor-choose/index'),
        name: 'MentorChoose',
        meta: { title: '导师选学生', icon: 'online' }
      },
      {
        path: 'confirm',
        component: () => import('@/views/selection/confirm/index'),
        name: 'ConfirmStudent',
        meta: { title: '确认学生', icon: 'list' }
      },
      {
        path: 'relationship',
        component: () => import('@/views/selection/relationship/index'),
        name: 'MentorStudentRelationship',
        meta: { title: '导师学生关系管理', icon: 'team' }
      },
      {
        path: 'mentor-change',
        component: () => import('@/views/selection/mentor-change-application/index'),
        name: 'MentorChange',
        meta: { title: '导师更换申请', icon: 'people' }
      },
      {
        path: 'mentor-change-manage',
        component: () => import('@/views/selection/mentor-change/index'),
        name: 'MentorChangeManage',
        meta: { title: '导师更换申请管理', icon: 'people' }
      },
      {
        path: 'round-manage',
        component: () => import('@/views/selection/round-manage/index'),
        name: 'RoundChange',
        meta: { title: '双选轮次管理', icon: 'build' }
      },
      {
        path: 'manual-assign',
        component: () => import('@/views/selection/manual-assign/index'),
        name: 'ManualAssign',
        meta: { title: '手动分配导师', icon: 'user' }
      },
      {
        path: 'quota-manage',
        component: () => import('@/views/selection/quota-manage/index'),
        name: 'QuotaManage',
        meta: { title: '导师名额管理', icon: 'peoples' }
      }
    ]
  }
]

// 动态路由，基于用户权限动态去加载
export const dynamicRoutes = [
  {
    path: '/system/user-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:user:edit'],
    children: [
      {
        path: 'role/:userId(\\d+)',
        component: () => import('@/views/system/user/authRole'),
        name: 'AuthRole',
        meta: { title: '分配角色', activeMenu: '/system/user' }
      }
    ]
  },
  {
    path: '/system/role-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:role:edit'],
    children: [
      {
        path: 'user/:roleId(\\d+)',
        component: () => import('@/views/system/role/authUser'),
        name: 'AuthUser',
        meta: { title: '分配用户', activeMenu: '/system/role' }
      }
    ]
  },
  {
    path: '/monitor/job-log',
    component: Layout,
    hidden: true,
    permissions: ['monitor:job:list'],
    children: [
      {
        path: 'index/:jobId(\\d+)',
        component: () => import('@/views/monitor/job/log'),
        name: 'JobLog',
        meta: { title: '调度日志', activeMenu: '/monitor/job' }
      }
    ]
  },
  {
    path: '/tool/gen-edit',
    component: Layout,
    hidden: true,
    permissions: ['tool:gen:edit'],
    children: [
      {
        path: 'index/:tableId(\\d+)',
        component: () => import('@/views/tool/gen/editTable'),
        name: 'GenEdit',
        meta: { title: '修改生成配置', activeMenu: '/tool/gen' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  },
});

export default router
