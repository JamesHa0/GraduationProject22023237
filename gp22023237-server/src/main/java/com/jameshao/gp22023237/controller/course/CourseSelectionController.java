package com.jameshao.gp22023237.controller.course;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.Course;
import com.jameshao.gp22023237.po.CourseSelection;
import com.jameshao.gp22023237.service.CourseSelectionService;
import com.jameshao.gp22023237.service.CourseService;
import com.jameshao.gp22023237.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/list")
    public String list(Long studentId, Long courseId, Integer status) {
        try {
            LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(!ObjectUtils.isEmpty(studentId), CourseSelection::getStudentId, studentId)
                    .eq(!ObjectUtils.isEmpty(courseId), CourseSelection::getCourseId, courseId)
                    .eq(!ObjectUtils.isEmpty(status), CourseSelection::getStatus, status)
                    .orderByDesc(CourseSelection::getSelectionTime);
            List<CourseSelection> list = courseSelectionService.list(queryWrapper);
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

    @PostMapping("/add")
    public String add(@RequestBody CourseSelection courseSelection) {
        try {
            // 检查选课条件
            String checkResult = checkSelectionConstraints(courseSelection);
            if (!"OK".equals(checkResult)) {
                return jsonReturn.returnFailed(checkResult);
            }

            boolean success = courseSelectionService.save(courseSelection);
            if (success) {
                return jsonReturn.returnSuccess("选课成功");
            } else {
                return jsonReturn.returnFailed("选课失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 检查选课约束条件
     */
    private String checkSelectionConstraints(CourseSelection courseSelection) {
        Long studentId = courseSelection.getStudentId();
        Long courseId = courseSelection.getCourseId();

        // 1. 检查学生是否已经选择了该课程
        LambdaQueryWrapper<CourseSelection> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getStatus, 1);
        CourseSelection exist = courseSelectionService.getOne(existWrapper);
        if (exist != null) {
            return "您已经选择了该课程，请勿重复选课";
        }

        // 2. 检查课程是否存在且已开课
        Course course = courseService.getById(courseId);
        if (course == null) {
            return "课程不存在";
        }
        if (course.getStatus() != 1) {
            return "该课程未开课，无法选择";
        }

        // 3. 检查人数限制
        LambdaQueryWrapper<CourseSelection> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getStatus, 1);
        long currentCount = courseSelectionService.count(countWrapper);
        if (course.getMaxStudents() != null && currentCount >= course.getMaxStudents()) {
            return "该课程选课人数已满";
        }

        // 4. 检查时间冲突
        String conflictResult = checkTimeConflict(studentId, course);
        if (conflictResult != null) {
            return conflictResult;
        }

        // 5. 检查学分限制
        String creditResult = checkCreditLimit(studentId, course);
        if (creditResult != null) {
            return creditResult;
        }

        return "OK";
    }

    /**
     * 检查时间冲突
     */
    private String checkTimeConflict(Long studentId, Course newCourse) {
        // 如果新课程没有时间信息，跳过检查
        if (newCourse.getDayOfWeek() == null || newCourse.getStartTime() == null) {
            return null;
        }

        // 查询学生已选课程
        LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getStatus, 1);
        List<CourseSelection> selections = courseSelectionService.list(wrapper);

        for (CourseSelection selection : selections) {
            Course existingCourse = courseService.getById(selection.getCourseId());
            if (existingCourse == null || existingCourse.getDayOfWeek() == null) {
                continue;
            }

            // 检查是否同一天
            if (!newCourse.getDayOfWeek().equals(existingCourse.getDayOfWeek())) {
                continue;
            }

            // 检查时间是否重叠
            if (isTimeOverlap(newCourse.getStartTime(), newCourse.getEndTime(),
                            existingCourse.getStartTime(), existingCourse.getEndTime())) {
                return "与已选课程《" + existingCourse.getName() + "》时间冲突";
            }
        }

        return null;
    }

    /**
     * 判断两个时间段是否重叠
     */
    private boolean isTimeOverlap(String start1, String end1, String start2, String end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }

        try {
            int s1 = timeToMinutes(start1);
            int e1 = timeToMinutes(end1);
            int s2 = timeToMinutes(start2);
            int e2 = timeToMinutes(end2);

            // 两个时间段不重叠的条件：end1 <= start2 或 end2 <= start1
            // 重叠的条件：s1 < e2 && s2 < e1
            return s1 < e2 && s2 < e1;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将时间字符串转换为分钟数
     */
    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    /**
     * 检查学分限制
     */
    private String checkCreditLimit(Long studentId, Course newCourse) {
        // 如果课程没有学分信息，跳过检查
        if (newCourse.getCredit() == null) {
            return null;
        }

        // 查询学生已选课程的总学分
        LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getStatus, 1);
        List<CourseSelection> selections = courseSelectionService.list(wrapper);

        double totalCredits = 0;
        for (CourseSelection selection : selections) {
            Course existingCourse = courseService.getById(selection.getCourseId());
            if (existingCourse != null && existingCourse.getCredit() != null) {
                totalCredits += existingCourse.getCredit();
            }
        }

        // 检查是否超过最大学分限制（假设每学期最多20学分）
        double maxCredits = 20.0;
        if (newCourse.getMaxCredits() != null) {
            maxCredits = newCourse.getMaxCredits();
        }

        if (totalCredits + newCourse.getCredit() > maxCredits) {
            return "选课后总学分将超过限制（最多" + maxCredits + "学分）";
        }

        return null;
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
                return jsonReturn.returnSuccess("退课成功");
            } else {
                return jsonReturn.returnFailed("退课失败");
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
