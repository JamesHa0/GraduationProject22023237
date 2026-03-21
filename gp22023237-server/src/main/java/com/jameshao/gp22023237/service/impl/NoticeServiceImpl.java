package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.Notice;
import com.jameshao.gp22023237.service.NoticeService;
import com.jameshao.gp22023237.mapper.NoticeMapper;
import org.springframework.stereotype.Service;

/**
* @author test
* @description 针对表【sys_notice(通知公告表)】的数据库操作Service实现
* @createDate 2025-01-01 00:00:00
*/
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice>
    implements NoticeService{

}
