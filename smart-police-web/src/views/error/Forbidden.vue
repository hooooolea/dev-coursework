<template>
  <div class="error-page">
    <div class="error-code">403</div>
    <div class="error-message">权限不足</div>
    <div class="error-desc">抱歉，您没有权限访问此页面</div>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<script setup>
</script>

<style scoped>
.error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 400px;
}
.error-code {
  font-size: 80px;
  font-weight: 700;
  color: #e6a23c;
  line-height: 1;
  margin-bottom: 16px;
}
.error-message {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
.error-desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 24px;
}
</style>
