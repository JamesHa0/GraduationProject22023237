package com.jameshao.gp22023237.controller.course;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.mapper.ScheduleMapper;
import com.jameshao.gp22023237.po.ClassEntity;
import com.jameshao.gp22023237.service.ClassService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 班级管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/class")
public class ClassController {

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private ClassService classService;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @GetMapping("/list")
    public String list(String className, String department, String major, Integer admissionYear,
                       Integer pageNum, Integer pageSize) {
        try {
            QueryWrapper<ClassEntity> wrapper = new QueryWrapper<>();
            if (className != null && !className.isEmpty()) {
                wrapper.like("class_name", className);
            }
            if (department != null && !department.isEmpty()) {
                wrapper.eq("department", department);
            }
            if (major != null && !major.isEmpty()) {
                wrapper.like("major", major);
            }
            if (admissionYear != null) {
                wrapper.eq("admission_year", admissionYear);
            }
            wrapper.orderByDesc("create_time");

            if (pageNum != null && pageSize != null) {
                int offset = (pageNum - 1) * pageSize;
                wrapper.last("LIMIT " + offset + ", " + pageSize);
                List<ClassEntity> rows = classService.list(wrapper);

                // 查询总数
                QueryWrapper<ClassEntity> countWrapper = new QueryWrapper<>();
                if (className != null && !className.isEmpty()) {
                    countWrapper.like("class_name", className);
                }
                if (department != null && !department.isEmpty()) {
                    countWrapper.eq("department", department);
                }
                if (major != null && !major.isEmpty()) {
                    countWrapper.like("major", major);
                }
                if (admissionYear != null) {
                    countWrapper.eq("admission_year", admissionYear);
                }
                long total = classService.count(countWrapper);

                // 附加学生人数
                List<Map<String, Object>> rowsWithCount = new java.util.ArrayList<>();
                for (ClassEntity entity : rows) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", entity.getId());
                    row.put("className", entity.getClassName());
                    row.put("department", entity.getDepartment());
                    row.put("major", entity.getMajor());
                    row.put("admissionYear", entity.getAdmissionYear());
                    row.put("createTime", entity.getCreateTime());
                    row.put("updateTime", entity.getUpdateTime());
                    row.put("studentCount", scheduleMapper.countStudentsByClassId(entity.getId()));
                    rowsWithCount.add(row);
                }

                Map<String, Object> data = new HashMap<>();
                data.put("rows", rowsWithCount);
                data.put("total", total);
                return jsonReturn.returnSuccess(data);
            } else {
                List<ClassEntity> list = classService.list(wrapper);
                return jsonReturn.returnSuccess(list);
            }
        } catch (Exception e) {
            log.error("查询班级列表失败", e);
            return jsonReturn.returnError("查询班级列表失败，请稍后重试");
        }
    }

    @GetMapping("/all")
    public String listAll() {
        try {
            List<ClassEntity> list = classService.list(new QueryWrapper<ClassEntity>().orderByAsc("class_name"));
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            log.error("查询所有班级失败", e);
            return jsonReturn.returnError("查询班级失败，请稍后重试");
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id) {
        try {
            ClassEntity entity = classService.getById(id);
            if (entity == null) {
                return jsonReturn.returnFailed("班级不存在");
            }
            Map<String, Object> data = new HashMap<>();
            data.put("id", entity.getId());
            data.put("className", entity.getClassName());
            data.put("department", entity.getDepartment());
            data.put("major", entity.getMajor());
            data.put("admissionYear", entity.getAdmissionYear());
            data.put("createTime", entity.getCreateTime());
            data.put("updateTime", entity.getUpdateTime());
            data.put("studentCount", scheduleMapper.countStudentsByClassId(id));
            return jsonReturn.returnSuccess(data);
        } catch (Exception e) {
            log.error("查询班级详情失败, id={}", id, e);
            return jsonReturn.returnError("查询班级详情失败，请稍后重试");
        }
    }

    @PostMapping("/add")
    public String add(@RequestBody ClassEntity entity) {
        try {
            entity.setCreateTime(new Date());
            entity.setUpdateTime(new Date());
            boolean success = classService.save(entity);
            if (success) {
                return jsonReturn.returnSuccess("新增成功");
            } else {
                return jsonReturn.returnFailed("新增失败");
            }
        } catch (Exception e) {
            log.error("新增班级失败", e);
            return jsonReturn.returnError("新增班级失败，请检查数据后重试");
        }
    }

    @PutMapping("/update")
    public String update(@RequestBody ClassEntity entity) {
        try {
            entity.setUpdateTime(new Date());
            boolean success = classService.updateById(entity);
            if (success) {
                return jsonReturn.returnSuccess("更新成功");
            } else {
                return jsonReturn.returnFailed("更新失败");
            }
        } catch (Exception e) {
            log.error("更新班级失败, id={}", entity.getId(), e);
            return jsonReturn.returnError("更新班级失败，请检查数据后重试");
        }
    }

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        try {
            int studentCount = scheduleMapper.countStudentsByClassId(id);
            if (studentCount > 0) {
                return jsonReturn.returnFailed("该班级下还有学生，无法删除");
            }
            boolean success = classService.removeById(id);
            if (success) {
                return jsonReturn.returnSuccess("删除成功");
            } else {
                return jsonReturn.returnFailed("删除失败");
            }
        } catch (Exception e) {
            log.error("删除班级失败, id={}", id, e);
            return jsonReturn.returnError("删除班级失败，请稍后重试");
        }
    }

    @DeleteMapping("/deleteBatch")
    public String deleteBatch(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                int studentCount = scheduleMapper.countStudentsByClassId(id);
                if (studentCount > 0) {
                    return jsonReturn.returnFailed("班级ID=" + id + "下还有学生，无法删除");
                }
            }
            boolean success = classService.removeByIds(ids);
            if (success) {
                return jsonReturn.returnSuccess("删除成功");
            } else {
                return jsonReturn.returnFailed("删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除班级失败, ids={}", ids, e);
            return jsonReturn.returnError("批量删除班级失败，请稍后重试");
        }
    }
}
