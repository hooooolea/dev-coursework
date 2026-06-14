<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span style="font-weight:600">角色管理</span>
          <el-button type="primary" :icon="Plus" @click="openCreate">新增角色</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="roleCode" label="角色编码" width="180" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="openPermAssign(row)">分配权限</el-button>
            <el-button type="danger" link size="small" @click="handleDel(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑角色 -->
    <el-dialog v-model="formVisible" :title="editRow ? '编辑角色' : '新增角色'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="!!editRow" placeholder="如 ROLE_OFFICER" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 权限分配 -->
    <el-dialog v-model="permVisible" :title="`分配权限 - ${currentRole?.roleName}`" width="520px">
      <el-tree
        ref="permTreeRef"
        :data="permTree"
        :props="{ label: 'permName', children: 'children' }"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedPermIds"
        check-strictly
        style="max-height:460px;overflow-y:auto"
      />
      <template #footer>
        <el-button @click="permVisible = false">取消</el-button>
        <el-button type="primary" :loading="permSaving" @click="handleSavePerms">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roleApi } from '@/api/system'

const list = ref([])
const loading = ref(false)

const formVisible = ref(false)
const submitting = ref(false)
const editRow = ref(null)
const formRef = ref()
const form = reactive({ roleCode: '', roleName: '', description: '', sortOrder: 0, status: 1 })
const rules = {
  roleCode: [{ required: true, message: '请输入角色编码' }],
  roleName: [{ required: true, message: '请输入角色名称' }]
}

const permVisible = ref(false)
const permSaving = ref(false)
const currentRole = ref(null)
const permTree = ref([])
const checkedPermIds = ref([])
const permTreeRef = ref()

async function loadList() {
  loading.value = true
  try {
    const res = await roleApi.list()
    list.value = res.data || []
  } finally { loading.value = false }
}

function openCreate() {
  editRow.value = null
  Object.assign(form, { roleCode: '', roleName: '', description: '', sortOrder: 0, status: 1 })
  formVisible.value = true
}

function openEdit(row) {
  editRow.value = row
  Object.assign(form, { roleCode: row.roleCode, roleName: row.roleName, description: row.description, sortOrder: row.sortOrder ?? 0, status: row.status ?? 1 })
  formVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editRow.value) {
      await roleApi.update(editRow.value.id, form)
      ElMessage.success('保存成功')
    } else {
      await roleApi.create(form)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    loadList()
  } finally { submitting.value = false }
}

async function handleDel(row) {
  await ElMessageBox.confirm(`确定删除角色「${row.roleName}」？`, '确认删除', { type: 'warning' })
  await roleApi.del(row.id)
  ElMessage.success('已删除')
  loadList()
}

async function openPermAssign(row) {
  currentRole.value = row
  permVisible.value = true
  const [treeRes, checkedRes] = await Promise.all([roleApi.permTree(), roleApi.getPerms(row.id)])
  permTree.value = treeRes.data || []
  checkedPermIds.value = checkedRes.data || []
}

async function handleSavePerms() {
  permSaving.value = true
  try {
    const ids = permTreeRef.value.getCheckedKeys()
    await roleApi.assignPerms(currentRole.value.id, ids)
    ElMessage.success('权限已保存')
    permVisible.value = false
  } finally { permSaving.value = false }
}

onMounted(loadList)
</script>
