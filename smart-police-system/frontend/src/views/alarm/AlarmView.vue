<template>
  <div>
    <el-card shadow="never">
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
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 1" type="warning" link size="small" @click="openDispatch(row)">派发</el-button>
            <el-button v-if="row.status === 2" type="success" link size="small" @click="handleArrive(row)">现场打卡</el-button>
            <el-button v-if="row.status === 1" type="success" link size="small" @click="upgradeToCase(row)">升级案件</el-button>
            <el-button v-if="[1,2,3].includes(row.status)" type="danger" link size="small" @click="handleClose(row)">关闭</el-button>
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
        <div style="font-size:14px;color:#909399;margin-bottom:12px">警情 #{{ equipAlarmId }} 装备推荐</div>
        <div v-if="equipResult.must && equipResult.must.length" style="margin-bottom:10px">
          <span style="font-weight:600;color:#f56c6c">必带 </span>
          <span v-for="e in equipResult.must" :key="e.name" style="margin-left:6px;font-size:13px">{{ e.name }}（{{ e.reason }}）</span>
        </div>
        <div v-if="equipResult.suggested && equipResult.suggested.length" style="margin-bottom:10px">
          <span style="font-weight:600;color:#67c23a">建议 </span>
          <span v-for="e in equipResult.suggested" :key="e.name" style="margin-left:6px;font-size:13px">{{ e.name }}（{{ e.reason }}）</span>
        </div>
        <div v-if="equipResult.summary" style="font-size:12px;color:#909399;padding-top:10px;border-top:1px dashed #e4e7ed">{{ equipResult.summary }}</div>
      </div>
      <template #footer>
        <el-button @click="equipVisible = false">关闭</el-button>
        <el-button type="success" @click="autoCreateCase" :disabled="!equipAlarmId">一键立案</el-button>
      </template>
    </el-dialog>
  </div>

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
import { Search, Plus, Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { alarmApi } from '@/api/alarm'
import { dictApi } from '@/api/dict'
import { officerApi } from '@/api/officer'

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
    callerName: '张先生',
    callerPhone: '13812345678',
    locationDetail: '海淀区中关村南大街5号',
    alarmType: 'fight',
    urgencyLevel: 2,
    alarmDesc: '两名男子在商场门口发生口角后斗殴，其中一人手持啤酒瓶，另一人头部受伤流血，现场围观群众较多，需立即处置。'
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

async function openCreate() {
  alarmTypes.value = await dictApi.get('alarm_type')
  createVisible.value = true
}

async function handleCreate() {
  await createFormRef.value.validate()
  submitting.value = true
  try {
    const res = await alarmApi.create(createForm)
    const alarmId = res.data
    ElMessage.success('接警录入成功')
    createVisible.value = false
    loadList()
    // 自动触发 AI 装备推荐
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

async function handleArrive(row) {
  try {
    // arrive API needs dispatch ID, not alarm ID
    await alarmApi.arriveByAlarm(row.id)
    ElMessage.success('已打卡，状态变为处置中')
    loadList()
  } catch { ElMessage.error('操作失败') }
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
    const { caseApi } = await import('@/api/caseinfo')
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
  await alarmApi.close(row.id, value)
  ElMessage.success('警情已关闭')
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
    const data = res.data || res
    const text = typeof data === 'string' ? data : JSON.stringify(data)
    try { equipResult.value = JSON.parse(text) }
    catch { equipResult.value = { must: [], suggested: [], summary: text } }
  } catch (e) {
    equipResult.value = { must: [], suggested: [], summary: 'AI 服务暂不可用' }
  } finally { equipLoading.value = false }
}

async function autoCreateCase() {
  const row = { alarmType: 'fight', alarmTime: new Date().toISOString(), locationDetail: '', alarmDesc: '', urgencyLevel: 2 }
  try {
    const { caseApi } = await import('@/api/caseinfo')
    const caseRes = await caseApi.create({
      caseName: '警情关联案件', caseCategory: 'criminal', caseType: 'other',
      occurredAt: new Date().toISOString(), locationDetail: '', caseDesc: '由 AI 装备推荐自动立案', severityLevel: 2
    })
    await alarmApi.linkCase(equipAlarmId.value, caseRes.data)
    ElMessage.success(`已立案 #${caseRes.data}`)
    equipVisible.value = false
    loadList()
  } catch { ElMessage.error('立案失败') }
}

onMounted(loadList)
</script>

<style scoped>
.search-form { margin-bottom: 0; }
</style>
