package com.jameshao.gp22023237.controller.student.selection;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.jameshao.gp22023237.DTO.SelectionDTO;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.po.MentorStudent;
import com.jameshao.gp22023237.service.MentorStudentService;
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
    private JSONReturn jsonReturn;

    // 学生查询可选导师
    @RequestMapping("/listMentor")
    public String listMentor(String name){
        try{
            LambdaQueryWrapper<Teacher> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(!ObjectUtils.isEmpty(name),Teacher::getTeacherName, name)
                    .eq(Teacher::getIsMentor, 1)
                    .gt(Teacher::getQuota, 0);
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

            // 关联查询Teacher表获取导师姓名
            for (MentorStudent ms : mentorStudents) {
                SelectionDTO dto = new SelectionDTO();
                dto.setMentorId(ms.getMentorId());
                dto.setStudentChoiceOrder(ms.getStudentChoiceOrder());

                Teacher teacher = teacherService.getById(ms.getMentorId());
                if (teacher != null) {
                    dto.setMentorName(teacher.getTeacherName());
                }

                result.add(dto);
            }
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

     // 学生提交选择
    @RequestMapping("/studentSubmit")
    public String submitSelection(@RequestBody MentorStudent mentorStudent){
        try {
            System.out.println("学生提交选择:"+mentorStudent);
            // 首先检查是否已经存在该学生的记录（除了StudentStatus字段）
            LambdaQueryWrapper<MentorStudent> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MentorStudent::getStudentId, mentorStudent.getStudentId())
                    .eq(MentorStudent::getMentorId, mentorStudent.getMentorId());

            MentorStudent existingRecord = mentorStudentService.getOne(queryWrapper);

            boolean success = false;
            if (existingRecord != null) {
                // 如果存在记录，更新StudentStatus字段
                UpdateWrapper<MentorStudent> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("student_id", mentorStudent.getStudentId())
                        .eq("mentor_id", mentorStudent.getMentorId())
                        .set("student_status", mentorStudent.getStudentStatus());

                success = mentorStudentService.update(updateWrapper);
            } else {
                // 如果不存在记录，插入新的数据
                mentorStudent.setSelectionTime(new Date());
                success = mentorStudentService.save(mentorStudent);
            }

            if (!success) {
                return jsonReturn.returnError("提交失败");
            }



            // 根据 StudentStatus 调整导师剩余名额
            int quotaChange = 0;
            if (mentorStudent.getStudentStatus() == 1) {
                // 选中，导师剩余名额减1
                quotaChange = -1;
            } else if (mentorStudent.getStudentStatus() == 0) {
                // 取消选中，导师剩余名额加1
                quotaChange = +1;
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

}
