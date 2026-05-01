package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.NoticeRead;
import com.jameshao.gp22023237.service.NoticeReadService;
import com.jameshao.gp22023237.mapper.NoticeReadMapper;
import org.springframework.stereotype.Service;

/**
 * 针对表【sys_notice_read(公告阅读记录表)】的数据库操作Service实现
 */
@Service
public class NoticeReadServiceImpl extends ServiceImpl<NoticeReadMapper, NoticeRead>
    implements NoticeReadService {

}
