<template>
  <div>
    <el-card shadow="never" class="alarm-page">
      <div class="page-header">
        <el-icon size="20" color="#1565c0"><Bell /></el-icon>
        <span class="page-title">报警受理</span>
        <span class="page-desc">接警录入 · 任务派发 · 处置跟踪</span>
      </div>
      <!-- 搜索栏 -->
      <el-form inline :model="query" class="search-form">
        <el-form-item label="范围">
          <el-radio-group v-model="queryScope" size="small" @change="onScopeChange">
            <el-radio-button value="all">全部</el-radio-button>
            <el-radio-button value="mine">我的警情</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:120px">
            <el-option label="待处置" :value="1" />
            <el-option label="处置中" :value="2" />
            <el-option label="已处置" :value="3" />
            <el-option label="已关闭" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
          <el-button :icon="Plus" @click="openCreate">接警录入</el-button>
          <el-button type="success" plain @click="runDemo">演示流程</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="list" v-loading="loading" stripe>
        <template #empty>
          <el-empty description="暂无数据" :image-size="80" />
        </template>
        <el-table-column prop="alarmNo" label="警情编号" width="150" />
        <el-table-column label="警情类型" width="110">
          <template #default="{ row }">{{ alarmTypeLabel(row.alarmType) }}</template>
        </el-table-column>
        <el-table-column prop="callerName" label="报警人" width="90" />
        <el-table-column prop="locationDetail" label="事发地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="urgencyLevel" label="紧急程度" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="urgencyType(row.urgencyLevel)" size="small">{{ urgencyLabel(row.urgencyLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alarmTime" label="报警时间" width="160" />
        <el-table-column label="关联" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.relatedCaseId" type="success" size="small" style="cursor:pointer" @click="$router.push('/case')">
              已立案 #{{ row.relatedCaseId }}
            </el-tag>
            <span v-else style="color:#c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="warning" link size="small" @click="openDispatch(row)">派发</el-button>
            <el-button v-if="row.status === 1" type="success" link size="small" @click="upgradeToCase(row)">升级案件</el-button>
            <el-button v-if="[1,2,3].includes(row.status)" type="danger" link size="small" @click="handleClose(row)">关闭</el-button>
            <el-button type="danger" link size="small" @click="handleDelAlarm(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top:16px;justify-content:flex-end"
        @current-change="loadList"
      />

    </el-card>

    <!-- AI 装备推荐弹窗 -->
    <el-dialog v-model="equipVisible" title="AI 智能分析" width="520px" :close-on-click-modal="false">
      <div v-if="equipLoading" style="text-align:center;padding:30px">
        <el-icon class="is-loading" size="40" color="#1a237e"><Loading /></el-icon>
        <div style="margin-top:16px;font-size:15px;color:#303133">AI 正在分析警情，推荐装备方案...</div>
        <div style="margin-top:8px;font-size:13px;color:#909399">预计需要 10-30 秒</div>
      </div>
      <div v-else-if="equipResult">
        <div style="font-size:14px;color:#909399;margin-bottom:16px">警情 #{{ equipAlarmId }} · AI 智能分析</div>
        <div v-if="equipResult.must && equipResult.must.length" style="margin-bottom:12px">
          <div style="font-weight:600;color:#f56c6c;margin-bottom:6px"> 必带装备</div>
          <div v-for="e in equipResult.must" :key="e.name" style="display:flex;align-items:center;gap:8px;padding:6px 0;border-bottom:1px solid #fafafa">
            <el-tag type="danger" size="small">{{ e.name }}</el-tag>
            <span style="font-size:13px;color:#606266">{{ e.reason }}</span>
          </div>
        </div>
        <div v-if="equipResult.suggested && equipResult.suggested.length" style="margin-bottom:12px">
          <div style="font-weight:600;color:#67c23a;margin-bottom:6px"> 建议携带</div>
          <div v-for="e in equipResult.suggested" :key="e.name" style="display:flex;align-items:center;gap:8px;padding:6px 0;border-bottom:1px solid #fafafa">
            <el-tag type="success" size="small">{{ e.name }}</el-tag>
            <span style="font-size:13px;color:#606266">{{ e.reason }}</span>
          </div>
        </div>
        <div v-if="equipResult.summary" style="font-size:13px;color:#909399;padding-top:10px;border-top:1px dashed #e4e7ed;line-height:1.6">{{ equipResult.summary }}</div>
        <div v-if="!equipResult.must && !equipResult.suggested && equipResult.summary" style="font-size:13px;color:#606266;padding:12px;background:#fafafa;border-radius:6px;white-space:pre-wrap;line-height:1.7">{{ equipResult.summary }}</div>
      </div>
      <template #footer>
        <el-button @click="equipVisible = false">关闭</el-button>
        <el-button type="success" @click="autoCreateCase" :disabled="!equipAlarmId">一键立案</el-button>
      </template>
    </el-dialog>

    <!-- 接警录入对话框 -->
    <el-dialog v-model="createVisible" title="接警录入" width="600px">
      <div style="margin-bottom:12px">
        <el-button size="small" @click="fillDemo">一键填入示例</el-button>
      </div>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="报警时间" prop="alarmTime">
          <el-date-picker v-model="createForm.alarmTime" type="datetime" style="width:100%" />
        </el-form-item>
        <el-form-item label="报警人">
          <el-input v-model="createForm.callerName" />
        </el-form-item>
        <el-form-item label="联系电话" prop="callerPhone">
          <el-input v-model="createForm.callerPhone" />
        </el-form-item>
        <el-form-item label="事发地址">
          <el-input v-model="createForm.locationDetail" />
        </el-form-item>
        <el-form-item label="警情类型" prop="alarmType">
          <el-select v-model="createForm.alarmType" style="width:100%">
            <el-option v-for="d in alarmTypes" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="紧急程度">
          <el-radio-group v-model="createForm.urgencyLevel">
            <el-radio :value="1">一般</el-radio>
            <el-radio :value="2">较紧急</el-radio>
            <el-radio :value="3">紧急</el-radio>
            <el-radio :value="4">特急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="警情描述" prop="alarmDesc">
          <el-input v-model="createForm.alarmDesc" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">提交接警</el-button>
      </template>
    </el-dialog>

    <!-- 派发对话框 -->
    <el-dialog v-model="dispatchVisible" title="任务派发" width="480px">
      <el-form label-width="80px">
        <el-form-item label="警情编号"><strong>{{ dispatchTarget?.alarmNo }}</strong></el-form-item>
        <el-form-item label="选择警员">
          <el-select v-model="dispatchOfficerId" filterable placeholder="搜索警员" style="width:100%" @visible-change="loadOfficers">
            <el-option v-for="o in officerList" :key="o.id" :label="`${o.realName}（${o.badgeNo}）— ${statusLabel(o.workStatus)}`" :value="o.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dispatchVisible = false">取消</el-button>
        <el-button type="primary" :loading="dispatching" @click="handleDispatch">确认派发</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Search, Plus, Loading, Bell } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { alarmApi } from '@/api/alarm'
