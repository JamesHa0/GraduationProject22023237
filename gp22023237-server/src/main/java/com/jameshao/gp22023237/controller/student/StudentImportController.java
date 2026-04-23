package com.jameshao.gp22023237.controller.student;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.jameshao.gp22023237.DTO.StudentImportDTO;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.ClassEntity;
import com.jameshao.gp22023237.service.ClassService;
import com.jameshao.gp22023237.service.StudentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
@RequestMapping("/student")
public class StudentImportController {

    private static final String TEMPLATE_VERSION = "v1.0";

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ClassService classService;

    @PostMapping("/import")
    public String importStudents(@RequestParam("file") MultipartFile file,
                                 @RequestParam("taskId") String taskId,
                                 @RequestParam(value = "classId", required = false) Long classId) {
        try {
            if (file == null || file.isEmpty()) {
                return jsonReturn.returnFailed("请选择要上传的文件");
            }
            String filename = file.getOriginalFilename();
            String lowerName = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
            if (!lowerName.endsWith(".xls") && !lowerName.endsWith(".xlsx") && !lowerName.endsWith(".csv")) {
                return jsonReturn.returnFailed("请上传 .xls/.xlsx/.csv 文件");
            }
            studentService.startImportTask(taskId, file, classId);
            Map<String, Object> data = new HashMap<>();
            data.put("taskId", taskId);
            return jsonReturn.returnSuccess(data);
        } catch (Exception e) {
            log.error("学生批量导入启动失败", e);
            return jsonReturn.returnError("导入任务启动失败，请稍后重试");
        }
    }

    @GetMapping("/importTemplate")
    public void downloadTemplate(@RequestParam(value = "classId", required = false) Long classId,
                                 HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");

            // 根据是否传入classId决定模板类型
            String templateName = "学生导入模板";
            StudentImportDTO example = new StudentImportDTO();
            example.setStudentNo("22023237");
            example.setStudentName("张三");
            example.setDepartment("人工智能学院");
            example.setMajor("计算机科学与技术");
            example.setAdmissionYear(2022);
            example.setCohortYear(2022);
            example.setGraduationYear(2026);
            example.setResearchDirection("计算机视觉");
            example.setStatus(1);
            example.setSelectionStatus(0);

            if (classId != null) {
                ClassEntity classEntity = classService.getById(classId);
                if (classEntity != null) {
                    templateName = classEntity.getClassName() + " 班级导入模板";
                    example.setDepartment(classEntity.getDepartment());
                    example.setMajor(classEntity.getMajor());
                    if (classEntity.getAdmissionYear() != null) {
                        example.setAdmissionYear(classEntity.getAdmissionYear());
                        example.setCohortYear(classEntity.getAdmissionYear());
                        example.setGraduationYear(classEntity.getAdmissionYear() + 3);
                    }
                    example.setResearchDirection("");
                }
            }

            String fileName = URLEncoder.encode(templateName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            excelWriter = EasyExcel.write(response.getOutputStream()).build();

            List<StudentImportDTO> dataList = new ArrayList<>();
            dataList.add(example);

            WriteSheet templateSheet = EasyExcel.writerSheet("学生模板").head(StudentImportDTO.class).build();
            excelWriter.write(dataList, templateSheet);

            WriteSheet ruleSheet = EasyExcel.writerSheet("填写说明").head(buildRuleHead()).build();
            excelWriter.write(buildRuleRows(), ruleSheet);
        } catch (Exception e) {
            log.error("下载学生导入模板失败", e);
            try {
                response.reset();
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(jsonReturn.returnFailed("模板下载失败，请稍后重试"));
            } catch (IOException ioException) {
                log.error("写入学生模板下载错误响应失败", ioException);
            }
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
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
        rows.add(Arrays.asList("学号", "是", "<=20且系统内唯一", "22023237", "IMP-STU-001/002/003/004", "必填且不可重复"));
        rows.add(Arrays.asList("姓名", "是", "<=50字符", "张三", "IMP-STU-005", "请填写真实姓名"));
        rows.add(Arrays.asList("学院", "是", "<=100字符", "人工智能学院", "IMP-STU-006", "按系统规范学院名称填写"));
        rows.add(Arrays.asList("专业", "是", "<=100字符", "计算机科学与技术", "IMP-STU-007", "按培养方案规范填写"));
        rows.add(Arrays.asList("入学年份", "是", "2000-2100", "2022", "IMP-STU-008", "4位年份"));
        rows.add(Arrays.asList("归属年级", "否", "2000-2100，空则默认入学年份", "2022", "-", "建议与入学年份一致"));
        rows.add(Arrays.asList("毕业年份", "是", "2000-2100，且>=入学年份", "2026", "IMP-STU-009", "通常为入学年份+3/4"));
        rows.add(Arrays.asList("状态", "否", "0/1", "1", "IMP-STU-010", "0禁用，1正常"));
        rows.add(Arrays.asList("双选状态", "否", "0/1/2/3", "0", "IMP-STU-011", "0未开始，1双选中，2补选中，3已确定"));
        rows.add(Arrays.asList("CSV说明", "是", "UTF-8编码，列名需与模板一致", "student.csv", "-", "建议先下载模板再另存为CSV填写"));
        return rows;
    }
}
