package com.jameshao.gp22023237.mapper;

import com.jameshao.gp22023237.po.Menu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author test
* @description 针对表【menu】的数据库操作Mapper
* @createDate 2025-09-21 16:57:09
* @Entity com.jameshao.gp22023237.po.Menu
*/
public interface MenuMapper extends BaseMapper<Menu> {
    List<Menu> selectbyroleid(Integer roleid);
}




