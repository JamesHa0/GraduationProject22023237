package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.service.TeacherService;
import com.jameshao.gp22023237.mapper.TeacherMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author test
* @description 针对表【teacher(教师信息表)】的数据库操作Service实现
* @createDate 2026-01-20 13:12:32
*/
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher>
    implements TeacherService{

    @Override
    public IPage<Map<String, Object>> pageQuotaList(Page<Map<String, Object>> page, String teacherName, String department) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getIsMentor, 1);
        if (teacherName != null && !teacherName.isEmpty()) {
            wrapper.like(Teacher::getTeacherName, teacherName);
        }
        if (department != null && !department.isEmpty()) {
            wrapper.like(Teacher::getDepartment, department);
        }
        wrapper.orderByAsc(Teacher::getTeacherNo);

        Page<Teacher> teacherPage = new Page<>(page.getCurrent(), page.getSize());
        IPage<Teacher> result = this.page(teacherPage, wrapper);

        // 转换为Map格式，保持与其他Controller一致
        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();
        for (Teacher t : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("teacherName", t.getTeacherName());
            map.put("teacherNo", t.getTeacherNo());
            map.put("title", t.getTitle());
            map.put("department", t.getDepartment());
            map.put("researchField", t.getResearchField());
            map.put("quota", t.getQuota() != null ? t.getQuota() : 0);
            map.put("remainingQuota", t.getRemainingQuota() != null ? t.getRemainingQuota() : 0);
            map.put("confirmedQuota", t.getConfirmedQuota() != null ? t.getConfirmedQuota() : 0);
            records.add(map);
        }
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public boolean updateQuota(Long teacherId, Integer quota) {
        if (teacherId == null || quota == null) {
            throw new IllegalArgumentException("导师ID和名额不能为空");
        }
        if (quota < 0) {
            throw new IllegalArgumentException("名额不能为负数");
        }

        Teacher teacher = this.getById(teacherId);
        if (teacher == null) {
            throw new IllegalArgumentException("未找到该导师");
        }
        if (teacher.getIsMentor() == null || teacher.getIsMentor() != 1) {
            throw new IllegalArgumentException("该教师不是导师");
        }

        int confirmedQuota = teacher.getConfirmedQuota() != null ? teacher.getConfirmedQuota() : 0;
        int newRemaining = quota - confirmedQuota;
        if (newRemaining < 0) {
            throw new IllegalArgumentException("已确认名额为" + confirmedQuota + "，新名额不能小于已确认名额");
        }

        teacher.setQuota(quota);
        teacher.setRemainingQuota(newRemaining);
        return this.updateById(teacher);
    }

    @Override
    @Transactional
    public Map<String, Object> batchUpdateQuota(List<Long> teacherIds, Integer quota) {
        if (teacherIds == null || teacherIds.isEmpty()) {
            throw new IllegalArgumentException("请选择至少一位导师");
        }
        if (quota == null) {
            throw new IllegalArgumentException("名额不能为空");
        }
        if (quota < 0) {
            throw new IllegalArgumentException("名额不能为负数");
        }

        int successCount = 0;
        List<String> failedDetails = new ArrayList<>();

        for (Long teacherId : teacherIds) {
            try {
                boolean result = updateQuota(teacherId, quota);
                if (result) {
                    successCount++;
                } else {
                    Teacher t = this.getById(teacherId);
                    String name = t != null ? t.getTeacherName() : String.valueOf(teacherId);
                    failedDetails.add(name + ": 更新失败");
                }
            } catch (IllegalArgumentException e) {
                Teacher t = this.getById(teacherId);
                String name = t != null ? t.getTeacherName() : String.valueOf(teacherId);
                failedDetails.add(name + ": " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", teacherIds.size() - successCount);
        result.put("failedDetails", failedDetails);
        return result;
    }

    @Override
    @Transactional
    public boolean resetAllQuota() {
        LambdaUpdateWrapper<Teacher> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Teacher::getIsMentor, 1)
               .set(Teacher::getQuota, 0)
               .set(Teacher::getRemainingQuota, 0)
               .set(Teacher::getConfirmedQuota, 0);
        return this.update(wrapper);
    }
}
