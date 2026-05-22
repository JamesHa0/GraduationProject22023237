package com.jameshao.gp22023237.mapper;

import com.jameshao.gp22023237.DTO.CourseSelectionWithDetailsDTO;
import com.jameshao.gp22023237.po.CourseSelection;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
* @author test
* @description 针对表【course_selection(选课记录表)】的数据库操作Mapper
* @createDate 2025-10-08 17:09:36
* @Entity com.jameshao.gp22023237.po.CourseSelection
*/
@Mapper
public interface CourseSelectionMapper extends BaseMapper<CourseSelection> {

    /**
     * 查询选课记录列表，关联课程和教师信息
     */
    List<CourseSelectionWithDetailsDTO> listSelectionWithCourseDetails(@Param("studentId") Long studentId,
                                                                       @Param("courseId") Long courseId,
                                                                       @Param("status") Integer status,
                                                                       @Param("semester") String semester);

    /**
     * 分页查询选课记录列表
     */
    List<CourseSelectionWithDetailsDTO> listSelectionWithCourseDetailsPage(@Param("studentId") Long studentId,
                                                                            @Param("courseId") Long courseId,
                                                                            @Param("status") Integer status,
                                                                            @Param("semester") String semester,
                                                                            @Param("offset") Integer offset,
                                                                            @Param("pageSize") Integer pageSize);

    /**
     * 统计选课记录总数
     */
    int countSelectionWithCourseDetails(@Param("studentId") Long studentId,
                                         @Param("courseId") Long courseId,
                                         @Param("status") Integer status,
                                         @Param("semester") String semester);

    /**
     * 批量查询课程已选人数（status=1 已通过）
     */
    List<Map<String, Object>> batchCountSelectedByCourseIds(@Param("courseIds") List<Long> courseIds);
}
