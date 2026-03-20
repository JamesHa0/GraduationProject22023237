-- =============================================
-- 恢复 menu 表的 path 字段到原来的短路径
-- =============================================

-- 一级菜单
UPDATE menu SET path = '/selection' WHERE menus_index = 1;
UPDATE menu SET path = '/student' WHERE menus_index = 2;
UPDATE menu SET path = '/course' WHERE menus_index = 3;
UPDATE menu SET path = '/academic' WHERE menus_index = 4;
UPDATE menu SET path = '/degree' WHERE menus_index = 5;
UPDATE menu SET path = '/system' WHERE menus_index = 6;

-- 双选管理子菜单
UPDATE menu SET path = '/student-choose' WHERE menus_index = 101;
UPDATE menu SET path = '/results' WHERE menus_index = 102;
UPDATE menu SET path = '/mentor-choose' WHERE menus_index = 103;
UPDATE menu SET path = '/confirm' WHERE menus_index = 104;

-- 学籍管理子菜单
UPDATE menu SET path = '/info' WHERE menus_index = 201;
UPDATE menu SET path = '/change' WHERE menus_index = 202;
UPDATE menu SET path = '/approval' WHERE menus_index = 203;
UPDATE menu SET path = '/graduation' WHERE menus_index = 204;

-- 课程管理子菜单
UPDATE menu SET path = '/info' WHERE menus_index = 301;
UPDATE menu SET path = '/selection' WHERE menus_index = 302;
UPDATE menu SET path = '/score' WHERE menus_index = 303;
UPDATE menu SET path = '/student-select' WHERE menus_index = 304;
UPDATE menu SET path = '/select-result' WHERE menus_index = 305;
UPDATE menu SET path = '/schedule' WHERE menus_index = 306;

-- 学术管理子菜单
UPDATE menu SET path = '/activity' WHERE menus_index = 401;
UPDATE menu SET path = '/innovation' WHERE menus_index = 402;
UPDATE menu SET path = '/achievement' WHERE menus_index = 403;
UPDATE menu SET path = '/review' WHERE menus_index = 404;

-- 学位管理子菜单
UPDATE menu SET path = '/proposal' WHERE menus_index = 501;
UPDATE menu SET path = '/midterm' WHERE menus_index = 502;
UPDATE menu SET path = '/predefense' WHERE menus_index = 503;
UPDATE menu SET path = '/application' WHERE menus_index = 504;
UPDATE menu SET path = '/approval' WHERE menus_index = 505;

-- 系统管理子菜单
UPDATE menu SET path = '/role' WHERE menus_index = 601;
UPDATE menu SET path = '/menu' WHERE menus_index = 602;
UPDATE menu SET path = '/user' WHERE menus_index = 603;

-- 验证更新
SELECT 'Menu paths restored successfully!' AS message;
SELECT menus_index, title, path, parent_id FROM menu ORDER BY menus_index;
