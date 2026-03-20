-- 分轮次导师选择机制 - 系统配置初始化脚本
-- 执行前请先备份数据库

-- 插入轮次相关配置
INSERT INTO system_config (config_key, config_name, config_value, config_type, remark, create_time, update_time) VALUES
('current_round', '当前轮次', '1', 'selection', '1=第一轮,2=第二轮,3=第三轮,4=补选', NOW(), NOW()),
('round_1_end_tutor', '第一轮导师确认截止时间', '', 'selection', '第一轮截止时间', NOW(), NOW()),
('round_2_end_tutor', '第二轮导师确认截止时间', '', 'selection', '第二轮截止时间', NOW(), NOW()),
('round_3_end_tutor', '第三轮导师确认截止时间', '', 'selection', '第三轮截止时间', NOW(), NOW()),
('supplementary_start', '补选开始时间', '', 'selection', '补选环节开始时间', NOW(), NOW()),
('supplementary_end', '补选结束时间', '', 'selection', '补选环节结束时间', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    update_time = NOW();
