-- 学术模块：为三张表添加附件路径字段（如已存在则可安全跳过）
-- 执行前提：已连接到 gp22023237 数据库

-- 学术活动表
ALTER TABLE `academic_activity`
  ADD COLUMN IF NOT EXISTS `attachment_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件路径（证明材料，逗号分隔多URL）' AFTER `dean_comment`;

-- 创新创业项目表
ALTER TABLE `innovation_project`
  ADD COLUMN IF NOT EXISTS `attachment_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件路径（逗号分隔多URL）' AFTER `dean_comment`;

-- 学术成果表
ALTER TABLE `academic_achievement`
  ADD COLUMN IF NOT EXISTS `attachment_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件路径（证明材料，逗号分隔多URL）' AFTER `dean_comment`;
