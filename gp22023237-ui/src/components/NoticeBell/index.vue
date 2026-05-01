<template>
  <div ref="bellRef" class="notice-bell" @click.stop="togglePanel">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
      <svg-icon icon-class="bell" />
    </el-badge>
  </div>

  <!-- 通知面板 - Teleport到body避免被navbar的overflow:hidden裁剪 -->
  <Teleport to="body">
    <div v-show="panelVisible" ref="panelRef" class="notice-panel" :style="panelStyle" @click.stop>
      <div class="panel-header">
        <span class="panel-title">通知</span>
        <el-button v-if="unreadCount > 0" type="primary" link size="small" @click="handleMarkAllRead">
          全部已读
        </el-button>
      </div>

      <div class="panel-body" v-loading="loading">
        <template v-if="noticeList.length > 0">
          <div
            v-for="item in noticeList"
            :key="item.noticeId"
            class="notice-item"
            @click="handleNoticeClick(item)"
          >
            <div class="notice-item-left">
              <span v-if="!item.isRead" class="unread-dot"></span>
            </div>
            <div class="notice-item-content">
              <div class="notice-item-title">
                <el-tag v-if="item.noticeType === '1'" type="primary" size="small" class="notice-tag">通知</el-tag>
                <el-tag v-else type="warning" size="small" class="notice-tag">公告</el-tag>
                <span class="title-text">{{ item.noticeTitle }}</span>
              </div>
              <div class="notice-item-time">{{ formatTime(item.createTime) }}</div>
            </div>
          </div>
        </template>
        <div v-else class="notice-empty">
          <el-empty description="暂无未读通知" :image-size="60" />
        </div>
      </div>

      <div class="panel-footer" @click="goToListDrawer">
        查看全部通知
      </div>
    </div>
  </Teleport>

  <!-- 全部通知列表抽屉 -->
  <el-drawer
    v-model="listDrawerVisible"
    title="全部通知"
    size="560px"
    direction="rtl"
    class="notice-list-drawer"
  >
    <div class="list-drawer-toolbar">
      <el-button v-if="unreadCount > 0" type="primary" link size="small" @click="handleMarkAllRead">
        全部已读
      </el-button>
    </div>
    <div class="list-drawer-body" v-loading="listLoading">
      <template v-if="allNoticeList.length > 0">
        <div
          v-for="item in allNoticeList"
          :key="item.noticeId"
          class="list-notice-item"
          :class="{ 'is-read': item.isRead }"
          @click="handleListNoticeClick(item)"
        >
          <div class="list-notice-left">
            <span v-if="!item.isRead" class="unread-dot"></span>
          </div>
          <div class="list-notice-content">
            <div class="list-notice-title">
              <el-tag v-if="item.noticeType === '1'" type="primary" size="small">通知</el-tag>
              <el-tag v-else type="warning" size="small">公告</el-tag>
              <span class="title-text">{{ item.noticeTitle }}</span>
            </div>
            <div class="list-notice-time">{{ formatDate(item.createTime) }}</div>
          </div>
        </div>
      </template>
      <el-empty v-else description="暂无通知" :image-size="80" />
    </div>
    <div class="list-drawer-footer" v-if="listTotal > listQuery.pageSize">
      <el-pagination
        v-model:current-page="listQuery.pageNum"
        :page-size="listQuery.pageSize"
        :total="listTotal"
        layout="prev, pager, next"
        small
        @current-change="fetchAllNoticeList"
      />
    </div>
  </el-drawer>

  <!-- 通知详情抽屉 -->
  <el-drawer
    v-model="drawerVisible"
    title="通知详情"
    size="560px"
    direction="rtl"
    :before-close="handleDrawerClose"
    class="notice-detail-drawer"
  >
    <template v-if="currentNotice">
      <div class="drawer-body">
        <!-- 标题区域 -->
        <div class="notice-header">
          <div class="notice-title-row">
            <el-tag v-if="currentNotice.noticeType === '1'" type="primary" size="default">通知</el-tag>
            <el-tag v-else type="warning" size="default">公告</el-tag>
            <h2 class="notice-title">{{ currentNotice.noticeTitle }}</h2>
          </div>
          <div class="notice-meta">
            <span class="meta-item">
              <el-icon><User /></el-icon>
              {{ currentNotice.createBy || '系统' }}
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ formatDate(currentNotice.createTime) }}
            </span>
            <span v-if="currentNotice.targetRoles" class="meta-item">
              <el-icon><UserFilled /></el-icon>
              {{ formatTargetRoles(currentNotice.targetRoles) }}
            </span>
          </div>
        </div>

        <!-- 内容区域 -->
        <div class="notice-content">
          <div v-if="currentNotice.noticeContent" class="content-html" v-html="currentNotice.noticeContent"></div>
          <el-empty v-else description="暂无内容" :image-size="80" />
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="drawer-footer">
        <el-button @click="drawerVisible = false">关闭</el-button>
        <el-button
          v-if="!currentNotice.isRead"
          type="primary"
          @click="handleMarkCurrentRead"
          :loading="markingRead"
        >
          标记已读
        </el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { User, Clock, UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getUnreadCount, getUnreadList, getNotice, markAsRead, markAllAsRead, getUserNoticeList } from '@/api/system/notice'
