package com.jameshao.gp22023237.DTO.importtask;

import com.jameshao.gp22023237.DTO.StudentImportResultDTO;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class StudentImportTaskDTO {
    private String taskId;
    private String status;
    private Integer total;
    private Integer successCount;
    private Integer failCount;
    private Integer progress;
    private String message;
    private List<StudentImportResultDTO.FailDetail> failDetails;
    private Date createdAt;
    private Date updatedAt;
}
