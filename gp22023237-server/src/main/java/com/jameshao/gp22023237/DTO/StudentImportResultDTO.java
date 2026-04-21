package com.jameshao.gp22023237.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 学生导入结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentImportResultDTO {

    private Integer total;
    private Integer successCount;
    private Integer failCount;
    private List<FailDetail> failDetails;

    @Data
    @NoArgsConstructor
    public static class FailDetail {
        private Integer row;
        private String studentNo;
        private String reason;
        private String errorCode;
        private String field;
        private String suggestion;
        private String rule;

        public FailDetail(Integer row, String studentNo, String reason, String errorCode, String field, String suggestion, String rule) {
            this.row = row;
            this.studentNo = studentNo;
            this.reason = reason;
            this.errorCode = errorCode;
            this.field = field;
            this.suggestion = suggestion;
            this.rule = rule;
        }
    }
}
