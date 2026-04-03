package com.jameshao.gp22023237.controller.course;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.jameshao.gp22023237.DTO.ScoreWithDetailsDTO;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.mapper.ScoreMapper;
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

    @Autowired
    private ScoreMapper scoreMapper;

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
            // 计算总成绩和等级
            calculateScore(score);
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
            // 计算总成绩和等级
            calculateScore(score);
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

    /**
     * 计算总成绩和等级
     */
    private void calculateScore(Score score) {
        // 设置默认权重
        if (score.getUsualWeight() == null) {
            score.setUsualWeight(0.3);
        }
        if (score.getExamWeight() == null) {
            score.setExamWeight(0.7);
        }

        // 计算总成绩
        if (score.getUsualScore() != null && score.getExamScore() != null) {
            Double totalScore = score.getUsualScore() * score.getUsualWeight()
                    + score.getExamScore() * score.getExamWeight();
            score.setTotalScore(Math.round(totalScore * 100.0) / 100.0); // 保留两位小数
            score.setScore(score.getTotalScore()); // 同时更新旧字段
        } else if (score.getExamScore() != null) {
            // 只有期末成绩
            score.setTotalScore(score.getExamScore());
            score.setScore(score.getExamScore());
        }

        // 计算等级
        if (score.getTotalScore() != null) {
            score.setGrade(calculateGrade(score.getTotalScore()));
        }
    }

    /**
     * 根据分数计算等级
     */
    private String calculateGrade(Double score) {
        if (score == null) {
            return null;
        }
        if (score >= 90) {
            return "A"; // 优秀
        } else if (score >= 80) {
            return "B"; // 良好
        } else if (score >= 70) {
            return "C"; // 中等
        } else if (score >= 60) {
            return "D"; // 及格
        } else {
            return "E"; // 不及格
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

    /**
     * 学生查询自己的成绩（带详情）
     */
    @GetMapping("/myScores")
    public String myScores(Long studentId, String grade, String semester) {
        try {
            if (studentId == null) {
                return jsonReturn.returnError("学生ID不能为空");
            }
            List<ScoreWithDetailsDTO> list = scoreMapper.listScoreWithDetails(studentId, null, grade, semester);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 查询成绩列表（带详情）- 管理员/教师使用
     */
    @GetMapping("/listWithDetails")
    public String listWithDetails(Long studentId, Long courseId, String grade, String semester) {
        try {
            List<ScoreWithDetailsDTO> list = scoreMapper.listScoreWithDetails(studentId, courseId, grade, semester);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
