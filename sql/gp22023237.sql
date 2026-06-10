/*
 研究生管理信息系统 - 数据库初始化脚本
 Graduate Student Management Information System - Database Schema

 使用说明:
 1. 创建数据库: CREATE DATABASE gp22023237 CHARACTER SET utf8mb4;
 2. 导入本脚本: mysql -u root -p gp22023237 < gp22023237.sql
 3. 默认测试账号: admin / admin123 (超级管理员)
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for academic_achievement_backup
-- ----------------------------
DROP TABLE IF EXISTS `academic_achievement_backup`;
CREATE TABLE `academic_achievement_backup`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `achievement_type` int NULL DEFAULT 1,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `authors` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `publication_date` datetime NULL DEFAULT NULL,
  `journal_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `journal_level` int NULL DEFAULT NULL,
  `volume` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `issue` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `pages` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `doi` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `patent_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `patent_type` int NULL DEFAULT NULL,
  `patent_status` int NULL DEFAULT NULL,
  `award_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `award_level` int NULL DEFAULT NULL,
  `award_issuer` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `project_role` int NULL DEFAULT NULL,
  `abstract_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `mentor_status` int NULL DEFAULT 0,
  `mentor_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `secretary_status` int NULL DEFAULT 0,
  `secretary_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `dean_status` int NULL DEFAULT 0,
  `dean_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `attachment_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `submit_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_achievement_type`(`achievement_type` ASC) USING BTREE,
  INDEX `idx_submit_time`(`submit_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of academic_achievement_backup
-- ----------------------------
DROP TABLE IF EXISTS `academic_achievement_detail`;
CREATE TABLE `academic_achievement_detail`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `submission_id` bigint NOT NULL COMMENT '关联提交主表ID',
  `achievement_type` tinyint NOT NULL DEFAULT 1 COMMENT '成果类型：1=论文，2=专利，3=科研奖励，4=项目参与',
  `authors` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '作者/参与者',
  `publication_date` datetime NULL DEFAULT NULL COMMENT '发表/授权时间',
  `journal_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '论文期刊名称',
  `journal_level` tinyint NULL DEFAULT NULL COMMENT '期刊级别：1=SCI/EI，2=核心期刊，3=普通期刊',
  `volume` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '卷号',
  `issue` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '期号',
  `pages` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '页码',
  `doi` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'DOI号',
  `patent_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '专利号/软著登记号',
  `patent_type` tinyint NULL DEFAULT NULL COMMENT '专利类型：1=发明，2=实用新型，3=外观设计',
  `patent_status` tinyint NULL DEFAULT NULL COMMENT '授权状态：0=申请中，1=已授权',
  `award_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '奖励名称',
  `award_level` tinyint NULL DEFAULT NULL COMMENT '奖励级别：1=国家级，2=省级，3=市级，4=校级',
  `award_issuer` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发奖单位',
  `project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目名称',
  `project_role` tinyint NULL DEFAULT NULL COMMENT '项目角色：1=负责人，2=核心成员，3=参与者',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_submission_id`(`submission_id` ASC) USING BTREE,
  INDEX `idx_achievement_type`(`achievement_type` ASC) USING BTREE,
  INDEX `idx_doi`(`doi` ASC) USING BTREE,
  INDEX `idx_patent_no`(`patent_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学术成果详情子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of academic_achievement_detail
-- ----------------------------
DROP TABLE IF EXISTS `academic_activity_backup`;
CREATE TABLE `academic_activity_backup`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `activity_type` int NULL DEFAULT 1 COMMENT '活动类型：1-学术讲座，2-研讨会，3-论坛，4-其他',
  `activity_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '活动名称',
  `activity_time` datetime NULL DEFAULT NULL COMMENT '活动时间',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '活动地点',
  `speaker` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主讲人/主持人',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '活动内容描述',
  `mentor_status` int NULL DEFAULT 0 COMMENT '导师审批状态：0-未审批，1-同意，2-拒绝',
  `mentor_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '导师审批意见',
  `secretary_status` int NULL DEFAULT 0 COMMENT '教学秘书审批状态：0-未审批，1-同意，2-拒绝',
  `secretary_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '教学秘书审批意见',
  `dean_status` int NULL DEFAULT 0 COMMENT '分管院长审批状态：0-未审批，1-同意，2-拒绝',
  `dean_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '分管院长审批意见',
  `attachment_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件路径（证明材料）',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_activity_type`(`activity_type` ASC) USING BTREE,
  INDEX `idx_submit_time`(`submit_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学术活动记录表（讲座、研讨会、论坛等）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of academic_activity_backup
-- ----------------------------

-- ----------------------------
-- Table structure for academic_activity_detail
-- ----------------------------
DROP TABLE IF EXISTS `academic_activity_detail`;
CREATE TABLE `academic_activity_detail`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `submission_id` bigint NOT NULL COMMENT '关联提交主表ID',
  `activity_type` tinyint NOT NULL DEFAULT 1 COMMENT '活动类型：1=学术讲座，2=研讨会，3=论坛，4=其他',
  `activity_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动名称',
  `activity_time` datetime NULL DEFAULT NULL COMMENT '活动时间',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '活动地点',
  `speaker` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主讲人/主持人',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '活动内容描述',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_submission_id`(`submission_id` ASC) USING BTREE,
  INDEX `idx_activity_type`(`activity_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学术活动详情子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of academic_activity_detail
-- ----------------------------
DROP TABLE IF EXISTS `academic_approval_record`;
CREATE TABLE `academic_approval_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `submission_id` bigint NOT NULL COMMENT '关联提交主表ID',
  `approver_id` bigint NOT NULL COMMENT '审批人ID（关联teacher表或user表）',
  `approver_type` tinyint NOT NULL COMMENT '审批人类型：2=导师(对应角色7)，5=教学秘书(对应角色5)，1=分管院长(对应角色2)',
  `approval_action` tinyint NOT NULL COMMENT '审批动作：1=提交，2=通过，3=驳回，4=撤回',
  `approval_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '审批意见',
  `reviewer_file_urls` json NULL COMMENT '审批人附件URL列表（JSON数组）',
  `approval_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_submission_id`(`submission_id` ASC) USING BTREE,
  INDEX `idx_approver`(`approver_id` ASC, `approver_type` ASC) USING BTREE,
  INDEX `idx_approval_time`(`approval_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学术审批流程记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of academic_approval_record
-- ----------------------------
DROP TABLE IF EXISTS `academic_innovation_detail`;
CREATE TABLE `academic_innovation_detail`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `submission_id` bigint NOT NULL COMMENT '关联提交主表ID',
  `project_type` tinyint NOT NULL DEFAULT 1 COMMENT '项目类型：1=创新项目，2=创业项目，3=竞赛',
  `project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目名称',
  `project_level` tinyint NULL DEFAULT NULL COMMENT '项目级别：1=国家级，2=省级，3=市级，4=校级',
  `project_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目编号',
  `leader` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负责人',
  `members` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '参与成员',
  `advisor` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '指导老师',
  `start_date` datetime NULL DEFAULT NULL COMMENT '项目开始时间',
  `end_date` datetime NULL DEFAULT NULL COMMENT '项目结束时间',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '项目描述',
  `achievements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '项目成果/获奖情况',
  `award_level` tinyint NULL DEFAULT NULL COMMENT '获奖等级：1=特等奖，2=一等奖，3=二等奖，4=三等奖，5=优秀奖',
  `funding_amount` decimal(12, 2) NULL DEFAULT NULL COMMENT '资助金额（元）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_submission_id`(`submission_id` ASC) USING BTREE,
  INDEX `idx_project_type`(`project_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '创新创业项目详情子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of academic_innovation_detail
-- ----------------------------
DROP TABLE IF EXISTS `academic_submission`;
CREATE TABLE `academic_submission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '归属学生ID（关联student表）',
  `submitter_id` bigint NOT NULL COMMENT '提交人ID（学生=student.id，导师=teacher.id，管理员=user.id）',
  `submitter_type` tinyint NOT NULL COMMENT '提交人类型：1=学生，2=导师，3=管理员',
  `content_type` tinyint NOT NULL COMMENT '内容类型：1=学术活动，2=学术成果，3=创新创业',
  `title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容标题',
  `abstract_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '内容摘要/说明',
  `file_urls` json NULL COMMENT '附件文件URL列表（JSON数组，如[\"/file1.pdf\",\"/file2.pdf\"]）',
  `approval_status` tinyint NOT NULL DEFAULT 0 COMMENT '审批状态：0=待提交(草稿)，1=待导师审批，2=待秘书审批，3=待院长审批，4=已通过，5=已驳回',
  `current_approver_id` bigint NULL DEFAULT NULL COMMENT '当前审批人ID',
  `current_approver_type` tinyint NULL DEFAULT NULL COMMENT '当前审批人类型：2=导师(角色7)，5=教学秘书(角色5)，1=分管院长(角色2)',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除：0=未删除，1=已删除',
  `version` int NOT NULL DEFAULT 1 COMMENT '乐观锁版本号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_submitter`(`submitter_id` ASC, `submitter_type` ASC) USING BTREE,
  INDEX `idx_content_type`(`content_type` ASC) USING BTREE,
  INDEX `idx_approval_status`(`approval_status` ASC) USING BTREE,
  INDEX `idx_current_approver`(`current_approver_id` ASC, `current_approver_type` ASC) USING BTREE,
  INDEX `idx_submit_time`(`submit_time` ASC) USING BTREE,
  INDEX `idx_deleted`(`is_deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学术内容提交主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of academic_submission
-- ----------------------------
DROP TABLE IF EXISTS `approval_audit_log_backup`;
CREATE TABLE `approval_audit_log_backup`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型：THESIS_PROGRESS, DEGREE_APPLICATION, THESIS_DEFENSE, EXTERNAL_REVIEW',
  `business_id` bigint NOT NULL COMMENT '业务记录ID',
  `student_id` bigint NULL DEFAULT NULL COMMENT '学生ID',
  `student_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学号',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学生姓名',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人姓名',
  `operator_role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人角色',
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型：SUBMIT, APPROVE, REJECT, GRANT_DEGREE',
  `old_status` tinyint NULL DEFAULT NULL COMMENT '变更前状态',
  `new_status` tinyint NULL DEFAULT NULL COMMENT '变更后状态',
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批意见',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_business`(`business_type` ASC, `business_id` ASC) USING BTREE,
  INDEX `idx_student`(`student_id` ASC) USING BTREE,
  INDEX `idx_operator`(`operator_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批审计日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of approval_audit_log_backup
-- ----------------------------

-- ----------------------------
-- Table structure for backup_menu_selection_cleanup
-- ----------------------------
DROP TABLE IF EXISTS `backup_menu_selection_cleanup`;
CREATE TABLE `backup_menu_selection_cleanup`  (
  `id` int NOT NULL DEFAULT 0 COMMENT '主键',
  `menus_index` int NOT NULL COMMENT '菜单索引',
  `title` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '菜单标题',
  `icon` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '菜单图标',
  `path` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '菜单路径',
  `parent_id` int NULL DEFAULT NULL COMMENT '父级Id'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of backup_menu_selection_cleanup
-- ----------------------------

-- ----------------------------
-- Table structure for backup_role_menu_selection_cleanup
-- ----------------------------
DROP TABLE IF EXISTS `backup_role_menu_selection_cleanup`;
CREATE TABLE `backup_role_menu_selection_cleanup`  (
  `id` int NOT NULL DEFAULT 0 COMMENT '主键',
  `role_id` int NOT NULL COMMENT '角色编号',
  `menu_id` int NOT NULL COMMENT '菜单编号'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of backup_role_menu_selection_cleanup
-- ----------------------------

-- ----------------------------
-- Table structure for class
-- ----------------------------
DROP TABLE IF EXISTS `class`;
CREATE TABLE `class`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `class_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '班级名称',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '院系',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '专业',
  `admission_year` year NULL DEFAULT NULL COMMENT '入学年份',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2008 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '班级表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of class
-- ----------------------------
DROP TABLE IF EXISTS `counselor`;
CREATE TABLE `counselor`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '辅导员ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `counselor_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '辅导员编号',
  `responsible_grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负责年级',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_counselor_no`(`counselor_no` ASC) USING BTREE,
  INDEX `fk_counselor_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_counselor_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '辅导员信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of counselor
-- ----------------------------

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `course_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程名称',
  `credit` decimal(3, 1) NOT NULL COMMENT '学分',
  `hours` int NOT NULL COMMENT '学时',
  `semester` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学期',
  `year` year NOT NULL COMMENT '学年',
  `max_credits` decimal(3, 1) NULL DEFAULT NULL COMMENT '最大学分限制',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-未开课，1-已开课，2-已结课',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '课程描述',
  `study_nature` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修读性质',
  `textbook` tinyint NULL DEFAULT 0 COMMENT '教材：1-是，0-否',
  `external_selection` tinyint NULL DEFAULT 0 COMMENT '外年级/专业选课：1-是，0-否',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_course_no`(`course_no` ASC) USING BTREE,
  INDEX `idx_course_status`(`status` ASC) USING BTREE,
  INDEX `idx_course_semester`(`semester` ASC, `year` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2035 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course
-- ----------------------------
DROP TABLE IF EXISTS `course_selection`;
CREATE TABLE `course_selection`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '选课记录ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `selection_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间，不为空表示已提交',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-已退课',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_course`(`student_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `fk_selection_student`(`student_id` ASC) USING BTREE,
  INDEX `fk_selection_course`(`course_id` ASC) USING BTREE,
  INDEX `idx_student_submit`(`student_id` ASC, `submit_time` ASC) USING BTREE,
  CONSTRAINT `fk_selection_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_selection_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6546 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '选课记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course_selection
-- ----------------------------
DROP TABLE IF EXISTS `degree_application`;
CREATE TABLE `degree_application`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学号',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学生姓名',
  `mentor_id` bigint NOT NULL COMMENT '导师ID',
  `mentor_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '导师姓名',
  `degree_type` tinyint NULL DEFAULT 1 COMMENT '学位类型：1-硕士，2-博士',
  `thesis_title` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '论文题目',
  `thesis_attachment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '论文终稿路径',
  `defense_time` datetime NULL DEFAULT NULL COMMENT '答辩时间',
  `defense_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '答辩地点',
  `committee_chair` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '答辩委员会主席',
  `committee_members` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '答辩委员会成员',
  `defense_result` tinyint NULL DEFAULT 0 COMMENT '答辩结果',
  `defense_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '答辩评分',
  `defense_committee_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '答辩委员会评语',
  `qa_record` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '问答记录',
  `committee_status` tinyint NULL DEFAULT 0 COMMENT '分委员会审批状态',
  `committee_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分委员会审批意见',
  `committee_approver_id` bigint NULL DEFAULT NULL COMMENT '学位分委员会审批人ID',
  `committee_approve_time` datetime NULL DEFAULT NULL COMMENT '学位分委员会审批时间',
  `degree_granted` tinyint NULL DEFAULT 0 COMMENT '学位授予状态',
  `degree_grant_date` date NULL DEFAULT NULL COMMENT '学位授予日期',
  `certificate_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学位证书编号',
  `attachment_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请材料路径',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NULL DEFAULT 1 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_student_application`(`student_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_certificate_no`(`certificate_no` ASC) USING BTREE,
  INDEX `mentor_id`(`mentor_id` ASC) USING BTREE,
  INDEX `idx_degree_granted`(`degree_granted` ASC) USING BTREE,
  INDEX `idx_committee_status`(`committee_status` ASC) USING BTREE,
  CONSTRAINT `degree_application_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `degree_application_ibfk_2` FOREIGN KEY (`mentor_id`) REFERENCES `teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学位申请表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of degree_application
-- ----------------------------
DROP TABLE IF EXISTS `degree_application_backup`;
CREATE TABLE `degree_application_backup`  (
  `id` bigint NOT NULL DEFAULT 0 COMMENT '主键ID',
  `student_id` bigint NULL DEFAULT NULL COMMENT '学生ID',
  `student_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学生姓名',
  `student_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学号',
  `mentor_id` bigint NULL DEFAULT NULL COMMENT '导师ID',
  `mentor_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '导师姓名',
  `degree_type` tinyint NULL DEFAULT 1 COMMENT '申请学位类型：1-硕士学位，2-博士学位',
  `thesis_title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '论文题目',
  `thesis_attachment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '论文最终稿附件',
  `defense_time` datetime NULL DEFAULT NULL COMMENT '答辩时间',
  `defense_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '答辩地点',
  `committee_chair` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '答辩委员会主席',
  `committee_members` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '答辩委员会成员',
  `defense_result` tinyint NULL DEFAULT 0 COMMENT '答辩结果：0-未答辩，1-通过，2-不通过',
  `defense_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '答辩评分（百分制）',
  `defense_committee_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '答辩委员会评语',
  `qa_record` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '问答记录',
  `committee_status` tinyint NULL DEFAULT 0 COMMENT '学位分委员会审批状态：0-未审批，1-同意，2-拒绝',
  `committee_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学位分委员会审批意见',
  `degree_granted` tinyint NULL DEFAULT 0 COMMENT '学位授予状态：0-未授予，1-已授予',
  `degree_grant_date` date NULL DEFAULT NULL COMMENT '学位授予日期',
  `certificate_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学位证书编号',
  `attachment_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件路径（申请材料）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of degree_application_backup
-- ----------------------------

-- ----------------------------
-- Table structure for electronic_record
-- ----------------------------
DROP TABLE IF EXISTS `electronic_record`;
CREATE TABLE `electronic_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '档案ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `type` tinyint NOT NULL COMMENT '档案类型：1-基本信息，2-学习经历，3-奖惩情况',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '档案内容',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件URL',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_record_student`(`student_id` ASC) USING BTREE,
  CONSTRAINT `fk_record_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '电子档案表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of electronic_record
-- ----------------------------

-- ----------------------------
-- Table structure for graduation_qualification
-- ----------------------------
DROP TABLE IF EXISTS `graduation_qualification`;
CREATE TABLE `graduation_qualification`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '审核ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `credit_check` tinyint NULL DEFAULT 0 COMMENT '学分审核：0-未审核，1-通过，2-未通过',
  `thesis_check` tinyint NULL DEFAULT 0 COMMENT '论文审核：0-未审核，1-通过，2-未通过',
  `practice_check` tinyint NULL DEFAULT 0 COMMENT '实践审核：0-未审核，1-通过，2-未通过',
  `overall_result` tinyint NULL DEFAULT 0 COMMENT '总结果：0-未审核，1-通过，2-未通过',
  `reviewer_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核意见',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NULL DEFAULT 1 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student`(`student_id` ASC) USING BTREE,
  INDEX `fk_qualification_student`(`student_id` ASC) USING BTREE,
  INDEX `fk_qualification_reviewer`(`reviewer_id` ASC) USING BTREE,
  CONSTRAINT `fk_qualification_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_qualification_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '毕业资格审核表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of graduation_qualification
-- ----------------------------
DROP TABLE IF EXISTS `innovation_practice_backup`;
CREATE TABLE `innovation_practice_backup`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '实践ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `type` tinyint NOT NULL COMMENT '实践类型：1-创新创业项目，2-创业比赛，3-竞赛',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目/比赛标题',
  `organizer` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主办方',
  `time` datetime NOT NULL COMMENT '项目/比赛时间',
  `award` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '获奖情况',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '项目/比赛描述',
  `proof_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '证明材料URL',
  `tutor_approval` tinyint NULL DEFAULT 0 COMMENT '导师审核：0-未审核，1-通过，2-未通过',
  `tutor_id` bigint NULL DEFAULT NULL COMMENT '审核导师ID',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_innovation_student`(`student_id` ASC) USING BTREE,
  INDEX `fk_innovation_tutor`(`tutor_id` ASC) USING BTREE,
  CONSTRAINT `fk_innovation_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_innovation_tutor` FOREIGN KEY (`tutor_id`) REFERENCES `teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '创新创业实践表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of innovation_practice_backup
-- ----------------------------

-- ----------------------------
-- Table structure for innovation_project_backup
-- ----------------------------
DROP TABLE IF EXISTS `innovation_project_backup`;
CREATE TABLE `innovation_project_backup`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `project_type` int NULL DEFAULT 1 COMMENT '项目类型：1-创新项目，2-创业项目，3-竞赛',
  `project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目名称',
  `project_level` int NULL DEFAULT 1 COMMENT '项目级别：1-国家级，2-省级，3-市级，4-校级',
  `project_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目编号',
  `leader` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负责人',
  `members` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '参与成员',
  `advisor` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '指导老师',
  `start_date` datetime NULL DEFAULT NULL COMMENT '项目开始时间',
  `end_date` datetime NULL DEFAULT NULL COMMENT '项目结束时间',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '项目描述',
  `achievements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '项目成果/获奖情况',
  `award_level` int NULL DEFAULT NULL COMMENT '获奖等级：1-特等奖，2-一等奖，3-二等奖，4-三等奖，5-优秀奖',
  `funding_amount` decimal(12, 2) NULL DEFAULT NULL COMMENT '资助金额（元）',
  `mentor_status` int NULL DEFAULT 0 COMMENT '导师审批状态：0-未审批，1-同意，2-拒绝',
  `mentor_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '导师审批意见',
  `secretary_status` int NULL DEFAULT 0 COMMENT '教学秘书审批状态：0-未审批，1-同意，2-拒绝',
  `secretary_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '教学秘书审批意见',
  `dean_status` int NULL DEFAULT 0 COMMENT '分管院长审批状态：0-未审批，1-同意，2-拒绝',
  `dean_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '分管院长审批意见',
  `attachment_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件路径',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_project_type`(`project_type` ASC) USING BTREE,
  INDEX `idx_submit_time`(`submit_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '创新创业项目表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of innovation_project_backup
-- ----------------------------

-- ----------------------------
-- Table structure for mentor_change_application
-- ----------------------------
DROP TABLE IF EXISTS `mentor_change_application`;
CREATE TABLE `mentor_change_application`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `original_mentor_id` bigint NOT NULL COMMENT '原导师ID',
  `new_mentor_id` bigint NOT NULL COMMENT '新导师ID',
  `change_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '更换原因',
  `original_mentor_status` int NULL DEFAULT 0 COMMENT '原导师审批状态：0-待审批，1-已通过，2-已拒绝',
  `original_mentor_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原导师审批意见',
  `original_mentor_time` datetime NULL DEFAULT NULL COMMENT '原导师审批时间',
  `new_mentor_status` int NULL DEFAULT 0 COMMENT '新导师审批状态：0-待审批，1-已通过，2-已拒绝',
  `new_mentor_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '新导师审批意见',
  `new_mentor_time` datetime NULL DEFAULT NULL COMMENT '新导师审批时间',
  `overall_status` int NULL DEFAULT 0 COMMENT '整体状态：0-待审批(待原导师)，1-待新导师审批，2-已通过，3-已拒绝',
  `apply_time` datetime NULL DEFAULT NULL COMMENT '申请时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_original_mentor_id`(`original_mentor_id` ASC) USING BTREE,
  INDEX `idx_new_mentor_id`(`new_mentor_id` ASC) USING BTREE,
  INDEX `idx_overall_status`(`overall_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '导师更换申请表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mentor_change_application
-- ----------------------------

-- ----------------------------
-- Table structure for mentor_student
-- ----------------------------
DROP TABLE IF EXISTS `mentor_student`;
CREATE TABLE `mentor_student`  (
  `id` bigint NOT NULL,
  `mentor_id` bigint NOT NULL COMMENT '导师的id',
  `student_id` bigint NOT NULL COMMENT '学生的id',
  `mentor_type` tinyint NULL DEFAULT 0 COMMENT '导师类型（0-未定 1-第一导师，2-合作导师）',
  `round` tinyint NOT NULL COMMENT '轮次：1-第一轮，2-第二轮',
  `student_choice_order` int NULL DEFAULT NULL COMMENT '学生选择顺序（志愿）',
  `student_status` tinyint NULL DEFAULT 1 COMMENT '学生选择状态：1-已提交，0-已取消',
  `teacher_status` tinyint NULL DEFAULT 0 COMMENT '导师选择状态：0-未处理，1-同意，2-拒绝',
  `selection_time` datetime NULL DEFAULT NULL COMMENT '学生选择时间',
  `confirm_time` datetime NULL DEFAULT NULL COMMENT '导师确认时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `mentor_student_ibfk_1`(`mentor_id` ASC) USING BTREE,
  INDEX `mentor_student_ibfk_2`(`student_id` ASC) USING BTREE,
  CONSTRAINT `mentor_student_ibfk_1` FOREIGN KEY (`mentor_id`) REFERENCES `teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `mentor_student_ibfk_2` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '双选关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mentor_student
-- ----------------------------
DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `menus_index` int NOT NULL COMMENT '菜单索引',
  `title` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '菜单标题',
  `icon` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '菜单图标',
  `path` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '菜单路径',
  `parent_id` int NULL DEFAULT NULL COMMENT '父级Id',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`, `menus_index`) USING BTREE,
  UNIQUE INDEX `menus_index`(`menus_index` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 190 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '菜单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of menu
-- ----------------------------
INSERT INTO `menu` VALUES (95, 1, '双选管理', 'tree', '/selection', 0, 1);
INSERT INTO `menu` VALUES (96, 2, '学籍管理', 'user', '/student', 0, 2);
INSERT INTO `menu` VALUES (97, 3, '课程管理', 'education', '/course', 0, 3);
INSERT INTO `menu` VALUES (98, 4, '学术管理', 'star', '/academic', 0, 4);
INSERT INTO `menu` VALUES (99, 5, '论文管理', 'edit', '/thesis', 0, 5);
INSERT INTO `menu` VALUES (100, 6, '系统管理', 'system', '/system', 0, 7);
INSERT INTO `menu` VALUES (101, 101, '学生选导师', 'tree', '/student-choose', 1, 1);
INSERT INTO `menu` VALUES (102, 102, '双选结果查询', 'tree', '/results', 1, 2);
INSERT INTO `menu` VALUES (103, 103, '导师选学生', 'tree', '/mentor-choose', 1, 3);
INSERT INTO `menu` VALUES (104, 104, '确认学生', 'tree', '/confirm', 1, 4);
INSERT INTO `menu` VALUES (105, 201, '学生信息维护', 'user', '/info', 2, 1);
INSERT INTO `menu` VALUES (107, 203, '学籍异动审核', 'user', '/approval', 2, 3);
INSERT INTO `menu` VALUES (108, 204, '毕业资格审核', 'user', '/graduation', 2, 4);
INSERT INTO `menu` VALUES (109, 301, '课程信息管理', 'education', '/info', 3, 1);
INSERT INTO `menu` VALUES (110, 302, '选课管理', 'education', '/selection', 3, 2);
INSERT INTO `menu` VALUES (111, 303, '成绩管理', 'education', '/score', 3, 3);
INSERT INTO `menu` VALUES (112, 304, '学生选课', 'education', '/student-select', 3, 4);
INSERT INTO `menu` VALUES (113, 305, '选课结果', 'education', '/select-result', 3, 5);
INSERT INTO `menu` VALUES (114, 306, '课程表查询', 'education', '/schedule', 3, 6);
INSERT INTO `menu` VALUES (118, 404, '学术内容审核', 'star', '/review', 4, 4);
INSERT INTO `menu` VALUES (124, 601, '角色管理', 'system', '/role', 6, 1);
INSERT INTO `menu` VALUES (125, 602, '菜单管理', 'system', '/menu', 6, 2);
INSERT INTO `menu` VALUES (126, 603, '用户管理', 'system', '/user', 6, 3);
INSERT INTO `menu` VALUES (127, 307, '学业成绩', 'education', '/student-score', 3, 7);
INSERT INTO `menu` VALUES (129, 105, '导师学生关系管理', 'tree', '/relationship', 1, 5);
INSERT INTO `menu` VALUES (131, 604, '权限管理', 'lock', '/permission', 6, 4);
INSERT INTO `menu` VALUES (132, 106, '导师更换申请管理', 'nested', '/mentor-change-manage', 1, 6);
INSERT INTO `menu` VALUES (133, 605, '系统配置管理', 'redis', '/config', 6, 5);
INSERT INTO `menu` VALUES (134, 107, '双选轮次管理', 'build', '/round-manage', 1, 7);
INSERT INTO `menu` VALUES (135, 205, '学籍异动申请', 'user', '/student-change', 2, 5);
INSERT INTO `menu` VALUES (137, 206, '学籍信息', 'user', '/student-profile', 2, 6);
INSERT INTO `menu` VALUES (137, 308, '课程阶段管理', 'build', '/phase-manage', 3, 6);
INSERT INTO `menu` VALUES (138, 309, '成绩录入', 'edit', '/teacher-score', 3, 9);
INSERT INTO `menu` VALUES (139, 310, '教学评价', 'education', '/evaluation', 3, 10);
INSERT INTO `menu` VALUES (140, 311, '排课管理', 'date', '/schedule-manage', 3, 1);
INSERT INTO `menu` VALUES (144, 606, '字典管理', 'dict', '/dict', 6, 6);
INSERT INTO `menu` VALUES (145, 607, '操作日志', 'log', '/oplog', 6, 7);
INSERT INTO `menu` VALUES (146, 401, '学术内容管理', 'star', '/submit', 4, 1);
INSERT INTO `menu` VALUES (147, 608, '通知公告', 'build', '/notice', 6, 8);
INSERT INTO `menu` VALUES (160, 501, '选题', 'edit', '/topic', 5, 1);
INSERT INTO `menu` VALUES (161, 502, '任务书', 'edit', '/task', 5, 2);
INSERT INTO `menu` VALUES (162, 503, '开题报告', 'edit', '/proposal', 5, 3);
INSERT INTO `menu` VALUES (163, 504, '中期检查', 'edit', '/midterm', 5, 4);
INSERT INTO `menu` VALUES (164, 505, '过程稿', 'edit', '/draft', 5, 5);
INSERT INTO `menu` VALUES (165, 506, '论文答辩稿', 'edit', '/defense-draft', 5, 6);
INSERT INTO `menu` VALUES (166, 507, '毕业论文', 'edit', '/thesis-final', 5, 7);
INSERT INTO `menu` VALUES (167, 510, '学生选题', 'edit', '/supervisor/topic', 5, 8);
INSERT INTO `menu` VALUES (168, 511, '选题修改申请', 'edit', '/supervisor/topic-modification', 5, 9);
INSERT INTO `menu` VALUES (169, 512, '任务书管理', 'edit', '/supervisor/task', 5, 10);
INSERT INTO `menu` VALUES (170, 513, '学生开题报告', 'edit', '/supervisor/proposal', 5, 11);
INSERT INTO `menu` VALUES (171, 514, '学生中期检查', 'edit', '/supervisor/midterm', 5, 12);
INSERT INTO `menu` VALUES (172, 515, '论文答辩稿管理', 'edit', '/supervisor/defense-draft', 5, 13);
INSERT INTO `menu` VALUES (173, 516, '论文最终稿管理', 'edit', '/supervisor/thesis-final', 5, 14);
INSERT INTO `menu` VALUES (174, 520, '进度查询', 'edit', '/progress', 5, 15);
INSERT INTO `menu` VALUES (176, 522, '流程配置', 'edit', '/process-config', 5, 17);
INSERT INTO `menu` VALUES (177, 523, '统计归档', 'edit', '/statistics', 5, 18);
INSERT INTO `menu` VALUES (178, 524, '成绩评定', 'edit', '/evaluation', 5, 19);
INSERT INTO `menu` VALUES (180, 7, '学位管理', 'date', '/degree', 0, 6);
INSERT INTO `menu` VALUES (181, 701, '毕业审核', 'finished', '/graduation-audit', 7, 1);
INSERT INTO `menu` VALUES (182, 702, '学位申请', 'form', '/degree-application', 7, 2);
INSERT INTO `menu` VALUES (183, 703, '答辩管理', 'date', '/defense-manage', 7, 3);
INSERT INTO `menu` VALUES (184, 704, '学位审批', 'approval', '/degree-approval', 7, 4);
INSERT INTO `menu` VALUES (185, 109, '导师名额管理', 'peoples', '/quota-manage', 1, 9);
INSERT INTO `menu` VALUES (187, 108, '导师更换申请', 'switch', '/mentor-change', 1, 8);
INSERT INTO `menu` VALUES (188, 189, '手动分配导师', 'user', '/manual-assign', 1, 10);
INSERT INTO `menu` VALUES (189, 190, '学位授予', 'education', '/degree-grant', 7, 5);

-- ----------------------------
-- Table structure for professional_practice
-- ----------------------------
DROP TABLE IF EXISTS `professional_practice`;
CREATE TABLE `professional_practice`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '实践ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `type` tinyint NOT NULL COMMENT '实践类型：1-企业实习，2-项目实训',
  `organization` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '实践单位',
  `position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '岗位',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '实践内容',
  `summary_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '实习总结报告URL',
  `tutor_approval` tinyint NULL DEFAULT 0 COMMENT '导师审核：0-未审核，1-通过，2-未通过',
  `tutor_id` bigint NULL DEFAULT NULL COMMENT '审核导师ID',
  `tutor_approval_time` datetime NULL DEFAULT NULL COMMENT '导师审核时间',
  `teacher_evaluation` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '教师评价',
  `teacher_id` bigint NULL DEFAULT NULL COMMENT '评价教师ID',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_practice_student`(`student_id` ASC) USING BTREE,
  INDEX `fk_practice_tutor`(`tutor_id` ASC) USING BTREE,
  INDEX `fk_practice_teacher`(`teacher_id` ASC) USING BTREE,
  CONSTRAINT `fk_practice_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_practice_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_practice_tutor` FOREIGN KEY (`tutor_id`) REFERENCES `teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '专业实践表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of professional_practice
-- ----------------------------

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_time` datetime(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `update_by` int NOT NULL COMMENT '更新人',
  `is_deleted` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0：显示；1：隐藏）',
  `name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '系统角色名',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, '2025-10-02 16:02:54.838232', '2025-10-02 16:02:54.838232', 1, 0, '超级管理员');
INSERT INTO `role` VALUES (2, '2025-10-02 15:39:34.000000', '2025-10-02 15:39:36.000000', 1, 0, '分管院长');
INSERT INTO `role` VALUES (3, '2025-10-02 15:39:58.000000', '2025-10-02 15:40:00.000000', 1, 0, '学院学位分委员会主席');
INSERT INTO `role` VALUES (4, '2025-10-02 15:40:11.000000', '2025-10-02 15:40:14.000000', 1, 0, '综合管理员');
INSERT INTO `role` VALUES (5, '2025-10-02 15:40:31.000000', '2025-10-02 15:40:33.000000', 1, 0, '教学秘书');
INSERT INTO `role` VALUES (6, '2025-10-02 16:02:46.529477', '2025-10-02 16:02:46.529477', 1, 0, '学生');
INSERT INTO `role` VALUES (7, '2025-10-02 16:02:43.543262', '2025-10-02 16:02:43.543262', 1, 0, '导师');
INSERT INTO `role` VALUES (8, '2025-10-02 16:16:21.957715', '2025-10-02 16:16:21.957715', 1, 0, '授课教师');

-- ----------------------------
-- Table structure for role_menu
-- ----------------------------
DROP TABLE IF EXISTS `role_menu`;
CREATE TABLE `role_menu`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` int NOT NULL COMMENT '角色编号',
  `menu_id` int NOT NULL COMMENT '菜单编号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `rolemenu_menu`(`menu_id` ASC) USING BTREE,
  INDEX `role_id`(`role_id` ASC) USING BTREE,
  CONSTRAINT `role_menu_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `role_menu_ibfk_2` FOREIGN KEY (`menu_id`) REFERENCES `menu` (`menus_index`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1558 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '角色菜单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role_menu
-- ----------------------------
INSERT INTO `role_menu` VALUES (349, 8, 301);
INSERT INTO `role_menu` VALUES (350, 8, 303);
INSERT INTO `role_menu` VALUES (363, 3, 105);
INSERT INTO `role_menu` VALUES (583, 4, 101);
INSERT INTO `role_menu` VALUES (584, 4, 102);
INSERT INTO `role_menu` VALUES (585, 4, 103);
INSERT INTO `role_menu` VALUES (586, 4, 104);
INSERT INTO `role_menu` VALUES (587, 4, 201);
INSERT INTO `role_menu` VALUES (589, 4, 203);
INSERT INTO `role_menu` VALUES (590, 4, 204);
INSERT INTO `role_menu` VALUES (591, 4, 301);
INSERT INTO `role_menu` VALUES (592, 4, 302);
INSERT INTO `role_menu` VALUES (593, 4, 303);
INSERT INTO `role_menu` VALUES (594, 4, 304);
INSERT INTO `role_menu` VALUES (595, 4, 305);
INSERT INTO `role_menu` VALUES (596, 4, 306);
INSERT INTO `role_menu` VALUES (600, 4, 404);
INSERT INTO `role_menu` VALUES (606, 4, 601);
INSERT INTO `role_menu` VALUES (607, 4, 602);
INSERT INTO `role_menu` VALUES (608, 4, 603);
INSERT INTO `role_menu` VALUES (609, 4, 307);
INSERT INTO `role_menu` VALUES (610, 4, 105);
INSERT INTO `role_menu` VALUES (611, 4, 107);
INSERT INTO `role_menu` VALUES (612, 4, 604);
INSERT INTO `role_menu` VALUES (613, 4, 605);
INSERT INTO `role_menu` VALUES (614, 4, 106);
INSERT INTO `role_menu` VALUES (871, 4, 308);
INSERT INTO `role_menu` VALUES (873, 3, 309);
INSERT INTO `role_menu` VALUES (875, 4, 309);
INSERT INTO `role_menu` VALUES (1004, 4, 311);
INSERT INTO `role_menu` VALUES (1010, 4, 311);
INSERT INTO `role_menu` VALUES (1111, 7, 103);
INSERT INTO `role_menu` VALUES (1112, 7, 104);
INSERT INTO `role_menu` VALUES (1113, 7, 303);
INSERT INTO `role_menu` VALUES (1114, 7, 404);
INSERT INTO `role_menu` VALUES (1115, 7, 106);
INSERT INTO `role_menu` VALUES (1116, 7, 203);
INSERT INTO `role_menu` VALUES (1121, 7, 309);
INSERT INTO `role_menu` VALUES (1122, 7, 401);
INSERT INTO `role_menu` VALUES (1265, 7, 510);
INSERT INTO `role_menu` VALUES (1266, 7, 511);
INSERT INTO `role_menu` VALUES (1267, 7, 512);
INSERT INTO `role_menu` VALUES (1268, 7, 513);
INSERT INTO `role_menu` VALUES (1269, 7, 514);
INSERT INTO `role_menu` VALUES (1270, 7, 515);
INSERT INTO `role_menu` VALUES (1271, 7, 516);
INSERT INTO `role_menu` VALUES (1282, 7, 524);
INSERT INTO `role_menu` VALUES (1357, 5, 105);
INSERT INTO `role_menu` VALUES (1358, 5, 201);
INSERT INTO `role_menu` VALUES (1360, 5, 203);
INSERT INTO `role_menu` VALUES (1361, 5, 204);
INSERT INTO `role_menu` VALUES (1362, 5, 301);
INSERT INTO `role_menu` VALUES (1363, 5, 302);
INSERT INTO `role_menu` VALUES (1364, 5, 303);
INSERT INTO `role_menu` VALUES (1365, 5, 404);
INSERT INTO `role_menu` VALUES (1366, 5, 107);
INSERT INTO `role_menu` VALUES (1367, 5, 106);
INSERT INTO `role_menu` VALUES (1368, 5, 308);
INSERT INTO `role_menu` VALUES (1369, 5, 309);
INSERT INTO `role_menu` VALUES (1370, 5, 311);
INSERT INTO `role_menu` VALUES (1371, 5, 311);
INSERT INTO `role_menu` VALUES (1372, 5, 520);
INSERT INTO `role_menu` VALUES (1373, 5, 522);
INSERT INTO `role_menu` VALUES (1374, 5, 523);
INSERT INTO `role_menu` VALUES (1375, 5, 512);
INSERT INTO `role_menu` VALUES (1376, 5, 516);
INSERT INTO `role_menu` VALUES (1377, 5, 515);
INSERT INTO `role_menu` VALUES (1378, 5, 514);
INSERT INTO `role_menu` VALUES (1379, 5, 513);
INSERT INTO `role_menu` VALUES (1380, 5, 510);
INSERT INTO `role_menu` VALUES (1381, 5, 511);
INSERT INTO `role_menu` VALUES (1386, 4, 7);
INSERT INTO `role_menu` VALUES (1387, 4, 701);
INSERT INTO `role_menu` VALUES (1388, 5, 7);
INSERT INTO `role_menu` VALUES (1389, 5, 701);
INSERT INTO `role_menu` VALUES (1390, 3, 7);
INSERT INTO `role_menu` VALUES (1391, 3, 701);
INSERT INTO `role_menu` VALUES (1397, 3, 703);
INSERT INTO `role_menu` VALUES (1398, 3, 704);
INSERT INTO `role_menu` VALUES (1399, 4, 702);
INSERT INTO `role_menu` VALUES (1400, 4, 703);
INSERT INTO `role_menu` VALUES (1401, 4, 704);
INSERT INTO `role_menu` VALUES (1402, 5, 703);
INSERT INTO `role_menu` VALUES (1403, 5, 704);
INSERT INTO `role_menu` VALUES (1405, 7, 703);
INSERT INTO `role_menu` VALUES (1406, 8, 703);
INSERT INTO `role_menu` VALUES (1408, 4, 109);
INSERT INTO `role_menu` VALUES (1409, 5, 109);
INSERT INTO `role_menu` VALUES (1410, 2, 404);
INSERT INTO `role_menu` VALUES (1411, 2, 105);
INSERT INTO `role_menu` VALUES (1412, 2, 7);
INSERT INTO `role_menu` VALUES (1413, 2, 701);
INSERT INTO `role_menu` VALUES (1414, 2, 703);
INSERT INTO `role_menu` VALUES (1415, 2, 704);
INSERT INTO `role_menu` VALUES (1416, 2, 501);
INSERT INTO `role_menu` VALUES (1417, 2, 502);
INSERT INTO `role_menu` VALUES (1418, 2, 503);
INSERT INTO `role_menu` VALUES (1419, 2, 504);
INSERT INTO `role_menu` VALUES (1420, 2, 505);
INSERT INTO `role_menu` VALUES (1421, 2, 506);
INSERT INTO `role_menu` VALUES (1422, 2, 507);
INSERT INTO `role_menu` VALUES (1423, 2, 510);
INSERT INTO `role_menu` VALUES (1424, 2, 511);
INSERT INTO `role_menu` VALUES (1425, 2, 512);
INSERT INTO `role_menu` VALUES (1426, 2, 513);
INSERT INTO `role_menu` VALUES (1427, 2, 514);
INSERT INTO `role_menu` VALUES (1428, 2, 515);
INSERT INTO `role_menu` VALUES (1429, 2, 516);
INSERT INTO `role_menu` VALUES (1430, 2, 520);
INSERT INTO `role_menu` VALUES (1431, 2, 522);
INSERT INTO `role_menu` VALUES (1432, 2, 523);
INSERT INTO `role_menu` VALUES (1433, 2, 524);
INSERT INTO `role_menu` VALUES (1477, 6, 101);
INSERT INTO `role_menu` VALUES (1478, 6, 102);
INSERT INTO `role_menu` VALUES (1479, 6, 304);
INSERT INTO `role_menu` VALUES (1480, 6, 305);
INSERT INTO `role_menu` VALUES (1481, 6, 306);
INSERT INTO `role_menu` VALUES (1482, 6, 307);
INSERT INTO `role_menu` VALUES (1483, 6, 205);
INSERT INTO `role_menu` VALUES (1484, 6, 206);
INSERT INTO `role_menu` VALUES (1485, 6, 310);
INSERT INTO `role_menu` VALUES (1486, 6, 401);
INSERT INTO `role_menu` VALUES (1487, 6, 501);
INSERT INTO `role_menu` VALUES (1488, 6, 502);
INSERT INTO `role_menu` VALUES (1489, 6, 503);
INSERT INTO `role_menu` VALUES (1490, 6, 504);
INSERT INTO `role_menu` VALUES (1491, 6, 505);
INSERT INTO `role_menu` VALUES (1492, 6, 506);
INSERT INTO `role_menu` VALUES (1493, 6, 507);
INSERT INTO `role_menu` VALUES (1494, 6, 702);
INSERT INTO `role_menu` VALUES (1495, 6, 1);
INSERT INTO `role_menu` VALUES (1496, 6, 108);
INSERT INTO `role_menu` VALUES (1497, 1, 101);
INSERT INTO `role_menu` VALUES (1498, 1, 102);
INSERT INTO `role_menu` VALUES (1499, 1, 103);
INSERT INTO `role_menu` VALUES (1500, 1, 104);
INSERT INTO `role_menu` VALUES (1501, 1, 201);
INSERT INTO `role_menu` VALUES (1502, 1, 203);
INSERT INTO `role_menu` VALUES (1503, 1, 204);
INSERT INTO `role_menu` VALUES (1504, 1, 301);
INSERT INTO `role_menu` VALUES (1505, 1, 302);
INSERT INTO `role_menu` VALUES (1506, 1, 303);
INSERT INTO `role_menu` VALUES (1507, 1, 304);
INSERT INTO `role_menu` VALUES (1508, 1, 305);
INSERT INTO `role_menu` VALUES (1509, 1, 306);
INSERT INTO `role_menu` VALUES (1510, 1, 404);
INSERT INTO `role_menu` VALUES (1511, 1, 601);
INSERT INTO `role_menu` VALUES (1512, 1, 602);
INSERT INTO `role_menu` VALUES (1513, 1, 603);
INSERT INTO `role_menu` VALUES (1514, 1, 307);
INSERT INTO `role_menu` VALUES (1515, 1, 105);
INSERT INTO `role_menu` VALUES (1516, 1, 604);
INSERT INTO `role_menu` VALUES (1517, 1, 106);
INSERT INTO `role_menu` VALUES (1518, 1, 605);
INSERT INTO `role_menu` VALUES (1519, 1, 107);
INSERT INTO `role_menu` VALUES (1520, 1, 205);
INSERT INTO `role_menu` VALUES (1521, 1, 206);
INSERT INTO `role_menu` VALUES (1522, 1, 308);
INSERT INTO `role_menu` VALUES (1523, 1, 309);
INSERT INTO `role_menu` VALUES (1524, 1, 310);
INSERT INTO `role_menu` VALUES (1525, 1, 311);
INSERT INTO `role_menu` VALUES (1526, 1, 311);
INSERT INTO `role_menu` VALUES (1527, 1, 606);
INSERT INTO `role_menu` VALUES (1528, 1, 607);
INSERT INTO `role_menu` VALUES (1529, 1, 401);
INSERT INTO `role_menu` VALUES (1530, 1, 608);
INSERT INTO `role_menu` VALUES (1531, 1, 520);
INSERT INTO `role_menu` VALUES (1532, 1, 522);
INSERT INTO `role_menu` VALUES (1533, 1, 523);
INSERT INTO `role_menu` VALUES (1534, 1, 524);
INSERT INTO `role_menu` VALUES (1535, 1, 501);
INSERT INTO `role_menu` VALUES (1536, 1, 502);
INSERT INTO `role_menu` VALUES (1537, 1, 503);
INSERT INTO `role_menu` VALUES (1538, 1, 504);
INSERT INTO `role_menu` VALUES (1539, 1, 505);
INSERT INTO `role_menu` VALUES (1540, 1, 506);
INSERT INTO `role_menu` VALUES (1541, 1, 507);
INSERT INTO `role_menu` VALUES (1542, 1, 510);
INSERT INTO `role_menu` VALUES (1543, 1, 511);
INSERT INTO `role_menu` VALUES (1544, 1, 512);
INSERT INTO `role_menu` VALUES (1545, 1, 513);
INSERT INTO `role_menu` VALUES (1546, 1, 514);
INSERT INTO `role_menu` VALUES (1547, 1, 515);
INSERT INTO `role_menu` VALUES (1548, 1, 516);
INSERT INTO `role_menu` VALUES (1549, 1, 7);
INSERT INTO `role_menu` VALUES (1550, 1, 701);
INSERT INTO `role_menu` VALUES (1551, 1, 702);
INSERT INTO `role_menu` VALUES (1552, 1, 703);
INSERT INTO `role_menu` VALUES (1553, 1, 704);
INSERT INTO `role_menu` VALUES (1554, 1, 109);
INSERT INTO `role_menu` VALUES (1555, 1, 108);
INSERT INTO `role_menu` VALUES (1556, 5, 189);
INSERT INTO `role_menu` VALUES (1557, 3, 190);

-- ----------------------------
-- Table structure for schedule
-- ----------------------------
DROP TABLE IF EXISTS `schedule`;
CREATE TABLE `schedule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '排课ID',
  `teacher_id` bigint NOT NULL COMMENT '授课教师ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `class_id` bigint NOT NULL COMMENT '班级ID',
  `start_section` bigint NULL DEFAULT NULL COMMENT '开始节次(关联sys_dict_data.dict_code)',
  `end_section` bigint NULL DEFAULT NULL COMMENT '结束节次(关联sys_dict_data.dict_code)',
  `day_of_week` tinyint NOT NULL COMMENT '星期几(1-7)',
  `classroom` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '教室',
  `semester` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学期',
  `year` year NOT NULL COMMENT '学年',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_teacher_time`(`teacher_id` ASC, `day_of_week` ASC, `start_section` ASC, `end_section` ASC, `semester` ASC, `year` ASC) USING BTREE,
  UNIQUE INDEX `uk_class_time`(`class_id` ASC, `day_of_week` ASC, `start_section` ASC, `end_section` ASC, `semester` ASC, `year` ASC) USING BTREE,
  INDEX `idx_teacher`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_course`(`course_id` ASC) USING BTREE,
  INDEX `idx_class`(`class_id` ASC) USING BTREE,
  INDEX `idx_semester`(`semester` ASC, `year` ASC) USING BTREE,
  INDEX `idx_time`(`day_of_week` ASC, `start_section` ASC, `end_section` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2049 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '排课计划表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of schedule
-- ----------------------------
DROP TABLE IF EXISTS `score`;
CREATE TABLE `score`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成绩ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `score` decimal(5, 2) NULL DEFAULT NULL COMMENT '成绩',
  `usual_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '平时成绩',
  `exam_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '期末成绩',
  `total_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '总成绩',
  `usual_weight` decimal(3, 2) NULL DEFAULT 0.30 COMMENT '平时成绩权重(默认30%)',
  `exam_weight` decimal(3, 2) NULL DEFAULT 0.70 COMMENT '期末成绩权重(默认70%)',
  `grade` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '等级：优秀，良好，中等，及格，不及格',
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评语',
  `teacher_id` bigint NOT NULL COMMENT '录入教师ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_course`(`student_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `fk_score_student`(`student_id` ASC) USING BTREE,
  INDEX `fk_score_course`(`course_id` ASC) USING BTREE,
  INDEX `fk_score_teacher`(`teacher_id` ASC) USING BTREE,
  CONSTRAINT `fk_score_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_score_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_score_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6145 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '成绩表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of score
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学生ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `class_id` bigint NULL DEFAULT NULL COMMENT '班级ID(关联class表)',
  `student_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学号',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学生姓名',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '院系',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '专业',
  `admission_year` year NOT NULL COMMENT '入学年份',
  `cohort_year` year NULL DEFAULT NULL COMMENT '归属年级（行政归属年级）',
  `graduation_year` year NULL DEFAULT NULL COMMENT '预计毕业年份',
  `research_direction` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '研究方向',
  `selection_status` tinyint NULL DEFAULT 0 COMMENT '双选状态：0-未开始，1-第一轮，2-第二轮，3-已确定',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-在读，2-休学，3-毕业，4-退学',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_no`(`student_no` ASC) USING BTREE,
  INDEX `fk_student_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_student_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2200 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of student
-- ----------------------------
DROP TABLE IF EXISTS `student_status_change`;
CREATE TABLE `student_status_change`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '异动记录ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `type` tinyint NOT NULL COMMENT '异动类型：1-休学，2-复学，3-退学，4-延期毕业',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '异动原因',
  `start_date` date NULL DEFAULT NULL COMMENT '开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期（仅休学）',
  `mentor_approval` tinyint NULL DEFAULT 0 COMMENT '导师审批：0-未审批，1-同意，2-拒绝',
  `mentor_id` bigint NULL DEFAULT NULL COMMENT '审批导师ID',
  `mentor_approval_time` datetime NULL DEFAULT NULL COMMENT '导师审批时间',
  `secretary_approval` tinyint NULL DEFAULT 0 COMMENT '教学秘书审批：0-未审批，1-同意，2-拒绝',
  `secretary_id` bigint NULL DEFAULT NULL COMMENT '审批教学秘书ID',
  `secretary_approval_time` datetime NULL DEFAULT NULL COMMENT '教学秘书审批时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待导师审批，1-待教学秘书审批，2-已批准，3-已拒绝',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_status_change_student`(`student_id` ASC) USING BTREE,
  INDEX `fk_status_change_tutor`(`mentor_id` ASC) USING BTREE,
  INDEX `fk_status_change_counselor`(`secretary_id` ASC) USING BTREE,
  CONSTRAINT `fk_status_change_counselor` FOREIGN KEY (`secretary_id`) REFERENCES `counselor` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_status_change_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_status_change_tutor` FOREIGN KEY (`mentor_id`) REFERENCES `teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学籍异动表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of student_status_change
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int NULL DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'default' COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 68 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典数据表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 1, '正常', '0', 'sys_normal_disable', NULL, 'primary', 'Y', '0', 'admin', '2026-04-23 13:12:46', '', NULL, '正常');
INSERT INTO `sys_dict_data` VALUES (2, 2, '停用', '1', 'sys_normal_disable', NULL, 'danger', 'N', '0', 'admin', '2026-04-23 13:12:46', '', NULL, '停用');
INSERT INTO `sys_dict_data` VALUES (8, 1, '男', '0', 'sys_user_sex', NULL, 'primary', 'Y', '0', 'admin', '2026-04-23 13:12:47', '', NULL, '性别男');
INSERT INTO `sys_dict_data` VALUES (9, 2, '女', '1', 'sys_user_sex', NULL, 'success', 'N', '0', 'admin', '2026-04-23 13:12:47', '', NULL, '性别女');
INSERT INTO `sys_dict_data` VALUES (10, 3, '未知', '2', 'sys_user_sex', NULL, 'info', 'N', '0', 'admin', '2026-04-23 13:12:47', '', NULL, '性别未知');
INSERT INTO `sys_dict_data` VALUES (11, 1, '第1节', '08:30-09:15', 'sys_time_slot', NULL, 'default', 'N', '0', 'admin', '2026-04-23 19:58:22', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (12, 2, '第2节', '09:20-10:05', 'sys_time_slot', NULL, 'default', 'N', '0', 'admin', '2026-04-23 19:58:22', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (13, 3, '第3节', '10:25-11:10', 'sys_time_slot', NULL, 'default', 'N', '0', 'admin', '2026-04-23 19:58:22', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (14, 4, '第4节', '11:15-12:00', 'sys_time_slot', NULL, 'default', 'N', '0', 'admin', '2026-04-23 19:58:22', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (15, 5, '第5节', '13:30-14:15', 'sys_time_slot', NULL, 'default', 'N', '0', 'admin', '2026-04-23 19:58:22', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (16, 6, '第6节', '14:20-15:05', 'sys_time_slot', NULL, 'default', 'N', '0', 'admin', '2026-04-23 19:58:22', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (17, 7, '第7节', '15:25-16:10', 'sys_time_slot', NULL, 'default', 'N', '0', 'admin', '2026-04-23 19:58:22', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (18, 8, '第8节', '16:15-17:00', 'sys_time_slot', NULL, 'default', 'N', '0', 'admin', '2026-04-23 19:58:22', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (19, 9, '第9节', '18:00-18:45', 'sys_time_slot', NULL, 'default', 'N', '0', 'admin', '2026-04-23 19:58:22', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (20, 10, '第10节', '18:50-19:35', 'sys_time_slot', NULL, 'default', 'N', '0', 'admin', '2026-04-23 19:58:22', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (21, 1, '其它', '0', 'sys_oper_type', NULL, 'info', 'N', '0', 'admin', '2026-04-24 12:00:00', '', NULL, '其它');
INSERT INTO `sys_dict_data` VALUES (22, 2, '新增', '1', 'sys_oper_type', NULL, 'success', 'N', '0', 'admin', '2026-04-24 12:00:00', '', NULL, '新增');
INSERT INTO `sys_dict_data` VALUES (23, 3, '修改', '2', 'sys_oper_type', NULL, 'primary', 'N', '0', 'admin', '2026-04-24 12:00:00', '', NULL, '修改');
INSERT INTO `sys_dict_data` VALUES (24, 4, '删除', '3', 'sys_oper_type', NULL, 'danger', 'N', '0', 'admin', '2026-04-24 12:00:00', '', NULL, '删除');
INSERT INTO `sys_dict_data` VALUES (25, 5, '导出', '4', 'sys_oper_type', NULL, 'warning', 'N', '0', 'admin', '2026-04-24 12:00:00', '', NULL, '导出');
INSERT INTO `sys_dict_data` VALUES (26, 6, '导入', '5', 'sys_oper_type', NULL, 'warning', 'N', '0', 'admin', '2026-04-24 12:00:00', '', NULL, '导入');
INSERT INTO `sys_dict_data` VALUES (27, 1, '正常', '0', 'sys_common_status', NULL, 'primary', 'Y', '0', 'admin', '2026-04-24 12:00:00', '', NULL, '正常');
INSERT INTO `sys_dict_data` VALUES (28, 2, '异常', '1', 'sys_common_status', NULL, 'danger', 'N', '0', 'admin', '2026-04-24 12:00:00', '', NULL, '异常');
INSERT INTO `sys_dict_data` VALUES (29, 7, '登录', '6', 'sys_oper_type', NULL, 'primary', 'N', '0', 'admin', '2026-04-28 10:14:16', '', NULL, '登录');
INSERT INTO `sys_dict_data` VALUES (30, 1, '学生', '1', 'academic_submitter_type', NULL, 'primary', 'Y', '0', 'admin', '2026-04-29 12:04:49', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (31, 2, '导师', '2', 'academic_submitter_type', NULL, 'success', 'N', '0', 'admin', '2026-04-29 12:04:49', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (32, 3, '管理员', '3', 'academic_submitter_type', NULL, 'info', 'N', '0', 'admin', '2026-04-29 12:04:49', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (33, 1, '学术活动', '1', 'academic_content_type', NULL, 'primary', 'Y', '0', 'admin', '2026-04-29 12:04:49', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (34, 2, '学术成果', '2', 'academic_content_type', NULL, 'success', 'N', '0', 'admin', '2026-04-29 12:04:49', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (35, 3, '创新创业', '3', 'academic_content_type', NULL, 'warning', 'N', '0', 'admin', '2026-04-29 12:04:49', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (36, 1, '待提交', '0', 'academic_approval_status', NULL, 'info', 'Y', '0', 'admin', '2026-04-29 12:04:49', '', NULL, '草稿状态');
INSERT INTO `sys_dict_data` VALUES (37, 2, '待导师审批', '1', 'academic_approval_status', NULL, 'warning', 'N', '0', 'admin', '2026-04-29 12:04:49', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (38, 3, '待秘书审批', '2', 'academic_approval_status', NULL, 'warning', 'N', '0', 'admin', '2026-04-29 12:04:49', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (39, 4, '待院长审批', '3', 'academic_approval_status', NULL, 'warning', 'N', '0', 'admin', '2026-04-29 12:04:49', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (40, 5, '已通过', '4', 'academic_approval_status', NULL, 'success', 'N', '0', 'admin', '2026-04-29 12:04:49', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (41, 6, '已驳回', '5', 'academic_approval_status', NULL, 'danger', 'N', '0', 'admin', '2026-04-29 12:04:49', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (42, 1, '提交', '1', 'academic_approval_action', NULL, 'primary', 'N', '0', 'admin', '2026-04-29 12:04:50', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (43, 2, '通过', '2', 'academic_approval_action', NULL, 'success', 'N', '0', 'admin', '2026-04-29 12:04:50', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (44, 3, '驳回', '3', 'academic_approval_action', NULL, 'danger', 'N', '0', 'admin', '2026-04-29 12:04:50', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (45, 4, '撤回', '4', 'academic_approval_action', NULL, 'info', 'N', '0', 'admin', '2026-04-29 12:04:50', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (46, 1, '学术讲座', '1', 'academic_activity_type', NULL, 'primary', 'Y', '0', 'admin', '2026-04-29 12:04:50', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (47, 2, '研讨会', '2', 'academic_activity_type', NULL, 'success', 'N', '0', 'admin', '2026-04-29 12:04:50', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (48, 3, '论坛', '3', 'academic_activity_type', NULL, 'warning', 'N', '0', 'admin', '2026-04-29 12:04:50', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (49, 4, '其他', '4', 'academic_activity_type', NULL, 'info', 'N', '0', 'admin', '2026-04-29 12:04:50', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (50, 1, '期刊论文', '1', 'academic_achievement_type', NULL, 'primary', 'Y', '0', 'admin', '2026-04-29 12:04:50', '', NULL, '{\"need_doi\":true}');
INSERT INTO `sys_dict_data` VALUES (51, 2, '专利', '2', 'academic_achievement_type', NULL, 'warning', 'N', '0', 'admin', '2026-04-29 12:04:50', '', NULL, '{\"need_patent_no\":true}');
INSERT INTO `sys_dict_data` VALUES (52, 3, '科研奖励', '3', 'academic_achievement_type', NULL, 'success', 'N', '0', 'admin', '2026-04-29 12:04:50', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (53, 4, '项目参与', '4', 'academic_achievement_type', NULL, 'info', 'N', '0', 'admin', '2026-04-29 12:04:50', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (54, 1, '创新项目', '1', 'academic_project_type', NULL, 'primary', 'Y', '0', 'admin', '2026-04-29 12:04:50', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (55, 2, '创业项目', '2', 'academic_project_type', NULL, 'success', 'N', '0', 'admin', '2026-04-29 12:04:50', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (56, 3, '竞赛', '3', 'academic_project_type', NULL, 'warning', 'N', '0', 'admin', '2026-04-29 12:04:50', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (57, 1, '通知', '1', 'sys_notice_type', NULL, 'primary', 'Y', '0', 'admin', '2026-04-30 10:24:50', '', NULL, '通知');
INSERT INTO `sys_dict_data` VALUES (58, 2, '公告', '2', 'sys_notice_type', NULL, 'success', 'N', '0', 'admin', '2026-04-30 10:24:50', '', NULL, '公告');
INSERT INTO `sys_dict_data` VALUES (59, 1, '正常', '0', 'sys_notice_status', NULL, 'primary', 'Y', '0', 'admin', '2026-04-30 10:24:50', '', NULL, '正常');
INSERT INTO `sys_dict_data` VALUES (60, 2, '关闭', '1', 'sys_notice_status', NULL, 'danger', 'N', '0', 'admin', '2026-04-30 10:24:50', '', NULL, '关闭');
INSERT INTO `sys_dict_data` VALUES (61, 1, '选题', '1', 'thesis_process_type', NULL, 'primary', 'Y', '0', 'admin', '2026-05-01 19:38:07', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (62, 2, '任务书', '2', 'thesis_process_type', NULL, 'success', 'N', '0', 'admin', '2026-05-01 19:38:07', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (63, 3, '开题报告', '3', 'thesis_process_type', NULL, 'info', 'N', '0', 'admin', '2026-05-01 19:38:07', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (64, 4, '中期检查', '4', 'thesis_process_type', NULL, 'warning', 'N', '0', 'admin', '2026-05-01 19:38:07', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (65, 5, '过程稿', '5', 'thesis_process_type', NULL, 'default', 'N', '0', 'admin', '2026-05-01 19:38:07', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (66, 6, '论文答辩稿', '6', 'thesis_process_type', NULL, 'default', 'N', '0', 'admin', '2026-05-01 19:38:07', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (67, 7, '毕业论文', '7', 'thesis_process_type', NULL, 'danger', 'N', '0', 'admin', '2026-05-01 19:38:07', '', NULL, NULL);

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE INDEX `idx_dict_type`(`dict_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '系统是否', 'sys_normal_disable', '0', 'admin', '2026-04-23 13:12:46', '', NULL, '系统是否列表');
INSERT INTO `sys_dict_type` VALUES (2, '时间片', 'sys_time_slot', '0', 'admin', '2026-04-23 13:12:46', '', NULL, '时间片字典类型');
INSERT INTO `sys_dict_type` VALUES (3, '用户性别', 'sys_user_sex', '0', 'admin', '2026-04-23 13:12:46', '', NULL, '用户性别列表');
INSERT INTO `sys_dict_type` VALUES (4, '操作类型', 'sys_oper_type', '0', 'admin', '2026-04-24 12:00:00', '', NULL, '操作类型列表');
INSERT INTO `sys_dict_type` VALUES (5, '系统状态', 'sys_common_status', '0', 'admin', '2026-04-24 12:00:00', '', NULL, '系统状态列表');
INSERT INTO `sys_dict_type` VALUES (6, '提交人类型', 'academic_submitter_type', '0', 'admin', '2026-04-29 12:04:49', '', NULL, '学术提交人类型');
INSERT INTO `sys_dict_type` VALUES (7, '学术内容类型', 'academic_content_type', '0', 'admin', '2026-04-29 12:04:49', '', NULL, '学术内容大类');
INSERT INTO `sys_dict_type` VALUES (8, '审批状态', 'academic_approval_status', '0', 'admin', '2026-04-29 12:04:49', '', NULL, '学术审批流程状态');
INSERT INTO `sys_dict_type` VALUES (9, '审批动作', 'academic_approval_action', '0', 'admin', '2026-04-29 12:04:49', '', NULL, '审批操作类型');
INSERT INTO `sys_dict_type` VALUES (10, '学术活动类型', 'academic_activity_type', '0', 'admin', '2026-04-29 12:04:49', '', NULL, '学术活动细分类型');
INSERT INTO `sys_dict_type` VALUES (11, '学术成果类型', 'academic_achievement_type', '0', 'admin', '2026-04-29 12:04:49', '', NULL, '学术成果细分类型');
INSERT INTO `sys_dict_type` VALUES (12, '创新项目类型', 'academic_project_type', '0', 'admin', '2026-04-29 12:04:49', '', NULL, '创新项目细分类型');
INSERT INTO `sys_dict_type` VALUES (13, '通知类型', 'sys_notice_type', '0', 'admin', '2026-04-30 10:24:50', '', NULL, '通知类型列表');
INSERT INTO `sys_dict_type` VALUES (14, '通知状态', 'sys_notice_status', '0', 'admin', '2026-04-30 10:24:50', '', NULL, '通知状态列表');
INSERT INTO `sys_dict_type` VALUES (15, '论文流程环节', 'thesis_process_type', '0', 'admin', '2026-05-01 19:38:07', '', NULL, '论文流程7环节类型');

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `notice_id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '公告内容',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `target_roles` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标角色（逗号分隔角色ID，空表示全体）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `notification_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知去重键（格式：模块:事件:关联ID:日期）',
  PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 79 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice_read`;
CREATE TABLE `sys_notice_read`  (
  `read_id` bigint NOT NULL AUTO_INCREMENT COMMENT '阅读记录ID',
  `notice_id` bigint NOT NULL COMMENT '公告ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`read_id`) USING BTREE,
  UNIQUE INDEX `uk_notice_user`(`notice_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_notice_read_notice` FOREIGN KEY (`notice_id`) REFERENCES `sys_notice` (`notice_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3966 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公告阅读记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice_read
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '模块标题',
  `business_type` int NULL DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '请求方式',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '操作人员',
  `oper_role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '操作人员角色',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '主机地址',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '返回参数',
  `status` int NULL DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint NULL DEFAULT 0 COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`) USING BTREE,
  INDEX `idx_sys_oper_log_bt`(`business_type` ASC) USING BTREE,
  INDEX `idx_sys_oper_log_s`(`status` ASC) USING BTREE,
  INDEX `idx_sys_oper_log_ot`(`oper_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 458 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作日志记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `config_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置项键名（唯一）',
  `config_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项值',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置项名称（中文描述）',
  `config_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项类型（用于分组）',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项说明',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user` bigint NULL DEFAULT NULL COMMENT '更新人ID（关联user表）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 65 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of system_config
-- ----------------------------
INSERT INTO `system_config` VALUES (1, 'student_max_choices', '3', '学生最大志愿数', 'selection', '学生每轮可选择的导师数量上限（上限4）', '2026-05-19 10:40:46', NULL);
INSERT INTO `system_config` VALUES (3, 'mentor_max_students', '10', '导师默认最大带教数', 'selection', '新导师默认的最大可指导学生数量', '2026-04-11 18:42:55', NULL);
INSERT INTO `system_config` VALUES (4, 'first_round_start', '2026-05-19 10:48:59', '第一轮开始时间', 'selection', '双选第一轮开始时间', '2026-05-19 10:49:00', NULL);
INSERT INTO `system_config` VALUES (6, 'first_round_end_tutor', '2026-05-20 00:00:00', '第一轮导师确认截止', 'selection', '导师确认学生的截止时间', '2026-05-19 10:49:00', NULL);
INSERT INTO `system_config` VALUES (7, 'second_round_start', '', '第二轮开始时间', 'selection', '双选第二轮开始时间', '2026-05-19 10:40:44', NULL);
INSERT INTO `system_config` VALUES (9, 'second_round_end_tutor', '', '第二轮导师确认截止', 'selection', '第二轮导师确认学生的截止时间', '2026-05-19 10:40:44', NULL);
INSERT INTO `system_config` VALUES (10, 'auto_match_unselected', '1', '未匹配学生自动分配', 'selection', '0-不自动分配，1-自动分配给有名额的导师', '2026-04-11 18:42:55', NULL);
INSERT INTO `system_config` VALUES (11, 'student_max_courses', '3', '学生最大选课数量', 'course', '学生一次最多可选择的课程数量', '2026-04-01 19:41:00', NULL);
INSERT INTO `system_config` VALUES (12, 'enable_extra_round', 'true', '开启落选学生额外轮次', 'selection', '是否为落选学生开启额外轮次', '2026-04-09 20:08:55', NULL);
INSERT INTO `system_config` VALUES (13, 'current_round', '1', '当前轮次', 'selection', '0=未开始,9=学生预选轮,1=第一轮,2=第二轮,3=第三轮,7=补选学生选择轮,8=补选导师选择轮,91/12/23/34/78=中间阶段', '2026-05-19 10:49:00', 1);
INSERT INTO `system_config` VALUES (14, 'third_round_start', '', '第三轮开始时间', 'selection', '第三轮开始时间', '2026-05-19 10:40:44', NULL);
INSERT INTO `system_config` VALUES (16, 'third_round_end_tutor', '', '第三轮导师确认截止时间', 'selection', '第三轮导师确认学生的截止时间', '2026-05-19 10:40:44', NULL);
INSERT INTO `system_config` VALUES (26, 'student_select_start', '2026-05-19 10:40:50', '学生选择开始时间', 'selection', '学生选择轮开始时间', '2026-05-19 10:40:50', NULL);
INSERT INTO `system_config` VALUES (27, 'student_select_end', '2026-05-19 10:48:59', '学生选择截止时间', 'selection', '学生选择轮截止时间', '2026-05-19 10:48:59', NULL);
INSERT INTO `system_config` VALUES (28, 'supplementary_student_start', '', '补选学生选择开始时间', 'selection', NULL, '2026-05-19 10:40:44', NULL);
INSERT INTO `system_config` VALUES (29, 'supplementary_student_end', '', '补选学生选择截止时间', 'selection', NULL, '2026-05-19 10:40:45', NULL);
INSERT INTO `system_config` VALUES (30, 'supplementary_tutor_start', '', '补选导师选择开始时间', 'selection', NULL, '2026-05-19 10:40:45', NULL);
INSERT INTO `system_config` VALUES (31, 'supplementary_tutor_end', '', '补选导师选择截止时间', 'selection', NULL, '2026-05-19 10:40:45', NULL);
INSERT INTO `system_config` VALUES (32, 'selection_cohort_year', '2023', '双选归属年级', 'selection', '当前双选允许参与的学生归属年级，为空表示所有年级', '2026-05-19 10:40:46', NULL);
INSERT INTO `system_config` VALUES (33, 'course_phase_status', '3', '课程阶段状态', 'course_phase', '0=未开始,1=选课阶段,2=已开课,3=成绩录入阶段,4=已结课', '2026-05-17 13:18:19', NULL);
INSERT INTO `system_config` VALUES (34, 'course_selection_start', '2026-05-17 13:17:41', '选课开始时间', 'course_phase', '选课阶段的开始时间', '2026-05-17 13:17:42', NULL);
INSERT INTO `system_config` VALUES (35, 'course_selection_end', '2026-05-17 13:17:57', '选课截止时间', 'course_phase', '选课阶段的截止时间', '2026-05-17 13:17:58', NULL);
INSERT INTO `system_config` VALUES (36, 'score_entry_start', '2026-05-17 13:18:18', '成绩录入开始时间', 'course_phase', '成绩录入阶段的开始时间', '2026-05-17 13:18:19', NULL);
INSERT INTO `system_config` VALUES (37, 'score_entry_end', '2026-05-28 00:00:00', '成绩录入截止时间', 'course_phase', '成绩录入阶段的截止时间', '2026-05-17 13:18:19', NULL);
INSERT INTO `system_config` VALUES (43, 'thesis_overdue_reminder', 'true', '逾期自动提醒', 'thesis', '开启后对逾期未提交的学生发送提醒通知', '2026-05-16 19:12:27', NULL);
INSERT INTO `system_config` VALUES (44, 'thesis_overdue_days', '7', '逾期天数阈值', 'thesis', '超过截止日期N天后发送提醒', '2026-05-16 19:12:27', NULL);
INSERT INTO `system_config` VALUES (45, 'thesis_serial_mode', 'true', '流程串行模式', 'thesis', '严格顺序模式下前序环节未通过不可提交后续环节', '2026-04-30 20:18:34', NULL);
INSERT INTO `system_config` VALUES (46, 'thesis_defense_eligibility_check', 'true', '答辩资格检查', 'thesis', '开启后学生必须通过外审才能申请答辩', '2026-04-30 20:18:34', NULL);
INSERT INTO `system_config` VALUES (47, 'thesis_max_resubmit_count', '3', '最大重新提交次数', 'thesis', '学位流程-最大重新提交次数', '2026-04-30 20:18:34', NULL);
INSERT INTO `system_config` VALUES (48, 'thesis_min_words', '30000', '论文字数下限要求', 'thesis', '学位论文最低字数要求', '2026-04-30 20:18:34', NULL);
INSERT INTO `system_config` VALUES (49, 'thesis_review_expert_count', '3', '外审专家人数', 'thesis', '学位论文外审专家人数', '2026-04-30 20:18:34', NULL);
INSERT INTO `system_config` VALUES (50, 'thesis_defense_pass_score', '60', '答辩及格分数线', 'thesis', '学位答辩及格分数线', '2026-04-30 20:18:34', NULL);
INSERT INTO `system_config` VALUES (51, 'thesis_topic_require_approval', 'true', '选题需要导师审批', 'thesis', NULL, '2026-05-01 19:38:07', NULL);
INSERT INTO `system_config` VALUES (52, 'thesis_task_book_require_secretary', 'true', '任务书需要教学秘书审核', 'thesis', NULL, '2026-05-01 19:38:07', NULL);
INSERT INTO `system_config` VALUES (53, 'thesis_defense_draft_require_comment', 'true', '答辩稿需要导师评语', 'thesis', NULL, '2026-05-01 19:38:07', NULL);
INSERT INTO `system_config` VALUES (55, 'graduation_required_credits', '30', '毕业要求学分', 'graduation', '研究生毕业所需的最低学分要求', '2026-05-14 09:06:46', NULL);
INSERT INTO `system_config` VALUES (56, 'graduation_required_practice', '1', '毕业要求实践项目数', 'graduation', '研究生毕业所需通过的创新创业项目最低数量（content_type=3, approval_status=4）', '2026-05-14 11:04:56', NULL);

-- ----------------------------
-- Table structure for teacher
-- ----------------------------
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '教师ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `teacher_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '教师姓名',
  `teacher_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '教师编号',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职称',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '院系',
  `research_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '研究领域（仅导师有值）',
  `quota` int NULL DEFAULT 0 COMMENT '招生名额（仅导师有值）',
  `remaining_quota` int NULL DEFAULT 0 COMMENT '剩余名额（仅导师有值）',
  `confirmed_quota` int NULL DEFAULT 0 COMMENT '已确认名额（仅导师有值）',
  `is_mentor` tinyint NOT NULL DEFAULT 0 COMMENT '是否可指导研究生：0-否，1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_teacher_no`(`teacher_no` ASC) USING BTREE,
  INDEX `fk_teacher_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_teacher_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 59 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教师信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of teacher
-- ----------------------------
DROP TABLE IF EXISTS `teaching_evaluation`;
CREATE TABLE `teaching_evaluation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `teacher_id` bigint NOT NULL COMMENT '被评价教师ID',
  `attitude_score` tinyint NOT NULL COMMENT '教学态度评分(1-5)',
  `content_score` tinyint NOT NULL COMMENT '教学内容评分(1-5)',
  `method_score` tinyint NOT NULL COMMENT '教学方法评分(1-5)',
  `effect_score` tinyint NOT NULL COMMENT '教学效果评分(1-5)',
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文字评语',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_course`(`student_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `idx_teacher`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_course`(`course_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3224 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教学评价表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of teaching_evaluation
-- ----------------------------
DROP TABLE IF EXISTS `thesis_grade`;
CREATE TABLE `thesis_grade`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `thesis_id` bigint NOT NULL COMMENT '关联论文主ID',
  `supervisor_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '指导教师评分',
  `supervisor_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '指导教师评语',
  `supervisor_time` datetime NULL DEFAULT NULL COMMENT '指导教师评分时间',
  `reviewer_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '评阅教师评分',
  `reviewer_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评阅教师评语',
  `reviewer_time` datetime NULL DEFAULT NULL COMMENT '评阅评分时间',
  `defense_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '答辩评分',
  `defense_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '答辩评语',
  `defense_time` datetime NULL DEFAULT NULL COMMENT '答辩评分时间',
  `total_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '总评成绩',
  `grade_level` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '等级：优秀/良好/中等/及格/不及格',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_thesis`(`thesis_id` ASC) USING BTREE,
  CONSTRAINT `fk_grade_thesis_main` FOREIGN KEY (`thesis_id`) REFERENCES `thesis_main` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '成绩评定表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of thesis_grade
-- ----------------------------
DROP TABLE IF EXISTS `thesis_main`;
CREATE TABLE `thesis_main`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '论文主ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学号（冗余，便于查询展示）',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学生姓名（冗余，便于查询展示）',
  `supervisor_id` bigint NOT NULL COMMENT '导师ID',
  `supervisor_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '导师姓名（冗余，便于查询展示）',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '专业',
  `grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '年级',
  `thesis_title` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最终论文题目',
  `thesis_final_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最终定稿论文路径',
  `final_result` tinyint NULL DEFAULT 0 COMMENT '论文最终结果：0-进行中，1-通过，2-未通过',
  `final_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '最终答辩评分',
  `archive_status` tinyint NULL DEFAULT 0 COMMENT '归档状态：0-未归档，1-已归档',
  `archive_time` datetime NULL DEFAULT NULL COMMENT '归档时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_thesis`(`student_id` ASC) USING BTREE COMMENT '一个学生仅对应一篇毕业论文',
  INDEX `idx_student_no`(`student_no` ASC) USING BTREE COMMENT '学号查询索引',
  INDEX `idx_supervisor_id`(`supervisor_id` ASC) USING BTREE COMMENT '导师查询索引',
  INDEX `idx_major_grade`(`major` ASC, `grade` ASC) USING BTREE COMMENT '专业年级联合查询索引',
  INDEX `idx_archive_status`(`archive_status` ASC) USING BTREE COMMENT '归档状态查询索引',
  INDEX `idx_final_result`(`final_result` ASC) USING BTREE COMMENT '最终结果查询索引',
  CONSTRAINT `fk_thesis_main_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_thesis_main_supervisor` FOREIGN KEY (`supervisor_id`) REFERENCES `teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '论文主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of thesis_main
-- ----------------------------
DROP TABLE IF EXISTS `thesis_process_config`;
CREATE TABLE `thesis_process_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_type` int NOT NULL COMMENT '流程环节类型：1-选题，2-任务书，3-开题报告，4-中期检查，5-过程稿，6-论文答辩稿，7-毕业论文',
  `process_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '环节名称',
  `enabled` int NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `deadline` datetime NULL DEFAULT NULL COMMENT '截止时间',
  `need_supervisor_approval` int NULL DEFAULT 1 COMMENT '是否需要导师审批：0-否，1-是',
  `need_secretary_approval` int NULL DEFAULT 0 COMMENT '是否需要秘书审批：0-否，1-是',
  `need_dean_approval` int NULL DEFAULT 0 COMMENT '是否需要院长审批：0-否，1-是',
  `need_review_result` int NULL DEFAULT 0 COMMENT '是否需要录入评审结果：0-否，1-是',
  `score_weight` decimal(3, 2) NULL DEFAULT NULL COMMENT '成绩权重（如0.20表示20%）',
  `sort` int NULL DEFAULT 0 COMMENT '排序号',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_process_type`(`process_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '流程环节配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of thesis_process_config
-- ----------------------------
INSERT INTO `thesis_process_config` VALUES (8, 1, '选题', 1, '2026-05-25 18:26:47', 1, 0, 0, 0, NULL, 1, '学生申报选题，导师审核确认', '2026-05-01 19:38:07', '2026-05-16 20:03:11');
INSERT INTO `thesis_process_config` VALUES (9, 2, '任务书', 1, NULL, 0, 1, 1, 0, NULL, 2, '导师下达任务书，教学秘书审核', '2026-05-01 19:38:07', '2026-05-17 14:25:28');
INSERT INTO `thesis_process_config` VALUES (10, 3, '开题报告', 1, NULL, 1, 0, 0, 0, NULL, 3, '学生填写开题报告，导师审核', '2026-05-01 19:38:07', '2026-05-16 19:50:10');
INSERT INTO `thesis_process_config` VALUES (11, 4, '中期检查', 1, NULL, 1, 0, 0, 0, NULL, 4, '学生填写进展报告，导师审核', '2026-05-01 19:38:07', '2026-05-16 19:50:10');
INSERT INTO `thesis_process_config` VALUES (12, 5, '过程稿', 1, NULL, 1, 0, 0, 0, NULL, 5, '学生提交论文修改稿，导师审阅，支持多版本', '2026-05-01 19:38:07', '2026-05-16 19:50:10');
INSERT INTO `thesis_process_config` VALUES (13, 6, '论文答辩稿', 1, NULL, 1, 0, 0, 0, NULL, 6, '学生提交答辩稿，导师审阅+评语', '2026-05-01 19:38:07', '2026-05-16 19:50:10');
INSERT INTO `thesis_process_config` VALUES (14, 7, '毕业论文', 1, NULL, 1, 0, 0, 0, NULL, 7, '定稿提交，提交后锁定', '2026-05-01 19:38:07', '2026-05-16 19:50:10');

-- ----------------------------
-- Table structure for thesis_process_record
-- ----------------------------
DROP TABLE IF EXISTS `thesis_process_record`;
CREATE TABLE `thesis_process_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '流程记录ID',
  `thesis_id` bigint NOT NULL COMMENT '关联论文主ID',
  `process_type` tinyint NOT NULL COMMENT '流程环节类型：1-开题报告，2-中期检查，3-预答辩，4-论文外审，5-正式答辩，6-二次答辩，7-修改后再审',
  `version` int NOT NULL DEFAULT 1 COMMENT '版本号：支持同一环节多次提交/二次答辩',
  `thesis_version_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本环节提交的论文版本路径',
  `attachment_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本环节附件路径（开题/中期报告等）',
  `content_extend` json NULL COMMENT '环节个性化内容（JSON格式）',
  `supervisor_status` tinyint NULL DEFAULT 0 COMMENT '导师审批状态：0-未审批，1-同意，2-拒绝',
  `supervisor_time` datetime NULL DEFAULT NULL COMMENT '导师审批时间',
  `supervisor_comment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '导师审批意见',
  `supervisor_approver_id` bigint NULL DEFAULT NULL COMMENT '导师审批操作人ID',
  `secretary_status` tinyint NULL DEFAULT 0 COMMENT '教学秘书审批状态：0-未审批，1-同意，2-拒绝',
  `secretary_time` datetime NULL DEFAULT NULL COMMENT '秘书审批时间',
  `secretary_comment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '秘书审批意见',
  `secretary_approver_id` bigint NULL DEFAULT NULL COMMENT '秘书审批操作人ID',
  `dean_status` tinyint NULL DEFAULT 0 COMMENT '院长审批状态：0-未审批，1-同意，2-拒绝',
  `dean_time` datetime NULL DEFAULT NULL COMMENT '院长审批时间',
  `dean_comment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '院长审批意见',
  `dean_approver_id` bigint NULL DEFAULT NULL COMMENT '院长审批操作人ID',
  `event_time` datetime NULL DEFAULT NULL COMMENT '事件时间（开题/答辩/外审时间）',
  `event_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件地点',
  `review_committee_chair` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评审委员会主席',
  `review_committee_members` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评审/外审专家成员',
  `review_result` tinyint NULL DEFAULT 0 COMMENT '评审/答辩结果：0-未进行，1-通过，2-修改后通过，3-未通过',
  `review_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '评审/答辩评分',
  `review_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评审/答辩委员会评语',
  `qa_record` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '答辩问答记录',
  `process_status` tinyint NULL DEFAULT 0 COMMENT '环节整体状态：0-未提交，1-审批中，2-评审中，3-已通过，4-已拒绝，5-已完成',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_thesis_process_version`(`thesis_id` ASC, `process_type` ASC, `version` ASC) USING BTREE COMMENT '同一论文同一环节同一版本唯一',
  INDEX `idx_thesis_id`(`thesis_id` ASC) USING BTREE COMMENT '关联主表查询索引',
  INDEX `idx_process_type`(`process_type` ASC) USING BTREE COMMENT '环节类型查询索引',
  INDEX `idx_process_status`(`process_status` ASC) USING BTREE COMMENT '状态查询索引',
  INDEX `idx_review_result`(`review_result` ASC) USING BTREE COMMENT '结果查询索引',
  INDEX `idx_event_time`(`event_time` ASC) USING BTREE COMMENT '事件时间查询索引',
  CONSTRAINT `fk_process_thesis_main` FOREIGN KEY (`thesis_id`) REFERENCES `thesis_main` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 260 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '论文流程记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of thesis_process_record
-- ----------------------------
DROP TABLE IF EXISTS `time_slot_dict`;
CREATE TABLE `time_slot_dict`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `slot_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '时间片键名（唯一，如time_slot_1）',
  `slot_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '时间片值（如08:00-09:40）',
  `slot_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '时间片名称（如第1-2节）',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user` bigint NULL DEFAULT NULL COMMENT '更新人ID（关联user表）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_slot_key`(`slot_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '时间片字典表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of time_slot_dict
-- ----------------------------
INSERT INTO `time_slot_dict` VALUES (38, 'time_slot_1', '08:00-09:40', '第1-2节', '上午第1-2节课时间', '2026-04-22 20:34:54', NULL);
INSERT INTO `time_slot_dict` VALUES (39, 'time_slot_2', '10:00-11:40', '第3-4节', '上午第3-4节课时间', '2026-04-22 20:34:54', NULL);
INSERT INTO `time_slot_dict` VALUES (40, 'time_slot_3', '14:00-15:40', '第5-6节', '下午第5-6节课时间', '2026-04-22 20:34:54', NULL);
INSERT INTO `time_slot_dict` VALUES (41, 'time_slot_4', '16:00-17:40', '第7-8节', '下午第7-8节课时间', '2026-04-22 20:34:54', NULL);
INSERT INTO `time_slot_dict` VALUES (42, 'time_slot_5', '19:00-20:40', '第9-10节', '晚上第9-10节课时间', '2026-04-22 20:34:54', NULL);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（加密存储）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `gender` tinyint NULL DEFAULT NULL COMMENT '性别：1-男，2-女',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话',
  `role_id` int NOT NULL COMMENT '角色',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `signature` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电子签名图片路径',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像图片路径',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2200 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_id` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `role_id`(`role_id` ASC) USING BTREE,
  CONSTRAINT `user_role_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_role
-- ----------------------------

-- ============================================================
-- CLEAN TEST DATA (replaces original personal data)
-- ============================================================

-- Clean up existing test data
DELETE FROM `user_role`;
DELETE FROM `student`;
DELETE FROM `teacher`;
DELETE FROM `user`;

-- ============================================================
-- Test Users (passwords are hashed with the same algorithm as original)
-- password for all test accounts: "123456" (hashed)
-- ============================================================
INSERT INTO `user` VALUES (1, 'admin', 'spOYgNvEQxQByz7xbuoaWfx/mnDyzi5uHtm7AUligK4=', '超级管理员', 1, 'admin@test.com', '13800000001', 1, 1, '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, NULL);
INSERT INTO `user` VALUES (2, 'student', 'ZnWGZb4GJeXfckS8384DidqjmqmpnneB0HLmnbefmLQ=', '测试学生', 1, 'student@test.com', '13800000002', 6, 1, '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, NULL);
INSERT INTO `user` VALUES (3, 'teacher', '69bVNNUZVCZ1jxPW1eHeHps+hyio02v/KjSb7Z2uDz4=', '测试导师', 1, 'teacher@test.com', '13800000003', 7, 1, '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, NULL);
INSERT INTO `user` VALUES (4, 'teacher2', '69bVNNUZVCZ1jxPW1eHeHps+hyio02v/KjSb7Z2uDz4=', '测试导师2', 1, 'teacher2@test.com', '13800000004', 7, 1, '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, NULL);
INSERT INTO `user` VALUES (5, 'student2', 'ZnWGZb4GJeXfckS8384DidqjmqmpnneB0HLmnbefmLQ=', '测试学生2', 1, 'student2@test.com', '13800000005', 6, 1, '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, NULL);
INSERT INTO `user` VALUES (6, 'dean', 'zlZy6SP/BFnpqn/AJlfFeiLNNQtFHabN5jFUtXe5460=', '测试院长', NULL, 'dean@test.com', '13800000006', 2, 1, '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, NULL);
INSERT INTO `user` VALUES (7, 'chairman', 'd2iexKLbptcVSm4VDNbO+aLY4G/9miLTLUXU5fTORn4=', '测试主席', NULL, 'chairman@test.com', '13800000007', 3, 1, '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, NULL);
INSERT INTO `user` VALUES (8, 'admin2', 'admin2123', '测试管理员', NULL, 'admin2@test.com', '13800000008', 4, 1, '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, NULL);
INSERT INTO `user` VALUES (9, 'secretary', 'j0jobNjRAkkuOZDEO3d/MEMQuz+HphK6rq/86DO0W44=', '测试秘书', NULL, 'secretary@test.com', '13800000009', 5, 1, '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, NULL);
INSERT INTO `user` VALUES (10, 'teacher3', '69bVNNUZVCZ1jxPW1eHeHps+hyio02v/KjSb7Z2uDz4=', '测试导师3', 1, 'teacher3@test.com', '13800000010', 8, 1, '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, NULL);

-- ============================================================
-- User-Role Mappings
-- ============================================================
INSERT INTO `user_role` VALUES (1, 1, 1);
INSERT INTO `user_role` VALUES (2, 2, 6);
INSERT INTO `user_role` VALUES (3, 3, 7);
INSERT INTO `user_role` VALUES (4, 4, 7);
INSERT INTO `user_role` VALUES (5, 5, 6);
INSERT INTO `user_role` VALUES (6, 6, 2);
INSERT INTO `user_role` VALUES (7, 7, 3);
INSERT INTO `user_role` VALUES (8, 8, 4);
INSERT INTO `user_role` VALUES (9, 9, 5);
INSERT INTO `user_role` VALUES (10, 10, 8);

-- ============================================================
-- Test Classes (clean)
-- ============================================================
INSERT INTO `class` VALUES (1, '计科2023-1班', '计算机学院', '计算机科学与技术', 2023, '2025-09-01 00:00:00', '2025-09-01 00:00:00');
INSERT INTO `class` VALUES (2, '计科2023-2班', '计算机学院', '计算机科学与技术', 2023, '2025-09-01 00:00:00', '2025-09-01 00:00:00');
INSERT INTO `class` VALUES (3, '软件2023-1班', '计算机学院', '软件工程', 2023, '2025-09-01 00:00:00', '2025-09-01 00:00:00');

-- ============================================================
-- Test Students
-- ============================================================
INSERT INTO `student` VALUES (1, 2, 1, '20230001', '测试学生', '计算机学院', '计算机科学与技术', 2023, 2023, 2026, '深度学习', 0, 1, '2025-09-01 00:00:00', '2025-09-01 00:00:00');
INSERT INTO `student` VALUES (2, 5, 2, '20230002', '测试学生2', '计算机学院', '软件工程', 2023, 2023, 2026, '自然语言处理', 0, 1, '2025-09-01 00:00:00', '2025-09-01 00:00:00');

-- ============================================================
-- Test Teachers
-- ============================================================
INSERT INTO `teacher` VALUES (1, 3, '测试导师', 'T0001', '教授', '计算机学院', '人工智能与机器学习', 10, 10, 0, 1, '2025-09-01 00:00:00', '2025-09-01 00:00:00');
INSERT INTO `teacher` VALUES (2, 4, '测试导师2', 'T0002', '副教授', '计算机学院', '深度学习与计算机视觉', 8, 8, 0, 1, '2025-09-01 00:00:00', '2025-09-01 00:00:00');
INSERT INTO `teacher` VALUES (3, 10, '测试导师3', 'T0003', '副教授', '计算机学院', '软件工程', 0, 0, 0, 0, '2025-09-01 00:00:00', '2025-09-01 00:00:00');
