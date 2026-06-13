<template>
  <div class="analysis-page">
    <el-row :gutter="12" style="margin-bottom:12px">
      <el-col :span="6" v-for="c in statCards" :key="c.label">
        <div class="stat-card">
          <div class="sc-label">{{ c.label }}</div>
          <div class="sc-num">{{ c.value }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <!-- 左侧：分析对话 -->
      <el-col :span="16">
        <div class="card chat-panel">
          <div class="card-title"> AI 警情研判</div>
          <div class="chat-messages" ref="msgBox">
            <div v-if="messages.length === 0 && !streaming" class="chat-empty">
              <div style="font-size:40px;margin-bottom:12px"></div>
              <div style="font-size:15px;font-weight:600;margin-bottom:8px">AI 警情研判助手</div>
              <div style="font-size:14px;color:#909399;margin-bottom:16px">基于近30天数据，分析警情规律、识别风险、给出预防建议</div>
              <div class="quick-asks">
                <el-tag v-for="q in quickQuestions" :key="q" class="quick-tag" @click="ask(q)" effect="plain">
                  {{ q }}
                </el-tag>
              </div>
            </div>

            <div v-for="(m, i) in messages" :key="i" :class="['chat-msg', m.role === 'user' ? 'msg-user' : 'msg-bot']">
              <div class="msg-content">{{ m.content }}</div>
            </div>

            <div v-if="streaming" class="chat-msg msg-bot">
              <div class="msg-content">{{ streamingText }}<span class="cursor">|</span></div>
            </div>
          </div>

          <div class="chat-input">
            <div class="voice-bar">
              <el-button :icon="Microphone" @click="startVoice" :loading="recording" :disabled="!aiReady" size="small" type="warning" plain>
                {{ recording ? '停止录音' : '语音录入' }}
              </el-button>
              <el-upload
                :auto-upload="false" :show-file-list="false" :on-change="handleAudioFile"
                accept=".wav,.mp3,.m4a,.webm,audio/wav,audio/mpeg,audio/mp4,audio/webm"
              >
                <el-button size="small" :disabled="!aiReady">上传录音</el-button>
              </el-upload>
              <el-upload
                :auto-upload="false" :show-file-list="false" :on-change="handleVideoFile"
                accept=".mp4,.mov,.avi,.webm,video/mp4,video/quicktime,video/x-msvideo,video/webm"
              >
                <el-button size="small" :disabled="!aiReady">上传视频</el-button>
              </el-upload>
              <span v-if="mediaLoading" style="font-size:14px;color:#909399">AI 分析中...</span>
            </div>
            <el-input v-model="input" placeholder="输入分析问题，或使用语音录入" @keyup.enter="ask()"
              :disabled="!aiReady || streaming" size="default">
              <template #append>
                <el-button :icon="Promotion" @click="ask()" :disabled="!aiReady || streaming || !input.trim()" />
              </template>
            </el-input>
          </div>
        </div>
      </el-col>

      <!-- 右侧：概况 -->
      <el-col :span="8">
        <div class="card" style="margin-bottom:12px">
          <div class="card-title">近30天警情类型 Top8</div>
          <div ref="typeChart" style="height:220px"></div>
        </div>
        <div class="card">
          <div class="card-title">区域分布</div>
          <div ref="areaChart" style="height:220px"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Promotion, Microphone } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { statApi } from '@/api/stat'
import { dashboardApi } from '@/api/dashboard'
import { aiApi } from '@/api/ai'

const input = ref('')
const messages = ref([])
const streaming = ref(false)
const streamingText = ref('')
const msgBox = ref(null)
const typeChart = ref(null)
const areaChart = ref(null)
const aiReady = ref(false)

const statCards = reactive([
  { label: '近30天接警', value: '-' },
  { label: '在办案件', value: '-' },
  { label: '破案率', value: '-' },
  { label: '在岗警员', value: '-' }
])

const quickQuestions = [
  '近30天警情整体趋势如何？',
  '哪些区域风险最高？',
  '近期哪种案件类型增长最快？',
  '给出针对性预防建议'
]

const PIE_COLORS = ['#1a237e','#283593','#303f9f','#3949ab','#5c6bc0','#7986cb','#9fa8da','#c5cae9']

async function loadStats() {
  try {
    const [stats, dashboard] = await Promise.all([
      statApi.caseStats(1),
      dashboardApi.stats()
    ])
    const s = stats.data
    const d = dashboard.data
    statCards[0].value = d.todayAlarm ?? '-'
    statCards[1].value = d.activeCase ?? '-'
    statCards[2].value = s.solveRate != null ? s.solveRate + '%' : '-'
    statCards[3].value = d.onDutyOfficer ?? '-'

    await nextTick()
    // 类型饼图
    const tChart = echarts.init(typeChart.value)
    tChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie', radius: ['40%', '60%'], center: ['50%', '50%'],
        data: s.typeStats.map((x, i) => ({ ...x, itemStyle: { color: PIE_COLORS[i % 8] } })),
        label: { fontSize: 13 }
      }]
    })
    // 区域分布（从 typeStats 模拟，实际需要区域接口）
    const aChart = echarts.init(areaChart.value)
    aChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie', radius: ['40%', '60%'], center: ['50%', '50%'],
        data: (s.typeStats || []).slice(0, 5).map((x, i) => ({ ...x, itemStyle: { color: PIE_COLORS[i % 8] } })),
        label: { fontSize: 13 }
      }]
    })
  } catch { /* ignore */ }
}

