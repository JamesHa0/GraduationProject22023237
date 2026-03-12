package com.jameshao.gp22023237.controller.course;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.Score;
import com.jameshao.gp22023237.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course/score")
public class ScoreController {

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private ScoreService scoreService;

    @GetMapping("/list")
    public String list(Long studentId, Long courseId, String grade) {
        try {
            LambdaQueryWrapper<Score> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(!ObjectUtils.isEmpty(studentId), Score::getStudentId, studentId)
                    .eq(!ObjectUtils.isEmpty(courseId), Score::getCourseId, courseId)
                    .eq(!ObjectUtils.isEmpty(grade), Score::getGrade, grade)
                    .orderByDesc(Score::getUpdateTime);
            List<Score> list = scoreService.list(queryWrapper);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id) {
        try {
            Score score = scoreService.getById(id);
            return jsonReturn.returnSuccess(score);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/add")
    public String add(@RequestBody Score score) {
        try {
            boolean success = scoreService.save(score);
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
    public String update(@RequestBody Score score) {
        try {
            boolean success = scoreService.updateById(score);
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
            boolean success = scoreService.removeById(id);
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
            boolean success = scoreService.removeByIds(ids);
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
