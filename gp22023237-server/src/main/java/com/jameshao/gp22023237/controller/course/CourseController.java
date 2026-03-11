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
    public String add(@RequestBody Course course) {
        try {
            boolean success = courseService.save(course);
            if (success) {
                return jsonReturn.returnSuccess("新增成功");
            } else {
                return jsonReturn.returnError("新增失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 更新课程
     */
    @PutMapping("/update")
    public String update(@RequestBody Course course) {
        try {
            boolean success = courseService.updateById(course);
            if (success) {
                return jsonReturn.returnSuccess("更新成功");
            } else {
                return jsonReturn.returnError("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        try {
            boolean success = courseService.removeById(id);
            if (success) {
                return jsonReturn.returnSuccess("删除成功");
            } else {
                return jsonReturn.returnError("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 批量删除课程
     */
    @DeleteMapping("/deleteBatch")
    public String deleteBatch(@RequestBody List<Long> ids) {
        try {
            boolean success = courseService.removeByIds(ids);
            if (success) {
                return jsonReturn.returnSuccess("删除成功");
            } else {
                return jsonReturn.returnError("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 查询课程列表
     */
    @GetMapping("/list")
    public String list() {
        try {
            List<Course> courses = courseService.list();
            return jsonReturn.returnSuccess(courses);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 查询课程详情
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id) {
        try {
            Course course = courseService.getById(id);
            return jsonReturn.returnSuccess(course);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
