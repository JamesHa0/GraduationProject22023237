package com.jameshao.gp22023237.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.Post;
import com.jameshao.gp22023237.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/post")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 查询岗位列表
     */
    @RequestMapping("/list")
    public String list(Integer pageNum, Integer pageSize, String postCode, String postName, String status) {
        try {
            Page<Post> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
            LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(!ObjectUtils.isEmpty(postCode), Post::getPostCode, postCode)
                    .like(!ObjectUtils.isEmpty(postName), Post::getPostName, postName)
                    .eq(!ObjectUtils.isEmpty(status), Post::getStatus, status);
            Page<Post> result = postService.page(page, queryWrapper);
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
     * 获取岗位详细信息
     */
    @RequestMapping("/{postId}")
    public String getInfo(@PathVariable Long postId) {
        try {
            Post post = postService.getById(postId);
            return jsonReturn.returnSuccess(post);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 新增岗位
     */
    @PostMapping
    public String add(@RequestBody Post post) {
        try {
            post.setCreateTime(new Date());
            post.setUpdateTime(new Date());
            postService.save(post);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 修改岗位
     */
    @PutMapping
    public String edit(@RequestBody Post post) {
        try {
            post.setUpdateTime(new Date());
            postService.updateById(post);
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 删除岗位
     */
    @DeleteMapping("/{postIds}")
    public String remove(@PathVariable Long[] postIds) {
        try {
            for (Long postId : postIds) {
                postService.removeById(postId);
            }
            return jsonReturn.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 查询所有岗位
     */
    @RequestMapping("/listAll")
    public String listAll() {
        try {
            List<Post> list = postService.list();
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
