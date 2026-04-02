-- 选课系统重构数据库升级脚本
-- 执行时间: 2026-04-01
-- 注意：执行前请先备份数据库

-- 1. 修改 course_selection 表，添加新字段（已注释，如需要请取消注释）
-- ALTER TABLE course_selection
-- ADD COLUMN choice_order INT COMMENT '选课顺序（志愿号）' AFTER course_id,
-- ADD COLUMN submit_status INT DEFAULT 0 COMMENT '提交状态：0-草稿，1-已提交' AFTER choice_order;

-- 2. 添加系统配置项 student_max_courses（学生最大选课数量）
INSERT INTO system_config (config_key, config_name, config_value, config_type, remark, create_time, update_time)
VALUES ('student_max_courses', '学生最大选课数量', '3', 'course', '学生一次最多可选择的课程数量', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    update_time = NOW();

-- 3. 移除 choice_order 字段（如需要请执行）
-- ALTER TABLE course_selection DROP COLUMN choice_order;
