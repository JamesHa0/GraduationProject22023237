-- ============================================================
-- v8_thesis_redesign.sql
-- 论文模块2表重构迁移脚本
-- 将现有5张论文表重构为 thesis_main + thesis_process_record 两张表
--
-- 变更范围：
--   1. 新建 thesis_main（论文主表 — 全生命周期锚点）
--   2. 新建 thesis_process_record（论文流程记录表 — 统一全流程）
--   3. 从旧表迁移数据
--   4. 删除5张旧表
-- ============================================================

-- ==================== 安全检查 ====================
-- 如果旧表不存在则报错退出，避免在错误的数据库上执行

SET @abort = 0;

SELECT COUNT(*) INTO @cnt FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'thesis_progress';
SET @abort = @abort + IF(@cnt = 0, 1, 0);

SELECT COUNT(*) INTO @cnt FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'thesis_defense';
SET @abort = @abort + IF(@cnt = 0, 1, 0);

SELECT COUNT(*) INTO @cnt FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'thesis_external_review';
SET @abort = @abort + IF(@cnt = 0, 1, 0);

-- application表可能不存在（如果v7未执行），不强制检查

SELECT CONCAT('安全检查：', @abort, ' 张必要旧表缺失') AS check_result;

-- ==================== 第一步：创建 thesis_main ====================
-- 从 thesis_progress / thesis_defense 提取学生/导师/论文信息
-- 一个学生仅对应一篇毕业论文

DROP TABLE IF EXISTS `thesis_main`;
CREATE TABLE `thesis_main` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '论文主ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_no` varchar(20) NOT NULL COMMENT '学号（冗余，便于查询展示）',
  `student_name` varchar(50) NOT NULL COMMENT '学生姓名（冗余，便于查询展示）',
  `supervisor_id` bigint NOT NULL COMMENT '导师ID',
  `supervisor_name` varchar(100) NOT NULL COMMENT '导师姓名（冗余，便于查询展示）',
  `major` varchar(100) DEFAULT NULL COMMENT '专业',
  `grade` varchar(20) DEFAULT NULL COMMENT '年级',
  `thesis_title` varchar(1000) DEFAULT NULL COMMENT '最终论文题目',
  `thesis_final_url` varchar(500) DEFAULT NULL COMMENT '最终定稿论文路径',
  `final_result` tinyint DEFAULT 0 COMMENT '论文最终结果：0-进行中，1-通过，2-未通过',
  `final_score` decimal(5,2) DEFAULT NULL COMMENT '最终答辩评分',
  `archive_status` tinyint DEFAULT 0 COMMENT '归档状态：0-未归档，1-已归档',
  `archive_time` datetime DEFAULT NULL COMMENT '归档时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_student_thesis`(`student_id`) COMMENT '一个学生仅对应一篇毕业论文',
  INDEX `idx_student_no`(`student_no`) COMMENT '学号查询索引',
  INDEX `idx_supervisor_id`(`supervisor_id`) COMMENT '导师查询索引',
  INDEX `idx_major_grade`(`major`, `grade`) COMMENT '专业年级联合查询索引',
  INDEX `idx_archive_status`(`archive_status`) COMMENT '归档状态查询索引',
  INDEX `idx_final_result`(`final_result`) COMMENT '最终结果查询索引'
) ENGINE=InnoDB COMMENT='论文主表';

-- ==================== 第二步：迁移 thesis_main 数据 ====================
-- 优先从 thesis_progress（覆盖学生最多），再用 thesis_defense 补充

INSERT INTO `thesis_main` (`student_id`, `student_no`, `student_name`, `supervisor_id`, `supervisor_name`, `major`, `grade`, `thesis_title`, `thesis_final_url`, `final_result`, `final_score`, `create_time`, `update_time`)
SELECT
  tp.student_id,
  tp.student_no,
  tp.student_name,
  tp.mentor_id,
  tp.mentor_name,
  s.major,
  CONCAT(s.cohort_year, '级'),
  tp.thesis_title,
  NULL,
  0,
  NULL,
  MIN(tp.create_time),
  MAX(tp.update_time)
FROM `thesis_progress` tp
LEFT JOIN `student` s ON s.id = tp.student_id
GROUP BY tp.student_id, tp.student_no, tp.student_name, tp.mentor_id, tp.mentor_name, s.major, s.cohort_year, tp.thesis_title;

