-- =============================================
-- 更新 menu 表的 path 字段，使其与前端路由匹配
-- =============================================

-- 双选管理子菜单
UPDATE menu SET path = '/student/selection/choices' WHERE menus_index = 101;
UPDATE menu SET path = '/student/selection/results' WHERE menus_index = 102;
UPDATE menu SET path = '/mentor/selection/applicants' WHERE menus_index = 103;
UPDATE menu SET path = '/mentor/selection/confirmed' WHERE menus_index = 104;

-- 学籍管理子菜单
UPDATE menu SET path = '/student/info' WHERE menus_index = 201;
UPDATE menu SET path = '/student/change' WHERE menus_index = 202;
UPDATE menu SET path = '/student/approval' WHERE menus_index = 203;
UPDATE menu SET path = '/student/graduation' WHERE menus_index = 204;

-- 课程管理子菜单
UPDATE menu SET path = '/course/course' WHERE menus_index = 301;
UPDATE menu SET path = '/course/selection' WHERE menus_index = 302;
UPDATE menu SET path = '/course/score' WHERE menus_index = 303;
UPDATE menu SET path = '/course/student-select' WHERE menus_index = 304;
UPDATE menu SET path = '/course/select-result' WHERE menus_index = 305;
UPDATE menu SET path = '/course/schedule' WHERE menus_index = 306;

-- 学术管理子菜单
UPDATE menu SET path = '/academic/activity' WHERE menus_index = 401;
UPDATE menu SET path = '/academic/innovation' WHERE menus_index = 402;
UPDATE menu SET path = '/academic/achievement' WHERE menus_index = 403;
UPDATE menu SET path = '/academic/review' WHERE menus_index = 404;

-- 学位管理子菜单
UPDATE menu SET path = '/degree/proposal' WHERE menus_index = 501;
UPDATE menu SET path = '/degree/midterm' WHERE menus_index = 502;
UPDATE menu SET path = '/degree/predefense' WHERE menus_index = 503;
UPDATE menu SET path = '/degree/application' WHERE menus_index = 504;
UPDATE menu SET path = '/degree/approval' WHERE menus_index = 505;

-- 系统管理子菜单
UPDATE menu SET path = '/system/role' WHERE menus_index = 601;
UPDATE menu SET path = '/system/menu' WHERE menus_index = 602;
UPDATE menu SET path = '/system/user' WHERE menus_index = 603;

-- 验证更新
SELECT 'Menu paths updated successfully!' AS message;
SELECT menus_index, title, path, parent_id FROM menu ORDER BY menus_index;
