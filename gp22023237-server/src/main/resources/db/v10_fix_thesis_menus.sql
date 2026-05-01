-- ============================================================
-- v10_fix_thesis_menus.sql
-- 1. 修复学位模块菜单：删除已废弃页面 + 新增缺失页面
-- 2. 新建流程配置相关数据表
--
-- 已删除页面：
--   /application → 学位申请页已合并到 approval
--
-- 新增页面：
--   512 学位审批 /approval
--   513 导师工作台 /workbench
--   514 流程配置 /process-config
-- ============================================================

-- ==================== 1. 删除已废弃菜单 ====================

-- 删除学位申请页面的 role_menu 权限记录
-- 先查找 /application 对应的 menus_index
DELETE FROM `role_menu` WHERE `menu_id` IN (
  SELECT `menus_index` FROM `menu` WHERE `path` = '/application'
);

-- 删除学位申请页面的 menu 记录
DELETE FROM `menu` WHERE `path` = '/application';

-- ==================== 2. 新增菜单 ====================

INSERT INTO `menu` (`id`, `menus_index`, `title`, `icon`, `path`, `sort`, `parent_id`) VALUES
(154, 512, '学位审批', 's-check', '/approval', 12, 5),
(155, 513, '导师工作台', 'checked', '/workbench', 13, 5),
(156, 514, '流程配置', 'setup', '/process-config', 14, 5);

-- ==================== 3. 角色权限分配 ====================

-- 512 学位审批：所有角色（学生提交申请 + 导师/管理层审批）
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES
(1, 512), (2, 512), (3, 512), (4, 512), (5, 512), (6, 512), (7, 512);

-- 513 导师工作台：导师(7) + 管理角色可查看
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES
(1, 513), (2, 513), (5, 513), (7, 513);

-- 514 流程配置：教学秘书(5) + 管理角色(1,2)
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES
(1, 514), (2, 514), (5, 514);

-- ==================== 4. 验证 ====================

SELECT '=== 学位模块菜单（parent_id=5）===' AS info;
SELECT `menus_index`, `title`, `path`, `sort`
FROM `menu`
WHERE `parent_id` = 5
ORDER BY `sort`;

SELECT '=== 新增菜单的角色权限 ===' AS info;
SELECT rm.`menu_id`, m.`title`, rm.`role_id`
FROM `role_menu` rm
INNER JOIN `menu` m ON m.`menus_index` = rm.`menu_id`
WHERE rm.`menu_id` IN (512, 513, 514)
ORDER BY rm.`menu_id`, rm.`role_id`;

-- ==================== 5. 新建流程环节配置表 ====================

CREATE TABLE IF NOT EXISTS `thesis_process_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_type` INT NOT NULL COMMENT '流程环节类型：1-开题，2-中期，3-预答辩，4-外审，5-正式答辩，6-二次答辩，7-修改后再审',
  `process_name` VARCHAR(64) NOT NULL COMMENT '环节名称',
  `enabled` INT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `deadline` DATETIME DEFAULT NULL COMMENT '截止时间',
  `need_supervisor_approval` INT DEFAULT 1 COMMENT '是否需要导师审批：0-否，1-是',
  `need_secretary_approval` INT DEFAULT 0 COMMENT '是否需要秘书审批：0-否，1-是',
  `need_dean_approval` INT DEFAULT 0 COMMENT '是否需要院长审批：0-否，1-是',
  `need_review_result` INT DEFAULT 0 COMMENT '是否需要录入评审结果：0-否，1-是',
  `sort` INT DEFAULT 0 COMMENT '排序号',
  `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_process_type` (`process_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程环节配置表';

-- ==================== 6. 初始化学位流程全局配置（合并到 system_config 表）====================

-- config_type = 'thesis' 用于区分学位流程配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_type`, `remark`) VALUES
('thesis_overdue_reminder', 'true', '逾期自动提醒', 'thesis', '学位流程-逾期自动提醒开关'),
('thesis_overdue_days', '7', '逾期天数阈值', 'thesis', '学位流程-逾期天数阈值'),
('thesis_serial_mode', 'true', '流程串行模式', 'thesis', '严格顺序模式下前序环节未通过不可提交后续环节'),
('thesis_defense_eligibility_check', 'true', '答辩资格检查', 'thesis', '开启后学生必须通过外审才能申请答辩'),
('thesis_max_resubmit_count', '3', '最大重新提交次数', 'thesis', '学位流程-最大重新提交次数'),
('thesis_min_words', '30000', '论文字数下限要求', 'thesis', '学位论文最低字数要求'),
('thesis_review_expert_count', '3', '外审专家人数', 'thesis', '学位论文外审专家人数'),
('thesis_defense_pass_score', '60', '答辩及格分数线', 'thesis', '学位答辩及格分数线')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);

-- ==================== 7. 初始化流程环节配置数据 ====================

INSERT INTO `thesis_process_config` (`process_type`, `process_name`, `enabled`, `need_supervisor_approval`, `need_secretary_approval`, `need_dean_approval`, `need_review_result`, `sort`) VALUES
(1, '开题报告', 1, 1, 1, 0, 0, 1),
(2, '中期考核', 1, 1, 1, 0, 0, 2),
(3, '预答辩', 1, 1, 0, 0, 1, 3),
(4, '外审', 1, 0, 1, 0, 1, 4),
(5, '正式答辩', 1, 0, 1, 1, 1, 5),
(6, '二次答辩', 0, 0, 1, 1, 1, 6),
(7, '修改后再审', 0, 1, 1, 0, 1, 7);
