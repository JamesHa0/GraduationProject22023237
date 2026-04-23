package com.jameshao.gp22023237.mapper;

import com.jameshao.gp22023237.po.Course;
import com.jameshao.gp22023237.DTO.CourseWithTeacherDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author test
 * @description 针对表【course(课程表)】的数据库操作Mapper
 * @createDate 2025-10-08 17:09:36
 * @Entity com.jameshao.gp22023237.po.Course
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 查询课程列表
     */
    List<CourseWithTeacherDTO> listCourseWithTeacher(@Param("name") String name,
                                                     @Param("courseNo") String courseNo,
                                                     @Param("status") Integer status,
                                                     @Param("semester") String semester);

    /**
     * 查询课程列表（分页）
     */
    List<CourseWithTeacherDTO> listCourseWithTeacherPage(@Param("name") String name,
                                                         @Param("courseNo") String courseNo,
                                                         @Param("status") Integer status,
                                                         @Param("semester") String semester,
                                                         @Param("offset") Integer offset,
                                                         @Param("pageSize") Integer pageSize);

    /**
     * 统计课程总数
     */
    int countCourseWithTeacher(@Param("name") String name,
                              @Param("courseNo") String courseNo,
                              @Param("status") Integer status,
                              @Param("semester") String semester);

    /**
     * 根据ID查询课程详情
     */
    CourseWithTeacherDTO getCourseWithTeacherById(@Param("id") Long id);

    /**
     * 按课程编号集合批量查询，用于导入前预加载校验
     */
    List<Course> listByCourseNos(@Param("courseNos") List<String> courseNos);
}
