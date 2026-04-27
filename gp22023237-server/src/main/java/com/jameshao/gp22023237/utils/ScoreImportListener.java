package com.jameshao.gp22023237.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.jameshao.gp22023237.DTO.ScoreImportDTO;
import com.jameshao.gp22023237.DTO.ScoreImportResultDTO;
import com.jameshao.gp22023237.common.error.ScoreImportErrorCode;
import com.jameshao.gp22023237.po.Score;
import com.jameshao.gp22023237.service.ScoreService;
import com.jameshao.gp22023237.service.TeachingEvaluationService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 成绩导入Excel监听器
 */
@Slf4j
public class ScoreImportListener implements ReadListener<ScoreImportDTO> {

    private final int batchCount;

    /** 缓存的数据 */
    private List<RowData> cachedDataList;

    private final ScoreService scoreService;

    /** 教学评价服务 */
    private final TeachingEvaluationService evaluationService;

    /** 学号 -> 学生ID 映射 */
    private final Map<String, Long> studentNoToIdMap;

    /** 学生ID -> 已有成绩ID 映射（用于更新已有记录） */
    private final Map<Long, Long> studentIdToScoreIdMap;

    /** 课程ID */
    private final Long courseId;

    /** 教师ID */
    private final Long teacherId;

    /** 文件内学号判重 */
    private final Set<String> fileStudentNoSet = new HashSet<>();

    private final List<ScoreImportResultDTO.FailDetail> failDetails = new ArrayList<>();
    private int successCount = 0;
    private int totalCount = 0;

    public ScoreImportListener(ScoreService scoreService,
                               TeachingEvaluationService evaluationService,
                               Map<String, Long> studentNoToIdMap,
                               Map<Long, Long> studentIdToScoreIdMap,
                               Long courseId,
                               Long teacherId,
                               int batchCount) {
        this.scoreService = scoreService;
        this.evaluationService = evaluationService;
        this.studentNoToIdMap = studentNoToIdMap;
        this.studentIdToScoreIdMap = studentIdToScoreIdMap;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.batchCount = batchCount;
        this.cachedDataList = ListUtils.newArrayListWithExpectedSize(batchCount);
    }

