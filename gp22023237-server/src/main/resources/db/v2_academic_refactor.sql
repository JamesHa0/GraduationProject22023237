-- ============================================================
-- 学术管理模块数据库表结构重构迁移脚本 v2
-- 执行顺序：建新表 → 迁移数据 → 新增字典 → 旧表备份
-- 可重复执行：使用 IF NOT EXISTS / IF EXISTS 判断
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- 第一部分：创建新表
-- ============================================================

-- 1. 学术内容提交主表
DROP TABLE IF EXISTS `academic_submission`;
CREATE TABLE `academic_submission` (
  `id`                    BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `student_id`            BIGINT       NOT NULL                 COMMENT '归属学生ID（关联student表）',
  `submitter_id`          BIGINT       NOT NULL                 COMMENT '提交人ID（学生=student.id，导师=teacher.id，管理员=user.id）',
  `submitter_type`        TINYINT      NOT NULL                 COMMENT '提交人类型：1=学生，2=导师，3=管理员',
  `content_type`          TINYINT      NOT NULL                 COMMENT '内容类型：1=学术活动，2=学术成果，3=创新创业',
  `title`                 VARCHAR(500) NOT NULL                 COMMENT '内容标题',
  `abstract_content`      TEXT         NULL                     COMMENT '内容摘要/说明',
  `file_urls`             JSON         NULL                     COMMENT '附件文件URL列表（JSON数组，如["/file1.pdf","/file2.pdf"]）',
  `approval_status`       TINYINT      NOT NULL DEFAULT 0       COMMENT '审批状态：0=待提交(草稿)，1=待导师审批，2=待秘书审批，3=待院长审批，4=已通过，5=已驳回',
  `current_approver_id`   BIGINT       NULL                     COMMENT '当前审批人ID',
  `current_approver_type` TINYINT      NULL                     COMMENT '当前审批人类型：2=导师(角色7)，5=教学秘书(角色5)，1=分管院长(角色2)',
  `submit_time`           DATETIME     NULL                     COMMENT '提交时间',
  `is_deleted`            TINYINT      NOT NULL DEFAULT 0       COMMENT '软删除：0=未删除，1=已删除',
  `version`               INT          NOT NULL DEFAULT 1       COMMENT '乐观锁版本号',
  `create_time`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_student_id` (`student_id`),
  INDEX `idx_submitter` (`submitter_id`, `submitter_type`),
  INDEX `idx_content_type` (`content_type`),
  INDEX `idx_approval_status` (`approval_status`),
  INDEX `idx_current_approver` (`current_approver_id`, `current_approver_type`),
  INDEX `idx_submit_time` (`submit_time`),
  INDEX `idx_deleted` (`is_deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='学术内容提交主表';

-- 2. 学术活动详情子表
DROP TABLE IF EXISTS `academic_activity_detail`;
CREATE TABLE `academic_activity_detail` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `submission_id`   BIGINT       NOT NULL                 COMMENT '关联提交主表ID',
  `activity_type`   TINYINT      NOT NULL DEFAULT 1       COMMENT '活动类型：1=学术讲座，2=研讨会，3=论坛，4=其他',
  `activity_name`   VARCHAR(255) NOT NULL                 COMMENT '活动名称',
  `activity_time`   DATETIME     NULL                     COMMENT '活动时间',
  `location`        VARCHAR(255) NULL                     COMMENT '活动地点',
  `speaker`         VARCHAR(100) NULL                     COMMENT '主讲人/主持人',
  `content`         TEXT         NULL                     COMMENT '活动内容描述',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_submission_id` (`submission_id`),
  INDEX `idx_activity_type` (`activity_type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='学术活动详情子表';

-- 3. 学术成果详情子表
DROP TABLE IF EXISTS `academic_achievement_detail`;
CREATE TABLE `academic_achievement_detail` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `submission_id`     BIGINT       NOT NULL                 COMMENT '关联提交主表ID',
  `achievement_type`  TINYINT      NOT NULL DEFAULT 1       COMMENT '成果类型：1=论文，2=专利，3=科研奖励，4=项目参与',
  `authors`           VARCHAR(255) NULL                     COMMENT '作者/参与者',
  `publication_date`  DATETIME     NULL                     COMMENT '发表/授权时间',
  `journal_name`      VARCHAR(255) NULL                     COMMENT '论文期刊名称',
  `journal_level`     TINYINT      NULL                     COMMENT '期刊级别：1=SCI/EI，2=核心期刊，3=普通期刊',
  `volume`            VARCHAR(20)  NULL                     COMMENT '卷号',
  `issue`             VARCHAR(20)  NULL                     COMMENT '期号',
  `pages`             VARCHAR(50)  NULL                     COMMENT '页码',
  `doi`               VARCHAR(255) NULL                     COMMENT 'DOI号',
  `patent_no`         VARCHAR(128) NULL                     COMMENT '专利号/软著登记号',
  `patent_type`       TINYINT      NULL                     COMMENT '专利类型：1=发明，2=实用新型，3=外观设计',
  `patent_status`     TINYINT      NULL                     COMMENT '授权状态：0=申请中，1=已授权',
  `award_name`        VARCHAR(255) NULL                     COMMENT '奖励名称',
  `award_level`       TINYINT      NULL                     COMMENT '奖励级别：1=国家级，2=省级，3=市级，4=校级',
  `award_issuer`      VARCHAR(255) NULL                     COMMENT '发奖单位',
  `project_name`      VARCHAR(255) NULL                     COMMENT '项目名称',
  `project_role`      TINYINT      NULL                     COMMENT '项目角色：1=负责人，2=核心成员，3=参与者',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_submission_id` (`submission_id`),
  INDEX `idx_achievement_type` (`achievement_type`),
  INDEX `idx_doi` (`doi`),
  INDEX `idx_patent_no` (`patent_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='学术成果详情子表';

-- 4. 创新创业详情子表
DROP TABLE IF EXISTS `academic_innovation_detail`;
CREATE TABLE `academic_innovation_detail` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `submission_id`   BIGINT        NOT NULL                 COMMENT '关联提交主表ID',
  `project_type`    TINYINT       NOT NULL DEFAULT 1       COMMENT '项目类型：1=创新项目，2=创业项目，3=竞赛',
  `project_name`    VARCHAR(255)  NOT NULL                 COMMENT '项目名称',
  `project_level`   TINYINT       NULL                     COMMENT '项目级别：1=国家级，2=省级，3=市级，4=校级',
  `project_no`      VARCHAR(50)   NULL                     COMMENT '项目编号',
  `leader`          VARCHAR(100)  NULL                     COMMENT '负责人',
  `members`         VARCHAR(500)  NULL                     COMMENT '参与成员',
  `advisor`         VARCHAR(100)  NULL                     COMMENT '指导老师',
  `start_date`      DATETIME      NULL                     COMMENT '项目开始时间',
  `end_date`        DATETIME      NULL                     COMMENT '项目结束时间',
  `description`     TEXT          NULL                     COMMENT '项目描述',
  `achievements`    TEXT          NULL                     COMMENT '项目成果/获奖情况',
  `award_level`     TINYINT       NULL                     COMMENT '获奖等级：1=特等奖，2=一等奖，3=二等奖，4=三等奖，5=优秀奖',
  `funding_amount`  DECIMAL(12,2) NULL                     COMMENT '资助金额（元）',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_submission_id` (`submission_id`),
  INDEX `idx_project_type` (`project_type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='创新创业项目详情子表';

-- 5. 审批流程记录表
DROP TABLE IF EXISTS `academic_approval_record`;
CREATE TABLE `academic_approval_record` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `submission_id`    BIGINT       NOT NULL                 COMMENT '关联提交主表ID',
  `approver_id`      BIGINT       NOT NULL                 COMMENT '审批人ID（关联teacher表或user表）',
  `approver_type`    TINYINT      NOT NULL                 COMMENT '审批人类型：2=导师(对应角色7)，5=教学秘书(对应角色5)，1=分管院长(对应角色2)',
  `approval_action`  TINYINT      NOT NULL                 COMMENT '审批动作：1=提交，2=通过，3=驳回，4=撤回',
  `approval_comment` TEXT         NULL                     COMMENT '审批意见',
  `approval_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
  `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_submission_id` (`submission_id`),
  INDEX `idx_approver` (`approver_id`, `approver_type`),
  INDEX `idx_approval_time` (`approval_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='学术审批流程记录表';


-- ============================================================
-- 第二部分：数据迁移（从旧表迁移到新表）
-- ============================================================

-- -------------------------------------------------------
-- 辅助函数：将旧的三字段审批状态转换为新的单字段审批状态
-- 旧: mentor_status(0/1/2), secretary_status(0/1/2), dean_status(0/1/2)
-- 新: approval_status(0=草稿, 1=待导师审批, 2=待秘书审批, 3=待院长审批, 4=已通过, 5=已驳回)
-- 逻辑：
--   全0且submit_time为NULL → 0(草稿)
--   全0且submit_time非NULL → 1(待导师审批)
--   mentor=1,secretary=0,dean=0 → 2(待秘书审批)
--   mentor=1,secretary=1,dean=0 → 3(待院长审批)
--   mentor=1,secretary=1,dean=1 → 4(已通过)
--   任一=2 → 5(已驳回)
-- -------------------------------------------------------

-- 2.1 迁移 academic_activity → academic_submission + academic_activity_detail
INSERT INTO `academic_submission` (
  `id`, `student_id`, `submitter_id`, `submitter_type`, `content_type`,
  `title`, `abstract_content`, `file_urls`, `approval_status`,
  `current_approver_id`, `current_approver_type`,
  `submit_time`, `is_deleted`, `version`, `create_time`, `update_time`
)
SELECT
  `id`,
  `student_id`,
  `student_id`   AS submitter_id,    -- 旧数据默认学生自提交
  1              AS submitter_type,   -- 1=学生
  1              AS content_type,     -- 1=学术活动
  `activity_name` AS title,
  `content`      AS abstract_content,
  CASE
    WHEN `attachment_path` IS NOT NULL AND `attachment_path` != ''
    THEN JSON_ARRAY(`attachment_path`)
    ELSE NULL
  END             AS file_urls,
  CASE
    WHEN `mentor_status` = 2 OR `secretary_status` = 2 OR `dean_status` = 2 THEN 5  -- 已驳回
    WHEN `mentor_status` = 1 AND `secretary_status` = 1 AND `dean_status` = 1 THEN 4  -- 已通过
    WHEN `mentor_status` = 1 AND `secretary_status` = 1 AND `dean_status` = 0 THEN 3  -- 待院长审批
    WHEN `mentor_status` = 1 AND `secretary_status` = 0 AND `dean_status` = 0 THEN 2  -- 待秘书审批
    WHEN `mentor_status` = 0 AND `secretary_status` = 0 AND `dean_status` = 0
         AND `submit_time` IS NOT NULL THEN 1  -- 待导师审批
    ELSE 0  -- 草稿
  END             AS approval_status,
  CASE
    WHEN `mentor_status` = 1 AND `secretary_status` = 1 AND `dean_status` = 0 THEN NULL  -- 待院长审批，暂无具体人
    WHEN `mentor_status` = 1 AND `secretary_status` = 0 AND `dean_status` = 0 THEN NULL  -- 待秘书审批，暂无具体人
    WHEN `mentor_status` = 0 AND `secretary_status` = 0 AND `dean_status` = 0
         AND `submit_time` IS NOT NULL THEN NULL  -- 待导师审批，暂无具体人
    ELSE NULL
  END             AS current_approver_id,
  CASE
    WHEN `mentor_status` = 1 AND `secretary_status` = 0 AND `dean_status` = 0 THEN 5  -- 教学秘书
    WHEN `mentor_status` = 1 AND `secretary_status` = 1 AND `dean_status` = 0 THEN 1  -- 分管院长
    WHEN `mentor_status` = 0 AND `secretary_status` = 0 AND `dean_status` = 0
         AND `submit_time` IS NOT NULL THEN 2  -- 导师
    ELSE NULL
  END             AS current_approver_type,
  `submit_time`,
  0              AS is_deleted,
  1              AS version,
  COALESCE(`create_time`, `submit_time`, NOW()) AS create_time,
  COALESCE(`update_time`, NOW()) AS update_time
FROM `academic_activity`;

-- academic_activity → academic_activity_detail
INSERT INTO `academic_activity_detail` (
  `submission_id`, `activity_type`, `activity_name`, `activity_time`, `location`, `speaker`, `content`
)
SELECT
  `id`             AS submission_id,
  `activity_type`,
  `activity_name`,
  `activity_time`,
  `location`,
  `speaker`,
  `content`
FROM `academic_activity`;


-- 2.2 迁移 academic_achievement → academic_submission + academic_achievement_detail
INSERT INTO `academic_submission` (
  `id`, `student_id`, `submitter_id`, `submitter_type`, `content_type`,
  `title`, `abstract_content`, `file_urls`, `approval_status`,
  `current_approver_id`, `current_approver_type`,
  `submit_time`, `is_deleted`, `version`, `create_time`, `update_time`
)
SELECT
  `id`,
  `student_id`,
  `student_id`   AS submitter_id,
  1              AS submitter_type,
  2              AS content_type,     -- 2=学术成果
  `title`,
  `abstract_content`,
  CASE
    WHEN `attachment_path` IS NOT NULL AND `attachment_path` != ''
    THEN JSON_ARRAY(`attachment_path`)
    ELSE NULL
  END             AS file_urls,
  CASE
    WHEN `mentor_status` = 2 OR `secretary_status` = 2 OR `dean_status` = 2 THEN 5
    WHEN `mentor_status` = 1 AND `secretary_status` = 1 AND `dean_status` = 1 THEN 4
    WHEN `mentor_status` = 1 AND `secretary_status` = 1 AND `dean_status` = 0 THEN 3
    WHEN `mentor_status` = 1 AND `secretary_status` = 0 AND `dean_status` = 0 THEN 2
    WHEN `mentor_status` = 0 AND `secretary_status` = 0 AND `dean_status` = 0
         AND `submit_time` IS NOT NULL THEN 1
    ELSE 0
  END             AS approval_status,
  NULL            AS current_approver_id,
  CASE
    WHEN `mentor_status` = 1 AND `secretary_status` = 0 AND `dean_status` = 0 THEN 5
    WHEN `mentor_status` = 1 AND `secretary_status` = 1 AND `dean_status` = 0 THEN 1
    WHEN `mentor_status` = 0 AND `secretary_status` = 0 AND `dean_status` = 0
         AND `submit_time` IS NOT NULL THEN 2
    ELSE NULL
  END             AS current_approver_type,
  `submit_time`,
  0              AS is_deleted,
  1              AS version,
  COALESCE(`create_time`, `submit_time`, NOW()) AS create_time,
  COALESCE(`update_time`, NOW()) AS update_time
FROM `academic_achievement`;

-- academic_achievement → academic_achievement_detail
INSERT INTO `academic_achievement_detail` (
  `submission_id`, `achievement_type`, `authors`, `publication_date`,
  `journal_name`, `journal_level`, `volume`, `issue`, `pages`, `doi`,
  `patent_no`, `patent_type`, `patent_status`,
  `award_name`, `award_level`, `award_issuer`,
  `project_name`, `project_role`
)
SELECT
  `id`              AS submission_id,
  `achievement_type`,
  `authors`,
  `publication_date`,
  `journal_name`,
  `journal_level`,
  `volume`,
  `issue`,
  `pages`,
  `doi`,
  `patent_no`,
  `patent_type`,
  `patent_status`,
  `award_name`,
  `award_level`,
  `award_issuer`,
  `project_name`,
  `project_role`
FROM `academic_achievement`;


-- 2.3 迁移 innovation_project → academic_submission + academic_innovation_detail
INSERT INTO `academic_submission` (
  `id`, `student_id`, `submitter_id`, `submitter_type`, `content_type`,
  `title`, `abstract_content`, `file_urls`, `approval_status`,
  `current_approver_id`, `current_approver_type`,
  `submit_time`, `is_deleted`, `version`, `create_time`, `update_time`
)
SELECT
  `id`,
  `student_id`,
  `student_id`   AS submitter_id,
  1              AS submitter_type,
  3              AS content_type,     -- 3=创新创业
  `project_name` AS title,
  `description`  AS abstract_content,
  CASE
    WHEN `attachment_path` IS NOT NULL AND `attachment_path` != ''
    THEN JSON_ARRAY(`attachment_path`)
    ELSE NULL
  END             AS file_urls,
  CASE
    WHEN `mentor_status` = 2 OR `secretary_status` = 2 OR `dean_status` = 2 THEN 5
    WHEN `mentor_status` = 1 AND `secretary_status` = 1 AND `dean_status` = 1 THEN 4
    WHEN `mentor_status` = 1 AND `secretary_status` = 1 AND `dean_status` = 0 THEN 3
    WHEN `mentor_status` = 1 AND `secretary_status` = 0 AND `dean_status` = 0 THEN 2
    WHEN `mentor_status` = 0 AND `secretary_status` = 0 AND `dean_status` = 0
         AND `submit_time` IS NOT NULL THEN 1
    ELSE 0
  END             AS approval_status,
  NULL            AS current_approver_id,
  CASE
    WHEN `mentor_status` = 1 AND `secretary_status` = 0 AND `dean_status` = 0 THEN 5
    WHEN `mentor_status` = 1 AND `secretary_status` = 1 AND `dean_status` = 0 THEN 1
    WHEN `mentor_status` = 0 AND `secretary_status` = 0 AND `dean_status` = 0
         AND `submit_time` IS NOT NULL THEN 2
    ELSE NULL
  END             AS current_approver_type,
  `submit_time`,
  0              AS is_deleted,
  1              AS version,
  COALESCE(`create_time`, `submit_time`, NOW()) AS create_time,
  COALESCE(`update_time`, NOW()) AS update_time
FROM `innovation_project`;

-- innovation_project → academic_innovation_detail
INSERT INTO `academic_innovation_detail` (
  `submission_id`, `project_type`, `project_name`, `project_level`, `project_no`,
  `leader`, `members`, `advisor`, `start_date`, `end_date`,
  `description`, `achievements`, `award_level`, `funding_amount`
)
SELECT
  `id`             AS submission_id,
  `project_type`,
  `project_name`,
  `project_level`,
  `project_no`,
  `leader`,
  `members`,
  `advisor`,
  `start_date`,
  `end_date`,
  `description`,
  `achievements`,
  `award_level`,
  `funding_amount`
FROM `innovation_project`;


-- ============================================================
-- 第三部分：生成旧审批记录的审批流程记录
-- 从旧表的三字段审批数据反推审批记录写入 academic_approval_record
-- ============================================================

-- 3.1 从 academic_activity 生成审批记录
-- 已提交的记录 → 生成"提交"动作记录
INSERT INTO `academic_approval_record` (`submission_id`, `approver_id`, `approver_type`, `approval_action`, `approval_comment`, `approval_time`, `create_time`)
SELECT `id`, `student_id`, 1, 1, NULL,
  COALESCE(`submit_time`, `create_time`, NOW()),
  COALESCE(`create_time`, NOW())
FROM `academic_activity` WHERE `submit_time` IS NOT NULL;

-- 导师已审批 → 生成"通过"或"驳回"记录
INSERT INTO `academic_approval_record` (`submission_id`, `approver_id`, `approver_type`, `approval_action`, `approval_comment`, `approval_time`, `create_time`)
SELECT `id`, NULL, 2,
  CASE WHEN `mentor_status` = 1 THEN 2 WHEN `mentor_status` = 2 THEN 3 ELSE 2 END,
  `mentor_comment`,
  COALESCE(`update_time`, NOW()),
  COALESCE(`create_time`, NOW())
FROM `academic_activity` WHERE `mentor_status` IN (1, 2);

-- 秘书已审批
INSERT INTO `academic_approval_record` (`submission_id`, `approver_id`, `approver_type`, `approval_action`, `approval_comment`, `approval_time`, `create_time`)
SELECT `id`, NULL, 5,
  CASE WHEN `secretary_status` = 1 THEN 2 WHEN `secretary_status` = 2 THEN 3 ELSE 2 END,
  `secretary_comment`,
  COALESCE(`update_time`, NOW()),
  COALESCE(`create_time`, NOW())
FROM `academic_activity` WHERE `secretary_status` IN (1, 2);

-- 院长已审批
INSERT INTO `academic_approval_record` (`submission_id`, `approver_id`, `approver_type`, `approval_action`, `approval_comment`, `approval_time`, `create_time`)
SELECT `id`, NULL, 1,
  CASE WHEN `dean_status` = 1 THEN 2 WHEN `dean_status` = 2 THEN 3 ELSE 2 END,
  `dean_comment`,
  COALESCE(`update_time`, NOW()),
  COALESCE(`create_time`, NOW())
FROM `academic_activity` WHERE `dean_status` IN (1, 2);

-- 3.2 从 academic_achievement 生成审批记录（同上逻辑）
INSERT INTO `academic_approval_record` (`submission_id`, `approver_id`, `approver_type`, `approval_action`, `approval_comment`, `approval_time`, `create_time`)
SELECT `id`, `student_id`, 1, 1, NULL,
  COALESCE(`submit_time`, `create_time`, NOW()),
  COALESCE(`create_time`, NOW())
FROM `academic_achievement` WHERE `submit_time` IS NOT NULL;

INSERT INTO `academic_approval_record` (`submission_id`, `approver_id`, `approver_type`, `approval_action`, `approval_comment`, `approval_time`, `create_time`)
SELECT `id`, NULL, 2,
  CASE WHEN `mentor_status` = 1 THEN 2 WHEN `mentor_status` = 2 THEN 3 ELSE 2 END,
  `mentor_comment`,
  COALESCE(`update_time`, NOW()),
  COALESCE(`create_time`, NOW())
FROM `academic_achievement` WHERE `mentor_status` IN (1, 2);

INSERT INTO `academic_approval_record` (`submission_id`, `approver_id`, `approver_type`, `approval_action`, `approval_comment`, `approval_time`, `create_time`)
SELECT `id`, NULL, 5,
  CASE WHEN `secretary_status` = 1 THEN 2 WHEN `secretary_status` = 2 THEN 3 ELSE 2 END,
  `secretary_comment`,
  COALESCE(`update_time`, NOW()),
  COALESCE(`create_time`, NOW())
FROM `academic_achievement` WHERE `secretary_status` IN (1, 2);

INSERT INTO `academic_approval_record` (`submission_id`, `approver_id`, `approver_type`, `approval_action`, `approval_comment`, `approval_time`, `create_time`)
SELECT `id`, NULL, 1,
  CASE WHEN `dean_status` = 1 THEN 2 WHEN `dean_status` = 2 THEN 3 ELSE 2 END,
  `dean_comment`,
  COALESCE(`update_time`, NOW()),
  COALESCE(`create_time`, NOW())
FROM `academic_achievement` WHERE `dean_status` IN (1, 2);

-- 3.3 从 innovation_project 生成审批记录（同上逻辑）
INSERT INTO `academic_approval_record` (`submission_id`, `approver_id`, `approver_type`, `approval_action`, `approval_comment`, `approval_time`, `create_time`)
SELECT `id`, `student_id`, 1, 1, NULL,
  COALESCE(`submit_time`, `create_time`, NOW()),
  COALESCE(`create_time`, NOW())
FROM `innovation_project` WHERE `submit_time` IS NOT NULL;

INSERT INTO `academic_approval_record` (`submission_id`, `approver_id`, `approver_type`, `approval_action`, `approval_comment`, `approval_time`, `create_time`)
SELECT `id`, NULL, 2,
  CASE WHEN `mentor_status` = 1 THEN 2 WHEN `mentor_status` = 2 THEN 3 ELSE 2 END,
  `mentor_comment`,
  COALESCE(`update_time`, NOW()),
  COALESCE(`create_time`, NOW())
FROM `innovation_project` WHERE `mentor_status` IN (1, 2);

INSERT INTO `academic_approval_record` (`submission_id`, `approver_id`, `approver_type`, `approval_action`, `approval_comment`, `approval_time`, `create_time`)
SELECT `id`, NULL, 5,
  CASE WHEN `secretary_status` = 1 THEN 2 WHEN `secretary_status` = 2 THEN 3 ELSE 2 END,
  `secretary_comment`,
  COALESCE(`update_time`, NOW()),
  COALESCE(`create_time`, NOW())
FROM `innovation_project` WHERE `secretary_status` IN (1, 2);

INSERT INTO `academic_approval_record` (`submission_id`, `approver_id`, `approver_type`, `approval_action`, `approval_comment`, `approval_time`, `create_time`)
SELECT `id`, NULL, 1,
  CASE WHEN `dean_status` = 1 THEN 2 WHEN `dean_status` = 2 THEN 3 ELSE 2 END,
  `dean_comment`,
  COALESCE(`update_time`, NOW()),
  COALESCE(`create_time`, NOW())
FROM `innovation_project` WHERE `dean_status` IN (1, 2);


-- ============================================================
-- 第四部分：新增字典类型和字典数据
-- ============================================================

-- 4.1 新增字典类型
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (6, '提交人类型', 'academic_submitter_type', '0', 'admin', NOW(), '', NULL, '学术提交人类型')
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`);

INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (7, '学术内容类型', 'academic_content_type', '0', 'admin', NOW(), '', NULL, '学术内容大类')
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`);

INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (8, '审批状态', 'academic_approval_status', '0', 'admin', NOW(), '', NULL, '学术审批流程状态')
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`);

INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (9, '审批动作', 'academic_approval_action', '0', 'admin', NOW(), '', NULL, '审批操作类型')
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`);

INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (10, '学术活动类型', 'academic_activity_type', '0', 'admin', NOW(), '', NULL, '学术活动细分类型')
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`);

INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (11, '学术成果类型', 'academic_achievement_type', '0', 'admin', NOW(), '', NULL, '学术成果细分类型')
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`);

INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (12, '创新项目类型', 'academic_project_type', '0', 'admin', NOW(), '', NULL, '创新项目细分类型')
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`);

-- 4.2 新增字典数据
-- 提交人类型
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (30, 1, '学生', '1', 'academic_submitter_type', NULL, 'primary', 'Y', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (31, 2, '导师', '2', 'academic_submitter_type', NULL, 'success', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (32, 3, '管理员', '3', 'academic_submitter_type', NULL, 'info', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

-- 学术内容大类
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (33, 1, '学术活动', '1', 'academic_content_type', NULL, 'primary', 'Y', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (34, 2, '学术成果', '2', 'academic_content_type', NULL, 'success', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (35, 3, '创新创业', '3', 'academic_content_type', NULL, 'warning', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

-- 审批状态
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (36, 1, '待提交', '0', 'academic_approval_status', NULL, 'info', 'Y', '0', 'admin', NOW(), '', NULL, '草稿状态')
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (37, 2, '待导师审批', '1', 'academic_approval_status', NULL, 'warning', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (38, 3, '待秘书审批', '2', 'academic_approval_status', NULL, 'warning', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (39, 4, '待院长审批', '3', 'academic_approval_status', NULL, 'warning', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (40, 5, '已通过', '4', 'academic_approval_status', NULL, 'success', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (41, 6, '已驳回', '5', 'academic_approval_status', NULL, 'danger', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

-- 审批动作
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (42, 1, '提交', '1', 'academic_approval_action', NULL, 'primary', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (43, 2, '通过', '2', 'academic_approval_action', NULL, 'success', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (44, 3, '驳回', '3', 'academic_approval_action', NULL, 'danger', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (45, 4, '撤回', '4', 'academic_approval_action', NULL, 'info', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

-- 学术活动类型
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (46, 1, '学术讲座', '1', 'academic_activity_type', NULL, 'primary', 'Y', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (47, 2, '研讨会', '2', 'academic_activity_type', NULL, 'success', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (48, 3, '论坛', '3', 'academic_activity_type', NULL, 'warning', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (49, 4, '其他', '4', 'academic_activity_type', NULL, 'info', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

-- 学术成果类型
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (50, 1, '期刊论文', '1', 'academic_achievement_type', NULL, 'primary', 'Y', '0', 'admin', NOW(), '', NULL, '{"need_doi":true}')
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (51, 2, '专利', '2', 'academic_achievement_type', NULL, 'warning', 'N', '0', 'admin', NOW(), '', NULL, '{"need_patent_no":true}')
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (52, 3, '科研奖励', '3', 'academic_achievement_type', NULL, 'success', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (53, 4, '项目参与', '4', 'academic_achievement_type', NULL, 'info', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

-- 创新项目类型
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (54, 1, '创新项目', '1', 'academic_project_type', NULL, 'primary', 'Y', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (55, 2, '创业项目', '2', 'academic_project_type', NULL, 'success', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (56, 3, '竞赛', '3', 'academic_project_type', NULL, 'warning', 'N', '0', 'admin', NOW(), '', NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);


-- ============================================================
-- 第五部分：旧表备份（重命名为 _backup，不删除）
-- ============================================================

RENAME TABLE `academic_activity` TO `academic_activity_backup`;
RENAME TABLE `academic_achievement` TO `academic_achievement_backup`;
RENAME TABLE `innovation_project` TO `innovation_project_backup`;
RENAME TABLE `innovation_practice` TO `innovation_practice_backup`;
RENAME TABLE `approval_audit_log` TO `approval_audit_log_backup`;


-- ============================================================
-- 第六部分：验证数据迁移结果
-- ============================================================

-- 验证主表数据量
SELECT 'academic_submission' AS table_name, COUNT(*) AS row_count FROM `academic_submission`
UNION ALL
SELECT 'academic_activity_detail', COUNT(*) FROM `academic_activity_detail`
UNION ALL
SELECT 'academic_achievement_detail', COUNT(*) FROM `academic_achievement_detail`
UNION ALL
SELECT 'academic_innovation_detail', COUNT(*) FROM `academic_innovation_detail`
UNION ALL
SELECT 'academic_approval_record', COUNT(*) FROM `academic_approval_record`;

-- 验证数据迁移一致性
SELECT 'activity_count_match' AS check_item,
  (SELECT COUNT(*) FROM `academic_activity_backup`) AS old_count,
  (SELECT COUNT(*) FROM `academic_submission` WHERE content_type = 1) AS new_count
UNION ALL
SELECT 'achievement_count_match',
  (SELECT COUNT(*) FROM `academic_achievement_backup`),
  (SELECT COUNT(*) FROM `academic_submission` WHERE content_type = 2)
UNION ALL
SELECT 'innovation_count_match',
  (SELECT COUNT(*) FROM `innovation_project_backup`),
  (SELECT COUNT(*) FROM `academic_submission` WHERE content_type = 3);

-- 验证审批状态分布
SELECT approval_status,
  CASE approval_status
    WHEN 0 THEN '待提交'
    WHEN 1 THEN '待导师审批'
    WHEN 2 THEN '待秘书审批'
    WHEN 3 THEN '待院长审批'
    WHEN 4 THEN '已通过'
    WHEN 5 THEN '已驳回'
  END AS status_name,
  COUNT(*) AS count
FROM `academic_submission`
GROUP BY approval_status
ORDER BY approval_status;
