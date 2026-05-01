package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 公告阅读记录表
 * @TableName sys_notice_read
 */
@TableName(value = "sys_notice_read")
@Data
public class NoticeRead {
    /**
     * 阅读记录ID
     */
    @TableId(value = "read_id", type = IdType.AUTO)
    private Long readId;

    /**
     * 公告ID
     */
    @TableField(value = "notice_id")
    private Long noticeId;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 阅读时间
     */
    @TableField(value = "read_time")
    private Date readTime;
}
