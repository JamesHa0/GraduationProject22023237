package com.jameshao.gp22023237.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.DTO.ScoreImportDTO;
import com.jameshao.gp22023237.DTO.ScoreImportResultDTO;
import com.jameshao.gp22023237.mapper.ScoreMapper;
import com.jameshao.gp22023237.mapper.StudentMapper;
import com.jameshao.gp22023237.po.Score;
import com.jameshao.gp22023237.po.Student;
import com.jameshao.gp22023237.service.NoticeService;
import com.jameshao.gp22023237.service.ScoreService;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.TeachingEvaluationService;
import com.jameshao.gp22023237.utils.ScoreImportListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author test
* @description 针对表【score(成绩表)】的数据库操作Service实现
* @createDate 2025-10-08 17:09:36
*/
@Slf4j
@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score>
    implements ScoreService{

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeachingEvaluationService evaluationService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private StudentService studentService;

    @Override
    public void calculateScore(Score score) {
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
            score.setTotalScore(Math.round(totalScore * 100.0) / 100.0);
            score.setScore(score.getTotalScore());
        } else if (score.getExamScore() != null) {
            score.setTotalScore(score.getExamScore());
            score.setScore(score.getExamScore());
        } else if (score.getUsualScore() != null) {
            score.setTotalScore(score.getUsualScore());
            score.setScore(score.getUsualScore());
        }

        // 计算等级
        if (score.getTotalScore() != null) {
            score.setGrade(calculateGrade(score.getTotalScore()));
        }

        // 更新时间
        score.setUpdateTime(new Date());
    }

    @Override
    public String calculateGrade(Double totalScore) {
        if (totalScore == null) {
            return null;
        }
        if (totalScore >= 90) {
            return "A";
        } else if (totalScore >= 80) {
            return "B";
        } else if (totalScore >= 70) {
            return "C";
        } else if (totalScore >= 60) {
            return "D";
        } else {
            return "E";
        }
    }

    @Override
    public String validateScore(Score score) {
        // 校验分数范围
        if (score.getUsualScore() != null && (score.getUsualScore() < 0 || score.getUsualScore() > 100)) {
            return "平时成绩必须在0-100之间";
        }
        if (score.getExamScore() != null && (score.getExamScore() < 0 || score.getExamScore() > 100)) {
            return "期末成绩必须在0-100之间";
        }

        // 校验权重
        if (score.getUsualWeight() != null && score.getExamWeight() != null) {
            double weightSum = score.getUsualWeight() + score.getExamWeight();
            if (Math.abs(weightSum - 1.0) > 0.01) {
                return "平时权重与期末权重之和必须为1";
            }
        }

        // 校验必要字段
        if (score.getStudentId() == null) {
            return "学生ID不能为空";
        }
        if (score.getCourseId() == null) {
            return "课程ID不能为空";
        }

        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> batchUpdateWithValidation(List<Score> scores) {
        List<String> errors = new ArrayList<>();
        List<Score> validScores = new ArrayList<>();

        for (int i = 0; i < scores.size(); i++) {
            Score score = scores.get(i);
            String error = validateScore(score);
            if (error != null) {
                errors.add("第" + (i + 1) + "条: " + error);
                continue;
            }
            calculateScore(score);
            validScores.add(score);
        }

        if (!validScores.isEmpty()) {
            updateBatchById(validScores, 100);
            // 5.4 通知：成绩录入/更新 → 通知对应学生
            try {
                for (Score s : validScores) {
                    if (s.getStudentId() != null) {
                        Long studentUserId = noticeService.getStudentUserId(s.getStudentId());
                        if (studentUserId != null && s.getTotalScore() != null) {
                            noticeService.createAndPush("成绩录入通知",
                                "您的成绩已录入：" + s.getTotalScore() + "分（" + s.getGrade() + "）",
                                "1", studentUserId);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("成绩录入通知推送失败: {}", e.getMessage());
            }
        }

        return errors;
    }

    @Override
    public ScoreImportResultDTO importScores(MultipartFile file, Long courseId, Long teacherId) {
        try {
            // 预加载学号->学生ID映射
            List<Student> allStudents = studentMapper.selectList(null);
            Map<String, Long> studentNoToIdMap = allStudents.stream()
                    .collect(Collectors.toMap(
                            Student::getStudentNo,
                            Student::getId,
                            (a, b) -> a
                    ));

            // 查询该课程已有成绩（学号->成绩ID映射，用于更新已有记录）
            LambdaQueryWrapper<Score> scoreWrapper = new LambdaQueryWrapper<>();
            scoreWrapper.eq(Score::getCourseId, courseId);
            List<Score> existingScores = list(scoreWrapper);
            Map<Long, Long> studentIdToScoreIdMap = existingScores.stream()
                    .collect(Collectors.toMap(
                            Score::getStudentId,
                            Score::getId,
                            (a, b) -> a
                    ));

            ScoreImportListener listener = new ScoreImportListener(
                    this, evaluationService, studentNoToIdMap, studentIdToScoreIdMap, courseId, teacherId, 100
            );

            EasyExcel.read(file.getInputStream(), ScoreImportDTO.class, listener).sheet().headRowNumber(1).doRead();
            return listener.getResult();
        } catch (Exception e) {
            log.error("成绩导入失败", e);
            ScoreImportResultDTO result = new ScoreImportResultDTO();
            result.setTotal(0);
            result.setSuccessCount(0);
            result.setFailCount(1);
            List<ScoreImportResultDTO.FailDetail> failDetails = new ArrayList<>();
            failDetails.add(new ScoreImportResultDTO.FailDetail(0, "", "导入失败: " + e.getMessage(), "IMP-SCORE-SYS-500", "system", "请联系管理员", "R-SYSTEM"));
            result.setFailDetails(failDetails);
            return result;
        }
    }
}
