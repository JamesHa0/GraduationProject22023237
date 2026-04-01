-- 选课系统重构数据库升级脚本
-- 执行时间: 2026-04-01

-- 1. 修改 course_selection 表，添加新字段
ALTER TABLE course_selection
ADD COLUMN choice_order INT COMMENT '选课顺序（志愿号）' AFTER course_id,
ADD COLUMN submit_status INT DEFAULT 0 COMMENT '提交状态：0-草稿，1-已提交' AFTER choice_order;

-- 2. 添加系统配置项 student_max_courses（学生最大选课数量）
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark)
VALUES ('学生最大选课数量', 'student_max_courses', '3', 'Y', 'admin', NOW(), '', NULL, '学生一次最多可选择的课程数量');
