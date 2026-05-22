<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>进度查询</span>
          <el-button type="success" size="small">导出Excel</el-button>
        </div>
      </template>

      <!-- 筛选栏 -->
      <el-form :inline="true" :model="queryForm" style="margin-bottom: 16px;">
        <el-form-item label="专业"><el-input v-model="queryForm.major" placeholder="专业" clearable /></el-form-item>
        <el-form-item label="环节">
          <el-select v-model="queryForm.processType" placeholder="全部" clearable>
            <el-option v-for="opt in PROCESS_TYPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="handleQuery">查询</el-button></el-form-item>
      </el-form>

      <!-- 批量进度表格 -->
      <el-table :data="studentProgress" v-loading="loading" stripe border>
        <el-table-column prop="studentName" label="学生" width="100" fixed />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="thesisTitle" label="论文题目" min-width="160" />
        <el-table-column v-for="pt in PROCESS_TYPE_OPTIONS" :key="pt.value" :label="pt.label" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.progress[pt.value])" size="small">
              {{ getStatusText(row.progress[pt.value]) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listThesisMain, listProcess } from '@/api/degree'
import { PROCESS_TYPE_OPTIONS } from '@/views/degree/processConfig'
import { getProcessStatusText, getProcessStatusType } from '@/composables/useDegreeStatus'

const loading = ref(false)
const queryForm = ref({ major: '', processType: null })
const studentProgress = ref([])
const pagination = ref({ pageNum: 1, pageSize: 10, total: 0 })

function getStatusText(status) {
  if (status === undefined || status === null) return '未提交'
  return getProcessStatusText(status)
}

function getStatusType(status) {
  if (status === undefined || status === null) return 'info'
  return getProcessStatusType(status)
}

function handleQuery() {
  pagination.value.pageNum = 1
  loadData()
}

function handleSizeChange(val) {
  pagination.value.pageSize = val
  pagination.value.pageNum = 1
  loadData()
}

function handleCurrentChange(val) {
  pagination.value.pageNum = val
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const thesisRes = await listThesisMain({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize
    })
    const thesisList = thesisRes.data || thesisRes.rows || []
    pagination.value.total = thesisRes.pagination?.total ?? 0

    const results = []
    for (const thesis of thesisList) {
      const processRes = await listProcess({ thesisId: thesis.id, pageSize: 50 })
      const records = processRes.data || processRes.rows || []

      const progress = {}
      for (const r of records) {
        const existing = progress[r.processType]
        if (!existing || r.version > (existing.version || 0)) {
          progress[r.processType] = { status: r.processStatus, version: r.version }
        }
      }

      const progressMap = {}
      for (const [k, v] of Object.entries(progress)) {
        progressMap[k] = v.status
      }

      results.push({
        studentName: thesis.studentName || '-',
        studentNo: thesis.studentNo || '-',
        thesisTitle: thesis.thesisTitle || '-',
        progress: progressMap
      })
    }

    studentProgress.value = results
  } catch (e) { console.error(e) }
  loading.value = false
}

onMounted(() => loadData())
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