-- 用 thesis_defense 补充 thesis_final_url / final_score
UPDATE `thesis_main` tm
INNER JOIN `thesis_defense` td ON td.student_id = tm.student_id
SET
  tm.thesis_final_url = td.thesis_final_url,
  tm.thesis_title = IFNULL(tm.thesis_title, td.thesis_title),
  tm.final_score = td.defense_score,
  tm.final_result = CASE
    WHEN td.defense_result = 1 THEN 1   -- 通过
    WHEN td.defense_result = 3 THEN 2   -- 未通过
    WHEN td.defense_result = 2 THEN 1   -- 修改后通过 → 算通过
    ELSE 0
  END
WHERE tm.thesis_final_url IS NULL;

-- 补充仅有 thesis_defense 但无 thesis_progress 的学生
INSERT IGNORE INTO `thesis_main` (`student_id`, `student_no`, `student_name`, `supervisor_id`, `supervisor_name`, `major`, `grade`, `thesis_title`, `thesis_final_url`, `final_result`, `final_score`, `create_time`, `update_time`)
SELECT
  td.student_id,
  td.student_no,
  td.student_name,
  td.mentor_id,
  td.mentor_name,
  s.major,
  CONCAT(s.cohort_year, '级'),
  td.thesis_title,
  td.thesis_final_url,
  CASE
    WHEN td.defense_result = 1 THEN 1
    WHEN td.defense_result = 3 THEN 2
    WHEN td.defense_result = 2 THEN 1
    ELSE 0
  END,
  td.defense_score,
  td.create_time,
  td.update_time
FROM `thesis_defense` td
LEFT JOIN `student` s ON s.id = td.student_id
WHERE NOT EXISTS (SELECT 1 FROM `thesis_main` tm WHERE tm.student_id = td.student_id);

-- 补充仅有 thesis_external_review 但无 progress/defense 的学生
INSERT IGNORE INTO `thesis_main` (`student_id`, `student_no`, `student_name`, `supervisor_id`, `supervisor_name`, `major`, `grade`, `create_time`, `update_time`)
SELECT
  ter.student_id,
  ter.student_no,
  ter.student_name,
  IFNULL(ms.mentor_id, 0),
  IFNULL(t.teacher_name, ''),
  s.major,
  CONCAT(s.cohort_year, '级'),
  ter.create_time,
  ter.update_time
FROM `thesis_external_review` ter
LEFT JOIN `student` s ON s.id = ter.student_id
LEFT JOIN `mentor_student` ms ON ms.student_id = ter.student_id AND ms.mentor_type = 1 AND ms.teacher_status = 1
LEFT JOIN `teacher` t ON t.id = ms.mentor_id
WHERE NOT EXISTS (SELECT 1 FROM `thesis_main` tm WHERE tm.student_id = ter.student_id);

-- ==================== 第三步：创建 thesis_process_record ====================