import { useSse } from '@/utils/useSse'

const { connect, disconnect, onNotice } = useSse()

const bellRef = ref(null)
const panelRef = ref(null)
const unreadCount = ref(0)
const noticeList = ref([])
const panelVisible = ref(false)
const loading = ref(false)
const panelStyle = ref({})

// 抽屉相关
const drawerVisible = ref(false)
const currentNotice = ref(null)
const markingRead = ref(false)

// 全部通知列表抽屉
const listDrawerVisible = ref(false)
const allNoticeList = ref([])
const listLoading = ref(false)
const listTotal = ref(0)
const listQuery = ref({ pageNum: 1, pageSize: 15 })

/** 角色ID映射 */
const roleMap = {
  1: '超管', 2: '院长', 3: '主席', 4: '综合管理',
  5: '教学秘书', 6: '学生', 7: '导师', 8: '授课教师'
}

/** 格式化目标角色 */
function formatTargetRoles(targetRoles) {
  if (!targetRoles) return ''
  return targetRoles.split(',').map(id => roleMap[Number(id)] || id).join('、')
}

// 加载未读数量
async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data?.unreadCount || 0
  } catch (e) {
    console.warn('获取未读数量失败', e)
  }
}

// 加载未读列表
async function fetchUnreadList() {
  loading.value = true
  try {
    const res = await getUnreadList()
    noticeList.value = (res.data || []).map(item => ({ ...item, isRead: false }))
  } catch (e) {
    console.warn('获取未读列表失败', e)
  } finally {
    loading.value = false
  }
}

// 切换面板
function togglePanel() {
  panelVisible.value = !panelVisible.value
  if (panelVisible.value) {
    nextTick(() => {
      updatePanelPosition()
    })
    fetchUnreadList()
  }
}

// 计算面板定位（相对于视口）
function updatePanelPosition() {
  if (!bellRef.value) return
  const rect = bellRef.value.getBoundingClientRect()
  panelStyle.value = {
    position: 'fixed',
    top: `${rect.bottom + 8}px`,
    right: `${window.innerWidth - rect.right}px`,
  }
}

// 点击通知项 -> 打开抽屉
async function handleNoticeClick(item) {
  panelVisible.value = false
  try {
    const res = await getNotice(item.noticeId)
    currentNotice.value = {
      ...res.data,
      isRead: item.isRead || false
    }
    drawerVisible.value = true
  } catch (e) {
    console.warn('获取通知详情失败', e)
    // 降级：用列表中的基本数据展示
    currentNotice.value = { ...item }
    drawerVisible.value = true
  }
}

// 标记当前通知已读
async function handleMarkCurrentRead() {
  if (!currentNotice.value) return
  markingRead.value = true
  try {
    await markAsRead(currentNotice.value.noticeId)
    currentNotice.value.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    // 从下拉列表中移除
    noticeList.value = noticeList.value.filter(n => n.noticeId !== currentNotice.value.noticeId)
    // 同步更新全部列表抽屉
    const found = allNoticeList.value.find(n => n.noticeId === currentNotice.value.noticeId)
    if (found) found.isRead = true
    ElMessage.success('已标记为已读')
  } catch (e) {
    console.warn('标记已读失败', e)
  } finally {
    markingRead.value = false
  }
}

