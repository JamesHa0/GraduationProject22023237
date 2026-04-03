package com.jameshao.gp22023237.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jameshao.gp22023237.DTO.ScoreWithDetailsDTO;
import com.jameshao.gp22023237.po.Score;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author test
* @description 针对表【score(成绩表)】的数据库操作Mapper
* @createDate 2025-10-08 17:09:36
* @Entity com.jameshao.gp22023237.po.Score
*/
public interface ScoreMapper extends BaseMapper<Score> {

    /**
     * 查询成绩列表，关联课程、学生和教师信息
     */
    List<ScoreWithDetailsDTO> listScoreWithDetails(@Param("studentId") Long studentId,
                                                    @Param("courseId") Long courseId,
                                                    @Param("grade") String grade,
                                                    @Param("semester") String semester);

}
