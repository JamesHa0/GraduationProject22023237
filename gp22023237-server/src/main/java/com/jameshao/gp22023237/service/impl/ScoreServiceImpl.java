package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.Score;
import com.jameshao.gp22023237.service.ScoreService;
import com.jameshao.gp22023237.mapper.ScoreMapper;
import org.springframework.stereotype.Service;

/**
* @author test
* @description 针对表【score(成绩表)】的数据库操作Service实现
* @createDate 2025-10-08 17:09:36
*/
@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score>
    implements ScoreService{

}
