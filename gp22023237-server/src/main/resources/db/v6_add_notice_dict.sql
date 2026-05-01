-- ============================================================
-- v6_add_notice_dict.sql
-- 通知公告模块字典数据：新增 sys_notice_type 和 sys_notice_status
-- 修复公告类型/状态下拉框无数据的问题
-- ============================================================

-- 1. 新增字典类型：通知类型
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (13, '通知类型', 'sys_notice_type', '0', 'admin', NOW(), '', NULL, '通知类型列表')
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`);

-- 2. 新增字典类型：通知状态
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (14, '通知状态', 'sys_notice_status', '0', 'admin', NOW(), '', NULL, '通知状态列表')
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`);

-- 3. 新增字典数据：通知类型
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (57, 1, '通知', '1', 'sys_notice_type', NULL, 'primary', 'Y', '0', 'admin', NOW(), '', NULL, '通知')
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (58, 2, '公告', '2', 'sys_notice_type', NULL, 'success', 'N', '0', 'admin', NOW(), '', NULL, '公告')
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

-- 4. 新增字典数据：通知状态
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (59, 1, '正常', '0', 'sys_notice_status', NULL, 'primary', 'Y', '0', 'admin', NOW(), '', NULL, '正常')
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (60, 2, '关闭', '1', 'sys_notice_status', NULL, 'danger', 'N', '0', 'admin', NOW(), '', NULL, '关闭')
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`);
