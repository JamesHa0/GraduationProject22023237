-- =============================================
-- Menu、Role、RoleMenu 表重构脚本
-- 按开题报告重新设计菜单体系和权限配置
-- =============================================

-- 设置字符集
SET NAMES utf8mb4;

-- 关闭外键检查（避免删除数据时报错）
SET FOREIGN_KEY_CHECKS = 0;

-- 清空现有数据
DELETE FROM role_menu;
DELETE FROM menu;

-- 开启外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 插入一级菜单（parent_id = 0）
-- =============================================
INSERT INTO `menu` (`menus_index`, `title`, `icon`, `path`, `parent_id`) VALUES
(1, '双选管理', 'tree', '/selection', 0),
(2, '学籍管理', 'user', '/student', 0),
(3, '课程管理', 'education', '/course', 0),
(4, '学术管理', 'star', '/academic', 0),
(5, '学位管理', 'date', '/degree', 0),
(6, '系统管理', 'setting', '/system', 0);

-- =============================================
-- 插入二级菜单
-- =============================================
INSERT INTO `menu` (`menus_index`, `title`, `icon`, `path`, `parent_id`) VALUES
-- 双选管理子菜单
(101, '学生选导师', 'tree', '/selection/student-choose', 1),
(102, '双选结果查询', 'tree', '/selection/results', 1),
(103, '导师选学生', 'tree', '/selection/mentor-choose', 1),
(104, '确认学生', 'tree', '/selection/confirm', 1),

-- 学籍管理子菜单
(201, '学生信息维护', 'user', '/student/info', 2),
(202, '学籍异动管理', 'user', '/student/change', 2),
(203, '学籍异动审核', 'user', '/student/approval', 2),
(204, '毕业资格审核', 'user', '/student/graduation', 2),

-- 课程管理子菜单
(301, '课程信息管理', 'book', '/course/info', 3),
(302, '选课管理', 'book', '/course/selection', 3),
(303, '成绩管理', 'book', '/course/score', 3),
(304, '学生选课', 'book', '/course/student-select', 3),
(305, '选课结果', 'book', '/course/select-result', 3),
(306, '课程表查询', 'book', '/course/schedule', 3),

-- 学术管理子菜单
(401, '学术活动登记', 'star', '/academic/activity', 4),
(402, '创新实践项目', 'star', '/academic/innovation', 4),
(403, '学术成果登记', 'star', '/academic/achievement', 4),
(404, '学术内容审核', 'star', '/academic/review', 4),

-- 学位管理子菜单
(501, '开题报告', 'date', '/degree/proposal', 5),
(502, '论文中期检查', 'date', '/degree/midterm', 5),
(503, '论文预答辩', 'date', '/degree/predefense', 5),
(504, '学位申请', 'date', '/degree/application', 5),
(505, '学位授予', 'date', '/degree/approval', 5),

-- 系统管理子菜单
(601, '角色管理', 'setting', '/system/role', 6),
(602, '菜单管理', 'setting', '/system/menu', 6),
(603, '用户管理', 'setting', '/system/user', 6);

-- =============================================
-- 配置角色菜单权限（role_menu）
-- 注意：同时添加一级菜单和子菜单
-- =============================================

-- 角色1：超级管理员 - 全部菜单
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES
-- 一级菜单
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6),
-- 二级菜单
(1, 101), (1, 102), (1, 103), (1, 104),
(1, 201), (1, 202), (1, 203), (1, 204),
(1, 301), (1, 302), (1, 303), (1, 304), (1, 305), (1, 306),
(1, 401), (1, 402), (1, 403), (1, 404),
(1, 501), (1, 502), (1, 503), (1, 504), (1, 505),
(1, 601), (1, 602), (1, 603);

-- 角色2：分管院长 - 学术管理审核、学位管理
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES
-- 一级菜单
(2, 4), (2, 5),
-- 二级菜单
(2, 404),
(2, 501), (2, 502), (2, 503), (2, 504), (2, 505);

-- 角色3：学位分委员会主席 - 学位管理
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES
-- 一级菜单
(3, 5),
-- 二级菜单
(3, 501), (3, 502), (3, 503), (3, 504), (3, 505);

-- 角色4：综合管理员 - 全部菜单
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES
-- 一级菜单
(4, 1), (4, 2), (4, 3), (4, 4), (4, 5), (4, 6),
-- 二级菜单
(4, 101), (4, 102), (4, 103), (4, 104),
(4, 201), (4, 202), (4, 203), (4, 204),
(4, 301), (4, 302), (4, 303), (4, 304), (4, 305), (4, 306),
(4, 401), (4, 402), (4, 403), (4, 404),
(4, 501), (4, 502), (4, 503), (4, 504), (4, 505),
(4, 601), (4, 602), (4, 603);

-- 角色5：研究生秘书 - 学籍管理、课程管理、学术审核、学位审核
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES
-- 一级菜单
(5, 2), (5, 3), (5, 4), (5, 5),
-- 二级菜单
(5, 201), (5, 202), (5, 203), (5, 204),
(5, 301), (5, 302), (5, 303), (5, 304),
(5, 404),
(5, 501), (5, 502), (5, 503), (5, 504);

-- 角色6：学生 - 学生端菜单
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES
-- 一级菜单
(6, 1), (6, 2), (6, 3), (6, 4), (6, 5),
-- 二级菜单
(6, 101), (6, 102),
(6, 201), (6, 202),
(6, 304), (6, 305), (6, 306),
(6, 401), (6, 402), (6, 403),
(6, 501), (6, 502), (6, 503), (6, 504);

-- 角色7：老师/导师 - 导师端菜单
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES
-- 一级菜单
(7, 1), (7, 3), (7, 4), (7, 5),
-- 二级菜单
(7, 103), (7, 104),
(7, 303),
(7, 404),
(7, 501), (7, 502), (7, 503), (7, 504);

-- 角色8：任课教师 - 课程成绩相关
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES
-- 一级菜单
(8, 3),
-- 二级菜单
(8, 301), (8, 303);

-- =============================================
-- 验证数据
-- =============================================
SELECT 'Menu data inserted successfully!' AS message;
SELECT COUNT(*) AS menu_count FROM menu;
SELECT 'Role_menu data inserted successfully!' AS message;
SELECT COUNT(*) AS role_menu_count FROM role_menu;
