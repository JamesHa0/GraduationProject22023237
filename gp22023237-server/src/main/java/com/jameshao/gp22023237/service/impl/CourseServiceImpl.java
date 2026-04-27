package com.jameshao.gp22023237.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.DTO.CourseImportDTO;
import com.jameshao.gp22023237.DTO.CourseImportResultDTO;
import com.jameshao.gp22023237.mapper.CourseMapper;
import com.jameshao.gp22023237.po.Course;
import com.jameshao.gp22023237.service.CourseService;
import com.jameshao.gp22023237.service.TeacherService;
import com.jameshao.gp22023237.utils.CourseImportListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

/**
 * @author test
 * @description 针对表【course(课程表)】的数据库操作Service实现
 * @createDate 2025-10-08 17:09:36
 */
@Slf4j
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course>
        implements CourseService {


    @Autowired
    private TeacherService teacherService;

    @Autowired
    private CourseMapper courseMapper;

    @Value("${course.import.batch-size:500}")
    private Integer importBatchSize;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseImportResultDTO importCourses(MultipartFile file) {
        long start = System.currentTimeMillis();
        try {
            int batchSize = (importBatchSize == null || importBatchSize <= 0) ? 500 : importBatchSize;

            CourseImportListener listener = new CourseImportListener(
                    this,
                    teacherService,
                    courseMapper,
                    batchSize
            );
            EasyExcel.read(file.getInputStream(), CourseImportDTO.class, listener).sheet().doRead();
            CourseImportResultDTO result = listener.getResult();
            long cost = System.currentTimeMillis() - start;
            log.info("课程导入完成，总记录:{}，成功:{}，失败:{}，总耗时:{}ms，批次:{}",
                    result.getTotal(), result.getSuccessCount(), result.getFailCount(), cost, batchSize);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("解析Excel文件失败，请检查文件格式", e);
        }
    }
}
