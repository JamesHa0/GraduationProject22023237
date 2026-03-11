package com.jameshao.gp22023237.controller.course;

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

    /**
     * 新增成绩
     */
    @PostMapping("/add")
    public JSONReturn add(@RequestBody Score score) {
        try {
            boolean success = scoreService.save(score);
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
     * 更新成绩
     */
    @PutMapping("/update")
    public JSONReturn update(@RequestBody Score score) {
        try {
            boolean success = scoreService.updateById(score);
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
     * 删除成绩
     */
    @DeleteMapping("/delete/{id}")
    public JSONReturn delete(@PathVariable Long id) {
        try {
            boolean success = scoreService.removeById(id);
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
     * 批量删除成绩
     */
    @DeleteMapping("/deleteBatch")
    public JSONReturn deleteBatch(@RequestBody List<Long> ids) {
        try {
            boolean success = scoreService.removeByIds(ids);
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
     * 查询成绩列表
     */
    @GetMapping("/list")
    public JSONReturn list() {
        try {
            List<Score> scores = scoreService.list();
            return jsonReturn.success(scores);
        } catch (Exception e) {
            return jsonReturn.fail(e.getMessage());
        }
    }

    /**
     * 查询成绩详情
     */
    @GetMapping("/{id}")
    public JSONReturn detail(@PathVariable Long id) {
        try {
            Score score = scoreService.getById(id);
            return jsonReturn.success(score);
        } catch (Exception e) {
            return jsonReturn.fail(e.getMessage());
        }
    }
}
