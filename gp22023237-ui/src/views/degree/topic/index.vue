<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>选题</span>
          <el-button v-if="canSubmit" type="primary" size="small" @click="handleSubmit">申报选题</el-button>
        </div>
      </template>

      <!-- 当前选题状态 -->
      <div v-if="currentRecord" class="topic-status">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="课题名称">{{ currentRecord.thesisTitle || extendData.topicName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="课题来源">{{ extendData.topicSource || '-' }}</el-descriptions-item>
          <el-descriptions-item label="课题说明" :span="2">{{ extendData.topicDesc || '-' }}</el-descriptions-item>
          <el-descriptions-item label="预期目标" :span="2">{{ extendData.expectedGoal || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="statusType">{{ statusText }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ parseDate(currentRecord.submitTime) }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRecord.supervisorComment" label="导师意见" :span="2">
            {{ currentRecord.supervisorComment }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 未提交状态 -->
      <el-empty v-else description="暂未申报选题，请点击右上角申报" />

      <!-- 选题申报对话框 -->
      <el-dialog v-model="dialogVisible" title="申报选题" width="600px">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="课题名称" prop="topicName">
            <el-input v-model="form.topicName" placeholder="请输入课题名称" />
          </el-form-item>
          <el-form-item label="课题来源" prop="topicSource">
            <el-select v-model="form.topicSource" placeholder="请选择课题来源">
              <el-option label="导师指定" value="导师指定" />
              <el-option label="学生自选" value="学生自选" />
              <el-option label="科研项目" value="科研项目" />
              <el-option label="生产/社会实际" value="生产/社会实际" />
            </el-select>
          </el-form-item>
          <el-form-item label="课题说明" prop="topicDesc">
            <el-input v-model="form.topicDesc" type="textarea" :rows="3" placeholder="请输入课题说明" />
          </el-form-item>
          <el-form-item label="预期目标" prop="expectedGoal">
            <el-input v-model="form.expectedGoal" type="textarea" :rows="3" placeholder="请输入预期目标" />
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
import { ref, computed, onMounted } from 'vue'
import { getCurrentInstance } from 'vue'
import { getThesisMainByStudent, listProcess, submitProcess } from '@/api/degree'
import { PROCESS_CONFIG, buildSubmitData, parseContentExtend } from '@/views/degree/processConfig'
import { getProcessStatusText, getProcessStatusType, parseDate } from '@/composables/useDegreeStatus'

const { proxy } = getCurrentInstance()
const userStore = proxy.$pinia._s.get('user')

const dialogVisible = ref(false)
const formRef = ref(null)
const currentRecord = ref(null)
const thesisId = ref(null)

const config = PROCESS_CONFIG[1]
const form = ref({ ...config.defaultForm })
const rules = config.rules

const extendData = computed(() => parseContentExtend(currentRecord.value))
const statusText = computed(() => getProcessStatusText(currentRecord.value?.processStatus))
const statusType = computed(() => getProcessStatusType(currentRecord.value?.processStatus))
const canSubmit = computed(() => !currentRecord.value || currentRecord.value.processStatus === 4)

function handleSubmit() {
  form.value = { ...config.defaultForm }
  if (currentRecord.value) {
    const ext = parseContentExtend(currentRecord.value)
    form.value.topicName = ext.topicName || ''
    form.value.topicSource = ext.topicSource || ''
    form.value.topicDesc = ext.topicDesc || ''
    form.value.expectedGoal = ext.expectedGoal || ''
  }
  dialogVisible.value = true
}

function doSubmit() {
  formRef.value?.validate(valid => {
    if (!valid) return
    const data = buildSubmitData({ ...form.value, thesisId: thesisId.value }, 1)
    submitProcess(data).then(() => {
      proxy.$modal.msgSuccess('提交成功')
      dialogVisible.value = false
      loadData()
    })
  })
}

async function loadData() {
  try {
    const studentId = userStore?.id
    if (!studentId) return
    const res = await getThesisMainByStudent(studentId)
    const thesis = res.data || res
    if (thesis && thesis.id) {
      thesisId.value = thesis.id
      const processRes = await listProcess({ thesisId: thesis.id, processType: 1, pageSize: 1 })
      const records = processRes.data?.records || processRes.rows || []
      if (records.length > 0) {
        currentRecord.value = records.reduce((a, b) => a.version > b.version ? a : b)
      }
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.topic-status { margin-top: 10px; }
</style>
