<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">学位授予管理</span>
          <el-button type="primary" size="small" icon="Refresh" @click="getList">刷新</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="dataList" border>
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="学号" prop="studentNo" align="center" width="120" />
        <el-table-column label="姓名" prop="studentName" align="center" width="100" />
        <el-table-column label="导师" prop="mentorName" align="center" width="100" />
        <el-table-column label="学位类型" prop="degreeType" align="center" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.degreeType === 1 ? '硕士学位' : '博士学位' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="论文题目" prop="thesisTitle" align="center" show-overflow-tooltip min-width="200" />
        <el-table-column label="答辩评分" prop="defenseScore" align="center" width="100" />
        <el-table-column label="分委会审批时间" prop="committeeApproveTime" align="center" width="160" />
        <el-table-column label="授予状态" prop="degreeGranted" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="row.degreeGranted === 1 ? 'success' : 'warning'" size="small">
              {{ row.degreeGranted === 1 ? '已授予' : '待授予' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="证书编号" prop="certificateNo" align="center" width="160">
          <template #default="{ row }">
            {{ row.certificateNo || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="120" class-name="small-padding fixed-width">
          <template #default="{ row }">
            <el-button
              link
              size="small"
              type="success"
              @click="handleGrant(row)"
              :disabled="row.degreeGranted === 1"
            >授予学位</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 授予学位对话框 -->
    <el-dialog :title="'授予学位 - ' + currentStudent" v-model="dialogVisible" width="500px" append-to-body>
      <el-form ref="grantFormRef" :model="grantForm" :rules="grantRules" label-width="100px">
        <el-form-item label="学生姓名" prop="studentName">
          <el-input v-model="grantForm.studentName" disabled />
        </el-form-item>
        <el-form-item label="论文题目" prop="thesisTitle">
          <el-input v-model="grantForm.thesisTitle" disabled />
        </el-form-item>
        <el-form-item label="证书编号" prop="certificateNo">
          <el-input v-model="grantForm.certificateNo" placeholder="请输入学位证书编号" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitGrant" :loading="submitting">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listDegreeApplication, grantDegree } from '@/api/degree'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const currentStudent = ref('')
const grantFormRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  committeeStatus: 1,
  degreeGranted: 0
})

const grantForm = reactive({
  id: null,
  studentName: '',
  thesisTitle: '',
  certificateNo: ''
})

const grantRules = {
  certificateNo: [
    { required: true, message: '请输入学位证书编号', trigger: 'blur' }
  ]
}

function getList() {
  loading.value = true
  listDegreeApplication(queryParams).then(res => {
    if (res.code === 200) {
      dataList.value = res.rows || res.data?.records || res.data || []
      total.value = res.total || res.data?.total || 0
    } else {
      dataList.value = []
      total.value = 0
    }
  }).catch(() => {
    dataList.value = []
    total.value = 0
  }).finally(() => {
    loading.value = false
  })
}

function handleGrant(row) {
  grantForm.id = row.id
  grantForm.studentName = row.studentName
  grantForm.thesisTitle = row.thesisTitle
  grantForm.certificateNo = ''
  currentStudent.value = row.studentName
  dialogVisible.value = true
}

function submitGrant() {
  grantFormRef.value?.validate(valid => {
    if (!valid) return
    submitting.value = true
    grantDegree(grantForm.id, grantForm.certificateNo).then(res => {
      if (res.code === 200) {
        ElMessage.success('学位授予成功')
        dialogVisible.value = false
        getList()
      } else {
        ElMessage.error(res.msg || '授予失败')
      }
    }).catch(() => {
      ElMessage.error('授予请求失败')
    }).finally(() => {
      submitting.value = false
    })
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-weight: 600;
  font-size: 16px;
}
</style>
