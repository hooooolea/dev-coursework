<template>
  <div>
    <el-card shadow="never">
      <el-form inline :model="query" @submit.prevent="loadList">
        <el-form-item label="操作人">
          <el-input v-model="query.userName" placeholder="姓名关键词" clearable style="width:140px" />
        </el-form-item>
        <el-form-item label="模块">
          <el-select v-model="query.module" clearable placeholder="全部" style="width:130px">
            <el-option v-for="m in modules" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width:340px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" :icon="Download" @click="handleExport" :loading="exporting">导出Excel</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe size="small">
        <el-table-column prop="userName"    label="操作人"   width="100" />
        <el-table-column prop="module"      label="模块"     width="110" />
        <el-table-column prop="action"      label="操作"     width="120" />
        <el-table-column prop="requestIp"   label="IP"       width="130" />
        <el-table-column prop="executeTime" label="耗时(ms)" width="90" align="right">
          <template #default="{ row }">
            <span :class="row.executeTime > 1000 ? 'slow' : ''">{{ row.executeTime }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="responseCode" label="响应码" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.responseCode === 200 ? 'success' : 'danger'" size="small">
              {{ row.responseCode }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestUrl"  label="请求路径" show-overflow-tooltip />
        <el-table-column prop="createdAt"   label="时间"     width="160" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Download } from '@element-plus/icons-vue'
import { logApi } from '@/api/system'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const exporting = ref(false)
const timeRange = ref(null)

const query = reactive({ userName: '', module: '', page: 1, size: 20 })

const modules = ['系统管理', '报警受理', '案件管理', '人员档案', '车辆管理', '巡逻调度', '警力资源']

async function loadList() {
  loading.value = true
  try {
    const params = { ...query }
    if (timeRange.value) {
      params.startTime = timeRange.value[0]
      params.endTime   = timeRange.value[1]
    }
    const res = await logApi.list(params)
    list.value  = res.data?.records || []
    total.value = res.data?.total   || 0
  } finally { loading.value = false }
}

function handleReset() {
  Object.assign(query, { userName: '', module: '', page: 1 })
  timeRange.value = null
  loadList()
}

async function handleExport() {
  exporting.value = true
  try {
    const params = { ...query }
    delete params.page
    delete params.size
    if (timeRange.value) {
      params.startTime = timeRange.value[0]
      params.endTime   = timeRange.value[1]
    }
    const res = await logApi.export(params)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `操作日志_${new Date().toISOString().slice(0, 10)}.xlsx`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  } finally {
    exporting.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.slow { color: #e6a23c; font-weight: 600; }
</style>
