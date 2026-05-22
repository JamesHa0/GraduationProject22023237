<template>
  <draggable
    :list="items"
    :animation="200"
    handle=".drag-handle"
    item-key="id"
    ghost-class="sortable-ghost"
    @end="onDragEnd"
  >
    <template #item="{ element }">
      <div class="menu-tree-node">
        <div class="menu-tree-row" :class="{ 'is-top-level': !element.parentId || element.parentId === 0 }">
          <!-- 拖拽手柄 -->
          <span class="drag-handle" title="拖拽排序">
            <svg viewBox="0 0 16 16" width="16" height="16" fill="none">
              <line x1="2" y1="5" x2="14" y2="5" stroke="currentColor" stroke-width="1.5" stroke-dasharray="2 1.5" />
              <line x1="2" y1="11" x2="14" y2="11" stroke="currentColor" stroke-width="1.5" stroke-dasharray="2 1.5" />
            </svg>
          </span>

          <!-- 展开/折叠箭头 -->
          <span
            class="expand-arrow"
            :class="{ 'has-children': element.children && element.children.length }"
            @click="toggleNode(element.id)"
          >
            <el-icon v-if="element.children && element.children.length">
              <ArrowRight v-if="!isNodeExpanded(element.id)" />
              <ArrowDown v-else />
            </el-icon>
            <span v-else class="expand-placeholder"></span>
          </span>

          <!-- 排序号（只读展示） -->
          <span class="col col-sort">{{ element.sort }}</span>

          <!-- 菜单标题 -->
          <span class="col col-title" :style="{ paddingLeft: level * 24 + 'px' }">
            <span class="title-text">{{ element.title }}</span>
          </span>

          <!-- 图标 -->
          <span class="col col-icon">
            <svg-icon v-if="element.icon" :icon-class="element.icon" />
            <span v-else>-</span>
          </span>

          <!-- 菜单索引 -->
          <span class="col col-index">{{ element.menusIndex }}</span>

          <!-- 路径 -->
          <span class="col col-path" :title="element.path">{{ element.path || '-' }}</span>

          <!-- 父级索引 -->
          <span class="col col-parent">{{ element.parentId }}</span>

          <!-- 操作 -->
          <span class="col col-actions">
            <el-button link type="primary" icon="Edit" @click.stop="$emit('edit', element)">修改</el-button>
          </span>
        </div>

        <!-- 子菜单（递归渲染） -->
        <MenuTreeItem
          v-if="isNodeExpanded(element.id) && element.children && element.children.length"
          :items="element.children"
          :level="level + 1"
          @sort-change="onChildSortChange"
          @edit="$emit('edit', $event)"
        />
      </div>
    </template>
  </draggable>
</template>

<script setup>
import { inject, ref } from 'vue'
import draggable from 'vuedraggable/dist/vuedraggable.common'
import { ArrowRight, ArrowDown } from '@element-plus/icons-vue'

const props = defineProps({
  items: {
    type: Array,
    required: true
  },
  level: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['sort-change', 'edit'])

// 注入全局展开状态
const expandedIds = inject('expandedIds', null)
const expandAllFlag = inject('expandAllFlag', ref(false))

// 检查节点是否展开
function isNodeExpanded(id) {
  if (!expandedIds) return true
  return expandedIds.has(id)
}

// 切换单个节点展开/折叠
function toggleNode(id) {
  if (!expandedIds) return
  if (expandedIds.has(id)) {
    expandedIds.delete(id)
  } else {
    expandedIds.add(id)
  }
}

// 拖拽结束：重新计算 sort 并通知父组件保存
function onDragEnd() {
  // 先更新每个元素的 sort 属性（前端显示），sort 从 1 开始
  props.items.forEach((item, index) => {
    item.sort = index + 1
  })
  // 再通知父组件保存到后端
  const sortData = props.items.map((item, index) => ({
    id: item.id,
    sort: index + 1
  }))
  emit('sort-change', sortData)
}

// 子级排序变更，透传给父组件
function onChildSortChange(sortData) {
  emit('sort-change', sortData)
}
</script>

<style scoped>
.menu-tree-node {
  /* 节点容器 */
}

.menu-tree-row {
  display: flex;
  align-items: center;
  min-height: 42px;
  padding: 4px 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: #fff;
  transition: background 0.2s;
}

.menu-tree-row:hover {
  background: var(--el-fill-color-light);
}

.menu-tree-row.is-top-level {
  background: #fafafa;
}

.menu-tree-row.is-top-level:hover {
  background: var(--el-fill-color-light);
}

/* 拖拽手柄 */
.drag-handle {
  flex-shrink: 0;
  width: 24px;
  cursor: move;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  user-select: none;
}

.drag-handle:hover {
  color: var(--el-color-primary);
}

/* 展开箭头 */
.expand-arrow {
  flex-shrink: 0;
  width: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  cursor: default;
}

.expand-arrow.has-children {
  cursor: pointer;
}

.expand-arrow.has-children:hover {
  color: var(--el-color-primary);
}

.expand-placeholder {
  width: 12px;
}

/* 表格列通用样式 */
.col {
  flex-shrink: 0;
  padding: 0 8px;
  font-size: 14px;
  color: #303133;
}

.col-title {
  flex: 1;
  min-width: 160px;
  overflow: hidden;
}

.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.col-icon {
  width: 80px;
  text-align: center;
}

.col-sort {
  width: 80px;
  text-align: center;
  color: #909399;
  font-weight: 500;
}

.col-index {
  width: 80px;
  text-align: center;
}

.col-path {
  width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #606266;
}

.col-parent {
  width: 80px;
  text-align: center;
  color: #606266;
}

.col-actions {
  width: 80px;
  text-align: center;
  flex-shrink: 0;
}

/* 拖拽时的高亮样式 */
.sortable-ghost {
  opacity: 0.4;
  background: var(--el-color-primary-light-9) !important;
}

.sortable-chosen {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
</style>
