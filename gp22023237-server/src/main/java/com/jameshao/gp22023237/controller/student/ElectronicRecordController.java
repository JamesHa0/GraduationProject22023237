package com.jameshao.gp22023237.controller.student;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.ElectronicRecord;
import com.jameshao.gp22023237.service.ElectronicRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 学生电子档案Controller
 */
@RestController
@RequestMapping("/student/electronicRecord")
public class ElectronicRecordController {

    @Autowired
    private ElectronicRecordService electronicRecordService;

    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 添加电子档案
     */
    @PostMapping("/add")
    public JSONReturn add(@RequestBody ElectronicRecord record) {
        try {
            record.setCreateTime(new Date());
            record.setUpdateTime(new Date());
            boolean success = electronicRecordService.save(record);
            if (success) {
                return jsonReturn.success("添加成功", record);
            } else {
                return jsonReturn.fail("添加失败");
            }
        } catch (Exception e) {
            return jsonReturn.fail("添加失败：" + e.getMessage());
        }
    }

    /**
     * 更新电子档案
     */
    @PostMapping("/update")
    public JSONReturn update(@RequestBody ElectronicRecord record) {
        try {
            record.setUpdateTime(new Date());
            boolean success = electronicRecordService.updateById(record);
            if (success) {
                return jsonReturn.success("更新成功");
            } else {
                return jsonReturn.fail("更新失败");
            }
        } catch (Exception e) {
            return jsonReturn.fail("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除电子档案
     */
    @PostMapping("/delete/{id}")
    public JSONReturn delete(@PathVariable Long id) {
        try {
            boolean success = electronicRecordService.removeById(id);
            if (success) {
                return jsonReturn.success("删除成功");
            } else {
                return jsonReturn.fail("删除失败");
            }
        } catch (Exception e) {
            return jsonReturn.fail("删除失败：" + e.getMessage());
        }
    }

    /**
     * 查询电子档案列表（分页）
     */
    @GetMapping("/list")
    public JSONReturn list(@RequestParam(defaultValue = "1") int pageNum,
                           @RequestParam(defaultValue = "10") int pageSize,
                           @RequestParam(required = false) Long studentId,
                           @RequestParam(required = false) Integer type) {
        try {
            QueryWrapper<ElectronicRecord> queryWrapper = new QueryWrapper<>();
            if (studentId != null) {
                queryWrapper.eq("student_id", studentId);
            }
            if (type != null) {
                queryWrapper.eq("type", type);
            }
            queryWrapper.orderByDesc("create_time");

            Page<ElectronicRecord> page = electronicRecordService.page(
                    new Page<>(pageNum, pageSize), queryWrapper);

            return jsonReturn.success("查询成功", page);
        } catch (Exception e) {
            return jsonReturn.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询详情
     */
    @GetMapping("/{id}")
    public JSONReturn getById(@PathVariable Long id) {
        try {
            ElectronicRecord record = electronicRecordService.getById(id);
            if (record != null) {
                return jsonReturn.success("查询成功", record);
            } else {
                return jsonReturn.fail("未找到档案记录");
            }
        } catch (Exception e) {
            return jsonReturn.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询学生的所有档案
     */
    @GetMapping("/student/{studentId}")
    public JSONReturn getByStudentId(@PathVariable Long studentId) {
        try {
            List<ElectronicRecord> records = electronicRecordService.list(
                    new QueryWrapper<ElectronicRecord>()
                            .eq("student_id", studentId)
                            .orderByDesc("create_time"));
            return jsonReturn.success("查询成功", records);
        } catch (Exception e) {
            return jsonReturn.fail("查询失败：" + e.getMessage());
        }
    }
}
