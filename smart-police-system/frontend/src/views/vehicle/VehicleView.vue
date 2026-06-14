<template>
  <div>
    <el-card shadow="never">
      <el-form inline :model="query">
        <el-form-item label="车牌/车主">
          <el-input v-model="query.keyword" placeholder="车牌号或车主姓名" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:120px">
            <el-option label="正常" value="normal" />
            <el-option label="可疑" value="suspicious" />
            <el-option label="布控中" value="controlled" />
            <el-option label="在逃" value="wanted" />
            <el-option label="已扣押" value="seized" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
          <el-button :icon="Plus" @click="createVisible = true">登记车辆</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <template #empty>
          <el-empty description="暂无数据" :image-size="80" />
        </template>
        <el-table-column prop="plateNo" label="车牌号" width="130" />
        <el-table-column label="车辆类型" width="110">
          <template #default="{ row }">{{ vehicleTypeLabel(row.vehicleType) }}</template>
        </el-table-column>
        <el-table-column label="品牌型号" min-width="180">
          <template #default="{ row }">{{ row.brand }} {{ row.model }}</template>
        </el-table-column>
        <el-table-column prop="color" label="颜色" width="90" />
        <el-table-column prop="ownerName" label="登记人" min-width="100" />
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="vehicleStatusType(row.status)" size="small">{{ vehicleStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewViolations(row)">违章</el-button>
            <el-button v-if="row.status !== 'controlled'" type="warning" link size="small" @click="openControl(row)">布控</el-button>
            <el-button v-if="row.status === 'controlled'" type="success" link size="small" @click="handleDecontrol(row)">解控</el-button>
            <el-button type="danger" link size="small" @click="handleDel(row)">删除</el-button>
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

    <!-- 登记车辆 -->
    <el-dialog v-model="createVisible" title="车辆登记" width="560px">
      <el-form ref="createFormRef" :model="createForm" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="车牌号" prop="plateNo">
              <el-input v-model="createForm.plateNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="车辆类型">
              <el-select v-model="createForm.vehicleType" style="width:100%">
                <el-option label="轿车" value="sedan" />
                <el-option label="SUV" value="suv" />
                <el-option label="货车" value="truck" />
                <el-option label="摩托车" value="moto" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-input v-model="createForm.brand" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="颜色">
              <el-input v-model="createForm.color" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="车主姓名">
          <el-input v-model="createForm.ownerName" />
        </el-form-item>
        <el-form-item label="车架号">
          <el-input v-model="createForm.vin" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">登记</el-button>
      </template>
    </el-dialog>

    <!-- 布控 -->
    <el-dialog v-model="controlVisible" title="设置布控" width="420px">
      <el-form :model="controlForm" label-width="90px">
        <el-form-item label="布控级别">
          <el-radio-group v-model="controlForm.level">
            <el-radio :label="1">一般</el-radio>
            <el-radio :label="2">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="布控原因">
          <el-input v-model="controlForm.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="controlVisible = false">取消</el-button>
        <el-button type="warning" @click="handleControl">确认布控</el-button>
      </template>
    </el-dialog>

    <!-- 违章记录抽屉 -->
    <el-drawer v-model="violationVisible" :title="`违章记录 - ${currentVehicle?.plateNo}`" size="640px">
      <div style="margin-bottom:12px">
        <el-button type="primary" :icon="Plus" size="small" @click="addViolationVisible = true">新增违章</el-button>
      </div>
      <el-table :data="violationList" size="small">
        <template #empty>
          <el-empty description="暂无数据" :image-size="80" />
        </template>
        <el-table-column prop="violationTime" label="违章时间" width="155" />
        <el-table-column prop="violationType" label="违章类型" width="110" />
        <el-table-column prop="location"      label="违章地点" show-overflow-tooltip />
        <el-table-column prop="fineAmount"    label="罚款(元)" width="90" align="right" />
        <el-table-column prop="deductedPoints" label="扣分" width="60" align="center" />
        <el-table-column prop="isPaid" label="缴款" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isPaid" type="success" size="small">已缴</el-tag>
            <el-button v-else type="warning" link size="small" @click="handleMarkPaid(row)">待缴</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!violationList.length" description="暂无违章记录" />
    </el-drawer>

    <!-- 新增违章弹窗 -->
    <el-dialog v-model="addViolationVisible" title="新增违章记录" width="480px">
      <el-form ref="violFormRef" :model="violForm" label-width="90px">
        <el-form-item label="违章时间" prop="violationTime">
          <el-date-picker v-model="violForm.violationTime" type="datetime" style="width:100%" />
        </el-form-item>
        <el-form-item label="违章类型">
          <el-select v-model="violForm.violationType" style="width:100%">
            <el-option label="超速行驶" value="speeding" />
            <el-option label="闯红灯" value="red_light" />
            <el-option label="违规停车" value="illegal_parking" />
            <el-option label="酒后驾车" value="drunk_driving" />
            <el-option label="无证驾驶" value="no_license" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="违章地点">
          <el-input v-model="violForm.location" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="罚款(元)">
              <el-input-number v-model="violForm.fineAmount" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="扣分">
              <el-input-number v-model="violForm.deductedPoints" :min="0" :max="12" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="addViolationVisible = false">取消</el-button>
        <el-button type="primary" :loading="violSaving" @click="handleAddViolation">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { vehicleApi } from '@/api/vehicle'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ keyword: '', status: '', page: 1, size: 20 })

