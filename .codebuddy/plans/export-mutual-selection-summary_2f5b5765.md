---
name: export-mutual-selection-summary
overview: 完善互选汇总表的导出功能：创建专门的 SummaryExportUtil 工具类按表格位置填充数据，重构 MentorStudentServiceImpl.exportMentorStudentSummary() 方法，支持将数据库查询到的已确认导师学生关系列表动态填充到 Word 表格中。
todos:
  - id: create-export-util
    content: 新建MutualSelectionExportUtil工具类，实现按表格坐标填充数据和vMerge合并逻辑
    status: completed
  - id: modify-template-placeholders
    content: 在互选汇总表docx模板的信息行添加department/studentCount/admissionYear占位符
    status: completed
  - id: rewrite-export-method
    content: 重写MentorStudentServiceImpl.exportMentorStudentSummary方法，增加数据聚合分组并调用新工具类
    status: completed
    dependencies:
      - create-export-util
  - id: update-sql-ordering
    content: 优化listConfirmedRelationships SQL按导师名和专业排序以匹配表格分组需求
    status: completed
---

## 产品 Overview

实现互选汇总表的导出功能，将已确认的导师学生关系数据按照模板格式导出为 Word 文档（.docx），支持自动替换文档中的占位符并填充表格数据。

## 核心功能

- 导出接口：`GET /selection/relationship/export/summary`（Controller 层已存在）
- 数据源：从 `listConfirmedRelationships()` 查询获取所有已确认的导师-学生关系记录
- 模板填充：加载 `互选汇总表.docx` 模板，按表格单元格位置直接填入数据（专业、导师姓名、学号、姓名、研究方向）
- 表格结构：5 列（专业 | 导师姓名 | 学号 | 姓名 | 研究方向），表头占 2 行（含合并单元格），数据行从第 2 行开始，共 18 个空白数据行
- 信息行：学院名称和学生总数需要通过段落文本替换方式写入标题下方信息行
- 数据量处理：当关系记录数超过模板预留行数时自动追加新行；不足时多余空行保留为空白

## 当前问题分析

现有 `exportMentorStudentSummary()` 方法使用 `${placeholder}` 占位符替换方式（WordExportUtil），但 `互选汇总表.docx` 模板中**没有任何 `${}` 占位符**——其数据区域是空白单元格，且包含合并单元格的复杂表头结构，无法通过简单文本替换实现数据填充。需要改为**按表格坐标位置直接操作单元格**的方式。

## Tech Stack

- **后端框架**: Spring Boot + MyBatis-Plus（已有）
- **Word 操作**: Apache POI XWPF（poi-ooxml-full 5.2.5 已在 pom.xml 中引入）
- **导出工具**: 新建 `MutualSelectionExportUtil` 工具类，复用 POI XWPF API 直接操作表格单元格

## 实现方案

### 核心策略：基于表格坐标位置的单元格直接填充

由于互选汇总表模板的特殊结构：

1. 标题行无占位符（纯下划线格式），需用 WordExportUtil 的文本替换能力或段落操作来填写学院/人数/年级
2. 数据区为空白单元格（无 `${}`），必须按 `(行号, 列号)` 坐标直接 setCellText
3. 表头有合并单元格（vMerge）：第 0 行「专业」和「导师姓名」向下合并到第 1 行；第 0 行「被指导学生」跨 3 列（学号/姓名/研究方向）

### 数据流设计

```
Controller.exportMentorStudentSummary()
  -> Service.exportMentorStudentSummary(response)
    -> Mapper.listConfirmedRelationships() 获取原始平铺列表
    -> 按 (teacherName, major) 分组聚合为嵌套结构（导师->学生列表）
    -> MutualSelectionExportUtil.exportSummary(templateStream, response, summaryData)
      -> 填写信息行（学院、人数、年级）[段落文本替换]
      -> 遍历分组数据，逐行写入表格单元格 [位置填充]
        - 处理导师合并：同一导师的多名学生，第0列(专业)和第1列(导师)设vMerge
        - 行不够时 createRow() 追加
```

### 关键技术决策

