package com.jameshao.gp22023237.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.annotation.Log;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.common.enums.BusinessType;
import com.jameshao.gp22023237.po.Notice;
import com.jameshao.gp22023237.po.NoticeRead;
import com.jameshao.gp22023237.service.NoticeReadService;
import com.jameshao.gp22023237.service.NoticeService;
import com.jameshao.gp22023237.utils.CurrentUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;
    @Autowired
    private NoticeReadService noticeReadService;
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
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
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
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
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
     * 删除通知公告（级联删除阅读记录）
     */
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public String remove(@PathVariable Long[] noticeIds) {
        try {
            for (Long noticeId : noticeIds) {
                // 级联删除阅读记录（数据库有ON DELETE CASCADE，此处双保险）
                LambdaQueryWrapper<NoticeRead> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(NoticeRead::getNoticeId, noticeId);
                noticeReadService.remove(wrapper);
                noticeService.removeById(noticeId);
            }
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取当前用户未读通知数量
     */
    @GetMapping("/unread-count")
    public String unreadCount() {
        try {
            Long userId = CurrentUserUtil.getCurrentUserId();
            Integer roleId = CurrentUserUtil.getCurrentRoleId();
            if (userId == null) {
                return jsonReturn.returnError("未登录");
            }
            int count = noticeService.getUnreadCount(userId, roleId);
            Map<String, Object> data = new HashMap<>();
            data.put("unreadCount", count);
            return jsonReturn.returnSuccess(data);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取当前用户未读通知列表
     */
    @GetMapping("/unread-list")
    public String unreadList() {
        try {
            Long userId = CurrentUserUtil.getCurrentUserId();
            Integer roleId = CurrentUserUtil.getCurrentRoleId();
            if (userId == null) {
                return jsonReturn.returnError("未登录");
            }
            List<Notice> list = noticeService.getUnreadList(userId, roleId);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 标记指定通知为已读
     */
    @PutMapping("/read/{noticeId}")
    public String markAsRead(@PathVariable Long noticeId) {
        try {
            Long userId = CurrentUserUtil.getCurrentUserId();
            if (userId == null) {
                return jsonReturn.returnError("未登录");
            }
            noticeService.markAsRead(userId, noticeId);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 标记当前用户所有通知为已读
     */
    @PutMapping("/read-all")
    public String markAllAsRead() {
        try {
            Long userId = CurrentUserUtil.getCurrentUserId();
            Integer roleId = CurrentUserUtil.getCurrentRoleId();
            if (userId == null) {
                return jsonReturn.returnError("未登录");
            }
            noticeService.markAllAsRead(userId, roleId);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 分页获取当前用户可见通知列表（含已读状态）
     */
    @GetMapping("/user-list")
    public String userList(Integer pageNum, Integer pageSize) {
        try {
            Long userId = CurrentUserUtil.getCurrentUserId();
            Integer roleId = CurrentUserUtil.getCurrentRoleId();
            if (userId == null) {
                return jsonReturn.returnError("未登录");
            }
            Page<Notice> result = noticeService.getUserNoticePage(
                    pageNum != null ? pageNum : 1,
                    pageSize != null ? pageSize : 15,
                    userId, roleId);
            Map<String, Object> data = new HashMap<>();
            data.put("rows", result.getRecords());
            data.put("total", result.getTotal());
            return jsonReturn.returnSuccess(data);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