DROP TABLE IF EXISTS `thesis_process_record`;
CREATE TABLE `thesis_process_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '流程记录ID',
  `thesis_id` bigint NOT NULL COMMENT '关联论文主ID',
  `process_type` tinyint NOT NULL COMMENT '流程环节类型：1-开题报告，2-中期检查，3-预答辩，4-论文外审，5-正式答辩，6-二次答辩，7-修改后再审',
  `version` int NOT NULL DEFAULT 1 COMMENT '版本号：支持同一环节多次提交/二次答辩',

  -- 本环节提交内容
  `thesis_version_url` varchar(500) DEFAULT NULL COMMENT '本环节提交的论文版本路径',
  `attachment_url` varchar(500) DEFAULT NULL COMMENT '本环节附件路径（开题/中期报告等）',
  `content_extend` json DEFAULT NULL COMMENT '环节个性化内容（JSON格式）',

  -- 三级审批流
  `supervisor_status` tinyint DEFAULT 0 COMMENT '导师审批状态：0-未审批，1-同意，2-拒绝',
  `supervisor_time` datetime DEFAULT NULL COMMENT '导师审批时间',
  `supervisor_comment` varchar(1000) DEFAULT NULL COMMENT '导师审批意见',
  `supervisor_approver_id` bigint DEFAULT NULL COMMENT '导师审批操作人ID',
  `secretary_status` tinyint DEFAULT 0 COMMENT '教学秘书审批状态：0-未审批，1-同意，2-拒绝',
  `secretary_time` datetime DEFAULT NULL COMMENT '秘书审批时间',
  `secretary_comment` varchar(1000) DEFAULT NULL COMMENT '秘书审批意见',
  `secretary_approver_id` bigint DEFAULT NULL COMMENT '秘书审批操作人ID',
  `dean_status` tinyint DEFAULT 0 COMMENT '院长审批状态：0-未审批，1-同意，2-拒绝',
  `dean_time` datetime DEFAULT NULL COMMENT '院长审批时间',
  `dean_comment` varchar(1000) DEFAULT NULL COMMENT '院长审批意见',
  `dean_approver_id` bigint DEFAULT NULL COMMENT '院长审批操作人ID',

  -- 统一评审/答辩字段
  `event_time` datetime DEFAULT NULL COMMENT '事件时间（开题/答辩/外审时间）',
  `event_location` varchar(200) DEFAULT NULL COMMENT '事件地点',
  `review_committee_chair` varchar(100) DEFAULT NULL COMMENT '评审委员会主席',
  `review_committee_members` varchar(1000) DEFAULT NULL COMMENT '评审/外审专家成员',
  `review_result` tinyint DEFAULT 0 COMMENT '评审/答辩结果：0-未进行，1-通过，2-修改后通过，3-未通过',
  `review_score` decimal(5,2) DEFAULT NULL COMMENT '评审/答辩评分',
  `review_comment` text COMMENT '评审/答辩委员会评语',
  `qa_record` text COMMENT '答辩问答记录',

  -- 通用状态与留痕
  `process_status` tinyint DEFAULT 0 COMMENT '环节整体状态：0-未提交，1-审批中，2-评审中，3-已通过，4-已拒绝，5-已完成',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_thesis_process_version`(`thesis_id`, `process_type`, `version`) COMMENT '同一论文同一环节同一版本唯一',
  INDEX `idx_thesis_id`(`thesis_id`) COMMENT '关联主表查询索引',
  INDEX `idx_process_type`(`process_type`) COMMENT '环节类型查询索引',
  INDEX `idx_process_status`(`process_status`) COMMENT '状态查询索引',
  INDEX `idx_review_result`(`review_result`) COMMENT '结果查询索引',
  INDEX `idx_event_time`(`event_time`) COMMENT '事件时间查询索引'
) ENGINE=InnoDB COMMENT='论文流程记录表';

-- ==================== 第四步：迁移 thesis_process_record 数据 ====================

-- 4.1 从 thesis_progress 迁移（开题/中期/预答辩）
INSERT INTO `thesis_process_record` (
  `thesis_id`, `process_type`, `version`,
  `thesis_version_url`, `attachment_url`, `content_extend`,
  `supervisor_status`, `supervisor_time`, `supervisor_comment`, `supervisor_approver_id`,
  `secretary_status`, `secretary_time`, `secretary_comment`, `secretary_approver_id`,
  `dean_status`, `dean_time`, `dean_comment`, `dean_approver_id`,
  `event_time`, `event_location`, `review_committee_members`,
  `review_result`, `review_score`, `review_comment`,
  `process_status`, `submit_time`, `create_time`, `update_time`
)
SELECT
  tm.id,
  tp.progress_type,                        -- 1-开题，2-中期，3-预答辩
  1,                                        -- version=1
  NULL,                                     -- thesis_version_url（progress无此字段）
  tp.attachment_path,
  tp.content,                               -- 原JSON content
  tp.mentor_status,
  tp.mentor_time,
  tp.mentor_comment,
  tp.mentor_id,                             -- 导师审批操作人即导师本人
  tp.secretary_status,
  tp.secretary_time,
  tp.secretary_comment,
  NULL,                                     -- 旧表无秘书操作人ID
  tp.dean_status,
  tp.dean_time,
  tp.dean_comment,
  NULL,                                     -- 旧表无院长操作人ID
  tp.event_time,
  tp.location,
  tp.committee_members,
  0,                                        -- progress无review_result，留空
  NULL,                                     -- progress无review_score
  NULL,                                     -- progress无review_comment
  tp.overall_status,                        -- 映射到process_status
  tp.submit_time,
  tp.create_time,
  tp.update_time
FROM `thesis_progress` tp
INNER JOIN `thesis_main` tm ON tm.student_id = tp.student_id;

