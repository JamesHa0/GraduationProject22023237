package com.jameshao.gp22023237.utils;

import org.apache.poi.xwpf.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Word文档导出工具类
 * 用于填充Word模板并导出
 */
public class WordExportUtil {

    /**
     * 填充Word模板并输出到响应流
     */
    public static void fillTemplateAndExport(InputStream templateInputStream,
                                               OutputStream outputStream,
                                               Map<String, String> dataMap,
                                               Map<Integer, List<List<String>>> tableDataMap) throws IOException {
        XWPFDocument document = new XWPFDocument(templateInputStream);

        try {
            // 替换文本占位符
            if (dataMap != null && !dataMap.isEmpty()) {
                replaceInDocument(document, dataMap);
            }

            // 填充表格数据
            if (tableDataMap != null && !tableDataMap.isEmpty()) {
                fillTableData(document, tableDataMap);
            }

            // 输出文档
            document.write(outputStream);
            outputStream.flush();
        } finally {
            document.close();
        }
    }

    /**
     * 替换文档中的所有占位符
     */
    private static void replaceInDocument(XWPFDocument document, Map<String, String> dataMap) {
        // 替换段落
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            replaceInParagraph(paragraph, dataMap);
        }

        // 替换表格中的文本
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        replaceInParagraph(paragraph, dataMap);
                    }
                }
            }
        }
    }

    /**
     * 替换段落中的占位符 - 更可靠的方法
     */
    private static void replaceInParagraph(XWPFParagraph paragraph, Map<String, String> dataMap) {
        // 获取段落的完整文本
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

        // 替换占位符
        String newText = text;
        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            newText = newText.replace(placeholder, value);
        }

        // 在删除之前先保存格式属性
        Boolean isBold = null;
        Boolean isItalic = null;
        Integer fontSize = null;
        String fontFamily = null;

        if (!paragraph.getRuns().isEmpty()) {
            XWPFRun firstRun = paragraph.getRuns().get(0);
            try {
                isBold = firstRun.isBold();
            } catch (Exception e) { /* ignore */ }
            try {
                isItalic = firstRun.isItalic();
            } catch (Exception e) { /* ignore */ }
            try {
                fontSize = firstRun.getFontSize();
            } catch (Exception e) { /* ignore */ }
            try {
                fontFamily = firstRun.getFontFamily();
            } catch (Exception e) { /* ignore */ }
        }

        // 清除所有内容
        int runCount = paragraph.getRuns().size();
        for (int i = runCount - 1; i >= 0; i--) {
            paragraph.removeRun(i);
        }

        // 添加新内容
        XWPFRun newRun = paragraph.createRun();
        newRun.setText(newText, 0);

        // 应用保存的格式
        if (isBold != null) {
            newRun.setBold(isBold);
        }
        if (isItalic != null) {
            newRun.setItalic(isItalic);
        }
        if (fontSize != null && fontSize != -1) {
            newRun.setFontSize(fontSize);
        }
        if (fontFamily != null) {
            newRun.setFontFamily(fontFamily);
        }
    }

    /**
     * 填充表格数据
     */
    private static void fillTableData(XWPFDocument document, Map<Integer, List<List<String>>> tableDataMap) {
        int tableIndex = 0;
        for (XWPFTable table : document.getTables()) {
            if (tableDataMap.containsKey(tableIndex)) {
                List<List<String>> tableData = tableDataMap.get(tableIndex);
                fillTable(table, tableData);
            }
            tableIndex++;
        }
    }

    /**
     * 填充单个表格
     */
    private static void fillTable(XWPFTable table, List<List<String>> tableData) {
        if (tableData == null || tableData.isEmpty()) {
            return;
        }

        int startRow = 1; // 从第二行开始（第一行是表头）

        for (int i = 0; i < tableData.size(); i++) {
            List<String> rowData = tableData.get(i);
            int rowIndex = startRow + i;

            XWPFTableRow row;
            if (rowIndex < table.getNumberOfRows()) {
                row = table.getRow(rowIndex);
            } else {
                row = table.createRow();
            }

            for (int j = 0; j < rowData.size(); j++) {
                XWPFTableCell cell;
                if (j < row.getTableCells().size()) {
                    cell = row.getCell(j);
                } else {
                    cell = row.createCell();
                }

                String value = rowData.get(j);
                if (value != null) {
                    // 设置单元格内容
                    if (cell.getParagraphs().size() > 0) {
                        XWPFParagraph p = cell.getParagraphs().get(0);
                        // 清除现有内容
                        while (p.getRuns().size() > 0) {
                            p.removeRun(0);
                        }
                        XWPFRun run = p.createRun();
                        run.setText(value);
                    } else {
                        XWPFParagraph p = cell.addParagraph();
                        XWPFRun run = p.createRun();
                        run.setText(value);
                    }
                }
            }
        }
    }
}
