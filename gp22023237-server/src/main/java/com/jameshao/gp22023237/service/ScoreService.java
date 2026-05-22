package com.jameshao.gp22023237.service;

import com.jameshao.gp22023237.DTO.ScoreImportResultDTO;
import com.jameshao.gp22023237.po.Score;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
* @author test
* @description 针对表【score(成绩表)】的数据库操作Service
* @createDate 2025-10-08 17:09:36
*/
public interface ScoreService extends IService<Score> {

    /**
     * 计算成绩：设置默认权重、计算总成绩和等级
     */
    void calculateScore(Score score);

    /**
     * 根据分数计算等级
     */
    String calculateGrade(Double totalScore);

    /**
     * 校验单条成绩数据
     * @return 错误信息，null表示校验通过
     */
    String validateScore(Score score);

    /**
     * 批量更新成绩（含校验和计算）
     * @param scores 成绩列表
     * @return 校验失败的信息列表，空列表表示全部成功
     */
    List<String> batchUpdateWithValidation(List<Score> scores);

    /**
     * 导入成绩Excel
     * @param file 上传的Excel文件
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 导入结果
     */
    ScoreImportResultDTO importScores(MultipartFile file, Long courseId, Long teacherId);

    /**
     * 计算课程及格率
     * @param courseId 课程ID
     * @return { total, passCount, failCount, passRate }
     */
    Map<String, Object> calculatePassRate(Long courseId);
}
