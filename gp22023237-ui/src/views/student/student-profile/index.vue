<template>
  <div class="app-container">
    <!-- 主要信息卡片 -->
    <el-card shadow="hover" class="info-card">
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <el-icon><User /></el-icon>
            <span>基本信息</span>
          </div>
          <el-tag :type="getStatusTagType(studentInfo?.status)" size="large">
            {{ getStatusText(studentInfo?.status) }}
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="2" border v-if="studentInfo">
        <el-descriptions-item label="学号" :span="1">
          <span >{{ studentInfo.studentNo || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="姓名" :span="1">
          <span class="info-text">{{ studentInfo.studentName || '-' }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="学院" :span="1">
          <span class="info-text">{{ studentInfo.department || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="专业" :span="1">
          <span class="info-text">{{ studentInfo.major || '-' }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="入学年份" :span="1">
          <span class="info-text">{{ studentInfo.admissionYear || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="归属年级" :span="1">
          <span class="info-text">{{ studentInfo.cohortYear || '-' }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="预计毕业年份" :span="1">
          <span class="info-text">{{ studentInfo.graduationYear || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="双选状态" :span="1">
          <el-tag :type="getSelectionTagType(studentInfo.selectionStatus)" size="default">
            {{ getSelectionStatusText(studentInfo.selectionStatus) }}
          </el-tag>
          <span v-if="studentInfo.selectionStatus === 3 && studentInfo.mentorName" class="mentor-name">
            {{ studentInfo.mentorName }}
          </span>
        </el-descriptions-item>

        <el-descriptions-item label="研究方向" :span="2">
          <span class="info-text">{{ studentInfo.researchDirection || '暂无' }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="档案建立时间" :span="1">
          <span class="info-text">{{ formatDate(studentInfo.createTime) }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="最后更新时间" :span="2">
          <span class="info-text">{{ formatDate(studentInfo.updateTime) }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 空状态 -->
      <el-empty v-else description="暂未查询到学籍信息" />
    </el-card>
  </div>
</template>

<script setup name="StudentProfile">
import { getCurrentStudent } from "@/api/student/info";
import useUserStore from "@/store/modules/user";
import { getCurrentInstance, ref, onMounted } from "vue";
import { User } from "@element-plus/icons-vue";

const { proxy } = getCurrentInstance();

// 用户 store
const userStore = useUserStore();

// 学生信息
const studentInfo = ref(null);
const loading = ref(true);

// 获取当前学生信息
function getStudentInfo() {
  loading.value = true;
  getCurrentStudent()
    .then((res) => {
      studentInfo.value = res.data;
    })
    .catch((err) => {
      proxy.$modal.msgError(err.message || "获取学籍信息失败");
    })
    .finally(() => {
      loading.value = false;
    });
}

// 获取状态标签类型
function getStatusTagType(status) {
  const typeMap = { 1: "success", 2: "warning", 3: "primary", 4: "danger" };
  return typeMap[status] || "info";
}

// 获取状态文本
function getStatusText(status) {
  const textMap = { 1: "在读", 2: "休学", 3: "毕业", 4: "退学" };
  return textMap[status] || "未知";
}

// 获取双选状态标签类型
function getSelectionTagType(status) {
  const typeMap = { 0: "info", 1: "primary", 2: "warning", 3: "success" };
  return typeMap[status] || "info";
}

// 获取双选状态文本
function getSelectionStatusText(status) {
  const textMap = { 0: "未开始", 1: "双选中", 2: "补选中", 3: "已确定" };
  return textMap[status] || "未知";
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return "-";
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return "-";
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
}

// 页面挂载时获取数据
onMounted(() => {
  // 检查是否已登录
  if (!userStore.token) {
    proxy.$modal.msgError("请先登录后再访问");
    return;
  }

  // 检查是否有学生信息
  if (!userStore.roleInfo || !userStore.roleInfo[0]) {
    proxy.$modal.msgError("未查询到您的学生档案信息，请联系管理员");
    return;
  }

  // 获取当前学生信息
  getStudentInfo();
});
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.header-title .el-icon {
  font-size: 20px;
  color: #409eff;
}

.info-text {
  font-size: 14px;
  color: #303133;
}

.mentor-name {
  margin-left: 8px;
  font-size: 14px;
  color: #409eff;
  font-weight: 500;
}

:deep(.el-descriptions__label) {
  width: 120px;
  font-weight: 500;
  background-color: #fafafa;
}

:deep(.el-descriptions__body) {
  font-size: 14px;
}
</style>
