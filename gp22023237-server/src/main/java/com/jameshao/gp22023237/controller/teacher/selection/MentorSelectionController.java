package com.jameshao.gp22023237.controller.teacher.selection;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.jameshao.gp22023237.DTO.SelectionDTO;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.MentorStudent;
import com.jameshao.gp22023237.po.Student;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.service.MentorStudentService;
import com.jameshao.gp22023237.service.SelectionRoundService;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/mentor/selection")
public class MentorSelectionController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private MentorStudentService mentorStudentService;
    @Autowired
    private SelectionRoundService selectionRoundService;
    @Autowired
    private JSONReturn jsonReturn;

    // 导师查询可选学生 - 增加轮次过滤，过滤已被接受的学生
    @RequestMapping("/listStudents")
    public String listStudents(@RequestBody MentorStudent mentorStudent){
        try{
            System.out.println("导师查询可选学生:"+mentorStudent);

            // 获取当前轮次
            int currentRound = selectionRoundService.getCurrentRound();
            System.out.println("当前轮次: " + currentRound);

            LambdaQueryWrapper<MentorStudent> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(!ObjectUtils.isEmpty(mentorStudent.getTeacherStatus()), MentorStudent::getTeacherStatus, mentorStudent.getTeacherStatus())
                    .eq(MentorStudent::getMentorId, mentorStudent.getMentorId())
                    .eq(MentorStudent::getStudentStatus, 1)
                    .eq(MentorStudent::getRound, currentRound); // 只查询当前轮次的学生
            List<MentorStudent> list = mentorStudentService.list(queryWrapper);
            List<SelectionDTO> result = new ArrayList<>();

            // 先查询所有已被接受的学生ID
            LambdaQueryWrapper<MentorStudent> acceptedWrapper = new LambdaQueryWrapper<>();
            acceptedWrapper.eq(MentorStudent::getTeacherStatus, 1)
                    .eq(MentorStudent::getStudentStatus, 1);
            List<MentorStudent> acceptedList = mentorStudentService.list(acceptedWrapper);
            List<Long> acceptedStudentIds = new ArrayList<>();
            for (MentorStudent ms : acceptedList) {
                if (!acceptedStudentIds.contains(ms.getStudentId())) {
                    acceptedStudentIds.add(ms.getStudentId());
                }
            }
            System.out.println("已被接受的学生ID列表: " + acceptedStudentIds);

            for (MentorStudent ms : list) {
                // 如果该学生已被某个导师接受，则跳过
                if (acceptedStudentIds.contains(ms.getStudentId())) {
                    System.out.println("学生 " + ms.getStudentId() + " 已被接受，跳过");
                    continue;
                }

                SelectionDTO dto = new SelectionDTO();
                dto.setId(ms.getId());
                dto.setStudentId(ms.getStudentId());
                dto.setMentorId(ms.getMentorId());
                dto.setTeacherStatus(ms.getTeacherStatus());
                dto.setRound(ms.getRound());

                Student student = studentService.getById(ms.getStudentId());
                if (student != null) {
                    dto.setStudentName(student.getStudentName());
                    dto.setStudentNo(student.getStudentNo());
                    dto.setDepartment(student.getDepartment());
                    dto.setMajor(student.getMajor());
                    dto.setAdmissionYear(student.getAdmissionYear());
                    dto.setUserId(student.getUserId());
                }

                System.out.println("返回记录: id=" + ms.getId() + ", mentorId=" + ms.getMentorId() + ", studentId=" + ms.getStudentId() + ", round=" + ms.getRound());
                result.add(dto);
            }
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // 导师提交选中
    @RequestMapping("/submitSelection")
    public String submitSelection(@RequestBody MentorStudent mentorStudent){
        try {
            System.out.println("导师提交选中，接收到的参数: " + mentorStudent);

            // 获取原始记录以确定是否为确认状态变化
            MentorStudent originalRecord = null;

            // 首先尝试通过 ID 查询
            if (mentorStudent.getId() != null) {
                originalRecord = mentorStudentService.getById(mentorStudent.getId());
                System.out.println("通过 ID 查询结果: " + originalRecord);
            }

            // 如果 ID 查询失败，尝试通过 mentorId + studentId 复合条件查询
            if (originalRecord == null && mentorStudent.getMentorId() != null && mentorStudent.getStudentId() != null) {
                System.out.println("ID 查询失败，尝试通过 mentorId + studentId 查询");
                LambdaQueryWrapper<MentorStudent> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(MentorStudent::getMentorId, mentorStudent.getMentorId())
                        .eq(MentorStudent::getStudentId, mentorStudent.getStudentId())
                        .eq(MentorStudent::getStudentStatus, 1);
                originalRecord = mentorStudentService.getOne(queryWrapper);
                System.out.println("复合条件查询结果: " + originalRecord);
            }

            if (originalRecord == null) {
                System.out.println("无法找到记录，返回错误");
                return jsonReturn.returnError("记录不存在");
            }

            // 校验：不允许退选已确认的学生
            if (originalRecord.getTeacherStatus() == 1 && mentorStudent.getTeacherStatus() == 2) {
                return jsonReturn.returnError("已确认的学生不可退选");
            }

            // 校验：状态未变化时不执行任何操作
            if (originalRecord.getTeacherStatus() == mentorStudent.getTeacherStatus()) {
                return jsonReturn.returnSuccess();
            }

            // 校验：仅允许从0（待处理）变为1（同意）或2（拒绝）
            if (originalRecord.getTeacherStatus() != 0) {
                return jsonReturn.returnError("不允许的状态变更");
            }

            // 更新导师-学生关系表 - 使用 originalRecord 的 ID 确保准确
            System.out.println("准备更新记录，使用 ID: " + originalRecord.getId());
            UpdateWrapper<MentorStudent> selectionWrapper = new UpdateWrapper<>();
            selectionWrapper.eq("id", originalRecord.getId())
                    .set("teacher_status", mentorStudent.getTeacherStatus())
                    .set("confirm_time", LocalDateTime.now());
            boolean updateResult = mentorStudentService.update(null, selectionWrapper);

            System.out.println("更新 mentor_student 结果: " + updateResult);
            if (!updateResult) {
                return jsonReturn.returnFailed("更新选中状态失败");
            }

            // 根据 teacherStatus 调整导师已确认名额（仅从0变为1时增加名额）
            int quotaChange = 0;
            if (mentorStudent.getTeacherStatus() == 1 && originalRecord.getTeacherStatus() == 0) {
                // 学生被确认选中，已确认名额加1
                quotaChange = 1;
            }

            if (quotaChange != 0) {
                // 查询当前导师的已确认名额 - 使用 originalRecord 的 mentorId
                Teacher currentTeacher = teacherService.getById(originalRecord.getMentorId());
                if (currentTeacher != null) {
                    int newConfirmedQuota = currentTeacher.getConfirmedQuota() + quotaChange;
                    System.out.println("更新导师已确认名额: " + currentTeacher.getConfirmedQuota() + " -> " + newConfirmedQuota);

                    // 更新导师表中的已确认名额
                    UpdateWrapper<Teacher> teacherWrapper = new UpdateWrapper<>();
                    teacherWrapper.eq("id", originalRecord.getMentorId())
                            .set("confirmed_quota", newConfirmedQuota);

                    boolean teacherUpdateResult = teacherService.update(null, teacherWrapper);
                    System.out.println("更新 teacher 表结果: " + teacherUpdateResult);
                    if (!teacherUpdateResult) {
                        return jsonReturn.returnError("更新导师已确认名额失败");
                    }
                }
            }

            // 如果是拒绝操作，自动将该学生的下一个志愿推进到下一轮
            if (mentorStudent.getTeacherStatus() == 2) {
                System.out.println("学生被拒绝，尝试推进到下一轮: studentId=" + originalRecord.getStudentId());
                selectionRoundService.advanceStudentToNextRound(originalRecord.getStudentId());
            }

            // 如果是同意操作，自动拒绝该学生的后续志愿
            if (mentorStudent.getTeacherStatus() == 1 && originalRecord.getTeacherStatus() == 0) {
                System.out.println("学生被同意，拒绝后续志愿: studentId=" + originalRecord.getStudentId());
                LambdaQueryWrapper<MentorStudent> studentChoicesWrapper = new LambdaQueryWrapper<>();
                studentChoicesWrapper.eq(MentorStudent::getStudentId, originalRecord.getStudentId())
                        .eq(MentorStudent::getStudentStatus, 1)
                        .orderByAsc(MentorStudent::getStudentChoiceOrder);
                List<MentorStudent> allChoices = mentorStudentService.list(studentChoicesWrapper);

                // 找到当前志愿的序号
                Integer currentOrder = originalRecord.getStudentChoiceOrder();

                // 拒绝后续所有志愿
                for (MentorStudent choice : allChoices) {
                    if (choice.getStudentChoiceOrder() != null && choice.getStudentChoiceOrder() > currentOrder) {
                        UpdateWrapper<MentorStudent> rejectWrapper = new UpdateWrapper<>();
                        rejectWrapper.eq("id", choice.getId())
                                .set("teacher_status", 2)
                                .set("confirm_time", LocalDateTime.now());
                        mentorStudentService.update(null, rejectWrapper);
                        System.out.println("已拒绝学生 " + originalRecord.getStudentId() + " 的志愿 " + choice.getStudentChoiceOrder());
                    }
                }
            }

            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

}
