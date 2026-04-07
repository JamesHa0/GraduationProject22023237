package com.jameshao.gp22023237.controller.student;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.DTO.MentorChangeApplicationWithDetailsDTO;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.mapper.MentorChangeApplicationMapper;
import com.jameshao.gp22023237.po.MentorChangeApplication;
import com.jameshao.gp22023237.service.MentorChangeApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student/mentor-change")
public class MentorChangeController {

    @Autowired
    private MentorChangeApplicationService mentorChangeApplicationService;

    @Autowired
    private MentorChangeApplicationMapper mentorChangeApplicationMapper;

    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 提交导师更换申请
     */
    @PostMapping("/submit")
    public String submitApplication(@RequestBody MentorChangeApplication application) {
        try {
            boolean success = mentorChangeApplicationService.submitApplication(application);
            if (success) {
                return jsonReturn.returnSuccess("申请提交成功");
            } else {
                return jsonReturn.returnFailed("申请提交失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 查询导师更换申请列表（带详情）
     */
    @GetMapping("/list")
    public String getChangeList(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) Long studentId,
                                 @RequestParam(required = false) Integer overallStatus,
                                 @RequestParam(required = false) String studentNo,
                                 @RequestParam(required = false) String studentName) {
        try {
            List<MentorChangeApplicationWithDetailsDTO> list = mentorChangeApplicationMapper.listWithDetails(
                    studentId, studentNo, studentName, overallStatus);

            // 手动分页
            int total = list.size();
            int fromIndex = Math.min((pageNum - 1) * pageSize, total);
            int toIndex = Math.min(fromIndex + pageSize, total);
            List<MentorChangeApplicationWithDetailsDTO> pageList = list.subList(fromIndex, toIndex);

            Map<String, Object> result = new HashMap<>();
            result.put("records", pageList);
            result.put("total", total);
            result.put("size", pageSize);
            result.put("current", pageNum);

            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取导师更换申请详情（带学生和导师信息）
     */
    @GetMapping("/{id}")
    public String getChangeDetail(@PathVariable Long id) {
        try {
            MentorChangeApplicationWithDetailsDTO application = mentorChangeApplicationMapper.getDetailWithDetails(id);
            if (application != null) {
                return jsonReturn.returnSuccess(application);
            } else {
                return jsonReturn.returnFailed("未找到申请记录");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 原导师审批
     */
    @PostMapping("/original-mentor/approve")
    public String originalMentorApprove(@RequestParam Long id,
                                         @RequestParam Integer status,
                                         @RequestParam(required = false) String comment) {
        try {
            boolean success = mentorChangeApplicationService.originalMentorApprove(id, status, comment);
            if (success) {
                return jsonReturn.returnSuccess("审批成功");
            } else {
                return jsonReturn.returnFailed("审批失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 新导师审批
     */
    @PostMapping("/new-mentor/approve")
    public String newMentorApprove(@RequestParam Long id,
                                      @RequestParam Integer status,
                                      @RequestParam(required = false) String comment) {
        try {
            boolean success = mentorChangeApplicationService.newMentorApprove(id, status, comment);
            if (success) {
                return jsonReturn.returnSuccess("审批成功");
            } else {
                return jsonReturn.returnFailed("审批失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
