-- 为course表添加新字段
-- 执行时间: 2026-04-02

-- 添加修读性质字段
ALTER TABLE course ADD COLUMN study_nature VARCHAR(50) COMMENT '修读性质' AFTER description;

-- 添加教材字段（1-是，0-否）
ALTER TABLE course ADD COLUMN textbook TINYINT DEFAULT 0 COMMENT '教材：1-是，0-否' AFTER study_nature;

-- 添加外年级/专业选课字段（1-是，0-否）
ALTER TABLE course ADD COLUMN external_selection TINYINT DEFAULT 0 COMMENT '外年级/专业选课：1-是，0-否' AFTER textbook;

-- 添加备注字段
ALTER TABLE course ADD COLUMN remark VARCHAR(500) COMMENT '备注' AFTER external_selection;

-- 更新测试数据（为现有课程设置默认值）
UPDATE course SET
    study_nature = COALESCE(study_nature, '必修'),
    textbook = COALESCE(textbook, 1),
    external_selection = COALESCE(external_selection, 0),
    remark = COALESCE(remark, ''),
    update_time = NOW()
WHERE study_nature IS NULL
   OR textbook IS NULL
   OR external_selection IS NULL
   OR remark IS NULL;

-- 验证字段添加成功
SELECT 'course表字段添加完成！' AS message;
DESCRIBE course;

-- 查看更新后的数据
SELECT id, course_no, name, study_nature, textbook, external_selection, remark
FROM course
LIMIT 10;
