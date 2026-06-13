const authHeader = () => ({
  Authorization: `Bearer ${localStorage.getItem('accessToken')}`
})

function ssePost(path, body, onToken, onDone) {
  const token = localStorage.getItem('accessToken')
  fetch(`/api${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify(body)
  }).then(async res => {
    const reader = res.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) { onDone?.(); break }
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const d = line.slice(5).trim()
          if (d && d !== '[DONE]') onToken?.(d)
        }
      }
    }
  }).catch(() => onDone?.())
}

import axios from 'axios'
export const aiApi = {
  scheduleRecommend: (data, onToken, onDone) => ssePost('/ai/schedule/recommend', data, onToken, onDone),
  equipmentRecommend: (data) => axios.post('/api/ai/equipment/recommend', data, { headers: authHeader() }),
  configStatus: () => axios.get('/api/ai/config/key', { headers: authHeader() }),

  // 多模态
  describeImage: (base64Image) =>
    axios.post('/api/ai/omni/describe-image', { image: base64Image }, { headers: authHeader() }),
  summarizeVideo: (videoUrl) =>
    axios.post('/api/ai/omni/summarize-video', { videoUrl }, { headers: authHeader() }),
  analyzeVideo: (base64Video, mimeType = 'video/mp4') =>
    axios.post('/api/ai/omni/analyze-video', { video: base64Video, mimeType }, { headers: authHeader() }),

  // 语音识别
  transcribe: (audioBase64, format = 'wav') =>
    axios.post('/api/ai/asr/transcribe', { audio: audioBase64, format }, { headers: authHeader() }),
  extractAlarm: (audioBase64) =>
    axios.post('/api/ai/asr/extract', { audio: audioBase64 }, { headers: authHeader() }),

  // 语音合成
  speak: (text, voice = '苏打', emotion = 'normal') =>
    axios.post('/api/ai/tts/speak', { text, voice, emotion }, { headers: authHeader(), responseType: 'text' })
}
