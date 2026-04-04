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
    path: '/student/selection',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'choices',
        component: () => import('@/views/student/selection/choices/index'),
        name: 'selection',
        meta: { title: '选择导师', icon: 'online' }
      },
      {
        path: 'results',
        component: () => import('@/views/student/selection/results/index'),
        name: 'results',
        meta: { title: '双选结果', icon: 'list' }
      }
    ]
  },
  {
    path: '/mentor/selection',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'applicants',
        component: () => import('@/views/mentor/selection/applicants/index'),
        name: 'applicants',
        meta: { title: '选择学生', icon: 'online' }
      },
      {
        path: 'confirmed',
        component: () => import('@/views/mentor/selection/confirmed/index'),
        name: 'confirmed',
        meta: { title: '已确认学生', icon: 'list' }
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
        meta: { title: '学籍变更申请', icon: 'edit' }
      },
      {
        path: 'approval',
        component: () => import('@/views/student/approval/index'),
        name: 'StudentApproval',
        meta: { title: '学籍变更审批', icon: 'check' }
      },
      {
        path: 'graduation',
        component: () => import('@/views/student/graduation/index'),
        name: 'GraduationAudit',
        meta: { title: '毕业资格审核', icon: 'finished' }
      }
    ]
  },
  {
    path: '/academic',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'activity',
        component: () => import('@/views/academic/activity/index'),
        name: 'AcademicActivity',
        meta: { title: '学术活动管理', icon: 'star' }
      },
      {
        path: 'innovation',
        component: () => import('@/views/academic/innovation/index'),
        name: 'InnovationProject',
        meta: { title: '创新实践项目', icon: 'trophy' }
      },
      {
        path: 'achievement',
        component: () => import('@/views/academic/achievement/index'),
        name: 'AcademicAchievement',
        meta: { title: '学术成果管理', icon: 'medal' }
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
    path: '/degree',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'proposal',
        component: () => import('@/views/degree/progress/index'),
        name: 'ThesisProposal',
        meta: { title: '论文开题', icon: 'document' }
      },
      {
        path: 'midterm',
        component: () => import('@/views/degree/progress/index'),
        name: 'ThesisMidterm',
        meta: { title: '论文中期检查', icon: 'date' }
      },
      {
        path: 'predefense',
        component: () => import('@/views/degree/progress/index'),
        name: 'ThesisPreDefense',
        meta: { title: '论文预答辩', icon: 'chat-dot-round' }
      },
      {
        path: 'application',
        component: () => import('@/views/degree/application/index'),
        name: 'DegreeApplication',
        meta: { title: '学位申请', icon: 's-promotion' }
      },
      {
        path: 'approval',
        component: () => import('@/views/degree/approval/index'),
        name: 'DegreeApproval',
        meta: { title: '学位审批', icon: 's-check' }
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
    path: '/system/dict-data',
    component: Layout,
    hidden: true,
    permissions: ['system:dict:list'],
    children: [
      {
        path: 'index/:dictId(\\d+)',
        component: () => import('@/views/system/dict/data'),
        name: 'Data',
        meta: { title: '字典数据', activeMenu: '/system/dict' }
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

export default router;
