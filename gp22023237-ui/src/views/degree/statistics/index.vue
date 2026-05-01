<template>
  <div class="app-container">
    <!-- 统计概览卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #409eff"><el-icon :size="28"><Document /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.total || 0 }}</div>
            <div class="stat-label">论文总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #67c23a"><el-icon :size="28"><CircleCheck /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.passedCount || 0 }}</div>
            <div class="stat-label">已通过</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #f56c6c"><el-icon :size="28"><CircleClose /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.failedCount || 0 }}</div>
            <div class="stat-label">未通过</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #e6a23c"><el-icon :size="28"><FolderChecked /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.archivedCount || 0 }}</div>
            <div class="stat-label">已归档</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">论文结果分布</span></template>
          <div ref="resultChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">各环节通过率</span></template>
          <div ref="processChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 论文管理表格 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">论文记录管理</span>
          <div>
            <el-button type="warning" :disabled="selectedIds.length === 0" @click="handleBatchArchive">
              批量归档 ({{ selectedIds.length }})
            </el-button>
          </div>
        </div>
      </template>

      <!-- 高级搜索区域 -->
      <el-form :model="queryParams" inline style="margin-bottom: 16px">
        <el-form-item label="学号">
          <el-input v-model="queryParams.studentNo" placeholder="请输入学号" clearable style="width: 140px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="queryParams.studentName" placeholder="请输入姓名" clearable style="width: 120px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="queryParams.major" placeholder="请输入专业" clearable style="width: 140px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="queryParams.grade" placeholder="请输入年级" clearable style="width: 100px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="导师">
          <el-input v-model="queryParams.supervisorName" placeholder="请输入导师" clearable style="width: 120px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="论文结果">
          <el-select v-model="queryParams.finalResult" placeholder="全部" clearable style="width: 120px">
            <el-option label="进行中" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="未通过" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="归档状态">
          <el-select v-model="queryParams.archiveStatus" placeholder="全部" clearable style="width: 120px">
            <el-option label="未归档" :value="0" />
            <el-option label="已归档" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="逾期状态">
          <el-select v-model="queryParams.overdueFilter" placeholder="全部" clearable style="width: 120px">
            <el-option label="逾期" value="overdue" />
            <el-option label="即将逾期" value="warning" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button @click="toggleAdvancedSearch">{{ showAdvancedSearch ? '收起' : '更多筛选' }}</el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="loading"
        :data="filteredList"
        border
        @selection-change="handleSelectionChange"
        :row-class-name="tableRowClassName"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="学号" prop="studentNo" width="120" align="center" />
        <el-table-column label="姓名" prop="studentName" width="90" align="center" />
        <el-table-column label="专业" prop="major" width="120" show-overflow-tooltip />
        <el-table-column label="年级" prop="grade" width="80" align="center" />
        <el-table-column label="论文题目" prop="thesisTitle" min-width="200" show-overflow-tooltip />
        <el-table-column label="导师" prop="supervisorName" width="90" align="center" />
        <el-table-column label="论文结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getFinalResultType(row.finalResult)" size="small">
              {{ getFinalResultText(row.finalResult) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前环节" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" v-if="row._currentStep">{{ row._currentStep }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="逾期" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row._overdue" type="danger" size="small" effect="dark">逾期</el-tag>
            <el-tag v-else-if="row._warning" type="warning" size="small">预警</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="归档状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.archiveStatus === 1 ? 'success' : 'info'" size="small">
              {{ row.archiveStatus === 1 ? '已归档' : '未归档' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="归档时间" width="160" align="center">
          <template #default="{ row }">
            {{ parseDate(row.archiveTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.archiveStatus !== 1" link type="warning" size="small" @click="handleArchive(row)">
              归档
            </el-button>
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; justify-content: flex-end"
        @size-change="getList"
        @current-change="getList"
      />
    </el-card>

    <!-- 学生论文详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" title="论文流程详情" size="55%">
      <template v-if="currentThesis">
        <el-descriptions :column="2" border style="margin-bottom: 20px">
          <el-descriptions-item label="学号">{{ currentThesis.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentThesis.studentName }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ currentThesis.major }}</el-descriptions-item>
          <el-descriptions-item label="导师">{{ currentThesis.supervisorName }}</el-descriptions-item>
          <el-descriptions-item label="论文题目" :span="2">{{ currentThesis.thesisTitle }}</el-descriptions-item>
        </el-descriptions>

        <el-timeline>
          <el-timeline-item
            v-for="step in processSteps"
            :key="step.type"
            :timestamp="step.label"
            :type="getTimelineColor(step.type)"
            placement="top"
          >
            <div v-if="detailProcessMap[step.type] && detailProcessMap[step.type].length > 0">
              <el-table :data="detailProcessMap[step.type]" border size="small">
                <el-table-column label="版本" prop="version" width="60" align="center" />
                <el-table-column label="导师" width="80" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getApprovalStatusType(row.supervisorStatus)" size="small">
                      {{ getApprovalStatusText(row.supervisorStatus) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="秘书" width="80" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getApprovalStatusType(row.secretaryStatus)" size="small">
                      {{ getApprovalStatusText(row.secretaryStatus) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="院长" width="80" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getApprovalStatusType(row.deanStatus)" size="small">
                      {{ getApprovalStatusText(row.deanStatus) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="80" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getProcessStatusType(row.processStatus)" size="small">
                      {{ getProcessStatusText(row.processStatus) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="提交时间" width="140" align="center">
                  <template #default="{ row }">{{ parseDate(row.submitTime) }}</template>
                </el-table-column>
              </el-table>
            </div>
            <el-text type="info" size="small" v-else>暂无记录</el-text>
          </el-timeline-item>
        </el-timeline>
      </template>
    </el-drawer>
  </div>
</template>

<script setup name="ThesisStatistics">
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { listThesisMain, archiveThesis, batchArchiveThesis, getThesisStatistics, listProcess } from '@/api/degree'
import * as echarts from 'echarts'
import { Document, CircleCheck, CircleClose, FolderChecked } from '@element-plus/icons-vue'
import {
  getFinalResultText, getFinalResultType,
  getApprovalStatusText, getApprovalStatusType,
  getProcessStatusText, getProcessStatusType,
  parseDate, getTimelineType
} from '@/composables/useDegreeStatus'

// ==================== 统计数据 ====================
const stats = ref({})
const resultChartRef = ref(null)
const processChartRef = ref(null)
let resultChart = null
let processChart = null

// ==================== 表格数据 ====================
const loading = ref(false)
const thesisList = ref([])
const total = ref(0)
const selectedIds = ref([])
const showAdvancedSearch = ref(false)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  studentNo: undefined,
  studentName: undefined,
  major: undefined,
  grade: undefined,
  supervisorName: undefined,
  finalResult: undefined,
  archiveStatus: undefined,
  overdueFilter: undefined
})

// ==================== 详情抽屉 ====================
const detailDrawerVisible = ref(false)
const currentThesis = ref(null)
const detailProcessMap = ref({})

const processSteps = [
  { type: 1, label: '开题报告' },
  { type: 2, label: '中期检查' },
  { type: 3, label: '预答辩' },
  { type: 4, label: '论文外审' },
  { type: 5, label: '正式答辩' },
  { type: 6, label: '二次答辩' },
  { type: 7, label: '修改后再审' }
]

// ==================== 逾期计算 ====================

// 从配置获取各环节截止时间
function getDeadlineConfig() {
  try {
    const stored = localStorage.getItem('degree_process_config')
    if (stored) {
      const data = JSON.parse(stored)
      return data.configMap || {}
    }
  } catch (e) { /* ignore */ }
  return {}
}

// 计算每条记录的逾期状态和当前环节
function enrichThesisData(list) {
  const configMap = getDeadlineConfig()

  return list.map(item => {
    const row = { ...item }
    // 计算当前环节：找到最后一个有记录且未通过的环节
    row._currentStep = null
    row._overdue = false
    row._warning = false

    // 这里只能做简单判断，因为流程记录需要单独查询
    // 基于论文状态做简单标记
    if (row.finalResult === 0) {
      row._currentStep = '进行中'
    }

    return row
  })
}

const filteredList = computed(() => {
  let list = enrichThesisData(thesisList.value)

  // 逾期筛选
  if (queryParams.overdueFilter === 'overdue') {
    list = list.filter(item => item._overdue)
  } else if (queryParams.overdueFilter === 'warning') {
    list = list.filter(item => item._warning)
  }

  return list
})

// ==================== 行样式 ====================

function tableRowClassName({ row }) {
  if (row._overdue) return 'overdue-row'
  if (row._warning) return 'warning-row'
  return ''
}

// ==================== 文本映射 ====================

function getTimelineColor(processType) {
  const records = detailProcessMap.value[processType] || []
  return getTimelineType(records)
}

function toggleAdvancedSearch() {
  showAdvancedSearch.value = !showAdvancedSearch.value
}

// ==================== 数据加载 ====================

function loadStatistics() {
  getThesisStatistics().then(res => {
    stats.value = res.data || {}
    nextTick(() => {
      renderResultChart()
      renderProcessChart()
    })
  })
}

function getList() {
  loading.value = true
  listThesisMain(queryParams).then(res => {
    const data = res.data
    thesisList.value = data.records || data || []
    total.value = data.total || thesisList.value.length
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.studentNo = undefined
  queryParams.studentName = undefined
  queryParams.major = undefined
  queryParams.grade = undefined
  queryParams.supervisorName = undefined
  queryParams.finalResult = undefined
  queryParams.archiveStatus = undefined
  queryParams.overdueFilter = undefined
  handleQuery()
}

// ==================== 图表渲染 ====================

function renderResultChart() {
  if (!resultChartRef.value) return
  if (!resultChart) {
    resultChart = echarts.init(resultChartRef.value)
  }
  const s = stats.value
  resultChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{c}人' },
      data: [
        { value: s.ongoingCount || 0, name: '进行中', itemStyle: { color: '#409eff' } },
        { value: s.passedCount || 0, name: '已通过', itemStyle: { color: '#67c23a' } },
        { value: s.failedCount || 0, name: '未通过', itemStyle: { color: '#f56c6c' } }
      ]
    }]
  })
}

function renderProcessChart() {
  if (!processChartRef.value) return
  if (!processChart) {
    processChart = echarts.init(processChartRef.value)
  }
  const ps = stats.value.processStats || {}
  const categories = []
  const rates = []
  const totals = []
  for (const step of processSteps) {
    const item = ps[step.label] || {}
    categories.push(step.label)
    rates.push(item.rate || 0)
    totals.push(item.total || 0)
  }
  processChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['通过率%', '提交数'] },
    grid: { left: 60, right: 40, bottom: 40, top: 40 },
    xAxis: { type: 'category', data: categories, axisLabel: { rotate: 15 } },
    yAxis: [
      { type: 'value', name: '通过率%', max: 100 },
      { type: 'value', name: '提交数' }
    ],
    series: [
      {
        name: '通过率%',
        type: 'bar',
        data: rates,
        itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] },
        barWidth: '35%'
      },
      {
        name: '提交数',
        type: 'line',
        yAxisIndex: 1,
        data: totals,
        itemStyle: { color: '#e6a23c' },
        lineStyle: { width: 2 }
      }
    ]
  })
}

// ==================== 操作 ====================

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
}

function handleArchive(row) {
  ElMessageBox.confirm(`确认归档 ${row.studentName} 的论文？`, '提示', { type: 'warning' }).then(() => {
    archiveThesis(row.id).then(() => {
      ElMessage.success('归档成功')
      getList()
      loadStatistics()
    })
  }).catch(() => {})
}

function handleBatchArchive() {
  ElMessageBox.confirm(`确认批量归档选中的 ${selectedIds.value.length} 条记录？`, '提示', { type: 'warning' }).then(() => {
    batchArchiveThesis(selectedIds.value).then(() => {
      ElMessage.success('批量归档成功')
      selectedIds.value = []
      getList()
      loadStatistics()
    })
  }).catch(() => {})
}

function handleViewDetail(row) {
  currentThesis.value = row
  detailProcessMap.value = {}
  detailDrawerVisible.value = true

  // 并行加载各环节记录
  const promises = processSteps.map(step =>
    listProcess({ thesisId: row.id, processType: step.type, pageSize: 100 }).then(res => {
      const list = res.data.records || res.data || []
      detailProcessMap.value[step.type] = list.map(item => {
        if (item.contentExtend) {
          try {
            item.contentExtend = typeof item.contentExtend === 'string' ? JSON.parse(item.contentExtend) : item.contentExtend
          } catch (e) { /* ignore */ }
        }
        return item
      })
    })
  )
  Promise.all(promises)
}

// ==================== 生命周期 ====================

import { ElMessage, ElMessageBox } from 'element-plus'

function handleResize() {
  resultChart?.resize()
  processChart?.resize()
}

onMounted(() => {
  loadStatistics()
  getList()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  resultChart?.dispose()
  processChart?.dispose()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 0;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  padding: 20px;
  width: 100%;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

:deep(.overdue-row) {
  background-color: #fef0f0 !important;
}

:deep(.warning-row) {
  background-color: #fdf6ec !important;
}
</style>
