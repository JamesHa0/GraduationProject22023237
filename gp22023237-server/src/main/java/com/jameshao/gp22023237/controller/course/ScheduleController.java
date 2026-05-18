package com.jameshao.gp22023237.controller.course;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jameshao.gp22023237.DTO.ScheduleDTO;
import com.jameshao.gp22023237.DTO.ScheduleImportDTO;
import com.jameshao.gp22023237.DTO.ScheduleImportResultDTO;
import com.jameshao.gp22023237.DTO.ScheduleWithDetailsDTO;
import com.jameshao.gp22023237.annotation.Log;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.common.enums.BusinessType;
import com.jameshao.gp22023237.mapper.CourseMapper;
import com.jameshao.gp22023237.po.Course;
import com.jameshao.gp22023237.po.DictData;
import com.jameshao.gp22023237.po.Schedule;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.service.ScheduleService;
import com.jameshao.gp22023237.service.DictDataService;
import com.jameshao.gp22023237.service.TeacherService;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 排课管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private DictDataService dictDataService;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private TeacherService teacherService;

    @GetMapping("/list")
    public String list(Long teacherId, Long classId, String semester, Integer year,
                       Integer pageNum, Integer pageSize) {
        try {
            Map<String, Object> data = scheduleService.listScheduleWithDetailsPage(
                teacherId, classId, semester, year, pageNum, pageSize);
            return jsonReturn.returnSuccess(data);
        } catch (Exception e) {
            log.error("查询排课列表失败", e);
            return jsonReturn.returnError("查询排课列表失败，请稍后重试");
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id) {
        try {
            ScheduleWithDetailsDTO detail = scheduleService.getScheduleWithDetailsById(id);
            if (detail == null) {
                return jsonReturn.returnFailed("排课记录不存在");
            }
            return jsonReturn.returnSuccess(detail);
        } catch (Exception e) {
            log.error("查询排课详情失败, id={}", id, e);
            return jsonReturn.returnError("查询排课详情失败，请稍后重试");
        }
    }

    @PostMapping("/add")
    public String add(@RequestBody ScheduleDTO dto) {
        try {
            String errorMsg = scheduleService.addSchedule(dto);
            if (errorMsg != null) {
                return jsonReturn.returnFailed(errorMsg);
            }
            return jsonReturn.returnSuccess("排课成功");
        } catch (Exception e) {
            log.error("新增排课失败", e);
            return jsonReturn.returnError("新增排课失败，请检查数据后重试");
        }
    }

    @PutMapping("/update")
    public String update(@RequestBody ScheduleDTO dto) {
        try {
            String errorMsg = scheduleService.updateSchedule(dto);
            if (errorMsg != null) {
                return jsonReturn.returnFailed(errorMsg);
            }
            return jsonReturn.returnSuccess("更新成功");
        } catch (Exception e) {
            log.error("更新排课失败, id={}", dto.getId(), e);
            return jsonReturn.returnError("更新排课失败，请检查数据后重试");
        }
    }

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        try {
            String errorMsg = scheduleService.deleteSchedule(id);
            if (errorMsg != null) {
                return jsonReturn.returnFailed(errorMsg);
            }
            return jsonReturn.returnSuccess("删除成功");
        } catch (Exception e) {
            log.error("删除排课失败, id={}", id, e);
            return jsonReturn.returnError("删除排课失败，请稍后重试");
        }
    }

    @Log(title = "排课管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteBatch")
    public String deleteBatch(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                String errorMsg = scheduleService.deleteSchedule(id);
                if (errorMsg != null) {
                    return jsonReturn.returnFailed(errorMsg);
                }
            }
            return jsonReturn.returnSuccess("删除成功");
        } catch (Exception e) {
            log.error("批量删除排课失败, ids={}", ids, e);
            return jsonReturn.returnError("批量删除排课失败，请稍后重试");
        }
    }

    /**
     * 复制排课记录
     */
    @PostMapping("/copy")
    public String copy(@RequestBody Map<String, Object> params) {
        try {
            if (params.get("id") == null || params.get("newClassId") == null
                    || params.get("newDayOfWeek") == null || params.get("newStartSection") == null
                    || params.get("newEndSection") == null) {
                return jsonReturn.returnFailed("缺少必要参数");
            }
            Long id = Long.valueOf(params.get("id").toString());
            Long newClassId = Long.valueOf(params.get("newClassId").toString());
            Integer newDayOfWeek = Integer.valueOf(params.get("newDayOfWeek").toString());
            Long newStartSection = Long.valueOf(params.get("newStartSection").toString());
            Long newEndSection = Long.valueOf(params.get("newEndSection").toString());

            String errorMsg = scheduleService.copySchedule(id, newClassId, newDayOfWeek, newStartSection, newEndSection);
            if (errorMsg != null) {
                return jsonReturn.returnFailed(errorMsg);
            }
            return jsonReturn.returnSuccess("复制成功");
        } catch (Exception e) {
            log.error("复制排课失败", e);
            return jsonReturn.returnError("复制排课失败，请稍后重试");
        }
    }

    /**
     * 冲突校验（含教室冲突检测，返回冲突详情）
     */
    @GetMapping("/checkConflict")
    public String checkConflict(Long teacherId, Long classId, String classroom,
                                Integer dayOfWeek, Long startSection, Long endSection,
                                String semester, Integer year, Long excludeId) {
        try {
            Map<String, Object> result = scheduleService.checkConflict(
                teacherId, classId, classroom, dayOfWeek, startSection, endSection, semester, year, excludeId);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            log.error("冲突校验失败", e);
            return jsonReturn.returnError("冲突校验失败，请稍后重试");
        }
    }

    /**
     * 获取时间片列表（从sys_dict_data表，dict_type=sys_time_slot）
     */
    @GetMapping("/timeSlots")
    public String listTimeSlots() {
        try {
            LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DictData::getDictType, "sys_time_slot");
            wrapper.eq(DictData::getStatus, "0");
            wrapper.orderByAsc(DictData::getDictSort);
            List<DictData> list = dictDataService.list(wrapper);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            log.error("获取时间片列表失败", e);
            return jsonReturn.returnError("获取时间片列表失败，请稍后重试");
        }
    }

    private static final String TEMPLATE_VERSION = "v2.0";

    /**
     * 批量导入排课
     */
    @PostMapping("/import")
    public String importSchedules(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return jsonReturn.returnFailed("请选择要上传的文件");
            }

            String filename = file.getOriginalFilename();
            String lowerName = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
            if (!lowerName.endsWith(".xls") && !lowerName.endsWith(".xlsx")) {
                return jsonReturn.returnFailed("请上传Excel文件(.xls或.xlsx)");
            }

            ScheduleImportResultDTO result = scheduleService.importSchedules(file);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            log.error("排课导入失败", e);
            return jsonReturn.returnError("导入失败，请核对模板字段与数据格式后重试");
        }
    }

    /**
     * 下载排课导入模板（支持courseIds参数预填课程数据）
     */
    @GetMapping("/importTemplate")
    public void downloadTemplate(@RequestParam(value = "courseIds", required = false) String courseIds,
                                 HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("排课导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            excelWriter = EasyExcel.write(response.getOutputStream()).build();

            // 模板数据Sheet
            List<ScheduleImportDTO> dataList = new ArrayList<>();

            // 如果传入了courseIds，查询课程及关联教师信息生成预填数据行
            if (courseIds != null && !courseIds.trim().isEmpty()) {
                String[] idArr = courseIds.split(",");
                for (String idStr : idArr) {
                    try {
                        Long courseId = Long.valueOf(idStr.trim());
                        Course course = courseMapper.selectById(courseId);
                        if (course == null) continue;

                        // 查询该课程已有的排课记录，获取关联教师（去重）
                        LambdaQueryWrapper<Schedule> scheduleWrapper = new LambdaQueryWrapper<>();
                        scheduleWrapper.eq(Schedule::getCourseId, courseId);
                        scheduleWrapper.select(Schedule::getTeacherId);
                        List<Schedule> existingSchedules = scheduleService.list(scheduleWrapper);

                        // 对教师去重
                        java.util.Set<Long> teacherIds = new java.util.LinkedHashSet<>();
                        for (Schedule s : existingSchedules) {
                            teacherIds.add(s.getTeacherId());
                        }

                        if (!teacherIds.isEmpty()) {
                            for (Long tid : teacherIds) {
                                Teacher teacher = teacherService.getById(tid);
                                ScheduleImportDTO row = new ScheduleImportDTO();
                                row.setTeacherNo(teacher != null ? teacher.getTeacherNo() : "");
                                row.setTeacherName(teacher != null ? teacher.getTeacherName() : "");
                                row.setCourseNo(course.getCourseNo() != null ? course.getCourseNo() : "");
                                row.setCourseName(course.getName() != null ? course.getName() : "");
                                row.setClassName("");
                                row.setDayOfWeek("");
                                row.setStartSection("");
                                row.setEndSection("");
                                row.setClassroom("");
                                row.setSemester(course.getSemester() != null ? course.getSemester() : "");
                                row.setYear(course.getYear() != null ? String.valueOf(course.getYear()) : "");
                                dataList.add(row);
                            }
                        } else {
                            ScheduleImportDTO row = new ScheduleImportDTO();
                            row.setTeacherNo("");
                            row.setTeacherName("");
                            row.setCourseNo(course.getCourseNo() != null ? course.getCourseNo() : "");
                            row.setCourseName(course.getName() != null ? course.getName() : "");
                            row.setClassName("");
                            row.setDayOfWeek("");
                            row.setStartSection("");
                            row.setEndSection("");
                            row.setClassroom("");
                            row.setSemester(course.getSemester() != null ? course.getSemester() : "");
                            row.setYear(course.getYear() != null ? String.valueOf(course.getYear()) : "");
                            dataList.add(row);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

            // 如果没有预填数据，添加示例行
            if (dataList.isEmpty()) {
                ScheduleImportDTO example = new ScheduleImportDTO();
                example.setTeacherNo("T001");
                example.setTeacherName("张三");
                example.setCourseNo("CS101");
                example.setCourseName("高等数学");
                example.setClassName("计算机2301班");
                example.setDayOfWeek("1");
                example.setStartSection("1");
                example.setEndSection("2");
                example.setClassroom("A101");
                example.setSemester("2024-2025-2");
                example.setYear("2024");
                dataList.add(example);
            }

            WriteSheet templateSheet = EasyExcel.writerSheet("排课模板").head(ScheduleImportDTO.class).build();
            excelWriter.write(dataList, templateSheet);

            // 填写说明Sheet
            WriteSheet ruleSheet = EasyExcel.writerSheet("填写说明")
                    .head(buildRuleHead())
                    .build();
            excelWriter.write(buildRuleRows(), ruleSheet);

        } catch (Exception e) {
            log.error("下载排课导入模板失败", e);
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
        rows.add(Arrays.asList("教师工号", "是", "系统内已有教师工号", "T001", "IMP-SCHED-001/002", "需先在教师管理中录入"));
        rows.add(Arrays.asList("教师姓名", "否", "仅展示用，不参与校验", "张三", "-", "预填模板时自动填充，导入时忽略"));
        rows.add(Arrays.asList("课程编号", "是", "系统内已有课程编号", "CS101", "IMP-SCHED-003/004", "需先在课程管理中录入"));
        rows.add(Arrays.asList("班级名称", "是", "系统内已有班级名称", "计算机23-1", "IMP-SCHED-005/006", "需先在班级管理中录入"));
        rows.add(Arrays.asList("星期", "是", "1-7数字或中文（周一/星期一）", "1 或 周一", "IMP-SCHED-007/008", "1=周一，7=周日"));
        rows.add(Arrays.asList("开始节次", "是", "纯数字或第x节格式", "1", "IMP-SCHED-009/010", "需与系统节次配置一致"));
        rows.add(Arrays.asList("结束节次", "是", "纯数字或第x节格式，且>=开始节次", "2", "IMP-SCHED-009/010", "需与系统节次配置一致"));
        rows.add(Arrays.asList("教室", "否", "不超过50字符", "A101", "-", "选填"));
        rows.add(Arrays.asList("学期", "是", "不超过20字符", "2024-2025-2", "IMP-SCHED-011", "按系统已有学期规则填写"));
        rows.add(Arrays.asList("学年", "是", "四位数字", "2024", "IMP-SCHED-012/013", "建议与学期一致"));
        return rows;
    }
}
