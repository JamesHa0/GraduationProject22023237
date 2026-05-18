package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.po.DegreeApplication;

public interface DegreeApplicationService extends IService<DegreeApplication> {

    boolean submitApplication(DegreeApplication application);

    boolean committeeApprove(Long id, Integer status, String comment, Long approverId);

    /**
     * 重新提交学位申请（分委驳回后允许修改内容重提）
     */
    boolean resubmitApplication(DegreeApplication application);

    boolean grantDegree(Long id, String certificateNo);

    /**
     * 获取学位申请详情（含学生信息+答辩信息）
     */
    DegreeApplication getDetailWithStudentInfo(Long id);

    /**
     * 录入答辩结果
     * 校验：记录存在 + 当前defenseResult为0（未答辩）才允许录入
     */
    boolean updateDefenseResult(Long id, Integer defenseResult, Double defenseScore,
                                String defenseCommitteeComment, String qaRecord);
}