1. **新建专用 ExportUtil 而非复用 WordExportUtil**：因为 WordExportUtil 仅支持 `${}` 文本替换和整表数据填充，不支持"部分单元格按坐标填充 + 合并单元格处理"的混合场景
2. **数据分组聚合**：SQL 返回的是平铺列表（每行一个学生+导师），但表格要求同一导师的学生连续排列且导师名只显示一次（使用 vMerge 合并）。需要在 Java 层按 teacherName + major 分组
3. **信息行处理**：模板中学院/人数处为空格下划线而非 `${}` 占位符，采用两种方案之一：(A) 在模板中预埋 `${department}` 等占位符；(B) 通过 POI 段落 API 定位修改。推荐方案 A 更简洁可靠，需同时更新 .docx 模板中信息行的文本为 `${department}`、`${studentCount}`、`${admissionYear}`
4. **vMerge 处理**：同一导师的第 2~N 名学生的前两列（专业、导师名）应隐藏（设置 vMerge=continue），与模板表头的合并风格一致

## Implementation Notes

- **性能**：数据量有限（研究生导师学生数量通常几十到几百条），无需分批处理，内存中一次性完成
- **权限检查**：保留已有的 `hasViewPermission()` 校验
- **响应头编码**：沿用现有的 URLEncoder + `%20` 替换模式，确保中文文件名兼容
- **资源管理**：使用 try-with-resources 确保 InputStream/OutputStream 正确关闭
- **日志**：保留 dataMap/system.out 调试输出模式（与现有代码一致）
- **向后兼容**：Controller 和 Service 接口签名不变，仅修改 ServiceImpl 实现 + 新增 Util 类
- **模板占位符**：需要在 `.docx` 模板的信息行中添加 `${department}`、`${studentCount}`、`${admissionYear}` 占位符

## Architecture Design

```
MentorStudentRelationshipController  (已有, 不变)
    |
    v
MentorStudentService  (接口, 不变)
    |
    v
MentorStudentServiceImpl.exportMentorStudentSummary()  [修改核心逻辑]
    |-- baseMapper.listConfirmedRelationships()
    |-- 聚合分组: List<MentorGroup> (teacherName+major -> List<StudentRow>)
    |-- MutualSelectionExportUtil.exportSummary()  [新增工具类]
         |-- 加载 XWPFDocument
         |-- WordExportUtil.replaceInDocument() 处理信息行 ${} 占位符
         |-- 按坐标遍历表格写入数据
         |     |-- 处理 vMerge（同导师合并）
         |     |-- 动态创建不足的行
         |-- document.write(outputStream)
```

## Directory Structure

```
gp22023237-server/src/main/java/com/jameshao/gp22023237/
├── utils/
│   └── MutualSelectionExportUtil.java   # [NEW] 互选汇总表专用导出工具类
├── service/impl/
│   └── MentorStudentServiceImpl.java   # [MODIFY] 重写 exportMentorStudentSummary 方法
├── controller/selection/
│   └── MentorStudentRelationshipController.java  # [不变]
├── service/
│   └── MentorStudentService.java       # [不变]
└── resources/templates/word/
    └── 互选汇总表.docx                 # [MODIFY] 在信息行添加${}占位符
```

## Key Code Structures

```java
// MutalSelectionExportUtil - 核心方法签名
public class MutualSelectionExportUtil {
    // 分组数据结构
    public static class MentorGroup {
        private String major;                    // 专业
        private String teacherName;              // 导师姓名
        private List<StudentRow> students;       // 学生列表
    }
    public static class StudentRow {
        private String studentNo;                // 学号
        private String studentName;              // 姓名
        private String researchField;            // 研究方向
    }
    
    // 导出入口
    public static void exportSummary(
        InputStream templateInputStream,
        OutputStream outputStream,
        Map<String, String> headerInfo,          // {department, studentCount, admissionYear}
        List<MentorGroup> groupData             // 分组后的导师-学生数据
    ) throws IOException;
}
```

### SubAgent

- **code-explorer**
- Purpose: 深度探索代码库中导出相关的完整实现细节和依赖关系
- Expected outcome: 已获取全部关键文件的精确内容和模板结构，为实现提供充分依据