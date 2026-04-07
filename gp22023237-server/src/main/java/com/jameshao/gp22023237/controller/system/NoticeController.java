package com.jameshao.gp22023237.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.Notice;
import com.jameshao.gp22023237.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/system/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;
    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 查询通知公告列表
     */
    @RequestMapping("/list")
    public String list(Integer pageNum, Integer pageSize, String noticeTitle, String createBy, String status) {
        try {
            Page<Notice> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
            LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(!ObjectUtils.isEmpty(noticeTitle), Notice::getNoticeTitle, noticeTitle)
                    .like(!ObjectUtils.isEmpty(createBy), Notice::getCreateBy, createBy)
                    .eq(!ObjectUtils.isEmpty(status), Notice::getStatus, status);
            Page<Notice> result = noticeService.page(page, queryWrapper);
            Map<String, Object> data = new HashMap<>();
            data.put("rows", result.getRecords());
            data.put("total", result.getTotal());
            return jsonReturn.returnSuccess(data);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取通知公告详细信息
     */
    @RequestMapping("/{noticeId}")
    public String getInfo(@PathVariable Long noticeId) {
        try {
            Notice notice = noticeService.getById(noticeId);
            return jsonReturn.returnSuccess(notice);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 新增通知公告
     */
    @PostMapping
    public String add(@RequestBody Notice notice) {
        try {
            notice.setCreateTime(new Date());
            notice.setUpdateTime(new Date());
            noticeService.save(notice);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 修改通知公告
     */
    @PutMapping
    public String edit(@RequestBody Notice notice) {
        try {
            notice.setUpdateTime(new Date());
            noticeService.updateById(notice);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 删除通知公告
     */
    @DeleteMapping("/{noticeIds}")
    public String remove(@PathVariable Long[] noticeIds) {
        try {
            for (Long noticeId : noticeIds) {
                noticeService.removeById(noticeId);
            }
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
