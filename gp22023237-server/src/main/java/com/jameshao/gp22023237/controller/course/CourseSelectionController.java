package com.jameshao.gp22023237.controller.course;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.CourseSelection;
import com.jameshao.gp22023237.service.CourseSelectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course/selection")
public class CourseSelectionController {

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private CourseSelectionService courseSelectionService;

    @GetMapping("/list")
    public String list(Long studentId, Long courseId, Integer status) {
        try {
            LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(!ObjectUtils.isEmpty(studentId), CourseSelection::getStudentId, studentId)
                    .eq(!ObjectUtils.isEmpty(courseId), CourseSelection::getCourseId, courseId)
                    .eq(!ObjectUtils.isEmpty(status), CourseSelection::getStatus, status)
                    .orderByDesc(CourseSelection::getSelectionTime);
            List<CourseSelection> list = courseSelectionService.list(queryWrapper);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id) {
        try {
            CourseSelection courseSelection = courseSelectionService.getById(id);
            return jsonReturn.returnSuccess(courseSelection);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/add")
    public String add(@RequestBody CourseSelection courseSelection) {
        try {
            boolean success = courseSelectionService.save(courseSelection);
            if (success) {
                return jsonReturn.returnSuccess("选课成功");
            } else {
                return jsonReturn.returnFailed("选课失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PutMapping("/update")
    public String update(@RequestBody CourseSelection courseSelection) {
        try {
            boolean success = courseSelectionService.updateById(courseSelection);
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
            boolean success = courseSelectionService.removeById(id);
            if (success) {
                return jsonReturn.returnSuccess("退课成功");
            } else {
                return jsonReturn.returnFailed("退课失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @DeleteMapping("/deleteBatch")
    public String deleteBatch(@RequestBody List<Long> ids) {
        try {
            boolean success = courseSelectionService.removeByIds(ids);
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
