<template>
  <div>
    <el-card shadow="never">
      <el-form inline :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="姓名或警号" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.workStatus" clearable placeholder="全部" style="width:120px">
            <el-option label="在岗"   value="on_duty" />
            <el-option label="休假"   value="vacation" />
            <el-option label="外出公干" value="business" />
            <el-option label="停职"   value="suspended" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
          <el-button :icon="Plus" @click="openCreate">新增警员</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <template #empty>
          <el-empty description="暂无数据" :image-size="80" />
        </template>
        <el-table-column prop="badgeNo"       label="警号"   width="110" />
        <el-table-column prop="realName"      label="姓名"   width="100" />
        <el-table-column prop="gender"        label="性别"   width="80" align="center">
          <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="policeCategory" label="警种"  min-width="100" />
        <el-table-column prop="rankTitle"      label="警衔"  min-width="120" />
        <el-table-column prop="position"       label="职务"  min-width="140" />
        <el-table-column prop="workStatus"     label="状态"  width="100" align="center">
          <template #default="{ row }">
            <span :class="row.workStatus === 'on_duty' ? 'tag-normal' : 'tag-active'">
              {{ statusLabel(row.workStatus) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="entryDate" label="入职日期" width="120" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link size="small" @click="openStatusChange(row)">更新状态</el-button>
            <el-button type="warning" link size="small" @click="openAssessment(row)">考核</el-button>
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

    <!-- 新增/编辑 -->
    <el-dialog v-model="dialogVisible" :title="editId ? '编辑警员' : '新增警员'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="警号" prop="badgeNo">
              <el-input v-model="form.badgeNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="警察类别">
              <el-select v-model="form.policeCategory" style="width:100%">
                <el-option label="刑警" value="criminal" />
                <el-option label="交警" value="traffic" />
                <el-option label="治安警" value="public" />
                <el-option label="派出所" value="station" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="警衔">
              <el-input v-model="form.rankTitle" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职务">
              <el-input v-model="form.position" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="入职日期">
          <el-date-picker v-model="form.entryDate" type="date" style="width:100%" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 考核记录抽屉 -->
    <el-drawer v-model="assessVisible" :title="`绩效考核 - ${currentOfficer?.realName}`" size="620px">
      <div style="margin-bottom:12px">
        <el-button type="primary" :icon="Plus" size="small" @click="addAssessVisible = true">新增考核</el-button>
      </div>
      <el-table :data="assessList" size="small">
        <template #empty>
          <el-empty description="暂无数据" :image-size="80" />
        </template>
        <el-table-column prop="periodValue"     label="考核期间" width="100" />
        <el-table-column prop="periodType"      label="类型"     width="70">
          <template #default="{ row }">{{ { month:'月度', quarter:'季度', year:'年度' }[row.periodType] }}</template>
        </el-table-column>
        <el-table-column prop="attendanceScore" label="出勤" width="65" align="right" />
        <el-table-column prop="caseScore"       label="案件" width="65" align="right" />
        <el-table-column prop="rewardScore"     label="奖励" width="65" align="right" />
        <el-table-column prop="violationScore"  label="违规" width="65" align="right" />
        <el-table-column prop="totalScore"      label="总分" width="70" align="right">
          <template #default="{ row }">
            <span :style="{ fontWeight: 600, color: resultColor(row.result) }">{{ row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="result" label="结果" width="75" align="center">
          <template #default="{ row }">
            <el-tag :type="resultTagType(row.result)" size="small">{{ resultLabel(row.result) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assessDate" label="日期"   width="100" />
        <el-table-column label="操作" width="60">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click="handleDelAssess(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!assessList.length" description="暂无考核记录" />
    </el-drawer>

    <!-- 新增考核弹窗 -->
    <el-dialog v-model="addAssessVisible" title="新增考核记录" width="480px">
      <el-form ref="assessFormRef" :model="assessForm" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="考核类型">
              <el-select v-model="assessForm.periodType" style="width:100%">
                <el-option label="月度" value="month" />
                <el-option label="季度" value="quarter" />
                <el-option label="年度" value="year" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考核期间">
              <el-input v-model="assessForm.periodValue" placeholder="如 2024-06" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="出勤得分">
              <el-input-number v-model="assessForm.attendanceScore" :min="0" :max="100" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="案件参与">
              <el-input-number v-model="assessForm.caseScore" :min="0" :max="100" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="表彰加分">
              <el-input-number v-model="assessForm.rewardScore" :min="0" :max="50" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="违规扣分">
              <el-input-number v-model="assessForm.violationScore" :min="0" :max="50" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="考核日期">
          <el-date-picker v-model="assessForm.assessDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="评语">
          <el-input v-model="assessForm.comment" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addAssessVisible = false">取消</el-button>
        <el-button type="primary" :loading="assessSaving" @click="handleAddAssess">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { officerApi } from '@/api/officer'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ keyword: '', workStatus: '', page: 1, size: 20 })

const dialogVisible = ref(false)
const submitting = ref(false)
const editId = ref(null)
const formRef = ref()
const form = reactive({ badgeNo: '', realName: '', gender: 1, policeCategory: '', rankTitle: '', position: '', entryDate: null, phone: '', remark: '' })
const rules = {
  badgeNo:  [{ required: true, message: '请输入警号' }],
  realName: [{ required: true, message: '请输入姓名' }]
}

const statusLabel = (v) => ({ on_duty:'在岗', vacation:'休假', business:'外出公干', suspended:'停职' })[v] || v

async function loadList() {
  loading.value = true
  try {
    const res = await officerApi.list(query)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function openCreate() {
  editId.value = null
  Object.assign(form, { badgeNo:'', realName:'', gender:1, policeCategory:'', rankTitle:'', position:'', entryDate:null, phone:'', remark:'' })
  dialogVisible.value = true
}

function openEdit(row) {
  editId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editId.value) {
      await officerApi.update(editId.value, form)
    } else {
      await officerApi.create(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadList()
  } finally { submitting.value = false }
}

async function openStatusChange(row) {
  const options = ['on_duty', 'vacation', 'business', 'suspended']
  const labels  = ['在岗', '休假', '外出公干', '停职']
  await ElMessageBox.confirm(
    `<el-select>更新 ${row.realName} 的状态</el-select>`,
    '更新状态',
    {
      distinguishCancelAndClose: true,
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    }
  ).catch(() => {})
  // 简化：弹出输入框让用户输入状态值
  const { value } = await ElMessageBox.prompt('输入状态（on_duty/vacation/business/suspended）', '更新状态', {
    inputValue: row.workStatus
  }).catch(() => ({ value: null }))
  if (!value) return
  await officerApi.updateStatus(row.id, value)
  ElMessage.success('状态已更新')
  loadList()
}

async function handleDel(row) {
  await ElMessageBox.confirm(`确定删除警员「${row.realName}」？`, '确认删除', { type: 'warning' })
  await officerApi.del(row.id)
  ElMessage.success('已删除')
  loadList()
}

// 考核记录
const assessVisible    = ref(false)
const addAssessVisible = ref(false)
const assessSaving     = ref(false)
const assessFormRef    = ref()
const assessList       = ref([])
const currentOfficer   = ref(null)
const assessForm       = reactive({
  periodType: 'month', periodValue: '', attendanceScore: 0,
  caseScore: 0, rewardScore: 0, violationScore: 0, assessDate: null, comment: ''
})

const resultLabel   = r => ({ excellent:'优秀', good:'良好', pass:'合格', fail:'不合格' })[r] || r
const resultTagType = r => ({ excellent:'success', good:'', pass:'info', fail:'danger' })[r] || ''
const resultColor   = r => ({ excellent:'#67c23a', good:'#409eff', pass:'#909399', fail:'#f56c6c' })[r] || '#303133'

async function openAssessment(row) {
  currentOfficer.value = row
  const res = await officerApi.listAssessments(row.id)
  assessList.value = res.data || []
  assessVisible.value = true
}

async function handleAddAssess() {
  assessSaving.value = true
  try {
    await officerApi.addAssessment(currentOfficer.value.id, { ...assessForm })
    ElMessage.success('考核记录已保存')
    addAssessVisible.value = false
    openAssessment(currentOfficer.value)
  } finally { assessSaving.value = false }
}

async function handleDelAssess(row) {
  await ElMessageBox.confirm('确定删除该考核记录？', '确认删除', { type: 'warning' })
  await officerApi.delAssessment(row.id)
  ElMessage.success('已删除')
  openAssessment(currentOfficer.value)
}

onMounted(loadList)
</script>

<style scoped>
.tag-normal { display:inline-block; padding:1px 8px; background:#f4f4f5; color:#606266; border-radius:2px; font-size:14px; }
.tag-active { display:inline-block; padding:1px 8px; background:#fef0f0; color:#f56c6c; border-radius:2px; font-size:14px; }
</style>
