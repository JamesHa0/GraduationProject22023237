package com.jameshao.gp22023237.controller.course;

import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.DTO.importtask.CourseImportTaskDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/course/importTask")
public class CourseImportTaskController {

    private static final Map<String, CourseImportTaskDTO> TASK_STORE = new ConcurrentHashMap<>();

    @Autowired
    private JSONReturn jsonReturn;

    @PostMapping("/create")
    public String createTask() {
        try {
            CourseImportTaskDTO task = new CourseImportTaskDTO();
            task.setTaskId(UUID.randomUUID().toString().replace("-", ""));
            task.setStatus("PENDING");
            task.setTotal(0);
            task.setSuccessCount(0);
            task.setFailCount(0);
            task.setProgress(0);
            task.setMessage("异步导入功能规划中，当前仍使用同步导入。请继续使用“批量导入”按钮上传模板。"
            );
            task.setCreatedAt(new Date());
            task.setUpdatedAt(new Date());
            TASK_STORE.put(task.getTaskId(), task);
            return jsonReturn.returnSuccess(task);
        } catch (Exception e) {
            return jsonReturn.returnError("创建导入任务失败");
        }
    }

    @GetMapping("/{taskId}")
    public String queryTask(@PathVariable String taskId) {
        try {
            CourseImportTaskDTO task = TASK_STORE.get(taskId);
            if (task == null) {
                return jsonReturn.returnFailed("任务不存在或已过期");
            }
            return jsonReturn.returnSuccess(task);
        } catch (Exception e) {
            return jsonReturn.returnError("查询导入任务失败");
        }
    }

    @GetMapping("/{taskId}/failFile")
    public void downloadFailFile(@PathVariable String taskId, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getOutputStream().write(("任务 " + taskId + " 的失败明细文件功能暂未启用").getBytes(StandardCharsets.UTF_8));
    }
}
