package com.jameshao.gp22023237.common.error;

/**
 * 排课导入错误码枚举
 */
public enum ScheduleImportErrorCode {
    TEACHER_NO_REQUIRED("IMP-SCHED-001", "教师工号不能为空", "teacherNo", "请填写系统内已有的教师工号", "R-TEACHER-NO-REQUIRED"),
    TEACHER_NOT_FOUND("IMP-SCHED-002", "教师工号在系统中不存在", "teacherNo", "请确认工号是否正确，需先在教师管理中录入", "R-TEACHER-NO-EXISTS"),
    COURSE_NO_REQUIRED("IMP-SCHED-003", "课程编号不能为空", "courseNo", "请填写系统内已有的课程编号", "R-COURSE-NO-REQUIRED"),
    COURSE_NOT_FOUND("IMP-SCHED-004", "课程编号在系统中不存在", "courseNo", "请确认编号是否正确，需先在课程管理中录入", "R-COURSE-NO-EXISTS"),
    CLASS_NAME_REQUIRED("IMP-SCHED-005", "班级名称不能为空", "className", "请填写系统内已有的班级名称", "R-CLASS-NAME-REQUIRED"),
    CLASS_NOT_FOUND("IMP-SCHED-006", "班级名称在系统中不存在", "className", "请确认班级名称是否正确，需先在班级管理中录入", "R-CLASS-NAME-EXISTS"),
    DAY_OF_WEEK_REQUIRED("IMP-SCHED-007", "星期不能为空", "dayOfWeek", "请填写1-7的数字，1=周一，7=周日", "R-DAY-OF-WEEK-REQUIRED"),
    DAY_OF_WEEK_INVALID("IMP-SCHED-008", "星期必须在1-7之间", "dayOfWeek", "1=周一，2=周二，…，7=周日", "R-DAY-OF-WEEK-RANGE"),
    TIME_SLOT_REQUIRED("IMP-SCHED-009", "开始节次和结束节次不能为空", "startSection", "请填写纯数字，如：1", "R-TIME-SLOT-REQUIRED"),
    TIME_SLOT_NOT_FOUND("IMP-SCHED-010", "节次在系统中不存在", "startSection", "请填写纯数字（如1）或第x节格式", "R-TIME-SLOT-EXISTS"),
    END_BEFORE_START("IMP-SCHED-0101", "结束节次不能早于开始节次", "endSection", "结束节次必须大于等于开始节次", "R-END-BEFORE-START"),
    SEMESTER_REQUIRED("IMP-SCHED-011", "学期不能为空", "semester", "示例：2024-2025-2", "R-SEMESTER-REQUIRED"),
    YEAR_REQUIRED("IMP-SCHED-012", "学年不能为空", "year", "请填写四位数字，如2024", "R-YEAR-REQUIRED"),
    YEAR_INVALID("IMP-SCHED-013", "学年不合法", "year", "请填写2000-2100之间的年份", "R-YEAR-RANGE"),
    TEACHER_CONFLICT("IMP-SCHED-014", "教师在该时间段已有排课", "teacherNo", "请调整教师或时间，避免冲突", "R-TEACHER-CONFLICT"),
    CLASS_CONFLICT("IMP-SCHED-015", "班级在该时间段已有排课", "className", "请调整班级或时间，避免冲突", "R-CLASS-CONFLICT"),
    CLASSROOM_CONFLICT("IMP-SCHED-016", "教室在该时间段已有排课", "classroom", "请调整教室或时间，避免冲突", "R-CLASSROOM-CONFLICT"),
    FILE_ROW_DUPLICATE("IMP-SCHED-017", "文件内存在重复排课", "teacherNo", "同一教师+班级+星期+节次+学期+学年不能重复", "R-ROW-DUPLICATE"),
    SYSTEM_ERROR("IMP-SCHED-500", "系统处理失败", "system", "请联系管理员并重试", "R-SYSTEM");

    private final String code;
    private final String message;
    private final String field;
    private final String suggestion;
    private final String rule;

    ScheduleImportErrorCode(String code, String message, String field, String suggestion, String rule) {
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
