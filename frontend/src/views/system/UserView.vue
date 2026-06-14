<template>
  <div>
    <el-card shadow="never">
      <el-form inline :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="姓名或警号" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:100px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
          <el-button :icon="Plus" @click="openCreate">新增用户</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="badgeNo"  label="警号"   width="120" />
        <el-table-column prop="realName" label="姓名"   min-width="100" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="gender"   label="性别"   width="70" align="center">
          <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="phone"    label="手机号" min-width="130" />
        <el-table-column prop="status"   label="状态"   width="80" align="center">
          <template #default="{ row }">
            <span :class="row.status === 1 ? 'tag-normal' : 'tag-danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最后登录" width="160" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="openAssignRole(row)">分配角色</el-button>
            <el-button type="primary" link size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button link size="small" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editId ? '编辑用户' : '新增用户'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="警号" prop="badgeNo">
              <el-input v-model="form.badgeNo" :disabled="!!editId" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" :disabled="!!editId" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="!editId" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色 -->
    <el-dialog v-model="roleVisible" :title="`分配角色 — ${roleTarget?.realName}`" width="400px">
      <el-checkbox-group v-model="selectedRoleIds" v-loading="roleLoading">
        <div v-for="role in allRoles" :key="role.id" style="margin-bottom:10px">
          <el-checkbox :value="role.id">
            <span style="font-weight:600">{{ role.roleName }}</span>
            <span style="font-size:12px;color:#909399;margin-left:8px">{{ role.roleCode }}</span>
            <div style="font-size:12px;color:#606266;margin-left:22px">{{ role.description }}</div>
          </el-checkbox>
        </div>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSaving" @click="handleSaveRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api/system'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ keyword: '', status: null, page: 1, size: 20 })

const dialogVisible = ref(false)
const submitting = ref(false)
const editId = ref(null)
const formRef = ref()
const form = reactive({ badgeNo: '', username: '', realName: '', gender: 1, password: '', phone: '', email: '' })

const rules = {
  badgeNo:  [{ required: true, message: '请输入警号' }],
  username: [{ required: true, message: '请输入用户名' }],
  realName: [{ required: true, message: '请输入姓名' }],
  password: [{ required: true, message: '请输入密码' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await userApi.list(query)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function openCreate() {
  editId.value = null
  Object.assign(form, { badgeNo: '', username: '', realName: '', gender: 1, password: '', phone: '', email: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  editId.value = row.id
  Object.assign(form, { badgeNo: row.badgeNo, username: row.username, realName: row.realName, gender: row.gender, phone: row.phone || '', email: row.email || '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editId.value) {
      await userApi.update(editId.value, form)
      ElMessage.success('修改成功')
    } else {
      await userApi.create(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally { submitting.value = false }
}

async function handleResetPwd(row) {
  const { value } = await ElMessageBox.prompt(`重置 ${row.realName} 的密码`, '重置密码', {
    inputPlaceholder: '请输入新密码',
    inputType: 'password',
    inputValidator: (val) => {
      if (!val || val.length < 8) return '密码需至少8位，包含字母和数字'
      if (!/[a-zA-Z]/.test(val)) return '密码需至少8位，包含字母和数字'
      if (!/\d/.test(val)) return '密码需至少8位，包含字母和数字'
      return true
    }
  })
  // Client-side validation safety net
  if (!value || value.length < 8 || !/[a-zA-Z]/.test(value) || !/\d/.test(value)) {
    ElMessage.warning('密码需至少8位，包含字母和数字')
    return
  }
  await userApi.resetPassword(row.id, value)
  ElMessage.success('密码已重置')
}

async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  await userApi.toggleStatus(row.id, newStatus)
  ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  loadList()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除用户 ${row.realName}？`, '确认', { type: 'warning' })
  await userApi.del(row.id)
  ElMessage.success('已删除')
  loadList()
}

// 角色分配
const roleVisible      = ref(false)
const roleLoading      = ref(false)
const roleSaving       = ref(false)
const roleTarget       = ref(null)
const allRoles         = ref([])
const selectedRoleIds  = ref([])

async function openAssignRole(row) {
  roleTarget.value = row
  roleVisible.value = true
  roleLoading.value = true
  try {
    const [rolesRes, currentRes] = await Promise.all([
      userApi.allRoles(),
      userApi.getUserRoles(row.id)
    ])
    allRoles.value        = rolesRes.data  || []
    selectedRoleIds.value = currentRes.data || []
  } finally { roleLoading.value = false }
}

async function handleSaveRoles() {
  roleSaving.value = true
  try {
    await userApi.assignRoles(roleTarget.value.id, selectedRoleIds.value)
    ElMessage.success('角色已保存')
    roleVisible.value = false
  } finally { roleSaving.value = false }
}

onMounted(loadList)
</script>

<style scoped>
.tag-normal { display:inline-block; padding:1px 8px; background:#f4f4f5; color:#606266; border-radius:2px; font-size:14px; }
.tag-danger { display:inline-block; padding:1px 8px; background:#fef0f0; color:#f56c6c; border-radius:2px; font-size:14px; }
</style>
