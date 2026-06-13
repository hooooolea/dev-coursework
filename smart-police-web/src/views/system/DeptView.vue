<template>
  <el-row :gutter="12">
    <!-- 左侧：部门树 -->
    <el-col :span="8">
      <el-card shadow="never">
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span style="font-weight:600">部门结构</span>
            <el-button type="primary" :icon="Plus" size="small" @click="openCreate(null)">新增根部门</el-button>
          </div>
        </template>
        <el-tree
          :data="tree"
          :props="{ label: 'deptName', children: 'children' }"
          node-key="id"
          default-expand-all
          highlight-current
          @node-click="handleNodeClick"
          v-loading="treeLoading"
        >
          <template #default="{ node, data }">
            <span class="tree-node">
              <span>{{ data.deptName }}</span>
              <span class="tree-actions">
                <el-icon class="action-icon" @click.stop="openCreate(data)"><Plus /></el-icon>
                <el-icon class="action-icon" @click.stop="openEdit(data)"><Edit /></el-icon>
                <el-icon class="action-icon danger" @click.stop="handleDel(data)"><Delete /></el-icon>
              </span>
            </span>
          </template>
        </el-tree>
      </el-card>
    </el-col>

    <!-- 右侧：部门详情 -->
    <el-col :span="16">
      <el-card shadow="never">
        <template #header>
          <span style="font-weight:600">{{ currentDept ? currentDept.deptName + ' - 详情' : '请选择部门' }}</span>
        </template>
        <el-descriptions v-if="currentDept" :column="2" border>
          <el-descriptions-item label="部门名称">{{ currentDept.deptName }}</el-descriptions-item>
          <el-descriptions-item label="部门编码">{{ currentDept.deptCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentDept.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ currentDept.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="排序">{{ currentDept.sortOrder ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentDept.status === 1 ? 'success' : 'info'" size="small">
              {{ currentDept.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <el-empty v-else description="点击左侧部门查看详情" />
      </el-card>
    </el-col>
  </el-row>

  <!-- 新增/编辑弹窗 -->
  <el-dialog v-model="formVisible" :title="editRow ? '编辑部门' : '新增部门'" width="480px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="上级部门">
        <el-tree-select
          v-model="form.parentId"
          :data="flatForSelect"
          :props="{ label: 'deptName', value: 'id', children: 'children' }"
          placeholder="根部门（不选）"
          clearable
          check-strictly
          style="width:100%"
        />
      </el-form-item>
      <el-form-item label="部门名称" prop="deptName">
        <el-input v-model="form.deptName" />
      </el-form-item>
      <el-form-item label="部门编码">
        <el-input v-model="form.deptCode" />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item label="地址">
        <el-input v-model="form.address" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deptApi } from '@/api/system'

const tree = ref([])
const treeLoading = ref(false)
const currentDept = ref(null)

const formVisible = ref(false)
const submitting = ref(false)
const editRow = ref(null)
const formRef = ref()
const form = reactive({ parentId: null, deptName: '', deptCode: '', phone: '', address: '', sortOrder: 0, status: 1 })
const rules = { deptName: [{ required: true, message: '请输入部门名称' }] }

// 用于 TreeSelect 的树形数据（加一个根节点）
const flatForSelect = computed(() => tree.value)

async function loadTree() {
  treeLoading.value = true
  try {
    const res = await deptApi.tree()
    tree.value = res.data || []
  } finally { treeLoading.value = false }
}

function handleNodeClick(data) {
  currentDept.value = data
}

function openCreate(parentNode) {
  editRow.value = null
  Object.assign(form, { parentId: parentNode?.id ?? null, deptName: '', deptCode: '', phone: '', address: '', sortOrder: 0, status: 1 })
  formVisible.value = true
}

function openEdit(row) {
  editRow.value = row
  Object.assign(form, { parentId: row.parentId || null, deptName: row.deptName, deptCode: row.deptCode || '', phone: row.phone || '', address: row.address || '', sortOrder: row.sortOrder ?? 0, status: row.status ?? 1 })
  formVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  submitting.value = true
  const payload = { ...form, parentId: form.parentId ?? 0 }
  try {
    if (editRow.value) {
      await deptApi.update(editRow.value.id, payload)
      ElMessage.success('保存成功')
    } else {
      await deptApi.create(payload)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    currentDept.value = null
    loadTree()
  } finally { submitting.value = false }
}

async function handleDel(row) {
  await ElMessageBox.confirm(`确定删除部门「${row.deptName}」？`, '确认删除', { type: 'warning' })
  try {
    await deptApi.del(row.id)
    ElMessage.success('已删除')
    if (currentDept.value?.id === row.id) currentDept.value = null
    loadTree()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '删除失败')
  }
}

onMounted(loadTree)
</script>

<style scoped>
.tree-node {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 8px;
  font-size: 13px;
}
.tree-actions {
  display: none;
  gap: 6px;
}
.el-tree-node__content:hover .tree-actions {
  display: flex;
}
.action-icon {
  font-size: 14px;
  color: #909399;
  cursor: pointer;
}
.action-icon:hover { color: #1565c0; }
.action-icon.danger:hover { color: #f56c6c; }
</style>
