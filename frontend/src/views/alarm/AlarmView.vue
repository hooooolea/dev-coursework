<template>
  <div>
    <el-card shadow="never">
      <!-- 搜索栏 -->
      <el-form inline :model="query" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:120px">
            <el-option label="待处置" :value="1" />
            <el-option label="处置中" :value="2" />
            <el-option label="已处置" :value="3" />
            <el-option label="已关闭" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="紧急程度">
          <el-select v-model="query.urgencyLevel" clearable placeholder="全部" style="width:120px">
            <el-option label="一般" :value="1" />
            <el-option label="较紧急" :value="2" />
            <el-option label="紧急" :value="3" />
            <el-option label="特急" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
          <el-button :icon="Plus" @click="openCreate">接警录入</el-button>
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

    <!-- 接警录入对话框 -->
    <el-dialog v-model="createVisible" title="接警录入" width="600px">
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
            <el-radio :label="1">一般</el-radio>
            <el-radio :label="2">较紧急</el-radio>
            <el-radio :label="3">紧急</el-radio>
            <el-radio :label="4">特急</el-radio>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { alarmApi } from '@/api/alarm'
import { dictApi } from '@/api/dict'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ status: null, urgencyLevel: null, page: 1, size: 20 })

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
    const res = await alarmApi.list(query)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function openCreate() {
  alarmTypes.value = await dictApi.get('alarm_type')
  createVisible.value = true
}

async function handleCreate() {
  await createFormRef.value.validate()
  submitting.value = true
  try {
    await alarmApi.create(createForm)
    ElMessage.success('接警录入成功')
    createVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

function viewDetail(row) {
  ElMessage.info(`警情编号：${row.alarmNo}`)
}

function openDispatch(row) {
  ElMessageBox.prompt('请输入处置警员ID', '任务派发', { inputPattern: /^\d+$/, inputErrorMessage: '请输入数字ID' })
    .then(({ value }) => alarmApi.dispatch(row.id, Number(value)))
    .then(() => { ElMessage.success('派发成功'); loadList() })
    .catch(() => {})
}

async function handleClose(row) {
  const { value } = await ElMessageBox.prompt('请填写处置结果摘要', '关闭警情', { inputType: 'textarea' })
  await alarmApi.close(row.id, value)
  ElMessage.success('警情已关闭')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.search-form { margin-bottom: 0; }
</style>
