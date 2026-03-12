package com.jameshao.gp22023237.controller.course;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
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

    @GetMapping("/list")
    public String list(String name, String courseNo, Integer status) {
        try {
            LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(!ObjectUtils.isEmpty(name), Course::getName, name)
                    .like(!ObjectUtils.isEmpty(courseNo), Course::getCourseNo, courseNo)
                    .eq(!ObjectUtils.isEmpty(status), Course::getStatus, status)
                    .orderByDesc(Course::getCreateTime);
            List<Course> list = courseService.list(queryWrapper);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

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

    @PostMapping("/add")
    public String add(@RequestBody Course course) {
        try {
            boolean success = courseService.save(course);
            if (success) {
                return jsonReturn.returnSuccess("新增成功");
            } else {
                return jsonReturn.returnFailed("新增失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PutMapping("/update")
    public String update(@RequestBody Course course) {
        try {
            boolean success = courseService.updateById(course);
            if (success) {
                return jsonReturn.returnSuccess("更新成功");
            } else {
                return jsonReturn.returnFailed("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        try {
            boolean success = courseService.removeById(id);
            if (success) {
                return jsonReturn.returnSuccess("删除成功");
            } else {
                return jsonReturn.returnFailed("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @DeleteMapping("/deleteBatch")
    public String deleteBatch(@RequestBody List<Long> ids) {
        try {
            boolean success = courseService.removeByIds(ids);
            if (success) {
                return jsonReturn.returnSuccess("删除成功");
            } else {
                return jsonReturn.returnFailed("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
