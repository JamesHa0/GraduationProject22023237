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
    @AllArgsConstructor
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
    }
}