import { dictApi } from '@/api/dict'
import { officerApi } from '@/api/officer'
import { caseApi } from '@/api/caseinfo'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ status: null, urgencyLevel: null, page: 1, size: 20 })
const queryScope = ref('all')
function onScopeChange() { query.page = 1; loadList() }

const createVisible = ref(false)
const submitting = ref(false)
const createFormRef = ref()
const alarmTypes = ref([])

const createForm = reactive({
  alarmTime: new Date(),
  callerName: '',
  callerPhone: '',
  locationDetail: '',
  alarmType: '',
  urgencyLevel: 2,
  alarmDesc: ''
})

const createRules = {
  alarmTime: [{ required: true, message: '请选择报警时间' }],
  callerPhone: [{ required: true, message: '请输入联系电话' }],
  alarmType: [{ required: true, message: '请选择警情类型' }],
  alarmDesc: [{ required: true, message: '请填写警情描述' }]
}

const urgencyLabel = (v) => ['', '一般', '较紧急', '紧急', '特急'][v] || '-'
const urgencyType  = (v) => ['', 'info', 'warning', 'danger', 'danger'][v] || 'info'
const statusLabel  = (v) => ({ 1: '待处置', 2: '处置中', 3: '已处置', 4: '已关闭' })[v] || '-'
const alarmTypeLabel = (v) => alarmTypes.value.find(d => d.dictValue === v)?.dictLabel || v || '-'
const statusType   = (v) => ({ 1: 'danger', 2: 'warning', 3: 'success', 4: 'info' })[v] || 'info'

