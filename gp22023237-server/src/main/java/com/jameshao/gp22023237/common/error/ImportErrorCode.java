package com.jameshao.gp22023237.common.error;

public enum ImportErrorCode {
    COURSE_NO_REQUIRED("IMP-COURSE-001", "课程编号不能为空", "courseNo", "请填写不超过20位的唯一课程编号", "R-COURSE-NO-REQUIRED"),
    COURSE_NO_DUPLICATE_DB("IMP-COURSE-002", "课程编号已存在", "courseNo", "请更换课程编号，避免与系统现有数据重复", "R-COURSE-NO-UNIQUE"),
    COURSE_NO_DUPLICATE_FILE("IMP-COURSE-003", "文件内课程编号重复", "courseNo", "同一导入文件中课程编号必须唯一", "R-COURSE-NO-UNIQUE"),
    NAME_REQUIRED("IMP-COURSE-004", "课程名称不能为空", "name", "请填写课程名称（不超过100字符）", "R-NAME-REQUIRED"),
    CREDIT_RANGE_INVALID("IMP-COURSE-005", "学分必须在0.5-10之间", "credit", "请按0.5步长填写，如3.0", "R-CREDIT-RANGE"),
    HOURS_RANGE_INVALID("IMP-COURSE-006", "学时必须在1-200之间", "hours", "请填写1-200之间的整数", "R-HOURS-RANGE"),
    SEMESTER_REQUIRED("IMP-COURSE-007", "学期不能为空", "semester", "示例：2024-2025-2", "R-SEMESTER-REQUIRED"),
    TIME_FORMAT_INVALID("IMP-COURSE-008", "时间格式不正确，应为HH:mm:ss", "time", "示例：08:00:00", "R-TIME-FORMAT"),
    STATUS_INVALID("IMP-COURSE-009", "课程状态必须是0/1/2", "status", "0-未开课，1-已开课，2-已结课", "R-STATUS-ENUM"),
    SYSTEM_ERROR("IMP-SYS-500", "系统处理失败", "system", "请联系管理员并重试", "R-SYSTEM");

    private final String code;
    private final String message;
    private final String field;
    private final String suggestion;
    private final String rule;

    ImportErrorCode(String code, String message, String field, String suggestion, String rule) {
        this.code = code;
        this.message = message;
        this.field = field;
        this.suggestion = suggestion;
        this.rule = rule;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getField() {
        return field;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public String getRule() {
        return rule;
    }
}