    @Override
    public void invoke(ScoreImportDTO data, AnalysisContext context) {
        totalCount++;
        int row = context.readRowHolder().getRowIndex() + 1;
        cachedDataList.add(new RowData(row, data));
        if (cachedDataList.size() >= batchCount) {
            saveData();
            cachedDataList = ListUtils.newArrayListWithExpectedSize(batchCount);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        log.info("成绩导入解析完成，总记录:{}，成功:{}，失败:{}", totalCount, successCount, failDetails.size());
    }

    /**
     * 批量保存并校验
     */
    public void saveData() {
        if (cachedDataList.isEmpty()) {
            return;
        }

        List<Score> validScores = new ArrayList<>();

        for (RowData rowData : cachedDataList) {
            ScoreImportResultDTO.FailDetail failDetail = validateData(rowData);
            if (failDetail != null) {
                failDetails.add(failDetail);
                continue;
            }
            try {
                Score score = convertToScore(rowData.dto);
                validScores.add(score);
            } catch (Exception e) {
                log.error("第{}行数据转换失败", rowData.row, e);
                failDetails.add(toFailDetail(rowData.row, rowData.dto.getStudentNo(),
                        ScoreImportErrorCode.SYSTEM_ERROR, "数据转换失败: " + e.getMessage()));
            }
        }

        if (!validScores.isEmpty()) {
            for (Score score : validScores) {
                scoreService.calculateScore(score);
                // 如果已有记录则更新，否则新增
                if (score.getId() != null) {
                    scoreService.updateById(score);
                } else {
                    scoreService.save(score);
                }
                successCount++;
            }
        }
    }

    /**
     * 校验数据
     */
    private ScoreImportResultDTO.FailDetail validateData(RowData rowData) {
        ScoreImportDTO data = rowData.dto;

        // 学号必填
        if (data.getStudentNo() == null || data.getStudentNo().trim().isEmpty()) {
            return toFailDetail(rowData.row, "", ScoreImportErrorCode.STUDENT_NO_REQUIRED);
        }

        String studentNo = data.getStudentNo().trim();

        // 学号存在于系统
        Long studentId = studentNoToIdMap.get(studentNo);
        if (studentId == null) {
            return toFailDetail(rowData.row, studentNo, ScoreImportErrorCode.STUDENT_NOT_FOUND);
        }

        // 校验学生是否已完成教学评价
        if (evaluationService != null && !evaluationService.checkEvaluated(studentId, courseId)) {
            return new ScoreImportResultDTO.FailDetail(rowData.row, studentNo,
                    "该学生尚未完成教学评价，无法导入成绩", "IMP-SCORE-EVAL",
                    "studentId", "请提醒学生先完成教学评价", "R-EVALUATION-REQUIRED");
        }

        // 文件内学号重复检查
        if (fileStudentNoSet.contains(studentNo)) {
            return toFailDetail(rowData.row, studentNo, ScoreImportErrorCode.STUDENT_NO_DUPLICATE_FILE);
        }
        fileStudentNoSet.add(studentNo);

        // 平时成绩范围
        if (data.getUsualScore() != null && (data.getUsualScore() < 0 || data.getUsualScore() > 100)) {
            return toFailDetail(rowData.row, studentNo, ScoreImportErrorCode.USUAL_SCORE_RANGE);
        }

        // 期末成绩范围
        if (data.getExamScore() != null && (data.getExamScore() < 0 || data.getExamScore() > 100)) {
            return toFailDetail(rowData.row, studentNo, ScoreImportErrorCode.EXAM_SCORE_RANGE);
        }

        // 至少有一项成绩
        if (data.getUsualScore() == null && data.getExamScore() == null) {
            return new ScoreImportResultDTO.FailDetail(rowData.row, studentNo,
                    "平时成绩和期末成绩至少填写一项", ScoreImportErrorCode.USUAL_SCORE_RANGE.getCode(),
                    "usualScore/examScore", "请至少填写一项成绩", "R-SCORE-REQUIRED");
        }

        return null;
    }

    /**
     * 转换DTO为Score实体
     */
    private Score convertToScore(ScoreImportDTO dto) {
        Score score = new Score();
        String studentNo = dto.getStudentNo().trim();
        Long studentId = studentNoToIdMap.get(studentNo);

        // 如果已有成绩记录，设置ID（更新模式）
        Long existingScoreId = studentIdToScoreIdMap.get(studentId);
        if (existingScoreId != null) {
            score.setId(existingScoreId);
        }

        score.setStudentId(studentId);
        score.setCourseId(courseId);
        score.setUsualScore(dto.getUsualScore());
        score.setExamScore(dto.getExamScore());
        score.setComment(dto.getComment());
        score.setTeacherId(teacherId);
        score.setUpdateTime(new Date());

        return score;
    }

    private ScoreImportResultDTO.FailDetail toFailDetail(int row, String studentNo, ScoreImportErrorCode code) {
        return new ScoreImportResultDTO.FailDetail(row, studentNo, code.getMessage(),
                code.getCode(), code.getField(), code.getSuggestion(), code.getRule());
    }

    private ScoreImportResultDTO.FailDetail toFailDetail(int row, String studentNo, ScoreImportErrorCode code, String customReason) {
        return new ScoreImportResultDTO.FailDetail(row, studentNo, customReason,
                code.getCode(), code.getField(), code.getSuggestion(), code.getRule());
    }

    /**
     * 获取导入结果
     */
    public ScoreImportResultDTO getResult() {
        return new ScoreImportResultDTO(totalCount, successCount, failDetails.size(), failDetails);
    }

    private static class RowData {
        private final int row;
        private final ScoreImportDTO dto;

        private RowData(int row, ScoreImportDTO dto) {
            this.row = row;
            this.dto = dto;
        }
    }
}
