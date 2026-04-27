package com.jameshao.gp22023237.service;

import com.jameshao.gp22023237.DTO.CourseImportResultDTO;
import com.jameshao.gp22023237.po.Course;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
* @author test
* @description 针对表【course(课程表)】的数据库操作Service
* @createDate 2025-10-08 17:09:36
*/
public interface CourseService extends IService<Course> {

    /**
     * 批量导入课程
     * @param file Excel文件
     * @return 导入结果
     */
    CourseImportResultDTO importCourses(MultipartFile file);

}
