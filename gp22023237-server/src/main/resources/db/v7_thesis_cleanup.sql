-- ============================================================
-- v7_thesis_cleanup.sql
-- 论文模块数据库清理迁移脚本
-- 1. 删除4张冗余备份表（被thesis_progress统一进展表覆盖）
-- 2. 重命名2张保留备份表为application（去掉_backup后缀）
-- 3. 统一3张表tutor→approver字段命名
-- 4. 清除3张表冗余索引
-- ============================================================

-- ==================== 第一步：删除冗余备份表 ====================
-- 这些表已被 thesis_progress(progress_type 1/2/3) 完全覆盖，后端无任何引用

DROP TABLE IF EXISTS `thesis_proposal_backup`;
DROP TABLE IF EXISTS `thesis_midterm_backup`;
DROP TABLE IF EXISTS `thesis_midterm_check_backup`;
DROP TABLE IF EXISTS `thesis_pre_defense_backup`;

-- ==================== 第二步：重命名保留备份表 ====================
-- 去掉 _backup 后缀，改为 _application 体现"申请/草稿暂存"语义

RENAME TABLE `thesis_defense_backup` TO `thesis_defense_application`;
RENAME TABLE `thesis_external_review_backup` TO `thesis_external_review_application`;

-- ==================== 第三步：统一字段命名（tutor → approver） ====================

-- ---------- 3.1 thesis_defense 主表 ----------
-- 需先删除引用 tutor_id 的外键和索引，再 CHANGE COLUMN，最后重建

ALTER TABLE `thesis_defense`
  DROP FOREIGN KEY `thesis_defense_ibfk_3`,
  DROP INDEX `tutor_id`,
  CHANGE COLUMN `tutor_approval` `approver_status` tinyint NULL DEFAULT 0 COMMENT '审批人审批状态：0-未审批，1-同意，2-拒绝',
  CHANGE COLUMN `tutor_id` `approver_id` bigint NULL DEFAULT NULL COMMENT '审批人ID',
  CHANGE COLUMN `tutor_approval_time` `approver_time` datetime NULL DEFAULT NULL COMMENT '审批人审批时间',
  ADD INDEX `idx_approver_id`(`approver_id` ASC) USING BTREE,
  ADD CONSTRAINT `thesis_defense_ibfk_3` FOREIGN KEY (`approver_id`) REFERENCES `teacher` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT;

-- ---------- 3.2 thesis_defense_application（原 thesis_defense_backup） ----------
-- 无外键和索引引用 tutor 字段，直接 CHANGE COLUMN

ALTER TABLE `thesis_defense_application`
  CHANGE COLUMN `tutor_approval` `approver_status` tinyint NULL DEFAULT 0 COMMENT '审批人审批状态：0-未审批，1-同意，2-拒绝',
  CHANGE COLUMN `tutor_id` `approver_id` bigint NULL DEFAULT NULL COMMENT '审批人ID',
  CHANGE COLUMN `tutor_approval_time` `approver_time` datetime NULL DEFAULT NULL COMMENT '审批人审批时间';

-- ---------- 3.3 thesis_external_review_application（原 thesis_external_review_backup） ----------
-- 无外键和索引引用 tutor 字段，直接 CHANGE COLUMN

ALTER TABLE `thesis_external_review_application`
  CHANGE COLUMN `tutor_approval` `approver_status` tinyint NULL DEFAULT 0 COMMENT '审批人审批状态：0-未审批，1-同意，2-拒绝',
  CHANGE COLUMN `tutor_id` `approver_id` bigint NULL DEFAULT NULL COMMENT '审批人ID',
  CHANGE COLUMN `tutor_approval_time` `approver_time` datetime NULL DEFAULT NULL COMMENT '审批人审批时间';

-- ==================== 第四步：清除冗余索引 ====================
-- 这些普通 idx_student_id 被同列的唯一索引完全覆盖

ALTER TABLE `thesis_defense` DROP INDEX `idx_student_id`;
ALTER TABLE `thesis_external_review` DROP INDEX `idx_student_id`;
ALTER TABLE `thesis_progress` DROP INDEX `idx_student_id`;
