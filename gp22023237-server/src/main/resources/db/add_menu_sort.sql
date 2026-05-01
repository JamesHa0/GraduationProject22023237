-- ============================================================
-- 菜单表新增排序字段
-- 将排序功能从 menus_index 解耦到独立的 sort 字段
-- ============================================================

-- 1. 新增 sort 字段
ALTER TABLE menu ADD COLUMN sort INT DEFAULT 0 COMMENT '排序';

-- 2. 用现有 menus_index 值初始化 sort，保持原有排序不变
UPDATE menu SET sort = menus_index;
