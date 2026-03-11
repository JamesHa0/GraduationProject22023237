package com.jameshao.gp22023237.controller.course;

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

    /**
     * 新增选课记录
     */
    @PostMapping("/add")
    public JSONReturn add(@RequestBody CourseSelection courseSelection) {
        try {
            boolean success = courseSelectionService.save(courseSelection);
            if (success) {
                return jsonReturn.success("选课成功");
            } else {
                return jsonReturn.fail("选课失败");
            }
        } catch (Exception e) {
            return jsonReturn.fail(e.getMessage());
        }
    }

    /**
     * 更新选课记录
     */
    @PutMapping("/update")
    public JSONReturn update(@RequestBody CourseSelection courseSelection) {
        try {
            boolean success = courseSelectionService.updateById(courseSelection);
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
     * 删除选课记录
     */
    @DeleteMapping("/delete/{id}")
    public JSONReturn delete(@PathVariable Long id) {
        try {
            boolean success = courseSelectionService.removeById(id);
            if (success) {
                return jsonReturn.success("退课成功");
            } else {
                return jsonReturn.fail("退课失败");
            }
        } catch (Exception e) {
            return jsonReturn.fail(e.getMessage());
        }
    }

    /**
     * 批量删除选课记录
     */
    @DeleteMapping("/deleteBatch")
    public JSONReturn deleteBatch(@RequestBody List<Long> ids) {
        try {
            boolean success = courseSelectionService.removeByIds(ids);
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
     * 查询选课记录列表
     */
    @GetMapping("/list")
    public JSONReturn list() {
        try {
            List<CourseSelection> courseSelections = courseSelectionService.list();
            return jsonReturn.success(courseSelections);
        } catch (Exception e) {
            return jsonReturn.fail(e.getMessage());
        }
    }

    /**
     * 查询选课记录详情
     */
    @GetMapping("/{id}")
    public JSONReturn detail(@PathVariable Long id) {
        try {
            CourseSelection courseSelection = courseSelectionService.getById(id);
            return jsonReturn.success(courseSelection);
        } catch (Exception e) {
            return jsonReturn.fail(e.getMessage());
        }
    }
}