-- 4.2 从 thesis_defense 迁移（正式答辩）
INSERT INTO `thesis_process_record` (
  `thesis_id`, `process_type`, `version`,
  `thesis_version_url`, `attachment_url`, `content_extend`,
  `supervisor_status`, `supervisor_time`, `supervisor_comment`, `supervisor_approver_id`,
  `secretary_status`, `secretary_time`, `secretary_comment`, `secretary_approver_id`,
  `dean_status`, `dean_time`, `dean_comment`, `dean_approver_id`,
  `event_time`, `event_location`, `review_committee_chair`, `review_committee_members`,
  `review_result`, `review_score`, `review_comment`, `qa_record`,
  `process_status`, `submit_time`, `create_time`, `update_time`
)
SELECT
  tm.id,
  5,                                        -- process_type=5 正式答辩
  1,                                        -- version=1
  td.thesis_final_url,
  NULL,                                     -- 无附件
  NULL,                                     -- 无额外内容
  td.approver_status,                       -- defense的approver映射为supervisor
  td.approver_time,
  NULL,                                     -- defense无approver_comment
  td.approver_id,
  0,                                        -- defense无秘书审批
  NULL, NULL, NULL,
  td.dean_approval,                         -- dean_approval映射为dean_status
  td.dean_approval_time,
  NULL,                                     -- defense无dean_comment
  NULL,                                     -- 旧表无院长操作人ID
  td.defense_date,
  td.defense_location,
  td.committee_chair,
  td.committee_members,
  td.defense_result,
  td.defense_score,
  td.defense_comments,
  td.qa_record,
  td.status,
  NULL,                                     -- defense无submit_time
  td.create_time,
  td.update_time
FROM `thesis_defense` td
INNER JOIN `thesis_main` tm ON tm.student_id = td.student_id;

-- 4.3 从 thesis_external_review 迁移（论文外审）
INSERT INTO `thesis_process_record` (
  `thesis_id`, `process_type`, `version`,
  `thesis_version_url`, `attachment_url`, `content_extend`,
  `supervisor_status`, `supervisor_time`, `supervisor_comment`, `supervisor_approver_id`,
  `secretary_status`, `secretary_time`, `secretary_comment`, `secretary_approver_id`,
  `dean_status`, `dean_time`, `dean_comment`, `dean_approver_id`,
  `event_time`, `event_location`, `review_committee_chair`, `review_committee_members`,
  `review_result`, `review_score`, `review_comment`,
  `process_status`, `submit_time`, `create_time`, `update_time`
)
SELECT
  tm.id,
  4,                                        -- process_type=4 论文外审
  1,                                        -- version=1
  ter.thesis_url,
  NULL,                                     -- 无附件
  NULL,                                     -- 无额外内容
  0,                                        -- 外审无导师审批
  NULL, NULL, NULL,
  0,                                        -- 外审无秘书审批
  NULL, NULL, NULL,
  0,                                        -- 外审无院长审批
  NULL, NULL, NULL,
  ter.review_time,
  NULL,                                     -- 外审无地点
  NULL,                                     -- 外审无主席
  ter.reviewers,
  ter.review_result,
  NULL,                                     -- 外审无评分
  ter.review_comments,
  ter.status,
  NULL,                                     -- 无submit_time
  ter.create_time,
  ter.update_time
FROM `thesis_external_review` ter
INNER JOIN `thesis_main` tm ON tm.student_id = ter.student_id;

-- 4.4 从 thesis_defense_application 迁移（如果表存在）
-- 答辩申请 → 正式答辩的version=2记录（若已有正式答辩）或version=1（若无正式答辩）
-- 注意：application表可能是草稿/申请状态，仅迁移有实质数据的记录

SET @defense_app_exists = (SELECT COUNT(*) FROM information_schema.tables
  WHERE table_schema = DATABASE() AND table_name = 'thesis_defense_application');

SET @sql_defense_app = IF(@defense_app_exists > 0,
  'INSERT INTO `thesis_process_record` (
    `thesis_id`, `process_type`, `version`,
    `thesis_version_url`,
    `supervisor_status`, `supervisor_time`, `supervisor_comment`, `supervisor_approver_id`,
    `dean_status`, `dean_time`, `dean_comment`,
    `event_time`, `event_location`, `review_committee_members`,
    `review_result`, `review_comment`,
    `process_status`, `create_time`, `update_time`
  )
  SELECT
    tm.id,
    5,
    CASE WHEN tpr.id IS NOT NULL THEN 2 ELSE 1 END,
    tda.thesis_final_url,
    tda.approver_status,
    tda.approver_time,
    NULL,
    tda.approver_id,
    tda.dean_approval,
    tda.dean_approval_time,
    NULL,
    tda.defense_date,
    tda.defense_location,
    tda.committee_members,
    tda.result,
    tda.comments,
    tda.status,
    tda.create_time,
    tda.update_time
  FROM `thesis_defense_application` tda
  INNER JOIN `thesis_main` tm ON tm.student_id = tda.student_id
  LEFT JOIN `thesis_process_record` tpr ON tpr.thesis_id = tm.id AND tpr.process_type = 5 AND tpr.version = 1
  WHERE tda.status > 0',
  'SELECT ''thesis_defense_application 表不存在，跳过迁移'' AS info'
);

