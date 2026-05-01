<template>
  <el-descriptions :column="1" border>
    <el-descriptions-item label="学号">{{ row.studentNo || '-' }}</el-descriptions-item>
    <el-descriptions-item label="姓名">{{ row.studentName || '-' }}</el-descriptions-item>
    <el-descriptions-item label="活动类型">
      <el-tag :type="helpers.getTypeTagType(row.activityType)">{{ helpers.getTypeName(row.activityType) }}</el-tag>
    </el-descriptions-item>
    <el-descriptions-item label="活动名称">{{ row.activityName || '-' }}</el-descriptions-item>
    <el-descriptions-item label="主讲人">{{ row.speaker || '-' }}</el-descriptions-item>
    <el-descriptions-item label="活动时间">{{ parseDate(row.activityTime) }}</el-descriptions-item>
    <el-descriptions-item label="活动地点">{{ row.location || '-' }}</el-descriptions-item>
    <el-descriptions-item label="活动内容">
      <div style="white-space: pre-wrap">{{ row.content || '-' }}</div>
    </el-descriptions-item>
    <el-descriptions-item label="导师审批">
      <el-tag :type="getStatusType(row.mentorStatus)">{{ getStatusText(row.mentorStatus) }}</el-tag>
      <span v-if="row.mentorComment" style="margin-left: 10px">意见: {{ row.mentorComment }}</span>
    </el-descriptions-item>
    <el-descriptions-item label="教学秘书审批">
      <el-tag :type="getStatusType(row.secretaryStatus)">{{ getStatusText(row.secretaryStatus) }}</el-tag>
      <span v-if="row.secretaryComment" style="margin-left: 10px">意见: {{ row.secretaryComment }}</span>
    </el-descriptions-item>
    <el-descriptions-item label="分管院长审批">
      <el-tag :type="getStatusType(row.deanStatus)">{{ getStatusText(row.deanStatus) }}</el-tag>
      <span v-if="row.deanComment" style="margin-left: 10px">意见: {{ row.deanComment }}</span>
    </el-descriptions-item>
    <el-descriptions-item label="提交时间">{{ parseDate(row.submitTime) }}</el-descriptions-item>
    <el-descriptions-item label="附件" v-if="getAttachmentList(row.attachmentPath).length">
      <div class="attachment-list">
        <template v-for="(item, index) in getAttachmentList(row.attachmentPath)" :key="index">
          <el-image
            v-if="isImageFile(item)"
            :src="signedUrls[getUrlFromItem(item)] || ''"
            :preview-src-list="getSignedImageList(row.attachmentPath)"
            :initial-index="getImageListIndex(row.attachmentPath, item)"
            fit="cover"
            style="width: 100px; height: 100px; margin-right: 8px; border-radius: 4px"
          />
          <el-link v-else type="primary" :underline="false" style="margin-right: 12px" @click="handleDownload(item)">
            <el-icon><Document /></el-icon>
            {{ getFileNameFromItem(item) }}
          </el-link>
        </template>
      </div>
    </el-descriptions-item>
  </el-descriptions>
</template>

<script setup>
import { watch } from 'vue'
import { useAcademicApproval } from '@/composables/useAcademicApproval'
import { useAttachment } from '@/composables/useAttachment'
import { Document } from '@element-plus/icons-vue'

const { getStatusText, getStatusType, parseDate } = useAcademicApproval()
const {
  signedUrls, getUrlFromItem, getFileNameFromItem,
  getAttachmentPath, getAttachmentList, isImageFile,
  getImageList, getImageListIndex, fetchSignedUrls,
  getSignedImageList, handleDownload
} = useAttachment()

const props = defineProps({
  row: { type: Object, required: true },
  helpers: { type: Object, required: true }
})

// 监听 row 变化，获取签名URL
watch(() => props.row, () => {
  if (props.row && (props.row.attachmentPath || props.row.fileUrls)) {
    fetchSignedUrls([props.row.attachmentPath || props.row.fileUrls])
  }
}, { immediate: true })
</script>

<style scoped>
.attachment-list {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}
</style>
