<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header><span>成绩评定</span></template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="指导教师评分" name="supervisor">
          <el-table :data="thesisList" v-loading="loading" stripe>
            <el-table-column prop="studentName" label="学生" width="100" />
            <el-table-column prop="thesisTitle" label="论文题目" min-width="160" />
            <el-table-column label="评分" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.supervisorScore" :min="0" :max="100" :precision="1" size="small" :disabled="row.scored" />
              </template>
            </el-table-column>
            <el-table-column label="评语" min-width="200">
              <template #default="{ row }">
                <el-input v-model="row.supervisorComment" type="textarea" :rows="1" size="small" :disabled="row.scored" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button v-if="!row.scored" type="primary" size="small" @click="submitSupervisorScore(row)">提交</el-button>
                <el-tag v-else type="success" size="small">已评</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="评阅教师评分" name="reviewer">
          <el-table :data="thesisList" v-loading="loading" stripe>
            <el-table-column prop="studentName" label="学生" width="100" />
            <el-table-column prop="thesisTitle" label="论文题目" min-width="160" />
            <el-table-column label="评分" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.reviewerScore" :min="0" :max="100" :precision="1" size="small" :disabled="row.reviewered" />
              </template>
            </el-table-column>
            <el-table-column label="评语" min-width="200">
              <template #default="{ row }">
                <el-input v-model="row.reviewerComment" type="textarea" :rows="1" size="small" :disabled="row.reviewered" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button v-if="!row.reviewered" type="primary" size="small" @click="submitReviewerScore(row)">提交</el-button>
                <el-tag v-else type="success" size="small">已评</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="答辩评分" name="defense">
          <el-table :data="thesisList" v-loading="loading" stripe>
            <el-table-column prop="studentName" label="学生" width="100" />
            <el-table-column prop="thesisTitle" label="论文题目" min-width="160" />
            <el-table-column label="评分" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.defenseScore" :min="0" :max="100" :precision="1" size="small" :disabled="row.defensed" />
              </template>
            </el-table-column>
            <el-table-column label="评语" min-width="200">
              <template #default="{ row }">
                <el-input v-model="row.defenseComment" type="textarea" :rows="1" size="small" :disabled="row.defensed" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button v-if="!row.defensed" type="primary" size="small" @click="submitDefenseScore(row)">提交</el-button>
                <el-tag v-else type="success" size="small">已评</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="总评汇总" name="total">
          <el-table :data="thesisList" v-loading="loading" stripe>
            <el-table-column prop="studentName" label="学生" width="100" />
            <el-table-column prop="thesisTitle" label="论文题目" min-width="140" />
            <el-table-column label="指导教师" width="80" align="center">
              <template #default="{ row }">{{ row.supervisorScore ?? '-' }}</template>
            </el-table-column>
            <el-table-column label="评阅" width="80" align="center">
              <template #default="{ row }">{{ row.reviewerScore ?? '-' }}</template>
            </el-table-column>
            <el-table-column label="答辩" width="80" align="center">
              <template #default="{ row }">{{ row.defenseScore ?? '-' }}</template>
            </el-table-column>
            <el-table-column label="总评" width="80" align="center">
              <template #default="{ row }">{{ row.totalScore ?? '-' }}</template>
            </el-table-column>
            <el-table-column label="等级" width="80" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.gradeLevel" :type="row.gradeLevel === '不及格' ? 'danger' : 'success'" size="small">{{ row.gradeLevel }}</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="calculateTotal(row)">计算总评</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCurrentInstance } from 'vue'
import { listThesisMain, getThesisGrade, supervisorGrade, reviewerGrade, defenseGrade, calculateTotalGrade } from '@/api/degree'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const activeTab = ref('supervisor')
const thesisList = ref([])

async function submitSupervisorScore(row) {
  await supervisorGrade({ thesisId: row.id, score: row.supervisorScore, comment: row.supervisorComment })
  proxy.$modal.msgSuccess('评分成功')
  row.scored = true
}

async function submitReviewerScore(row) {
  await reviewerGrade({ thesisId: row.id, score: row.reviewerScore, comment: row.reviewerComment })
  proxy.$modal.msgSuccess('评分成功')
  row.reviewered = true
}

async function submitDefenseScore(row) {
  await defenseGrade({ thesisId: row.id, score: row.defenseScore, comment: row.defenseComment })
  proxy.$modal.msgSuccess('评分成功')
  row.defensed = true
}

async function calculateTotal(row) {
  const res = await calculateTotalGrade(row.id)
  const grade = res.data || res
  row.totalScore = grade.totalScore
  row.gradeLevel = grade.gradeLevel
  proxy.$modal.msgSuccess('计算成功')
}

async function loadData() {
  loading.value = true
  try {
    const res = await listThesisMain({ pageSize: 100 })
    const list = res.data?.records || res.rows || []
    thesisList.value = list.map(t => ({
      ...t,
      supervisorScore: null, supervisorComment: '', scored: false,
      reviewerScore: null, reviewerComment: '', reviewered: false,
      defenseScore: null, defenseComment: '', defensed: false,
      totalScore: null, gradeLevel: null
    }))

    // 加载已有成绩
    for (const t of thesisList.value) {
      try {
        const gradeRes = await getThesisGrade(t.id)
        const grade = gradeRes.data || gradeRes
        if (grade && grade.id) {
          t.supervisorScore = grade.supervisorScore
          t.supervisorComment = grade.supervisorComment || ''
          t.scored = grade.supervisorScore != null
          t.reviewerScore = grade.reviewerScore
          t.reviewerComment = grade.reviewerComment || ''
          t.reviewered = grade.reviewerScore != null
          t.defenseScore = grade.defenseScore
          t.defenseComment = grade.defenseComment || ''
          t.defensed = grade.defenseScore != null
          t.totalScore = grade.totalScore
          t.gradeLevel = grade.gradeLevel
        }
      } catch { /* no grade yet */ }
    }
  } catch (e) { console.error(e) }
  loading.value = false
}

onMounted(() => loadData())
</script>
