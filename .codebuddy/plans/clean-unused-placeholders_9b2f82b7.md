---
name: clean-unused-placeholders
overview: 在互选汇总表导出的 fillTableData 方法末尾，清除未使用的模板数据行中的 ${} 占位符文本，防止导出文件中残留多余占位符。
todos:
  - id: clean-unused-rows
    content: 在 fillTableData 方法末尾添加清理逻辑，清除剩余空行的残留占位符文本
    status: completed
---

## 产品概述

修复互选汇总表导出时，模板中未被实际数据覆盖的空行仍保留 `${majorN}`、`${teacherNameN}`、`${studentNoN}` 等占位符文本的问题。

## 核心功能

- 在 `fillTableData()` 填充完所有实际数据后，遍历表格中从 `currentRowIndex` 到最后一行的所有剩余行
- 对剩余每行的每个单元格调用 `setCellText(cell, "")` 清除残留占位符文本
- 同时考虑 `groupData` 为空的边界情况：此时需清理从 DATA_START_ROW 到表格末尾的所有预留数据行

## Tech Stack

- Java 17 + Spring Boot 3.4.4 + Apache POI 5.2.5（poi-ooxml-full）
- 现有工具类：`MutualSelectionExportUtil.java`

## Implementation Approach

### 策略：在 `fillTableData` 方法末尾追加清理逻辑

**核心思路**：填充循环结束后，`currentRowIndex` 指向下一个待写入行（即第一个未使用的空行）。只需从 `currentRowIndex` 遍历到 `table.getNumberOfRows() - 1`，对每行每个单元格执行 `setCellText(cell, "")` 即可清除所有残留占位符。

**关键决策**：

1. **不清除/删除空行**——保留表格原有行结构（边框等格式），仅清空内容。删除行会导致表格布局变化（如页眉重复、分页异常）。
2. **复用已有 `setCellText(cell, "")`**——该方法已能正确清除段落并写入空字符串。
3. **边界处理**：当 `groupData` 为空时，`currentRowIndex` 仍为初始值 DATA_START_ROW(2)，清理范围正确覆盖全部预留数据行；当数据量超过预留行数时，`ensureRowExists` 创建的新行无占位符，无需清理（`currentRowIndex >= table.getNumberOfRows()` 时循环不进入）。

## 实现细节

### 修改文件

```
gp22023237-server/src/main/java/com/jameshao/gp22023237/utils/MutualSelectionExportUtil.java
```

- **位置**：`fillTableData` 方法体末尾（第 249 行 `currentRowIndex++;` 之后，第 250 行 `}` 之前）
- **变更内容**：在 for 循环结束后添加一个 while 循环：

```java
// 清理未使用的预留行中的占位符文本
while (currentRowIndex < table.getNumberOfRows()) {
    XWPFTableRow emptyRow = table.getRow(currentRowIndex);
    if (emptyRow != null) {
        for (int col = 0; col < TOTAL_COLUMNS; col++) {
            if (col < emptyRow.getTableCells().size()) {
                setCellText(emptyRow.getCell(col), "");
            }
        }
    }
    currentRowIndex++;
}


```