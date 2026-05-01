<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>任务书管理</span>
          <el-button type="primary" size="small" @click="handleCreate">新建任务书</el-button>
        </div>
      </template>
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="thesisId" label="论文ID" width="80" />
        <el-table-column label="课题背景" min-width="160">
          <template #default="{ row }">{{ (parseContentExtend(row).background || '-').substring(0, 50) }}</template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getProcessStatusType(row.processStatus)">{{ getProcessStatusText(row.processStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ parseDate(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 16px;" v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" @current-change="loadData" />

      <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑任务书' : '新建任务书'" width="700px">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item v-if="!isEdit" label="学生论文" prop="thesisId">
            <el-select v-model="form.thesisId" placeholder="选择学生论文">
              <el-option v-for="t in thesisList" :key="t.id" :label="t.studentName + ' - ' + (t.thesisTitle || '未定题')" :value="t.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="课题背景" prop="background">
            <el-input v-model="form.background" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item label="主要任务" prop="mainTask">
            <el-input v-model="form.mainTask" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item label="进度安排" prop="schedule">
            <el-input v-model="form.schedule" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item label="参考文献要求">
            <el-input v-model="form.references" type="textarea" :rows="2" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="doSubmit">提交</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCurrentInstance } from 'vue'
import { getSupervisorTask, createTask, updateTask, getSupervisorStudents } from '@/api/degree'
import { parseContentExtend } from '@/views/degree/processConfig'
import { getProcessStatusText, getProcessStatusType, parseDate } from '@/composables/useDegreeStatus'

const { proxy } = getCurrentInstance()
const userStore = proxy.$pinia._s.get('user')

const loading = ref(false)
const records = ref([])
const thesisList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = ref({ thesisId: null, background: '', mainTask: '', schedule: '', references: '' })
const rules = {
  background: [{ required: true, message: '请输入课题背景', trigger: 'blur' }],
  mainTask: [{ required: true, message: '请输入主要任务', trigger: 'blur' }],
  schedule: [{ required: true, message: '请输入进度安排', trigger: 'blur' }]
}

function handleCreate() {
  isEdit.value = false
  form.value = { thesisId: null, background: '', mainTask: '', schedule: '', references: '' }
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  const ext = parseContentExtend(row)
  form.value = { id: row.id, background: ext.background || '', mainTask: ext.mainTask || '', schedule: ext.schedule || '', references: ext.references || '' }
  dialogVisible.value = true
}

function doSubmit() {
  formRef.value?.validate(valid => {
    if (!valid) return
    if (isEdit.value) {
      updateTask(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); dialogVisible.value = false; loadData() })
    } else {
      createTask(form.value).then(() => { proxy.$modal.msgSuccess('创建成功'); dialogVisible.value = false; loadData() })
    }
  })
}

async function loadData() {
  loading.value = true
  try {
    const [taskRes, studentRes] = await Promise.all([
      getSupervisorTask({ supervisorId: userStore?.id, pageNum: pageNum.value, pageSize: pageSize.value }),
      getSupervisorStudents(userStore?.id)
    ])
    records.value = taskRes.data?.records || taskRes.rows || []
    total.value = taskRes.data?.total || taskRes.total || 0
    thesisList.value = studentRes.data || studentRes || []
  } catch (e) { console.error(e) }
  loading.value = false
}

onMounted(() => loadData())
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
