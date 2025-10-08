package com.jameshao.gp22023237.controller.selection;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/selection")
public class SelectionController {

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
                    .gt(Teacher::getRemainingQuota, 0);
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
            System.out.println("-------------------------"+studentId);
            LambdaQueryWrapper<MentorStudent> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MentorStudent::getStudentId, studentId);

            // 使用MyBatis Plus的关联查询
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
            System.out.println("-------------------------"+mentorStudent);
            int round = mentorStudent.getRound();
            int studentChoiceOrder = mentorStudent.getStudentChoiceOrder();
            long studentId = mentorStudent.getStudentId();
            long mentorId = mentorStudent.getMentorId();

            // 创建单个MentorStudent对象
            MentorStudent mentorStudentRecord = new MentorStudent();
            mentorStudentRecord.setStudentChoiceOrder(studentChoiceOrder);
            mentorStudentRecord.setRound(round);
            mentorStudentRecord.setStudentId(studentId);
            mentorStudentRecord.setMentorId(mentorId);

            // 设置其他必要字段
            mentorStudentRecord.setStudentStatus(1);
            mentorStudentRecord.setSelectionTime(new Date());

            // 使用MyBatis Plus的saveOrUpdate方法
            boolean success = mentorStudentService.saveOrUpdate(mentorStudentRecord);

            if (success) {
                return jsonReturn.returnSuccess("提交成功");
            } else {
                return jsonReturn.returnError("提交失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
