<template>
    <div class="course-query-container">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">课程查询</h3>
                <div class="search-bar">
                    <input type="text" v-model="searchKeyword" placeholder="搜索课程名称或编号..."
                        class="form-control search-input" @keyup.enter="fetchCourses" />
                    <button class="btn search-btn" @click="fetchCourses">
                        <font-awesome-icon icon="search" /> 搜索
                    </button>
                </div>
            </div>

            <div class="filter-section">
                <div class="filter-group">
                    <label>学期：</label>
                    <select v-model="selectedSemester" class="form-control filter-select" @change="fetchCourses">
                        <option value="">全部学期</option>
                        <option value="2023-2024-1">2023-2024学年 第一学期</option>
                        <option value="2023-2024-2">2023-2024学年 第二学期</option>
                        <option value="2024-2025-1">2024-2025学年 第一学期</option>
                    </select>
                </div>

                <div class="filter-group">
                    <label>课程类型：</label>
                    <select v-model="selectedType" class="form-control filter-select" @change="fetchCourses">
                        <option value="">全部类型</option>
                        <option value="必修课">必修课</option>
                        <option value="选修课">选修课</option>
                        <option value="专业选修课">专业选修课</option>
                        <option value="公共选修课">公共选修课</option>
                    </select>
                </div>
            </div>

            <div class="table-container">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>课程编号</th>
                            <th>课程名称</th>
                            <th>学分</th>
                            <th>学期</th>
                            <th>课程类型</th>
                            <th>授课教师</th>
                            <th>成绩</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="course in courses" :key="course.id">
                            <td>{{ course.code }}</td>
                            <td>{{ course.name }}</td>
                            <td>{{ course.credit }}</td>
                            <td>{{ course.semester }}</td>
                            <td><span class="status-tag" :class="getCourseTypeClass(course.type)">{{ course.type
                                    }}</span></td>
                            <td>{{ course.teacher }}</td>
                            <td>{{ course.score || '-' }}</td>
                            <td>
                                <button class="btn detail-btn" @click="viewCourseDetail(course)">
                                    <font-awesome-icon icon="eye" /> 详情
                                </button>
                            </td>
                        </tr>
                        <tr v-if="courses.length === 0 && !loading">
                            <td colspan="8" class="no-data">暂无课程数据</td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div class="pagination" v-if="totalPages > 1">
                <button class="btn page-btn" @click="changePage(currentPage - 1)" :disabled="currentPage === 1">
                    上一页
                </button>
                <span class="page-info">
                    第 {{ currentPage }} / {{ totalPages }} 页
                </span>
                <button class="btn page-btn" @click="changePage(currentPage + 1)"
                    :disabled="currentPage === totalPages">
                    下一页
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useAppStore } from '../../stores/appStore';

// 状态管理
const appStore = useAppStore();

// 搜索和筛选条件
const searchKeyword = ref('');
const selectedSemester = ref('');
const selectedType = ref('');

// 分页相关
const currentPage = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);

// 课程数据
const courses = ref([]);
const loading = ref(false);

// 页面加载时获取课程数据
onMounted(() => {
    fetchCourses();
});

// 获取课程数据
const fetchCourses = () => {
    loading.value = true;

    // 模拟API请求
    setTimeout(() => {
        // 模拟课程数据
        const mockCourses = [
            {
                id: 1,
                code: 'CS501',
                name: '高级数据结构',
                credit: 3,
                semester: '2023-2024-1',
                type: '必修课',
                teacher: '张教授',
                score: 85
            },
            {
                id: 2,
                code: 'CS502',
                name: '机器学习基础',
                credit: 4,
                semester: '2023-2024-1',
                type: '专业选修课',
                teacher: '李教授',
                score: 90
            },
            {
                id: 3,
                code: 'MA501',
                name: '数值分析',
                credit: 3,
                semester: '2023-2024-2',
                type: '必修课',
                teacher: '王教授',
                score: 88
            },
            {
                id: 4,
                code: 'CS503',
                name: '深度学习',
                credit: 4,
                semester: '2023-2024-2',
                type: '专业选修课',
                teacher: '赵教授',
                score: '-'
            },
            {
                id: 5,
                code: 'HU501',
                name: '科技伦理',
                credit: 2,
                semester: '2024-2025-1',
                type: '公共选修课',
                teacher: '陈教授',
                score: '-'
            }
        ];

        // 模拟筛选逻辑
        let filtered = mockCourses;

        if (searchKeyword.value) {
            const keyword = searchKeyword.value.toLowerCase();
            filtered = filtered.filter(course =>
                course.name.toLowerCase().includes(keyword) ||
                course.code.toLowerCase().includes(keyword)
            );
        }

        if (selectedSemester.value) {
            filtered = filtered.filter(course => course.semester === selectedSemester.value);
        }

        if (selectedType.value) {
            filtered = filtered.filter(course => course.type === selectedType.value);
        }

        // 模拟分页
        totalPages.value = Math.ceil(filtered.length / pageSize.value);
        const startIndex = (currentPage.value - 1) * pageSize.value;
        courses.value = filtered.slice(startIndex, startIndex + pageSize.value);

        loading.value = false;
    }, 500);
};

// 切换页码
const changePage = (page) => {
    if (page < 1 || page > totalPages.value) return;
    currentPage.value = page;
    fetchCourses();
};

// 根据课程类型获取样式类
const getCourseTypeClass = (type) => {
    switch (type) {
        case '必修课':
            return 'status-active';
        case '选修课':
            return 'status-pending';
        case '专业选修课':
            return 'status-info';
        case '公共选修课':
            return 'status-secondary';
        default:
            return '';
    }
};

// 查看课程详情
const viewCourseDetail = (course) => {
    appStore.addNotification(`查看《${course.name}》课程详情`, 'info');
    // 实际应用中可以打开课程详情弹窗或跳转到详情页
};
</script>

<style scoped>
.course-query-container {
    animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(10px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    flex-wrap: wrap;
    gap: 16px;
}

.search-bar {
    display: flex;
    gap: 10px;
    width: 400px;
    min-width: 300px;
}

.search-input {
    flex: 1;
}

.search-btn {
    background-color: #409eff;
    color: white;
}

.filter-section {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;
    flex-wrap: wrap;
}

.filter-group {
    display: flex;
    align-items: center;
    gap: 8px;
}

.filter-select {
    width: 200px;
}

.table-container {
    overflow-x: auto;
}

.no-data {
    text-align: center;
    color: #86909c;
    padding: 40px 0;
}

.detail-btn {
    background-color: #409eff;
    color: white;
    padding: 4px 8px;
    font-size: 12px;
}

.pagination {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 10px;
    margin-top: 20px;
}

.page-btn {
    background-color: #f5f7fa;
    color: #4e5969;
}

.dark-mode .page-btn {
    background-color: #333355;
    color: #bbb;
}

.page-info {
    color: #666;
}

.dark-mode .page-info {
    color: #bbb;
}

.status-info {
    background-color: #e6f7ff;
    color: #1890ff;
}

.dark-mode .status-info {
    background-color: #1a365d;
    color: #66b1ff;
}

.status-secondary {
    background-color: #f0f2f5;
    color: #86909c;
}

.dark-mode .status-secondary {
    background-color: #383850;
    color: #aaa;
}

@media (max-width: 768px) {
    .card-header {
        flex-direction: column;
        align-items: stretch;
    }

    .search-bar {
        width: 100%;
    }

    .filter-group {
        width: 100%;
    }

    .filter-select {
        width: 100%;
        flex: 1;
    }
}
</style>