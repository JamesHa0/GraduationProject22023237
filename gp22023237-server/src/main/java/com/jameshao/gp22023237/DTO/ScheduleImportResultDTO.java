package com.jameshao.gp22023237.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 排课导入结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleImportResultDTO {

    private Integer total;
    private Integer successCount;
    private Integer failCount;
    private List<FailDetail> failDetails;

    @Data
    @NoArgsConstructor
    public static class FailDetail {
        private Integer row;
        private String teacherNo;
        private String courseNo;
        private String courseName;
        private String className;
        private String reason;
        private String errorCode;
        private String field;
        private String suggestion;
        private String rule;

        public FailDetail(Integer row, String teacherNo, String courseNo, String courseName, String className, String reason, String errorCode, String field, String suggestion, String rule) {
            this.row = row;
            this.teacherNo = teacherNo;
            this.courseNo = courseNo;
            this.courseName = courseName;
            this.className = className;
            this.reason = reason;
            this.errorCode = errorCode;
            this.field = field;
            this.suggestion = suggestion;
            this.rule = rule;
        }
    }
}
