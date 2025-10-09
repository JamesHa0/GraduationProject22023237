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
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/mentor/selection")
public class MentorSelectionController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private MentorStudentService mentorStudentService;
    @Autowired
    private JSONReturn jsonReturn;

    // 导师查询可选学生
    @RequestMapping("/listStudents")
    public String listStudents(@RequestBody MentorStudent mentorStudent){
        try{
            System.out.println("导师查询可选学生:"+mentorStudent);
            LambdaQueryWrapper<MentorStudent> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MentorStudent::getMentorId, mentorStudent.getMentorId())
                    .eq(MentorStudent::getStudentStatus, 1);
            List<MentorStudent> list = mentorStudentService.list(queryWrapper);
            List<SelectionDTO> result = new ArrayList<>();
            for (MentorStudent ms : list) {
                SelectionDTO dto = new SelectionDTO();
                dto.setStudentId(ms.getStudentId());

                Student student = studentService.getById(ms.getStudentId());
                if (student != null) {
                    dto.setStudentName(student.getStudentName());
                    dto.setStudentNo(student.getStudentNo());
                    dto.setDepartment(student.getDepartment());
                    dto.setMajor(student.getMajor());
                    dto.setAdmissionYear(student.getAdmissionYear());
                }

                result.add(dto);
            }
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // 查询学生已选志愿
//    @RequestMapping("/getStudentChoices")
//    public String getStudentChoices(String studentId){
//        try{
//            System.out.println("查询学生已选志愿:"+studentId);
//            LambdaQueryWrapper<MentorStudent> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.eq(MentorStudent::getStudentId, studentId)
//                    .eq(MentorStudent::getStudentStatus, 1);
//
//            // 使用MyBatis Plus的关联查询
//            List<MentorStudent> mentorStudents = mentorStudentService.list(queryWrapper);
//
//            List<SelectionDTO> result = new ArrayList<>();
//
//            // 关联查询Teacher表获取导师姓名
//            for (MentorStudent ms : mentorStudents) {
//                SelectionDTO dto = new SelectionDTO();
//                dto.setMentorId(ms.getMentorId());
//                dto.setStudentChoiceOrder(ms.getStudentChoiceOrder());
//
//                Teacher teacher = teacherService.getById(ms.getMentorId());
//                if (teacher != null) {
//                    dto.setMentorName(teacher.getTeacherName());
//                }
//
//                result.add(dto);
//            }
//            return jsonReturn.returnSuccess(result);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return jsonReturn.returnError(e.getMessage());
//        }
//    }

     // 学生提交选择
//    @RequestMapping("/studentSubmit")
//    public String submitSelection(@RequestBody MentorStudent mentorStudent){
//        try {
//            System.out.println("学生提交选择:"+mentorStudent);
//            MentorStudent mentorStudentRecord = getMentorStudent(mentorStudent);
//
//            // 使用MyBatis Plus的saveOrUpdate方法
//            boolean success = mentorStudentService.saveOrUpdate(mentorStudentRecord);
//
//            if (success) {
//                return jsonReturn.returnSuccess("提交成功");
//            } else {
//                return jsonReturn.returnError("提交失败");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return jsonReturn.returnError(e.getMessage());
//        }
//    }

    // 获取MentorStudent对象
//    private static MentorStudent getMentorStudent(MentorStudent mentorStudent) {
//        int round = mentorStudent.getRound();
//        int studentChoiceOrder = mentorStudent.getStudentChoiceOrder();
//        long studentId = mentorStudent.getStudentId();
//        long mentorId = mentorStudent.getMentorId();
//
//        // 创建单个MentorStudent对象
//        MentorStudent mentorStudentRecord = new MentorStudent();
//        mentorStudentRecord.setStudentChoiceOrder(studentChoiceOrder);
//        mentorStudentRecord.setRound(round);
//        mentorStudentRecord.setStudentId(studentId);
//        mentorStudentRecord.setMentorId(mentorId);
//
//        // 设置其他必要字段
//        mentorStudentRecord.setStudentStatus(1);
//        mentorStudentRecord.setSelectionTime(new Date());
//        return mentorStudentRecord;
//    }

    // 学生删除志愿
//    @RequestMapping("/studentDeselect")
//    public String studentDeselect(@RequestBody MentorStudent mentorStudent){
//        try {
//            System.out.println("删除学生志愿:"+mentorStudent);
//            MentorStudent mentorStudentRecord = getMentorStudent(mentorStudent);
//            mentorStudentRecord.setStudentStatus(0);
//            mentorStudentRecord.setUpdateTime(new Date());
//
//            UpdateWrapper<MentorStudent> updateWrapper = new UpdateWrapper<>();
//            updateWrapper.eq("student_id", mentorStudentRecord.getStudentId())
//                    .eq("mentor_id", mentorStudentRecord.getMentorId())
//                    .eq("round", mentorStudentRecord.getRound())
//                    .eq("student_choice_order", mentorStudentRecord.getStudentChoiceOrder());
//            boolean success = mentorStudentService.update(mentorStudentRecord, updateWrapper);
//            if (success) {
//                return jsonReturn.returnSuccess("取消成功");
//            } else {
//                return jsonReturn.returnError("取消失败");
//            }
//        } catch (Exception e) {
//        e.printStackTrace();
//        return jsonReturn.returnError(e.getMessage());
//    }
//    }
}
