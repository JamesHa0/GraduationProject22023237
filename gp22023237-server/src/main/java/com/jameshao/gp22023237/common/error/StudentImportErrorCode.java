package com.jameshao.gp22023237.common.error;

public enum StudentImportErrorCode {
    STUDENT_NO_REQUIRED("IMP-STU-001", "学号不能为空", "studentNo", "请填写不超过20位的唯一学号", "R-STU-NO-REQUIRED"),
    STUDENT_NO_DUPLICATE_DB("IMP-STU-002", "学号已存在", "studentNo", "请更换学号，避免与系统现有学生重复", "R-STU-NO-UNIQUE"),
    STUDENT_NO_DUPLICATE_FILE("IMP-STU-003", "文件内学号重复", "studentNo", "同一导入文件中学号必须唯一", "R-STU-NO-UNIQUE"),
    STUDENT_NO_DUPLICATE_USER("IMP-STU-004", "学号已被占用为系统账号", "studentNo", "请更换学号或先清理冲突账号", "R-STU-USER-UNIQUE"),
    STUDENT_NAME_REQUIRED("IMP-STU-005", "姓名不能为空", "studentName", "请填写姓名（不超过50字符）", "R-STU-NAME-REQUIRED"),
    DEPARTMENT_REQUIRED("IMP-STU-006", "学院不能为空", "department", "请填写学院（不超过100字符）", "R-STU-DEPT-REQUIRED"),
    MAJOR_REQUIRED("IMP-STU-007", "专业不能为空", "major", "请填写专业（不超过100字符）", "R-STU-MAJOR-REQUIRED"),
    ADMISSION_YEAR_INVALID("IMP-STU-008", "入学年份不合法", "admissionYear", "请填写2000-2100之间的年份", "R-STU-ADMISSION-YEAR"),
    GRADUATION_YEAR_INVALID("IMP-STU-009", "毕业年份不合法", "graduationYear", "请填写不早于入学年份的毕业年份", "R-STU-GRAD-YEAR"),
    STATUS_INVALID("IMP-STU-010", "状态必须是0或1", "status", "0-禁用，1-正常", "R-STU-STATUS-ENUM"),
    SELECTION_STATUS_INVALID("IMP-STU-011", "双选状态必须是0/1/2/3", "selectionStatus", "0-未开始，1-双选中，2-补选中，3-已确定", "R-STU-SELECT-ENUM"),
    SYSTEM_ERROR("IMP-STU-500", "系统处理失败", "system", "请联系管理员并重试", "R-STU-SYSTEM");

    private final String code;
    private final String message;
    private final String field;
    private final String suggestion;
    private final String rule;

    StudentImportErrorCode(String code, String message, String field, String suggestion, String rule) {
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
