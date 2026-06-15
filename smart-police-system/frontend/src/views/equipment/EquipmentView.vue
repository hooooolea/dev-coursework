<template>
  <div>
    <el-card shadow="never">
      <el-form inline :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="装备名称/编号" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:110px">
            <el-option label="空闲" value="idle" />
            <el-option label="借出中" value="borrowed" />
            <el-option label="维修中" value="maintenance" />
            <el-option label="已报废" value="scrapped" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.equipType" clearable placeholder="全部" style="width:120px">
            <el-option label="枪支弹药" value="weapon" />
            <el-option label="警用车辆" value="vehicle" />
            <el-option label="通信设备" value="comm" />
            <el-option label="防护装备" value="protection" />
            <el-option label="办公设备" value="office" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
          <el-button :icon="Plus" @click="openCreate">新增装备</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <template #empty>
          <el-empty description="暂无数据" :image-size="80" />
        </template>
        <el-table-column prop="equipNo"   label="装备编号" width="130" />
        <el-table-column prop="equipName" label="装备名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="equipType" label="类型" width="100">
          <template #default="{ row }">
            {{ equipTypeLabel(row.equipType) }}
          </template>
        </el-table-column>
        <el-table-column prop="brand" label="品牌" min-width="120" />
        <el-table-column prop="storageLocation" label="存放位置" width="130" show-overflow-tooltip />
        <el-table-column prop="status"    label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewBorrows(row)">借还记录</el-button>
            <el-button v-if="row.status === 'idle'" type="warning" link size="small" @click="openBorrow(row)">领用</el-button>
            <el-button type="info"    link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger"  link size="small" @click="handleDel(row)">删除</el-button>
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

    <!-- 新增/编辑装备 -->
    <el-dialog v-model="formVisible" :title="editRow ? '编辑装备' : '新增装备'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="装备编号" prop="equipNo">
              <el-input v-model="form.equipNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="装备名称" prop="equipName">
              <el-input v-model="form.equipName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="装备类型">
              <el-select v-model="form.equipType" style="width:100%">
                <el-option label="枪支弹药" value="weapon" />
                <el-option label="警用车辆" value="vehicle" />
                <el-option label="通信设备" value="comm" />
                <el-option label="防护装备" value="protection" />
                <el-option label="办公设备" value="office" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌型号">
              <el-input v-model="form.brand" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="存放位置">
          <el-input v-model="form.storageLocation" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="购置日期">
              <el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="购置价格">
              <el-input-number v-model="form.purchasePrice" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 领用弹窗 -->
    <el-dialog v-model="borrowVisible" :title="`领用装备 - ${currentEquip?.equipName}`" width="440px">
      <el-form ref="borrowFormRef" :model="borrowForm" label-width="90px">
        <el-form-item label="借用人ID" prop="borrowerId">
          <el-select
            v-model="borrowForm.borrowerId"
            filterable remote
            :remote-method="searchOfficer"
            :loading="officerLoading"
            placeholder="输入姓名或警号"
            style="width:100%"
          >
            <el-option v-for="o in officerOptions" :key="o.id" :label="`${o.realName}（${o.badgeNo}）`" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="借用用途" prop="borrowPurpose">
          <el-input v-model="borrowForm.borrowPurpose" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="预计归还">
          <el-date-picker v-model="borrowForm.expectedReturn" type="datetime" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="borrowVisible = false">取消</el-button>
        <el-button type="primary" :loading="borrowSaving" @click="handleBorrow">确认领用</el-button>
      </template>
    </el-dialog>

    <!-- 借还记录抽屉 -->
    <el-drawer v-model="borrowsVisible" :title="`借还记录 - ${currentEquip?.equipName}`" size="600px">
      <el-table :data="borrowList" size="small">
        <template #empty>
          <el-empty description="暂无数据" :image-size="80" />
        </template>
        <el-table-column prop="borrowerName"   label="借用人"   width="100" />
        <el-table-column prop="borrowPurpose"  label="用途"     show-overflow-tooltip />
        <el-table-column prop="borrowTime" label="领用时间" min-width="155" />
        <el-table-column prop="expectedReturn" label="预计归还" width="155" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'warning' : 'success'" size="small">
              {{ row.status === 1 ? '借出中' : '已归还' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="success" link size="small" @click="handleReturn(row)">归还</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!borrowList.length" description="暂无借还记录" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { equipmentApi } from '@/api/equipment'
import { officerApi } from '@/api/officer'

const list    = ref([])
const total   = ref(0)
const loading = ref(false)
const query   = reactive({ keyword: '', status: '', equipType: '', page: 1, size: 20 })

const equipTypeLabel = t => ({ weapon:'枪支弹药', vehicle:'警用车辆', comm:'通信设备', protection:'防护装备', office:'办公设备' })[t] || t
const statusLabel    = s => ({ idle:'空闲', borrowed:'借出中', maintenance:'维修中', scrapped:'已报废' })[s] || s
const statusTagType  = s => ({ idle:'success', borrowed:'warning', maintenance:'info', scrapped:'danger' })[s] || ''

async function loadList() {
  loading.value = true
  try {
    const res = await equipmentApi.list(query)
    list.value  = res.data?.records || []
    total.value = res.data?.total   || 0
  } finally { loading.value = false }
}

// 新增/编辑
const formVisible = ref(false)
const submitting  = ref(false)
const editRow     = ref(null)
const formRef     = ref()
const form        = reactive({ equipNo: '', equipName: '', equipType: 'comm', brand: '', storageLocation: '', purchaseDate: null, purchasePrice: 0, description: '' })
const rules = {
  equipNo:   [{ required: true, message: '请输入装备编号' }],
  equipName: [{ required: true, message: '请输入装备名称' }]
}

function openCreate() {
  editRow.value = null
  Object.assign(form, { equipNo: '', equipName: '', equipType: 'comm', brand: '', storageLocation: '', purchaseDate: null, purchasePrice: 0, description: '' })
  formVisible.value = true
}

function openEdit(row) {
  editRow.value = row
  Object.assign(form, { equipNo: row.equipNo, equipName: row.equipName, equipType: row.equipType, brand: row.brand || '', storageLocation: row.storageLocation || '', purchaseDate: row.purchaseDate, purchasePrice: row.purchasePrice || 0, description: row.description || '' })
  formVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editRow.value) {
      await equipmentApi.update(editRow.value.id, form)
    } else {
      await equipmentApi.create(form)
    }
    ElMessage.success('保存成功')
    formVisible.value = false
    loadList()
  } finally { submitting.value = false }
}