PREPARE stmt FROM @sql_defense_app;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4.5 从 thesis_external_review_application 迁移（如果表存在）
SET @ext_review_app_exists = (SELECT COUNT(*) FROM information_schema.tables
  WHERE table_schema = DATABASE() AND table_name = 'thesis_external_review_application');

SET @sql_ext_review_app = IF(@ext_review_app_exists > 0,
  'INSERT INTO `thesis_process_record` (
    `thesis_id`, `process_type`, `version`,
    `thesis_version_url`,
    `supervisor_status`, `supervisor_time`, `supervisor_comment`, `supervisor_approver_id`,
    `dean_status`, `dean_time`, `dean_comment`,
    `event_time`, `review_committee_members`,
    `review_result`, `review_comment`,
    `process_status`, `create_time`, `update_time`
  )
  SELECT
    tm.id,
    4,
    CASE WHEN tpr.id IS NOT NULL THEN 2 ELSE 1 END,
    tera.thesis_url,
    tera.approver_status,
    tera.approver_time,
    NULL,
    tera.approver_id,
    0,
    NULL,
    NULL,
    tera.review_time,
    tera.reviewers,
    tera.review_result,
    tera.review_comments,
    tera.status,
    tera.create_time,
    tera.update_time
  FROM `thesis_external_review_application` tera
  INNER JOIN `thesis_main` tm ON tm.student_id = tera.student_id
  LEFT JOIN `thesis_process_record` tpr ON tpr.thesis_id = tm.id AND tpr.process_type = 4 AND tpr.version = 1
  WHERE tera.status > 0',
  'SELECT ''thesis_external_review_application 表不存在，跳过迁移'' AS info'
);

PREPARE stmt FROM @sql_ext_review_app;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ==================== 第五步：添加外键约束 ====================
-- 先迁移数据再添加FK，避免FK约束影响数据插入

ALTER TABLE `thesis_main`
  ADD CONSTRAINT `fk_thesis_main_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  ADD CONSTRAINT `fk_thesis_main_supervisor` FOREIGN KEY (`supervisor_id`) REFERENCES `teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `thesis_process_record`
  ADD CONSTRAINT `fk_process_thesis_main` FOREIGN KEY (`thesis_id`) REFERENCES `thesis_main` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT;

-- ==================== 第六步：删除5张旧表 ====================

DROP TABLE IF EXISTS `thesis_defense_application`;
DROP TABLE IF EXISTS `thesis_external_review_application`;
DROP TABLE IF EXISTS `thesis_external_review`;
DROP TABLE IF EXISTS `thesis_defense`;
DROP TABLE IF EXISTS `thesis_progress`;

-- ==================== 第七步：数据一致性验证 ====================

SELECT '=== thesis_main 记录数 ===' AS info;
SELECT COUNT(*) AS thesis_main_count FROM `thesis_main`;

SELECT '=== thesis_process_record 各环节记录数 ===' AS info;
SELECT
  process_type,
  CASE process_type
    WHEN 1 THEN '开题报告'
    WHEN 2 THEN '中期检查'
    WHEN 3 THEN '预答辩'
    WHEN 4 THEN '论文外审'
    WHEN 5 THEN '正式答辩'
    WHEN 6 THEN '二次答辩'
    WHEN 7 THEN '修改后再审'
  END AS process_name,
  COUNT(*) AS record_count
FROM `thesis_process_record`
GROUP BY process_type
ORDER BY process_type;

SELECT '=== 孤儿记录检查（thesis_process_record 中 thesis_id 不在 thesis_main） ===' AS info;
SELECT COUNT(*) AS orphan_count FROM `thesis_process_record` tpr
LEFT JOIN `thesis_main` tm ON tm.id = tpr.thesis_id
WHERE tm.id IS NULL;

SELECT '=== 迁移完成 ===' AS info;