// 标记全部已读
async function handleMarkAllRead() {
  try {
    await markAllAsRead()
    unreadCount.value = 0
    noticeList.value = []
    // 同步更新全部列表抽屉
    allNoticeList.value.forEach(n => { n.isRead = true })
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    console.warn('标记全部已读失败', e)
  }
}

// 抽屉关闭前
function handleDrawerClose(done) {
  if (markingRead.value) return
  done()
}

// 打开全部通知抽屉
function goToListDrawer() {
  panelVisible.value = false
  listDrawerVisible.value = true
  fetchAllNoticeList()
}

// 获取全部通知列表
async function fetchAllNoticeList() {
  listLoading.value = true
  try {
    const res = await getUserNoticeList(listQuery.value)
    // 响应拦截器将 JSONReturn.data 解包为 res.data
    // 后端返回 { rows: [...], total: N }，所以取 res.data.rows
    const listData = res.data || {}
    allNoticeList.value = (listData.rows || []).map(item => ({
      ...item,
      isRead: !!item.isRead
    }))
    listTotal.value = listData.total || 0
  } catch (e) {
    console.warn('获取通知列表失败', e)
  } finally {
    listLoading.value = false
  }
}

// 列表中点击通知 -> 打开详情抽屉
async function handleListNoticeClick(item) {
  try {
    const res = await getNotice(item.noticeId)
    currentNotice.value = {
      ...res.data,
      isRead: item.isRead || false
    }
    drawerVisible.value = true
  } catch (e) {
    console.warn('获取通知详情失败', e)
    currentNotice.value = { ...item }
    drawerVisible.value = true
  }
}

// 格式化时间（相对时间）
function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return date.toLocaleDateString()
}

