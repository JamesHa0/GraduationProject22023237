package com.jameshao.gp22023237.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.DTO.StudentImportDTO;
import com.jameshao.gp22023237.DTO.StudentImportResultDTO;
import com.jameshao.gp22023237.DTO.importtask.StudentImportTaskDTO;
import com.jameshao.gp22023237.common.error.StudentImportErrorCode;
import com.jameshao.gp22023237.mapper.StudentMapper;
import com.jameshao.gp22023237.po.Student;
import com.jameshao.gp22023237.po.User;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
* @author test
* @description 针对表【student(学生信息表)】的数据库操作Service实现
* @createDate 2025-10-08 17:09:36
*/
@Slf4j
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student>
    implements StudentService {

    private static final ConcurrentHashMap<String, StudentImportTaskDTO> TASK_STORE = new ConcurrentHashMap<>();

    @Autowired
    private UserService userService;

    @Value("${student.import.batch-size:300}")
    private Integer importBatchSize;

    @Override
    public StudentImportTaskDTO createImportTask() {
        StudentImportTaskDTO task = new StudentImportTaskDTO();
        task.setTaskId(UUID.randomUUID().toString().replace("-", ""));
        task.setStatus("PENDING");
        task.setTotal(0);
        task.setSuccessCount(0);
        task.setFailCount(0);
        task.setProgress(0);
        task.setMessage("任务已创建，等待上传文件");
        task.setFailDetails(new ArrayList<>());
        task.setCreatedAt(new Date());
        task.setUpdatedAt(new Date());
        TASK_STORE.put(task.getTaskId(), task);
        return task;
    }

    @Override
    public StudentImportTaskDTO queryImportTask(String taskId) {
        return TASK_STORE.get(taskId);
    }

    @Override
    public void startImportTask(String taskId, MultipartFile file, Long classId) {
        StudentImportTaskDTO task = TASK_STORE.get(taskId);
        if (task == null) {
            throw new RuntimeException("导入任务不存在或已过期");
        }

        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String lowerName = fileName.toLowerCase(Locale.ROOT);
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("读取上传文件失败", e);
        }

        updateTaskRunning(task, "文件上传成功，开始解析与校验...");

        CompletableFuture.runAsync(() -> {
            try {
                StudentImportResultDTO result = doImport(lowerName, bytes, task, classId);
                task.setStatus("SUCCESS");
                task.setTotal(result.getTotal());
                task.setSuccessCount(result.getSuccessCount());
                task.setFailCount(result.getFailCount());
                task.setFailDetails(result.getFailDetails());
                task.setProgress(100);
                task.setMessage("导入完成：成功 " + result.getSuccessCount() + " 条，失败 " + result.getFailCount() + " 条");
                task.setUpdatedAt(new Date());
                log.info("学生导入任务完成 taskId={}, total={}, success={}, fail={}", taskId, result.getTotal(), result.getSuccessCount(), result.getFailCount());
            } catch (Exception e) {
                task.setStatus("FAILED");
                task.setProgress(100);
                task.setMessage("导入失败：" + e.getMessage());
                task.setUpdatedAt(new Date());
                log.error("学生导入任务失败 taskId={}", taskId, e);
            }
        });
    }

    private void updateTaskRunning(StudentImportTaskDTO task, String message) {
        task.setStatus("RUNNING");
        task.setProgress(1);
        task.setMessage(message);
        task.setUpdatedAt(new Date());
    }

    private StudentImportResultDTO doImport(String lowerName, byte[] bytes, StudentImportTaskDTO task, Long classId) {
        List<StudentImportDTO> rows = parseImportRows(lowerName, bytes);

        int total = rows.size();
        int successCount = 0;
        List<StudentImportResultDTO.FailDetail> failDetails = new ArrayList<>();

        if (total == 0) {
            task.setProgress(100);
            task.setMessage("导入文件为空");
            return new StudentImportResultDTO(0, 0, 0, failDetails);
        }

        int batchSize = (importBatchSize == null || importBatchSize <= 0) ? 300 : importBatchSize;
        task.setTotal(total);
        task.setProgress(5);
        task.setMessage("正在预加载历史数据...");

        Set<String> fileStudentNos = new HashSet<>();
        Set<String> studentNos = new HashSet<>();
        for (StudentImportDTO row : rows) {
            if (row.getStudentNo() != null && !row.getStudentNo().trim().isEmpty()) {
                studentNos.add(row.getStudentNo().trim());
            }
        }

        Set<String> existingStudentNos = new HashSet<>();
        Set<String> existingUsernames = new HashSet<>();
        if (!studentNos.isEmpty()) {
            QueryWrapper<Student> studentWrapper = new QueryWrapper<>();
            studentWrapper.in("student_no", studentNos).select("student_no");
            List<Student> existingStudents = this.list(studentWrapper);
            for (Student student : existingStudents) {
                existingStudentNos.add(student.getStudentNo());
            }

            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.in("username", studentNos).select("username");
            List<User> users = userService.list(userWrapper);
            for (User user : users) {
                existingUsernames.add(user.getUsername());
            }
        }

        int processed = 0;
        for (int i = 0; i < rows.size(); i++) {
            StudentImportDTO dto = rows.get(i);
            int rowNum = i + 2;

            StudentImportResultDTO.FailDetail failDetail = validateAndNormalize(dto, rowNum, fileStudentNos, existingStudentNos, existingUsernames);
            if (failDetail != null) {
                failDetails.add(failDetail);
                processed++;
                updateProgress(task, processed, total);
                continue;
            }

            try {
                Date now = new Date();
                User user = new User();
                user.setUsername(dto.getStudentNo());
                user.setPassword(dto.getStudentNo());
                user.setName(dto.getStudentName());
                user.setRoleId(1);
                user.setStatus(1);
                user.setCreateTime(now);
                user.setUpdateTime(now);

                boolean userSaved = userService.save(user);
                if (!userSaved) {
                    failDetails.add(toFail(rowNum, dto.getStudentNo(), StudentImportErrorCode.SYSTEM_ERROR, "创建用户账号失败"));
                    processed++;
                    updateProgress(task, processed, total);
                    continue;
                }

                Student student = new Student();
                student.setUserId(user.getId());
                student.setClassId(classId);
                student.setStudentNo(dto.getStudentNo());
                student.setStudentName(dto.getStudentName());
                student.setDepartment(dto.getDepartment());
                student.setMajor(dto.getMajor());
                student.setAdmissionYear(dto.getAdmissionYear());
                student.setCohortYear(dto.getCohortYear());
                student.setGraduationYear(dto.getGraduationYear());
                student.setResearchDirection(dto.getResearchDirection());
                student.setStatus(dto.getStatus());
                student.setSelectionStatus(dto.getSelectionStatus());
                student.setCreateTime(now);
                student.setUpdateTime(now);

                boolean studentSaved = this.save(student);
                if (!studentSaved) {
                    userService.removeById(user.getId());
                    failDetails.add(toFail(rowNum, dto.getStudentNo(), StudentImportErrorCode.SYSTEM_ERROR, "保存学生信息失败"));
                } else {
                    successCount++;
                    existingStudentNos.add(dto.getStudentNo());
                    existingUsernames.add(dto.getStudentNo());
                }
            } catch (Exception e) {
                failDetails.add(toFail(rowNum, dto.getStudentNo(), StudentImportErrorCode.SYSTEM_ERROR, "数据写入异常: " + e.getMessage()));
            }

            processed++;
            if (processed % batchSize == 0 || processed == total) {
                updateProgress(task, processed, total);
            }
        }

        return new StudentImportResultDTO(total, successCount, failDetails.size(), failDetails);
    }

    private void updateProgress(StudentImportTaskDTO task, int processed, int total) {
        int progress = 5 + (int) Math.floor((processed * 90.0) / total);
        if (progress > 99) {
            progress = 99;
        }
        task.setProgress(progress);
        task.setMessage("正在导入：" + processed + "/" + total);
        task.setUpdatedAt(new Date());
    }

    private List<StudentImportDTO> parseImportRows(String lowerName, byte[] bytes) {
        if (lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls")) {
            return parseExcel(bytes);
        }
        if (lowerName.endsWith(".csv")) {
            return parseCsv(bytes);
        }
        throw new RuntimeException("仅支持 .xls/.xlsx/.csv 文件");
    }

    private List<StudentImportDTO> parseExcel(byte[] bytes) {
        try {
            return EasyExcel.read(new ByteArrayInputStream(bytes))
                    .head(StudentImportDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (Exception e) {
            throw new RuntimeException("解析Excel失败，请检查模板和格式", e);
        }
    }

    private List<StudentImportDTO> parseCsv(byte[] bytes) {
        List<StudentImportDTO> rows = new ArrayList<>();
        try (Reader reader = new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8)) {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build();
            CSVParser parser = format.parse(reader);
            for (CSVRecord record : parser) {
                StudentImportDTO dto = new StudentImportDTO();
                dto.setStudentNo(read(record, "学号"));
                dto.setStudentName(read(record, "姓名"));
                dto.setDepartment(read(record, "学院"));
                dto.setMajor(read(record, "专业"));
                dto.setAdmissionYear(toInteger(read(record, "入学年份")));
                dto.setCohortYear(toInteger(read(record, "归属年级")));
                dto.setGraduationYear(toInteger(read(record, "毕业年份")));
                dto.setResearchDirection(read(record, "研究方向"));
                dto.setStatus(toInteger(read(record, "状态")));
                dto.setSelectionStatus(toInteger(read(record, "双选状态")));
                rows.add(dto);
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("解析CSV失败，请检查编码（UTF-8）和模板列名", e);
        }
    }

    private String read(CSVRecord record, String header) {
        if (record.isMapped(header)) {
            return trimToNull(record.get(header));
        }
        String bomHeader = "\uFEFF" + header;
        if (record.isMapped(bomHeader)) {
            return trimToNull(record.get(bomHeader));
        }
        return null;
    }

    private Integer toInteger(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private StudentImportResultDTO.FailDetail validateAndNormalize(StudentImportDTO dto,
                                                                   int row,
                                                                   Set<String> fileStudentNos,
                                                                   Set<String> existingStudentNos,
                                                                   Set<String> existingUsernames) {
        if (dto.getStudentNo() == null || dto.getStudentNo().trim().isEmpty()) {
            return toFail(row, dto.getStudentNo(), StudentImportErrorCode.STUDENT_NO_REQUIRED);
        }
        dto.setStudentNo(dto.getStudentNo().trim());
        if (dto.getStudentNo().length() > 20) {
            return toFail(row, dto.getStudentNo(), StudentImportErrorCode.STUDENT_NO_REQUIRED, "学号长度不能超过20");
        }

        if (existingStudentNos.contains(dto.getStudentNo())) {
            return toFail(row, dto.getStudentNo(), StudentImportErrorCode.STUDENT_NO_DUPLICATE_DB);
        }
        if (existingUsernames.contains(dto.getStudentNo())) {
            return toFail(row, dto.getStudentNo(), StudentImportErrorCode.STUDENT_NO_DUPLICATE_USER);
        }
        if (fileStudentNos.contains(dto.getStudentNo())) {
            return toFail(row, dto.getStudentNo(), StudentImportErrorCode.STUDENT_NO_DUPLICATE_FILE);
        }

        if (dto.getStudentName() == null || dto.getStudentName().trim().isEmpty()) {
            return toFail(row, dto.getStudentNo(), StudentImportErrorCode.STUDENT_NAME_REQUIRED);
        }
        dto.setStudentName(dto.getStudentName().trim());
        if (dto.getStudentName().length() > 50) {
            return toFail(row, dto.getStudentNo(), StudentImportErrorCode.STUDENT_NAME_REQUIRED, "姓名长度不能超过50");
        }

        if (dto.getDepartment() == null || dto.getDepartment().trim().isEmpty()) {
            return toFail(row, dto.getStudentNo(), StudentImportErrorCode.DEPARTMENT_REQUIRED);
        }
        dto.setDepartment(dto.getDepartment().trim());
        if (dto.getDepartment().length() > 100) {
            return toFail(row, dto.getStudentNo(), StudentImportErrorCode.DEPARTMENT_REQUIRED, "学院长度不能超过100");
        }

        if (dto.getMajor() == null || dto.getMajor().trim().isEmpty()) {
            return toFail(row, dto.getStudentNo(), StudentImportErrorCode.MAJOR_REQUIRED);
        }
        dto.setMajor(dto.getMajor().trim());
        if (dto.getMajor().length() > 100) {
            return toFail(row, dto.getStudentNo(), StudentImportErrorCode.MAJOR_REQUIRED, "专业长度不能超过100");
        }

        if (dto.getAdmissionYear() == null || dto.getAdmissionYear() < 2000 || dto.getAdmissionYear() > 2100) {
            return toFail(row, dto.getStudentNo(), StudentImportErrorCode.ADMISSION_YEAR_INVALID);
        }

        if (dto.getCohortYear() == null) {
            dto.setCohortYear(dto.getAdmissionYear());
        }

        if (dto.getGraduationYear() == null || dto.getGraduationYear() < dto.getAdmissionYear() || dto.getGraduationYear() > 2100) {
            return toFail(row, dto.getStudentNo(), StudentImportErrorCode.GRADUATION_YEAR_INVALID);
        }

        if (dto.getResearchDirection() != null) {
            dto.setResearchDirection(dto.getResearchDirection().trim());
            if (dto.getResearchDirection().length() > 200) {
                return toFail(row, dto.getStudentNo(), StudentImportErrorCode.SYSTEM_ERROR, "研究方向长度不能超过200");
            }
        }

        if (dto.getStatus() == null) {
            dto.setStatus(1);
        }
        if (dto.getStatus() != 0 && dto.getStatus() != 1) {
            return toFail(row, dto.getStudentNo(), StudentImportErrorCode.STATUS_INVALID);
        }

        if (dto.getSelectionStatus() == null) {
            dto.setSelectionStatus(0);
        }
        if (dto.getSelectionStatus() < 0 || dto.getSelectionStatus() > 3) {
            return toFail(row, dto.getStudentNo(), StudentImportErrorCode.SELECTION_STATUS_INVALID);
        }

        fileStudentNos.add(dto.getStudentNo());
        return null;
    }

    private StudentImportResultDTO.FailDetail toFail(int row, String studentNo, StudentImportErrorCode code) {
        return new StudentImportResultDTO.FailDetail(
                row,
                studentNo,
                code.getMessage(),
                code.getCode(),
                code.getField(),
                code.getSuggestion(),
                code.getRule()
        );
    }

    private StudentImportResultDTO.FailDetail toFail(int row, String studentNo, StudentImportErrorCode code, String customReason) {
        return new StudentImportResultDTO.FailDetail(
                row,
                studentNo,
                customReason,
                code.getCode(),
                code.getField(),
                code.getSuggestion(),
                code.getRule()
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
