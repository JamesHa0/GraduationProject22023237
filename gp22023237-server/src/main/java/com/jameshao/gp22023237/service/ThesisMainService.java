package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.po.ThesisMain;

public interface ThesisMainService extends IService<ThesisMain> {

    /**
     * 根据学生ID获取论文主记录，不存在则返回null
     */
    ThesisMain getByStudentId(Long studentId);

    /**
     * 根据学生ID获取或创建论文主记录
     */
    ThesisMain getOrCreateByStudentId(Long studentId);

    /**
     * 更新论文题目
     */
    boolean updateThesisTitle(Long id, String thesisTitle);

    /**
     * 更新最终论文路径
     */
    boolean updateThesisFinalUrl(Long id, String thesisFinalUrl);

    /**
     * 归档论文
     */
    boolean archiveThesis(Long id);

    /**
     * 更新论文最终结果
     */
    boolean updateFinalResult(Long id, Integer finalResult);

    /**
     * 批量归档论文
     */
    boolean batchArchiveThesis(java.util.List<Long> ids);
}
