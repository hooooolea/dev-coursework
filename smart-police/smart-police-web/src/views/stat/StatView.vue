<template>
  <div class="stat-wrap">
    <!-- 顶部 Tab -->
    <el-tabs v-model="activeTab" type="card" @tab-change="onTabChange">
      <el-tab-pane label="案件统计" name="case" />
      <el-tab-pane label="警力效能" name="officer" />
    </el-tabs>

    <!-- ===== 案件统计 ===== -->
    <template v-if="activeTab === 'case'">
      <el-row :gutter="12" style="margin-bottom:12px">
        <!-- 汇总卡片 -->
        <el-col :span="6">
          <div class="stat-card">
            <div class="sc-label">统计周期立案总数</div>
            <div class="sc-num">{{ caseData.total ?? '-' }}</div>
            <div v-if="caseData.total != null && compDelta('total') != null" class="sc-delta" :class="compClass('total')">
              {{ compLabel }} {{ compDelta('total') }}
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="sc-label">已结案</div>
            <div class="sc-num">{{ caseData.closed ?? '-' }}</div>
            <div v-if="caseData.closed != null && compDelta('closed') != null" class="sc-delta" :class="compClass('closed')">
              {{ compLabel }} {{ compDelta('closed') }}
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="sc-label">破案率</div>
            <div class="sc-num">{{ caseData.solveRate != null ? caseData.solveRate + '%' : '-' }}</div>
            <div v-if="caseData.solveRate != null && compDelta('solveRate') != null" class="sc-delta" :class="compClass('solveRate')">
              {{ compLabel }} {{ compDelta('solveRate') }}
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card" style="display:flex;flex-direction:column;align-items:center;justify-content:center;gap:6px">
            <div style="display:flex;align-items:center;gap:10px">
              <el-select v-model="caseMonths" style="width:100px" @change="loadCaseStats">
                <el-option label="近3个月" :value="3" />
                <el-option label="近6个月" :value="6" />
                <el-option label="近12个月" :value="12" />
              </el-select>
              <el-button type="primary" :icon="Download" @click="doExport('case')">导出</el-button>
            </div>
            <el-radio-group v-model="compMode" size="small" @change="calcComparison">
              <el-radio-button value="mom">环比</el-radio-button>
              <el-radio-button value="yoy">同比</el-radio-button>
            </el-radio-group>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="12">
        <el-col :span="16">
          <div class="card">
            <div class="card-title">按月立案趋势</div>
            <div ref="caseLineRef" style="height:280px" v-loading="caseLoading" />
          </div>
        </el-col>
        <el-col :span="8">
          <div class="card">
            <div class="card-title">案件类型分布</div>
            <div ref="casePieRef" style="height:280px" v-loading="caseLoading" />
          </div>
        </el-col>
      </el-row>

      <!-- 状态汇总表 -->
      <div class="card" style="margin-top:12px">
        <div class="card-title">案件状态汇总</div>
        <el-table :data="caseStatusRows" size="small">
          <el-table-column prop="label" label="状态" width="120" />
          <el-table-column prop="count" label="数量" width="100" align="right" />
        </el-table>
      </div>
    </template>

    <!-- ===== 警力效能 ===== -->
    <template v-if="activeTab === 'officer'">
      <el-row :gutter="12" style="margin-bottom:12px">
        <el-col :span="6">
          <div class="stat-card">
            <div class="sc-label">警员总数</div>
            <div class="sc-num">{{ officerData.total ?? '-' }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="sc-label">当前在岗</div>
            <div class="sc-num">{{ officerData.onDuty ?? '-' }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="sc-label">在岗率</div>
            <div class="sc-num">{{ officerData.onDutyRate != null ? officerData.onDutyRate + '%' : '-' }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card" style="display:flex;align-items:center;justify-content:center">
            <el-button type="primary" :icon="Download" @click="doExport('officer')">导出警员表</el-button>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="12">
        <el-col :span="8">
          <div class="card">
            <div class="card-title">警员状态分布</div>
            <div ref="officerPieRef" style="height:260px" v-loading="officerLoading" />
          </div>
        </el-col>
        <el-col :span="16">
          <div class="card">
            <div class="card-title">近30天活跃警员（案件+巡逻任务数 Top10）</div>
            <el-table :data="officerData.officerList || []" size="small" v-loading="officerLoading">
              <el-table-column type="index" label="#" width="45" />
              <el-table-column prop="badgeNo" label="警号" width="100" />
              <el-table-column prop="name"    label="姓名" width="100" />
              <el-table-column prop="workStatus" label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="row.workStatus === 'on_duty' ? 'success' : 'info'" size="small">
                    {{ { on_duty:'在岗', vacation:'休假', business:'外出', suspended:'停职' }[row.workStatus] || row.workStatus }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="cases30d"   label="30天案件" width="100" align="right" />
              <el-table-column prop="patrols30d" label="30天巡逻" width="100" align="right" />
            </el-table>
          </div>
        </el-col>
      </el-row>
    </template>

    <!-- 警情报表导出入口 -->
    <div class="card" style="margin-top:12px;display:flex;align-items:center;gap:12px">
      <span style="font-size:14px;font-weight:600;color:#303133">报表导出：</span>
      <el-button :icon="Download" @click="doExport('alarm')">本月警情汇报</el-button>
      <el-button :icon="Download" @click="doExport('case')">案件统计表</el-button>
      <el-button :icon="Download" @click="doExport('officer')">警员信息汇总</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { statApi } from '@/api/stat'

const activeTab    = ref('case')
const caseLoading  = ref(false)
const officerLoading = ref(false)
const caseMonths   = ref(6)
const compMode     = ref('mom')   // mom | yoy
const caseData     = reactive({ total: null, closed: null, solveRate: null, byStatus: {}, mom: null, yoy: null })
const officerData  = reactive({ total: null, onDuty: null, onDutyRate: null, statusDist: [], officerList: [] })
const caseStatusRows = ref([])

// 同比/环比计算
const compLabel = computed(() => compMode.value === 'mom' ? '环比' : '同比')

function compData() {
  return caseData[compMode.value]  // { total, closed, solveRate }
}

function compDelta(field) {
  const prev = compData()
  if (!prev || caseData[field] == null) return null
  const curr = caseData[field]
  const last = prev[field]
  if (last == null || last === 0) {
    // 上期无数据 → 新增
    return curr > 0 ? '新增' : null
  }
  const diff = curr - last
  const pct  = Math.round(Math.abs(diff) * 1000 / last) / 10
  const sign = diff >= 0 ? '↑' : '↓'
  return `${sign} ${pct}%`
}

function compClass(field) {
  const prev = compData()
  if (!prev || caseData[field] == null) return ''
  const curr = caseData[field]
  const last = prev[field]
  if (last == null || last === 0) return curr > 0 ? 'delta-up' : ''
  return curr >= last ? 'delta-up' : 'delta-down'
}

function calcComparison() { /* no-op — compData/compLabel are computed reactively */ }

const caseLineRef  = ref()
const casePieRef   = ref()
const officerPieRef = ref()
let caseLineChart = null
let casePieChart  = null
let officerPieChart = null

const PIE_COLORS = ['#1a237e','#283593','#303f9f','#3949ab','#5c6bc0','#7986cb','#9fa8da','#c5cae9']
const STATUS_MAP = { pending:'待立案', investigating:'侦查中', transferred:'已移送', closed:'已结案', cancelled:'已撤案' }

async function loadCaseStats() {
  caseLoading.value = true
  try {
    const res = await statApi.caseStats(caseMonths.value)
    const d = res.data
    Object.assign(caseData, { total: d.total, closed: d.closed, solveRate: d.solveRate, byStatus: d.byStatus, mom: d.mom, yoy: d.yoy })

    caseStatusRows.value = Object.entries(d.byStatus || {}).map(([k, v]) => ({
      label: STATUS_MAP[k] || k, count: v
    }))

    await nextTick()
    if (!caseLineChart) caseLineChart = echarts.init(caseLineRef.value)
    caseLineChart.setOption({
      grid: { left: 40, right: 16, top: 16, bottom: 28 },
      xAxis: { type: 'category', data: d.months, axisLabel: { color: '#606266', fontSize: 13 } },
      yAxis: { type: 'value', minInterval: 1, axisLabel: { color: '#606266', fontSize: 13 }, splitLine: { lineStyle: { color: '#f0f0f0' } } },
      series: [{ data: d.monthlyCounts, type: 'bar', itemStyle: { color: '#1565c0' }, barMaxWidth: 40 }],
      tooltip: { trigger: 'axis' }
    })

    if (!casePieChart) casePieChart = echarts.init(casePieRef.value)
    const pieData = (d.typeStats || []).map((item, i) => ({ ...item, itemStyle: { color: PIE_COLORS[i % PIE_COLORS.length] } }))
    casePieChart.setOption({
      legend: { bottom: 4, textStyle: { fontSize: 13, color: '#606266' } },
      series: [{ type: 'pie', radius: ['35%', '55%'], center: ['50%', '44%'], data: pieData, label: { show: false } }],
      tooltip: { trigger: 'item' }
    })
  } finally { caseLoading.value = false }
}

async function loadOfficerStats() {
  officerLoading.value = true
  try {
    const res = await statApi.officerStats()
    const d = res.data
    Object.assign(officerData, d)
    await nextTick()
    if (!officerPieChart) officerPieChart = echarts.init(officerPieRef.value)
    const pieData = (d.statusDist || []).map((item, i) => ({ ...item, itemStyle: { color: PIE_COLORS[i % PIE_COLORS.length] } }))
    officerPieChart.setOption({
      legend: { bottom: 4, textStyle: { fontSize: 13, color: '#606266' } },
      series: [{ type: 'pie', radius: ['35%', '55%'], center: ['50%', '44%'], data: pieData, label: { show: false } }],
      tooltip: { trigger: 'item' }
    })
  } finally { officerLoading.value = false }
}

async function onTabChange(tab) {
  if (tab === 'officer' && officerData.total === null) loadOfficerStats()
}

async function doExport(type) {
  try {
    await statApi.exportExcel(type)
    ElMessage.success('下载已开始')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

onMounted(() => loadCaseStats())
</script>

<style scoped>
.stat-wrap { display: flex; flex-direction: column; gap: 12px; }

.stat-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 18px 20px;
  min-height: 90px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.sc-label { font-size: 14px; color: #909399; margin-bottom: 6px; }
.sc-num   { font-size: 24px; font-weight: 600; color: #303133; }
.sc-delta { font-size: 14px; margin-top: 4px; font-weight: 500; }
.delta-up   { color: #67c23a; }
.delta-down { color: #f56c6c; }

.card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 3px;
  padding: 16px;
}
.card-title { font-size: 13px; font-weight: 600; color: #303133; margin-bottom: 12px; }
</style>
