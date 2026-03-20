package com.jameshao.gp22023237.controller.student.selection;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.jameshao.gp22023237.DTO.SelectionDTO;
import com.jameshao.gp22023237.DTO.BatchSelectionDTO;
import org.springframework.transaction.annotation.Transactional;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.po.MentorStudent;
import com.jameshao.gp22023237.service.MentorStudentService;
import com.jameshao.gp22023237.service.SelectionRoundService;
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
@RequestMapping("/student/selection")
public class StudentSelectionController {

    @Autowired
    private TeacherService teacherService;
    @Autowired
    private MentorStudentService mentorStudentService;
    @Autowired
    private SelectionRoundService selectionRoundService;
    @Autowired
    private JSONReturn jsonReturn;

    // 学生查询可选导师 - 补选阶段时，显示仍有名额的导师
    @RequestMapping("/listMentor")
    public String listMentor(String name){
        try{
            int currentRound = selectionRoundService.getCurrentRound();
            System.out.println("学生查询可选导师，当前轮次: " + currentRound);

            LambdaQueryWrapper<Teacher> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(!ObjectUtils.isEmpty(name),Teacher::getTeacherName, name)
                    .eq(Teacher::getIsMentor, 1);

            // 补选阶段（round=4）时，只显示还有剩余名额的导师
            if (currentRound == 4) {
                queryWrapper.gt(Teacher::getRemainingQuota, 0);
            } else {
                queryWrapper.gt(Teacher::getQuota, 0);
            }

            List<Teacher> list = teacherService.list(queryWrapper);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // 查询学生已选志愿
    @RequestMapping("/getStudentChoices")
    public String getStudentChoices(String studentId){
        try{
            System.out.println("查询学生已选志愿:"+studentId);
            LambdaQueryWrapper<MentorStudent> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MentorStudent::getStudentId, studentId)
                    .eq(MentorStudent::getStudentStatus, 1);

            List<MentorStudent> mentorStudents = mentorStudentService.list(queryWrapper);

            List<SelectionDTO> result = new ArrayList<>();

            // 关联查询Teacher表获取导师信息
            for (MentorStudent ms : mentorStudents) {
                SelectionDTO dto = new SelectionDTO();
                dto.setId(ms.getId());
                dto.setMentorId(ms.getMentorId());
                dto.setStudentChoiceOrder(ms.getStudentChoiceOrder());
                dto.setRound(ms.getRound());
                dto.setStudentStatus(ms.getStudentStatus());
                dto.setTeacherStatus(ms.getTeacherStatus());

                Teacher teacher = teacherService.getById(ms.getMentorId());
                if (teacher != null) {
                    dto.setMentorName(teacher.getTeacherName());
                    dto.setMentorTitle(teacher.getTitle());
                    dto.setMentorDepartment(teacher.getDepartment());
                    dto.setMentorResearchField(teacher.getResearchField());
                }

                result.add(dto);
            }
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

     // 学生提交选择 - 根据当前轮次设置round字段
    @RequestMapping("/studentSubmit")
    public String submitSelection(@RequestBody MentorStudent mentorStudent){
        try {
            System.out.println("学生提交选择:"+mentorStudent);

            // 获取当前轮次
            int currentRound = selectionRoundService.getCurrentRound();
            System.out.println("当前轮次: " + currentRound);

            // 如果前端没有设置round，使用当前轮次
            if (mentorStudent.getRound() == null) {
                mentorStudent.setRound(currentRound);
            }

            // 首先检查是否已经存在该学生的记录（除了 StudentStatus 字段）
            LambdaQueryWrapper<MentorStudent> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MentorStudent::getStudentId, mentorStudent.getStudentId())
                    .eq(MentorStudent::getMentorId, mentorStudent.getMentorId());

            // 使用 getOne(false) 避免抛出 TooManyResultsException
            MentorStudent existingRecord = mentorStudentService.getOne(queryWrapper, false);

            // 校验：不允许取消已提交的志愿
            if (existingRecord != null && existingRecord.getStudentStatus() == 1 && mentorStudent.getStudentStatus() == 0) {
                return jsonReturn.returnError("已提交的志愿不可取消");
            }

            boolean success = false;
            if (existingRecord != null) {
                // 如果存在记录，更新 StudentStatus 字段（仅允许从 0 改为 1）
                if (existingRecord.getStudentStatus() == 0 && mentorStudent.getStudentStatus() == 1) {
                    UpdateWrapper<MentorStudent> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("student_id", mentorStudent.getStudentId())
                            .eq("mentor_id", mentorStudent.getMentorId())
                            .set("student_status", mentorStudent.getStudentStatus())
                            .set("round", mentorStudent.getRound());

                    success = mentorStudentService.update(updateWrapper);
                } else if (existingRecord.getStudentStatus() == 1 && mentorStudent.getStudentStatus() == 1) {
                    // 已是已选状态，无需更新
                    success = true;
                } else {
                    return jsonReturn.returnError("不允许的状态变更");
                }
            } else {
                // 如果不存在记录，插入新的数据（仅允许studentStatus=1）
                if (mentorStudent.getStudentStatus() != 1) {
                    return jsonReturn.returnError("首次提交必须选择导师");
                }
                mentorStudent.setSelectionTime(new Date());
                success = mentorStudentService.save(mentorStudent);
            }

            if (!success) {
                return jsonReturn.returnError("提交失败");
            }



            // 根据 StudentStatus 调整导师剩余名额（仅当从0变为1时扣除名额）
            int quotaChange = 0;
            if (mentorStudent.getStudentStatus() == 1 && (existingRecord == null || existingRecord.getStudentStatus() == 0)) {
                // 选中，导师剩余名额减1
                quotaChange = -1;
            }

            if (quotaChange != 0) {
                // 查询当前导师的剩余名额
                Teacher currentTeacher = teacherService.getById(mentorStudent.getMentorId());
                if (currentTeacher != null) {
                    int newRemainingQuota = currentTeacher.getRemainingQuota() + quotaChange;

                    // 更新导师表中的剩余名额
                    UpdateWrapper<Teacher> teacherWrapper = new UpdateWrapper<>();
                    teacherWrapper.eq("id", mentorStudent.getMentorId())
                            .set("remaining_quota", newRemainingQuota);

                    boolean teacherUpdateResult = teacherService.update(null, teacherWrapper);
                    if (!teacherUpdateResult) {
                        return jsonReturn.returnError("更新导师剩余名额失败");
                    }
                }
            }

            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // 学生批量提交志愿 - 一次性提交所有志愿
    @RequestMapping("/batchSubmit")
    @Transactional(rollbackFor = Exception.class)
    public String batchSubmitSelection(@RequestBody BatchSelectionDTO batchDTO){
        try {
            System.out.println("学生批量提交志愿:" + batchDTO);

            // 验证参数
            if (batchDTO.getStudentId() == null) {
                return jsonReturn.returnError("学生ID不能为空");
            }
            if (batchDTO.getChoices() == null || batchDTO.getChoices().isEmpty()) {
                return jsonReturn.returnError("志愿列表不能为空");
            }

            // 获取当前轮次
            int currentRound = selectionRoundService.getCurrentRound();
            System.out.println("当前轮次: " + currentRound);

            // 检查该学生是否已经提交过志愿
            LambdaQueryWrapper<MentorStudent> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(MentorStudent::getStudentId, batchDTO.getStudentId())
                    .eq(MentorStudent::getStudentStatus, 1);
            long existingCount = mentorStudentService.count(checkWrapper);
            if (existingCount > 0) {
                return jsonReturn.returnError("您已提交过志愿，不可重复提交");
            }

            Date now = new Date();
            List<Long> mentorIds = new ArrayList<>();

            // 验证并准备数据
            for (BatchSelectionDTO.ChoiceItem choice : batchDTO.getChoices()) {
                if (choice.getMentorId() == null) {
                    return jsonReturn.returnError("第" + choice.getStudentChoiceOrder() + "志愿的导师不能为空");
                }
                if (choice.getStudentChoiceOrder() == null) {
                    return jsonReturn.returnError("志愿顺序不能为空");
                }
                // 检查是否重复选择同一位导师
                if (mentorIds.contains(choice.getMentorId())) {
                    return jsonReturn.returnError("不能重复选择同一位导师");
                }
                mentorIds.add(choice.getMentorId());

                // 检查导师是否存在且有名额
                Teacher teacher = teacherService.getById(choice.getMentorId());
                if (teacher == null) {
                    return jsonReturn.returnError("第" + choice.getStudentChoiceOrder() + "志愿的导师不存在");
                }
                if (teacher.getRemainingQuota() <= 0) {
                    return jsonReturn.returnError("导师 " + teacher.getTeacherName() + " 已无剩余名额");
                }
            }

            // 保存所有志愿记录
            for (BatchSelectionDTO.ChoiceItem choice : batchDTO.getChoices()) {
                MentorStudent mentorStudent = new MentorStudent();
                mentorStudent.setStudentId(batchDTO.getStudentId());
                mentorStudent.setMentorId(choice.getMentorId());
                mentorStudent.setStudentChoiceOrder(choice.getStudentChoiceOrder());
                mentorStudent.setRound(currentRound);
                mentorStudent.setStudentStatus(1);
                mentorStudent.setTeacherStatus(0);
                mentorStudent.setSelectionTime(now);

                boolean saveResult = mentorStudentService.save(mentorStudent);
                if (!saveResult) {
                    throw new RuntimeException("保存第" + choice.getStudentChoiceOrder() + "志愿失败");
                }
            }

            // 扣除所有所选导师的剩余名额
            for (Long mentorId : mentorIds) {
                Teacher currentTeacher = teacherService.getById(mentorId);
                if (currentTeacher != null) {
                    int newRemainingQuota = currentTeacher.getRemainingQuota() - 1;
                    UpdateWrapper<Teacher> teacherWrapper = new UpdateWrapper<>();
                    teacherWrapper.eq("id", mentorId)
                            .set("remaining_quota", newRemainingQuota);
                    boolean teacherUpdateResult = teacherService.update(null, teacherWrapper);
                    if (!teacherUpdateResult) {
                        throw new RuntimeException("更新导师剩余名额失败");
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
