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
     * 查询课程列表，关联教师信息
     */
    List<CourseWithTeacherDTO> listCourseWithTeacher(@Param("name") String name,
                                                  @Param("courseNo") String courseNo,
                                                  @Param("status") Integer status,
                                                  @Param("semester") String semester);

    /**
     * 根据ID查询课程详情，关联教师信息
     */
    CourseWithTeacherDTO getCourseWithTeacherById(@Param("id") Long id);
}
