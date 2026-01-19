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
                dto.setId(ms.getId());
                dto.setStudentId(ms.getStudentId());
                dto.setTeacherStatus(ms.getTeacherStatus());

                Student student = studentService.getById(ms.getStudentId());
                if (student != null) {
                    dto.setStudentName(student.getStudentName());
                    dto.setStudentNo(student.getStudentNo());
                    dto.setDepartment(student.getDepartment());
                    dto.setMajor(student.getMajor());
                    dto.setAdmissionYear(student.getAdmissionYear());
                    dto.setUserId(student.getUserId());
                }

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
            UpdateWrapper<MentorStudent> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", mentorStudent.getId()).set("teacher_status", mentorStudent.getTeacherStatus()).set("confirm_time",  LocalDateTime.now());
            boolean update = mentorStudentService.update(null, updateWrapper);
            if (update) {
                return jsonReturn.returnSuccess();
            } else {
                return jsonReturn.returnFailed();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

}
