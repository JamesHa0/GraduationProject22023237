-- v3: 为审批记录表添加审批人附件字段
ALTER TABLE `academic_approval_record`
  ADD COLUMN `reviewer_file_urls` JSON NULL COMMENT '审批人附件URL列表（JSON数组）' AFTER `approval_comment`;
