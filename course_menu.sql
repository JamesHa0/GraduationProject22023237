-- 课程管理模块菜单配置
-- 执行此脚本后，需要重启后端服务以使菜单生效

-- 课程管理父菜单
INSERT INTO `menu` (`menus_index`, `title`, `icon`, `path`, `parent_id`) 
VALUES ('3', '课程管理', 'education', '/course', 0);

-- 获取课程管理父菜单的ID
SET @course_parent_id = LAST_INSERT_ID();

-- 课程管理子菜单
INSERT INTO `menu` (`menus_index`, `title`, `icon`, `path`, `parent_id`) 
VALUES ('3-1', '课程信息管理', 'book', '/course/course', @course_parent_id);

-- 选课管理子菜单
INSERT INTO `menu` (`menus_index`, `title`, `icon`, `path`, `parent_id`) 
VALUES ('3-2', '选课管理', 'list', '/course/selection', @course_parent_id);

-- 成绩管理子菜单
INSERT INTO `menu` (`menus_index`, `title`, `icon`, `path`, `parent_id`) 
VALUES ('3-3', '成绩管理', 'score', '/course/score', @course_parent_id);
