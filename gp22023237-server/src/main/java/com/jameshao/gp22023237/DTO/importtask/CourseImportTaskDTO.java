package com.jameshao.gp22023237.DTO.importtask;

import lombok.Data;

import java.util.Date;

@Data
public class CourseImportTaskDTO {
    private String taskId;
    private String status;
    private Integer total;
    private Integer successCount;
    private Integer failCount;
    private Integer progress;
    private String failFileUrl;
    private String message;
    private Date createdAt;
    private Date updatedAt;
}
