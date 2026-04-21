package com.jameshao.gp22023237.service;

import com.jameshao.gp22023237.DTO.importtask.CourseImportTaskDTO;

public interface CourseImportTaskService {

    CourseImportTaskDTO createTask();

    CourseImportTaskDTO queryTask(String taskId);
}
