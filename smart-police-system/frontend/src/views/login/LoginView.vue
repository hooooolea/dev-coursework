<template>
  <div class="login-wrap">
    <div class="login-box">
      <div class="login-header">
        <el-icon size="40" color="#1a237e"><Platform /></el-icon>
        <h1>智能警务管理系统</h1>
        <p>Public Security Intelligence Platform</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" clearable class="login-input" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" placeholder="密码" size="large" :prefix-icon="Lock" type="password" show-password clearable class="login-input" />
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
          登录
        </el-button>
      </el-form>

      <p class="hint">默认账号：admin / 123456</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Platform } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  min-height: 100vh;
  background: #f5f6f8;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-box {
  width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 48px 40px 36px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h1 {
  font-size: 18px;
  color: #1a237e;
  font-weight: 600;
  margin: 12px 0 4px;
}

.login-header p {
  font-size: 14px;
  color: #909399;
}

.login-input :deep(.el-input__wrapper) {
  transition: box-shadow 0.25s ease;
}
.login-input :deep(.el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 2px rgba(26,35,126,0.15);
}

.login-btn {
  width: 100%;
  margin-top: 4px;
  background: #1a237e;
  border-color: #1a237e;
  transition: background 0.2s;
}
.login-btn:hover { background: #283593; border-color: #283593; }
.login-btn:active { transform: scale(0.98); }

.hint {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #909399;
}
</style>
