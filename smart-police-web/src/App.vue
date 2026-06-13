<template>
  <router-view />
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
.el-button { font-size: 15px; }
.el-menu-item { font-size: 16px; }
.el-menu-item .el-icon { font-size: 18px; }
.el-breadcrumb { font-size: 15px; }
.el-dialog__title { font-size: 17px; font-weight: 600; }
.el-pagination { font-size: 14px; }
.el-tag { font-size: 13px; }
.el-dropdown-menu__item { font-size: 15px; }
.el-drawer__title { font-size: 17px; font-weight: 600; }
.el-tabs__item { font-size: 15px; }

::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-thumb { background: #c5c5c5; border-radius: 3px; }
</style>
