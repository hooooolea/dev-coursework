<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <span style="font-weight:600">AI 配置</span>
      </template>

      <el-alert
        :type="configured ? 'success' : 'warning'"
        :title="configured ? 'API Key 已配置' : '尚未配置 API Key，AI 功能不可用'"
        :closable="false"
        show-icon
        style="margin-bottom:20px"
      />

      <el-form label-width="100px" style="max-width:520px">
        <el-form-item label="API Key">
          <el-input
            v-model="apiKey"
            type="password"
            show-password
            placeholder="请输入 MiMo API Key（sk-...）"
          />
        </el-form-item>
        <el-form-item v-if="maskedKey" label="当前 Key">
          <el-tag type="info">{{ maskedKey }}</el-tag>
          <span style="font-size:12px;color:#909399;margin-left:8px">仅显示前后各 3 位</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
          <el-button @click="handleClear" :disabled="!apiKey.trim() && !configured">清除</el-button>
        </el-form-item>
      </el-form>

      <el-divider />
      <div style="font-size:12px;color:#909399">
        API Key 获取地址：<a href="https://platform.xiaomimimo.com" target="_blank">小米 MiMo 开放平台</a> → Token Plan<br/>
        已支持模型：Pro（文本推理）· V2.5（全模态）· ASR（语音识别）· TTS（语音合成）<br/>
        套餐 ¥39/月起，每日 00:00-08:00 八折
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const apiKey = ref('')
const maskedKey = ref('')
const configured = ref(false)
const saving = ref(false)

const authHeader = () => ({
  Authorization: `Bearer ${localStorage.getItem('accessToken')}`
})

async function loadStatus() {
  try {
    const res = await axios.get('/api/ai/config/key', { headers: authHeader() })
    configured.value = res.data.data?.configured || false
    maskedKey.value = res.data.data?.maskedKey || ''
  } catch { /* ignore */ }
}

async function handleSave() {
  const val = apiKey.value.trim()
  saving.value = true
  try {
    await axios.post('/api/ai/config/key', { apiKey: val },
      { headers: { ...authHeader(), 'Content-Type': 'application/json' } })
    ElMessage.success(val ? 'API Key 已保存' : 'API Key 已清除')
    apiKey.value = ''
    loadStatus()
  } catch { ElMessage.error('保存失败') }
  finally { saving.value = false }
}

async function handleClear() {
  await ElMessageBox.confirm('确定清除 API Key？AI 功能将不可用。', '确认', { type: 'warning' })
  apiKey.value = ''
  await handleSave()
}

onMounted(loadStatus)
</script>
