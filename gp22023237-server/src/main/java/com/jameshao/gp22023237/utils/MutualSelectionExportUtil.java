package com.jameshao.gp22023237.utils;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * 互选汇总表专用导出工具类
 *
 * 模板结构说明：
 * - 信息行（标题下方）：包含 ${department} ${studentCount} ${admissionYear} 占位符
 * - 表格第0行：表头合并行（专业/导师姓名 vMerge + 被指导学生 gridSpan=3）
 * - 表格第1行：子表头（vMerge continue + 学号/姓名/研究方向）
 * - 表格第2行起：数据行，共预留 18 个空行
 */
public class MutualSelectionExportUtil {

    /** 数据起始行号（前2行为表头） */
    private static final int DATA_START_ROW = 2;
    /** 表格总列数 */
    private static final int TOTAL_COLUMNS = 5;

    /**
     * 导师-学生分组数据结构
     */
    public static class MentorGroup {
        private String major;                      // 专业
        private String teacherName;                // 导师姓名
        private List<StudentRow> students;         // 该导师的学生列表

        public MentorGroup(String major, String teacherName) {
            this.major = major != null ? major : "";
            this.teacherName = teacherName != null ? teacherName : "";
            this.students = new ArrayList<>();
        }

        public void addStudent(StudentRow student) {
            this.students.add(student);
        }

        // getters
        public String getMajor() { return major; }
        public String getTeacherName() { return teacherName; }
        public List<StudentRow> getStudents() { return students; }
        public int getStudentCount() { return students.size(); }
    }

    /**
     * 学生行数据
     */
    public static class StudentRow {
        private String studentNo;      // 学号
        private String studentName;    // 姓名
        private String researchField;  // 研究方向

        public StudentRow(String studentNo, String studentName, String researchField) {
            this.studentNo = studentNo != null ? studentNo : "";
            this.studentName = studentName != null ? studentName : "";
            this.researchField = researchField != null ? researchField : "";
        }

        // getters
        public String getStudentNo() { return studentNo; }
        public String getStudentName() { return studentName; }
        public String getResearchField() { return researchField; }
    }

    /**
     * 导出入口方法
     *
     * @param templateInputStream 模板文件输入流
     * @param outputStream       响应输出流
     * @param headerInfo         信息行占位符数据 {department, studentCount, admissionYear}
     * @param groupData          分组后的导师-学生数据列表
     */
    public static void exportSummary(InputStream templateInputStream,
                                      OutputStream outputStream,
                                      Map<String, String> headerInfo,
                                      List<MentorGroup> groupData) throws IOException {
        XWPFDocument document = new XWPFDocument(templateInputStream);

        try {
            // 1. 替换文档中的 ${} 占位符（信息行：学院、学生数、年级）
            replacePlaceholders(document, headerInfo);

            // 2. 填充表格数据
            if (document.getTables().size() > 0) {
                XWPFTable table = document.getTables().get(0);
                fillTableData(table, groupData);
            }

            // 3. 输出文档
            document.write(outputStream);
            outputStream.flush();
        } finally {
            document.close();
        }
    }

    /**
     * 使用 WordExportUtil 的占位符替换逻辑处理信息行
     */
    private static void replacePlaceholders(XWPFDocument document, Map<String, String> dataMap) {
        if (dataMap == null || dataMap.isEmpty()) {
            return;
        }
        // 构造带 ${} 前缀的 map 用于 WordExportUtil 风格的替换
        Map<String, String> placeholderMap = new HashMap<>();
        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
            placeholderMap.put(entry.getKey(), entry.getValue());
        }

