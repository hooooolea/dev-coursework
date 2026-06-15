<template>
  <div>
    <el-card shadow="never">
      <el-form inline :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="案件名称/编号" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:120px">
            <el-option label="待审批" value="pending" />
            <el-option label="侦查中" value="investigating" />
            <el-option label="已移送" value="transferred" />
            <el-option label="已结案" value="closed" />
            <el-option label="已撤案" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="案件等级">
          <el-select v-model="query.severityLevel" clearable placeholder="全部" style="width:110px">
            <el-option label="一般" :value="1" />
            <el-option label="重要" :value="2" />
            <el-option label="重大" :value="3" />
            <el-option label="特重大" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
          <el-button :icon="Plus" @click="createVisible = true">案件立案</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="caseNo" label="案件编号" width="160" />
        <el-table-column prop="caseName" label="案件名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="caseType" label="案件类型" width="110" />
        <el-table-column prop="severityLevel" label="等级" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="severityType(row.severityLevel)" size="small">
              {{ severityLabel(row.severityLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="caseStatusType(row.status)" size="small">{{ caseStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileDate" label="立案日期" width="120" />
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openCaseDetail(row)">案件详情</el-button>
            <el-button v-if="row.status === 'investigating'" type="success" link size="small" @click="advanceStatus(row)">推进</el-button>
            <el-button type="success" link size="small" @click="handleStatusChange(row, 'closed')">结案</el-button>
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

    <!-- 立案对话框 -->
    <el-dialog v-model="createVisible" title="案件立案" width="640px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="案件名称" prop="caseName">
          <el-input v-model="createForm.caseName" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="案件大类" prop="caseCategory">
              <el-select v-model="createForm.caseCategory" style="width:100%">
                <el-option label="刑事案件" value="criminal" />
                <el-option label="治安案件" value="public" />
                <el-option label="交通案件" value="traffic" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="案件小类" prop="caseType">
              <el-select v-model="createForm.caseType" style="width:100%">
                <el-option v-for="d in caseTypes" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="发案时间" prop="occurredAt">
          <el-date-picker v-model="createForm.occurredAt" type="datetime" style="width:100%" />
        </el-form-item>
        <el-form-item label="发案地点">
          <el-input v-model="createForm.locationDetail" />
        </el-form-item>
        <el-form-item label="案件等级">
          <el-radio-group v-model="createForm.severityLevel">
            <el-radio :value="1">一般</el-radio>
            <el-radio :value="2">重要</el-radio>
            <el-radio :value="3">重大</el-radio>
            <el-radio :value="4">特重大</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="案情描述" prop="caseDesc">
          <el-input v-model="createForm.caseDesc" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">立案</el-button>
      </template>
    </el-dialog>

    <!-- 统一案件详情抽屉 -->
    <el-drawer v-model="detailVisible" :title="`案件详情 - ${currentCase?.caseNo || ''}`" size="650px">
      <el-tabs v-model="detailTab" type="card">
        <el-tab-pane label="侦查进展" name="progress">
          <div v-for="p in progressList" :key="p.id" class="progress-item">
            <div class="prog-time">{{ p.progressTime }}</div>
            <div class="prog-content">{{ p.content }}</div>
            <div class="prog-plan" v-if="p.nextPlan">下一步：{{ p.nextPlan }}</div>
          </div>
          <el-empty v-if="!progressList.length" description="暂无进展" />
          <el-button type="primary" style="margin-top:12px" @click="addProgressVisible = true">新增进展</el-button>
        </el-tab-pane>
        <el-tab-pane label="证据材料" name="evidence">
          <el-table :data="evidenceList" size="small">
            <el-table-column prop="evidenceName" label="证据名称" show-overflow-tooltip />
            <el-table-column prop="evidenceType" label="类型" width="90" />
            <el-table-column label="文件" width="160" show-overflow-tooltip>
              <template #default="{ row }">
                <a v-if="row.fileUrl" :href="row.fileUrl" target="_blank">{{ row.fileName || '查看' }}</a>
                <span v-else style="color:#c0c4cc">无附件</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70">
              <template #default="{ row }">
                <el-button type="danger" link size="small" @click="handleDelEvidence(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!evidenceList.length" description="暂无证据" />
          <el-button type="primary" style="margin-top:12px" @click="addEvidenceVisible = true">新增证据</el-button>
        </el-tab-pane>
        <el-tab-pane label="嫌疑人" name="suspect">
          <el-table :data="suspectList" size="small" v-loading="suspectLoading">
            <el-table-column prop="name" label="姓名" width="80" />
            <el-table-column prop="gender" label="性别" width="50"><template #default="{r}">{{ genderLabel(r.gender) }}</template></el-table-column>
            <el-table-column prop="idCard" label="身份证号" width="160" />
            <el-table-column prop="suspectRole" label="角色" width="80">
              <template #default="{r}"><el-tag :type="suspectRoleType(r.suspectRole)" size="small">{{r.suspectRole||'-'}}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openEditSuspect(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelSuspect(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!suspectList.length" description="暂无嫌疑人" />
          <el-button type="primary" style="margin-top:12px" @click="openAddSuspect">新增嫌疑人</el-button>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <div style="display:flex;gap:8px">
          <el-button v-if="currentCase?.status === 'investigating'" type="success" @click="approveCase">审批通过</el-button>
          <el-button v-if="currentCase?.status === 'investigating'" type="warning" @click="advanceStatus(currentCase)">移送检察院</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 新增进展 -->
    <el-dialog v-model="addProgressVisible" title="新增侦查进展" width="500px">
      <el-form ref="progFormRef" :model="progForm" label-width="80px">
        <el-form-item label="进展内容" prop="content">
          <el-input v-model="progForm.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="下一步计划">
          <el-input v-model="progForm.nextPlan" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addProgressVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProgress">提交</el-button>
      </template>
    </el-dialog>

    <!-- 新增证据 -->
    <el-dialog v-model="addEvidenceVisible" title="新增证据材料" width="480px">
      <el-form ref="evidFormRef" :model="evidForm" label-width="90px">
        <el-form-item label="证据名称" prop="evidenceName"><el-input v-model="evidForm.evidenceName" /></el-form-item>
        <el-form-item label="证据类型">
          <el-select v-model="evidForm.evidenceType" style="width:100%">
            <el-option label="实物证据" value="physical" /><el-option label="书证" value="document" /><el-option label="视听资料" value="media" /><el-option label="证人证言" value="witness" /><el-option label="鉴定意见" value="appraisal" /><el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="收集地点"><el-input v-model="evidForm.collectLocation" /></el-form-item>
        <el-form-item label="存放位置"><el-input v-model="evidForm.storageLocation" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="evidForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addEvidenceVisible = false">取消</el-button>
        <el-button type="primary" :loading="evidSaving" @click="handleAddEvidence">提交</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑嫌疑人 -->
    <el-dialog v-model="suspectFormVisible" :title="suspectForm.id ? '编辑嫌疑人' : '新增嫌疑人'" width="500px">
      <el-form ref="suspectFormRef" :model="suspectForm" :rules="suspectRules" label-width="80px">
        <el-form-item label="姓名" prop="name"><el-input v-model="suspectForm.name" /></el-form-item>
        <el-form-item label="角色" prop="suspectRole">
          <el-select v-model="suspectForm.suspectRole" style="width:100%">
            <el-option label="主犯" value="主犯" /><el-option label="从犯" value="从犯" /><el-option label="嫌疑人" value="嫌疑人" /><el-option label="在逃" value="在逃" /><el-option label="已抓获" value="已抓获" />
          </el-select>
        </el-form-item>
        <el-form-item label="身份证号"><el-input v-model="suspectForm.idCard" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="suspectForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="suspectFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="suspectSaving" @click="handleSaveSuspect">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus, Upload } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { caseApi } from '@/api/caseinfo'
import { dictApi } from '@/api/dict'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ keyword: '', status: '', severityLevel: null, page: 1, size: 20 })

const createVisible = ref(false)
const submitting = ref(false)
const createFormRef = ref()
const caseTypes = ref([])

const createForm = reactive({
  caseName: '', caseCategory: '', caseType: '',
  occurredAt: new Date(), locationDetail: '',
  severityLevel: 1, caseDesc: ''
})

const createRules = {
  caseName: [{ required: true, message: '请填写案件名称' }],
  caseCategory: [{ required: true, message: '请选择案件大类' }],
  caseType: [{ required: true, message: '请选择案件小类' }],
  occurredAt: [{ required: true, message: '请选择发案时间' }],
  caseDesc: [{ required: true, message: '请填写案情描述' }]
}

const detailVisible = ref(false)
const detailTab = ref('progress')
const currentCase = ref(null)
const progressList = ref([])
const evidenceList = ref([])
const suspectList = ref([])
const suspectLoading = ref(false)
const progForm = reactive({ content: '', nextPlan: '' })
const addProgressVisible = ref(false)

const severityLabel = (v) => ['', '一般', '重要', '重大', '特重大'][v] || '-'
const severityType  = (v) => ['', 'info', 'warning', 'danger', 'danger'][v] || 'info'
const caseStatusLabel = (v) => ({ investigating: '侦查中', transferred: '已移送', closed: '已结案', cancelled: '已撤案' })[v] || v
const caseStatusType  = (v) => ({ investigating: 'primary', transferred: 'warning', closed: 'success', cancelled: 'info' })[v] || 'info'

async function loadList() {
  loading.value = true
  try {
    const res = await caseApi.list(query)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function handleCreate() {
  await createFormRef.value.validate()
  submitting.value = true
  try {
    await caseApi.create(createForm)
    ElMessage.success('立案成功')
    createVisible.value = false
    loadList()
  } finally { submitting.value = false }
}

async function handleStatusChange(row, status) {
  const label = status === 'closed' ? '结案' : '撤案'
  const { value } = await ElMessageBox.prompt(`请填写${label}原因`, label, { inputType: 'textarea' }).catch(() => ({ value: '' }))
  if (!value && status === 'cancelled') return
  await caseApi.updateStatus(row.id, status, value)
  ElMessage.success(`${label}成功`)
  loadList()
}

async function handleDel(row) {
  await ElMessageBox.confirm(`确定删除案件「${row.caseName}」？此操作不可恢复`, '确认删除', { type: 'warning' })
  await caseApi.del(row.id)
  ElMessage.success('已删除')
  loadList()
}

async function openCaseDetail(row) {
  currentCase.value = row
  detailTab.value = 'progress'
  const [pRes, eRes, sRes] = await Promise.all([
    caseApi.listProgress(row.id),
    caseApi.listEvidence(row.id),
    caseApi.listSuspect(row.id)
  ])
  progressList.value = pRes.data || []
  evidenceList.value = eRes.data || []
  suspectList.value = sRes.data || []
  detailVisible.value = true
}

function advanceStatus(row) {
  ElMessageBox.confirm(`将案件「${row.caseName}」移送检察院？`, '移送确认', { type: 'warning' })
    .then(() => caseApi.updateStatus(row.id, 'transferred', '已移送检察院'))
    .then(() => { ElMessage.success('已移送检察院'); detailVisible.value = false; loadList() })
    .catch(() => {})
}

async function approveCase() {
  const row = currentCase.value
  if (!row) return
  await ElMessageBox.confirm(`审批通过案件「${row.caseName}」？审批后案件将保持侦查中状态。`, '审批确认', { type: 'warning' })
  ElMessage.success('审批通过，案件继续侦查')
  // Keep status as investigating, just log approval
}

async function submitProgress() {
  await caseApi.addProgress(currentCase.value.id, progForm)
  ElMessage.success('进展已记录')
  addProgressVisible.value = false
  const res = await caseApi.listProgress(currentCase.value.id)
  progressList.value = res.data || []
}

// 编辑案件
const editVisible = ref(false)
const editSaving  = ref(false)
const editFormRef = ref()
const editForm    = reactive({ caseName: '', caseCategory: '', caseType: '', severityLevel: 1, caseDesc: '', remark: '' })
const editTarget  = ref(null)

function openEdit(row) {
  editTarget.value = row
  Object.assign(editForm, {
    caseName: row.caseName, caseCategory: row.caseCategory, caseType: row.caseType,
    severityLevel: row.severityLevel ?? 1, caseDesc: row.caseDesc, remark: row.remark
  })
  editVisible.value = true
}

async function handleEditSave() {
  editSaving.value = true
  try {
    await caseApi.update(editTarget.value.id, editForm)
    ElMessage.success('保存成功')
    editVisible.value = false
    loadList()
  } finally { editSaving.value = false }
}

// 证据管理
const addEvidenceVisible = ref(false)
const evidSaving         = ref(false)
const evidFormRef        = ref()
const evidenceList       = ref([])
const evidForm           = reactive({ evidenceName: '', evidenceType: 'physical', collectTime: null, collectLocation: '', storageLocation: '', description: '' })
// 证据文件上传相关状态
const evidenceFileList      = ref([])
const evidenceUploading     = ref(false)
const evidenceUploadPercent = ref(0)
const pendingEvidenceFile   = ref(null)
const EVIDENCE_MAX_BYTES    = 10 * 1024 * 1024 // 10MB

function handleEvidenceFileChange(file) {
  // 前端二次校验（与服务端保持一致）
  if (file.size > EVIDENCE_MAX_BYTES) {
    ElMessage.error('文件超过 10MB 限制')
    evidenceFileList.value = []
    pendingEvidenceFile.value = null
    return
  }
  pendingEvidenceFile.value = file.raw
  evidenceFileList.value = [file]
}

function handleEvidenceExceed() {
  ElMessage.warning('只能上传一个文件，请先移除当前文件')
}

function handleEvidenceRemove() {
  pendingEvidenceFile.value = null
  evidenceFileList.value = []
}

async function handleAddEvidence() {
  await evidFormRef.value.validate()
  evidSaving.value = true
  try {
    let fileUrl = null, fileName = null, fileSize = null, fileType = null
    // 1. 若有附件，先上传
    if (pendingEvidenceFile.value) {
      evidenceUploading.value = true
      evidenceUploadPercent.value = 0
      try {
        const upRes = await caseApi.uploadEvidence(currentCase.value.id, pendingEvidenceFile.value)
        fileUrl   = upRes.data?.fileUrl
        fileName  = upRes.data?.fileName
        fileSize  = upRes.data?.fileSize
        fileType  = upRes.data?.fileType
      } finally {
        evidenceUploading.value = false
      }
    }
    // 2. 写入 metadata
    await caseApi.addEvidence(currentCase.value.id, {
      ...evidForm,
      fileUrl, fileName, fileSize, fileType
    })
    ElMessage.success('证据已添加')
    addEvidenceVisible.value = false
    Object.assign(evidForm, { evidenceName: '', evidenceType: 'physical', collectTime: null, collectLocation: '', storageLocation: '', description: '' })
    evidenceFileList.value = []
    pendingEvidenceFile.value = null
    const res = await caseApi.listEvidence(currentCase.value.id)
    evidenceList.value = res.data || []
  } finally { evidSaving.value = false }
}

async function handleDelEvidence(row) {
  await ElMessageBox.confirm(`确定删除证据「${row.evidenceName}」？`, '确认删除', { type: 'warning' })
  await caseApi.delEvidence(row.id)
  ElMessage.success('已删除')
  const res = await caseApi.listEvidence(currentCase.value.id)
  evidenceList.value = res.data || []
}

// 嫌疑人管理
const suspectVisible    = ref(false)
const suspectLoading    = ref(false)
const suspectList       = ref([])
const suspectFormVisible= ref(false)
const suspectSaving     = ref(false)
const suspectFormRef    = ref()
const suspectForm       = reactive({ id: null, name: '', gender: 'unknown', age: null, idCard: '', phone: '', address: '', suspectRole: '嫌疑人', description: '' })
const suspectRules      = {
  name:        [{ required: true, message: '请填写嫌疑人姓名' }],
  suspectRole: [{ required: true, message: '请选择角色' }]
}

const genderLabel = (v) => ({ male: '男', female: '女', unknown: '未知' })[v] || '-'
const suspectRoleType = (v) => ({ '主犯': 'danger', '从犯': 'warning', '嫌疑人': 'info', '在逃': 'danger', '已抓获': 'success' })[v] || 'info'

function resetSuspectForm() {
  Object.assign(suspectForm, { id: null, name: '', gender: 'unknown', age: null, idCard: '', phone: '', address: '', suspectRole: '嫌疑人', description: '' })
}

function openAddSuspect() {
  resetSuspectForm()
  suspectFormVisible.value = true
}

function openEditSuspect(row) {
  Object.assign(suspectForm, { ...row })
  suspectFormVisible.value = true
}

async function handleSaveSuspect() {
  await suspectFormRef.value.validate()
  suspectSaving.value = true
  try {
    const { id, ...payload } = suspectForm
    if (id) {
      await caseApi.updateSuspect(currentCase.value.id, id, payload)
      ElMessage.success('已更新')
    } else {
      await caseApi.addSuspect(currentCase.value.id, payload)
      ElMessage.success('已添加')
    }
    suspectFormVisible.value = false
    const res = await caseApi.listSuspect(currentCase.value.id)
    suspectList.value = res.data || []
  } finally { suspectSaving.value = false }
}

async function handleDelSuspect(row) {
  await ElMessageBox.confirm(`确定删除嫌疑人「${row.name}」？此操作不可恢复`, '确认删除', { type: 'warning' })
  await caseApi.delSuspect(currentCase.value.id, row.id)
  ElMessage.success('已删除')
  const res = await caseApi.listSuspect(currentCase.value.id)
  suspectList.value = res.data || []
}

onMounted(async () => {
  caseTypes.value = await dictApi.get('case_type')
  loadList()
})
</script>

<style scoped>
.progress-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.prog-time { font-size: 14px; color: #909399; margin-bottom: 4px; }
.prog-content { font-size: 14px; color: #303133; }
.prog-plan { font-size: 14px; color: #606266; margin-top: 4px; }
</style>
