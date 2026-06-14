<template>
  <router-view v-slot="{ Component }">
    <transition name="page-fade">
      <component :is="Component" />
    </transition>
  </router-view>
  <AiAssistant />
</template>

<script setup>
import AiAssistant from '@/components/AiAssistant.vue'
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }

html { font-size: 16px; }

body {
  font-family: -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 15px;
  line-height: 1.7;
  background: #f5f6f8;
  color: #303133;
}

/* Element Plus 全局字号覆盖 */
.el-table { font-size: 15px; }
.el-table th.el-table__cell { font-size: 14px; color: #606266; font-weight: 600; }
.el-form-item__label { font-size: 15px; }
.el-input__inner { font-size: 15px; }
.el-select .el-input__inner { font-size: 15px; }
.el-button { font-size: 15px; transition: transform 0.1s ease; }
.el-button:active { transform: scale(0.96); }
.el-menu-item { font-size: 16px; }
.el-menu-item .el-icon { font-size: 18px; }
.el-breadcrumb { font-size: 15px; }
.el-dialog__title { font-size: 17px; font-weight: 600; }
.el-pagination { font-size: 14px; }
.el-tag { font-size: 13px; transition: transform 0.15s ease; }
.el-tag:hover { transform: scale(1.05); }
.el-dropdown-menu__item { font-size: 15px; }
.el-drawer__title { font-size: 17px; font-weight: 600; }
.el-tabs__item { font-size: 15px; }

/* 表格行 hover */
.el-table__body tr { transition: background-color 0.15s ease; }
.el-table__body tr:hover > td { background-color: #f0f2fc !important; box-shadow: inset 3px 0 0 #1a237e; }

/* 弹窗滑入 */
.el-dialog { animation: dialogIn 0.25s ease-out; }
@keyframes dialogIn { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }

/* 页面切换 */
.page-fade-enter-active, .page-fade-leave-active { transition: opacity 0.12s ease; }
.page-fade-enter-from, .page-fade-leave-to { opacity: 0; }

/* 空状态呼吸 */
.el-empty__image { animation: breathe 2s ease-in-out infinite; }
@keyframes breathe { 0%,100% { transform: scale(1); } 50% { transform: scale(1.03); } }

/* 滚动条 */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #c5c5c5; border-radius: 3px; }
::-webkit-scrollbar-thumb:hover { background: #a0a0a0; }

/* 卡片间距 */
.el-row { row-gap: 16px; }
</style>
