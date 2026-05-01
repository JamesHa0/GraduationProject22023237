<template>
  <el-descriptions :column="2" border>
    <el-descriptions-item label="学号">{{ row.thesisMain?.studentNo || '-' }}</el-descriptions-item>
    <el-descriptions-item label="姓名">{{ row.thesisMain?.studentName || '-' }}</el-descriptions-item>
    <el-descriptions-item label="论文题目" :span="2">{{ row.thesisMain?.thesisTitle || '-' }}</el-descriptions-item>
    <el-descriptions-item label="版本">{{ row.version || '-' }}</el-descriptions-item>
    <el-descriptions-item label="环节状态">
      <el-tag :type="getProcessStatusType(row.processStatus)">
        {{ getProcessStatusText(row.processStatus) }}
      </el-tag>
    </el-descriptions-item>

    <!-- 开题报告详情 -->
    <template v-if="processType === 1">
      <el-descriptions-item label="研究背景" :span="2">{{ row.contentExtend?.background || '-' }}</el-descriptions-item>
      <el-descriptions-item label="研究现状" :span="2">{{ row.contentExtend?.researchStatus || '-' }}</el-descriptions-item>
      <el-descriptions-item label="研究内容" :span="2">{{ row.contentExtend?.researchContent || '-' }}</el-descriptions-item>
      <el-descriptions-item label="研究方法" :span="2">{{ row.contentExtend?.researchMethod || '-' }}</el-descriptions-item>
    </template>

    <!-- 中期检查详情 -->
    <template v-if="processType === 2">
      <el-descriptions-item label="已完成工作" :span="2">{{ row.contentExtend?.completedWork || '-' }}</el-descriptions-item>
      <el-descriptions-item label="未完成工作" :span="2">{{ row.contentExtend?.remainingWork || '-' }}</el-descriptions-item>
      <el-descriptions-item label="遇到的问题" :span="2">{{ row.contentExtend?.problems || '-' }}</el-descriptions-item>
      <el-descriptions-item label="下一步计划" :span="2">{{ row.contentExtend?.nextPlan || '-' }}</el-descriptions-item>
      <el-descriptions-item label="论文初稿完成度">{{ row.contentExtend?.draftProgress || '-' }}%</el-descriptions-item>
    </template>

    <!-- 预答辩详情 -->
    <template v-if="processType === 3">
      <el-descriptions-item label="论文摘要" :span="2">
        <div style="white-space: pre-wrap">{{ row.contentExtend?.abstractContent || '-' }}</div>
      </el-descriptions-item>
    </template>

    <!-- 外审详情 -->
    <template v-if="processType === 4">
      <el-descriptions-item label="外审专家">{{ row.contentExtend?.reviewerName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="专家单位">{{ row.contentExtend?.reviewerInstitution || '-' }}</el-descriptions-item>
      <el-descriptions-item label="评审领域" :span="2">{{ row.contentExtend?.reviewField || '-' }}</el-descriptions-item>
      <el-descriptions-item label="评审结果">
        <el-tag :type="getReviewResultType(row.reviewResult)">{{ getReviewResultText(row.reviewResult) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="评审评分">{{ row.reviewScore || '-' }}</el-descriptions-item>
      <el-descriptions-item label="评审意见" :span="2">{{ row.reviewComment || '-' }}</el-descriptions-item>
    </template>

    <!-- 答辩/二次答辩详情 -->
    <template v-if="processType === 5 || processType === 6">
      <el-descriptions-item label="答辩委员会主席">{{ row.reviewCommitteeChair || '-' }}</el-descriptions-item>
      <el-descriptions-item label="答辩委员">{{ row.reviewCommitteeMembers || '-' }}</el-descriptions-item>
      <el-descriptions-item label="答辩结果">
        <el-tag :type="getReviewResultType(row.reviewResult)">{{ getReviewResultText(row.reviewResult) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="答辩评分">{{ row.reviewScore || '-' }}</el-descriptions-item>
      <el-descriptions-item label="答辩评语" :span="2">{{ row.reviewComment || '-' }}</el-descriptions-item>
      <el-descriptions-item label="问答记录" :span="2">
        <div style="white-space: pre-wrap">{{ row.qaRecord || '-' }}</div>
      </el-descriptions-item>
    </template>

    <!-- 修改后再审详情 -->
    <template v-if="processType === 7">
      <el-descriptions-item label="修改说明" :span="2">{{ row.contentExtend?.modificationDescription || '-' }}</el-descriptions-item>
      <el-descriptions-item label="修改详情" :span="2">
        <div style="white-space: pre-wrap">{{ row.contentExtend?.modificationDetails || '-' }}</div>
      </el-descriptions-item>
      <el-descriptions-item label="评审结果">
        <el-tag :type="getReviewResultType(row.reviewResult)">{{ getReviewResultText(row.reviewResult) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="评审评分">{{ row.reviewScore || '-' }}</el-descriptions-item>
    </template>

    <!-- 通用审批信息 -->
    <el-descriptions-item label="时间">{{ parseDate(row.eventTime) }}</el-descriptions-item>
    <el-descriptions-item label="地点">{{ row.eventLocation || '-' }}</el-descriptions-item>
    <el-descriptions-item label="论文版本" :span="2">
      <el-link v-if="row.thesisVersionUrl" type="primary" :href="row.thesisVersionUrl" target="_blank">查看论文版本</el-link>
      <span v-else>-</span>
    </el-descriptions-item>
    <el-descriptions-item label="附件" :span="2">
      <template v-if="row.attachmentUrl">
        <el-link v-for="(url, idx) in parseFileList(row.attachmentUrl)" :key="idx" type="primary" :href="url" target="_blank" style="margin-right: 10px">
          附件{{ idx + 1 }}
        </el-link>
      </template>
      <span v-else>-</span>
    </el-descriptions-item>
    <el-descriptions-item label="导师审批">
      <el-tag :type="getApprovalStatusType(row.supervisorStatus)">
        {{ getApprovalStatusText(row.supervisorStatus) }}
      </el-tag>
      <span v-if="row.supervisorComment" style="margin-left: 10px">意见: {{ row.supervisorComment }}</span>
    </el-descriptions-item>
    <el-descriptions-item label="导师审批时间">{{ parseDate(row.supervisorTime) }}</el-descriptions-item>
    <el-descriptions-item label="教学秘书审批">
      <el-tag :type="getApprovalStatusType(row.secretaryStatus)">
        {{ getApprovalStatusText(row.secretaryStatus) }}
      </el-tag>
      <span v-if="row.secretaryComment" style="margin-left: 10px">意见: {{ row.secretaryComment }}</span>
    </el-descriptions-item>
    <el-descriptions-item label="秘书审批时间">{{ parseDate(row.secretaryTime) }}</el-descriptions-item>
    <el-descriptions-item label="分管院长审批">
      <el-tag :type="getApprovalStatusType(row.deanStatus)">
        {{ getApprovalStatusText(row.deanStatus) }}
      </el-tag>
      <span v-if="row.deanComment" style="margin-left: 10px">意见: {{ row.deanComment }}</span>
    </el-descriptions-item>
    <el-descriptions-item label="院长审批时间">{{ parseDate(row.deanTime) }}</el-descriptions-item>
    <el-descriptions-item label="提交时间" :span="2">{{ parseDate(row.submitTime) }}</el-descriptions-item>
  </el-descriptions>
</template>

<script setup>
import { getApprovalStatusText, getApprovalStatusType, getProcessStatusText, getProcessStatusType, getReviewResultText, getReviewResultType, parseDate, parseFileList } from '@/composables/useDegreeStatus'
import PdfPreview from '@/components/PdfPreview/index.vue'

defineProps({
  row: { type: Object, required: true },
  processType: { type: Number, required: true }
})
</script>
