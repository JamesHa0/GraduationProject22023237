package com.jameshao.gp22023237.service;

import com.jameshao.gp22023237.po.CourseSelection;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author test
* @description 针对表【course_selection(选课记录表)】的数据库操作Service
* @createDate 2025-10-08 17:09:36
*/
public interface CourseSelectionService extends IService<CourseSelection> {

    /**
     * 校验课程容量是否已满
     * @param courseId 课程ID
     * @return 错误消息，null 表示校验通过
     */
    String checkCourseCapacity(Long courseId);

    /**
     * 校验学生选课数量是否达上限
     * @param studentId 学生ID
     * @param newSelectionCount 本次新增选课数
     * @return 错误消息，null 表示校验通过
     */
    String checkStudentCourseLimit(Long studentId, int newSelectionCount);

}
