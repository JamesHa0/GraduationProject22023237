package com.jameshao.gp22023237.mapper;

import com.jameshao.gp22023237.po.Notice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 针对表【sys_notice(通知公告表)】的数据库操作Mapper
 */
public interface NoticeMapper extends BaseMapper<Notice> {

    /**
     * 查询用户未读通知列表
     */
    List<Notice> selectUnreadList(@Param("userId") Long userId, @Param("roleId") Integer roleId);

    /**
     * 查询用户未读通知数量
     */
    int selectUnreadCount(@Param("userId") Long userId, @Param("roleId") Integer roleId);

    /**
     * 分页查询用户可见的通知列表（含已读状态）
     */
    Page<Notice> selectUserNoticePage(Page<Notice> page, @Param("userId") Long userId, @Param("roleId") Integer roleId);
}
