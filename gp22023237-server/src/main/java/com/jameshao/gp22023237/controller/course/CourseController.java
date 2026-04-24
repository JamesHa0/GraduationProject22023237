package com.jameshao.gp22023237.controller.course;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jameshao.gp22023237.DTO.CourseImportDTO;
import com.jameshao.gp22023237.DTO.CourseImportResultDTO;
import com.jameshao.gp22023237.DTO.CourseWithTeacherDTO;
import com.jameshao.gp22023237.annotation.Log;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.common.enums.BusinessType;
import com.jameshao.gp22023237.mapper.CourseMapper;
import com.jameshao.gp22023237.po.Course;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.service.CourseService;
import com.jameshao.gp22023237.service.TeacherService;
import com.jameshao.gp22023237.service.UserService;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/course")
public class CourseController {

    private static final String TEMPLATE_VERSION = "v3.0";

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public String list(String name, String courseNo, Integer status, String semester, Integer pageNum, Integer pageSize) {
        try {
            if (pageNum != null && pageSize != null) {
                int offset = (pageNum - 1) * pageSize;
                List<CourseWithTeacherDTO> rows = courseMapper.listCourseWithTeacherPage(name, courseNo, status, semester, offset, pageSize);
                int total = courseMapper.countCourseWithTeacher(name, courseNo, status, semester);
                Map<String, Object> data = new HashMap<>();
                data.put("rows", rows);
                data.put("total", total);
                return jsonReturn.returnSuccess(data);
            } else {
                List<CourseWithTeacherDTO> list = courseMapper.listCourseWithTeacher(name, courseNo, status, semester);
                return jsonReturn.returnSuccess(list);
            }
        } catch (Exception e) {
            log.error("查询课程列表失败", e);
            return jsonReturn.returnError("查询课程列表失败，请稍后重试");
        }
    }

    @GetMapping("/teachers")
    public String listTeachers() {
        try {
            QueryWrapper<Teacher> wrapper = new QueryWrapper<>();
            wrapper.ne("teacher_no", "TBD001");
            wrapper.orderByAsc("teacher_no");
            List<Teacher> list = teacherService.list(wrapper);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            log.error("查询教师列表失败", e);
            return jsonReturn.returnError("查询教师列表失败，请稍后重试");
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id) {
        try {
            CourseWithTeacherDTO course = courseMapper.getCourseWithTeacherById(id);
            return jsonReturn.returnSuccess(course);
        } catch (Exception e) {
            log.error("查询课程详情失败, id={}", id, e);
            return jsonReturn.returnError("查询课程详情失败，请稍后重试");
        }
    }

    @Log(title = "课程管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public String add(@RequestBody Course course) {
        try {
            course.setCreateTime(new Date());
            course.setUpdateTime(new Date());

            boolean success = courseService.save(course);
            if (success) {
                return jsonReturn.returnSuccess("新增成功");
            } else {
                return jsonReturn.returnFailed("新增失败");
            }
        } catch (Exception e) {
            log.error("新增课程失败", e);
            return jsonReturn.returnError("新增课程失败，请检查数据后重试");
        }
    }

    @Log(title = "课程管理", businessType = BusinessType.UPDATE)
    @PutMapping("/update")
    public String update(@RequestBody Course course) {
        try {
            course.setUpdateTime(new Date());

            boolean success = courseService.updateById(course);
            if (success) {
                return jsonReturn.returnSuccess("更新成功");
            } else {
                return jsonReturn.returnFailed("更新失败");
            }
        } catch (Exception e) {
            log.error("更新课程失败, id={}", course.getId(), e);
            return jsonReturn.returnError("更新课程失败，请检查数据后重试");
        }
    }

    @Log(title = "课程管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        try {
            boolean success = courseService.removeById(id);
            if (success) {
                return jsonReturn.returnSuccess("删除成功");
            } else {
                return jsonReturn.returnFailed("删除失败");
            }
        } catch (Exception e) {
            log.error("删除课程失败, id={}", id, e);
            return jsonReturn.returnError("删除课程失败，请稍后重试");
        }
    }

    @Log(title = "课程管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteBatch")
    public String deleteBatch(@RequestBody List<Long> ids) {
        try {
            boolean success = courseService.removeByIds(ids);
            if (success) {
                return jsonReturn.returnSuccess("删除成功");
            } else {
                return jsonReturn.returnFailed("删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除课程失败, ids={}", ids, e);
            return jsonReturn.returnError("批量删除课程失败，请稍后重试");
        }
    }

    /**
     * 批量导入课程
     */
    @Log(title = "课程管理", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public String importCourses(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return jsonReturn.returnFailed("请选择要上传的文件");
            }

            String filename = file.getOriginalFilename();
            String lowerName = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
            if (!lowerName.endsWith(".xls") && !lowerName.endsWith(".xlsx")) {
                return jsonReturn.returnFailed("请上传Excel文件(.xls或.xlsx)");
            }

            CourseImportResultDTO result = courseService.importCourses(file);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            log.error("课程导入失败", e);
            return jsonReturn.returnError("导入失败，请核对模板字段与数据格式后重试");
        }
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/importTemplate")
    public void downloadTemplate(HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("课程导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            excelWriter = EasyExcel.write(response.getOutputStream()).build();

            List<CourseImportDTO> dataList = new ArrayList<>();
            CourseImportDTO example = new CourseImportDTO();
            example.setCourseNo("CS101");
            example.setName("数据结构");
            example.setCredit(3.0);
            example.setHours(48);
            example.setSemester("2024-2025-2");
            example.setYear(2024);
            example.setMaxCredits(6.0);
            example.setStatus(0);
            example.setStudyNature("必修");
            example.setTextbook(0);
            example.setExternalSelection(0);
            example.setDescription("计算机专业核心课程");
            example.setRemark("模板版本" + TEMPLATE_VERSION);
            dataList.add(example);

            WriteSheet templateSheet = EasyExcel.writerSheet("课程模板").head(CourseImportDTO.class).build();
            excelWriter.write(dataList, templateSheet);

            WriteSheet ruleSheet = EasyExcel.writerSheet("填写说明")
                    .head(buildRuleHead())
                    .build();
            excelWriter.write(buildRuleRows(), ruleSheet);

        } catch (Exception e) {
            log.error("下载课程导入模板失败", e);
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
        rows.add(Arrays.asList("课程编号", "是", "<=20且系统内唯一", "CS101", "IMP-COURSE-001/002/003", "必填，且避免重复"));
        rows.add(Arrays.asList("课程名称", "是", "<=100字符", "数据结构", "IMP-COURSE-004", "建议使用规范课程名称"));
        rows.add(Arrays.asList("学分", "是", "0.5-10", "3.0", "IMP-COURSE-005", "建议按0.5步长"));
        rows.add(Arrays.asList("学时", "是", "1-200整数", "48", "IMP-COURSE-006", "与教学计划保持一致"));
        rows.add(Arrays.asList("学期", "是", "<=20字符", "2024-2025-2", "IMP-COURSE-007", "按系统已有学期规则填写"));
        rows.add(Arrays.asList("学年", "是", "四位数字", "2024", "IMP-COURSE-007", "建议与学期一致"));
        rows.add(Arrays.asList("课程状态", "否", "0/1/2", "0", "IMP-COURSE-009", "0未开课，1已开课，2已结课"));
        rows.add(Arrays.asList("教材", "否", "0/1", "0", "-", "1代表有教材"));
        rows.add(Arrays.asList("外年级选课", "否", "0/1", "0", "-", "1代表允许"));
        return rows;
    }
}
