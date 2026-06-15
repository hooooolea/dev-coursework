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

      <!-- AI 装备推荐 -->
      <div v-if="equipResult" class="equip-card">
        <div class="equip-card-header">
          <span> AI 装备推荐 — 警情 #{{ equipAlarmId }}</span>
          <el-button link size="small" @click="closeEquipCard">关闭</el-button>
        </div>
        <div v-if="equipResult.must && equipResult.must.length" class="equip-row">
          <span class="equip-tag equip-must">必带</span>
          <span v-for="e in equipResult.must" :key="e.name" class="equip-item">{{ e.name }}（{{ e.reason }}）</span>
        </div>
        <div v-if="equipResult.suggested && equipResult.suggested.length" class="equip-row">
          <span class="equip-tag equip-suggest">建议</span>
          <span v-for="e in equipResult.suggested" :key="e.name" class="equip-item">{{ e.name }}（{{ e.reason }}）</span>
        </div>
        <div v-if="equipResult.summary" class="equip-summary">{{ equipResult.summary }}</div>
      </div>
    </el-card>

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
import { Search, Plus } from '@element-plus/icons-vue'
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
        await alarmApi.dispatch(latest.id, 2)
        ElMessage.success('已自动派发给张建国（P001）')
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
    await caseApi.create(caseData)
    // 关联网关警情
    await alarmApi.close(row.id, '已升级为案件')
    ElMessage.success('已升级为案件，原警情已关闭')
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
const equipLoading = ref(false)
const equipResult = ref(null)
const equipAlarmId = ref(null)

async function fetchEquipRecommend(alarmId) {
  if (!alarmId) return
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
    console.error('装备推荐失败', e)
  } finally { equipLoading.value = false }
}

function closeEquipCard() {
  equipResult.value = null
  equipAlarmId.value = null
}

onMounted(loadList)
</script>

<style scoped>
.search-form { margin-bottom: 0; }

.equip-card {
  margin-top: 16px; padding: 16px; background: #fafbfc;
  border: 1px solid #e4e7ed; border-radius: 8px;
}
.equip-card-header {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 12px;
}
.equip-row { display: flex; align-items: flex-start; gap: 8px; margin-bottom: 6px; flex-wrap: wrap; }
.equip-tag { font-size: 12px; padding: 1px 8px; border-radius: 3px; font-weight: 600; }
.equip-must { background: #fef0f0; color: #f56c6c; }
.equip-suggest { background: #f0f9eb; color: #67c23a; }
.equip-item { font-size: 13px; color: #606266; }
.equip-summary { font-size: 12px; color: #909399; margin-top: 8px; padding-top: 8px; border-top: 1px dashed #e4e7ed; }
</style>
