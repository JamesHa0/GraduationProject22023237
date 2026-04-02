-- 课程表测试数据初始化脚本
-- 执行时间: 2026-04-01
-- 注意：执行前请先备份数据库

-- 说明：
-- 1. 假设 teacher 表中已有教师数据，ID 从 1 开始
-- 2. 使用 INSERT ... ON DUPLICATE KEY UPDATE 确保可以重复执行
-- 3. 如果 course 表中已有数据，会更新空字段

-- 更新或插入测试课程数据
INSERT INTO course (course_no, name, credit, hours, teacher_id, semester, year, max_students, day_of_week, start_time, end_time, classroom, max_credits, status, description, create_time, update_time)
VALUES
-- 计算机类课程
('CS101', 'Java程序设计', 3.0, 48, 1, '2024-2025-2', 2024, 60, 1, '08:00', '09:40', 'A101', 6.0, 1, 'Java语言基础与面向对象编程', NOW(), NOW()),
('CS102', '数据结构', 4.0, 64, 1, '2024-2025-2', 2024, 50, 2, '10:00', '11:40', 'A102', 6.0, 1, '常用数据结构与算法分析', NOW(), NOW()),
('CS201', '数据库原理', 3.5, 56, 2, '2024-2025-2', 2024, 55, 3, '08:00', '09:40', 'A201', 6.0, 1, '关系数据库理论与SQL', NOW(), NOW()),
('CS202', '计算机网络', 3.0, 48, 2, '2024-2025-2', 2024, 45, 4, '14:00', '15:40', 'A202', 6.0, 1, 'TCP/IP协议与网络编程', NOW(), NOW()),
('CS301', '操作系统', 4.0, 64, 3, '2024-2025-2', 2024, 40, 5, '08:00', '09:40', 'A301', 6.0, 1, '操作系统原理与实现', NOW(), NOW()),
('CS302', '软件工程', 3.0, 48, 3, '2024-2025-2', 2024, 50, 1, '14:00', '15:40', 'A302', 6.0, 1, '软件开发过程与项目管理', NOW(), NOW()),

-- 数学类课程
('MATH101', '高等数学', 5.0, 80, 4, '2024-2025-2', 2024, 80, 2, '08:00', '09:40', 'B101', 6.0, 1, '微积分与解析几何', NOW(), NOW()),
('MATH102', '线性代数', 3.0, 48, 4, '2024-2025-2', 2024, 75, 3, '10:00', '11:40', 'B102', 6.0, 1, '矩阵理论与线性变换', NOW(), NOW()),
('MATH201', '概率论与数理统计', 3.5, 56, 5, '2024-2025-2', 2024, 70, 4, '08:00', '09:40', 'B201', 6.0, 1, '概率分布与统计推断', NOW(), NOW()),

-- 英语类课程
('ENG101', '大学英语', 2.0, 32, 6, '2024-2025-2', 2024, 90, 5, '10:00', '11:40', 'C101', 6.0, 1, '英语听说读写综合训练', NOW(), NOW()),
('ENG201', '学术英语写作', 2.0, 32, 6, '2024-2025-2', 2024, 60, 1, '16:00', '17:40', 'C102', 6.0, 1, '学术论文写作规范', NOW(), NOW()),

-- 物理类课程
('PHY101', '大学物理', 4.0, 64, 7, '2024-2025-2', 2024, 65, 2, '14:00', '15:40', 'D101', 6.0, 1, '力学、热学、电磁学', NOW(), NOW()),
('PHY201', '物理实验', 1.5, 24, 7, '2024-2025-2', 2024, 40, 6, '08:00', '11:00', 'D102', 6.0, 1, '基础物理实验', NOW(), NOW()),

-- 其他课程
('ELEC101', '电路分析', 3.0, 48, 8, '2024-2025-2', 2024, 55, 4, '16:00', '17:40', 'E101', 6.0, 1, '电路理论与分析方法', NOW(), NOW()),
('ECON101', '经济学基础', 2.5, 40, 9, '2024-2025-2', 2024, 80, 5, '14:00', '15:40', 'E201', 6.0, 1, '微观经济学与宏观经济学', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    credit = IFNULL(credit, VALUES(credit)),
    hours = IFNULL(hours, VALUES(hours)),
    teacher_id = IFNULL(teacher_id, VALUES(teacher_id)),
    semester = IFNULL(semester, VALUES(semester)),
    year = IFNULL(year, VALUES(year)),
    max_students = IFNULL(max_students, VALUES(max_students)),
    day_of_week = IFNULL(day_of_week, VALUES(day_of_week)),
    start_time = IFNULL(start_time, VALUES(start_time)),
    end_time = IFNULL(end_time, VALUES(end_time)),
    classroom = IFNULL(classroom, VALUES(classroom)),
    max_credits = IFNULL(max_credits, VALUES(max_credits)),
    status = IFNULL(status, VALUES(status)),
    description = IFNULL(description, VALUES(description)),
    update_time = NOW();

-- 如果需要单独更新已有课程的空字段，可以使用以下语句（取消注释执行）：
/*
UPDATE course SET
    credit = COALESCE(credit, 3.0),
    hours = COALESCE(hours, 48),
    teacher_id = COALESCE(teacher_id, 1),
    semester = COALESCE(semester, '2024-2025-2'),
    year = COALESCE(year, 2024),
    max_students = COALESCE(max_students, 60),
    day_of_week = COALESCE(day_of_week, 1),
    start_time = COALESCE(start_time, '08:00'),
    end_time = COALESCE(end_time, '09:40'),
    classroom = COALESCE(classroom, 'A101'),
    status = COALESCE(status, 1),
    update_time = NOW()
WHERE credit IS NULL
   OR hours IS NULL
   OR teacher_id IS NULL
   OR semester IS NULL
   OR year IS NULL
   OR max_students IS NULL
   OR day_of_week IS NULL
   OR start_time IS NULL
   OR end_time IS NULL
   OR classroom IS NULL
   OR status IS NULL;
*/

-- 验证数据
SELECT '课程数据初始化完成！' AS message;
SELECT COUNT(*) AS total_courses FROM course;
SELECT id, course_no, name, credit, teacher_id, status FROM course LIMIT 10;
