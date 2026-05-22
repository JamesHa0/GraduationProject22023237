package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.Course;
import com.jameshao.gp22023237.po.CourseSelection;
import com.jameshao.gp22023237.service.CourseSelectionService;
import com.jameshao.gp22023237.service.CourseService;
import com.jameshao.gp22023237.service.SystemConfigService;
import com.jameshao.gp22023237.mapper.CourseSelectionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author test
* @description 针对表【course_selection(选课记录表)】的数据库操作Service实现
* @createDate 2025-10-08 17:09:36
*/
@Service
public class CourseSelectionServiceImpl extends ServiceImpl<CourseSelectionMapper, CourseSelection>
    implements CourseSelectionService{

    /** 默认单门课程容量（未设置时） */
    private static final int DEFAULT_CAPACITY = 50;

    /** 默认每位学生最多选课数 */
    private static final int DEFAULT_MAX_PER_STUDENT = 10;

    /** 系统配置：每位学生最大选课数 */
    private static final String CONFIG_KEY_MAX_PER_STUDENT = "course_selection_max_per_student";

    @Autowired
    private CourseService courseService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    public String checkCourseCapacity(Long courseId) {
        Course course = courseService.getById(courseId);
        if (course == null) {
            return "课程不存在";
        }

        int capacity = course.getCapacity() != null ? course.getCapacity() : DEFAULT_CAPACITY;

        // 统计当前正常选课人数（status=1）
        LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getStatus, 1);
        long selectedCount = this.count(wrapper);

        if (selectedCount >= capacity) {
            return "课程《" + course.getName() + "》选课人数已满（" + selectedCount + "/" + capacity + "）";
        }
        return null;
    }

    @Override
    public String checkStudentCourseLimit(Long studentId, int newSelectionCount) {
        int maxPerStudent = DEFAULT_MAX_PER_STUDENT;
        try {
            String configValue = systemConfigService.getConfigValue(CONFIG_KEY_MAX_PER_STUDENT);
            if (configValue != null && !configValue.isEmpty()) {
                maxPerStudent = Integer.parseInt(configValue);
            }
        } catch (Exception e) {
            // 配置读取失败时使用默认值
        }

        if (newSelectionCount > maxPerStudent) {
            return "本次选课数量（" + newSelectionCount + "）超过每位学生最大选课数（" + maxPerStudent + "）";
        }
        return null;
    }

}
