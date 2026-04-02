package com.jameshao.gp22023237.controller.course;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jameshao.gp22023237.DTO.CourseSelectionWithDetailsDTO;
import com.jameshao.gp22023237.DTO.BatchCourseSelectionDTO;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.mapper.CourseMapper;
import com.jameshao.gp22023237.mapper.CourseSelectionMapper;
import com.jameshao.gp22023237.po.Course;
import com.jameshao.gp22023237.DTO.CourseWithTeacherDTO;
import com.jameshao.gp22023237.po.CourseSelection;
import com.jameshao.gp22023237.service.CourseSelectionService;
import com.jameshao.gp22023237.service.CourseService;
import com.jameshao.gp22023237.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/course/selection")
public class CourseSelectionController {

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private CourseSelectionService courseSelectionService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseSelectionMapper courseSelectionMapper;

    @Autowired
    private CourseMapper courseMapper;

    @GetMapping("/list")
    public String list(Long studentId, Long courseId, Integer status) {
        try {
            List<CourseSelectionWithDetailsDTO> list = courseSelectionMapper.listSelectionWithCourseDetails(studentId, courseId, status);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id) {
        try {
            CourseSelection courseSelection = courseSelectionService.getById(id);
            return jsonReturn.returnSuccess(courseSelection);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/getStudentCourseChoices")
    public String getStudentCourseChoices(Long studentId) {
        try {
            System.out.println("查询学生已选课程:" + studentId);
            List<CourseSelectionWithDetailsDTO> result = courseSelectionMapper.listSelectionWithCourseDetails(studentId, null, 1);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/save")
    @Transactional(rollbackFor = Exception.class)
    public String save(@RequestBody BatchCourseSelectionDTO batchDTO) {
        try {
            System.out.println("保存学生选课:" + batchDTO);

            if (batchDTO.getStudentId() == null) {
                return jsonReturn.returnError("学生ID不能为空");
            }
            if (batchDTO.getChoices() == null || batchDTO.getChoices().isEmpty()) {
                return jsonReturn.returnError("选课列表不能为空");
            }

            Date now = new Date();
            List<Long> courseIds = new ArrayList<>();
            int index = 0;

            for (BatchCourseSelectionDTO.ChoiceItem choice : batchDTO.getChoices()) {
                index++;
                if (choice.getCourseId() == null) {
                    return jsonReturn.returnError("第" + index + "个选课的课程不能为空");
                }
                if (courseIds.contains(choice.getCourseId())) {
                    return jsonReturn.returnError("不能重复选择同一门课程");
                }
                courseIds.add(choice.getCourseId());

                Course course = courseService.getById(choice.getCourseId());
                if (course == null) {
                    return jsonReturn.returnError("第" + index + "个选课的课程不存在");
                }
                if (course.getStatus() != 1) {
                    return jsonReturn.returnError("课程《" + course.getName() + "》未开课，无法选择");
                }

                LambdaQueryWrapper<CourseSelection> countWrapper = new LambdaQueryWrapper<>();
                countWrapper.eq(CourseSelection::getCourseId, choice.getCourseId())
                        .eq(CourseSelection::getStatus, 1);
                long currentCount = courseSelectionService.count(countWrapper);
                if (course.getMaxStudents() != null && currentCount >= course.getMaxStudents()) {
                    return jsonReturn.returnError("课程《" + course.getName() + "》选课人数已满");
                }
            }

            String timeConflict = checkTimeConflict(batchDTO.getStudentId(), courseIds);
            if (timeConflict != null) {
                return jsonReturn.returnError(timeConflict);
            }

            LambdaQueryWrapper<CourseSelection> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(CourseSelection::getStudentId, batchDTO.getStudentId());
            courseSelectionService.remove(deleteWrapper);

            index = 0;
            for (BatchCourseSelectionDTO.ChoiceItem choice : batchDTO.getChoices()) {
                index++;
                CourseSelection cs = new CourseSelection();
                cs.setStudentId(batchDTO.getStudentId());
                cs.setCourseId(choice.getCourseId());
                cs.setStatus(1);
                cs.setSelectionTime(now);
                cs.setCreateTime(now);
                cs.setUpdateTime(now);

                boolean saveResult = courseSelectionService.save(cs);
                if (!saveResult) {
                    throw new RuntimeException("保存第" + index + "个选课失败");
                }
            }

            return jsonReturn.returnSuccess("保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    private String checkTimeConflict(Long studentId, List<Long> courseIds) {
        List<Course> courses = new ArrayList<>();
        for (Long courseId : courseIds) {
            Course course = courseService.getById(courseId);
            if (course != null) {
                courses.add(course);
            }
        }

        for (int i = 0; i < courses.size(); i++) {
            Course course1 = courses.get(i);
            if (course1.getDayOfWeek() == null || course1.getStartTime() == null) {
                continue;
            }
            for (int j = i + 1; j < courses.size(); j++) {
                Course course2 = courses.get(j);
                if (course2.getDayOfWeek() == null || course2.getStartTime() == null) {
                    continue;
                }
                if (course1.getDayOfWeek().equals(course2.getDayOfWeek())) {
                    if (isTimeOverlap(course1.getStartTime(), course1.getEndTime(),
                            course2.getStartTime(), course2.getEndTime())) {
                        return "课程《" + course1.getName() + "》与《" + course2.getName() + "》时间冲突";
                    }
                }
            }
        }
        return null;
    }

    private boolean isTimeOverlap(String start1, String end1, String start2, String end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        try {
            int s1 = timeToMinutes(start1);
            int e1 = timeToMinutes(end1);
            int s2 = timeToMinutes(start2);
            int e2 = timeToMinutes(end2);
            return s1 < e2 && s2 < e1;
        } catch (Exception e) {
            return false;
        }
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    @PutMapping("/update")
    public String update(@RequestBody CourseSelection courseSelection) {
        try {
            boolean success = courseSelectionService.updateById(courseSelection);
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
            boolean success = courseSelectionService.removeById(id);
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
            boolean success = courseSelectionService.removeByIds(ids);
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
}
