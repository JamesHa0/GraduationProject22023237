-- ============================================================
-- v5_add_notice_menu.sql
-- 通知公告菜单权限配置
-- 在系统管理下新增"通知公告"子菜单，并为相关角色分配权限
-- ============================================================

-- 1. 新增通知公告菜单（系统管理的子菜单）
-- id=147, menus_index=608, title=通知公告, icon=message, path=/notice, parent_id=6(系统管理), sort=8
INSERT INTO `menu` (`id`, `menus_index`, `title`, `icon`, `path`, `parent_id`, `sort`)
VALUES (147, 608, '通知公告', 'message', '/notice', 6, 8)
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`);

-- 2. 为角色分配通知公告菜单权限
-- 角色1=超级管理员、角色4=综合管理员 拥有通知公告管理权限
INSERT IGNORE INTO `role_menu` (`role_id`, `menu_id`) VALUES (1, 608);
INSERT IGNORE INTO `role_menu` (`role_id`, `menu_id`) VALUES (4, 608);