const createVisible = ref(false)
const submitting = ref(false)
const createFormRef = ref()
const createForm = reactive({ plateNo: '', vehicleType: 'sedan', brand: '', color: '', ownerName: '', vin: '' })

const controlVisible = ref(false)
const currentVehicle = ref(null)
const controlForm = reactive({ level: 1, reason: '' })

const vehicleTypeLabel   = (v) => {
  const key = (v || '').toLowerCase()
  return ({ sedan: '轿车', suv: 'SUV', truck: '货车', motorcycle: '摩托车', moto: '摩托车', other: '其他' })[key] || v
}
const vehicleStatusLabel = (v) => ({ normal: '正常', suspicious: '可疑', suspect: '可疑', controlled: '布控中', wanted: '在逃', seized: '已扣押' })[v] || v
const vehicleStatusType  = (v) => ({ normal: 'success', suspicious: 'warning', suspect: 'warning', controlled: 'danger', wanted: 'danger', seized: 'info' })[v] || 'info'

async function loadList() {
  loading.value = true
  try {
    const res = await vehicleApi.list(query)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function handleCreate() {
  submitting.value = true
  try {
    await vehicleApi.create(createForm)
    ElMessage.success('车辆登记成功')
    createVisible.value = false
    loadList()
  } finally { submitting.value = false }
}

function openControl(row) {
  currentVehicle.value = row
  controlForm.reason = ''
  controlVisible.value = true
}

async function handleControl() {
  await vehicleApi.control(currentVehicle.value.id, controlForm)
  ElMessage.success('布控成功')
  controlVisible.value = false
  loadList()
}

async function handleDecontrol(row) {
  const { value } = await ElMessageBox.prompt('请填写解控原因', '解除布控').catch(() => ({ value: '' }))
  if (!value) return
  await vehicleApi.decontrol(row.id, value)
  ElMessage.success('已解除布控')
  loadList()
}

async function handleDel(row) {
  await ElMessageBox.confirm(`确定删除车辆「${row.plateNo}」？`, '确认删除', { type: 'warning' })
  await vehicleApi.del(row.id)
  ElMessage.success('已删除')
  loadList()
}

// 违章记录
const violationVisible    = ref(false)
const addViolationVisible = ref(false)
const violSaving          = ref(false)
const violFormRef         = ref()
const violationList       = ref([])
const violForm            = reactive({ violationTime: null, violationType: 'other', location: '', fineAmount: 0, deductedPoints: 0 })

async function viewViolations(row) {
  currentVehicle.value = row
  const res = await vehicleApi.listViolations(row.id)
  violationList.value = res.data || []
  violationVisible.value = true
}

async function handleAddViolation() {
  violSaving.value = true
  try {
    await vehicleApi.addViolation(currentVehicle.value.id, { ...violForm })
    ElMessage.success('违章记录已添加')
    addViolationVisible.value = false
    Object.assign(violForm, { violationTime: null, violationType: 'other', location: '', fineAmount: 0, deductedPoints: 0 })
    viewViolations(currentVehicle.value)
  } finally { violSaving.value = false }
}

async function handleMarkPaid(row) {
  await vehicleApi.markViolationPaid(row.id)
  ElMessage.success('已标记为已缴款')
  viewViolations(currentVehicle.value)
}

onMounted(loadList)
</script>
