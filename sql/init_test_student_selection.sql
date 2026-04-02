-- 测试学生选课数据初始化脚本
-- 为测试学生创建选课记录，用于测试课程表显示功能

-- 说明：
-- 1. 假设 student 表中有学生ID为 1 的测试学生
-- 2. 假设 course 表中有课程ID为 1-6 的测试课程（来自 init_course_test_data.sql）
-- 3. 使用 INSERT ... ON DUPLICATE KEY UPDATE 确保可以重复执行

-- 为测试学生（ID=1）创建选课记录
INSERT INTO course_selection (student_id, course_id, selection_time, status, create_time, update_time)
VALUES
-- 选择3门课程用于测试课程表显示
(1, 1, NOW(), 1, NOW(), NOW()),   -- Java程序设计 (周一 08:00-09:40)
(1, 3, NOW(), 1, NOW(), NOW()),   -- 数据库原理 (周三 08:00-09:40)
(1, 5, NOW(), 1, NOW(), NOW())    -- 操作系统 (周五 08:00-09:40)
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    update_time = NOW();

-- 验证数据
SELECT '测试学生选课数据初始化完成！' AS message;
SELECT COUNT(*) AS total_selections FROM course_selection WHERE student_id = 1;
SELECT cs.id, cs.student_id, c.name, c.day_of_week, c.start_time, c.end_time, c.classroom
FROM course_selection cs
LEFT JOIN course c ON cs.course_id = c.id
WHERE cs.student_id = 1 AND cs.status = 1;
