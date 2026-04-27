package com.jameshao.gp22023237.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 课程导入结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseImportResultDTO {

    /**
     * 总记录数
     */
    private Integer total;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failCount;

    /**
     * 失败详情
     */
    private List<FailDetail> failDetails;

    @Data
    @NoArgsConstructor
    public static class FailDetail {
        /**
         * 行号（从2开始，第1行是表头）
         */
        private Integer row;

        /**
         * 课程编号
         */
        private String courseNo;

        /**
         * 失败原因
         */
        private String reason;

        /**
         * 错误码
         */
        private String errorCode;

        /**
         * 出错字段
         */
        private String field;

        /**
         * 修复建议
         */
        private String suggestion;

        /**
         * 模板规则标识
         */
        private String rule;

        public FailDetail(Integer row, String courseNo, String reason) {
            this.row = row;
            this.courseNo = courseNo;
            this.reason = reason;
        }

        public FailDetail(Integer row, String courseNo, String reason, String errorCode, String field, String suggestion, String rule) {
            this.row = row;
            this.courseNo = courseNo;
            this.reason = reason;
            this.errorCode = errorCode;
            this.field = field;
            this.suggestion = suggestion;
            this.rule = rule;
        }
    }
}
