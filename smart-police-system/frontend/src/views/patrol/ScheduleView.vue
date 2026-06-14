<template>
  <div>
    <el-card shadow="never">
      <!-- 周导航 -->
      <div class="week-nav">
        <el-button :icon="ArrowLeft" @click="changeWeek(-1)" />
        <span class="week-label">{{ weekStart }} ~ {{ weekEnd }}</span>
        <el-button :icon="ArrowRight" @click="changeWeek(1)" />
        <el-button @click="weekOffset = 0; loadWeek()">本周</el-button>
        <el-button type="primary" :icon="Plus" style="margin-left:auto" @click="openCreate()">新增排班</el-button>
      </div>

      <!-- 周视图表格 -->
      <el-table
        :data="shiftRows"
        border
        v-loading="loading"
        style="margin-top:12px"
      >
        <template #empty>
          <el-empty description="暂无排班数据" :image-size="80" />
        </template>
        <el-table-column label="班次" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="shiftTagType(row.shiftType)" size="small">{{ shiftLabel(row.shiftType) }}</el-tag>
            <div style="font-size:11px;color:#909399;margin-top:2px">{{ shiftTime(row.shiftType) }}</div>
          </template>
        </el-table-column>
        <el-table-column
          v-for="day in weekDays"
          :key="day.date"
          :label="day.label"
          align="center"
          min-width="120"
        >
          <template #default="{ row }">
            <div class="cell-wrap">
              <div
                v-for="s in getSchedules(day.date, row.shiftType)"
                :key="s.id"
                :class="['schedule-tag', statusClass(s.status)]"
              >
                <span>{{ s.officerName || s.officerId }}</span>
                <span class="badge-no">{{ s.badgeNo }}</span>
                <el-icon class="del-icon" @click.stop="handleDel(s)"><Close /></el-icon>
              </div>
              <el-button
                type="primary"
                link
                size="small"
                :icon="Plus"
                @click="openCreate({ date: day.date, shiftType: row.shiftType })"
                class="add-btn"
              />
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增排班弹窗 -->
    <el-dialog v-model="createVisible" title="新增排班" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="排班日期" prop="scheduleDate">
          <el-date-picker v-model="form.scheduleDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="班次" prop="shiftType">
          <el-select v-model="form.shiftType" style="width:100%">
            <el-option label="早班（06:00-14:00）" value="morning" />
            <el-option label="中班（14:00-22:00）" value="afternoon" />
            <el-option label="夜班（22:00-06:00）" value="night" />
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
        <el-form-item label="备注">
          <el-input v-model="form.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ArrowLeft, ArrowRight, Plus, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { patrolApi } from '@/api/patrol'
import { officerApi } from '@/api/officer'

const loading    = ref(false)
const weekOffset = ref(0)
const weekStart  = ref('')
const weekEnd    = ref('')
const scheduleMap = ref({})   // { 'YYYY-MM-DD': [PatrolSchedule, ...] }

const SHIFT_TYPES = ['morning', 'afternoon', 'night']
const shiftRows   = computed(() => SHIFT_TYPES.map(s => ({ shiftType: s })))

const weekDays = computed(() => {
  if (!weekStart.value) return []
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(weekStart.value)
    d.setDate(d.getDate() + i)
    const dateStr = d.toISOString().slice(0, 10)
    const weekLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    return { date: dateStr, label: `${weekLabels[i]}\n${dateStr.slice(5)}` }
  })
})

function getSchedules(date, shiftType) {
  return (scheduleMap.value[date] || []).filter(s => s.shiftType === shiftType)
}

const shiftLabel = s => ({ morning: '早班', afternoon: '中班', night: '夜班' })[s] || s
const shiftTime  = s => ({ morning: '06:00-14:00', afternoon: '14:00-22:00', night: '22:00-06:00' })[s] || ''
const shiftTagType = s => ({ morning: 'success', afternoon: '', night: 'info' })[s] || ''
const statusClass  = s => ({ normal: '', adjusted: 'adjusted', leave: 'leave' })[s] || ''

async function loadWeek() {
  loading.value = true
  try {
    const res = await patrolApi.scheduleWeek(weekOffset.value)
    const d = res.data
    weekStart.value  = d.weekStart
    weekEnd.value    = d.weekEnd
    scheduleMap.value = d.schedules
  } finally { loading.value = false }
}

function changeWeek(delta) {
  weekOffset.value += delta
  loadWeek()
}

// 新增排班
const createVisible = ref(false)
const submitting    = ref(false)
const formRef       = ref()
const form          = reactive({ scheduleDate: '', shiftType: 'morning', officerId: null, remark: '' })
const rules = {
  scheduleDate: [{ required: true, message: '请选择日期' }],
  shiftType:    [{ required: true, message: '请选择班次' }],
  officerId:    [{ required: true, message: '请选择警员' }]
}

const officerOptions = ref([])
const officerLoading = ref(false)
async function searchOfficer(kw) {
  if (!kw) return
  officerLoading.value = true
  try {
    const res = await officerApi.list({ keyword: kw, page: 1, size: 20 })
    officerOptions.value = res.data?.records || []
  } finally { officerLoading.value = false }
}

function openCreate(preset = {}) {
  Object.assign(form, { scheduleDate: preset.date || '', shiftType: preset.shiftType || 'morning', officerId: null, remark: '' })
  createVisible.value = true
}

async function handleCreate() {
  await formRef.value.validate()
  submitting.value = true
  try {
    // 根据班次填默认时间
    const times = { morning: ['06:00', '14:00'], afternoon: ['14:00', '22:00'], night: ['22:00', '06:00'] }
    const [st, et] = times[form.shiftType]
    await patrolApi.scheduleCreate({ ...form, startTime: st, endTime: et })
    ElMessage.success('排班成功')
    createVisible.value = false
    loadWeek()
  } finally { submitting.value = false }
}

async function handleDel(schedule) {
  await ElMessageBox.confirm(
    `确定删除 ${schedule.officerName || schedule.officerId} 的 ${shiftLabel(schedule.shiftType)} 排班？`,
    '确认删除', { type: 'warning' })
  await patrolApi.scheduleDel(schedule.id)
  ElMessage.success('已删除')
  loadWeek()
}

onMounted(loadWeek)
</script>

<style scoped>
.week-nav {
  display: flex;
  align-items: center;
  gap: 10px;
}
.week-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  min-width: 180px;
  text-align: center;
}

.cell-wrap {
  min-height: 36px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: flex-start;
  padding: 4px 0;
}

.schedule-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  background: #e8f0fe;
  color: #1a237e;
  border-radius: 3px;
  padding: 2px 6px;
  font-size: 14px;
  position: relative;
}
.schedule-tag.adjusted { background: #fdf6ec; color: #e6a23c; }
.schedule-tag.leave    { background: #f4f4f5; color: #909399; text-decoration: line-through; }

.badge-no  { font-size: 13px; opacity: 0.7; }
.del-icon  { font-size: 13px; cursor: pointer; opacity: 0.5; }
.del-icon:hover { opacity: 1; color: #f56c6c; }

.add-btn { padding: 0 2px; opacity: 0.4; }
.add-btn:hover { opacity: 1; }
</style>
