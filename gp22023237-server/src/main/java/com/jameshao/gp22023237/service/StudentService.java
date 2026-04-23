package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.DTO.importtask.StudentImportTaskDTO;
import com.jameshao.gp22023237.po.Student;
import org.springframework.web.multipart.MultipartFile;

/**
* @author test
* @description 针对表【student(学生信息表)】的数据库操作Service
* @createDate 2025-10-08 17:09:36
*/
public interface StudentService extends IService<Student> {

    StudentImportTaskDTO createImportTask();

    StudentImportTaskDTO queryImportTask(String taskId);

    void startImportTask(String taskId, MultipartFile file, Long classId);
}
