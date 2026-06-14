<template>
  <div>
    <el-card shadow="never">
      <el-form inline :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="姓名/身份证后4位" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="人员类型">
          <el-select v-model="query.personType" clearable placeholder="全部" style="width:120px">
            <el-option label="普通人员" value="normal" />
            <el-option label="重点关注" value="focus" />
            <el-option label="在逃人员" value="wanted" />
            <el-option label="涉毒人员" value="drug" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
          <el-button :icon="Plus" @click="openCreate">新增人员</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <template #empty>
          <el-empty description="暂无数据" :image-size="80" />
        </template>
        <el-table-column label="照片" width="80" align="center">
          <template #default="{ row }">
            <el-avatar :src="row.photoUrl" :size="36" :icon="UserFilled" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="90" />
        <el-table-column prop="gender" label="性别" width="70" align="center">
          <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="idCardTail" label="身份证尾号" width="110" align="center">
          <template #default="{ row }">****{{ row.idCardTail }}</template>
        </el-table-column>
        <el-table-column prop="phoneTail" label="手机尾号" width="100" align="center">
          <template #default="{ row }">****{{ row.phoneTail }}</template>
        </el-table-column>
        <el-table-column prop="currentAddr" label="现居地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="personType" label="人员类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="personTypeTag(row.personType)" size="small">{{ personTypeLabel(row.personType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
            <el-button type="warning" link size="small" @click="openLabel(row)">标注</el-button>
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

    <!-- 新增人员 -->
    <el-dialog v-model="createVisible" title="新增人员档案" width="580px">
      <el-form ref="createFormRef" :model="createForm" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="createForm.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="createForm.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="身份证号">
          <el-input v-model="createForm.idCard" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="createForm.phone" />
        </el-form-item>
        <el-form-item label="户籍地址">
          <el-input v-model="createForm.householdAddr" />
        </el-form-item>
        <el-form-item label="现居住地">
          <el-input v-model="createForm.currentAddr" />
        </el-form-item>
        <el-form-item label="职业">
          <el-input v-model="createForm.occupation" />
        </el-form-item>
        <el-form-item label="照片">
          <el-upload
            ref="createAvatarUploadRef"
            :auto-upload="false"
            :limit="1"
            :show-file-list="false"
            :on-change="handleCreateAvatarChange"
            :on-exceed="handleCreateAvatarExceed"
            accept="image/jpeg,image/png,.jpg,.jpeg,.png"
          >
            <el-button type="primary" plain :icon="Upload" size="small">选择照片</el-button>
          </el-upload>
          <div v-if="createAvatarPreview" class="create-avatar-preview">
            <el-avatar :src="createAvatarPreview" :size="48" />
            <span class="preview-meta">{{ createAvatarFile?.name }}</span>
          </div>
          <div class="upload-tip">仅支持 jpg/png，单文件 ≤ 2MB</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉（含头像上传） -->
    <el-drawer v-model="detailVisible" :title="`人员详情 - ${currentPerson?.name || ''}`" size="480px">
      <div v-if="currentPerson" class="detail-wrap">
        <div class="avatar-block">
          <el-avatar :src="currentPerson.photoUrl" :size="96" :icon="UserFilled" />
          <div class="avatar-name">{{ currentPerson.name }}</div>
          <div class="avatar-tags">
            <el-tag :type="personTypeTag(currentPerson.personType)" size="small">
              {{ personTypeLabel(currentPerson.personType) }}
            </el-tag>
          </div>
        </div>

        <el-divider />

        <div class="info-grid">
          <div class="info-row"><span class="info-key">性别</span><span>{{ currentPerson.gender === 1 ? '男' : '女' }}</span></div>
          <div class="info-row"><span class="info-key">身份证尾号</span><span>****{{ currentPerson.idCardTail }}</span></div>
          <div class="info-row"><span class="info-key">手机尾号</span><span>****{{ currentPerson.phoneTail }}</span></div>
          <div class="info-row"><span class="info-key">户籍地址</span><span>{{ currentPerson.householdAddr || '-' }}</span></div>
          <div class="info-row"><span class="info-key">现居地址</span><span>{{ currentPerson.currentAddr || '-' }}</span></div>
          <div class="info-row"><span class="info-key">职业</span><span>{{ currentPerson.occupation || '-' }}</span></div>
          <div class="info-row" v-if="currentPerson.typeReason">
            <span class="info-key">标注原因</span><span>{{ currentPerson.typeReason }}</span>
          </div>
        </div>

        <el-divider />

        <div class="upload-block">
          <div class="upload-title">更新头像</div>
          <el-upload
            ref="avatarUploadRef"
            :auto-upload="false"
            :limit="1"
            :show-file-list="false"
            :on-change="handleAvatarChange"
            :on-exceed="handleAvatarExceed"
            accept="image/jpeg,image/png,image/gif,image/webp,.jpg,.jpeg,.png,.gif,.webp"
          >
            <el-button type="primary" plain :icon="Upload">选择新头像</el-button>
          </el-upload>
          <div v-if="avatarPreview" class="avatar-preview">
            <el-avatar :src="avatarPreview" :size="64" />
            <span class="preview-meta">预览 · {{ avatarPreviewFile?.name }} ({{ formatSize(avatarPreviewFile?.size) }})</span>
          </div>
          <div class="upload-tip">支持 jpg/png/gif/webp，单文件 ≤ 2MB</div>
          <div style="margin-top:12px">
            <el-button type="success" :loading="avatarUploading" :disabled="!avatarPreviewFile" @click="submitAvatar">
              {{ avatarUploading ? '上传中...' : '确认上传' }}
            </el-button>
            <el-button v-if="avatarPreview" @click="cancelAvatarPick">取消</el-button>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 标注类型 -->
    <el-dialog v-model="labelVisible" title="标注人员类型" width="400px">
      <el-form :model="labelForm" label-width="80px">
        <el-form-item label="人员类型">
          <el-select v-model="labelForm.type" style="width:100%">
            <el-option label="普通人员" value="normal" />
            <el-option label="重点关注" value="focus" />
            <el-option label="在逃人员" value="wanted" />
            <el-option label="社区矫正" value="correction" />
            <el-option label="涉毒人员" value="drug" />
          </el-select>
        </el-form-item>
        <el-form-item label="标注原因">
          <el-input v-model="labelForm.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="labelVisible = false">取消</el-button>
        <el-button type="primary" @click="handleLabel">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus, Upload, UserFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { personApi } from '@/api/person'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ keyword: '', personType: '', page: 1, size: 20 })

const createVisible = ref(false)
const submitting = ref(false)
const createFormRef = ref()
const createForm = reactive({ name: '', gender: 1, idCard: '', phone: '', householdAddr: '', currentAddr: '', occupation: '' })

// 新增弹窗内的照片上传
const createAvatarUploadRef = ref()
const createAvatarPreview = ref('')
const createAvatarFile = ref(null)
const CREATE_AVATAR_MAX_BYTES = 2 * 1024 * 1024

const labelVisible = ref(false)
const currentPerson = ref(null)
const labelForm = reactive({ type: 'normal', reason: '' })

// 详情抽屉 + 头像上传
const detailVisible = ref(false)
const avatarUploadRef = ref()
const avatarPreview = ref(null)
const avatarPreviewFile = ref(null)
const avatarUploading = ref(false)
const AVATAR_MAX_BYTES = 2 * 1024 * 1024 // 2MB
const AVATAR_ALLOWED_EXT = ['jpg', 'jpeg', 'png', 'gif', 'webp']

const personTypeLabel = (v) => ({ normal: '普通', focus: '重点关注', wanted: '在逃', correction: '社区矫正', drug: '涉毒' })[v] || v
const personTypeTag   = (v) => ({ normal: 'info', focus: 'warning', wanted: 'danger', correction: '', drug: 'danger' })[v] || 'info'

const formatSize = (n) => {
  if (!n && n !== 0) return '-'
  if (n < 1024) return `${n} B`
  if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`
  return `${(n / 1024 / 1024).toFixed(2)} MB`
}

function openCreate() {
  Object.assign(createForm, { name: '', gender: 1, idCard: '', phone: '', householdAddr: '', currentAddr: '', occupation: '' })
  createAvatarPreview.value = ''
  createAvatarFile.value = null
  createAvatarUploadRef.value?.clearFiles()
  createVisible.value = true
}

async function loadList() {
  loading.value = true
  try {
    const res = await personApi.list(query)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function handleCreate() {
  await createFormRef.value.validate()
  submitting.value = true
  try {
    let photoUrl = ''
    if (createAvatarFile.value) {
      const formData = new FormData()
      formData.append('file', createAvatarFile.value)
      formData.append('biz', 'avatar')
      const upRes = await request.post('/file/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      photoUrl = upRes.data?.accessUrl || ''
    }
    await personApi.create({ ...createForm, photoUrl })
    ElMessage.success('人员档案已录入')
    createVisible.value = false
    loadList()
  } finally { submitting.value = false }
}

async function viewDetail(row) {
  // 拉一次最新详情，保证 photoUrl 最新
  try {
    const res = await personApi.detail(row.id)
    currentPerson.value = res.data || row
  } catch {
    currentPerson.value = row
  }
  resetAvatarPick()
  detailVisible.value = true
}

async function handleDel(row) {
  await ElMessageBox.confirm(`确定删除人员「${row.name}」？`, '确认删除', { type: 'warning' })
  await personApi.del(row.id)
  ElMessage.success('已删除')
  loadList()
}

function openLabel(row) {
  currentPerson.value = row
  labelForm.type = row.personType || 'normal'
  labelForm.reason = ''
  labelVisible.value = true
}

async function handleLabel() {
  await personApi.label(currentPerson.value.id, labelForm.type, labelForm.reason)
  ElMessage.success('标注成功')
  labelVisible.value = false
  loadList()
}

// 新增弹窗照片上传逻辑
function handleCreateAvatarChange(file) {
  if (file.size > CREATE_AVATAR_MAX_BYTES) {
    ElMessage.error('照片文件超过 2MB 限制')
    return
  }
  const raw = file.raw
  const name = raw.name || ''
  const ext = name.includes('.') ? name.split('.').pop().toLowerCase() : ''
  if (!['jpg', 'jpeg', 'png'].includes(ext)) {
    ElMessage.error('仅支持 jpg / png 格式')
    return
  }
  createAvatarFile.value = raw
  const reader = new FileReader()
  reader.onload = (e) => { createAvatarPreview.value = e.target.result }
  reader.readAsDataURL(raw)
}

function handleCreateAvatarExceed() {
  ElMessage.warning('只能选择一张照片')
}

// 头像上传逻辑
function handleAvatarChange(file) {
  // 前端二次校验
  if (file.size > AVATAR_MAX_BYTES) {
    ElMessage.error('头像文件超过 2MB 限制')
    return
  }
  const raw = file.raw
  const name = raw.name || ''
  const ext = name.includes('.') ? name.split('.').pop().toLowerCase() : ''
  if (!AVATAR_ALLOWED_EXT.includes(ext)) {
    ElMessage.error('仅支持 jpg / png / gif / webp 格式')
    return
  }
  avatarPreviewFile.value = raw
  // FileReader 生成预览
  const reader = new FileReader()
  reader.onload = (e) => { avatarPreview.value = e.target.result }
  reader.readAsDataURL(raw)
}

function handleAvatarExceed() {
  ElMessage.warning('只能选择一张图片，请先取消当前选择')
}

function cancelAvatarPick() {
  resetAvatarPick()
}

function resetAvatarPick() {
  avatarPreview.value = null
  avatarPreviewFile.value = null
}

async function submitAvatar() {
  if (!avatarPreviewFile.value || !currentPerson.value) return
  avatarUploading.value = true
  try {
    const res = await personApi.uploadAvatar(currentPerson.value.id, avatarPreviewFile.value)
    const newUrl = res.data?.photoUrl
    // 局部更新：详情抽屉里的 photoUrl + 列表里的 photoUrl
    if (currentPerson.value) currentPerson.value.photoUrl = newUrl
    const row = list.value.find(r => r.id === currentPerson.value.id)
    if (row) row.photoUrl = newUrl
    ElMessage.success('头像已更新')
    resetAvatarPick()
  } catch (e) {
    // request.js 已统一弹错，这里无需重复提示
  } finally {
    avatarUploading.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.detail-wrap { padding: 0 8px; }
.avatar-block { text-align: center; padding: 12px 0 8px; }
.avatar-name { font-size: 18px; font-weight: 600; margin-top: 12px; }
.avatar-tags { margin-top: 8px; }
.info-grid { display: grid; grid-template-columns: 1fr; gap: 6px 0; padding: 0 4px; }
.info-row { display: flex; padding: 6px 0; font-size: 14px; border-bottom: 1px dashed #f0f0f0; }
.info-key { width: 100px; color: #909399; flex-shrink: 0; }
.create-avatar-preview { display: flex; align-items: center; gap: 10px; margin-top: 8px; }
.upload-block { padding: 0 4px; }
.upload-title { font-size: 14px; font-weight: 600; margin-bottom: 12px; }
.upload-tip { font-size: 14px; color: #909399; margin-top: 6px; }
.avatar-preview { display: flex; align-items: center; gap: 12px; margin-top: 12px; padding: 8px; background: #fafafa; border-radius: 6px; }
.preview-meta { font-size: 14px; color: #606266; }
</style>
