package com.jameshao.gp22023237.common.error;

/**
 * 成绩导入错误码枚举
 */
public enum ScoreImportErrorCode {
    STUDENT_NO_REQUIRED("IMP-SCORE-001", "学号不能为空", "studentNo", "请填写系统内已有的学号", "R-STUDENT-NO-REQUIRED"),
    STUDENT_NOT_FOUND("IMP-SCORE-002", "学号在系统中不存在", "studentNo", "请确认学号是否正确，需先在学生管理中录入", "R-STUDENT-NO-EXISTS"),
    STUDENT_NO_DUPLICATE_FILE("IMP-SCORE-003", "文件内学号重复", "studentNo", "同一导入文件中学号必须唯一", "R-STUDENT-NO-UNIQUE"),
    USUAL_SCORE_RANGE("IMP-SCORE-004", "平时成绩必须在0-100之间", "usualScore", "请填写0-100的数值", "R-USUAL-SCORE-RANGE"),
    EXAM_SCORE_RANGE("IMP-SCORE-005", "期末成绩必须在0-100之间", "examScore", "请填写0-100的数值", "R-EXAM-SCORE-RANGE"),
    SCORE_DUPLICATE("IMP-SCORE-006", "该学生已有成绩记录（将更新）", "studentNo", "如需覆盖请确认", "R-SCORE-UPDATE"),
    SYSTEM_ERROR("IMP-SCORE-SYS-500", "系统处理失败", "system", "请联系管理员并重试", "R-SYSTEM");

    private final String code;
    private final String message;
    private final String field;
    private final String suggestion;
    private final String rule;

    ScoreImportErrorCode(String code, String message, String field, String suggestion, String rule) {
        this.code = code;
        this.message = message;
        this.field = field;
        this.suggestion = suggestion;
        this.rule = rule;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getField() { return field; }
    public String getSuggestion() { return suggestion; }
    public String getRule() { return rule; }
}
