<template>
  <div class="ai-assistant">
    <!-- 收起态 -->
    <div v-if="!open" class="ai-fab" @click="openChat">
      <el-icon size="22"><ChatDotRound /></el-icon>
      <div v-if="hasNotification" class="ai-dot" />
    </div>

    <!-- 展开态 -->
    <div v-else class="ai-panel">
      <div class="ai-panel-header">
        <span> AI 警务助手</span>
        <div style="display:flex;align-items:center;gap:8px">
          <el-button v-if="!configured" type="warning" link size="small" @click="$router.push('/system/ai-config')">配置Key</el-button>
          <el-icon class="ai-close" size="18" @click="open = false"><Close /></el-icon>
        </div>
      </div>

      <div class="ai-messages" ref="msgBox">
        <template v-if="messages.length === 0 && !streaming">
          <div class="ai-welcome">
            <div class="ai-welcome-icon"></div>
            <div style="font-size:14px;font-weight:600;margin-bottom:8px">你好！我是 AI 警务助手</div>
            <div style="font-size:12px;color:#909399;margin-bottom:12px">试试问我：</div>
            <div class="ai-hints">
              <div v-for="h in hints" :key="h" class="ai-hint" @click="sendHint(h)">{{ h }}</div>
            </div>
          </div>
        </template>

        <div v-for="(m, i) in messages" :key="i" :class="['ai-msg', m.role === 'user' ? 'ai-msg-user' : 'ai-msg-bot']">
          <div class="ai-msg-content" v-html="renderMd(m.content)"></div>
        </div>

        <div v-if="streaming" class="ai-msg ai-msg-bot">
          <div class="ai-msg-content" v-html="renderMd(streamingText) + '<span class=ai-cursor>|</span>'"></div>
        </div>
      </div>

      <div class="ai-input-area">
        <el-input
          v-model="input"
          placeholder="输入问题..."
          @keyup.enter="send"
          :disabled="!configured || streaming"
          size="small"
        />
        <el-button type="primary" :icon="Promotion" circle size="small" @click="send"
          :disabled="!configured || streaming || !input.trim()" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ChatDotRound, Close, Promotion } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { aiApi } from '@/api/ai'

const open = ref(false)
const configured = ref(false)
const hasNotification = ref(true)
const input = ref('')
const messages = ref([])
const streaming = ref(false)
const streamingText = ref('')
const msgBox = ref(null)

const hints = [
  '今天接了多少警情？',
  '目前在办案件有多少？',
  '现在有多少警员在岗？',
  '近30天哪个区域警情最多？'
]

function renderMd(text) {
  if (!text) return ''
  let html = text.replace(/\r\n/g, '\n').replace(/\r/g, '\n')
  html = html.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/^### (.+)$/gm, '<h4>$1</h4>')
  html = html.replace(/^## (.+)$/gm, '<h4>$1</h4>')
  html = html.replace(/^# (.+)$/gm, '<h4>$1</h4>')
  html = html.replace(/^---$/gm, '<hr>')
  html = html.replace(/^[\*\-] (.+)$/gm, '<li>$1</li>')
  html = html.replace(/^\d+[\.\)] (.+)$/gm, '<li>$2</li>')
  html = html.replace(/((?:<li>.*<\/li>\n?)+)/g, '<ul>$1</ul>')
  html = html.replace(/\n/g, '<br>')
  return html
}

async function openChat() {
  open.value = true
  hasNotification.value = false
  try {
    const res = await aiApi.configStatus()
    configured.value = res.data?.data?.configured || false
  } catch { configured.value = false }
}

function sendHint(hint) {
  input.value = hint
  send()
}

async function send() {
  const text = input.value.trim()
  if (!text || streaming.value) return
  if (!configured.value) {
    ElMessage.warning('请先在 AI配置 页面设置 MiMo API Key')
    return
  }
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  streaming.value = true
  streamingText.value = ''

  await nextTick()
  scrollBottom()

  const token = localStorage.getItem('accessToken')
  try {
    const res = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
      body: JSON.stringify({ message: text })
    })
    const reader = res.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const d = line.slice(5).trim()
          if (d && d !== '[DONE]') {
            streamingText.value += d
            await nextTick()
            scrollBottom()
          }
        }
      }
    }
    if (streamingText.value) {
      messages.value.push({ role: 'assistant', content: streamingText.value })
    }
  } catch {
    messages.value.push({ role: 'assistant', content: '抱歉，AI 服务暂不可用，请检查网络或 API Key。' })
  } finally {
    streaming.value = false
    streamingText.value = ''
  }
}

function scrollBottom() {
  if (msgBox.value) {
    msgBox.value.scrollTop = msgBox.value.scrollHeight
  }
}
</script>

<style scoped>
.ai-assistant { position: fixed; bottom: 20px; right: 20px; z-index: 2000; }

.ai-fab {
  width: 52px; height: 52px; border-radius: 50%;
  background: #1a237e; color: #fff;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; box-shadow: 0 4px 16px rgba(26,35,126,0.35);
  position: relative; transition: transform 0.2s;
}
.ai-fab:hover { transform: scale(1.08); }
.ai-dot {
  position: absolute; top: 4px; right: 4px;
  width: 10px; height: 10px; border-radius: 50%;
  background: #f56c6c; border: 2px solid #fff;
}

.ai-panel {
  width: 380px; height: 520px; background: #fff; border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.18);
  display: flex; flex-direction: column; overflow: hidden;
}
.ai-panel-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px; background: #1a237e; color: #fff;
  font-size: 14px; font-weight: 600;
}
.ai-close { cursor: pointer; opacity: 0.8; }
.ai-close:hover { opacity: 1; }

.ai-messages {
  flex: 1; overflow-y: auto; padding: 12px;
  display: flex; flex-direction: column; gap: 8px;
}
.ai-welcome { text-align: center; padding: 20px 0; }
.ai-welcome-icon { font-size: 48px; margin-bottom: 8px; }
.ai-hints { display: flex; flex-wrap: wrap; gap: 6px; justify-content: center; }
.ai-hint {
  padding: 4px 12px; background: #f0f2f5; border-radius: 14px;
  font-size: 14px; color: #606266; cursor: pointer; transition: background 0.2s;
}
.ai-hint:hover { background: #e4e7ed; }

.ai-msg { max-width: 85%; }
.ai-msg-user { align-self: flex-end; }
.ai-msg-bot  { align-self: flex-start; }
.ai-msg-content {
  padding: 8px 14px; border-radius: 14px; font-size: 13px;
  line-height: 1.6;
}
.ai-msg-content ul { padding-left: 18px; margin: 4px 0; }
.ai-msg-content li { margin: 2px 0; }
.ai-msg-content h4 { font-size: 14px; margin: 8px 0 4px; }
.ai-msg-content hr { border: none; border-top: 1px solid #e4e7ed; margin: 10px 0; }
.ai-msg-content strong { color: #1a237e; }
.ai-msg-user .ai-msg-content { background: #1a237e; color: #fff; border-bottom-right-radius: 4px; }
.ai-msg-bot  .ai-msg-content { background: #f0f2f5; color: #303133; border-bottom-left-radius: 4px; }

.ai-cursor { animation: blink 0.8s infinite; color: #909399; }
@keyframes blink { 0%,100% { opacity:1; } 50% { opacity:0; } }

.ai-input-area {
  display: flex; gap: 8px; padding: 10px 12px;
  border-top: 1px solid #f0f0f0;
}
</style>
