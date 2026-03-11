package com.jameshao.gp22023237.controller.course;

import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.Course;
import com.jameshao.gp22023237.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private CourseService courseService;

    /**
     * 新增课程
     */
    @PostMapping("/add")
    public JSONReturn add(@RequestBody Course course) {
        try {
            boolean success = courseService.save(course);
            if (success) {
                return jsonReturn.success("新增成功");
            } else {
                return jsonReturn.fail("新增失败");
            }
        } catch (Exception e) {
            return jsonReturn.fail(e.getMessage());
        }
    }

    /**
     * 更新课程
     */
    @PutMapping("/update")
    public JSONReturn update(@RequestBody Course course) {
        try {
            boolean success = courseService.updateById(course);
            if (success) {
                return jsonReturn.success("更新成功");
            } else {
                return jsonReturn.fail("更新失败");
            }
        } catch (Exception e) {
            return jsonReturn.fail(e.getMessage());
        }
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/delete/{id}")
    public JSONReturn delete(@PathVariable Long id) {
        try {
            boolean success = courseService.removeById(id);
            if (success) {
                return jsonReturn.success("删除成功");
            } else {
                return jsonReturn.fail("删除失败");
            }
        } catch (Exception e) {
            return jsonReturn.fail(e.getMessage());
        }
    }

    /**
     * 批量删除课程
     */
    @DeleteMapping("/deleteBatch")
    public JSONReturn deleteBatch(@RequestBody List<Long> ids) {
        try {
            boolean success = courseService.removeByIds(ids);
            if (success) {
                return jsonReturn.success("删除成功");
            } else {
                return jsonReturn.fail("删除失败");
            }
        } catch (Exception e) {
            return jsonReturn.fail(e.getMessage());
        }
    }

    /**
     * 查询课程列表
     */
    @GetMapping("/list")
    public JSONReturn list() {
        try {
            List<Course> courses = courseService.list();
            return jsonReturn.success(courses);
        } catch (Exception e) {
            return jsonReturn.fail(e.getMessage());
        }
    }

    /**
     * 查询课程详情
     */
    @GetMapping("/{id}")
    public JSONReturn detail(@PathVariable Long id) {
        try {
            Course course = courseService.getById(id);
            return jsonReturn.success(course);
        } catch (Exception e) {
            return jsonReturn.fail(e.getMessage());
        }
    }
}
