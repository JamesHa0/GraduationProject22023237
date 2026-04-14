package com.jameshao.gp22023237.controller.course;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jameshao.gp22023237.DTO.CourseImportDTO;
import com.jameshao.gp22023237.DTO.CourseImportResultDTO;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.DTO.CourseWithTeacherDTO;
import com.jameshao.gp22023237.mapper.CourseMapper;
import com.jameshao.gp22023237.po.Course;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.po.User;
import com.jameshao.gp22023237.service.CourseService;
import com.jameshao.gp22023237.service.TeacherService;
import com.jameshao.gp22023237.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/course")
public class CourseController {

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
    public String list(String name, String courseNo, Integer status, String semester) {
        try {
            List<CourseWithTeacherDTO> list = courseMapper.listCourseWithTeacher(name, courseNo, status, semester);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/teachers")
    public String listTeachers() {
        try {
            QueryWrapper<Teacher> wrapper = new QueryWrapper<>();
            wrapper.ne("teacher_no", "TBD001"); // 排除"待定教师"
            wrapper.orderByAsc("teacher_no");
            List<Teacher> list = teacherService.list(wrapper);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id) {
        try {
            CourseWithTeacherDTO course = courseMapper.getCourseWithTeacherById(id);
            return jsonReturn.returnSuccess(course);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/add")
    public String add(@RequestBody Course course) {
        try {
            // 设置创建时间和更新时间
            course.setCreateTime(new Date());
            course.setUpdateTime(new Date());

            // 如果teacherId为0，表示待定，查找或创建默认教师
            if (course.getTeacherId() != null && course.getTeacherId() <= 0) {
                Long defaultTeacherId = getOrCreateDefaultTeacher();
                course.setTeacherId(defaultTeacherId);
            }

            boolean success = courseService.save(course);
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

    /**
     * 获取或创建默认的"待定教师"
     */
    @Transactional(rollbackFor = Exception.class)
    private Long getOrCreateDefaultTeacher() {
        // 先查找是否已有"待定教师"
        QueryWrapper<Teacher> wrapper = new QueryWrapper<>();
        wrapper.eq("teacher_no", "TBD001");
        Teacher defaultTeacher = teacherService.getOne(wrapper);

        if (defaultTeacher != null) {
            return defaultTeacher.getId();
        }

        // 如果没有，先创建用户账号
        User user = new User();
        user.setUsername("tbd_teacher");
        user.setPassword("tbd_teacher");
        user.setName("待定教师");
        user.setRoleId(3); // 3-指导教师
        user.setStatus(1);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        userService.save(user);

        // 再创建教师
        defaultTeacher = new Teacher();
        defaultTeacher.setUserId(user.getId());
        defaultTeacher.setTeacherNo("TBD001");
        defaultTeacher.setTeacherName("待定教师");
        defaultTeacher.setTitle("待定");
        defaultTeacher.setDepartment("待定");
        defaultTeacher.setIsMentor(0);
        defaultTeacher.setCreateTime(new Date());
        defaultTeacher.setUpdateTime(new Date());
        teacherService.save(defaultTeacher);

        return defaultTeacher.getId();
    }

    @PutMapping("/update")
    public String update(@RequestBody Course course) {
        try {
            // 设置更新时间
            course.setUpdateTime(new Date());

            // 如果teacherId为0，表示待定，查找或创建默认教师
            if (course.getTeacherId() != null && course.getTeacherId() <= 0) {
                Long defaultTeacherId = getOrCreateDefaultTeacher();
                course.setTeacherId(defaultTeacherId);
            }

            boolean success = courseService.updateById(course);
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
            boolean success = courseService.removeById(id);
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
            boolean success = courseService.removeByIds(ids);
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
     * 批量导入课程
     */
    @PostMapping("/import")
    public String importCourses(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return jsonReturn.returnFailed("请选择要上传的文件");
            }

            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".xls") && !filename.endsWith(".xlsx"))) {
                return jsonReturn.returnFailed("请上传Excel文件(.xls或.xlsx)");
            }

            CourseImportResultDTO result = courseService.importCourses(file);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("导入失败: " + e.getMessage());
        }
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/importTemplate")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("课程导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            List<CourseImportDTO> dataList = new ArrayList<>();
            CourseImportDTO example = new CourseImportDTO();
            example.setCourseNo("CS101");
            example.setName("数据结构");
            example.setCredit(3.0);
            example.setHours(48);
            example.setTeacherNo("T001");
            example.setSemester("2024-2025-2");
            example.setYear(2024);
            example.setMaxStudents(50);
            example.setDayOfWeek(1);
            example.setStartTime("08:00:00");
            example.setEndTime("09:40:00");
            example.setClassroom("A201");
            example.setMaxCredits(6.0);
            example.setStatus(0);
            example.setStudyNature("必修");
            example.setTextbook(0);
            example.setExternalSelection(0);
            example.setDescription("计算机专业核心课程");
            example.setRemark("");
            dataList.add(example);

            EasyExcel.write(response.getOutputStream(), CourseImportDTO.class)
                    .sheet("课程模板")
                    .doWrite(dataList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
