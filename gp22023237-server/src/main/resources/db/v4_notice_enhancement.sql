-- =====================================================
-- v4_notice_enhancement.sql
-- 通知公告模块增强：新增阅读记录表 + 目标角色字段
-- =====================================================

-- 1. 新增公告阅读记录表
-- 外键加 ON DELETE CASCADE，删除通知时自动清理阅读记录
CREATE TABLE IF NOT EXISTS `sys_notice_read` (
    `read_id`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '阅读记录ID',
    `notice_id`  BIGINT       NOT NULL               COMMENT '公告ID',
    `user_id`    BIGINT       NOT NULL               COMMENT '用户ID',
    `read_time`  DATETIME                            COMMENT '阅读时间',
    PRIMARY KEY (`read_id`),
    UNIQUE KEY `uk_notice_user` (`notice_id`, `user_id`),
    INDEX `idx_user_id` (`user_id`),
    CONSTRAINT `fk_notice_read_notice` FOREIGN KEY (`notice_id`) REFERENCES `sys_notice`(`notice_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告阅读记录表';

-- 2. sys_notice 新增目标角色字段
-- 逗号分隔角色ID，NULL或空字符串表示全体可见
-- 角色ID对照：1=超管, 2=院长, 3=主席, 4=综合管理, 5=教学秘书, 6=学生, 7=导师, 8=授课教师
ALTER TABLE `sys_notice`
    ADD COLUMN `target_roles` VARCHAR(255) DEFAULT NULL COMMENT '目标角色（逗号分隔角色ID，空表示全体）' AFTER `status`;
