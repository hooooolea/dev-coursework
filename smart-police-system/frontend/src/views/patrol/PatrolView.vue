<template>
  <div>
    <el-card shadow="never">
      <el-form inline :model="query">
        <el-form-item label="任务状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:120px">
            <el-option label="待接收" value="pending" />
            <el-option label="已接收" value="accepted" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
          <el-button :icon="Plus" @click="createVisible = true">派发任务</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="taskNo"    label="任务编号" width="160" />
        <el-table-column prop="taskName" label="任务名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="taskType"  label="类型"     width="90">
          <template #default="{ row }">
            {{ { routine:'例行巡逻', special:'专项检查', fixed:'定点值守' }[row.taskType] || row.taskType }}
          </template>
        </el-table-column>
        <el-table-column prop="areaName" label="巡逻区域" min-width="160" />
        <el-table-column prop="taskStart" label="计划开始" width="160" />
        <el-table-column prop="taskEnd"   label="计划结束" width="160" />
        <el-table-column prop="status"    label="状态"     width="90" align="center">
          <template #default="{ row }">
            <span :class="['ongoing','pending'].includes(row.status) ? 'tag-active' : 'tag-normal'">
              {{ statusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewCheckins(row)">打卡记录</el-button>
            <el-button v-if="row.status === 'pending'" link size="small" @click="handleAccept(row)">接收</el-button>
            <el-button v-if="row.status === 'accepted'" link size="small" @click="handleCheckin(row, 'start')">开始巡逻</el-button>
            <el-button v-if="row.status === 'ongoing'" link size="small" @click="handleCheckin(row, 'end')">结束巡逻</el-button>
            <el-button v-if="row.status === 'ongoing'" type="success" link size="small" @click="openComplete(row)">完成任务</el-button>
            <el-button v-if="['pending','cancelled'].includes(row.status)" type="danger" link size="small" @click="handleDel(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top:16px;justify-content:flex-end"
        @current-change="loadList"
      />
    </el-card>

    <!-- 派发任务 -->
    <el-dialog v-model="createVisible" title="派发巡逻任务" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" />
        </el-form-item>
        <el-form-item label="任务类型">
          <el-select v-model="form.taskType" style="width:100%">
            <el-option label="例行巡逻" value="routine" />
            <el-option label="专项检查" value="special" />
            <el-option label="定点值守" value="fixed" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责警员" prop="officerId">
          <el-select
            v-model="form.officerId"
            filterable
            remote
            :remote-method="searchOfficer"
            :loading="officerLoading"
            placeholder="输入姓名或警号搜索"
            style="width:100%"
          >
            <el-option
              v-for="o in officerOptions"
              :key="o.id"
              :label="`${o.realName}（${o.badgeNo}）`"
              :value="o.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="">
          <el-button type="warning" :icon="MagicStick" @click="handleAiRecommend" :loading="aiRecommending" :disabled="!aiConfigured">
            {{ aiRecommending ? 'AI 分析中...' : 'AI 推荐警员' }}
          </el-button>
          <el-button type="success" :icon="MagicStick" @click="handleEquipRecommend" :loading="equipRecommending" :disabled="!aiConfigured">
            {{ equipRecommending ? '分析中...' : 'AI 推荐装备' }}
          </el-button>
        </el-form-item>

        <el-form-item v-if="aiResult" label="AI 建议">
          <div class="ai-result-card">
            <div class="ai-result-text">{{ aiResult }}</div>
            <el-button v-if="aiOfficers.length > 0" type="primary" size="small" style="margin-top:8px" @click="applyAiRecommend">
              一键填入推荐警员
            </el-button>
          </div>
        </el-form-item>

        <el-form-item v-if="equipResult" label="装备建议">
          <div class="ai-result-card">
            <div v-if="equipResult.must && equipResult.must.length" style="margin-bottom:6px">
              <span style="font-weight:600;color:#e6a23c">【必带】</span>
              <span v-for="e in equipResult.must" :key="e.name" style="margin-left:6px;font-size:12px">{{ e.name }}（{{ e.reason }}）</span>
            </div>
            <div v-if="equipResult.suggested && equipResult.suggested.length">
              <span style="font-weight:600;color:#67c23a">【建议】</span>
              <span v-for="e in equipResult.suggested" :key="e.name" style="margin-left:6px;font-size:12px">{{ e.name }}（{{ e.reason }}）</span>
            </div>
            <div v-if="equipResult.summary" style="margin-top:4px;font-size:12px;color:#909399">{{ equipResult.summary }}</div>
          </div>
        </el-form-item>
        <el-form-item label="巡逻区域">
          <el-input v-model="form.areaName" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="taskStart">
              <el-date-picker v-model="form.taskStart" type="datetime" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="taskEnd">
              <el-date-picker v-model="form.taskEnd" type="datetime" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">派发</el-button>
      </template>
    </el-dialog>

    <!-- 打卡记录抽屉 -->
    <el-drawer v-model="checkinVisible" :title="`打卡记录 - ${currentTask?.taskNo}`" size="420px">
      <el-timeline>
        <el-timeline-item
          v-for="c in checkinList"
          :key="c.id"
          :timestamp="c.checkinTime"
          placement="top"
        >
          <div class="ci-type">{{ { start:'开始巡逻', end:'结束巡逻', mid:'途中汇报' }[c.checkinType] }}</div>
          <div class="ci-note" v-if="c.note">{{ c.note }}</div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-if="!checkinList.length" description="暂无打卡记录" />
    </el-drawer>

    <!-- 完成任务弹窗 -->
    <el-dialog v-model="completeVisible" title="完成任务" width="440px">
      <el-form label-width="80px">
        <el-form-item label="巡逻小结">
          <el-input v-model="completeSummary" type="textarea" :rows="4" placeholder="填写本次巡逻情况小结（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" :loading="completeLoading" @click="handleComplete">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus, MagicStick } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { patrolApi } from '@/api/patrol'
import { officerApi } from '@/api/officer'
import { aiApi } from '@/api/ai'

const officerOptions = ref([])
const officerLoading = ref(false)
async function searchOfficer(keyword) {
  if (!keyword) return
  officerLoading.value = true
  try {
    const res = await officerApi.list({ keyword, page: 1, size: 20 })
    officerOptions.value = res.data?.records || []
  } finally { officerLoading.value = false }
}

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ status: '', page: 1, size: 20 })

const createVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ taskName: '', taskType: 'routine', officerId: null, areaName: '', taskStart: null, taskEnd: null })
const rules = {
  taskName:  [{ required: true, message: '请输入任务名称' }],
  officerId: [{ required: true, message: '请输入负责警员ID' }],
  taskStart: [{ required: true, message: '请选择开始时间' }],
  taskEnd:   [{ required: true, message: '请选择结束时间' }]
}

const checkinVisible = ref(false)
const currentTask = ref(null)
const checkinList = ref([])

const completeVisible = ref(false)
const completeLoading = ref(false)
const completeSummary = ref('')
const completeTarget = ref(null)

const aiConfigured = ref(false)
const aiRecommending = ref(false)
const aiResult = ref('')
const aiOfficers = ref([])
const equipRecommending = ref(false)
const equipResult = ref(null)

function openComplete(row) {
  completeTarget.value = row
  completeSummary.value = ''
  completeVisible.value = true
}

async function handleComplete() {
  completeLoading.value = true
  try {
    await patrolApi.complete(completeTarget.value.id, completeSummary.value)
    ElMessage.success('任务已完成')
    completeVisible.value = false
    loadList()
  } finally { completeLoading.value = false }
}

const statusLabel = (v) => ({ pending:'待接收', accepted:'已接收', ongoing:'进行中', completed:'已完成', cancelled:'已取消' })[v] || v

async function loadList() {
  loading.value = true
  try {
    const res = await patrolApi.list(query)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function handleCreate() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await patrolApi.create(form)
    ElMessage.success('任务已派发')
    createVisible.value = false
    loadList()
  } finally { submitting.value = false }
}

async function handleAccept(row) {
  await patrolApi.accept(row.id)
  ElMessage.success('已接收任务')
  loadList()
}

async function handleCheckin(row, type) {
  const label = type === 'start' ? '开始巡逻' : '结束巡逻'
  const { value } = await ElMessageBox.prompt(`${label} - 可填写情况说明`, label, { inputType: 'textarea' }).catch(() => ({ value: '' }))
  await patrolApi.checkin(row.id, type, value)
  ElMessage.success(`${label}已记录`)
  loadList()
}

async function viewCheckins(row) {
  currentTask.value = row
  const res = await patrolApi.checkins(row.id)
  checkinList.value = res.data || []
  checkinVisible.value = true
}

async function handleDel(row) {
  await ElMessageBox.confirm(`确定删除任务「${row.taskNo}」？`, '确认删除', { type: 'warning' })
  await patrolApi.del(row.id)
  ElMessage.success('已删除')
  loadList()
}

async function checkAiConfig() {
  try {
    const res = await aiApi.configStatus()
    aiConfigured.value = res.data?.data?.configured || false
  } catch { aiConfigured.value = false }
}

async function handleAiRecommend() {
  if (!form.taskName || !form.areaName) {
    ElMessage.warning('请先填写任务名称和巡逻区域')
    return
  }
  aiRecommending.value = true
  aiResult.value = ''
  aiOfficers.value = []
  aiApi.scheduleRecommend({
    taskType: form.taskType,
    areaName: form.areaName,
    taskStart: form.taskStart,
    taskEnd: form.taskEnd
  }, token => {
    aiResult.value += token
  }, () => {
    aiRecommending.value = false
    try {
      const json = JSON.parse(aiResult.value)
      aiOfficers.value = json.recommended || []
    } catch { /* raw text */ }
  })
}

async function handleEquipRecommend() {
  equipRecommending.value = true
  equipResult.value = null
  try {
    const res = await aiApi.equipmentRecommend({
      taskType: form.taskType,
      areaName: form.areaName
    })
    const text = typeof res.data === 'string' ? res.data : (res.data?.data || JSON.stringify(res.data))
    try { equipResult.value = JSON.parse(text) }
    catch { equipResult.value = { must: [], suggested: [], summary: text } }
  } catch {
    ElMessage.error('装备推荐服务不可用')
  } finally { equipRecommending.value = false }
}

function applyAiRecommend() {
  if (aiOfficers.value.length > 0) {
    form.officerId = aiOfficers.value[0].officerId
    ElMessage.success(`已填入推荐警员：${aiOfficers.value[0].name}`)
  }
}

onMounted(() => { loadList(); checkAiConfig() })
</script>

<style scoped>
.tag-active { display:inline-block; padding:1px 8px; background:#fef0f0; color:#f56c6c; border-radius:2px; font-size:14px; }
.tag-normal { display:inline-block; padding:1px 8px; background:#f4f4f5; color:#606266; border-radius:2px; font-size:14px; }
.ci-type { font-size:14px; font-weight:600; color:#303133; }
.ci-note { font-size:14px; color:#606266; margin-top:4px; }
.ai-result-card {
  background: #fafafa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px;
  width: 100%;
}
.ai-result-text {
  font-size: 13px;
  color: #303133;
  white-space: pre-wrap;
  line-height: 1.7;
}
</style>