        // 复用 WordExportUtil 的替换方式：遍历段落和表格单元格进行文本替换
        replaceInParagraphs(document, placeholderMap);
    }

    /**
     * 在文档的所有段落中替换占位符（与 WordExportUtil.replaceInDocument 逻辑一致）
     */
    private static void replaceInParagraphs(XWPFDocument document, Map<String, String> dataMap) {
        // 替换正文段落
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            replaceTextInParagraph(paragraph, dataMap);
        }
        // 替换表格内段落
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        replaceTextInParagraph(paragraph, dataMap);
                    }
                }
            }
        }
    }

    /**
     * 替换单个段落中的占位符（与 WordExportUtil.replaceInParagraph 逻辑一致）
     */
    private static void replaceTextInParagraph(XWPFParagraph paragraph, Map<String, String> dataMap) {
        String text = paragraph.getText();
        if (text == null || text.isEmpty()) {
            return;
        }

        // 检查是否包含任何占位符
        boolean hasPlaceholder = false;
        for (String key : dataMap.keySet()) {
            String placeholder = "${" + key + "}";
            if (text.contains(placeholder)) {
                hasPlaceholder = true;
                break;
            }
        }
        if (!hasPlaceholder) {
            return;
        }

        // 执行替换
        String newText = text;
        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            newText = newText.replace(placeholder, value);
        }

        // 保存格式
        Boolean isBold = null;
        Boolean isItalic = null;
        Integer fontSize = null;
        String fontFamily = null;

        if (!paragraph.getRuns().isEmpty()) {
            XWPFRun firstRun = paragraph.getRuns().get(0);
            try { isBold = firstRun.isBold(); } catch (Exception ignored) {}
            try { isItalic = firstRun.isItalic(); } catch (Exception ignored) {}
            try { fontSize = firstRun.getFontSize(); } catch (Exception ignored) {}
            try { fontFamily = firstRun.getFontFamily(); } catch (Exception ignored) {}
        }

        // 清除旧 run
        int runCount = paragraph.getRuns().size();
        for (int i = runCount - 1; i >= 0; i--) {
            paragraph.removeRun(i);
        }

        // 创建新 run
        XWPFRun newRun = paragraph.createRun();
        newRun.setText(newText, 0);
        if (isBold != null) newRun.setBold(isBold);
        if (isItalic != null) newRun.setItalic(isItalic);
        if (fontSize != null && fontSize != -1) newRun.setFontSize(fontSize);
        if (fontFamily != null) newRun.setFontFamily(fontFamily);
    }

    /**
     * 将分组数据填充到表格中
     * - 同一导师的多名学生：第一行的专业/导师名正常显示，后续行的前两列设为 vMerge=continue
     * - 行数不足时自动追加新行
     */
    private static void fillTableData(XWPFTable table, List<MentorGroup> groupData) {
        if (groupData == null || groupData.isEmpty()) {
            return;
        }

        int currentRowIndex = DATA_START_ROW;

        for (MentorGroup group : groupData) {
            for (int studentIdx = 0; studentIdx < group.getStudentCount(); studentIdx++) {
                StudentRow student = group.getStudents().get(studentIdx);

                // 确保行存在
                ensureRowExists(table, currentRowIndex);
                XWPFTableRow row = table.getRow(currentRowIndex);

                // 第0列：专业（仅该导师的第一名学生显示，其余 vMerge continue）
                if (studentIdx == 0) {
                    setCellText(row.getCell(0), group.getMajor());
                    setCellAsVMergeRestart(row.getCell(0));
                } else {
                    setCellAsVMergeContinue(row.getCell(0));
                }

                // 第1列：导师姓名（仅该导师的第一名学生显示，其余 vMerge continue）
                if (studentIdx == 0) {
                    setCellText(row.getCell(1), group.getTeacherName());
                    setCellAsVMergeRestart(row.getCell(1));
                } else {
                    setCellAsVMergeContinue(row.getCell(1));
                }

                // 第2列：学号
                setCellText(row.getCell(2), student.getStudentNo());

                // 第3列：姓名
                setCellText(row.getCell(3), student.getStudentName());

                // 第4列：研究方向
                setCellText(row.getCell(4), student.getResearchField());

                currentRowIndex++;
            }
        }

        // 清除剩余未使用的模板数据行中的残留占位符文本
        clearRemainingRows(table, currentRowIndex);
    }

    /**
     * 清除表格中从指定行开始到末尾的所有行内容（删除残留的 ${} 占位符）
     */
    private static void clearRemainingRows(XWPFTable table, int fromRowIndex) {
        for (int i = fromRowIndex; i < table.getNumberOfRows(); i++) {
            XWPFTableRow row = table.getRow(i);
            if (row == null) {
                continue;
            }
            for (XWPFTableCell cell : row.getTableCells()) {
                setCellText(cell, "");
            }
        }
    }

    /**
     * 确保表格指定行存在，不存在则创建
     */
    private static void ensureRowExists(XWPFTable table, int rowIndex) {
        while (rowIndex >= table.getNumberOfRows()) {
            XWPFTableRow newRow = table.createRow();
            // 确保新行有足够的列
            while (newRow.getTableCells().size() < TOTAL_COLUMNS) {
                newRow.createCell();
            }
        }
    }

    /**
     * 设置单元格文本内容
     */
    private static void setCellText(XWPFTableCell cell, String text) {
        if (cell == null) {
            return;
        }
        if (text == null) {
            text = "";
        }

        // 清除现有段落
        while (cell.getParagraphs().size() > 0) {
            cell.removeParagraph(0);
        }

        // 新建段落和 run
        XWPFParagraph paragraph = cell.addParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text);
    }

    /**
     * 设置单元格为垂直合并起始（显示内容，后续行将合并到此单元格）
     */
    private static void setCellAsVMergeRestart(XWPFTableCell cell) {
        if (cell == null) {
            return;
        }
        CTTcPr tcPr = cell.getCTTc().addNewTcPr();
        CTVMerge vmerge = tcPr.addNewVMerge();
        vmerge.setVal(STMerge.RESTART);
    }

    /**
     * 设置单元格为垂直合并继续（隐藏内容，视觉上合并到上方单元格）
     */
    private static void setCellAsVMergeContinue(XWPFTableCell cell) {
        if (cell == null) {
            return;
        }

        // 清除现有内容
        setCellText(cell, "");

        // 设置 vMerge 为 continue
        CTTcPr tcPr = cell.getCTTc().addNewTcPr();
        CTVMerge vmerge = tcPr.addNewVMerge();
        vmerge.setVal(STMerge.CONTINUE);
    }

    /**
     * 将平铺的关系列表按 (major, teacherName) 分组聚合
     *
     * @param relationships SQL查询出的平铺列表，每条记录包含：
     *                      studentNo, studentName, major, teacherName, researchField 等
     * @return 分组后的 MentorGroup 列表
     */
    public static List<MentorGroup> groupByMentor(List<Map<String, Object>> relationships) {
        if (relationships == null || relationships.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用 LinkedHashMap 保持插入顺序（SQL已排序）
        Map<String, MentorGroup> groupMap = new LinkedHashMap<>();

        for (Map<String, Object> record : relationships) {
            String major = safeGetString(record, "major");
            String teacherName = safeGetString(record, "teacherName");

            String groupKey = major + "|" + teacherName;

            MentorGroup group = groupMap.computeIfAbsent(groupKey,
                    k -> new MentorGroup(major, teacherName));

            StudentRow studentRow = new StudentRow(
                    safeGetString(record, "studentNo"),
                    safeGetString(record, "studentName"),
                    safeGetString(record, "researchField")
            );

            group.addStudent(studentRow);
        }

        return new ArrayList<>(groupMap.values());
    }

    /**
     * 安全地从 Map 中获取字符串值
     */
    private static String safeGetString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }
}
