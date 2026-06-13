import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/LayoutView.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '警情驾驶舱', icon: 'Monitor' }
      },
      {
        path: 'alarm',
        name: 'Alarm',
        component: () => import('@/views/alarm/AlarmView.vue'),
        meta: { title: '报警受理', icon: 'Bell', perm: 'alarm:view' }
      },
      {
        path: 'case',
        name: 'Case',
        component: () => import('@/views/case/CaseView.vue'),
        meta: { title: '案件管理', icon: 'Folder', perm: 'case:view' }
      },
      {
        path: 'person',
        name: 'Person',
        component: () => import('@/views/person/PersonView.vue'),
        meta: { title: '人员档案', icon: 'User', perm: 'person:view' }
      },
      {
        path: 'vehicle',
        name: 'Vehicle',
        component: () => import('@/views/vehicle/VehicleView.vue'),
        meta: { title: '车辆管理', icon: 'Van', perm: 'vehicle:view' }
      },
      {
        path: 'patrol',
        name: 'Patrol',
        component: () => import('@/views/patrol/PatrolView.vue'),
        meta: { title: '巡逻调度', icon: 'Location', perm: 'patrol:view' }
      },
      {
        path: 'patrol/schedule',
        name: 'PatrolSchedule',
        component: () => import('@/views/patrol/ScheduleView.vue'),
        meta: { title: '排班管理', icon: 'Calendar', perm: 'patrol:view' }
      },
      {
        path: 'officer',
        name: 'Officer',
        component: () => import('@/views/officer/OfficerView.vue'),
        meta: { title: '警力资源', icon: 'Avatar', perm: 'patrol:view' }
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/UserView.vue'),
        meta: { title: '用户管理', icon: 'Setting', perm: 'sys:user:view' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/RoleView.vue'),
        meta: { title: '角色管理', icon: 'Lock', perm: 'sys:role:view' }
      },
      {
        path: 'system/dept',
        name: 'SystemDept',
        component: () => import('@/views/system/DeptView.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding', perm: 'sys:dept:view' }
      },
      {
        path: 'system/log',
        name: 'SystemLog',
        component: () => import('@/views/system/LogView.vue'),
        meta: { title: '操作日志', icon: 'Document', perm: 'sys:log:view' }
      },
      {
        path: 'system/ai-config',
        name: 'SystemAiConfig',
        component: () => import('@/views/system/AiConfigView.vue'),
        meta: { title: 'AI配置', icon: 'Setting', perm: 'sys:user:edit' }
      },
      {
        path: 'equipment',
        name: 'Equipment',
        component: () => import('@/views/equipment/EquipmentView.vue'),
        meta: { title: '装备管理', icon: 'Box' }
      },
      {
        path: 'stat',
        name: 'Stat',
        component: () => import('@/views/stat/StatView.vue'),
        meta: { title: '统计分析', icon: 'DataAnalysis' }
      },
      {
        path: 'analysis',
        name: 'Analysis',
        component: () => import('@/views/stat/AnalysisView.vue'),
        meta: { title: 'AI警情研判', icon: 'TrendCharts', perm: 'stat:view' }
      }
    ]
  },
  { path: '/404', name: 'NotFound', component: () => import('@/views/error/NotFound.vue'), meta: { title: '页面不存在', public: true } },
  { path: '/403', name: 'Forbidden', component: () => import('@/views/error/Forbidden.vue'), meta: { title: '权限不足', public: true } },
  { path: '/:pathMatch(.*)*', redirect: '/404' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  NProgress.start()
  const token = localStorage.getItem('accessToken')
  if (!to.meta.public && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    document.title = `${to.meta.title || '智能警务'} - 智能警务管理系统`
    next()
  }
})

router.afterEach(() => NProgress.done())

export default router
