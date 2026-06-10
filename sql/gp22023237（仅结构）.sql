/*
 Navicat Premium Data Transfer

 Source Server         : 毕设
 Source Server Type    : MySQL
 Source Server Version : 80030 (8.0.30-txsql)
 Source Host           : sh-cdb-40r5oaz2.sql.tencentcdb.com:28194
 Source Schema         : gp22023237

 Target Server Type    : MySQL
 Target Server Version : 80030 (8.0.30-txsql)
 File Encoding         : 65001

 Date: 22/05/2026 14:12:41
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for academic_achievement_detail
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
-- Table structure for academic_approval_record
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
-- Table structure for academic_innovation_detail
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
-- Table structure for academic_submission
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

-- [CLEANUP] counselor 表已删除（空表，无代码引用）

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
-- Table structure for course_selection
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
-- Table structure for degree_application
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

-- [CLEANUP] electronic_record 表已删除（空表，无代码引用）

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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '导师更换申请表' ROW_FORMAT = DYNAMIC;

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
-- Table structure for menu
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

-- [CLEANUP] professional_practice 表已删除（空表，无代码引用）

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
) ENGINE = InnoDB AUTO_INCREMENT = 1638 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '角色菜单表' ROW_FORMAT = DYNAMIC;

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
-- Table structure for score
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
-- Table structure for student
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
-- Table structure for student_status_change
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
  INDEX `fk_status_change_secretary`(`secretary_id` ASC) USING BTREE,
  CONSTRAINT `fk_status_change_secretary` FOREIGN KEY (`secretary_id`) REFERENCES `teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_status_change_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_status_change_tutor` FOREIGN KEY (`mentor_id`) REFERENCES `teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学籍异动表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_dict_data
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
-- Table structure for sys_notice_read
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
-- Table structure for sys_oper_log
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
) ENGINE = InnoDB AUTO_INCREMENT = 468 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作日志记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for system_config
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
-- Table structure for teaching_evaluation
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
-- Table structure for thesis_grade
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
-- Table structure for thesis_main
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
-- Table structure for thesis_process_config
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
-- Table structure for time_slot_dict
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
-- Table structure for user_role
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

SET FOREIGN_KEY_CHECKS = 1;