async function checkAi() {
  try {
    const res = await aiApi.configStatus()
    aiReady.value = res.data?.data?.configured || false
  } catch { aiReady.value = false }
}

function ask(question) {
  const q = question || input.value.trim()
  if (!q || streaming.value) return
  if (!aiReady.value) { ElMessage.warning('请先在 AI配置 页面设置 API Key'); return }
  input.value = ''
  messages.value.push({ role: 'user', content: q })
  streaming.value = true
  streamingText.value = ''

  nextTick(() => scrollBottom())

  const token = localStorage.getItem('accessToken')
  fetch('/api/ai/analysis/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify({ focus: q })
  }).then(async res => {
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
            nextTick(() => scrollBottom())
          }
        }
      }
    }
    if (streamingText.value) messages.value.push({ role: 'assistant', content: streamingText.value })
  }).catch(() => {
    messages.value.push({ role: 'assistant', content: '抱歉，AI 服务暂不可用。' })
  }).finally(() => {
    streaming.value = false
    streamingText.value = ''
  })
}

function scrollBottom() {
  if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight
}

// 语音 & 视频录入
const voiceSupported = !!navigator.mediaDevices?.getUserMedia
const recording = ref(false)
const transcribing = ref(false)
const mediaLoading = ref(false)
let mediaRecorder = null
let audioChunks = []

function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result.split(',')[1])
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

async function handleAudioFile(file) {
  const raw = file.raw
  if (raw.size > 10 * 1024 * 1024) { ElMessage.warning('音频超过 10MB 限制'); return }
  mediaLoading.value = true
  try {
    const base64 = await fileToBase64(raw)
    const res = await aiApi.transcribe(base64)
    const text = res.data?.data || res.data || ''
    if (text) { input.value = text; ElMessage.success('识别完成，点击发送提问') }
  } catch { ElMessage.error('识别失败') }
  finally { mediaLoading.value = false }
}

async function handleVideoFile(file) {
  const raw = file.raw
  const maxMB = 50
  if (raw.size > maxMB * 1024 * 1024) { ElMessage.warning(`视频超过 ${maxMB}MB 限制`); return }
  mediaLoading.value = true
  try {
    const base64 = await fileToBase64(raw)
    const mime = raw.type || 'video/mp4'
    const res = await aiApi.analyzeVideo(base64, mime)
    const text = res.data?.data || res.data || ''
    if (text) {
      messages.value.push({ role: 'user', content: '[上传了一段视频进行分析]' })
      messages.value.push({ role: 'assistant', content: text })
      await nextTick(); scrollBottom()
    }
  } catch { ElMessage.error('视频分析失败') }
  finally { mediaLoading.value = false }
}

async function startVoice() {
  if (recording.value) { mediaRecorder?.stop(); recording.value = false; return }
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder = new MediaRecorder(stream, { mimeType: 'audio/webm' })
    audioChunks = []
    mediaRecorder.ondataavailable = e => audioChunks.push(e.data)
    mediaRecorder.onstop = async () => {
      stream.getTracks().forEach(t => t.stop())
      const blob = new Blob(audioChunks, { type: 'audio/webm' })
      const base64 = await fileToBase64(blob)
      mediaLoading.value = true
      try {
        const res = await aiApi.transcribe(base64)
        const text = res.data?.data || res.data || ''
        if (text) { input.value = text; ElMessage.success('识别完成，点击发送提问') }
      } catch { ElMessage.error('识别失败') }
      finally { mediaLoading.value = false }
    }
    mediaRecorder.start(); recording.value = true
  } catch { ElMessage.error('无法访问麦克风') }
}

onMounted(() => { loadStats(); checkAi() })
</script>

<style scoped>
.analysis-page { display: flex; flex-direction: column; gap: 12px; }

.stat-card {
  background: #fff; border: 1px solid #e4e7ed; border-radius: 6px;
  padding: 20px 24px; display: flex; flex-direction: column; justify-content: center;
  min-height: 88px;
}
.sc-label { font-size: 13px; color: #909399; margin-bottom: 8px; }
.sc-num { font-size: 28px; font-weight: 700; color: #303133; line-height: 1.2; }

.card {
  background: #fff; border: 1px solid #e4e7ed; border-radius: 3px; padding: 16px;
}
.card-title { font-size: 13px; font-weight: 600; color: #303133; margin-bottom: 12px; }

.chat-panel { display: flex; flex-direction: column; height: 500px; }
.chat-messages { flex: 1; overflow-y: auto; padding: 8px 0; display: flex; flex-direction: column; gap: 10px; }
.chat-empty { text-align: center; padding: 20px 0; }

.quick-asks { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.quick-tag { cursor: pointer; font-size: 13px; }
.quick-tag:hover { background: #1a237e; color: #fff; border-color: #1a237e; }

.chat-msg { max-width: 85%; }
.msg-user { align-self: flex-end; }
.msg-bot { align-self: flex-start; }
.msg-content {
  padding: 10px 16px; border-radius: 12px; font-size: 13px; line-height: 1.7; white-space: pre-wrap;
}
.msg-user .msg-content { background: #1a237e; color: #fff; border-bottom-right-radius: 4px; }
.msg-bot .msg-content { background: #f0f2f5; color: #303133; border-bottom-left-radius: 4px; }

.cursor { animation: blink 0.8s infinite; color: #909399; }
@keyframes blink { 0%,100% { opacity:1 } 50% { opacity:0 } }

.chat-input { margin-top: 12px; padding-top: 12px; border-top: 1px solid #f0f0f0; }
.voice-bar { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
</style>
