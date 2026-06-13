<template>
  <div class="dashboard">
    <div v-if="error" style="text-align:right;margin-bottom:8px">
      <el-button type="danger" size="small" @click="loadData">重新加载</el-button>
    </div>

    <el-row :gutter="12">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <div class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div v-if="loading" class="stat-loading">
            <el-skeleton :rows="2" animated />
          </div>
          <div v-else-if="error" class="stat-error">加载失败</div>
          <div v-else class="stat-num">{{ card.value }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top:12px">
      <el-col :span="16">
        <div class="card">
          <div class="card-title">近 7 天接警趋势</div>
          <div class="chart-wrapper">
            <div ref="trendChart" style="height:260px"></div>
            <div v-if="error" class="chart-error-overlay">数据加载失败</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="card">
          <div class="card-title">案件类型分布</div>
          <div class="chart-wrapper">
            <div ref="pieChart" style="height:260px"></div>
            <div v-if="error" class="chart-error-overlay">数据加载失败</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="card" style="margin-top:12px">
      <div class="card-title">
        最新警情
        <el-button type="primary" link size="small" @click="$router.push('/alarm')">全部</el-button>
      </div>
      <el-table :data="recentAlarms" size="small">
        <el-table-column prop="alarmNo" label="警情编号" width="150" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ alarmTypeLabel(row.alarmType) }}</template>
        </el-table-column>
        <el-table-column prop="locationDetail" label="地址" show-overflow-tooltip />
        <el-table-column prop="urgencyLevel" label="紧急程度" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="urgencyTagType(row.urgencyLevel)" size="small" effect="dark">
              {{ urgencyLabel(row.urgencyLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <span :class="row.status === 1 ? 'tag-danger' : 'tag-normal'">
              {{ {1:'待处置',2:'处置中',3:'已处置',4:'已关闭'}[row.status] }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="alarmTime" label="报警时间" width="160" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { alarmApi } from '@/api/alarm'
import { dashboardApi } from '@/api/dashboard'
import { dictApi } from '@/api/dict'

const trendChart = ref()
const pieChart = ref()
const recentAlarms = ref([])
const loading = ref(false)
const error = ref(false)

const statCards = reactive([
  { label: '今日接警', value: '-' },
  { label: '在办案件', value: '-' },
  { label: '布控车辆', value: '-' },
  { label: '在岗警员', value: '-' }
])

const alarmTypes = ref([])
const alarmTypeLabel = (v) => alarmTypes.value.find(d => d.dictValue === v)?.dictLabel || v || '-'

const urgencyLabel   = (v) => ['', '一般', '较紧急', '紧急', '特急'][v] || '-'
const urgencyTagType = (v) => ['', 'info', 'warning', 'danger', 'danger'][v] || 'info'

let trendInstance = null
let pieInstance   = null

const PIE_COLORS = ['#1a237e','#283593','#303f9f','#3949ab','#5c6bc0','#7986cb','#9fa8da','#c5cae9']

function initTrendChart(dates, counts) {
  if (!trendInstance) trendInstance = echarts.init(trendChart.value)
  trendInstance.setOption({
    grid: { left: 36, right: 16, top: 16, bottom: 28 },
    xAxis: {
      type: 'category', data: dates,
      axisLine: { lineStyle: { color: '#e4e7ed' } },
      axisLabel: { color: '#606266', fontSize: 14 }
    },
    yAxis: {
      type: 'value', minInterval: 1,
      axisLabel: { color: '#606266', fontSize: 14 },
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: [{
      data: counts, type: 'line', smooth: true, symbol: 'none',
      lineStyle: { color: '#1565c0', width: 2 },
      areaStyle: { color: 'rgba(21,101,192,0.06)' }
    }],
    tooltip: { trigger: 'axis', backgroundColor: '#fff', borderColor: '#e4e7ed', textStyle: { color: '#303133', fontSize: 14 } }
  })
}

function initPieChart(typeStats) {
  if (!pieInstance) pieInstance = echarts.init(pieChart.value)
  const data = typeStats.length
    ? typeStats.map((item, i) => ({ ...item, itemStyle: { color: PIE_COLORS[i % PIE_COLORS.length] } }))
    : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e4e7ed' } }]
  pieInstance.setOption({
    legend: { bottom: 4, textStyle: { color: '#606266', fontSize: 13 } },
    series: [{
      type: 'pie', radius: ['38%', '58%'], center: ['50%', '44%'],
      data, label: { show: false }
    }],
    tooltip: { trigger: 'item', backgroundColor: '#fff', borderColor: '#e4e7ed', textStyle: { color: '#303133', fontSize: 14 } }
  })
}

async function loadData() {
  loading.value = true
  error.value = false

  // Reset stat cards
  statCards.forEach(c => { c.value = '-' })
  recentAlarms.value = []

  // Dispose old chart instances
  if (trendInstance) { trendInstance.dispose(); trendInstance = null }
  if (pieInstance) { pieInstance.dispose(); pieInstance = null }

  await nextTick()

  // Render empty chart placeholders
  initTrendChart([], [])
  initPieChart([])

  try {
    const [statsRes, alarmRes, types] = await Promise.all([
      dashboardApi.stats(),
      alarmApi.list({ page: 1, size: 8 }),
      dictApi.get('alarm_type')
    ])
    alarmTypes.value = types

    const s = statsRes.data
    statCards[0].value = s.todayAlarm
    statCards[1].value = s.activeCase
    statCards[2].value = s.controlledVehicle
    statCards[3].value = s.onDutyOfficer

    initTrendChart(s.trendDates, s.trendCounts)
    initPieChart(s.typeStats || [])

    recentAlarms.value = alarmRes.data?.records || []
  } catch (e) {
    console.error('Dashboard 数据加载失败', e)
    error.value = true
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.stat-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 3px;
  padding: 18px 20px;
}
.stat-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.stat-num { font-size: 26px; font-weight: 600; color: #303133; }
.stat-loading { padding-top: 4px; }
.stat-error { font-size: 13px; color: #c0c4cc; padding: 6px 0; }

.card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 3px;
  padding: 16px;
}
.card-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-wrapper { position: relative; }
.chart-error-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.85);
  color: #909399;
  font-size: 13px;
}

.tag-danger {
  display: inline-block;
  padding: 1px 6px;
  background: #fef0f0;
  color: #f56c6c;
  border-radius: 2px;
  font-size: 14px;
}
.tag-normal {
  display: inline-block;
  padding: 1px 6px;
  background: #f4f4f5;
  color: #606266;
  border-radius: 2px;
  font-size: 14px;
}
</style>