async function handleDel(row) {
  await ElMessageBox.confirm(`确定删除装备「${row.equipName}」？`, '确认删除', { type: 'warning' })
  try {
    await equipmentApi.del(row.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '删除失败')
  }
}

// 领用
const borrowVisible  = ref(false)
const borrowSaving   = ref(false)
const borrowFormRef  = ref()
const currentEquip   = ref(null)
const borrowForm     = reactive({ borrowerId: null, borrowPurpose: '', expectedReturn: null })
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

function openBorrow(row) {
  currentEquip.value = row
  Object.assign(borrowForm, { borrowerId: null, borrowPurpose: '', expectedReturn: null })
  borrowVisible.value = true
}

async function handleBorrow() {
  borrowSaving.value = true
  try {
    await equipmentApi.borrow(currentEquip.value.id, { ...borrowForm })
    ElMessage.success('领用成功')
    borrowVisible.value = false
    loadList()
  } finally { borrowSaving.value = false }
}

// 借还记录
const borrowsVisible = ref(false)
const borrowList     = ref([])

async function viewBorrows(row) {
  currentEquip.value = row
  const res = await equipmentApi.borrows(row.id)
  borrowList.value = res.data || []
  borrowsVisible.value = true
}

async function handleReturn(record) {
  const { value } = await ElMessageBox.prompt('请填写归还备注（损坏情况等，可留空）', '装备归还', {
    inputType: 'textarea'
  }).catch(() => ({ value: '' }))
  await equipmentApi.returnEquip(record.id, value || '')
  ElMessage.success('归还成功')
  viewBorrows(currentEquip.value)
  loadList()
}

onMounted(loadList)
</script>
