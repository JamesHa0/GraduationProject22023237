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
    public String add(@RequestBody CourseSelection courseSelection) {
        try {
            boolean success = courseSelectionService.save(courseSelection);
            if (success) {
                return jsonReturn.returnSuccess("选课成功");
            } else {
                return jsonReturn.returnError("选课失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 更新选课记录
     */
    @PutMapping("/update")
    public String update(@RequestBody CourseSelection courseSelection) {
        try {
            boolean success = courseSelectionService.updateById(courseSelection);
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
     * 删除选课记录
     */
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        try {
            boolean success = courseSelectionService.removeById(id);
            if (success) {
                return jsonReturn.returnSuccess("退课成功");
            } else {
                return jsonReturn.returnError("退课失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 批量删除选课记录
     */
    @DeleteMapping("/deleteBatch")
    public String deleteBatch(@RequestBody List<Long> ids) {
        try {
            boolean success = courseSelectionService.removeByIds(ids);
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
     * 查询选课记录列表
     */
    @GetMapping("/list")
    public String list() {
        try {
            List<CourseSelection> courseSelections = courseSelectionService.list();
            return jsonReturn.returnSuccess(courseSelections);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 查询选课记录详情
     */
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
}
