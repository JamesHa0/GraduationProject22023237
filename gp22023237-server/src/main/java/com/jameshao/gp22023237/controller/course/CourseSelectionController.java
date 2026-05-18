package com.jameshao.gp22023237.controller.course;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jameshao.gp22023237.DTO.CourseSelectionWithDetailsDTO;
import com.jameshao.gp22023237.DTO.BatchCourseSelectionDTO;
import com.jameshao.gp22023237.annotation.Log;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.common.enums.BusinessType;
import com.jameshao.gp22023237.mapper.CourseMapper;
import com.jameshao.gp22023237.mapper.CourseSelectionMapper;
import com.jameshao.gp22023237.mapper.ScheduleMapper;
import com.jameshao.gp22023237.po.Course;
import com.jameshao.gp22023237.DTO.CourseWithTeacherDTO;
import com.jameshao.gp22023237.po.CourseSelection;
import com.jameshao.gp22023237.po.Schedule;
import com.jameshao.gp22023237.po.Student;
import com.jameshao.gp22023237.service.CourseSelectionService;
import com.jameshao.gp22023237.service.CoursePhaseService;
import com.jameshao.gp22023237.service.CourseService;
import com.jameshao.gp22023237.service.NoticeService;
import com.jameshao.gp22023237.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/course/selection")
public class CourseSelectionController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CourseSelectionController.class);

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private CourseSelectionService courseSelectionService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private CourseSelectionMapper courseSelectionMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CoursePhaseService coursePhaseService;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @GetMapping("/list")
    public String list(Long studentId, Long courseId, Integer status, String semester) {
        try {
            List<CourseSelectionWithDetailsDTO> list = courseSelectionMapper.listSelectionWithCourseDetails(studentId, courseId, status, semester);
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
            List<CourseSelectionWithDetailsDTO> result = courseSelectionMapper.listSelectionWithCourseDetails(studentId, null, 1, null);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 查询学生是否已提交选课
     */
    @GetMapping("/getSubmitStatus")
    public String getSubmitStatus(Long studentId) {
        try {
            if (studentId == null) {
                return jsonReturn.returnError("学生ID不能为空");
            }
            LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CourseSelection::getStudentId, studentId)
                    .isNotNull(CourseSelection::getSubmitTime);
            long count = courseSelectionService.count(wrapper);
            return jsonReturn.returnSuccess(count > 0);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @Log(title = "选课管理", businessType = BusinessType.INSERT)
    @PostMapping("/save")
    @Transactional(rollbackFor = Exception.class)
    public String save(@RequestBody BatchCourseSelectionDTO batchDTO) {
        try {
            // 校验选课时间窗口
            if (!coursePhaseService.isSelectionOpen()) {
                return jsonReturn.returnError("当前不在选课时间窗口内，无法选课");
            }

            System.out.println("保存学生选课:" + batchDTO);

            if (batchDTO.getStudentId() == null) {
                return jsonReturn.returnError("学生ID不能为空");
            }
            if (batchDTO.getChoices() == null || batchDTO.getChoices().isEmpty()) {
                return jsonReturn.returnError("选课列表不能为空");
            }

            // 检查是否已提交
            LambdaQueryWrapper<CourseSelection> checkSubmitWrapper = new LambdaQueryWrapper<>();
            checkSubmitWrapper.eq(CourseSelection::getStudentId, batchDTO.getStudentId())
                    .isNotNull(CourseSelection::getSubmitTime);
            long submittedCount = courseSelectionService.count(checkSubmitWrapper);
            if (submittedCount > 0) {
                return jsonReturn.returnError("您已提交选课，不可修改");
            }

            // 获取学生所在班级
            Student student = studentService.getById(batchDTO.getStudentId());
            Long studentClassId = student != null ? student.getClassId() : null;

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
            }

            // 通过schedule表检查时间冲突
            String timeConflict = checkTimeConflictViaSchedule(studentClassId, courseIds, batchDTO.getStudentId());
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
                cs.setSubmitTime(now);
                cs.setCreateTime(now);
                cs.setUpdateTime(now);

                boolean saveResult = courseSelectionService.save(cs);
                if (!saveResult) {
                    throw new RuntimeException("保存第" + index + "个选课失败");
                }
            }

            // 5.2 通知：选课提交 → 通知学生
            try {
                Student stu = studentService.getById(batchDTO.getStudentId());
                if (stu != null && stu.getUserId() != null) {
                    noticeService.createAndPush("选课通知", "选课已提交，共选" + batchDTO.getChoices().size() + "门课程", "1", stu.getUserId());
                }
            } catch (Exception ex) {
                logger.warn("选课提交通知推送失败: {}", ex.getMessage());
            }
            return jsonReturn.returnSuccess("保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 通过schedule表检查选课时间冲突
     * 逻辑：查询学生班级对应的所有schedule记录，检查所选课程的排课时间是否有重叠
     */
    private String checkTimeConflictViaSchedule(Long studentClassId, List<Long> courseIds, Long studentId) {
        if (studentClassId == null) {
            return null; // 无班级信息，跳过冲突检测
        }

        // 查询学生班级的所有排课记录
        LambdaQueryWrapper<Schedule> classWrapper = new LambdaQueryWrapper<>();
        classWrapper.eq(Schedule::getClassId, studentClassId);
        List<Schedule> classSchedules = scheduleMapper.selectList(classWrapper);

        // 筛选出所选课程对应的排课
        Set<Long> courseIdSet = new HashSet<>(courseIds);
        List<Schedule> selectedSchedules = new ArrayList<>();
        for (Schedule s : classSchedules) {
            if (courseIdSet.contains(s.getCourseId())) {
                selectedSchedules.add(s);
            }
        }

        // 检查排课之间是否有时间冲突（范围重叠判断）
        for (int i = 0; i < selectedSchedules.size(); i++) {
            Schedule s1 = selectedSchedules.get(i);
            if (s1.getDayOfWeek() == null || s1.getStartSection() == null || s1.getEndSection() == null) {
                continue;
            }
            for (int j = i + 1; j < selectedSchedules.size(); j++) {
                Schedule s2 = selectedSchedules.get(j);
                if (s2.getDayOfWeek() == null || s2.getStartSection() == null || s2.getEndSection() == null) {
                    continue;
                }
                // 同一天 + 节次范围重叠
                if (s1.getDayOfWeek().equals(s2.getDayOfWeek())
                        && s1.getStartSection() <= s2.getEndSection()
                        && s1.getEndSection() >= s2.getStartSection()) {
                    // 获取课程名称用于提示
                    Course c1 = courseService.getById(s1.getCourseId());
                    Course c2 = courseService.getById(s2.getCourseId());
                    String name1 = c1 != null ? c1.getName() : "课程" + s1.getCourseId();
                    String name2 = c2 != null ? c2.getName() : "课程" + s2.getCourseId();
                    return "课程《" + name1 + "》与《" + name2 + "》时间冲突";
                }
            }
        }
        return null;
    }

    @Log(title = "选课管理", businessType = BusinessType.UPDATE)
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

    @Log(title = "选课管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        try {
            // 先获取记录信息用于通知
            CourseSelection cs = courseSelectionService.getById(id);
            if (cs == null) {
                return jsonReturn.returnError("记录不存在");
            }
            Long studentId = cs.getStudentId();
            Long courseId = cs.getCourseId();
            boolean success = courseSelectionService.removeById(id);
            if (success) {
                // 5.3 通知：退课 → 通知学生
                try {
                    Long studentUserId = noticeService.getStudentUserId(studentId);
                    if (studentUserId != null) {
                        Course course = courseService.getById(courseId);
                        String courseName = course != null ? course.getName() : "课程";
                        noticeService.createAndPush("退课通知", "已退选课程：" + courseName, "1", studentUserId);
                    }
                } catch (Exception ex) {
                    logger.warn("退课通知推送失败: {}", ex.getMessage());
                }
                return jsonReturn.returnSuccess("删除成功");
            } else {
                return jsonReturn.returnFailed("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @Log(title = "选课管理", businessType = BusinessType.DELETE)
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
