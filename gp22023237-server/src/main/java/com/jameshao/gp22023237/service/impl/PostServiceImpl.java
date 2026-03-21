package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.Post;
import com.jameshao.gp22023237.service.PostService;
import com.jameshao.gp22023237.mapper.PostMapper;
import org.springframework.stereotype.Service;

/**
* @author test
* @description 针对表【sys_post(岗位信息表)】的数据库操作Service实现
* @createDate 2025-01-01 00:00:00
*/
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
    implements PostService{

}
