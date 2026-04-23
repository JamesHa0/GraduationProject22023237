package com.jameshao.gp22023237.controller.course;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.jameshao.gp22023237.DTO.CourseStudentScoreDTO;
import com.jameshao.gp22023237.DTO.ScoreImportDTO;
import com.jameshao.gp22023237.DTO.ScoreImportResultDTO;
import com.jameshao.gp22023237.DTO.ScoreWithDetailsDTO;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.mapper.ScoreMapper;
import com.jameshao.gp22023237.po.Score;
import com.jameshao.gp22023237.service.CoursePhaseService;
import com.jameshao.gp22023237.service.ScoreService;
import com.jameshao.gp22023237.service.TeachingEvaluationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/course/score")
public class ScoreController {

    private static final String TEMPLATE_VERSION = "v1.0";

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private ScoreMapper scoreMapper;

    @Autowired
    private CoursePhaseService coursePhaseService;

    @Autowired
    private TeachingEvaluationService evaluationService;

    @GetMapping("/list")
    public String list(Long studentId, Long courseId, String grade) {
        try {
            List<ScoreWithDetailsDTO> list = scoreMapper.listScoreWithDetails(studentId, courseId, grade, null, null, null, null);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            log.error("查询成绩列表失败", e);
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id) {
        try {
            Score score = scoreService.getById(id);
            return jsonReturn.returnSuccess(score);
        } catch (Exception e) {
            log.error("查询成绩详情失败, id={}", id, e);
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/add")
    public String add(@RequestBody Score score) {
        try {
            // 校验成绩录入时间窗口
            if (!coursePhaseService.isScoreEntryOpen()) {
                return jsonReturn.returnError("当前不在成绩录入时间窗口内，无法录入成绩");
            }
            // 校验学生是否已完成教学评价
            if (score.getStudentId() != null && score.getCourseId() != null) {
                if (!evaluationService.checkEvaluated(score.getStudentId(), score.getCourseId())) {
                    return jsonReturn.returnFailed("该学生尚未完成教学评价，无法录入成绩");
                }
            }
            String error = scoreService.validateScore(score);
            if (error != null) {
                return jsonReturn.returnFailed(error);
            }
            scoreService.calculateScore(score);
            boolean success = scoreService.save(score);
            if (success) {
                return jsonReturn.returnSuccess("新增成功");
            } else {
                return jsonReturn.returnFailed("新增失败");
            }
        } catch (Exception e) {
            log.error("新增成绩失败", e);
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PutMapping("/update")
    public String update(@RequestBody Score score) {
        try {
            // 校验成绩录入时间窗口
            if (!coursePhaseService.isScoreEntryOpen()) {
                return jsonReturn.returnError("当前不在成绩录入时间窗口内，无法修改成绩");
            }
            // 校验学生是否已完成教学评价
            if (score.getStudentId() != null && score.getCourseId() != null) {
                if (!evaluationService.checkEvaluated(score.getStudentId(), score.getCourseId())) {
                    return jsonReturn.returnFailed("该学生尚未完成教学评价，无法修改成绩");
                }
            }
            String error = scoreService.validateScore(score);
            if (error != null) {
                return jsonReturn.returnFailed(error);
            }
            scoreService.calculateScore(score);
            boolean success = scoreService.updateById(score);
            if (success) {
                return jsonReturn.returnSuccess("更新成功");
            } else {
                return jsonReturn.returnFailed("更新失败");
            }
        } catch (Exception e) {
            log.error("更新成绩失败", e);
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 批量更新成绩
     */
    @PutMapping("/batchUpdate")
    public String batchUpdate(@RequestBody List<Score> scores) {
        try {
            // 校验成绩录入时间窗口
            if (!coursePhaseService.isScoreEntryOpen()) {
                return jsonReturn.returnError("当前不在成绩录入时间窗口内，无法修改成绩");
            }
            // 过滤掉未完成教学评价的学生
            List<Score> validScores = new ArrayList<>();
            List<String> evalErrors = new ArrayList<>();
            for (int i = 0; i < scores.size(); i++) {
                Score score = scores.get(i);
                if (score.getStudentId() != null && score.getCourseId() != null) {
                    if (!evaluationService.checkEvaluated(score.getStudentId(), score.getCourseId())) {
                        evalErrors.add("第" + (i + 1) + "条: 该学生尚未完成教学评价，无法录入成绩");
                        continue;
                    }
                }
                validScores.add(score);
            }
            if (validScores.isEmpty()) {
                Map<String, Object> data = new HashMap<>();
                data.put("updatedCount", 0);
                data.put("errors", evalErrors);
                return jsonReturn.returnSuccess(data);
            }
            List<String> errors = scoreService.batchUpdateWithValidation(validScores);
            errors.addAll(0, evalErrors);
            Map<String, Object> data = new HashMap<>();
            data.put("updatedCount", validScores.size() - errors.size() + evalErrors.size());
            data.put("errors", errors);
            if (errors.isEmpty()) {
                return jsonReturn.returnSuccess("批量更新成功");
            } else {
                return jsonReturn.returnSuccess(data);
            }
        } catch (Exception e) {
            log.error("批量更新成绩失败", e);
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
            log.error("删除成绩失败, id={}", id, e);
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
            log.error("批量删除成绩失败", e);
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
            List<ScoreWithDetailsDTO> list = scoreMapper.listScoreWithDetails(studentId, null, grade, semester, null, null, null);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            log.error("查询学生成绩失败", e);
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 查询成绩列表（带详情）- 管理员/教师使用
     * 支持分页和teacherId过滤
     */
    @GetMapping("/listWithDetails")
    public String listWithDetails(Long studentId, Long courseId, String grade, String semester,
                                   Long teacherId, Integer pageNum, Integer pageSize) {
        try {
            if (pageNum != null && pageSize != null) {
                int offset = (pageNum - 1) * pageSize;
                List<ScoreWithDetailsDTO> rows = scoreMapper.listScoreWithDetails(studentId, courseId, grade, semester, teacherId, offset, pageSize);
                int total = scoreMapper.countScoreWithDetails(studentId, courseId, grade, semester, teacherId);
                Map<String, Object> data = new HashMap<>();
                data.put("rows", rows);
                data.put("total", total);
                return jsonReturn.returnSuccess(data);
            } else {
                List<ScoreWithDetailsDTO> list = scoreMapper.listScoreWithDetails(studentId, courseId, grade, semester, teacherId, null, null);
                return jsonReturn.returnSuccess(list);
            }
        } catch (Exception e) {
            log.error("查询成绩列表失败", e);
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * Excel批量导入成绩
     */
    @PostMapping("/import")
    public String importScores(@RequestParam("file") MultipartFile file,
                               @RequestParam("courseId") Long courseId,
                               @RequestParam("teacherId") Long teacherId) {
        try {
            // 校验成绩录入时间窗口
            if (!coursePhaseService.isScoreEntryOpen()) {
                return jsonReturn.returnError("当前不在成绩录入时间窗口内，无法导入成绩");
            }
            if (file == null || file.isEmpty()) {
                return jsonReturn.returnFailed("请选择要上传的文件");
            }
            String filename = file.getOriginalFilename();
            String lowerName = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
            if (!lowerName.endsWith(".xls") && !lowerName.endsWith(".xlsx")) {
                return jsonReturn.returnFailed("请上传Excel文件(.xls或.xlsx)");
            }
            if (courseId == null) {
                return jsonReturn.returnFailed("课程ID不能为空");
            }
            ScoreImportResultDTO result = scoreService.importScores(file, courseId, teacherId);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            log.error("成绩导入失败", e);
            return jsonReturn.returnError("导入失败，请核对模板字段与数据格式后重试");
        }
    }

    /**
     * 下载成绩导入模板
     */
    @GetMapping("/importTemplate")
    public void downloadTemplate(HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("成绩导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            excelWriter = EasyExcel.write(response.getOutputStream()).build();

            // 示例数据
            List<ScoreImportDTO> dataList = new ArrayList<>();
            ScoreImportDTO example = new ScoreImportDTO();
            example.setStudentNo("22023237");
            example.setStudentName("张三");
            example.setUsualScore(85.0);
            example.setExamScore(78.5);
            example.setComment("表现良好");
            dataList.add(example);

            WriteSheet templateSheet = EasyExcel.writerSheet("成绩模板").head(ScoreImportDTO.class).build();
            excelWriter.write(dataList, templateSheet);

            WriteSheet ruleSheet = EasyExcel.writerSheet("填写说明").head(buildRuleHead()).build();
            excelWriter.write(buildRuleRows(), ruleSheet);
        } catch (Exception e) {
            log.error("下载成绩导入模板失败", e);
            try {
                response.reset();
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(jsonReturn.returnFailed("模板下载失败，请稍后重试"));
            } catch (IOException ioException) {
                log.error("写入模板下载错误响应失败", ioException);
            }
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    /**
     * 导出成绩Excel
     */
    @GetMapping("/export")
    public void exportScores(Long courseId, Long teacherId, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("成绩导出", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            List<ScoreWithDetailsDTO> list = scoreMapper.listScoreWithDetails(null, courseId, null, null, teacherId, null, null);

            excelWriter = EasyExcel.write(response.getOutputStream()).build();
            WriteSheet sheet = EasyExcel.writerSheet("成绩数据").head(ScoreExportDTO.class).build();

            List<ScoreExportDTO> exportList = new ArrayList<>();
            for (ScoreWithDetailsDTO dto : list) {
                ScoreExportDTO export = new ScoreExportDTO();
                export.setStudentNo(dto.getStudentNo());
                export.setStudentName(dto.getStudentName());
                export.setCourseNo(dto.getCourseNo());
                export.setCourseName(dto.getCourseName());
                export.setCredit(dto.getCredit());
                export.setUsualScore(dto.getUsualScore());
                export.setExamScore(dto.getExamScore());
                export.setTotalScore(dto.getTotalScore());
                export.setGrade(dto.getGrade() != null ? getGradeLabel(dto.getGrade()) : "");
                export.setComment(dto.getComment());
                exportList.add(export);
            }

            excelWriter.write(exportList, sheet);
        } catch (Exception e) {
            log.error("导出成绩失败", e);
            try {
                response.reset();
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(jsonReturn.returnFailed("导出失败，请稍后重试"));
            } catch (IOException ioException) {
                log.error("写入导出错误响应失败", ioException);
            }
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    /**
     * 查询某课程下所有选课学生及成绩状态（教师专用）
     * 返回包含未录入成绩的学生
     */
    @GetMapping("/courseStudents")
    public String courseStudents(Long courseId, String keyword, String grade) {
        try {
            if (courseId == null) {
                return jsonReturn.returnError("课程ID不能为空");
            }
            List<CourseStudentScoreDTO> list = scoreMapper.listCourseStudentsWithScore(courseId, keyword, grade);
            int total = scoreMapper.countCourseStudents(courseId);

            // 统计信息
            long enteredCount = list.stream().filter(s -> s.getTotalScore() != null).count();
            double avgScore = list.stream()
                    .filter(s -> s.getTotalScore() != null)
                    .mapToDouble(CourseStudentScoreDTO::getTotalScore)
                    .average().orElse(0.0);
            long passCount = list.stream()
                    .filter(s -> s.getTotalScore() != null && s.getTotalScore() >= 60)
                    .count();
            long excellentCount = list.stream()
                    .filter(s -> s.getTotalScore() != null && s.getTotalScore() >= 90)
                    .count();

            Map<String, Object> data = new HashMap<>();
            data.put("rows", list);
            data.put("total", total);
            data.put("enteredCount", enteredCount);
            data.put("avgScore", Math.round(avgScore * 10.0) / 10.0);
            data.put("passRate", enteredCount > 0 ? Math.round((passCount * 100.0 / enteredCount) * 10.0) / 10.0 : 0.0);
            data.put("excellentRate", enteredCount > 0 ? Math.round((excellentCount * 100.0 / enteredCount) * 10.0) / 10.0 : 0.0);
            return jsonReturn.returnSuccess(data);
        } catch (Exception e) {
            log.error("查询课程学生成绩失败", e);
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 下载预填充学生数据的成绩导入模板（教师专用）
     * 根据courseId查询选课学生，生成包含学号姓名的Excel模板
     */
    @GetMapping("/importTemplateWithStudents")
    public void downloadTemplateWithStudents(Long courseId, Long teacherId, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            if (courseId == null) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(jsonReturn.returnError("课程ID不能为空"));
                return;
            }

            // 查询该课程所有选课学生
            List<CourseStudentScoreDTO> students = scoreMapper.listCourseStudentsWithScore(courseId, null, null);

            // 获取课程名称
            String courseName = students.isEmpty() ? "未知课程" : students.get(0).getCourseName();

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode(courseName + "_成绩录入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            excelWriter = EasyExcel.write(response.getOutputStream()).build();

            // 生成学生数据模板
            List<ScoreImportDTO> dataList = new ArrayList<>();
            for (CourseStudentScoreDTO student : students) {
                ScoreImportDTO row = new ScoreImportDTO();
                row.setStudentNo(student.getStudentNo());
                row.setStudentName(student.getStudentName());
                // 成绩列留空供教师填写
                row.setUsualScore(null);
                row.setExamScore(null);
                row.setComment(null);
                dataList.add(row);
            }

            WriteSheet templateSheet = EasyExcel.writerSheet("成绩模板").head(ScoreImportDTO.class).build();
            excelWriter.write(dataList, templateSheet);

            WriteSheet ruleSheet = EasyExcel.writerSheet("填写说明").head(buildRuleHead()).build();
            excelWriter.write(buildRuleRows(), ruleSheet);
        } catch (Exception e) {
            log.error("下载学生成绩模板失败", e);
            try {
                response.reset();
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(jsonReturn.returnFailed("模板下载失败，请稍后重试"));
            } catch (IOException ioException) {
                log.error("写入模板下载错误响应失败", ioException);
            }
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    private String getGradeLabel(String grade) {
        switch (grade) {
            case "A": return "优秀";
            case "B": return "良好";
            case "C": return "中等";
            case "D": return "及格";
            case "E": return "不及格";
            default: return grade;
        }
    }

    private List<List<String>> buildRuleHead() {
        return Arrays.asList(
                Arrays.asList("字段"),
                Arrays.asList("是否必填"),
                Arrays.asList("格式/范围"),
                Arrays.asList("示例"),
                Arrays.asList("错误码"),
                Arrays.asList("填写建议")
        );
    }

    private List<List<String>> buildRuleRows() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("模板版本", "是", TEMPLATE_VERSION, TEMPLATE_VERSION, "-", "请优先使用最新模板"));
        rows.add(Arrays.asList("学号", "是", "系统内已有学号", "22023237", "IMP-SCORE-001/002", "必须与系统内学号一致"));
        rows.add(Arrays.asList("姓名", "否", "仅做核对参考", "张三", "-", "系统以学号匹配，姓名仅供参考"));
        rows.add(Arrays.asList("平时成绩", "否", "0-100", "85.0", "IMP-SCORE-004", "支持小数，保留一位"));
        rows.add(Arrays.asList("期末成绩", "否", "0-100", "78.5", "IMP-SCORE-005", "支持小数，保留一位"));
        rows.add(Arrays.asList("评语", "否", "文本", "表现良好", "-", "简短评语"));
        rows.add(Arrays.asList("说明", "是", "平时和期末至少填一项", "-", "IMP-SCORE-004/005", "两项均填则按权重自动计算总成绩"));
        rows.add(Arrays.asList("权重说明", "否", "默认平时30%+期末70%", "-", "-", "可在页面中修改权重设置"));
        return rows;
    }

    /**
     * 成绩导出DTO（内部类）
     */
    @lombok.Data
    public static class ScoreExportDTO {
        @com.alibaba.excel.annotation.ExcelProperty("学号")
        private String studentNo;
        @com.alibaba.excel.annotation.ExcelProperty("姓名")
        private String studentName;
        @com.alibaba.excel.annotation.ExcelProperty("课程编号")
        private String courseNo;
        @com.alibaba.excel.annotation.ExcelProperty("课程名称")
        private String courseName;
        @com.alibaba.excel.annotation.ExcelProperty("学分")
        private Double credit;
        @com.alibaba.excel.annotation.ExcelProperty("平时成绩")
        private Double usualScore;
        @com.alibaba.excel.annotation.ExcelProperty("期末成绩")
        private Double examScore;
        @com.alibaba.excel.annotation.ExcelProperty("总成绩")
        private Double totalScore;
        @com.alibaba.excel.annotation.ExcelProperty("等级")
        private String grade;
        @com.alibaba.excel.annotation.ExcelProperty("评语")
        private String comment;
    }
}