// 格式化日期（完整日期时间）
function formatDate(time) {
  if (!time) return ''
  const date = new Date(time)
  if (isNaN(date.getTime())) return ''
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 点击外部关闭面板
function handleClickOutside(e) {
  if (!panelVisible.value) return
  const clickedBell = bellRef.value && bellRef.value.contains(e.target)
  const clickedPanel = panelRef.value && panelRef.value.contains(e.target)
  if (!clickedBell && !clickedPanel) {
    panelVisible.value = false
  }
}

onMounted(() => {
  fetchUnreadCount()
  connect()
  // 监听SSE实时通知
  onNotice(() => {
    unreadCount.value++
    if (panelVisible.value) {
      fetchUnreadList()
    }
  })
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  disconnect()
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style lang="scss" scoped>
.notice-bell {
  cursor: pointer;
  font-size: 22px;
  padding: 0 5px;

  :deep(.el-badge__content) {
    top: 10px;        // 默认约 0，增大这个值让角标往下移
    right: calc(-1px + var(--el-badge-size) / 2);  // 水平微调（可选）
  }
}

.notice-panel {
  width: 340px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
  z-index: 2000;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border-bottom: 1px solid var(--el-border-color-lighter);

    .panel-title {
      font-weight: 600;
      font-size: 15px;
    }
  }

  .panel-body {
    max-height: 360px;
    overflow-y: auto;

    .notice-item {
      display: flex;
      align-items: flex-start;
      padding: 10px 16px;
      cursor: pointer;
      border-bottom: 1px solid var(--el-border-color-extra-light);

      &:hover {
        background: var(--el-fill-color-light);
      }

      .notice-item-left {
        flex-shrink: 0;
        width: 8px;
        display: flex;
        align-items: center;
        padding-top: 6px;
        margin-right: 6px;

        .unread-dot {
          width: 6px;
          height: 6px;
          border-radius: 50%;
          background: var(--el-color-primary);
        }
      }

      .notice-item-content {
        flex: 1;
        min-width: 0;
      }

      .notice-item-title {
        display: flex;
        align-items: center;
        gap: 6px;

        .notice-tag {
          flex-shrink: 0;
        }

        .title-text {
          font-size: 13px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .notice-item-time {
        font-size: 12px;
        color: var(--el-text-color-secondary);
        margin-top: 4px;
      }
    }

    .notice-empty {
      padding: 20px 0;
    }
  }

  .panel-footer {
    text-align: center;
    padding: 10px;
    border-top: 1px solid var(--el-border-color-lighter);
    color: var(--el-color-primary);
    cursor: pointer;
    font-size: 13px;

    &:hover {
      background: var(--el-fill-color-light);
    }
  }
}

/* 抽屉详情样式 */
.notice-detail-drawer {
  :deep(.el-drawer__header) {
    margin-bottom: 0;
    padding: 16px 20px;
    border-bottom: 1px solid #e5e6eb;
    background: #f7f8fa;
  }

  :deep(.el-drawer__body) {
    padding: 0;
    display: flex;
    flex-direction: column;
  }
}

.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.notice-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e6eb;

  .notice-title-row {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    margin-bottom: 12px;
  }

  .notice-title {
    margin: 0;
    font-size: 18px;
    font-weight: 700;
    color: #1d2129;
    line-height: 1.4;
  }

  .notice-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;

    .meta-item {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: #86909c;

      .el-icon {
        font-size: 14px;
      }
    }
  }
}

.notice-content {
  .content-html {
    font-size: 14px;
    line-height: 1.8;
    color: #303133;
    word-break: break-word;

    :deep(img) {
      max-width: 100%;
      height: auto;
      border-radius: 4px;
      margin: 8px 0;
    }

    :deep(p) {
      margin: 0 0 12px;
    }

    :deep(h1), :deep(h2), :deep(h3), :deep(h4) {
      margin: 16px 0 8px;
      color: #1d2129;
    }

    :deep(ul), :deep(ol) {
      padding-left: 20px;
      margin: 8px 0;
    }

    :deep(blockquote) {
      margin: 8px 0;
      padding: 8px 16px;
      border-left: 4px solid var(--el-color-primary);
      background: #f7f8fa;
      color: #606266;
    }

    :deep(table) {
      border-collapse: collapse;
      width: 100%;
      margin: 8px 0;

      th, td {
        border: 1px solid #dcdfe6;
        padding: 8px 12px;
        text-align: left;
      }

      th {
        background: #f5f7fa;
        font-weight: 600;
      }
    }

    :deep(a) {
      color: var(--el-color-primary);
      text-decoration: none;

      &:hover {
        text-decoration: underline;
      }
    }

    :deep(pre) {
      background: #f5f7fa;
      border-radius: 4px;
      padding: 12px;
      overflow-x: auto;
    }

    :deep(code) {
      background: #f5f7fa;
      padding: 2px 6px;
      border-radius: 3px;
      font-size: 13px;
    }
  }
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #e5e6eb;
  background: #fff;
}

/* 全部通知列表抽屉样式 */
.notice-list-drawer {
  :deep(.el-drawer__header) {
    margin-bottom: 0;
    padding: 16px 20px;
    border-bottom: 1px solid #e5e6eb;
    background: #f7f8fa;
  }

  :deep(.el-drawer__body) {
    padding: 0;
    display: flex;
    flex-direction: column;
  }
}

.list-drawer-toolbar {
  display: flex;
  justify-content: flex-end;
  padding: 0 16px;
  border-bottom: 1px solid var(--el-border-color-extra-light);
}

.list-drawer-body {
  flex: 1;
  overflow-y: auto;

  .list-notice-item {
    display: flex;
    align-items: flex-start;
    padding: 6px 20px;
    cursor: pointer;
    border-bottom: 1px solid var(--el-border-color-extra-light);
    transition: background 0.2s;

    &:hover {
      background: var(--el-fill-color-light);
    }

    &.is-read {
      .title-text {
        color: var(--el-text-color-secondary);
      }
      .list-notice-time {
        color: var(--el-text-color-placeholder);
      }
    }

    .list-notice-left {
      flex-shrink: 0;
      width: 8px;
      display: flex;
      align-items: center;
      padding-top: 6px;
      margin-right: 8px;

      .unread-dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: var(--el-color-primary);
      }
    }

    .list-notice-content {
      flex: 1;
      min-width: 0;
    }

    .list-notice-title {
      display: flex;
      align-items: center;
      gap: 6px;

      .title-text {
        font-size: 14px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        color: var(--el-text-color-primary);
      }
    }

    .list-notice-time {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin-top: 4px;
    }
  }
}

.list-drawer-footer {
  display: flex;
  justify-content: center;
  padding: 12px 0;
  border-top: 1px solid var(--el-border-color-lighter);
}
</style>
