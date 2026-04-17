package com.jameshao.gp22023237.utils;

import org.apache.poi.xwpf.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 专门用于学生志愿表的导出工具类
 * 按表格位置直接填充数据
 */
public class StudentVolunteerExportUtil {

    public static void exportStudentVolunteer(InputStream templateInputStream,
                                               OutputStream outputStream,
                                               Map<String, String> basicInfo,
                                               List<List<String>> volunteerData) throws IOException {
        XWPFDocument document = new XWPFDocument(templateInputStream);

        try {
            if (document.getTables().size() > 0) {
                XWPFTable table = document.getTables().get(0);

                // 第0行：填充学生基本信息（学号、姓名、院系、专业、年级）
                // 根据实际模板结构调整位置
                // 这里假设第0行第1格是学号，第0行第3格是姓名，等等
                // 实际位置需要根据模板调整
                setCellText(table, 0, 1, basicInfo.get("studentNo"));
                setCellText(table, 0, 3, basicInfo.get("studentName"));
                // 更多基本信息...

                // 填充志愿列表（从第2行开始）
                if (volunteerData != null) {
                    for (int i = 0; i < volunteerData.size(); i++) {
                        List<String> rowData = volunteerData.get(i);
                        int rowIndex = 1 + i; // 假设第1行是表头

                        // 确保有足够的行
                        while (rowIndex >= table.getRows().size()) {
                            table.createRow();
                        }

                        XWPFTableRow row = table.getRow(rowIndex);
                        for (int j = 0; j < rowData.size(); j++) {
                            // 确保有足够的单元格
                            while (j >= row.getTableCells().size()) {
                                row.createCell();
                            }
                            setCellText(table, rowIndex, j, rowData.get(j));
                        }
                    }
                }
            }

            document.write(outputStream);
            outputStream.flush();
        } finally {
            document.close();
        }
    }

    private static void setCellText(XWPFTable table, int rowIndex, int cellIndex, String text) {
        if (text == null) {
            text = "";
        }

        if (rowIndex < table.getRows().size()) {
            XWPFTableRow row = table.getRow(rowIndex);
            if (cellIndex < row.getTableCells().size()) {
                XWPFTableCell cell = row.getCell(cellIndex);

                // 清除现有内容
                while (cell.getParagraphs().size() > 0) {
                    cell.removeParagraph(0);
                }

                // 添加新内容
                XWPFParagraph paragraph = cell.addParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(text);
            }
        }
    }
}
