package com.jameshao.gp22023237.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.DTO.CourseImportDTO;
import com.jameshao.gp22023237.DTO.CourseImportResultDTO;
import com.jameshao.gp22023237.mapper.CourseMapper;
import com.jameshao.gp22023237.po.Course;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.po.User;
import com.jameshao.gp22023237.service.CourseService;
import com.jameshao.gp22023237.service.TeacherService;
import com.jameshao.gp22023237.service.UserService;
import com.jameshao.gp22023237.utils.CourseImportListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.Date;

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
    private UserService userService;

    @Autowired
    private CourseMapper courseMapper;

    @Value("${course.import.batch-size:500}")
    private Integer importBatchSize;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseImportResultDTO importCourses(MultipartFile file) {
        long start = System.currentTimeMillis();
        try {
            Long defaultTeacherId = getOrCreateDefaultTeacher();
            int batchSize = (importBatchSize == null || importBatchSize <= 0) ? 500 : importBatchSize;

            CourseImportListener listener = new CourseImportListener(
                    this,
                    teacherService,
                    courseMapper,
                    defaultTeacherId,
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

    /**
     * 获取或创建默认的"待定教师"
     */
    @Transactional(rollbackFor = Exception.class)
    private Long getOrCreateDefaultTeacher() {
        QueryWrapper<Teacher> wrapper = new QueryWrapper<>();
        wrapper.eq("teacher_no", "TBD001");
        Teacher defaultTeacher = teacherService.getOne(wrapper);

        if (defaultTeacher != null) {
            return defaultTeacher.getId();
        }

        User user = new User();
        user.setUsername("tbd_teacher");
        user.setPassword("tbd_teacher");
        user.setName("待定教师");
        user.setRoleId(3);
        user.setStatus(1);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        userService.save(user);

        defaultTeacher = new Teacher();
        defaultTeacher.setUserId(user.getId());
        defaultTeacher.setTeacherNo("TBD001");
        defaultTeacher.setTeacherName("待定教师");
        defaultTeacher.setTitle("待定");
        defaultTeacher.setDepartment("待定");
        defaultTeacher.setIsMentor(0);
        defaultTeacher.setCreateTime(new Date());
        defaultTeacher.setUpdateTime(new Date());
        teacherService.save(defaultTeacher);

        return defaultTeacher.getId();
    }
}
