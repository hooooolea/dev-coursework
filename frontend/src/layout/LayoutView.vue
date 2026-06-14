<template>
  <el-container class="layout-wrap">
    <el-aside :width="isCollapse ? '56px' : '210px'" class="sidebar">
      <div class="sidebar-logo">
        <el-icon size="18" color="#fff"><Platform /></el-icon>
        <span v-show="!isCollapse" class="logo-text">智能警务系统</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        background-color="#1a237e"
        text-color="rgba(255,255,255,0.75)"
        active-text-color="#ffffff"
        router
      >
        <template v-for="item in menuList" :key="item.path ?? item.title">
          <!-- 子菜单（可折叠） -->
          <el-sub-menu v-if="item.children" :index="item.title">
            <template #title>
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.title }}</span>
            </template>
            <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
              <template #title>{{ child.title }}</template>
            </el-menu-item>
          </el-sub-menu>
          <!-- 分组标题 -->
          <div v-else-if="item.group" v-show="!isCollapse" class="menu-group-title">{{ item.title }}</div>
          <div v-else-if="item.group && isCollapse" class="menu-divider"></div>
          <!-- 普通菜单项 -->
          <el-menu-item v-else :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" /><Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="28" style="background:#1a237e;font-size:12px">
                {{ authStore.userInfo?.realName?.charAt(0) }}
              </el-avatar>
              <span class="user-name">{{ authStore.userInfo?.realName }}</span>
              <el-icon size="12"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Platform } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const isCollapse = ref(false)

const menuList = [
  { path: '/dashboard',     title: '警情驾驶舱', icon: 'Monitor' },
  { path: '/alarm',         title: '报警受理',   icon: 'Bell' },
  { path: '/case',          title: '案件管理',   icon: 'Folder' },
  { path: '/person',        title: '人员档案',   icon: 'User' },
  { path: '/vehicle',       title: '车辆管理',   icon: 'Van' },
  { path: '/patrol',          title: '巡逻调度',   icon: 'Location' },
  { path: '/patrol/schedule', title: '排班管理',   icon: 'Calendar' },
  { path: '/officer',         title: '警力资源',   icon: 'Avatar' },
  { path: '/equipment',       title: '装备管理',   icon: 'Box' },
  { path: '/stat',            title: '统计分析',   icon: 'DataAnalysis' },
  { path: '/analysis',       title: 'AI警情研判', icon: 'TrendCharts' },
  { title: '系统管理', icon: 'Setting', children: [
    { path: '/system/user', title: '用户管理' },
    { path: '/system/role', title: '角色管理' },
    { path: '/system/dept', title: '部门管理' },
    { path: '/system/log',  title: '操作日志' },
    { path: '/system/ai-config', title: 'AI配置' }
  ] }
]

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => {
  // 先查一级菜单
  const flat = menuList.filter(m => m.path).find(m => route.path.startsWith(m.path))
  if (flat) return flat.title
  // 再查子菜单项（如系统管理下的子路由）
  for (const item of menuList) {
    if (item.children) {
      const child = item.children.find(c => route.path.startsWith(c.path))
      if (child) return child.title
    }
  }
  return ''
})

async function handleCommand(cmd) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
    await authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-wrap { height: 100vh; }

.sidebar {
  background: #1a237e;
  transition: width 0.25s;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sidebar-logo {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 10px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  overflow: hidden;
  white-space: nowrap;
  flex-shrink: 0;
}

.logo-text {
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.el-menu { border-right: none; flex:1; overflow-y: auto; }
.el-menu::-webkit-scrollbar { width: 4px; }
.el-menu::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.2); border-radius: 2px; }
.el-menu-item { height: 48px; font-size: 14px; transition: border-left 0.2s ease; }
.el-menu-item.is-active { background: rgba(255,255,255,0.15) !important; border-left: 3px solid #fff; }
.el-menu-item:hover { background: rgba(255,255,255,0.08) !important; }

.menu-group-title {
  padding: 16px 20px 4px;
  font-size: 14px;
  color: rgba(255,255,255,0.4);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  user-select: none;
}
.menu-divider {
  margin: 8px 12px;
  border-top: 1px solid rgba(255,255,255,0.12);
}

.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 56px;
}

.header-left { display: flex; align-items: center; gap: 14px; }

.collapse-btn {
  font-size: 18px; cursor: pointer; color: #606266;
  transition: transform 0.3s ease, color 0.2s;
}
.collapse-btn:hover { color: #1a237e; transform: scale(1.1); }

.header-right { display: flex; align-items: center; }

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 3px;
}
.user-info:hover { background: #f5f6f8; }
.user-name { font-size: 13px; color: #303133; }

.main-content {
  background: #f5f6f8;
  padding: 16px;
  overflow-y: auto;
}
</style>
