package com.jameshao.gp22023237.service;

import com.jameshao.gp22023237.po.Menu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author test
* @description 针对表【menu】的数据库操作Service
* @createDate 2025-09-21 16:57:09
*/
public interface MenuService extends IService<Menu> {
    List<Menu> getByroleid(Integer roleid);
}
