package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.po.Menu;
import com.jameshao.gp22023237.service.MenuService;
import com.jameshao.gp22023237.mapper.MenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author test
* @description 针对表【menu】的数据库操作Service实现
* @createDate 2025-09-21 16:57:09
*/
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu>
    implements MenuService{

    @Autowired
    MenuMapper menuMapper;

    @Override
    public List<Menu> getByroleid(Integer roleid) {
        return menuMapper.selectbyroleid(roleid);
    }
}




