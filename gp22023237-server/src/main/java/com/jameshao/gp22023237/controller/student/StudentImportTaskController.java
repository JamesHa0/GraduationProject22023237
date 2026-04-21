package com.jameshao.gp22023237.controller.student;

import com.jameshao.gp22023237.DTO.importtask.StudentImportTaskDTO;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student/importTask")
public class StudentImportTaskController {

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private StudentService studentService;

    @PostMapping("/create")
    public String createTask() {
        try {
            StudentImportTaskDTO task = studentService.createImportTask();
            return jsonReturn.returnSuccess(task);
        } catch (Exception e) {
            return jsonReturn.returnError("创建学生导入任务失败");
        }
    }

    @GetMapping("/{taskId}")
    public String queryTask(@PathVariable String taskId) {
        try {
            StudentImportTaskDTO task = studentService.queryImportTask(taskId);
            if (task == null) {
                return jsonReturn.returnFailed("任务不存在或已过期");
            }
            return jsonReturn.returnSuccess(task);
        } catch (Exception e) {
            return jsonReturn.returnError("查询学生导入任务失败");
        }
    }
}
