-- ============================================================
-- 批量更新 student_status_change 表的 status 字段
-- 根据 mentor_approval 和 secretary_approval 的值重新计算
--
-- 状态定义：
--   status=0  待导师审批：导师未审批 (mentor_approval=0)
--   status=1  待教学秘书审批：导师已通过，秘书未审批 (mentor_approval=1, secretary_approval=0)
--   status=2  已批准：导师通过且秘书通过 (mentor_approval=1, secretary_approval=1)
--   status=3  已拒绝：任一级拒绝 (mentor_approval=2 OR secretary_approval=2)
-- ============================================================

-- 1. 已拒绝：任一级拒绝
UPDATE student_status_change
SET status = 3, update_time = NOW()
WHERE (mentor_approval = 2 OR secretary_approval = 2)
  AND status != 3;

-- 2. 已批准：导师通过且秘书通过
UPDATE student_status_change
SET status = 2, update_time = NOW()
WHERE mentor_approval = 1 AND secretary_approval = 1
  AND status != 2;

-- 3. 待教学秘书审批：导师已通过，秘书未审批
UPDATE student_status_change
SET status = 1, update_time = NOW()
WHERE mentor_approval = 1 AND (secretary_approval IS NULL OR secretary_approval = 0)
  AND status != 1;

-- 4. 待导师审批：导师未审批（排除以上情况后剩余的）
UPDATE student_status_change
SET status = 0, update_time = NOW()
WHERE mentor_approval = 0
  AND status != 0;

-- 验证结果
SELECT id, mentor_approval, secretary_approval, status,
       CASE
           WHEN mentor_approval = 2 OR secretary_approval = 2 THEN '已拒绝'
           WHEN mentor_approval = 1 AND secretary_approval = 1 THEN '已批准'
           WHEN mentor_approval = 1 AND (secretary_approval IS NULL OR secretary_approval = 0) THEN '待教学秘书审批'
           WHEN mentor_approval = 0 THEN '待导师审批'
           ELSE '未知'
       END AS status_label
FROM student_status_change
ORDER BY id;
