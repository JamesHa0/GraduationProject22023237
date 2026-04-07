package com.jameshao.gp22023237.controller.selection;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.MentorStudent;
import com.jameshao.gp22023237.service.MentorStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 导师学生关系管理控制器
 * 用于管理学生与导师之间的关系，支持查看、新增、编辑、删除操作
 * 权限：
 * - 超级管理员(1)、综合管理员(4)、教学秘书(5)：可查看、可修改
 * - 分管院长(2)、学位分委会主席(3)：仅可查看
 * - 学生(6)、导师(7)、任课教师(8)：不可见
 */
@RestController
@RequestMapping("/selection/relationship")
public class MentorStudentRelationshipController {

    @Autowired
    private MentorStudentService mentorStudentService;

    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 分页查询导师学生关系列表
     */
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Long studentId,
                       @RequestParam(required = false) Long mentorId) {
        try {
            Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
            IPage<Map<String, Object>> result = mentorStudentService.pageRelationship(page, studentId, mentorId);
            return jsonReturn.returnSuccess(result);
        } catch (IllegalStateException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 查询单个关系详情
     */
    @GetMapping("/{id}")
    public String getDetail(@PathVariable Long id) {
        try {
            MentorStudent relationship = mentorStudentService.getById(id);
            return relationship != null ? jsonReturn.returnSuccess(relationship) : jsonReturn.returnError("未找到记录");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 创建导师学生关系
     */
    @PostMapping("/create")
    public String create(@RequestBody MentorStudent mentorStudent) {
        try {
            boolean success = mentorStudentService.createRelationship(mentorStudent);
            return success ? jsonReturn.returnSuccess("创建成功") : jsonReturn.returnError("创建失败");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新导师学生关系
     */
    @PostMapping("/update")
    public String update(@RequestBody MentorStudent mentorStudent) {
        try {
            boolean success = mentorStudentService.updateRelationship(mentorStudent);
            return success ? jsonReturn.returnSuccess("更新成功") : jsonReturn.returnError("更新失败");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除导师学生关系
     */
    @PostMapping("/delete")
    public String delete(@RequestParam Long id) {
        try {
            boolean success = mentorStudentService.deleteRelationship(id);
            return success ? jsonReturn.returnSuccess("删除成功") : jsonReturn.returnError("删除失败");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取可选学生列表（还没有确定导师的学生）
     */
    @GetMapping("/students")
    public String listStudents() {
        try {
            List<Map<String, Object>> students = mentorStudentService.listAvailableStudents();
            return jsonReturn.returnSuccess(students);
        } catch (IllegalStateException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取可选导师列表（还有剩余名额的导师）
     */
    @GetMapping("/mentors")
    public String listMentors() {
        try {
            List<Map<String, Object>> mentors = mentorStudentService.listAvailableMentors();
            return jsonReturn.returnSuccess(mentors);
        } catch (IllegalStateException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取学生当前的导师信息
     */
    @GetMapping("/student-current-mentor/{studentId}")
    public String getStudentCurrentMentor(@PathVariable Long studentId) {
        try {
            Map<String, Object> mentor = mentorStudentService.getStudentCurrentMentor(studentId);
            return jsonReturn.returnSuccess(mentor);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
