<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">选课结果</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="selectedCourses" border>
        <el-table-column label="序号" width="80" type="index" align="center" />
        <el-table-column label="课程编号" prop="courseNo" width="150" />
        <el-table-column label="课程名称" prop="name" />
        <el-table-column label="学分" prop="credit" width="80" align="center" />
        <el-table-column label="学时" prop="hours" width="80" align="center" />
        <el-table-column label="选课状态" width="100" align="center">
          <template #default="scope">
            <el-tag type="success">已确认</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getSelectedCourses" />

      <el-empty v-if="!loading && selectedCourses.length === 0" description="暂无选课记录" />
    </el-card>

    <!-- 最终结果展示 -->
    <el-card class="mt20" shadow="never" v-if="totalCourseCount > 0">
      <template #header>
        <div class="card-header">
          <span class="card-title">选课统计</span>
        </div>
      </template>
      <el-result icon="success" title="选课成功">
        <template #sub-title>
          您已选择 {{ totalCourseCount }} 门课程，共 {{ totalCredits }} 学分
        </template>
        <template #extra>
          <el-descriptions :column="2" border style="width: 600px; margin: 0 auto;">
            <el-descriptions-item label="已选课程">{{ totalCourseCount }} 门</el-descriptions-item>
            <el-descriptions-item label="总学分">{{ totalCredits }} 学分</el-descriptions-item>
          </el-descriptions>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<script setup name="SelectResult">
import { listCourseSelection } from "@/api/course/selection";
import useUserStore from '@/store/modules/user';

const { proxy } = getCurrentInstance();

// 学生ID
const studentId = ref(null);
const getStudentId = () => {
  const userStore = useUserStore();
  if (userStore.roleInfo && userStore.roleInfo[0]) {
    return userStore.roleInfo[0].id;
  } else {
    proxy.$modal.msgError('学生信息尚未加载');
    return null;
  }
};

// 分页参数
const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10
  }
});
const { queryParams } = toRefs(data);

const total = ref(0);

// 加载状态
const loading = ref(true);

// 已选课程
const selectedCourses = ref([]);

// 统计信息（分页后 totalCredits 通过单独查询全量数据计算）
const totalCredits = ref(0);
const totalCourseCount = ref(0);

// 获取已选课程（分页）
function getSelectedCourses() {
  const sid = getStudentId();
  if (!sid) {
    loading.value = false;
    return;
  }
  studentId.value = sid;

  loading.value = true;
  listCourseSelection({
    studentId: sid,
    status: 1,
    pageNum: queryParams.value.pageNum,
    pageSize: queryParams.value.pageSize
  }).then(res => {
    selectedCourses.value = res.data.rows || res.data || [];
    total.value = res.data.total || 0;
    loading.value = false;
  }).catch((err) => {
    console.error('选课结果查询失败:', err);
    loading.value = false;
  });
}

// 获取全量统计数据（不分页，仅用于统计卡片）
function getStats() {
  const sid = getStudentId();
  if (!sid) return;
  listCourseSelection({ studentId: sid, status: 1 }).then(res => {
    const rows = res.data && res.data.rows ? res.data.rows : (res.data || []);
    totalCourseCount.value = rows.length;
    totalCredits.value = rows.reduce((sum, c) => sum + (c.credit || 0), 0);
  }).catch(() => {});
}

// 初始化
function init() {
  const sid = getStudentId();
  if (!sid) {
    loading.value = false;
    return;
  }
  studentId.value = sid;
  getSelectedCourses();
  getStats();
}

init();
</script>

<style scoped>
.mt20 {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
}
</style>
