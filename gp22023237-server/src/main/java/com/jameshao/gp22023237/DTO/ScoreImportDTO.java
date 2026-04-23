package com.jameshao.gp22023237.DTO;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 成绩导入Excel DTO
 */
@Data
public class ScoreImportDTO {

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("平时成绩")
    private Double usualScore;

    @ExcelProperty("期末成绩")
    private Double examScore;

    @ExcelProperty("评语")
    private String comment;
}