async function loadList() {
  loading.value = true
  try {
    const apiMethod = queryScope.value === 'mine' ? alarmApi.myTasks : alarmApi.list
    const res = await apiMethod(query)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function fillDemo() {
  Object.assign(createForm, {
    alarmTime: new Date(),
    callerName: '李女士',
    callerPhone: '13900001234',
    locationDetail: '朝阳区三里屯酒吧街18号',
    alarmType: 'theft',
    urgencyLevel: 2,
    alarmDesc: '顾客在酒吧消费时发现随身挎包被盗，内有现金3000元、身份证及银行卡数张。嫌疑人疑似趁其起身跳舞时从后方拿走，已逃离现场约10分钟。'
  })
}

async function runDemo() {
  await openCreate()
  await nextTick()
  fillDemo()
  await nextTick()
  await handleCreate()
  // 自动派发给张建国 (officer ID 2)
  setTimeout(async () => {
    try {
      const res = await alarmApi.list({ page: 1, size: 1, status: 1 })
      const latest = res?.data?.records?.[0]
      if (latest) {
        await alarmApi.dispatch(latest.id, 10)
        ElMessage.success('已自动派发给张明（P010）')
        loadList()
        setTimeout(() => fetchEquipRecommend(latest.id), 500)
      } else {
        console.warn('没有找到待派发警情')
      }
    } catch (e) {
      console.error('自动派发失败', e)
    }
  }, 1500)
}

function openCreate() {
  createVisible.value = true
}

async function handleCreate() {
  await createFormRef.value.validate()
  submitting.value = true
  try {
    const res = await alarmApi.create(createForm)
    const alarmId = res.data
    lastAlarmData.value = { ...createForm }
    ElMessage.success('接警录入成功')
    createVisible.value = false
    loadList()
    if (alarmId) {
      setTimeout(() => fetchEquipRecommend(alarmId), 500)
    }
  } finally {
    submitting.value = false
  }
}

function viewDetail(row) {
  ElMessage.info(`警情编号：${row.alarmNo}`)
}

// 派发
const dispatchVisible = ref(false)
const dispatchTarget = ref(null)
const dispatchOfficerId = ref(null)
const dispatching = ref(false)
const officerList = ref([])

async function loadOfficers(show) {
  if (show) {
    try {
      const res = await officerApi.list({ workStatus: 'on_duty', page: 1, size: 50 })
      officerList.value = res.data?.records || []
    } catch { /* ignore */ }
  }
}

function openDispatch(row) {
  dispatchTarget.value = row
  dispatchOfficerId.value = null
  dispatchVisible.value = true
}

async function handleDispatch() {
  if (!dispatchOfficerId.value) { ElMessage.warning('请选择警员'); return }
  dispatching.value = true
  try {
    await alarmApi.dispatch(dispatchTarget.value.id, dispatchOfficerId.value)
    ElMessage.success('派发成功')
    dispatchVisible.value = false
    loadList()
  } finally { dispatching.value = false }
}

// 升级为案件
async function upgradeToCase(row) {
  await ElMessageBox.confirm(`将警情 ${row.alarmNo} 升级为案件？系统将自动生成案件编号并关联该警情。`, '确认升级', { type: 'warning' })
  try {
    const caseData = {
      caseName: (row.alarmTypeLabel || row.alarmType) + '案件',
      caseCategory: row.alarmType === 'traffic' ? 'traffic' : 'criminal',
      caseType: row.alarmType || 'other',
      occurredAt: row.alarmTime || new Date().toISOString(),
      locationDetail: row.locationDetail || '',
      caseDesc: row.alarmDesc || '',
      severityLevel: row.urgencyLevel || 2
    }
    const caseRes = await caseApi.create(caseData)
    const caseId = caseRes.data
    // 关联警情与案件（不关闭警情，案件继续侦查）
    await alarmApi.linkCase(row.id, caseId)
    ElMessage.success(`已升级为案件 #${caseId}，原警情关联保持处置中`)
    loadList()
  } catch (e) { ElMessage.error('升级失败: ' + (e.message || '')) }
}

async function handleClose(row) {
  const { value } = await ElMessageBox.prompt('请填写处置结果摘要', '关闭警情', { inputType: 'textarea' })
  if (!value) return
  await alarmApi.close(row.id, value)
  ElMessage.success('警情已关闭')
  loadList()
}

async function handleDelAlarm(row) {
  await ElMessageBox.confirm(`确定删除警情「${row.alarmNo}」？此操作不可恢复。`, '确认删除', { type: 'warning' })
  await alarmApi.del(row.id)
  ElMessage.success('已删除')
  loadList()
}

// AI 装备推荐
const equipVisible = ref(false)
const equipLoading = ref(false)
const equipResult = ref(null)
const equipAlarmId = ref(null)

async function fetchEquipRecommend(alarmId) {
  if (!alarmId) return
  equipVisible.value = true
  equipLoading.value = true
  equipResult.value = null
  equipAlarmId.value = alarmId
  try {
    const res = await alarmApi.equipmentRecommend(alarmId)
    let raw = res.data || res
    if (typeof raw !== 'string') raw = JSON.stringify(raw)
    // 清理 markdown 代码块包裹（开头和结尾的 ```json / ```）
    raw = raw.replace(/^```(?:json)?\s*\n?/gm, '').replace(/\n?```\s*$/gm, '').trim()
    try {
      equipResult.value = JSON.parse(raw)
    } catch {
      // 解析失败则展示原文
      equipResult.value = { must: [], suggested: [], summary: raw }
    }
  } catch (e) {
    equipResult.value = { must: [], suggested: [], summary: 'AI 服务暂不可用，请检查 API Key 配置' }
  } finally { equipLoading.value = false }
}

const lastAlarmData = ref(null)

async function autoCreateCase() {
  const alarm = lastAlarmData.value || {}
  const token = localStorage.getItem('accessToken')
  try {
    const payload = {
      caseName: (alarm.alarmType || '警情') + '案件',
      caseCategory: (alarm.alarmType === 'traffic' ? 'traffic' : 'criminal'),
      caseType: alarm.alarmType || 'other',
      occurredAt: alarm.alarmTime instanceof Date ? alarm.alarmTime.toISOString() : (alarm.alarmTime || new Date().toISOString()),
      locationDetail: alarm.locationDetail || '未知地址',
      caseDesc: alarm.alarmDesc || '无描述',
      severityLevel: Number(alarm.urgencyLevel) || 2
    }
    const res = await fetch('/api/case', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token },
      body: JSON.stringify(payload)
    })
    const data = await res.json()
    if (data.code !== 200) throw new Error(data.message || '创建失败')
    await alarmApi.linkCase(equipAlarmId.value, data.data)
    ElMessage.success(`已立案 #${data.data}`)
    equipVisible.value = false
    loadList()
  } catch (e) { ElMessage.error('立案失败: ' + (e.message || '请重试')) }
}

onMounted(async () => {
  alarmTypes.value = await dictApi.get('alarm_type')
  loadList()
})
</script>

<style scoped>
.search-form { margin-bottom: 0; }
</style>
