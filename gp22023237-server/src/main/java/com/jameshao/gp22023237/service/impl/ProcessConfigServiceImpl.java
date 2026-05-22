package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.ProcessConfigMapper;
import com.jameshao.gp22023237.po.ProcessConfig;
import com.jameshao.gp22023237.po.Student;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.service.NoticeService;
import com.jameshao.gp22023237.service.ProcessConfigService;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.TeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ProcessConfigServiceImpl extends ServiceImpl<ProcessConfigMapper, ProcessConfig>
        implements ProcessConfigService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessConfigServiceImpl.class);

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSaveConfigs(List<ProcessConfig> configs) {
        for (ProcessConfig config : configs) {
            if (config.getId() != null) {
                ProcessConfig existing = getById(config.getId());
                boolean deadlineChanged = existing != null
                        && !Objects.equals(existing.getDeadline(), config.getDeadline());
                updateById(config);
                if (deadlineChanged) {
                    notifyDeadlineChange(config);
                }
            } else {
                save(config);
            }
        }
        return true;
    }

    /**
     * 论文流程截止时间变更 → 通知全体学生和导师
     * 使用 notificationKey 防止同一配置重复推送
     */
    private void notifyDeadlineChange(ProcessConfig config) {
        try {
            String processName = config.getProcessName() != null ? config.getProcessName() : "未知环节";
            String deadlineStr = config.getDeadline() != null
                    ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(config.getDeadline())
                    : "未设置";
            String notificationKey = "thesis:deadline:" + config.getId() + ":"
                    + (config.getDeadline() != null ? config.getDeadline().getTime() : "null");

            // 通知全体学生
            List<Student> students = studentService.list();
            List<Long> studentUserIds = new ArrayList<>();
            for (Student s : students) {
                if (s.getUserId() != null) studentUserIds.add(s.getUserId());
            }
            if (!studentUserIds.isEmpty()) {
                noticeService.createAndPushToUsersWithKey(notificationKey, studentUserIds,
                        "截止时间变更通知",
                        "论文【" + processName + "】截止时间已调整为 " + deadlineStr, "1");
            }

            // 通知导师
            List<Teacher> mentors = teacherService.list(
                    new LambdaQueryWrapper<Teacher>().eq(Teacher::getIsMentor, 1));
            List<Long> teacherUserIds = new ArrayList<>();
            for (Teacher t : mentors) {
                if (t.getUserId() != null) teacherUserIds.add(t.getUserId());
            }
            if (!teacherUserIds.isEmpty()) {
                // 导师使用不同的 notificationKey 前缀，避免与学生通知互相去重
                noticeService.createAndPushToUsersWithKey("mentor_" + notificationKey, teacherUserIds,
                        "截止时间变更通知",
                        "论文【" + processName + "】截止时间已调整为 " + deadlineStr, "1");
            }
        } catch (Exception e) {
            logger.warn("论文截止时间变更通知推送失败: {}", e.getMessage());
        }
    }
}
