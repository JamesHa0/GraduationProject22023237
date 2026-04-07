<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>论文预答辩</span>
          <el-button type="primary" size="small" @click="handleAdd">新增预答辩</el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="queryParams.studentNo" placeholder="请输入学号" clearable />
        </el-form-item>
        <el-form-item label="答辩状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="待答辩" :value="1" />
            <el-option label="已通过" :value="2" />
            <el-option label="未通过" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="dataList" border>
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="学号" prop="studentNo" align="center" />
        <el-table-column label="姓名" prop="studentName" align="center" />
        <el-table-column label="答辩时间" prop="defenseTime" align="center" />
        <el-table-column label="答辩地点" prop="defenseLocation" align="center" />
        <el-table-column label="答辩结果" prop="defenseResult" align="center">
          <template #default="{ row }">
            <el-tag :type="getResultType(row.defenseResult)">
              {{ getResultText(row.defenseResult) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="答辩评分" prop="defenseScore" align="center" />
        <el-table-column label="导师审批" prop="mentorStatus" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.mentorStatus)">
              {{ getStatusText() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="handleView(row)">详情</el-button>
            <el-button link size="small" type="primary" @click="handleRecord(row)">记录答辩</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 答辩记录对话框 -->
    <el-dialog v-model="dialogVisible" title="预答辩记录" width="700px">
      <el-form :model="form" ref="formRef" label-width="120px">
        <el-form-item label="答辩时间" prop="defenseTime">
          <el-date-picker v-model="form.defenseTime" type="datetime" placeholder="选择时间" />
        </el-form-item>
        <el-form-item label="答辩地点" prop="defenseLocation">
          <el-input v-model="form.defenseLocation" placeholder="请输入地点" />
        </el-form-item>
        <el-form-item label="答辩结果" prop="defenseResult">
          <el-radio-group v-model="form.defenseResult">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="答辩评分" prop="defenseScore">
          <el-input-number v-model="form.defenseScore" :min="0" :max="100" placeholder="0-100" />
        </el-form-item>
        <el-form-item label="答委评语" prop="committeeComment">
          <el-input v-model="form.committeeComment" type="textarea" :rows="4" placeholder="请输入评语" />
        </el-form-item>
        <el-form-item label="问答记录" prop="qaRecord">
          <el-input v-model="form.qaRecord" type="textarea" :rows="4" placeholder="请输入问答记录" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { listPreDefense, getPreDefenseDetail, submitPreDefense, recordPreDefenseResult, approvePreDefenseMentor, approvePreDefenseSecretary, approvePreDefenseDean } from '@/api/degree'

const loading = ref(false)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isView = ref(false)
const isRecord = ref(false)

const queryParams = reactive({
  pageNum:1,
  pageSize: 10,
  studentId: undefined,
  overallStatus: undefined
})

const form = reactive({
  id: '',
  studentId: undefined,
  thesisTitle: '',
  thesisAbstract: '',
  defenseTime: '',
  defenseLocation: '',
  defenseResult: 1,
  defenseScore: 0,
  committeeChair: '',
  committeeMembers: '',
  committeeComment: '',
  qaRecord: '',
  mentorStatus: 0,
  secretaryStatus: 0,
  deanStatus: 0,
  status: 1,
  comment: ''
})

function getResultText(result) {
  const map = { 0: '未答辩', 1: '通过', 2: '不通过' }
  return map[result] || '-'
}

function getResultType(result) {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[result] || ''
}

function getStatusText(status) {
  const map = { 0: '待审批', 1: '已通过', 2: '已拒绝' }
  return map[status] || '-'
}

function getStatusType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || ''
}

function handleQuery() {
  loading.value = true
  getList()
}

function resetQuery() {
  queryParams.studentNo = ''
  queryParams.status = ''
  handleQuery()
}

function handleView(row) {
  isView.value = true
  isRecord.value = false
  getPreDefenseDetail(row.id).then(res => {
    if (res.code === 200) {
      Object.assign(form, res.data)
      dialogVisible.value = true
    } else {
      ElMessage.error(res.msg || '获取详情失败')
    }
  })
}

function handleRecord(row) {
  isView.value = true
  isRecord.value = true
  getPreDefenseDetail(row.id).then(res => {
    if (res.code === 200) {
      Object.assign(form, res.data)
      dialogVisible.value = true
    } else {
      ElMessage.error(res.msg || '获取详情失败')
    }
  })
}

function handleSubmit() {
  if (isView.value && !isRecord.value) {
    dialogVisible.value = false
    return
  }

  if (isRecord.value) {
    // 记录答辩结果
    const { id, defenseResult, defenseScore, committeeComment, qaRecord } = form
    recordPreDefenseResult(id, defenseResult, committeeComment, qaRecord).then(res => {
      if (res.code === 200) {
        ElMessage.success('记录成功')
        dialogVisible.value = false
        getList()
      } else {
        ElMessage.error(res.msg || '记录失败')
      }
    }).catch(err => {
      ElMessage.error('记录失败')
      console.error(err)
    })
  } else {
    // 提交审批
    const { id, status, comment } = form
    // TODO: 根据当前用户角色选择不同的审批方法
    approvePreDefenseMentor(id, status, comment).then(res => {
      if (res.code === 200) {
        ElMessage.success('审批成功')
        dialogVisible.value = false
        getList()
      } else {
        ElMessage.error(res.msg || '审批失败')
      }
    }).catch(err => {
      ElMessage.error('审批失败')
      console.error(err)
    })
  }
}

function getList() {
  loading.value = true
  listPreDefense(queryParams).then(res => {
    loading.value = false
    if (res.code === 200) {
      const result = res.data
      dataList.value = result.records || []
      total.value = result.total || 0
    } else {
      ElMessage.error(res.msg || '获取数据失败')
    }
  }).catch(err => {
    loading.value = false
    ElMessage.error('获取数据失败')
    console.error(err)
  })
}

function handleSizeChange(val) {
  queryParams.pageSize = val
  handleQuery()
}

function handleCurrentChange(val) {
  queryParams.pageNum = val
  handleQuery()
}

// 初始化加载数据
getList()
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
